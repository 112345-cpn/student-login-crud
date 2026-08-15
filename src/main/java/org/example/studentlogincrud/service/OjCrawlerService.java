package org.example.studentlogincrud.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;

/**
 * OJ 分数爬虫：用已登录的会话 Cookie 调比赛排名接口，
 * 按 OJ 用户名（约定为学号）匹配，返回该学生的总分。
 */
@Service
public class OjCrawlerService {

    private static final Logger log = LoggerFactory.getLogger(OjCrawlerService.class);

    private final RestClient restClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String baseUrl;
    private final Long contestId;
    private final String contestPassword;
    private boolean contestAccessGranted = false;

    public OjCrawlerService(
            @Value("${oj.base-url:}") String baseUrl,
            @Value("${oj.contest-id:}") Long contestId,
            @Value("${oj.contest-password:}") String contestPassword,
            @Value("${oj.session-cookie:}") String sessionCookie) {
        this.baseUrl = baseUrl == null ? "" : baseUrl.trim();
        this.contestId = contestId;
        this.contestPassword = contestPassword == null ? "" : contestPassword.trim();

        RestClient.Builder builder = RestClient.builder().baseUrl(this.baseUrl);
        String cookie = sessionCookie == null ? "" : sessionCookie.trim();
        if (!cookie.isEmpty()) {
            builder.defaultHeader("Cookie", cookie);
            String csrf = extractCsrfToken(cookie);
            if (!csrf.isEmpty()) {
                builder.defaultHeader("X-CSRFToken", csrf);
            }
        }
        this.restClient = builder.build();
    }

    /** 爬虫是否启用：配置了 OJ 地址和比赛 id 才算启用。 */
    public boolean isEnabled() {
        return !baseUrl.isEmpty() && contestId != null;
    }

    /**
     * 按学号（OJ 用户名）爬取比赛总分。
     * 找不到该学生、或爬取失败时返回 null。
     */
    public BigDecimal fetchScore(String studentNo) {
        if (!isEnabled() || studentNo == null || studentNo.trim().isEmpty()) {
            return null;
        }
        ensureContestAccess();

        String uri = "/api/contest_rank/?contest_id=" + contestId;
        String body = restClient.get().uri(uri).retrieve().body(String.class);
        JsonNode root = parseJson(body);
        if (root == null) {
            log.warn("OJ 排名接口无响应");
            return null;
        }
        if (!root.path("error").isNull()) {
            log.warn("OJ 排名接口返回错误: {}", root.path("data").asText());
            return null;
        }

        String target = studentNo.trim();
        for (JsonNode row : root.path("data").path("results")) {
            String username = row.path("user").path("username").asText("");
            if (username.equals(target)) {
                String scoreText = row.path("total_score").asText("");
                if (scoreText.isEmpty()) {
                    return null;
                }
                return new BigDecimal(scoreText);
            }
        }
        return null;
    }

    /** 进入密码保护的比赛：先查访问状态，没权限就用密码提交。 */
    private void ensureContestAccess() {
        if (contestAccessGranted) {
            return;
        }
        try {
            String accessBody = restClient.get()
                    .uri("/api/contest/access/?contest_id=" + contestId)
                    .retrieve()
                    .body(String.class);
            JsonNode access = parseJson(accessBody);
            if (access != null && access.path("data").path("access").asBoolean(false)) {
                contestAccessGranted = true;
                return;
            }
            if (contestPassword.isEmpty()) {
                log.warn("OJ 比赛需要密码，但未配置 oj.contest-password");
                return;
            }
            restClient.post()
                    .uri("/api/contest/password/")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("{\"contest_id\":" + contestId + ",\"password\":\"" + contestPassword + "\"}")
                    .retrieve()
                    .body(String.class);
            contestAccessGranted = true;
        } catch (Exception e) {
            log.warn("进入 OJ 比赛失败: {}", e.getMessage());
        }
    }

    private JsonNode parseJson(String body) {
        if (body == null || body.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readTree(body);
        } catch (Exception e) {
            log.warn("解析 OJ 响应失败: {}", e.getMessage());
            return null;
        }
    }

    private String extractCsrfToken(String cookie) {
        for (String part : cookie.split(";")) {
            String[] kv = part.trim().split("=", 2);
            if (kv.length == 2 && "csrftoken".equalsIgnoreCase(kv[0])) {
                return kv[1];
            }
        }
        return "";
    }
}
