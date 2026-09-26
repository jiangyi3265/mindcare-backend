package com.ruoyi.system.domain;

import java.math.BigDecimal;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * A user-side assessment, appointment, learning, activity or message record.
 */
public class MindcareRecord extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long recordId;

    @NotBlank(message = "记录标识不能为空")
    @Size(max = 64, message = "记录标识不能超过64个字符")
    @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "记录标识只能包含字母、数字、下划线和连字符")
    private String recordKey;

    private String clientId;
    @JsonIgnore
    private String ownerKey;

    @NotBlank(message = "记录类型不能为空")
    @Pattern(regexp = "^(assessment|consultation|course|activity|message)$", message = "记录类型不正确")
    private String recordType;

    private String contentKey;

    @Size(max = 100, message = "标题不能超过100个字符")
    private String title;

    @Size(max = 50, message = "联系人不能超过50个字符")
    private String contactName;

    @Size(max = 30, message = "联系电话不能超过30个字符")
    private String contactPhone;

    private String status;
    private Integer score;
    private BigDecimal progress;
    private String dataJson;
    /** Server-derived safety signal for assessment records. */
    private String riskLevel;
    private String riskReason;

    public Long getRecordId() { return recordId; }
    public void setRecordId(Long recordId) { this.recordId = recordId; }
    public String getRecordKey() { return recordKey; }
    public void setRecordKey(String recordKey) { this.recordKey = recordKey; }
    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }
    public String getOwnerKey() { return ownerKey; }
    public void setOwnerKey(String ownerKey) { this.ownerKey = ownerKey; }
    public String getRecordType() { return recordType; }
    public void setRecordType(String recordType) { this.recordType = recordType; }
    public String getContentKey() { return contentKey; }
    public void setContentKey(String contentKey) { this.contentKey = contentKey; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContactName() { return contactName; }
    public void setContactName(String contactName) { this.contactName = contactName; }
    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }
    public BigDecimal getProgress() { return progress; }
    public void setProgress(BigDecimal progress) { this.progress = progress; }
    public String getDataJson() { return dataJson; }
    public void setDataJson(String dataJson) { this.dataJson = dataJson; }
    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
    public String getRiskReason() { return riskReason; }
    public void setRiskReason(String riskReason) { this.riskReason = riskReason; }
}
