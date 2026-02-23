package com.capo.bill_factory_agent_chat.kafka;

import reactor.core.publisher.Flux;

public interface EventInUse<T> {
	Flux<T> publish();
}
