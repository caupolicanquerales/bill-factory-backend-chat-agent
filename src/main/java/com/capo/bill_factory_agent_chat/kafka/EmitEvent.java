package com.capo.bill_factory_agent_chat.kafka;

import reactor.core.publisher.Sinks;

public interface EmitEvent {
	<T> void emitEvent(Sinks.Many<T> sink, T event);
}
