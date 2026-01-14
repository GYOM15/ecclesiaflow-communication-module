package com.ecclesiaflow.communication.web.controller;

import com.ecclesiaflow.communication.business.services.EmailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class EmailTestControllerTest {

    @Mock
    private EmailService emailService;

    @Test
    void processQueue_shouldReturnSuccessJson() throws Exception {
        when(emailService.processQueuedEmails(10)).thenReturn(3);
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new EmailTestController(emailService)).build();

        mvc.perform(post("/test/emails/process-queue"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"success\":true")))
                .andExpect(content().string(containsString("\"processed\":3")));

        verify(emailService).processQueuedEmails(10);
    }

    @Test
    void processQueue_shouldReturnErrorJsonOnException() throws Exception {
        when(emailService.processQueuedEmails(10)).thenThrow(new RuntimeException("boom"));
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new EmailTestController(emailService)).build();

        mvc.perform(post("/test/emails/process-queue"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"success\":false")))
                .andExpect(content().string(containsString("boom")));

        verify(emailService).processQueuedEmails(10);
    }
}
