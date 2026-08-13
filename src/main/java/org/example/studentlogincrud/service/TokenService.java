package org.example.studentlogincrud.service;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenService {
    private static final long TOKEN_EXPIRE_SECONDS = 12 * 60 * 60;
    private final SecureRandom secureRandom = new SecureRandom();
    private final Map<String, LoginToken> tokens = new ConcurrentHashMap<>();

    public String create(Long adminId) {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        tokens.put(token, new LoginToken(adminId, Instant.now().plusSeconds(TOKEN_EXPIRE_SECONDS).getEpochSecond()));
        return token;
    }

    public boolean valid(String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }
        LoginToken loginToken = tokens.get(token);
        if (loginToken == null) {
            return false;
        }
        if (loginToken.expireAt < Instant.now().getEpochSecond()) {
            tokens.remove(token);
            return false;
        }
        return true;
    }

    public void remove(String token) {
        if (token != null) {
            tokens.remove(token);
        }
    }

    private static class LoginToken {
        private final Long adminId;
        private final long expireAt;

        private LoginToken(Long adminId, long expireAt) {
            this.adminId = adminId;
            this.expireAt = expireAt;
        }
    }
}
