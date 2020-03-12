package com.github.xuyuanxiang.janus.service;

import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;

import java.util.Map;

public class NoopFindByIndexNameSessionRepository<S extends Session> implements FindByIndexNameSessionRepository<S> {
    @Override
    public Map<String, S> findByIndexNameAndIndexValue(String s, String s1) {
        return null;
    }

    @Override
    public S createSession() {
        return null;
    }

    @Override
    public void save(S s) {

    }

    @Override
    public S findById(String s) {
        return null;
    }

    @Override
    public void deleteById(String s) {

    }
}
