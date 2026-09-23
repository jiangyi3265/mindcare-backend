package com.ruoyi.system.service;

import java.util.List;
import java.util.Map;
import com.ruoyi.system.domain.MindcareClient;
import com.ruoyi.system.domain.MindcareContent;
import com.ruoyi.system.domain.MindcareRecord;

public interface IMindcareService
{
    List<MindcareContent> selectContentList(MindcareContent content);
    MindcareContent selectContentById(Long contentId);
    int insertContent(MindcareContent content);
    int updateContent(MindcareContent content);
    int deleteContentByIds(Long[] contentIds);

    List<MindcareRecord> selectRecordList(MindcareRecord record);
    MindcareRecord selectRecordById(Long recordId);
    int updateRecordStatus(Long recordId, String status, String updateBy);
    List<MindcareClient> selectClientList(MindcareClient client);
    Map<String, Object> dashboard();

    MindcareClient registerClient(String clientId, String token, String nickname, String phone);
    MindcareClient authenticateClient(String clientId, String token);
    Map<String, Object> bootstrap(String clientId, String token);
    MindcareRecord saveClientRecord(String clientId, String token, MindcareRecord record);
    int clearClientRecords(String clientId, String token);
}
