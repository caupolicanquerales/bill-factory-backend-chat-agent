package com.capo.bill_factory_agent_chat.emitEvents;

import org.springframework.beans.factory.annotation.Autowired;

import com.capo.bill_factory_agent_chat.kafka.EmitEvent;
import com.capo.bill_factory_agent_chat.kafka.EmitingEvent;
import com.capo.bill_factory_agent_chat.kafka.EventInUse;

import kafkaEvents.PromptGeneratedEvent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

public class EmitUpdatePromptEvent implements EmitingEvent<PromptGeneratedEvent>,EventInUse<PromptGeneratedEvent>{
	
	@Autowired
	private EmitEvent emitEvent;
	
	private final Sinks.Many<PromptGeneratedEvent> sink;
	private final Flux<PromptGeneratedEvent> flux;
	
	public EmitUpdatePromptEvent(Sinks.Many<PromptGeneratedEvent> sink,Flux<PromptGeneratedEvent> flux) {
		this.sink=sink;
		this.flux=flux;
	}
	
	@Override
    public Flux<PromptGeneratedEvent> publish() {
        return this.flux;
    }
	
	@Override
	public void emit(PromptGeneratedEvent event) {
		emitEvent.emitEvent(sink, event);
	}
}
