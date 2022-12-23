package top.xiqiu.north.support;

import top.xiqiu.north.core.JsonConverter;
import top.xiqiu.north.util.AESUtils;
import top.xiqiu.north.util.DigestUtils;
import top.xiqiu.north.util.NorthUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 安全的 Cookie
 */
public class SecretCookie extends Cookie {
    /**
     * 加密密钥，建议修改
     */
    private String secretKey = "default-2202-01-01";

    private final HttpServletRequest request;

    public SecretCookie(HttpServletRequest request, String cookieName) {
        this(request, cookieName, 86400);
    }

    public SecretCookie(HttpServletRequest request, String cookieName, int expiry) {
        super(cookieName, null);
        super.setMaxAge(expiry);
        this.request = request;
    }

    /**
     * 覆盖setMaxAge，使其失效
     */
    @Override
    public void setMaxAge(int expiry) {

    }

    /**
     * 设置 - 加密密钥
     *
     * @param secretKey 加密密钥
     */
    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    /**
     * 设置 Cookie
     *
     * @param data cookie数据，不建议存储很多数据
     */
    public void setValue(Map data) {
        // 为了使加密后的Cookie是变化的，且cookie的有效期不宜过长，避免被攻击
        int timestamp = (int) Math.ceil(new Date().getTime() / 1000);
        data.put("timestamp", timestamp + getMaxAge());

        String encryptCookie = AESUtils.encrypt(getKey(), new JsonConverter().stringify(data));
        setValue(encryptCookie);
    }

    /**
     * 获得加密密钥
     */
    private String getKey() {
        String domain = request.getServerName();
        if ("".equals(domain)) {
            domain = "127.0.0.1";
        }

        return DigestUtils.getMD5(secretKey + domain).substring(0, 16);
    }

    /**
     * 获取解密后的Cookie
     */
    public Map<String, Object> getCookie() {
        String cookieName = getName();
        Cookie[] cookies = request.getCookies();

        if (cookies == null) {
            return Map.of();
        }

        String encryptCookie = null;
        for (Cookie cookie : cookies) {
            if (cookieName.equals(cookie.getName())) {
                encryptCookie = cookie.getValue();
                break;
            }
        }

        // 没找到加密的cookie，返回空
        if (encryptCookie == null) {
            return Map.of();
        }

        String domain = request.getServerName();
        if ("".equals(domain)) {
            domain = "127.0.0.1";
        }

        // 解密Cookie
        String plainCookie = AESUtils.decrypt(getKey(), encryptCookie);

        // 解密失败
        if (NorthUtils.isBlank(plainCookie)) {
            return Map.of();
        }

        LinkedHashMap<String, Object> data = new JsonConverter().parse(plainCookie, LinkedHashMap.class);

        // 解密失败
        if (data == null) {
            return Map.of();
        }

        // 没有 timestamp 字段
        if (!data.containsKey("timestamp")) {
            return Map.of();
        }

        // 无效的Cookie时间戳，过期了
        final int timestamp = Double.valueOf(data.get("timestamp").toString()).intValue();
        if (timestamp < new Date().getTime() / 1000) {
            return Map.of();
        }

        // 删除timestamp
        data.remove("timestamp");

        return data;
    }
}
