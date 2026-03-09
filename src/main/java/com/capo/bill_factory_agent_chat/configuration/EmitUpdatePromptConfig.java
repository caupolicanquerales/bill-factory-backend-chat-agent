package com.capo.bill_factory_agent_chat.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.capo.bill_factory_agent_chat.emitEvents.EmitUpdatePromptEvent;

import kafkaEvents.PromptGeneratedEvent;
import reactor.core.publisher.Sinks;


@Configuration
public class EmitUpdatePromptConfig {
	
	@Bean
    public EmitUpdatePromptEvent emitUpdatePointEventListener(){
        var sink = Sinks.many().multicast().<PromptGeneratedEvent>onBackpressureBuffer();
        var flux = sink.asFlux();
        return new EmitUpdatePromptEvent(sink, flux);
    }
}
