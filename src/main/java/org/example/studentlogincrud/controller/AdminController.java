package org.example.studentlogincrud.controller;

import org.example.studentlogincrud.dto.AdminLoginResponse;
import org.example.studentlogincrud.entity.Admin;
import org.example.studentlogincrud.entity.Result;
import org.example.studentlogincrud.service.AccessPassphraseService;
import org.example.studentlogincrud.service.AdminService;
import org.example.studentlogincrud.service.TokenService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private static final String PASSPHRASE_HEADER = "X-Access-Passphrase";

    private final AdminService adminService;
    private final TokenService tokenService;
    private final AccessPassphraseService accessPassphraseService;

    public AdminController(AdminService adminService, TokenService tokenService,
                           AccessPassphraseService accessPassphraseService) {
        this.adminService = adminService;
        this.tokenService = tokenService;
        this.accessPassphraseService = accessPassphraseService;
    }

    @PostMapping("/verify-passphrase")
    public Result<Object> verifyPassphrase(
            @RequestHeader(value = PASSPHRASE_HEADER, required = false) String passphrase
    ) {
        return accessPassphraseService.matches(passphrase)
                ? Result.success()
                : Result.error(403, "Access passphrase is incorrect");
    }
    @PostMapping("/login")
    public Result<AdminLoginResponse> login(@RequestBody Admin admin,
                                             @RequestHeader(value = PASSPHRASE_HEADER, required = false) String passphrase) {
        Result<AdminLoginResponse> passphraseCheck = validatePassphrase(passphrase);
        if (passphraseCheck != null) {
            return passphraseCheck;
        }
        if (admin == null || isBlank(admin.getUsername())) {
            return Result.error(400, "Please enter an administrator name");
        }
        if (isBlank(admin.getPassword())) {
            return Result.error(400, "Please enter a password");
        }
        AdminLoginResponse response = adminService.login(admin);
        return response == null
                ? Result.error(401, "Administrator name or password is incorrect")
                : Result.success(response);
    }

    @PostMapping("/register")
    public Result<AdminLoginResponse> register(@RequestBody Admin admin,
                                                @RequestHeader(value = PASSPHRASE_HEADER, required = false) String passphrase) {
        Result<AdminLoginResponse> passphraseCheck = validatePassphrase(passphrase);
        if (passphraseCheck != null) {
            return passphraseCheck;
        }
        if (admin == null || isBlank(admin.getUsername())) {
            return Result.error(400, "Please enter an administrator name");
        }
        if (isBlank(admin.getPassword())) {
            return Result.error(400, "Please enter a password");
        }
        if (admin.getPassword().trim().length() < 6) {
            return Result.error(400, "Password must contain at least 6 characters");
        }
        AdminLoginResponse response = adminService.register(admin);
        return response == null
                ? Result.error(409, "Administrator name already exists")
                : Result.success(response);
    }

    @PostMapping("/logout")
    public Result<Object> logout(@RequestHeader(value = "Authorization", required = false) String authorization) {
        tokenService.remove(extractToken(authorization));
        return Result.success();
    }

    private Result<AdminLoginResponse> validatePassphrase(String passphrase) {
        return accessPassphraseService.matches(passphrase)
                ? null
                : Result.error(403, "Access passphrase is incorrect");
    }

    private String extractToken(String authorization) {
        if (authorization == null) {
            return null;
        }
        return authorization.startsWith("Bearer ") ? authorization.substring(7) : authorization;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
