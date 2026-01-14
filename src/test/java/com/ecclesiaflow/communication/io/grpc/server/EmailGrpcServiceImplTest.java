package com.ecclesiaflow.communication.io.grpc.server;

import com.ecclesiaflow.communication.business.domain.email.Email;
import com.ecclesiaflow.communication.business.domain.email.EmailStatus;
import com.ecclesiaflow.communication.business.services.EmailService;
import com.ecclesiaflow.communication.business.services.TemplateResolver;
import com.ecclesiaflow.grpc.email.*;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailGrpcServiceImplTest {

    private static class CapturingObserver<T> implements StreamObserver<T> {
        T value;
        Throwable error;
        boolean completed;
        @Override public void onNext(T value) { this.value = value; }
        @Override public void onError(Throwable t) { this.error = t; }
        @Override public void onCompleted() { this.completed = true; }
    }

    @Test
    void sendEmail_shouldQueueAndReturnResponse() {
        EmailService emailService = mock(EmailService.class);
        TemplateResolver templateResolver = mock(TemplateResolver.class);
        when(templateResolver.resolveTemplateName(EmailTemplateType.EMAIL_TEMPLATE_WELCOME)).thenReturn("emails/welcome");

        UUID id = UUID.randomUUID();
        Email queued = Email.builder()
                .id(id)
                .toAddresses(List.of("a@b.com"))
                .subject("S")
                .status(EmailStatus.QUEUED)
                .queuedAt(LocalDateTime.now())
                .build();
        when(emailService.queueEmail(any(Email.class))).thenReturn(queued);

        EmailGrpcServiceImpl svc = new EmailGrpcServiceImpl(emailService, templateResolver);

        SendEmailRequest req = SendEmailRequest.newBuilder()
                .addTo("a@b.com")
                .setFrom("noreply@ecclesiaflow.com")
                .setSubject("S")
                .setTemplateType(EmailTemplateType.EMAIL_TEMPLATE_WELCOME)
                .setPriority(Priority.PRIORITY_HIGH)
                .putVariables("k","v")
                .build();

        CapturingObserver<SendEmailResponse> obs = new CapturingObserver<>();
        svc.sendEmail(req, obs);

        assertThat(obs.error).isNull();
        assertThat(obs.value).isNotNull();
        assertThat(obs.value.getEmailId()).isEqualTo(id.toString());
        assertThat(obs.value.getStatus()).isEqualTo(Status.STATUS_QUEUED);
        assertThat(obs.value.getQueuedAt()).isGreaterThan(0L);
        assertThat(obs.completed).isTrue();
    }

    @Test
    void sendEmail_shouldReturnInternalErrorOnException() {
        EmailService emailService = mock(EmailService.class);
        TemplateResolver templateResolver = mock(TemplateResolver.class);
        when(templateResolver.resolveTemplateName(any())).thenReturn("emails/x");
        when(emailService.queueEmail(any(Email.class))).thenThrow(new RuntimeException("boom"));

        EmailGrpcServiceImpl svc = new EmailGrpcServiceImpl(emailService, templateResolver);
        SendEmailRequest req = SendEmailRequest.newBuilder().addTo("a@b.com").setTemplateType(EmailTemplateType.EMAIL_TEMPLATE_WELCOME).build();

        CapturingObserver<SendEmailResponse> obs = new CapturingObserver<>();
        svc.sendEmail(req, obs);

        assertThat(obs.error).isNotNull();
    }

    @Test
    void getEmailStatus_shouldReturnStatusWhenFound() {
        EmailService emailService = mock(EmailService.class);
        TemplateResolver templateResolver = mock(TemplateResolver.class);
        UUID id = UUID.randomUUID();
        Email email = Email.builder()
                .id(id)
                .toAddresses(List.of("a@b.com"))
                .subject("S")
                .status(EmailStatus.SENT)
                .queuedAt(LocalDateTime.now())
                .sentAt(LocalDateTime.now())
                .build();
        when(emailService.findById(id)).thenReturn(Optional.of(email));

        EmailGrpcServiceImpl svc = new EmailGrpcServiceImpl(emailService, templateResolver);
        EmailStatusRequest req = EmailStatusRequest.newBuilder().setEmailId(id.toString()).build();
        CapturingObserver<EmailStatusResponse> obs = new CapturingObserver<>();

        svc.getEmailStatus(req, obs);

        assertThat(obs.error).isNull();
        assertThat(obs.value).isNotNull();
        assertThat(obs.value.getEmailId()).isEqualTo(id.toString());
        assertThat(obs.value.getStatus()).isEqualTo(Status.STATUS_SENT);
        assertThat(obs.completed).isTrue();
    }

    @Test
    void getEmailStatus_shouldReturnNotFoundWhenMissing() {
        EmailService emailService = mock(EmailService.class);
        TemplateResolver templateResolver = mock(TemplateResolver.class);
        UUID id = UUID.randomUUID();
        when(emailService.findById(id)).thenReturn(Optional.empty());

        EmailGrpcServiceImpl svc = new EmailGrpcServiceImpl(emailService, templateResolver);
        EmailStatusRequest req = EmailStatusRequest.newBuilder().setEmailId(id.toString()).build();
        CapturingObserver<EmailStatusResponse> obs = new CapturingObserver<>();

        svc.getEmailStatus(req, obs);

        assertThat(obs.error).isNotNull();
    }

    @Test
    void sendBulkEmails_shouldAggregateResults() {
        EmailService emailService = mock(EmailService.class);
        TemplateResolver templateResolver = mock(TemplateResolver.class);
        when(templateResolver.resolveTemplateName(any())).thenReturn("emails/x");

        UUID id1 = UUID.randomUUID();
        Email queued1 = Email.builder().id(id1).toAddresses(List.of("a@b.com")).status(EmailStatus.QUEUED).queuedAt(LocalDateTime.now()).build();
        when(emailService.queueEmail(any(Email.class)))
                .thenReturn(queued1)
                .thenThrow(new RuntimeException("fail"));

        EmailGrpcServiceImpl svc = new EmailGrpcServiceImpl(emailService, templateResolver);

        SendEmailRequest r1 = SendEmailRequest.newBuilder().addTo("a@b.com").setTemplateType(EmailTemplateType.EMAIL_TEMPLATE_WELCOME).build();
        SendEmailRequest r2 = SendEmailRequest.newBuilder().addTo("c@d.com").setTemplateType(EmailTemplateType.EMAIL_TEMPLATE_WELCOME).build();
        SendBulkEmailsRequest req = SendBulkEmailsRequest.newBuilder().addEmails(r1).addEmails(r2).build();

        CapturingObserver<SendBulkEmailsResponse> obs = new CapturingObserver<>();
        svc.sendBulkEmails(req, obs);

        assertThat(obs.error).isNull();
        assertThat(obs.value).isNotNull();
        assertThat(obs.value.getTotal()).isEqualTo(2);
        assertThat(obs.value.getQueued()).isEqualTo(1);
        assertThat(obs.value.getFailed()).isEqualTo(1);
        assertThat(obs.completed).isTrue();
    }
}
