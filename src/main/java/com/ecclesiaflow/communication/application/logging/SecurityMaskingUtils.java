package com.ecclesiaflow.communication.application.logging;

import java.util.Arrays;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Utility class for masking sensitive data in logs.
 *
 * @author EcclesiaFlow Team
 * @since 1.0.0
 */
public final class SecurityMaskingUtils {

    private static final String MASK = "****";
    private static final String UNKNOWN = "[UNKNOWN]";
    private static final String INVALID_FORMAT = "[INVALID_FORMAT]";
    private static final String URL_MASKING_ERROR = "[URL_MASKING_ERROR]";
    private static final String REDACTED = "[REDACTED]";
    private static final int UUID_VISIBLE_LENGTH = 8;
    private static final int DEFAULT_ABBREVIATE_LENGTH = 120;

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private static final Pattern JWT_PATTERN =
            Pattern.compile("^[A-Za-z0-9\\-_]+\\.[A-Za-z0-9\\-_]+\\.[A-Za-z0-9\\-_]+$");

    private SecurityMaskingUtils() {
    }

    public static String maskEmail(String email) {
        if (email == null || email.isBlank()) {
            return UNKNOWN;
        }

        int atIndex = email.indexOf('@');
        if (atIndex <= 0) {
            return INVALID_FORMAT;
        }

        String localPart = email.substring(0, atIndex);
        String domain = email.substring(atIndex);

        if (localPart.length() <= 2) {
            return localPart.charAt(0) + MASK + domain;
        }
        return localPart.substring(0, 2) + MASK + domain;
    }

    public static String maskId(Object id) {
        if (id == null) {
            return UNKNOWN;
        }

        String idString = String.valueOf(id);
        if (idString.isBlank()) {
            return UNKNOWN;
        }

        if (isValidUuid(idString)) {
            return idString.substring(0, UUID_VISIBLE_LENGTH) + "********";
        }

        if (idString.length() <= UUID_VISIBLE_LENGTH) {
            return "********";
        }
        return idString.substring(0, UUID_VISIBLE_LENGTH) + "********";
    }

    public static String maskUrlQueryParam(String url, String paramName) {
        if (url == null || url.isBlank()) {
            return UNKNOWN;
        }
        if (paramName == null || paramName.isBlank()) {
            return "[URL]";
        }

        try {
            int queryIndex = url.indexOf('?');
            if (queryIndex < 0) {
                return "[URL]";
            }

            String basePath = url.substring(0, queryIndex);
            String queryString = url.substring(queryIndex + 1);
            String[] params = queryString.split("&");
            StringBuilder maskedQuery = new StringBuilder();

            for (String param : params) {
                if (!maskedQuery.isEmpty()) {
                    maskedQuery.append("&");
                }

                int equalsIndex = param.indexOf('=');
                if (equalsIndex < 0) {
                    maskedQuery.append(param);
                    continue;
                }

                String key = param.substring(0, equalsIndex);
                maskedQuery.append(key).append("=");
                maskedQuery.append(key.equals(paramName) ? MASK : REDACTED);
            }

            return basePath + "?" + maskedQuery;
        } catch (Exception e) {
            return URL_MASKING_ERROR;
        }
    }

    public static String maskTokenLink(String link) {
        return maskUrlQueryParam(link, "token");
    }

    public static String maskArgs(Object[] args) {
        if (args == null || args.length == 0) {
            return "[]";
        }

        String[] maskedArgs = new String[args.length];
        for (int i = 0; i < args.length; i++) {
            maskedArgs[i] = maskAny(args[i]);
        }
        return Arrays.toString(maskedArgs);
    }

    public static String maskAny(Object value) {
        if (value == null) {
            return UNKNOWN;
        }

        String raw = String.valueOf(value);
        if (raw.isBlank()) {
            return UNKNOWN;
        }

        // Email detection
        if (EMAIL_PATTERN.matcher(raw).matches()) {
            return maskEmail(raw);
        }

        // JWT detection
        if (JWT_PATTERN.matcher(raw).matches()) {
            return REDACTED;
        }

        // URL detection
        if (raw.startsWith("http://") || raw.startsWith("https://")) {
            return raw.contains("token=") ? maskTokenLink(raw) : "[URL]";
        }

        // Bearer token detection
        if (raw.regionMatches(true, 0, "bearer ", 0, 7)) {
            return "Bearer " + MASK;
        }

        return abbreviate(raw, DEFAULT_ABBREVIATE_LENGTH);
    }

    public static String rootMessage(Throwable throwable) {
        if (throwable == null) {
            return "[NO_ERROR]";
        }

        Throwable current = throwable;
        while (current.getCause() != null) {
            current = current.getCause();
        }

        String message = current.getMessage();
        if (message != null && !message.isBlank()) {
            return sanitizeInfra(message);
        }
        return current.getClass().getSimpleName();
    }

    public static String sanitizeInfra(String message) {
        if (message == null || message.isBlank()) {
            return message;
        }

        String sanitized = message;
        // Replace URLs
        sanitized = sanitized.replaceAll("https?://[^\\s]+", "[URL]");
        // Replace host:port patterns
        sanitized = sanitized.replaceAll("[a-zA-Z0-9._-]+:\\d{2,5}", "[HOST:PORT]");
        // Replace bare domain names (but not emails)
        sanitized = sanitized.replaceAll("(?<!@)[a-zA-Z0-9._-]+\\.[a-zA-Z]{2,}(?![a-zA-Z0-9._-])", "[HOST]");

        return sanitized;
    }

    public static String abbreviate(String text, int maxLength) {
        if (text == null) {
            return UNKNOWN;
        }
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength) + "...";
    }

    public static String formatMethod(String className, String methodName) {
        if (className == null || className.isBlank()) {
            return methodName != null ? methodName : UNKNOWN;
        }
        // Extract simple class name if fully qualified
        int lastDot = className.lastIndexOf('.');
        String simpleName = lastDot >= 0 ? className.substring(lastDot + 1) : className;
        return simpleName + "." + (methodName != null ? methodName : UNKNOWN);
    }

    private static boolean isValidUuid(String value) {
        try {
            UUID.fromString(value);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
