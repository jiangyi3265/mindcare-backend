package com.ruoyi.system.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonIgnore;

/** Phone/password account. The phone is an identifier, not SMS-verified ownership. */
public class MindcareAccount
{
    private Long accountId;
    private String phone;
    @JsonIgnore
    private String passwordHash;
    @JsonIgnore
    private String recoveryHash;
    private String nickname;
    @JsonIgnore
    private Integer failedAttempts;
    @JsonIgnore
    private Date lockedUntil;
    private Date createTime;

    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getRecoveryHash() { return recoveryHash; }
    public void setRecoveryHash(String recoveryHash) { this.recoveryHash = recoveryHash; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public Integer getFailedAttempts() { return failedAttempts; }
    public void setFailedAttempts(Integer failedAttempts) { this.failedAttempts = failedAttempts; }
    public Date getLockedUntil() { return lockedUntil; }
    public void setLockedUntil(Date lockedUntil) { this.lockedUntil = lockedUntil; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
}
