package com.lx.callback.controller;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.extra.servlet.ServletUtil;
import com.alibaba.fastjson.JSON;
import com.lx.callback.model.IResult;
import com.lx.callback.properties.CallbackProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 回调接口
 *
 * @author chenhaizhuang
 */
@Slf4j
@RestController
public class CallbackController {

    @Autowired
    private CallbackProperties callbackProperties;

    @Autowired
    private RestTemplate restTemplate;

    @RequestMapping("/{requestCode}")
    public void handle(@PathVariable String requestCode, HttpServletRequest request, HttpServletResponse response) {
        try {
            Assert.isTrue(callbackProperties.getRequestCodes().containsKey(requestCode));
            String targetPath = callbackProperties.getRequestCodes().get(requestCode);

            HttpMethod method = HttpMethod.valueOf(request.getMethod());
            String url = targetPath + getParams(request);
            HttpHeaders headers = getHeaders(request);
            String requestBody = ServletUtil.getBody(request);

            log.info("转发：\n" +
                            "----------------请求地址----------------\n" +
                            "{} {}\n" +
                            "----------------请求头  ----------------\n" +
                            "{}\n" +
                            "----------------请求体  ----------------\n" +
                            "{}",
                    method.name(), url,
                    headers.keySet().stream()
                            .map(key -> key + ": " + String.join(","))
                            .collect(Collectors.joining("\n")),
                    requestBody);

            HttpEntity<String> httpEntity = new HttpEntity<>(requestBody, headers);
            ParameterizedTypeReference<IResult<String>> reference = new ParameterizedTypeReference<>() {
            };
            ResponseEntity<IResult<String>> responseEntity = restTemplate.exchange(url, method, httpEntity, reference);
            Assert.isTrue(responseEntity.getStatusCode().is2xxSuccessful());
            IResult<String> result = Objects.requireNonNull(responseEntity.getBody());
            Assert.isTrue(result.isSuccess());
            response.getWriter().write(result.getData());
            log.info("转发成功：{}", JSON.toJSONString(result));
        } catch (Exception e) {
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            log.info("转发失败", e);
        }
    }

    private String getParams(HttpServletRequest request) {
        Map<String, String[]> queryParams = request.getParameterMap();
        StringBuilder builder = new StringBuilder();
        String split = "?";
        for (String key : queryParams.keySet()) {
            String value = String.join(",", queryParams.get(key));
            builder.append(split).append(key).append("=").append(URLEncoder.encode(value, StandardCharsets.UTF_8));
            split = "&";
        }
        return builder.toString();
    }

    private HttpHeaders getHeaders(HttpServletRequest request) {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        Enumeration<String> names = request.getHeaderNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            headers.addAll(name, ListUtil.list(false, request.getHeaders(name)));
        }
        return new HttpHeaders(headers);
    }
}
