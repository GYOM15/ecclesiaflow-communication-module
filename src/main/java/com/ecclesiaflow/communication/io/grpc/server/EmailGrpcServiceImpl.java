package com.ecclesiaflow.communication.io.grpc.server;

import com.ecclesiaflow.communication.business.domain.email.Email;
import com.ecclesiaflow.communication.business.domain.email.EmailPriority;
import com.ecclesiaflow.communication.business.domain.email.EmailStatus;
import com.ecclesiaflow.communication.business.services.EmailService;
import com.ecclesiaflow.grpc.email.*;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

/**
 * gRPC service implementation for Email.
 * <p>
 * Exposes communication sending and tracking operations via gRPC
 * for high-performance inter-service communication.
 * </p>
 *
 * <p><strong>Architectural Role:</strong> gRPC Service - Inter-service API</p>
 *
 * @author EcclesiaFlow Team
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "grpc.enabled", havingValue = "true", matchIfMissing = false)
public class EmailGrpcServiceImpl extends EmailServiceGrpc.EmailServiceImplBase {

    private final EmailService emailService;
    private final com.ecclesiaflow.communication.business.services.TemplateResolver templateResolver;

    @Override
    public void sendEmail(SendEmailRequest request, StreamObserver<SendEmailResponse> responseObserver) {
        try {
            // Convert Protobuf to Domain
            Email email = convertToDomain(request);

            // Send via business service
            Email sentEmail = emailService.queueEmail(email);

            // Convert Domain to Protobuf
            SendEmailResponse response = SendEmailResponse.newBuilder()
                    .setEmailId(sentEmail.getId().toString())
                    .setStatus(convertStatus(sentEmail.getStatus()))
                    .setQueuedAt(toEpochMillis(sentEmail.getQueuedAt()))
                    .setMessage("Email queued successfully")
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void getEmailStatus(EmailStatusRequest request, StreamObserver<EmailStatusResponse> responseObserver) {
        try {
            UUID emailId = UUID.fromString(request.getEmailId());
            Optional<Email> emailOpt = emailService.findById(emailId);

            if (emailOpt.isEmpty()) {
                responseObserver.onError(io.grpc.Status.NOT_FOUND
                        .withDescription("Email not found")
                        .asRuntimeException());
                return;
            }

            Email email = emailOpt.get();
            EmailStatusResponse response = EmailStatusResponse.newBuilder()
                    .setEmailId(email.getId().toString())
                    .addAllTo(email.getToAddresses())
                    .setSubject(email.getSubject())
                    .setStatus(convertStatus(email.getStatus()))
                    .setProvider(email.getProvider() != null ? email.getProvider() : "")
                    .setQueuedAt(toEpochMillis(email.getQueuedAt()))
                    .setSentAt(toEpochMillis(email.getSentAt()))
                    .setDeliveredAt(toEpochMillis(email.getDeliveredAt()))
                    .setFailedAt(toEpochMillis(email.getFailedAt()))
                    .setOpenedAt(0) // TODO: tracking
                    .setClicks(0) // TODO: tracking
                    .setErrorMessage(email.getErrorMessage() != null ? email.getErrorMessage() : "")
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void sendBulkEmails(SendBulkEmailsRequest request, StreamObserver<SendBulkEmailsResponse> responseObserver) {
        int queued = 0;
        int failed = 0;
        List<SendEmailResponse> results = new ArrayList<>();

        for (SendEmailRequest emailRequest : request.getEmailsList()) {
            try {
                Email email = convertToDomain(emailRequest);
                Email sentEmail = emailService.queueEmail(email);
                
                results.add(SendEmailResponse.newBuilder()
                        .setEmailId(sentEmail.getId().toString())
                        .setStatus(convertStatus(sentEmail.getStatus()))
                        .setQueuedAt(toEpochMillis(sentEmail.getQueuedAt()))
                        .build());
                
                queued++;
            } catch (Exception e) {
                failed++;
            }
        }

        SendBulkEmailsResponse response = SendBulkEmailsResponse.newBuilder()
                .addAllResults(results)
                .setTotal(request.getEmailsCount())
                .setQueued(queued)
                .setFailed(failed)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private Email convertToDomain(SendEmailRequest request) {
        String templateName = templateResolver.resolveTemplateName(request.getTemplateType());
        
        return Email.builder()
                .toAddresses(new ArrayList<>(request.getToList()))
                .fromAddress(request.hasFrom() ? request.getFrom() : "noreply@ecclesiaflow.com")
                .subject(request.getSubject())
                .templateName(templateName)
                .variables(new HashMap<>(request.getVariablesMap()))
                .priority(convertPriority(request.getPriority()))
                .status(EmailStatus.QUEUED)
                .queuedAt(LocalDateTime.now())
                .retryCount(0)
                .build();
    }

    private EmailPriority convertPriority(Priority priority) {
        return switch (priority) {
            case PRIORITY_HIGH -> EmailPriority.HIGH;
            case PRIORITY_LOW -> EmailPriority.LOW;
            case PRIORITY_NORMAL, PRIORITY_UNSPECIFIED, UNRECOGNIZED -> EmailPriority.NORMAL;
        };
    }

    private Status convertStatus(EmailStatus status) {
        return switch (status) {
            case QUEUED -> Status.STATUS_QUEUED;
            case SENT -> Status.STATUS_SENT;
            case DELIVERED -> Status.STATUS_DELIVERED;
            case FAILED -> Status.STATUS_FAILED;
            case BOUNCED -> Status.STATUS_BOUNCED;
        };
    }

    private long toEpochMillis(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.toInstant(ZoneOffset.UTC).toEpochMilli() : 0;
    }
}
