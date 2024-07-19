package com.lx.callback.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 回调配置
 *
 * @author chenhaizhuang
 */
@Data
@Component
@ConfigurationProperties("callback")
public class CallbackProperties {

    /**
     * 请求码 -> 目标地址
     */
    private Map<String, String> requestCodes;
}
