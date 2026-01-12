package edu.fudan.se.sctap_lowcode_tool.utils;

import org.springframework.util.DigestUtils;
import java.nio.charset.StandardCharsets;

public class SignUtil {

    /**
     * 计算 MD5 (对应 Python 的 hashlib.md5(...).hexdigest())
     */
    public static String md5Hex(String content) {
        return DigestUtils.md5DigestAsHex(content.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成签名
     * Python 逻辑: sign_raw = sign_query_str + app_id + token + timestamp + nonce
     * 针对 6.3 接口，query_params 为空，所以 sign_query_str 为空字符串。
     */
    public static String calculateSignature(String appId, String token, String timestamp, String nonce) {
        // 原始拼接字符串
        String raw = appId + token + timestamp + nonce;

        // 返回 MD5
        return md5Hex(raw);
    }
}