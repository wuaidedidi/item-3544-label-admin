package com.label.admin.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * SPA前端路由转发控制器
 * 将所有非API、非静态资源请求转发到index.html，支持前后端不分离部署
 */
@Controller
public class SpaForwardController {

    @GetMapping(value = {"/", "/login", "/register", "/dashboard", "/users", "/roles", "/files", "/profile"})
    public String forward() {
        return "forward:/index.html";
    }
}
