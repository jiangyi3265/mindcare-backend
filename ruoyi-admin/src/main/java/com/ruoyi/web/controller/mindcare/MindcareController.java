package com.ruoyi.web.controller.mindcare;

import java.util.List;
import java.util.Map;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.system.domain.MindcareClient;
import com.ruoyi.system.domain.MindcareAccount;
import com.ruoyi.system.domain.MindcareContent;
import com.ruoyi.system.domain.MindcareRecord;
import com.ruoyi.system.service.IMindcareService;

@RestController
@RequestMapping("/mindcare")
public class MindcareController extends BaseController
{
    @Autowired
    private IMindcareService service;

    @PreAuthorize("@ss.hasPermi('mindcare:dashboard:view')")
    @GetMapping("/dashboard")
    public AjaxResult dashboard()
    {
        return success(service.dashboard());
    }

    @PreAuthorize("@ss.hasPermi('mindcare:content:list')")
    @GetMapping("/content/list")
    public TableDataInfo contentList(MindcareContent content)
    {
        startPage();
        List<MindcareContent> list = service.selectContentList(content);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('mindcare:content:query')")
    @GetMapping("/content/{contentId}")
    public AjaxResult contentInfo(@PathVariable Long contentId)
    {
        return success(service.selectContentById(contentId));
    }

    @PreAuthorize("@ss.hasPermi('mindcare:content:add')")
    @Log(title = "MindCare内容", businessType = BusinessType.INSERT)
    @PostMapping("/content")
    public AjaxResult addContent(@Validated @RequestBody MindcareContent content)
    {
        content.setCreateBy(getUsername());
        return toAjax(service.insertContent(content));
    }

    @PreAuthorize("@ss.hasPermi('mindcare:content:edit')")
    @Log(title = "MindCare内容", businessType = BusinessType.UPDATE)
    @PutMapping("/content")
    public AjaxResult editContent(@Validated @RequestBody MindcareContent content)
    {
        content.setUpdateBy(getUsername());
        return toAjax(service.updateContent(content));
    }

    @PreAuthorize("@ss.hasPermi('mindcare:content:remove')")
    @Log(title = "MindCare内容", businessType = BusinessType.DELETE)
    @DeleteMapping("/content/{contentIds}")
    public AjaxResult removeContent(@PathVariable Long[] contentIds)
    {
        return toAjax(service.deleteContentByIds(contentIds));
    }

    @PreAuthorize("@ss.hasPermi('mindcare:record:list')")
    @GetMapping("/record/list")
    public TableDataInfo recordList(MindcareRecord record)
    {
        startPage();
        return getDataTable(service.selectRecordList(record));
    }

    @PreAuthorize("@ss.hasPermi('mindcare:record:query')")
    @GetMapping("/record/{recordId}")
    public AjaxResult recordInfo(@PathVariable Long recordId)
    {
        return success(service.selectRecordById(recordId));
    }

    @PreAuthorize("@ss.hasPermi('mindcare:record:edit')")
    @Log(title = "MindCare业务记录", businessType = BusinessType.UPDATE)
    @PutMapping("/record/{recordId}/status")
    public AjaxResult updateRecordStatus(@PathVariable Long recordId, @RequestBody Map<String, String> body)
    {
        return toAjax(service.updateRecordStatus(recordId, body.get("status"), getUsername()));
    }

    @PreAuthorize("@ss.hasPermi('mindcare:client:list')")
    @GetMapping("/client/list")
    public TableDataInfo clientList(MindcareClient client)
    {
        startPage();
        return getDataTable(service.selectClientList(client));
    }

    @PreAuthorize("@ss.hasPermi('mindcare:client:list')")
    @GetMapping("/account/list")
    public TableDataInfo accountList(MindcareAccount account)
    {
        startPage();
        return getDataTable(service.selectAccountList(account));
    }
}
