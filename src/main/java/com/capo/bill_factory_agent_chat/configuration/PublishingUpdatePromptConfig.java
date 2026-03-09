package com.capo.bill_factory_agent_chat.configuration;

import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import com.capo.bill_factory_agent_chat.kafka.EventInUse;

import kafkaEvents.PromptGeneratedEvent;
import reactor.core.publisher.Flux;

@Configuration
public class PublishingUpdatePromptConfig {
	
	private final EventInUse<PromptGeneratedEvent> eventPublisher;
	
	private static final String DESTINATION_HEADER = "spring.cloud.stream.send.prompt";
    private static final String EVENTS_CHANNEL = "event-send-prompt";
    
	@Autowired
	public PublishingUpdatePromptConfig(EventInUse<PromptGeneratedEvent> eventPublisher) {
		this.eventPublisher=eventPublisher;
	}
	
	@Bean
    public Supplier<Flux<Message<PromptGeneratedEvent>>> publishingUpdatePromptEvent() {
        return () -> this.eventPublisher.publish()
                                        .map(this::toMessage)
                                        .onErrorContinue((err, obj) ->
                                            System.err.println("Kafka publish error (skipping event): " + err.getMessage()));
    }
	
	private Message<PromptGeneratedEvent> toMessage(PromptGeneratedEvent event) {
        return MessageBuilder.withPayload(event)
                             .setHeader(KafkaHeaders.KEY, "15")
                             .setHeader(DESTINATION_HEADER, EVENTS_CHANNEL)
                             .build();
    }
}
