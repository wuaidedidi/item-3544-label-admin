package com.label.admin.init;

import com.label.admin.entity.SysUser;
import com.label.admin.mapper.SysUserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final SysUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(SysUserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        initUserPasswords();
    }

    private void initUserPasswords() {
        log.info("开始初始化用户密码...");

        fixPassword("admin", "123456");
        fixPassword("user1", "123456");
        fixPassword("user2", "123456");

        log.info("用户密码初始化完成");
    }

    private void fixPassword(String username, String rawPassword) {
        try {
            SysUser user = userMapper.selectByUsername(username);
            if (user != null) {
                boolean needUpdate = false;
                try {
                    needUpdate = !passwordEncoder.matches(rawPassword, user.getPassword());
                } catch (Exception e) {
                    needUpdate = true;
                }

                if (needUpdate) {
                    user.setPassword(passwordEncoder.encode(rawPassword));
                    userMapper.updateById(user);
                    log.info("用户 {} 密码已重新初始化", username);
                } else {
                    log.info("用户 {} 密码已是正确的BCrypt格式", username);
                }
            }
        } catch (Exception e) {
            log.error("初始化用户 {} 密码失败: {}", username, e.getMessage());
        }
    }
}
