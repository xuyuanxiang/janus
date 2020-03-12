package com.github.xuyuanxiang.janus.model;

import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;

@Data
public abstract class AlipayBaseResponse implements Serializable {
    protected ErrorResponse errorResponse;

    abstract public boolean isSuccess();

    @Data
    public static class ErrorResponse {
        private String code;
        private String msg;
        private String subCode;
        private String subMsg;

        @Override
        public String toString() {
            return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
                .append("code", code)
                .append("msg", msg)
                .append("subCode", subCode)
                .append("subMsg", subMsg)
                .toString();
        }
    }
}
