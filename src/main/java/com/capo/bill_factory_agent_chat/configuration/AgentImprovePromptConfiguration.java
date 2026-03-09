package com.capo.bill_factory_agent_chat.configuration;

import java.util.function.Function;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

import com.capo.bill_factory_agent_chat.record.ImproveRequest;
import com.capo.bill_factory_agent_chat.record.PromptRequest;

@Configuration
public class AgentImprovePromptConfiguration {

	@Bean
    public ChatClient chatClientImprover(ChatClient.Builder builder) {
        return builder
        	.clone()
        	.defaultTools()
        	.defaultToolNames()
        	.defaultAdvisors()
        	.defaultSystem(systemPrompt)
            .build();
    }
	
	@Bean("improvePromptTool") 
    @Description("Use this tool to improve the prompt to generate an image to be used in the model gpt-image-1")
    public Function<ImproveRequest, String> improvePromptTool(@Qualifier("chatClientImprover") ChatClient chatClient) {
		return (request) ->{ 
			try {
				String combinedUserPrompt = String.format(
		                "CURRENT PROMPT: %s\n\nUSER FEEDBACK: %s\n\nTask: Refactor the current prompt based on the feedback.",
		                request.currentPrompt(), 
		                request.userInstructions()
		            );
				ChatResponse response= chatClient.prompt()
	                .user(combinedUserPrompt)
	                .call()
	                .chatResponse();
	            return getTokenMessage(response);
			}catch(Exception e) {
				return "Fail to improve prompt: "+e.getMessage();
			}
		};
    }
	
	private String getTokenMessage(ChatResponse chatResponse) {
	    if (chatResponse == null || chatResponse.getResult() == null) {
	        return "The sub-agent was unable to process the request.";
	    }
	    String content = chatResponse.getResult().getOutput().getText();
	    return (content != null && !content.isEmpty()) ? content : "Error: Sub-agent could not generate a response.";
	}
	
	private String systemPrompt = """
		    You are a Senior UI/UX Prompt Engineer. Your goal is to refactor and enhance user-provided UI descriptions into high-fidelity prompts for gpt-image-1.

			INSTRUCTIONS:
			1. ENHANCE VISUAL FIDELITY: Add descriptors for digital surfaces (e.g., "frosted glass," "brushed metal," "matte plastic," "vibrant OLED colors").
			2. DEFINE LIGHTING: Use UI-specific lighting terms like "soft global illumination," "subtle drop shadows," "rim lighting on buttons," or "backlit elements."
			3. FIX COMPOSITION: Ensure the layout is described as a "clean front-facing screenshot" or "isometric 3D web view" to avoid warped perspectives.
			4. SPECIFY STYLE: Apply modern design trends (e.g., "Apple-inspired minimalism," "Material Design 3," "SaaS dashboard aesthetic," "high-end Fintech UI").
			5. TECHNICAL POLISH: Append quality tokens like "8k, clean typography, pixel-perfect, sharp edges, professional color grading."
			
			OUTPUT:
			Output ONLY the final improved prompt. No conversational filler.
		    """;
}
