package com.ruoyi.system.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.MindcareClient;
import com.ruoyi.system.domain.MindcareAccount;
import com.ruoyi.system.domain.MindcareContent;
import com.ruoyi.system.domain.MindcareRecord;
import com.ruoyi.system.mapper.MindcareMapper;
import com.ruoyi.system.service.IMindcareService;

@Service
public class MindcareServiceImpl implements IMindcareService
{
    private static final int MAX_PAYLOAD_LENGTH = 20000;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

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
        MindcareRecord record = mapper.selectRecordById(recordId);
        if (record == null)
        {
            throw new ServiceException("记录不存在");
        }
        boolean crisisAssessment = "assessment".equals(record.getRecordType())
            && StringUtils.isNotEmpty(record.getRiskLevel()) && !"normal".equals(record.getRiskLevel());
        if (!(crisisAssessment || "consultation".equals(record.getRecordType()) || "activity".equals(record.getRecordType())
            || "message".equals(record.getRecordType())))
        {
            throw new ServiceException("此记录类型不支持人工处理");
        }
        if (!("pending".equals(status) || "confirmed".equals(status)
            || "canceled".equals(status) || "completed".equals(status)))
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
    public List<MindcareAccount> selectAccountList(MindcareAccount account)
    {
        return mapper.selectAccountList(account);
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
        MindcareClient client = new MindcareClient();
        client.setClientId(clientId);
        client.setTokenHash(SecurityUtils.encryptPassword(token));
        client.setNickname(limit(nickname, 50));
        client.setPhone(limit(phone, 30));
        // Concurrent app launches may register the same locally persisted identity.
        // The insert is atomic and leaves an existing credential untouched.
        mapper.insertClient(client);
        MindcareClient existing = mapper.selectClientById(clientId);
        if (existing == null || !SecurityUtils.matchesPassword(token, existing.getTokenHash()))
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
    @Transactional
    public Map<String, Object> registerAccount(String clientId, String token, String phone, String password, String nickname)
    {
        MindcareClient client = authenticateClient(clientId, token);
        if (client.getAccountId() != null)
        {
            throw new ServiceException("请先退出当前账号");
        }
        validatePhone(phone);
        validatePassword(password);
        if (mapper.selectAccountByPhoneForUpdate(phone) != null)
        {
            throw new ServiceException("该手机号已注册，请直接登录");
        }
        String recoveryCode = newRecoveryCode();
        MindcareAccount account = new MindcareAccount();
        account.setPhone(phone);
        account.setPasswordHash(SecurityUtils.encryptPassword(password));
        account.setRecoveryHash(SecurityUtils.encryptPassword(normalizeRecoveryCode(recoveryCode)));
        account.setNickname(nickname == null ? "心友" : limit(nickname, 20).trim());
        if (StringUtils.isEmpty(account.getNickname())) account.setNickname("心友");
        mapper.insertAccount(account);
        bindAccount(client, account);
        Map<String, Object> result = accountInfo(account);
        result.put("recoveryCode", recoveryCode);
        return result;
    }

    @Override
    @Transactional(noRollbackFor = ServiceException.class)
    public Map<String, Object> loginAccount(String clientId, String token, String phone, String password)
    {
        MindcareClient client = authenticateClient(clientId, token);
        validatePhone(phone);
        if (password == null || password.length() > 72)
        {
            throw new ServiceException("手机号或密码错误");
        }
        MindcareAccount account = mapper.selectAccountByPhoneForUpdate(phone);
        if (account == null)
        {
            throw new ServiceException("手机号或密码错误");
        }
        if (account.getLockedUntil() != null && account.getLockedUntil().after(new Date()))
        {
            throw new ServiceException("尝试次数过多，请10分钟后再试");
        }
        if (!SecurityUtils.matchesPassword(password, account.getPasswordHash()))
        {
            int attempts = account.getLockedUntil() == null ? account.getFailedAttempts() : 0;
            account.setFailedAttempts(attempts + 1);
            account.setLockedUntil(attempts + 1 >= 5 ? new Date(System.currentTimeMillis() + 10 * 60 * 1000L) : null);
            mapper.updateAccountFailures(account);
            throw new ServiceException("手机号或密码错误");
        }
        if (client.getAccountId() != null && !client.getAccountId().equals(account.getAccountId()))
        {
            throw new ServiceException("请先退出当前账号");
        }
        account.setFailedAttempts(0);
        account.setLockedUntil(null);
        mapper.updateAccountFailures(account);
        bindAccount(client, account);
        return accountInfo(account);
    }

    @Override
    @Transactional
    public Map<String, Object> recoverAccount(String clientId, String token, String phone, String recoveryCode, String newPassword)
    {
        MindcareClient client = authenticateClient(clientId, token);
        validatePhone(phone);
        validatePassword(newPassword);
        String normalized = normalizeRecoveryCode(recoveryCode);
        MindcareAccount account = mapper.selectAccountByPhoneForUpdate(phone);
        if (account == null || normalized == null || !SecurityUtils.matchesPassword(normalized, account.getRecoveryHash()))
        {
            throw new ServiceException("手机号或恢复码错误");
        }
        if (client.getAccountId() != null && !client.getAccountId().equals(account.getAccountId()))
        {
            throw new ServiceException("请先退出当前账号");
        }
        String nextRecoveryCode = newRecoveryCode();
        account.setPasswordHash(SecurityUtils.encryptPassword(newPassword));
        account.setRecoveryHash(SecurityUtils.encryptPassword(normalizeRecoveryCode(nextRecoveryCode)));
        mapper.updateAccountCredentials(account);
        // Password recovery invalidates every other device's account binding.
        mapper.unbindAccountClients(account.getAccountId());
        bindAccount(client, account);
        Map<String, Object> result = accountInfo(account);
        result.put("recoveryCode", nextRecoveryCode);
        return result;
    }

    @Override
    @Transactional
    public Map<String, Object> updateAccountProfile(String clientId, String token, String nickname)
    {
        MindcareClient client = authenticateClient(clientId, token);
        if (client.getAccountId() == null) throw new ServiceException("请先登录");
        String value = nickname == null ? "" : limit(nickname, 20).trim();
        if (value.isEmpty()) throw new ServiceException("昵称不能为空");
        MindcareAccount account = mapper.selectAccountById(client.getAccountId());
        account.setNickname(value);
        mapper.updateAccountNickname(account);
        client.setNickname(value);
        mapper.touchClient(client);
        return accountInfo(account);
    }

    @Override
    @Transactional
    public void logoutAccount(String clientId, String token)
    {
        MindcareClient client = authenticateClient(clientId, token);
        if (client.getAccountId() == null) return;
        mapper.revokeClient(clientId, SecurityUtils.encryptPassword(newRecoveryCode()));
    }

    private void bindAccount(MindcareClient client, MindcareAccount account)
    {
        String guestOwner = "c:" + client.getClientId();
        String accountOwner = "a:" + account.getAccountId();
        if (client.getAccountId() == null)
        {
            if (mapper.countGuestAccountCollisions(guestOwner, accountOwner) > 0)
            {
                throw new ServiceException("当前设备记录与账号已有记录冲突，请联系客服");
            }
            mapper.moveGuestRecordsToAccount(guestOwner, accountOwner);
        }
        mapper.bindClientAccount(client.getClientId(), account.getAccountId());
    }

    private Map<String, Object> accountInfo(MindcareAccount account)
    {
        Map<String, Object> result = new HashMap<>();
        result.put("accountId", account.getAccountId());
        result.put("phone", account.getPhone());
        result.put("nickname", account.getNickname());
        return result;
    }

    private String ownerKey(MindcareClient client)
    {
        return client.getAccountId() == null ? "c:" + client.getClientId() : "a:" + client.getAccountId();
    }

    private void validatePhone(String phone)
    {
        if (phone == null || !phone.matches("^1[3-9]\\d{9}$"))
        {
            throw new ServiceException("请输入正确的11位手机号");
        }
    }

    private void validatePassword(String password)
    {
        if (password == null || password.length() < 8 || password.length() > 72
            || !password.matches(".*[A-Za-z].*") || !password.matches(".*\\d.*"))
        {
            throw new ServiceException("密码需为8到72位，且包含字母和数字");
        }
    }

    private String newRecoveryCode()
    {
        byte[] bytes = new byte[16];
        SECURE_RANDOM.nextBytes(bytes);
        StringBuilder value = new StringBuilder(39);
        for (int i = 0; i < bytes.length; i++)
        {
            if (i > 0 && i % 2 == 0) value.append('-');
            value.append(String.format("%02X", bytes[i] & 0xff));
        }
        return value.toString();
    }

    private String normalizeRecoveryCode(String code)
    {
        if (code == null) return null;
        String value = code.replace("-", "").replace(" ", "").toUpperCase();
        return value.matches("^[A-F0-9]{32}$") ? value : null;
    }

    @Override
    public Map<String, Object> bootstrap(String clientId, String token)
    {
        MindcareClient client = authenticateClient(clientId, token);
        List<MindcareContent> contents = mapper.selectPublishedContentList();
        List<Object> assessments = new ArrayList<>();
        List<Object> courses = new ArrayList<>();
        List<Object> activities = new ArrayList<>();
        List<Object> banners = new ArrayList<>();
        List<Object> experts = new ArrayList<>();
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
                JSONObject activity = (JSONObject) payload;
                activity.put("enrolled", activity.getIntValue("enrolled")
                    + mapper.selectActivityEnrollmentCountExcludingOwner(content.getContentKey(), ownerKey(client)));
                activities.add(payload);
            }
            else if ("banner".equals(content.getContentType()))
            {
                banners.add(payload);
            }
            else if ("expert".equals(content.getContentType()))
            {
                experts.add(payload);
            }
        }
        Map<String, Object> data = new HashMap<>();
        data.put("assessments", assessments);
        data.put("courses", courses);
        data.put("activities", activities);
        data.put("banners", banners);
        data.put("experts", experts);
        data.put("records", mapper.selectOwnerRecordList(ownerKey(client)));
        data.put("account", client.getAccountId() == null ? null : accountInfo(mapper.selectAccountById(client.getAccountId())));
        return data;
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public MindcareRecord saveClientRecord(String clientId, String token, Long expectedAccountId, MindcareRecord record)
    {
        MindcareClient client = authenticateClient(clientId, token);
        requireExpectedAccount(client, expectedAccountId);
        if (record.getDataJson() != null && record.getDataJson().length() > MAX_PAYLOAD_LENGTH)
        {
            throw new ServiceException("记录内容过长");
        }
        if (!isAllowedRecordType(record.getRecordType()))
        {
            throw new ServiceException("记录类型不正确");
        }
        record.setClientId(clientId);
        record.setOwnerKey(ownerKey(client));
        if ("activity".equals(record.getRecordType()) && StringUtils.isNotEmpty(record.getContentKey()))
        {
            // Serialize signups for one activity before checking its remaining capacity.
            mapper.lockActivityContentByKey(record.getContentKey());
        }
        MindcareRecord existing = mapper.selectOwnerRecordByKey(record.getOwnerKey(), record.getRecordKey());
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
    public int clearClientRecords(String clientId, String token, Long expectedAccountId)
    {
        MindcareClient client = authenticateClient(clientId, token);
        requireExpectedAccount(client, expectedAccountId);
        return mapper.deleteOwnerRecords(ownerKey(client));
    }

    private void requireExpectedAccount(MindcareClient client, Long expectedAccountId)
    {
        if (expectedAccountId != null && !expectedAccountId.equals(client.getAccountId()))
        {
            throw new ServiceException("登录状态已变化，请重新登录");
        }
    }

    private void validateContent(MindcareContent content)
    {
        try
        {
            if (content.getPayloadJson() == null || content.getPayloadJson().length() > MAX_PAYLOAD_LENGTH)
            {
                throw new ServiceException("内容配置不能为空或超过长度限制");
            }
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
            if (content.getTitle() == null || !content.getTitle().equals(object.getString("title")))
            {
                throw new ServiceException("内容配置中的 title 必须与标题一致");
            }
            if ("assessment".equals(content.getContentType()))
            {
                String sourceName = object.getString("sourceName");
                String sourceUrl = object.getString("sourceUrl");
                if (StringUtils.isEmpty(sourceName) || StringUtils.isEmpty(sourceUrl)
                    || !sourceUrl.matches("^https://[^\\s]+$"))
                {
                    throw new ServiceException("量表必须填写权威来源名称和 HTTPS 来源链接");
                }
                JSONArray questions = object.getJSONArray("questions");
                int count = object.getIntValue("count");
                if (questions == null || count < 1 || count > 100 || questions.size() != count)
                {
                    throw new ServiceException("量表题数必须与非空题目列表一致（1-100题）");
                }
                for (Object question : questions)
                {
                    if (!(question instanceof String) || StringUtils.isEmpty(((String) question).trim()))
                    {
                        throw new ServiceException("量表题目不能为空");
                    }
                }
                if (object.getIntValue("minutes") < 1)
                {
                    throw new ServiceException("量表预计时长必须大于 0");
                }
            }
            else if ("course".equals(content.getContentType()))
            {
                JSONArray chapters = object.getJSONArray("chapters");
                if (object.getIntValue("minutes") < 1 || chapters == null || chapters.isEmpty())
                {
                    throw new ServiceException("课程时长和章节不能为空");
                }
                for (Object chapterValue : chapters)
                {
                    if (!(chapterValue instanceof JSONObject))
                    {
                        throw new ServiceException("课程章节格式不正确");
                    }
                    JSONObject chapter = (JSONObject) chapterValue;
                    String duration = chapter.getString("duration");
                    if (StringUtils.isEmpty(chapter.getString("title")) || duration == null
                        || !duration.matches("^\\d{1,3}:[0-5]\\d$"))
                    {
                        throw new ServiceException("课程章节需要标题和分:秒格式的时长");
                    }
                }
            }
            else if ("activity".equals(content.getContentType()))
            {
                String date = object.getString("date");
                if (StringUtils.isEmpty(date))
                {
                    throw new ServiceException("活动日期不能为空");
                }
                try
                {
                    LocalDate.parse(date);
                }
                catch (Exception e)
                {
                    throw new ServiceException("活动日期必须为 YYYY-MM-DD");
                }
                JSONArray schedule = object.getJSONArray("schedule");
                if (StringUtils.isEmpty(object.getString("time")) || StringUtils.isEmpty(object.getString("location"))
                    || object.getIntValue("capacity") < 1 || object.getIntValue("enrolled") < 0
                    || object.getIntValue("enrolled") > object.getIntValue("capacity")
                    || !("报名中".equals(object.getString("status")) || "进行中".equals(object.getString("status"))
                        || "已结束".equals(object.getString("status"))) || schedule == null)
                {
                    throw new ServiceException("活动时间、地点、人数或日程配置不正确");
                }
                for (Object item : schedule)
                {
                    if (!(item instanceof JSONArray) || ((JSONArray) item).size() < 3)
                    {
                        throw new ServiceException("活动日程需使用 [时间, 标题, 说明] 格式");
                    }
                }
            }
            else if ("banner".equals(content.getContentType()))
            {
                String image = object.getString("image");
                if (image == null || !(image.matches("^builtin:(hero|rest)$")
                    || image.matches("^/profile/upload/[A-Za-z0-9/_-]+\\.(png|jpe?g|webp)$")))
                {
                    throw new ServiceException("轮播图必须使用内置图片或后台上传的图片");
                }
            }
            else if ("expert".equals(content.getContentType()))
            {
                if (StringUtils.isEmpty(object.getString("name")) || StringUtils.isEmpty(object.getString("profile"))
                    || StringUtils.isEmpty(object.getString("credentials")))
                {
                    throw new ServiceException("专家资料需要姓名、简介和资质说明");
                }
                String photo = object.getString("photo");
                if (StringUtils.isEmpty(photo) || !(photo.matches("^builtin:avatar$")
                    || photo.matches("^/profile/upload/[A-Za-z0-9/_-]+\\.(png|jpe?g|webp)$")))
                {
                    throw new ServiceException("专家头像必须使用内置图片或后台上传的图片");
                }
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
            record.setStatus("normal".equals(record.getRiskLevel()) ? "completed" : "pending");
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
        else if ("assessment".equals(record.getRecordType())
            && StringUtils.isNotEmpty(existing.getRiskLevel()) && !"normal".equals(existing.getRiskLevel()))
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
            JSONArray optionValues = contentData.getJSONArray("optionValues");
            JSONObject scoring = contentData.getJSONObject("scoring");
            String scoringType = scoring == null ? "percent" : scoring.getString("type");
            int score = 0;
            for (Object answer : answers)
            {
                int value = Integer.parseInt(String.valueOf(answer));
                boolean valid = optionValues == null ? value >= 0 && value <= 3 : optionValues.contains(value);
                if (!valid)
                {
                    throw new ServiceException("测评答案超出范围");
                }
                score += value;
            }
            if ("sum".equalsIgnoreCase(scoringType))
            {
                record.setScore(score);
            }
            else
            {
                int max = scoring == null ? answers.size() * 3 : scoring.getIntValue("maxScore");
                if (max < 1) max = answers.size() * (optionValues == null ? 3 : optionValues.getIntValue(optionValues.size() - 1));
                record.setScore((int) Math.round((score * 100.0) / max));
            }
            record.setRiskLevel("normal");
            record.setRiskReason("");
            JSONObject crisis = contentData.getJSONObject("crisisRules");
            if (crisis != null)
            {
                String direction = crisis.getString("direction");
                int threshold = crisis.getIntValue("threshold");
                int answerIndex = crisis.containsKey("answerIndex") ? crisis.getIntValue("answerIndex") : -1;
                int answerMin = crisis.containsKey("answerMin") ? crisis.getIntValue("answerMin") : Integer.MAX_VALUE;
                boolean answerTriggered = answerIndex >= 0 && answerIndex < answers.size()
                    && Integer.parseInt(String.valueOf(answers.get(answerIndex))) >= answerMin;
                boolean triggered = answerTriggered || ("low".equalsIgnoreCase(direction) ? record.getScore() <= threshold
                    : ("high".equalsIgnoreCase(direction) && record.getScore() >= threshold));
                if (triggered)
                {
                    record.setRiskLevel(StringUtils.isEmpty(crisis.getString("level")) ? "high" : crisis.getString("level"));
                    record.setRiskReason(StringUtils.defaultIfEmpty(crisis.getString("reason"), "测评结果提示需要进一步关注。"));
                }
            }
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
            && mapper.countDuplicateConsultation(record.getOwnerKey(), record.getRecordKey(), date, time) > 0)
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
        if (mapper.countActiveContentRecord(record.getOwnerKey(), "activity", record.getContentKey(), record.getRecordKey()) > 0)
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
