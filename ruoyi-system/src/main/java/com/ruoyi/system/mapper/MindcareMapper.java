package com.ruoyi.system.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.system.domain.MindcareClient;
import com.ruoyi.system.domain.MindcareAccount;
import com.ruoyi.system.domain.MindcareContent;
import com.ruoyi.system.domain.MindcareRecord;

public interface MindcareMapper
{
    List<MindcareContent> selectContentList(MindcareContent content);
    List<MindcareContent> selectPublishedContentList();
    MindcareContent selectContentById(Long contentId);
    MindcareContent selectContentByKey(@Param("contentType") String contentType, @Param("contentKey") String contentKey);
    Long lockActivityContentByKey(@Param("contentKey") String contentKey);
    int insertContent(MindcareContent content);
    int updateContent(MindcareContent content);
    int deleteContentByIds(Long[] contentIds);

    List<MindcareRecord> selectRecordList(MindcareRecord record);
    List<MindcareRecord> selectOwnerRecordList(String ownerKey);
    MindcareRecord selectRecordById(Long recordId);
    MindcareRecord selectOwnerRecordByKey(@Param("ownerKey") String ownerKey, @Param("recordKey") String recordKey);
    MindcareRecord selectClientRecordByKey(@Param("clientId") String clientId, @Param("recordKey") String recordKey);
    int upsertRecord(MindcareRecord record);
    int countActiveContentRecord(@Param("ownerKey") String ownerKey, @Param("recordType") String recordType,
        @Param("contentKey") String contentKey, @Param("recordKey") String recordKey);
    int countDuplicateConsultation(@Param("ownerKey") String ownerKey, @Param("recordKey") String recordKey,
        @Param("appointmentDate") String appointmentDate, @Param("appointmentTime") String appointmentTime);
    int selectActivityEnrollmentCount(@Param("contentKey") String contentKey, @Param("recordKey") String recordKey);
    int selectActivityEnrollmentCountExcludingOwner(@Param("contentKey") String contentKey, @Param("ownerKey") String ownerKey);
    int updateRecordStatus(@Param("recordId") Long recordId, @Param("status") String status,
        @Param("handlingMethod") String handlingMethod, @Param("handlingNote") String handlingNote,
        @Param("updateBy") String updateBy);
    int deleteOwnerRecords(String ownerKey);

    List<MindcareClient> selectClientList(MindcareClient client);
    MindcareClient selectClientById(String clientId);
    int insertClient(MindcareClient client);
    int touchClient(MindcareClient client);
    int bindClientAccount(@Param("clientId") String clientId, @Param("accountId") Long accountId);
    int revokeClient(@Param("clientId") String clientId, @Param("tokenHash") String tokenHash);
    int unbindAccountClients(Long accountId);
    int countGuestAccountCollisions(@Param("guestOwner") String guestOwner, @Param("accountOwner") String accountOwner);
    int moveGuestRecordsToAccount(@Param("guestOwner") String guestOwner, @Param("accountOwner") String accountOwner);

    MindcareAccount selectAccountByPhoneForUpdate(String phone);
    MindcareAccount selectAccountById(Long accountId);
    List<MindcareAccount> selectAccountList(MindcareAccount account);
    int insertAccount(MindcareAccount account);
    int updateAccountFailures(MindcareAccount account);
    int updateAccountCredentials(MindcareAccount account);
    int updateAccountNickname(MindcareAccount account);

    Map<String, Object> selectDashboardStats();
    List<MindcareRecord> selectRecentRecords();
}
