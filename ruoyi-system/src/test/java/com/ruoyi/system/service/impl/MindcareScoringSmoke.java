package com.ruoyi.system.service.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.system.domain.MindcareClient;
import com.ruoyi.system.domain.MindcareContent;
import com.ruoyi.system.domain.MindcareRecord;
import com.ruoyi.system.mapper.MindcareMapper;

/** Lightweight scoring regression that can run without a database or Spring context. */
public final class MindcareScoringSmoke
{
    public static void main(String[] args) throws Exception
    {
        MindcareServiceImpl service = new MindcareServiceImpl();
        Method score = MindcareServiceImpl.class.getDeclaredMethod("scoreAssessment", MindcareRecord.class, MindcareContent.class);
        score.setAccessible(true);

        MindcareContent scale = new MindcareContent();
        scale.setPayloadJson("{\"questions\":[\"Q1\",\"Q2\",\"Q3\",\"Q4\"],\"optionValues\":[0,1,2,3],\"scoring\":{\"type\":\"sum\",\"maxScore\":12,\"reverseItems\":[1,3]}}");
        MindcareRecord record = new MindcareRecord();
        record.setDataJson("{\"answers\":[3,0,3,0]}");
        score.invoke(service, record, scale);
        if (record.getScore() == null || record.getScore() != 12)
        {
            throw new AssertionError("Expected reversed total 12, got " + record.getScore());
        }

        scale.setPayloadJson("{\"questions\":[\"Q1\",\"Q2\"],\"optionValues\":[0,1,2,3],\"scoring\":{\"type\":\"sum\",\"reverseItems\":[2]}}");
        record.setDataJson("{\"answers\":[0,0]}");
        try
        {
            score.invoke(service, record, scale);
            throw new AssertionError("Out-of-range reverseItems index was accepted");
        }
        catch (InvocationTargetException e)
        {
            if (!(e.getCause() instanceof ServiceException)) throw e;
        }

        MindcareContent expert = new MindcareContent();
        expert.setContentType("expert");
        expert.setContentKey("expert-smoke");
        expert.setTitle("测试专家");
        expert.setPayloadJson("{\"id\":\"expert-smoke\",\"title\":\"测试专家\",\"name\":\"测试专家\",\"credentials\":\"专业背景\",\"profile\":\"咨询简介\",\"photo\":\"builtin:avatar\",\"availableTimes\":[\"10:00\"]}");
        Method validateContent = MindcareServiceImpl.class.getDeclaredMethod("validateContent", MindcareContent.class);
        validateContent.setAccessible(true);
        validateContent.invoke(service, expert);

        // The service must pass the follow-up method/note to the persistence mapper.
        MindcareRecord risk = new MindcareRecord();
        risk.setRecordType("assessment");
        risk.setRiskLevel("high");
        Object[][] captured = { null };
        MindcareMapper mapper = (MindcareMapper) Proxy.newProxyInstance(
            MindcareMapper.class.getClassLoader(), new Class<?>[] { MindcareMapper.class },
            (_proxy, method, arguments) -> {
                if ("selectRecordById".equals(method.getName())) return risk;
                if ("updateRecordStatus".equals(method.getName())) {
                    captured[0] = arguments;
                    return 1;
                }
                throw new UnsupportedOperationException(method.getName());
            });
        Field mapperField = MindcareServiceImpl.class.getDeclaredField("mapper");
        mapperField.setAccessible(true);
        mapperField.set(service, mapper);
        expectStatusRejected(service, "completed", null, "已电话回访");
        expectStatusRejected(service, "completed", "phone", null);
        expectStatusRejected(service, "completed", "  ", "已电话回访");
        expectStatusRejected(service, "completed", "phone", "  ");
        expectStatusRejected(service, "confirmed", "phone", "已电话回访");
        if (captured[0] != null)
        {
            throw new AssertionError("Invalid handling update reached persistence");
        }
        int pending = service.updateRecordStatus(42L, "pending", null, null, "admin");
        if (pending != 1 || captured[0] == null || captured[0][2] != null || captured[0][3] != null)
        {
            throw new AssertionError("Pending warning should not require handling details");
        }
        int updated = service.updateRecordStatus(42L, "completed", " phone ", " 已电话回访并记录安全情况 ", "admin");
        if (updated != 1 || captured[0] == null || captured[0].length != 5
            || !"phone".equals(captured[0][2])
            || !"已电话回访并记录安全情况".equals(captured[0][3]))
        {
            throw new AssertionError("Follow-up details were not forwarded to persistence");
        }

        // A client retry must not replace an assessment after staff have handled it.
        String clientId = "mc_12345678901234567890";
        String token = "0123456789abcdef0123456789abcdef";
        MindcareClient client = new MindcareClient();
        client.setClientId(clientId);
        client.setTokenHash(SecurityUtils.encryptPassword(token));
        client.setAccountId(7L);
        MindcareRecord handled = new MindcareRecord();
        handled.setRecordKey("report-existing");
        handled.setRecordType("assessment");
        handled.setOwnerKey("a:7");
        handled.setStatus("completed");
        handled.setRiskLevel("high");
        handled.setScore(21);
        MindcareMapper retryMapper = (MindcareMapper) Proxy.newProxyInstance(
            MindcareMapper.class.getClassLoader(), new Class<?>[] { MindcareMapper.class },
            (_proxy, method, arguments) -> {
                if ("selectClientById".equals(method.getName())) return client;
                if ("touchClient".equals(method.getName())) return 1;
                if ("selectClientRecordByKey".equals(method.getName())
                    || "selectOwnerRecordByKey".equals(method.getName())) return handled;
                if ("upsertRecord".equals(method.getName())) throw new AssertionError("Assessment retry wrote to database");
                throw new UnsupportedOperationException(method.getName());
            });
        mapperField.set(service, retryMapper);
        MindcareRecord retry = new MindcareRecord();
        retry.setRecordKey("report-existing");
        retry.setRecordType("assessment");
        retry.setDataJson("{\"answers\":[0,0,0,0]}");
        MindcareRecord result = service.saveClientRecord(clientId, token, 7L, retry);
        if (result != handled || result.getScore() != 21 || !"completed".equals(result.getStatus()))
        {
            throw new AssertionError("Assessment retry changed the handled record");
        }
        System.out.println("MindcareScoringSmoke PASS");
    }

    private static void expectStatusRejected(MindcareServiceImpl service, String status, String method, String note)
    {
        try
        {
            service.updateRecordStatus(42L, status, method, note, "admin");
            throw new AssertionError("Invalid handling update was accepted: " + status);
        }
        catch (ServiceException expected)
        {
            // The service must reject invalid updates before they reach the mapper.
        }
    }
}
