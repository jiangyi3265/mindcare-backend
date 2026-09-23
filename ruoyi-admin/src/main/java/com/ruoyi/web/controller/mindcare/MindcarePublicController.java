package com.ruoyi.web.controller.mindcare;

import java.util.Map;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.domain.AjaxResult;
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

    @GetMapping("/bootstrap")
    public AjaxResult bootstrap(@RequestHeader("X-Client-Id") String clientId,
        @RequestHeader("X-Client-Token") String token)
    {
        return AjaxResult.success(service.bootstrap(clientId, token));
    }

    @PostMapping("/records")
    public AjaxResult saveRecord(@RequestHeader("X-Client-Id") String clientId,
        @RequestHeader("X-Client-Token") String token, @Valid @RequestBody MindcareRecord record)
    {
        return AjaxResult.success(service.saveClientRecord(clientId, token, record));
    }

    @DeleteMapping("/records")
    public AjaxResult clearRecords(@RequestHeader("X-Client-Id") String clientId,
        @RequestHeader("X-Client-Token") String token)
    {
        service.clearClientRecords(clientId, token);
        return AjaxResult.success();
    }
}
