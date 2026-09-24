package com.ruoyi.web.controller.mindcare;

import java.util.Map;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.annotation.RateLimiter;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.LimitType;
import com.ruoyi.system.domain.MindcareRecord;
import com.ruoyi.system.service.IMindcareService;

@Anonymous
@RestController
@RequestMapping("/app/mindcare")
public class MindcarePublicController
{
    @Autowired
    private IMindcareService service;

    @PostMapping("/client/register")
    public AjaxResult register(@RequestBody Map<String, String> body)
    {
        return AjaxResult.success(service.registerClient(body.get("clientId"), body.get("token"), body.get("nickname"), body.get("phone")));
    }

    @RateLimiter(time = 60, count = 8, limitType = LimitType.IP)
    @PostMapping("/account/register")
    public AjaxResult registerAccount(@RequestHeader("X-Client-Id") String clientId,
        @RequestHeader("X-Client-Token") String token, @RequestBody Map<String, String> body)
    {
        return AjaxResult.success(service.registerAccount(clientId, token,
            body.get("phone"), body.get("password"), body.get("nickname")));
    }

    @RateLimiter(time = 60, count = 12, limitType = LimitType.IP)
    @PostMapping("/account/login")
    public AjaxResult loginAccount(@RequestHeader("X-Client-Id") String clientId,
        @RequestHeader("X-Client-Token") String token, @RequestBody Map<String, String> body)
    {
        return AjaxResult.success(service.loginAccount(clientId, token, body.get("phone"), body.get("password")));
    }

    @RateLimiter(time = 60, count = 8, limitType = LimitType.IP)
    @PostMapping("/account/recover")
    public AjaxResult recoverAccount(@RequestHeader("X-Client-Id") String clientId,
        @RequestHeader("X-Client-Token") String token, @RequestBody Map<String, String> body)
    {
        return AjaxResult.success(service.recoverAccount(clientId, token,
            body.get("phone"), body.get("recoveryCode"), body.get("newPassword")));
    }

    @PostMapping("/account/logout")
    public AjaxResult logoutAccount(@RequestHeader("X-Client-Id") String clientId,
        @RequestHeader("X-Client-Token") String token)
    {
        service.logoutAccount(clientId, token);
        return AjaxResult.success();
    }

    @PutMapping("/account/profile")
    public AjaxResult updateAccountProfile(@RequestHeader("X-Client-Id") String clientId,
        @RequestHeader("X-Client-Token") String token, @RequestBody Map<String, String> body)
    {
        return AjaxResult.success(service.updateAccountProfile(clientId, token, body.get("nickname")));
    }

    @GetMapping("/bootstrap")
    public AjaxResult bootstrap(@RequestHeader("X-Client-Id") String clientId,
        @RequestHeader("X-Client-Token") String token)
    {
        return AjaxResult.success(service.bootstrap(clientId, token));
    }

    @PostMapping("/records")
    public AjaxResult saveRecord(@RequestHeader("X-Client-Id") String clientId,
        @RequestHeader("X-Client-Token") String token,
        @RequestHeader(value = "X-Expected-Account-Id", required = false) Long expectedAccountId,
        @Valid @RequestBody MindcareRecord record)
    {
        return AjaxResult.success(service.saveClientRecord(clientId, token, expectedAccountId, record));
    }

    @DeleteMapping("/records")
    public AjaxResult clearRecords(@RequestHeader("X-Client-Id") String clientId,
        @RequestHeader("X-Client-Token") String token,
        @RequestHeader(value = "X-Expected-Account-Id", required = false) Long expectedAccountId)
    {
        service.clearClientRecords(clientId, token, expectedAccountId);
        return AjaxResult.success();
    }
}
