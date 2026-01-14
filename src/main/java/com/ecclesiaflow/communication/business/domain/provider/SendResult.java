package com.ecclesiaflow.communication.business.domain.provider;

import lombok.*;

/**
 * Immutable result from an communication provider send operation.
 *
 * @author EcclesiaFlow Team
 * @since 1.0.0
 */
@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
public class SendResult {

    private boolean success;
    private String messageId;
    private String errorMessage;

    /**
     * Provider name used.
     */
    private String providerName;

    /**
     * Creates a success result.
     */
    public static SendResult success(String messageId, String providerName) {
        return SendResult.builder()
                .success(true)
                .messageId(messageId)
                .providerName(providerName)
                .build();
    }

    /**
     * Creates a failure result.
     */
    public static SendResult failure(String errorMessage, String providerName) {
        return SendResult.builder()
                .success(false)
                .errorMessage(errorMessage)
                .providerName(providerName)
                .build();
    }
}
