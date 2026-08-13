package org.example.studentlogincrud.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Service
public class AccessPassphraseService {
    private final String passphrase;

    public AccessPassphraseService(@Value("${app.admin.access-passphrase}") String passphrase) {
        this.passphrase = passphrase;
    }

    public boolean matches(String submittedPassphrase) {
        if (submittedPassphrase == null || submittedPassphrase.isBlank()) {
            return false;
        }
        return MessageDigest.isEqual(
                passphrase.getBytes(StandardCharsets.UTF_8),
                submittedPassphrase.trim().getBytes(StandardCharsets.UTF_8)
        );
    }
}
