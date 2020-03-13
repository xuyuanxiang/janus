package com.github.xuyuanxiang.janus;

import com.github.xuyuanxiang.janus.model.JanusAuthentication;
import com.github.xuyuanxiang.janus.model.User;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profile")
public class MyController {
    @GetMapping
    public User getUser() {
        JanusAuthentication authentication = (JanusAuthentication) SecurityContextHolder.getContext().getAuthentication();
        return authentication.getUser();
    }
}
