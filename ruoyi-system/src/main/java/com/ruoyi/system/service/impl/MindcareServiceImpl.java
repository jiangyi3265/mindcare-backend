package com.ruoyi.system.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.MindcareClient;
import com.ruoyi.system.domain.MindcareContent;
import com.ruoyi.system.domain.MindcareRecord;
import com.ruoyi.system.mapper.MindcareMapper;
import com.ruoyi.system.service.IMindcareService;

@Service
public class MindcareServiceImpl implements IMindcareService
{
    private static final int MAX_PAYLOAD_LENGTH = 20000;

    @Autowired
    private MindcareMapper mapper;

    @Override
    public List<MindcareContent> selectContentList(MindcareContent content)
    {
        return mapper.selectContentList(content);
    }

    @Override
    public MindcareContent selectContentById(Long contentId)
    {
        return mapper.selectContentById(contentId);
    }

    @Override
    public int insertContent(MindcareContent content)
    {
        validateContent(content);
        if (content.getStatus() == null)
        {
            content.setStatus("0");
        }
        if (content.getSortOrder() == null)
        {
            content.setSortOrder(0);
        }
        return mapper.insertContent(content);
    }

    @Override
    public int updateContent(MindcareContent content)
    {
        validateContent(content);
        return mapper.updateContent(content);
    }

    @Override
    public int deleteContentByIds(Long[] contentIds)
    {
        return mapper.deleteContentByIds(contentIds);
    }

    @Override
    public List<MindcareRecord> selectRecordList(MindcareRecord record)
    {
        return mapper.selectRecordList(record);
    }

    @Override
    public MindcareRecord selectRecordById(Long recordId)
    {
        return mapper.selectRecordById(recordId);
    }

    @Override
    public int updateRecordStatus(Long recordId, String status, String updateBy)
    {
        if (!isAllowedStatus(status))
        {
            throw new ServiceException("记录状态不正确");
        }
        return mapper.updateRecordStatus(recordId, status, updateBy);
    }

    @Override
    public List<MindcareClient> selectClientList(MindcareClient client)
    {
        return mapper.selectClientList(client);
    }

    @Override
    public Map<String, Object> dashboard()
    {
        Map<String, Object> result = new HashMap<>();
        result.put("stats", mapper.selectDashboardStats());
        result.put("recentRecords", mapper.selectRecentRecords());
        return result;
    }

    @Override
    @Transactional
    public MindcareClient registerClient(String clientId, String token, String nickname, String phone)
    {
        validateCredentials(clientId, token);
        MindcareClient existing = mapper.selectClientById(clientId);
        if (existing == null)
        {
            MindcareClient client = new MindcareClient();
            client.setClientId(clientId);
            client.setTokenHash(SecurityUtils.encryptPassword(token));
            client.setNickname(limit(nickname, 50));
            client.setPhone(limit(phone, 30));
            mapper.insertClient(client);
            return client;
        }
        if (!SecurityUtils.matchesPassword(token, existing.getTokenHash()))
        {
            throw new ServiceException("客户端凭证无效");
        }
        existing.setNickname(limit(nickname, 50));
        existing.setPhone(limit(phone, 30));
        mapper.touchClient(existing);
        return existing;
    }

    @Override
    public MindcareClient authenticateClient(String clientId, String token)
    {
        validateCredentials(clientId, token);
        MindcareClient client = mapper.selectClientById(clientId);
        if (client == null || !SecurityUtils.matchesPassword(token, client.getTokenHash()))
        {
            throw new ServiceException("客户端凭证无效，请重新初始化应用");
        }
        mapper.touchClient(client);
        return client;
    }

    @Override
    public Map<String, Object> bootstrap(String clientId, String token)
    {
        authenticateClient(clientId, token);
        List<MindcareContent> contents = mapper.selectPublishedContentList();
        List<Object> assessments = new ArrayList<>();
        List<Object> courses = new ArrayList<>();
        List<Object> activities = new ArrayList<>();
        for (MindcareContent content : contents)
        {
            Object payload = JSON.parse(content.getPayloadJson());
            if ("assessment".equals(content.getContentType()))
            {
                assessments.add(payload);
            }
            else if ("course".equals(content.getContentType()))
            {
                courses.add(payload);
            }
            else if ("activity".equals(content.getContentType()))
            {
                activities.add(payload);
            }
        }
        Map<String, Object> data = new HashMap<>();
        data.put("assessments", assessments);
        data.put("courses", courses);
        data.put("activities", activities);
        data.put("records", mapper.selectClientRecordList(clientId));
        return data;
    }

    @Override
    @Transactional
    public MindcareRecord saveClientRecord(String clientId, String token, MindcareRecord record)
    {
        authenticateClient(clientId, token);
        if (record.getDataJson() != null && record.getDataJson().length() > MAX_PAYLOAD_LENGTH)
        {
            throw new ServiceException("记录内容过长");
        }
        if (!isAllowedRecordType(record.getRecordType()))
        {
            throw new ServiceException("记录类型不正确");
        }
        record.setClientId(clientId);
        MindcareRecord existing = mapper.selectClientRecordByKey(clientId, record.getRecordKey());
        if (existing != null && !record.getRecordType().equals(existing.getRecordType()))
        {
            throw new ServiceException("记录标识已被其他业务类型使用");
        }
        boolean cancelRequested = "canceled".equals(record.getStatus());
        normalizeRecord(record);
        preserveManagedStatus(record, existing, cancelRequested);
        mapper.upsertRecord(record);
        return record;
    }

    @Override
    @Transactional
    public int clearClientRecords(String clientId, String token)
    {
        authenticateClient(clientId, token);
        return mapper.deleteClientRecords(clientId);
    }

    private void validateContent(MindcareContent content)
    {
        try
        {
            Object payload = JSON.parse(content.getPayloadJson());
            if (!(payload instanceof JSONObject))
            {
                throw new ServiceException("内容配置必须是 JSON 对象");
            }
            JSONObject object = (JSONObject) payload;
            if (!content.getContentKey().equals(object.getString("id")))
            {
                throw new ServiceException("内容配置中的 id 必须与内容标识一致");
            }
        }
        catch (ServiceException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new ServiceException("内容配置不是有效的 JSON");
        }
    }

    private void normalizeRecord(MindcareRecord record)
    {
        if (StringUtils.isEmpty(record.getStatus()))
        {
            record.setStatus(defaultStatus(record.getRecordType()));
        }
        if (!isAllowedStatus(record.getStatus()))
        {
            throw new ServiceException("记录状态不正确");
        }
        MindcareContent content = null;
        if (StringUtils.isNotEmpty(record.getContentKey()))
        {
            String contentType = "assessment".equals(record.getRecordType()) ? "assessment"
                : ("course".equals(record.getRecordType()) ? "course"
                : ("activity".equals(record.getRecordType()) ? "activity" : null));
            if (contentType != null)
            {
                content = mapper.selectContentByKey(contentType, record.getContentKey());
                if (content == null || !"0".equals(content.getStatus()))
                {
                    throw new ServiceException("关联内容不存在或已下架");
                }
                record.setTitle(content.getTitle());
            }
        }
        if (("assessment".equals(record.getRecordType()) || "course".equals(record.getRecordType())
            || "activity".equals(record.getRecordType())) && content == null)
        {
            throw new ServiceException("关联内容不能为空");
        }
        if ("assessment".equals(record.getRecordType()))
        {
            scoreAssessment(record, content);
            record.setStatus("completed");
        }
        if ("course".equals(record.getRecordType()))
        {
            BigDecimal progress = record.getProgress() == null ? BigDecimal.ZERO : record.getProgress();
            progress = progress.max(BigDecimal.ZERO).min(new BigDecimal("100")).setScale(1, RoundingMode.HALF_UP);
            record.setProgress(progress);
            record.setStatus(progress.compareTo(new BigDecimal("100")) >= 0 ? "completed" : "in_progress");
        }
        if ("consultation".equals(record.getRecordType()))
        {
            validateContact(record);
            record.setStatus("canceled".equals(record.getStatus()) ? "canceled" : "submitted");
            validateConsultation(record);
        }
        if ("activity".equals(record.getRecordType()))
        {
            validateContact(record);
            record.setStatus("canceled".equals(record.getStatus()) ? "canceled" : "submitted");
            validateActivity(record, content);
        }
        if ("message".equals(record.getRecordType()))
        {
            JSONObject data = parseData(record.getDataJson());
            if (StringUtils.isEmpty(data.getString("text")))
            {
                throw new ServiceException("留言内容不能为空");
            }
            record.setStatus("submitted");
        }
    }

    private void preserveManagedStatus(MindcareRecord record, MindcareRecord existing, boolean cancelRequested)
    {
        if (existing == null)
        {
            return;
        }
        if ("message".equals(record.getRecordType()))
        {
            record.setStatus(existing.getStatus());
        }
        else if ("consultation".equals(record.getRecordType()) || "activity".equals(record.getRecordType()))
        {
            if (cancelRequested && !"completed".equals(existing.getStatus()))
            {
                record.setStatus("canceled");
            }
            else
            {
                record.setStatus(existing.getStatus());
            }
        }
    }

    private void scoreAssessment(MindcareRecord record, MindcareContent content)
    {
        try
        {
            JSONObject data = JSON.parseObject(record.getDataJson());
            JSONArray answers = data.getJSONArray("answers");
            if (answers == null || answers.isEmpty())
            {
                throw new ServiceException("测评答案不能为空");
            }
            JSONObject contentData = JSON.parseObject(content.getPayloadJson());
            JSONArray questions = contentData.getJSONArray("questions");
            if (questions == null || answers.size() != questions.size())
            {
                throw new ServiceException("测评答案数量与量表题目不一致");
            }
            int score = 0;
            for (Object answer : answers)
            {
                int value = Integer.parseInt(String.valueOf(answer));
                if (value < 0 || value > 3)
                {
                    throw new ServiceException("测评答案超出范围");
                }
                score += value;
            }
            record.setScore((int) Math.round((score * 100.0) / (answers.size() * 3.0)));
        }
        catch (ServiceException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new ServiceException("测评答案格式不正确");
        }
    }

    private void validateConsultation(MindcareRecord record)
    {
        JSONObject data = parseData(record.getDataJson());
        String date = data.getString("date");
        String time = data.getString("time");
        if (StringUtils.isEmpty(date) || StringUtils.isEmpty(time))
        {
            throw new ServiceException("预约日期和时段不能为空");
        }
        if (!"canceled".equals(record.getStatus())
            && mapper.countDuplicateConsultation(record.getClientId(), record.getRecordKey(), date, time) > 0)
        {
            throw new ServiceException("该时段已经预约，请勿重复提交");
        }
    }

    private void validateActivity(MindcareRecord record, MindcareContent content)
    {
        if ("canceled".equals(record.getStatus()))
        {
            return;
        }
        if (mapper.countActiveContentRecord(record.getClientId(), "activity", record.getContentKey(), record.getRecordKey()) > 0)
        {
            throw new ServiceException("你已经报名该活动");
        }
        JSONObject data = parseData(record.getDataJson());
        JSONObject contentData = JSON.parseObject(content.getPayloadJson());
        int count = data.getIntValue("count");
        String emergency = data.getString("emergency");
        int capacity = contentData.getIntValue("capacity");
        int initialEnrolled = contentData.getIntValue("enrolled");
        if (count < 1 || count > 20)
        {
            throw new ServiceException("报名人数不正确");
        }
        if (StringUtils.isEmpty(emergency) || !emergency.matches(".*1[3-9]\\d{9}.*"))
        {
            throw new ServiceException("紧急联系人格式不正确");
        }
        int enrolled = mapper.selectActivityEnrollmentCount(record.getContentKey(), record.getRecordKey());
        if (capacity > 0 && initialEnrolled + enrolled + count > capacity)
        {
            throw new ServiceException("活动剩余名额不足");
        }
    }

    private void validateContact(MindcareRecord record)
    {
        if (StringUtils.isEmpty(record.getContactName()))
        {
            throw new ServiceException("联系人不能为空");
        }
        if (StringUtils.isEmpty(record.getContactPhone()) || !record.getContactPhone().matches("^1[3-9]\\d{9}$"))
        {
            throw new ServiceException("联系电话格式不正确");
        }
    }

    private JSONObject parseData(String dataJson)
    {
        try
        {
            JSONObject data = JSON.parseObject(dataJson);
            if (data == null)
            {
                throw new ServiceException("记录明细不能为空");
            }
            return data;
        }
        catch (ServiceException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new ServiceException("记录明细格式不正确");
        }
    }

    private void validateCredentials(String clientId, String token)
    {
        if (StringUtils.isEmpty(clientId) || !clientId.matches("^[A-Za-z0-9_-]{16,64}$"))
        {
            throw new ServiceException("客户端标识格式不正确");
        }
        if (StringUtils.isEmpty(token) || token.length() < 32 || token.length() > 128)
        {
            throw new ServiceException("客户端凭证格式不正确");
        }
    }

    private boolean isAllowedRecordType(String type)
    {
        return "assessment".equals(type) || "consultation".equals(type) || "course".equals(type)
            || "activity".equals(type) || "message".equals(type);
    }

    private boolean isAllowedStatus(String status)
    {
        return "pending".equals(status) || "confirmed".equals(status) || "canceled".equals(status)
            || "completed".equals(status) || "in_progress".equals(status) || "submitted".equals(status);
    }

    private String defaultStatus(String type)
    {
        return "course".equals(type) ? "in_progress" : ("assessment".equals(type) ? "completed" : "submitted");
    }

    private String limit(String value, int max)
    {
        if (value == null)
        {
            return null;
        }
        return value.length() > max ? value.substring(0, max) : value;
    }
}
