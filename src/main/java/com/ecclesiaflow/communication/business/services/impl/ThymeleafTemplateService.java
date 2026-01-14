package com.ecclesiaflow.communication.business.services.impl;

import com.ecclesiaflow.communication.business.services.TemplateService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.HashMap;
import java.util.Map;

/**
 * Thymeleaf-based template renderer with auto-injection of branding (logoUrl).
 *
 * @author EcclesiaFlow Team
 * @since 1.0.0
 */
@Service
public class ThymeleafTemplateService implements TemplateService {
    
    private final TemplateEngine templateEngine;
    private final String logoUrl;
    
    public ThymeleafTemplateService(
            TemplateEngine templateEngine,
            @Value("${ecclesiaflow.email.logo-url:}") String logoUrl) {
        this.templateEngine = templateEngine;
        this.logoUrl = logoUrl;
    }
    
    @Override
    public String renderTemplate(String templateName, Map<String, Object> variables) {
        Context context = new Context();
        
        Map<String, Object> allVariables = new HashMap<>();
        if (variables != null) {
            allVariables.putAll(variables);
        }
        
        // Inject branding variables
        if (logoUrl != null && !logoUrl.isBlank()) {
            allVariables.put("logoUrl", logoUrl);
        }
        
        context.setVariables(allVariables);
        return templateEngine.process(templateName, context);
    }
    
    @Override
    public String renderTemplateById(String templateId, Map<String, Object> variables) {
        return renderTemplate(templateId, variables);
    }
    
    @Override
    public boolean templateExists(String templateName) {
        try {
            templateEngine.process(templateName, new Context());
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
