package com.capo.bill_factory_agent_chat.kafka;

public interface EmitingEvent<T> {
	void emit(T event);
}
