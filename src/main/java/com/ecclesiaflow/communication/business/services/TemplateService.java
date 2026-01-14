package com.ecclesiaflow.communication.business.services;

import java.util.Map;

/**
 * Template rendering service interface.
 *
 * @author EcclesiaFlow Team
 * @since 1.0.0
 */
public interface TemplateService {
    
    String renderTemplate(String templateName, Map<String, Object> variables);
    
    String renderTemplateById(String templateId, Map<String, Object> variables);
    
    boolean templateExists(String templateName);
}
