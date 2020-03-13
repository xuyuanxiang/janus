package com.github.xuyuanxiang.janus.service;

import org.springframework.context.MessageSource;
import org.springframework.context.support.ResourceBundleMessageSource;

public class JanusMessageSource extends ResourceBundleMessageSource {
    public static final MessageSource INSTANCE = new JanusMessageSource();
    private JanusMessageSource() {
        setBasename("janus");
    }
}
