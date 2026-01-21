package edu.fudan.se.sctap_lowcode_tool.utils;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.digest.MD5;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.time.Duration;
import java.util.Map;
import java.util.function.Consumer;

/**
 * <h3>TslApiUtil 特斯联 API 工具类</h3>
 * 封装特斯联 API 的请求、签名与响应解析逻辑。
 * @author Lin Yicheng
 * @since 2026-01-19
 */
@Component
@RequiredArgsConstructor
public class TslApiUtil {

    @Value("${tsl.app.id}")
    private String appId;

    @Value("${tsl.app.code}")
    private String appCode;

    @Value("${tsl.app.token}")
    private String appToken;

    private final ObjectMapper objectMapper;

    /**
     * <h3>TslApiException 特斯联 API 异常</h3>
     * 包装网络、响应与解析过程中出现的错误。
     */
    public static class TslApiException extends Exception {
        @Nullable private final HttpStatusCode httpStatusCode;

        public TslApiException(String message) {
            super(message);
            httpStatusCode = null;
        }

        public TslApiException(String message, Throwable cause) {
            super(message, cause);
            httpStatusCode = null;
        }

        public TslApiException(String message, @Nullable HttpStatusCode httpStatusCode, Throwable cause) {
            super(message, cause);
            this.httpStatusCode = httpStatusCode;
        }

        @Override
        public String getMessage() {
            StringBuilder builder = new StringBuilder("特斯联API接口调用失败");
            if (httpStatusCode != null) {
                builder.append("(HTTP ").append(httpStatusCode.value()).append(")");
            }
            builder.append(": ").append(super.getMessage());
            if (getCause() != null) {
                builder
                    .append("\n Cause: ")
                    .append(getCause().getClass().getSimpleName())
                    .append("\n Message: ")
                    .append(getCause().getMessage());
            }
            return builder.toString();
        }
    }

    /**
     * RequestAction 请求动作
     * 用于构造并执行 RestClient 请求。
     */
    @FunctionalInterface
    public interface RequestAction {
        RestClient.ResponseSpec execute(RestClient restClient) throws Exception;
    }

    /**
     * 发起请求并解析响应
     *
     * @param action 请求动作
     * @param timeoutSec 超时时间（秒）
     * @return 解析后的 data 字段
     * @throws TslApiException 调用或解析失败时抛出
     */
    public Map<String, Object> fetch(RequestAction action, int timeoutSec) throws TslApiException {
        // 构建 RestClient
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(timeoutSec));
        requestFactory.setReadTimeout(Duration.ofSeconds(timeoutSec));
        RestClient restClient = RestClient.builder().requestFactory(requestFactory).build();

        // 获得响应
        String rawResponse = fetchRaw(restClient, action);

        // 解析响应
        return getData(rawResponse);
    }

    /**
     * 执行请求并获取原始响应体
     *
     * @param restClient RestClient 实例
     * @param action 请求动作
     * @return 原始响应体字符串
     * @throws TslApiException 调用失败时抛出
     */
    private String fetchRaw(RestClient restClient, RequestAction action)
        throws TslApiException {
        try {
            return action.execute(restClient).body(String.class);
        } catch (RestClientResponseException e) {
            String responseBody = e.getResponseBodyAsString();
            int status = e.getStatusCode().value();
            throw new TslApiException(
                String.format("API接口返回非2xx响应(HTTP %d):\n响应体:\n%s", status, responseBody), e.getStatusCode(), e
            );
        } catch (ResourceAccessException e) {
            throw new TslApiException("与API通信时发生I/O错误: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new TslApiException("调用API时发生意外错误: " + e.getMessage(), e);
        }
    }

    /**
     * 解析响应并提取 data 字段
     *
     * @param raw 原始响应体
     * @return data 字段内容
     * @throws TslApiException 解析失败或响应格式不正确时抛出
     */
    private Map<String, Object> getData(String raw) throws TslApiException {

        // 解析响应体 JSON 字符串为 Map<String, Object>
        Map<String, Object> responseBody;
        try {
            responseBody = objectMapper.readValue(raw, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            String msg = String.format("解析特斯联API接口的响应JSON失败: %s\n响应体:\n%s", e.getMessage(), raw);
            throw new TslApiException(msg, e);
        }

        // 确认 responseBody 的 success 为 true 且 data 字段存在
        try {
            if (responseBody == null || responseBody.get("success") == null)
                throw new IllegalArgumentException("特斯联API接口的响应格式不正确: 缺少 success 字段");
            if (!responseBody.get("success").equals(Boolean.TRUE))
                throw new IllegalArgumentException("特斯联API接口的响应表示请求未成功: success 不为 true");
            if (responseBody.get("data") == null)
                throw new IllegalArgumentException("特斯联API接口的响应格式不正确: 缺少 data 字段");
        } catch (IllegalArgumentException e) {
            if (responseBody == null)
                throw new TslApiException(e.getMessage());
            else {
                throw new TslApiException(String.format(
                    "%s\ncode=%s, message=%s, success=%s",
                    e.getMessage(),
                    responseBody.get("code"),
                    responseBody.get("message"),
                    responseBody.get("success")
                ));
            }
        }

        // 提取 data 字段
        Object dataObj = responseBody.get("data");
        try {
            return objectMapper.convertValue(dataObj, new TypeReference<>() {});
        } catch (IllegalArgumentException e) {
            String msg = String.format(
                "解析特斯联API接口的响应data字段失败: %s\n响应体:\n%s",
                e.getMessage(), raw
            );
            throw new TslApiException(msg, e);
        }

    }


    /**
     * 构建请求头（包含签名）
     *
     * @param queryString 查询字符串，用于参与签名（可为 null）
     * @return 请求头构造器
     */
    public Consumer<HttpHeaders> buildHeaders(@Nullable String queryString) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String nonce = String.valueOf(RandomUtil.randomInt(0, 9999));
        if (queryString == null) {queryString = "";}
        String signStr = queryString + appId + appToken + timestamp + nonce;
        String sign = MD5.create().digestHex(signStr, CharsetUtil.CHARSET_UTF_8);

        return headers -> {
            headers.set("Content-Type", "application/json");
            headers.set("appId", appId);
            headers.set("appCode", appCode);
            headers.set("nonce", nonce);
            headers.set("timestamp", timestamp);
            headers.set("sign", sign);
            headers.set("authorization", appToken);
        };
    }

}
