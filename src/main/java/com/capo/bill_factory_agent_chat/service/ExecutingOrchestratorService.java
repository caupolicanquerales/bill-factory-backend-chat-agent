package com.capo.bill_factory_agent_chat.service;

import java.util.UUID;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;

import com.capo.bill_factory_agent_chat.kafka.EmitingEvent;
import com.capo.bill_factory_agent_chat.response.DataMessage;
import com.capo.bill_factory_agent_chat.utils.ConverterUtil;
import com.capo.bill_factory_agent_chat.utils.MapperUtil;

import kafkaEvents.PromptGeneratedEvent;
import reactor.core.publisher.Flux;

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
	
	/*
	public Flux<ServerSentEvent<DataMessage>> executing(String prompt){
		String conversationId = UUID.randomUUID().toString();
        StringBuilder fullPrompt= new StringBuilder();
		return this.chatClient.prompt()
		.system(s->s.text(systemPrompt))        
		.user(prompt)
        .advisors(a -> a.param("chat_memory_conversation_id", conversationId))
        .stream()
        .chatResponse()
        .map(response -> {
            if (response.getResult() != null && response.getResult().getOutput() != null) {
                String text = response.getResult().getOutput().getText();
                return (text != null) ? text : "";
            }
            return "";
        })
        .doOnNext(fullPrompt::append)
	    .map(ConverterUtil::setDataMessage)
	    .map(data -> ConverterUtil.setServerSentEvent(data, eventName))
	    .doOnComplete(() ->{
	    	System.out.println("AI Stream Finished. Sending completion prompt...");
	    	PromptGeneratedEvent finalPrompt = MapperUtil.setPromptGeneratedEvent(fullPrompt.toString());
	    	emitEvent.emit(finalPrompt);
	    })
	    .doOnTerminate(() -> System.out.println("HTTP Response fully closed on server"))
	    .onErrorResume(Exception.class, e -> {
            System.err.println("Error during Orchestration stream: " + e.getMessage());
            return Flux.error(new RuntimeException("AI Stream failed", e));
        });
	}*/
	
	
	public Flux<ServerSentEvent<DataMessage>> executing(String prompt) {
	    String conversationId = UUID.randomUUID().toString();
	    
	    return Flux.defer(() -> {
	        try {
	            // 1. Change .stream() to .call() for synchronous execution
	            String fullContent = this.chatClient.prompt()
	                    .system(s -> s.text(systemPrompt))
	                    .user(prompt)
	                    .advisors(a -> a.param("chat_memory_conversation_id", conversationId))
	                    .call()
	                    .content(); // This blocks until the sub-agents finish

	            // 2. Log for debugging
	            System.out.println("AI Response received: " + fullContent);

	            // 3. Emit the final event to Kafka (matching your original logic)
	            PromptGeneratedEvent finalPrompt = MapperUtil.setPromptGeneratedEvent(fullContent);
	            emitEvent.emit(finalPrompt);

	            // 4. Wrap the result in the expected Flux/SSE format for the frontend
	            DataMessage dataMessage = ConverterUtil.setDataMessage(fullContent);
	            return Flux.just(ConverterUtil.setServerSentEvent(dataMessage, eventName));

	        } catch (Exception e) {
	            System.err.println("Error during Orchestration call: " + e.getMessage());
	            return Flux.error(new RuntimeException("AI processing failed", e));
	        }
	    });
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
			
			Analyze the input and call the tool immediately.
			""";
}
