package com.ecclesiaflow.communication.application.logging;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityMaskingUtilsTest {

    @Nested
    class MaskEmail {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t"})
        void shouldReturnUnknownForNullOrBlank(String email) {
            assertThat(SecurityMaskingUtils.maskEmail(email)).isEqualTo("[UNKNOWN]");
        }

        @Test
        void shouldReturnInvalidFormatWhenNoAtSign() {
            assertThat(SecurityMaskingUtils.maskEmail("invalid")).isEqualTo("[INVALID_FORMAT]");
        }

        @Test
        void shouldReturnInvalidFormatWhenAtSignAtStart() {
            assertThat(SecurityMaskingUtils.maskEmail("@domain.com")).isEqualTo("[INVALID_FORMAT]");
        }

        @Test
        void shouldMaskLocalPartWithTwoCharsVisible() {
            assertThat(SecurityMaskingUtils.maskEmail("john.doe@example.com")).isEqualTo("jo****@example.com");
        }

        @Test
        void shouldMaskShortLocalPartWithOneCharVisible() {
            assertThat(SecurityMaskingUtils.maskEmail("ab@example.com")).isEqualTo("a****@example.com");
            assertThat(SecurityMaskingUtils.maskEmail("a@example.com")).isEqualTo("a****@example.com");
        }
    }

    @Nested
    class MaskId {

        @Test
        void shouldReturnUnknownForNull() {
            assertThat(SecurityMaskingUtils.maskId(null)).isEqualTo("[UNKNOWN]");
        }

        @Test
        void shouldReturnUnknownForBlankString() {
            assertThat(SecurityMaskingUtils.maskId("   ")).isEqualTo("[UNKNOWN]");
        }

        @Test
        void shouldMaskUuidShowingFirst8Chars() {
            UUID uuid = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
            String masked = SecurityMaskingUtils.maskId(uuid);
            assertThat(masked).isEqualTo("a1b2c3d4********");
        }

        @Test
        void shouldMaskStringUuidShowingFirst8Chars() {
            String masked = SecurityMaskingUtils.maskId("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
            assertThat(masked).isEqualTo("a1b2c3d4********");
        }

        @Test
        void shouldMaskShortIdCompletely() {
            assertThat(SecurityMaskingUtils.maskId("abc")).isEqualTo("********");
            assertThat(SecurityMaskingUtils.maskId("12345678")).isEqualTo("********");
        }

        @Test
        void shouldMaskLongNonUuidIdShowingFirst8Chars() {
            assertThat(SecurityMaskingUtils.maskId("123456789012345")).isEqualTo("12345678********");
        }
    }

    @Nested
    class MaskUrlQueryParam {

        @Test
        void shouldReturnUnknownForNullUrl() {
            assertThat(SecurityMaskingUtils.maskUrlQueryParam(null, "token")).isEqualTo("[UNKNOWN]");
        }

        @Test
        void shouldReturnUnknownForBlankUrl() {
            assertThat(SecurityMaskingUtils.maskUrlQueryParam("   ", "token")).isEqualTo("[UNKNOWN]");
        }

        @Test
        void shouldReturnUrlPlaceholderForNullParamName() {
            assertThat(SecurityMaskingUtils.maskUrlQueryParam("https://example.com?token=abc", null)).isEqualTo("[URL]");
        }

        @Test
        void shouldReturnUrlPlaceholderForBlankParamName() {
            assertThat(SecurityMaskingUtils.maskUrlQueryParam("https://example.com?token=abc", "   ")).isEqualTo("[URL]");
        }

        @Test
        void shouldReturnUrlPlaceholderForNoQueryString() {
            assertThat(SecurityMaskingUtils.maskUrlQueryParam("https://example.com/path", "token")).isEqualTo("[URL]");
        }

        @Test
        void shouldMaskSpecifiedParameter() {
            String url = "https://example.com/reset?token=secret123&user=john";
            String masked = SecurityMaskingUtils.maskUrlQueryParam(url, "token");
            assertThat(masked).isEqualTo("https://example.com/reset?token=****&user=[REDACTED]");
        }

        @Test
        void shouldHandleParamWithoutValue() {
            String url = "https://example.com?flag&token=secret";
            String masked = SecurityMaskingUtils.maskUrlQueryParam(url, "token");
            assertThat(masked).contains("flag").contains("token=****");
        }
    }

    @Nested
    class MaskTokenLink {

        @Test
        void shouldMaskTokenParameter() {
            String link = "https://app.com/confirm?token=abc123";
            assertThat(SecurityMaskingUtils.maskTokenLink(link)).isEqualTo("https://app.com/confirm?token=****");
        }
    }

    @Nested
    class MaskArgs {

        @Test
        void shouldReturnEmptyArrayForNull() {
            assertThat(SecurityMaskingUtils.maskArgs(null)).isEqualTo("[]");
        }

        @Test
        void shouldReturnEmptyArrayForEmptyArray() {
            assertThat(SecurityMaskingUtils.maskArgs(new Object[]{})).isEqualTo("[]");
        }

        @Test
        void shouldMaskEmailsInArgs() {
            Object[] args = {"john@example.com", "plain text"};
            String masked = SecurityMaskingUtils.maskArgs(args);
            assertThat(masked).contains("jo****@example.com");
            assertThat(masked).contains("plain text");
        }

        @Test
        void shouldMaskMultipleSensitiveArgs() {
            Object[] args = {"user@test.com", "Bearer eyJhbGciOiJIUzI1NiJ9.payload.signature"};
            String masked = SecurityMaskingUtils.maskArgs(args);
            assertThat(masked).contains("us****@test.com");
            assertThat(masked).contains("Bearer ****");
        }
    }

    @Nested
    class MaskAny {

        @Test
        void shouldReturnUnknownForNull() {
            assertThat(SecurityMaskingUtils.maskAny(null)).isEqualTo("[UNKNOWN]");
        }

        @Test
        void shouldReturnUnknownForBlank() {
            assertThat(SecurityMaskingUtils.maskAny("   ")).isEqualTo("[UNKNOWN]");
        }

        @Test
        void shouldMaskEmail() {
            assertThat(SecurityMaskingUtils.maskAny("test@domain.com")).isEqualTo("te****@domain.com");
        }

        @Test
        void shouldRedactJwt() {
            String jwt = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyIn0.signature";
            assertThat(SecurityMaskingUtils.maskAny(jwt)).isEqualTo("[REDACTED]");
        }

        @Test
        void shouldMaskUrlWithToken() {
            String url = "https://app.com/reset?token=secret";
            assertThat(SecurityMaskingUtils.maskAny(url)).contains("token=****");
        }

        @Test
        void shouldReturnUrlPlaceholderForHttpsUrlWithoutToken() {
            assertThat(SecurityMaskingUtils.maskAny("https://example.com/page")).isEqualTo("[URL]");
        }

        @Test
        void shouldReturnUrlPlaceholderForHttpUrlWithoutToken() {
            assertThat(SecurityMaskingUtils.maskAny("http://example.com/page")).isEqualTo("[URL]");
        }

        @Test
        void shouldMaskBearerToken() {
            assertThat(SecurityMaskingUtils.maskAny("Bearer abc123xyz")).isEqualTo("Bearer ****");
            assertThat(SecurityMaskingUtils.maskAny("bearer ABC123")).isEqualTo("Bearer ****");
        }

        @Test
        void shouldAbbreviateLongText() {
            String longText = "a".repeat(200);
            String masked = SecurityMaskingUtils.maskAny(longText);
            assertThat(masked).hasSize(123); // 120 + "..."
            assertThat(masked).endsWith("...");
        }
    }

    @Nested
    class RootMessage {

        @Test
        void shouldReturnNoErrorForNull() {
            assertThat(SecurityMaskingUtils.rootMessage(null)).isEqualTo("[NO_ERROR]");
        }

        @Test
        void shouldReturnMessageFromSimpleException() {
            Exception ex = new RuntimeException("Something went wrong");
            assertThat(SecurityMaskingUtils.rootMessage(ex)).isEqualTo("Something went wrong");
        }

        @Test
        void shouldReturnRootCauseMessage() {
            Exception root = new IllegalArgumentException("Root cause");
            Exception wrapper = new RuntimeException("Wrapper", root);
            assertThat(SecurityMaskingUtils.rootMessage(wrapper)).isEqualTo("Root cause");
        }

        @Test
        void shouldReturnClassNameWhenNoMessage() {
            Exception ex = new NullPointerException();
            assertThat(SecurityMaskingUtils.rootMessage(ex)).isEqualTo("NullPointerException");
        }

        @Test
        void shouldReturnClassNameWhenMessageIsBlank() {
            Exception ex = new RuntimeException("   ");
            assertThat(SecurityMaskingUtils.rootMessage(ex)).isEqualTo("RuntimeException");
        }

        @Test
        void shouldSanitizeInfraFromMessage() {
            Exception ex = new RuntimeException("Failed to connect to localhost:5432");
            assertThat(SecurityMaskingUtils.rootMessage(ex)).isEqualTo("Failed to connect to [HOST:PORT]");
        }
    }

    @Nested
    class SanitizeInfra {

        @Test
        void shouldReturnNullForNull() {
            assertThat(SecurityMaskingUtils.sanitizeInfra(null)).isNull();
        }

        @Test
        void shouldReturnBlankForBlank() {
            assertThat(SecurityMaskingUtils.sanitizeInfra("   ")).isEqualTo("   ");
        }

        @Test
        void shouldReplaceUrls() {
            String msg = "Error at https://api.example.com/endpoint";
            assertThat(SecurityMaskingUtils.sanitizeInfra(msg)).isEqualTo("Error at [URL]");
        }

        @Test
        void shouldReplaceHostPort() {
            String msg = "Connection refused: db-server:5432";
            assertThat(SecurityMaskingUtils.sanitizeInfra(msg)).isEqualTo("Connection refused: [HOST:PORT]");
        }

        @Test
        void shouldReplaceDomainNames() {
            String msg = "DNS lookup failed for api.example.com";
            assertThat(SecurityMaskingUtils.sanitizeInfra(msg)).isEqualTo("DNS lookup failed for [HOST]");
        }

        @Test
        void shouldPreserveEmailFormat() {
            String msg = "Invalid communication: user@test.org";
            String result = SecurityMaskingUtils.sanitizeInfra(msg);
            assertThat(result).contains("user@");
        }
    }

    @Nested
    class Abbreviate {

        @Test
        void shouldReturnUnknownForNull() {
            assertThat(SecurityMaskingUtils.abbreviate(null, 10)).isEqualTo("[UNKNOWN]");
        }

        @Test
        void shouldReturnTextIfShorterThanMax() {
            assertThat(SecurityMaskingUtils.abbreviate("short", 10)).isEqualTo("short");
        }

        @Test
        void shouldTruncateAndAddEllipsis() {
            assertThat(SecurityMaskingUtils.abbreviate("this is a long text", 10)).isEqualTo("this is a ...");
        }
    }

    @Nested
    class FormatMethod {

        @Test
        void shouldReturnMethodNameOnlyWhenClassNameNull() {
            assertThat(SecurityMaskingUtils.formatMethod(null, "sendEmail")).isEqualTo("sendEmail");
        }

        @Test
        void shouldReturnMethodNameOnlyWhenClassNameBlank() {
            assertThat(SecurityMaskingUtils.formatMethod("   ", "sendEmail")).isEqualTo("sendEmail");
        }

        @Test
        void shouldReturnUnknownWhenBothNull() {
            assertThat(SecurityMaskingUtils.formatMethod(null, null)).isEqualTo("[UNKNOWN]");
        }

        @Test
        void shouldExtractSimpleClassName() {
            String result = SecurityMaskingUtils.formatMethod("com.example.EmailService", "sendEmail");
            assertThat(result).isEqualTo("EmailService.sendEmail");
        }

        @Test
        void shouldHandleSimpleClassNameDirectly() {
            String result = SecurityMaskingUtils.formatMethod("EmailService", "sendEmail");
            assertThat(result).isEqualTo("EmailService.sendEmail");
        }

        @Test
        void shouldHandleNullMethodName() {
            String result = SecurityMaskingUtils.formatMethod("EmailService", null);
            assertThat(result).isEqualTo("EmailService.[UNKNOWN]");
        }
    }
}
