package com.ecclesiaflow.communication.application.config;

import com.google.protobuf.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.MessageConversionException;
import org.springframework.amqp.support.converter.MessageConverter;

/**
 * RabbitMQ message converter for Protobuf messages.
 *
 * @author EcclesiaFlow Team
 * @since 1.0.0
 */
public class ProtobufMessageConverter implements MessageConverter {

    private static final String CONTENT_TYPE = "application/x-protobuf";
    private static final String TYPE_ID_HEADER = "__TypeId__";

    private final Class<? extends Message> defaultType;

    public ProtobufMessageConverter(Class<? extends Message> defaultType) {
        this.defaultType = defaultType;
    }

    @Override
    public org.springframework.amqp.core.Message toMessage(Object object, MessageProperties messageProperties) 
            throws MessageConversionException {
        if (!(object instanceof Message protoMessage)) {
            throw new MessageConversionException("Object must be a Protobuf Message");
        }
        
        messageProperties.setContentType(CONTENT_TYPE);
        messageProperties.setHeader(TYPE_ID_HEADER, object.getClass().getName());
        
        return new org.springframework.amqp.core.Message(protoMessage.toByteArray(), messageProperties);
    }

    @Override
    public Object fromMessage(org.springframework.amqp.core.Message message) throws MessageConversionException {
        try {
            String typeId = message.getMessageProperties().getHeader(TYPE_ID_HEADER);
            Class<? extends Message> targetClass = typeId != null 
                    ? getClassFromTypeId(typeId) 
                    : defaultType;
            
            java.lang.reflect.Method parseFrom = targetClass.getMethod("parseFrom", byte[].class);
            return parseFrom.invoke(null, message.getBody());
        } catch (Exception e) {
            throw new MessageConversionException("Failed to convert Protobuf message", e);
        }
    }

    private static final String TRUSTED_PACKAGE_PREFIX = "com.ecclesiaflow.";

    @SuppressWarnings("unchecked")
    private Class<? extends Message> getClassFromTypeId(String typeId) {
        if (!typeId.startsWith(TRUSTED_PACKAGE_PREFIX)) {
            throw new MessageConversionException(
                    "Untrusted message type: " + typeId + ". Only com.ecclesiaflow.* types are allowed.");
        }
        try {
            return (Class<? extends Message>) Class.forName(typeId);
        } catch (ClassNotFoundException e) {
            return defaultType;
        }
    }
}
