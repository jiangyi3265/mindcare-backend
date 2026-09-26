package com.ruoyi.system.service;

import java.util.List;
import java.util.Map;
import com.ruoyi.system.domain.MindcareClient;
import com.ruoyi.system.domain.MindcareAccount;
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
    int updateRecordStatus(Long recordId, String status, String handlingMethod, String handlingNote, String updateBy);
    List<MindcareClient> selectClientList(MindcareClient client);
    List<MindcareAccount> selectAccountList(MindcareAccount account);
    Map<String, Object> dashboard();

    MindcareClient registerClient(String clientId, String token, String nickname, String phone);
    MindcareClient authenticateClient(String clientId, String token);
    Map<String, Object> registerAccount(String clientId, String token, String phone, String password, String nickname);
    Map<String, Object> loginAccount(String clientId, String token, String phone, String password);
    Map<String, Object> recoverAccount(String clientId, String token, String phone, String recoveryCode, String newPassword);
    Map<String, Object> updateAccountProfile(String clientId, String token, String nickname);
    void logoutAccount(String clientId, String token);
    Map<String, Object> bootstrap(String clientId, String token);
    MindcareRecord saveClientRecord(String clientId, String token, Long expectedAccountId, MindcareRecord record);
    int clearClientRecords(String clientId, String token, Long expectedAccountId);
}
