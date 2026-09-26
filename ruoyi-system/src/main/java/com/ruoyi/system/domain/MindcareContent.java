package com.ruoyi.system.domain;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * MindCare content published to the user application.
 */
public class MindcareContent extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long contentId;

    @NotBlank(message = "内容标识不能为空")
    @Size(max = 64, message = "内容标识不能超过64个字符")
    @Pattern(regexp = "^[a-z0-9][a-z0-9-]*$", message = "内容标识只能包含小写字母、数字和连字符")
    private String contentKey;

    @NotBlank(message = "内容类型不能为空")
    @Pattern(regexp = "^(assessment|course|activity|banner|expert)$", message = "内容类型不正确")
    private String contentType;

    @NotBlank(message = "标题不能为空")
    @Size(max = 100, message = "标题不能超过100个字符")
    private String title;

    @Size(max = 50, message = "分类不能超过50个字符")
    private String category;

    @Size(max = 500, message = "简介不能超过500个字符")
    private String summary;

    @NotBlank(message = "内容配置不能为空")
    private String payloadJson;

    private String status;
    private Integer sortOrder;

    public Long getContentId() { return contentId; }
    public void setContentId(Long contentId) { this.contentId = contentId; }
    public String getContentKey() { return contentKey; }
    public void setContentKey(String contentKey) { this.contentKey = contentKey; }
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public String getPayloadJson() { return payloadJson; }
    public void setPayloadJson(String payloadJson) { this.payloadJson = payloadJson; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
}
