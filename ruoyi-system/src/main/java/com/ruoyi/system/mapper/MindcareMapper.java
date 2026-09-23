package com.ruoyi.system.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.system.domain.MindcareClient;
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
    List<MindcareRecord> selectClientRecordList(String clientId);
    MindcareRecord selectRecordById(Long recordId);
    MindcareRecord selectClientRecordByKey(@Param("clientId") String clientId, @Param("recordKey") String recordKey);
    int upsertRecord(MindcareRecord record);
    int countActiveContentRecord(@Param("clientId") String clientId, @Param("recordType") String recordType,
        @Param("contentKey") String contentKey, @Param("recordKey") String recordKey);
    int countDuplicateConsultation(@Param("clientId") String clientId, @Param("recordKey") String recordKey,
        @Param("appointmentDate") String appointmentDate, @Param("appointmentTime") String appointmentTime);
    int selectActivityEnrollmentCount(@Param("contentKey") String contentKey, @Param("recordKey") String recordKey);
    int selectActivityEnrollmentCountExcludingClient(@Param("contentKey") String contentKey, @Param("clientId") String clientId);
    int updateRecordStatus(@Param("recordId") Long recordId, @Param("status") String status, @Param("updateBy") String updateBy);
    int deleteClientRecords(String clientId);

    List<MindcareClient> selectClientList(MindcareClient client);
    MindcareClient selectClientById(String clientId);
    int insertClient(MindcareClient client);
    int touchClient(MindcareClient client);

    Map<String, Object> selectDashboardStats();
    List<MindcareRecord> selectRecentRecords();
}
