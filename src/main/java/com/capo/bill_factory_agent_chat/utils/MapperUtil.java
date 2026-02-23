package com.capo.bill_factory_agent_chat.utils;

import kafkaEvents.PromptGeneratedEvent;

public class MapperUtil {
	
	public static PromptGeneratedEvent setPromptGeneratedEvent(String prompt) {
		PromptGeneratedEvent event= new PromptGeneratedEvent();
		event.setPrompt(prompt);
		return event;
	}
}
