package org.example.studentlogincrud.config;

import jakarta.servlet.http.HttpServletResponse;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.example.studentlogincrud.entity.Result;
import org.example.studentlogincrud.service.TokenService;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
public class TokenAuthAspect {
    private final TokenService tokenService;

    public TokenAuthAspect(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Around("execution(* org.example.studentlogincrud.controller..*(..))"
            + " && !execution(* org.example.studentlogincrud.controller.AdminController.login(..))"
            + " && !execution(* org.example.studentlogincrud.controller.AdminController.register(..))"
            + " && !execution(* org.example.studentlogincrud.controller.AdminController.verifyPassphrase(..))"
            + " && !execution(* org.example.studentlogincrud.controller.PublicScoreController.*(..))")
    public Object checkToken(ProceedingJoinPoint joinPoint) throws Throwable {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return joinPoint.proceed();
        }

        String token = attributes.getRequest().getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        if (!tokenService.valid(token)) {
            HttpServletResponse response = attributes.getResponse();
            if (response != null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            }
            return Result.error(401, "Login session has expired");
        }
        return joinPoint.proceed();
    }
}
