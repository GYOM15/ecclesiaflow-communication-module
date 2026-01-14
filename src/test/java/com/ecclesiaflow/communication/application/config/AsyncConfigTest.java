package com.ecclesiaflow.communication.application.config;

import org.junit.jupiter.api.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

import static org.assertj.core.api.Assertions.assertThat;

class AsyncConfigTest {

    @Test
    void emailTaskExecutor_shouldReturnConfiguredExecutor() {
        AsyncConfig config = new AsyncConfig();

        Executor executor = config.emailTaskExecutor();

        assertThat(executor).isNotNull();
        assertThat(executor).isInstanceOf(ThreadPoolTaskExecutor.class);

        ThreadPoolTaskExecutor taskExecutor = (ThreadPoolTaskExecutor) executor;
        assertThat(taskExecutor.getCorePoolSize()).isEqualTo(5);
        assertThat(taskExecutor.getMaxPoolSize()).isEqualTo(20);
        assertThat(taskExecutor.getQueueCapacity()).isEqualTo(500);
        assertThat(taskExecutor.getKeepAliveSeconds()).isEqualTo(60);
        assertThat(taskExecutor.getThreadNamePrefix()).isEqualTo("communication-async-");

        taskExecutor.shutdown();
    }
}
