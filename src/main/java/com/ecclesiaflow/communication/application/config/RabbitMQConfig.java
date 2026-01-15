package com.ecclesiaflow.communication.application.config;

import com.ecclesiaflow.grpc.email.EmailQueueMessage;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ configuration for async communication processing with DLQ support.
 *
 * @author EcclesiaFlow Team
 * @since 1.0.0
 */
@Configuration
@EnableRabbit
@ConditionalOnProperty(
    name = "rabbitmq.enabled",
    havingValue = "true",
    matchIfMissing = true
)
public class RabbitMQConfig {
    
    @Value("${rabbitmq.exchange.email:ecclesiaflow.communication.exchange}")
    private String emailExchange;
    
    @Value("${rabbitmq.queue.email.send:ecclesiaflow.communication.send.queue}")
    private String emailQueue;
    
    @Value("${rabbitmq.queue.email.retry:ecclesiaflow.communication.retry.queue}")
    private String emailRetryQueue;
    
    @Value("${rabbitmq.queue.email.dlq:ecclesiaflow.communication.dlq}")
    private String emailDlq;
    
    @Value("${rabbitmq.routing-key.email.send:communication.send.*}")
    private String emailRoutingKey;
    
    @Value("${rabbitmq.routing-key.email.retry:communication.retry.*}")
    private String emailRetryRoutingKey;
    
    /**
     * Main exchange for emails.
     */
    @Bean
    public TopicExchange emailExchange() {
        return new TopicExchange(emailExchange, true, false);
    }
    
    /**
     * Main queue for sending emails.
     * Configured with DLQ for messages that fail after retry.
     */
    @Bean
    public Queue emailQueue() {
        return QueueBuilder.durable(emailQueue)
                .withArgument("x-dead-letter-exchange", emailExchange)
                .withArgument("x-dead-letter-routing-key", "communication.dead")
                .build();
    }
    
    /**
     * Queue for retrying failed emails.
     * Configured with 5 minutes TTL before retry.
     */
    @Bean
    public Queue emailRetryQueue() {
        return QueueBuilder.durable(emailRetryQueue)
                .withArgument("x-message-ttl", 300000) // 5 minutes
                .withArgument("x-dead-letter-exchange", emailExchange)
                .withArgument("x-dead-letter-routing-key", "communication.send.retry")
                .build();
    }
    
    /**
     * Dead Letter Queue for emails that permanently fail.
     */
    @Bean
    public Queue emailDeadLetterQueue() {
        return QueueBuilder.durable(emailDlq).build();
    }
    
    /**
     * Main queue binding.
     */
    @Bean
    public Binding emailQueueBinding(Queue emailQueue, TopicExchange emailExchange) {
        return BindingBuilder.bind(emailQueue)
                .to(emailExchange)
                .with(emailRoutingKey);
    }
    
    /**
     * Retry queue binding.
     */
    @Bean
    public Binding emailRetryQueueBinding(Queue emailRetryQueue, TopicExchange emailExchange) {
        return BindingBuilder.bind(emailRetryQueue)
                .to(emailExchange)
                .with(emailRetryRoutingKey);
    }
    
    /**
     * DLQ binding.
     */
    @Bean
    public Binding emailDlqBinding(Queue emailDeadLetterQueue, TopicExchange emailExchange) {
        return BindingBuilder.bind(emailDeadLetterQueue)
                .to(emailExchange)
                .with("communication.dead");
    }
    
    /**
     * Protobuf message converter for RabbitMQ.
     */
    @Bean
    public MessageConverter protobufMessageConverter() {
        return new ProtobufMessageConverter(EmailQueueMessage.class);
    }
    
    /**
     * RabbitTemplate configured with Protobuf converter.
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, 
                                         MessageConverter protobufMessageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(protobufMessageConverter);
        return template;
    }
}
