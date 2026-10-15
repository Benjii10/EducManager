package com.educmanager.config;

import com.educmanager.audit.AuditInterceptor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** Enregistre l'intercepteur d'audit sur l'API. [Benjamin – #04, 2026-10-15] Création. @author Benjamin */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final ObjectProvider<AuditInterceptor> auditInterceptor;

    public WebConfig(ObjectProvider<AuditInterceptor> auditInterceptor) {
        this.auditInterceptor = auditInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        auditInterceptor.ifAvailable(interceptor -> registry.addInterceptor(interceptor).addPathPatterns("/api/**"));
    }
}
