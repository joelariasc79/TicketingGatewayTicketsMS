package com.ticketing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;

import jakarta.jms.ConnectionFactory;

@SpringBootApplication
public class NotificationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }

    // Configure a custom JMS listener container factory for reliability and JSON conversion
//    @Bean
//    public JmsListenerContainerFactory<?> jmsListenerContainerFactory(ConnectionFactory connectionFactory,
//                                                                      DefaultJmsListenerContainerFactoryConfigurer configurer) {
//        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
//        configurer.configure(factory, connectionFactory);
//        // You can customize factory settings here, e.g., for concurrency, transactions, error handling
//        factory.setConcurrency("3-10"); // Example: 3 to 10 concurrent consumers
//        factory.setSessionTransacted(true); // Enable JMS transactions
//        // factory.setErrorHandler(t -> System.err.println("JMS Error: " + t.getMessage())); // Custom error handler
//
//        // Set the message converter
//        factory.setMessageConverter(jacksonJmsMessageConverter());
//
//        return factory;
//    }

//    // Configure the message converter to use Jackson for JSON
//    @Bean
//    public MessageConverter jacksonJmsMessageConverter() {
//        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
//        converter.setTargetType(MessageType.TEXT); // Messages will be sent as TEXT
//        converter.setTypeIdPropertyName("_type"); // Optional: adds a type ID header for robust deserialization
//        return converter;
//    }
}