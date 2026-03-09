package com.capo.bill_factory_agent_chat.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;

import com.capo.bill_factory_agent_chat.kafka.EmitingEvent;
import com.capo.bill_factory_agent_chat.request.GenerationSyntheticDataRequest;
import com.capo.bill_factory_agent_chat.response.DataMessage;
import com.capo.bill_factory_agent_chat.utils.ConverterUtil;
import com.capo.bill_factory_agent_chat.utils.MapperUtil;

import kafkaEvents.PromptGeneratedEvent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class ExecutingOrchestratorService {
	
	private final ChatClient chatClient;
	private final EmitingEvent<PromptGeneratedEvent> emitEvent;
	
	@Value(value="${event.name-chat}")
	private String eventName;
	
	public ExecutingOrchestratorService(@Qualifier("chatClientOrchestrator") ChatClient chatClient,
			EmitingEvent<PromptGeneratedEvent> emitEvent) {
		this.chatClient = chatClient;
		this.emitEvent= emitEvent;
	}
	
	public Flux<ServerSentEvent<DataMessage>> executing(GenerationSyntheticDataRequest request) {

	    return Mono.fromCallable(() ->
	                this.chatClient.prompt()
	                    .advisors(a -> a.param("chat_memory_conversation_id", request.getConversationId())
	                                    .param("chat_memory_retrieve_size", 10))
	                    .system(s -> s.text(systemPrompt))
	                    .user(request.getPrompt())
	                    .call()
	                    .content()
	    )
	    .subscribeOn(Schedulers.boundedElastic())
	    .flatMapMany(fullContent -> {
	        try {
	            PromptGeneratedEvent finalPrompt = MapperUtil.setPromptGeneratedEvent(fullContent);
	            emitEvent.emit(finalPrompt);
	        } catch (Exception kafkaError) {
	            System.err.println("Kafka event emission failed (SSE response will still be returned): "
	                    + kafkaError.getMessage());
	        }

	        DataMessage dataMessage = ConverterUtil.setDataMessage(fullContent);
	        return Flux.just(ConverterUtil.setServerSentEvent(dataMessage, eventName));
	    })
	    .onErrorMap(e -> new RuntimeException("AI processing failed", e));
	}
	
	private String systemPrompt= """
			You are a High-Precision Prompt Engineering Orchestrator. Your primary function is to classify input type and route it to the correct tool.
			1. DETECT CODE (Highest Priority): 
			   - If the input contains [INPUT_FORMAT: RAW_CODE], you MUST use 'convertorHTMLToPromptTool'.
			   - If the input begins with or contains CSS selectors (e.g., .document-page, background:, display: flex) or HTML structure (e.g., <style>, <div>), you are forbidden from using improvePromptTool. The existence of curly braces {} and angle brackets <> combined with CSS properties is an absolute trigger for convertorHTMLToPromptTool.
			   - DO NOT interpret the meaning of the text inside the code; only identify the presence of code syntax.
			
			2. DETECT PLAIN TEXT:
			   - If the input is natural language, a description, or an instruction to "fix" or "better" a concept, use 'improvePromptTool'.
			
			- NO PRE-PROCESSING: Do not attempt to "improve" the prompt before converting it. 
			- NON-AMBIGUITY RULE: Valid CSS/HTML code is never considered ambiguous. Even if the code contains text descriptions, treat the entire block as a technical specification for convertorHTMLToPromptTool.
			
			3. REFACTORING MODE:
			   - If the user provides feedback (e.g., "change the color", "make it more cinematic") and you see a previous prompt in the conversation history, you MUST use 'improvePromptTool'.
			   - When calling 'improvePromptTool', pass the LAST GENERATED PROMPT as the 'currentPrompt' and the user's new request as 'userInstructions'.
			
			Analyze the input and call the tool immediately.
			""";
}
