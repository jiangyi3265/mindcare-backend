package com.ruoyi.web.core.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.mapper.SysUserMapper;

/**
 * Allows a deployment to inject the first administrator password without
 * placing a reusable password or hash in source control.
 */
@Component
public class AdminPasswordBootstrap implements ApplicationRunner
{
    private static final Logger log = LoggerFactory.getLogger(AdminPasswordBootstrap.class);

    private final SysUserMapper userMapper;

    @Value("${mindcare.admin-bootstrap-password:}")
    private String bootstrapPassword;

    public AdminPasswordBootstrap(SysUserMapper userMapper)
    {
        this.userMapper = userMapper;
    }

    @Override
    public void run(ApplicationArguments args)
    {
        if (StringUtils.isEmpty(bootstrapPassword))
        {
            log.warn("ADMIN_BOOTSTRAP_PASSWORD 未设置，初始化数据库中的 admin 随机密码不会被修改");
            return;
        }
        if (bootstrapPassword.length() < 8 || bootstrapPassword.length() > 50)
        {
            throw new IllegalStateException("ADMIN_BOOTSTRAP_PASSWORD 长度必须为 8 到 50 个字符");
        }
        userMapper.resetUserPwd(1L, SecurityUtils.encryptPassword(bootstrapPassword));
        log.info("已通过环境变量更新初始管理员密码；部署完成后建议移除 ADMIN_BOOTSTRAP_PASSWORD");
    }
}
