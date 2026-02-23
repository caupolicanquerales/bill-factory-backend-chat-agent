package com.capo.bill_factory_agent_chat.controller;

import java.util.Objects;

import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.capo.bill_factory_agent_chat.request.GenerationSyntheticDataRequest;
import com.capo.bill_factory_agent_chat.response.DataMessage;
import com.capo.bill_factory_agent_chat.service.ExecutingOrchestratorService;

import reactor.core.publisher.Flux;

@RestController
@RequestMapping("agent-chat")
@CrossOrigin(origins = "${app.frontend.url}")
public class GenerationAgentDataController {
	
	private final ExecutingOrchestratorService executingOrchestrator;

    public GenerationAgentDataController(ExecutingOrchestratorService executingOrchestrator) {
    	this.executingOrchestrator= executingOrchestrator;
    }
	
	
	@PostMapping(path = "/chat-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public Flux<ServerSentEvent<DataMessage>> chatClient(@RequestBody GenerationSyntheticDataRequest request) {
		return Flux.just(request.getPrompt())
	    		.filter(message -> Objects.nonNull(message)) 
	            .filter(message ->  !message.trim().isEmpty())
	            .doOnNext(req->{System.out.println(req);} )
	            .flatMap(executingOrchestrator::executing);
	}
	
}
