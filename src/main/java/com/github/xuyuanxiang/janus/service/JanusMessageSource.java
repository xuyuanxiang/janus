package com.github.xuyuanxiang.janus.service;

import org.springframework.context.support.ResourceBundleMessageSource;

public class JanusMessageSource extends ResourceBundleMessageSource {
    public JanusMessageSource() {
        setBasename("janus");
    }
}
