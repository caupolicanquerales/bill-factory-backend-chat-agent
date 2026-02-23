package com.capo.bill_factory_agent_chat.configuration;

import java.util.function.Function;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

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
    public Function<PromptRequest, String> improvePromptTool(@Qualifier("chatClientImprover") ChatClient chatClient) {
		return (request) ->{ 
			try {
				ChatResponse response= chatClient.prompt()
	                .user(request.prompt())
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
		    You are a professional Prompt Engineer specializing in Text-to-Image models. 
		    Your goal is to take a basic prompt and enhance it for high-quality image generation.
		    
		    INSTRUCTIONS:
		    1. Expand the user's concept with vivid descriptions of:
		       - Style (e.g., photorealistic, oil painting, cinematic, 3D render).
		       - Lighting (e.g., golden hour, neon, soft studio light, dramatic shadows).
		       - Composition (e.g., wide shot, close-up, rule of thirds, low angle).
		       - Detail (e.g., 8k resolution, highly detailed textures, masterpiece).
		    2. Remove ambiguity and ensure the subject is clear.
		    3. Keep the language descriptive and punchy.
		    4. Output ONLY the final improved prompt text. No "Here is your prompt" or "Sure, I can help."
		    """;
}
