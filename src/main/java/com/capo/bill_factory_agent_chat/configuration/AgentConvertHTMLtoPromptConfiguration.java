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
public class AgentConvertHTMLtoPromptConfiguration {

	@Bean
    public ChatClient chatClientConverter(ChatClient.Builder builder) {
        return builder
    		.clone()
    		.defaultTools()
    		.defaultToolNames()
        	.defaultAdvisors()
        	.defaultSystem(systemPrompt)
            .build();
    }
	
	@Bean("convertorHTMLToPromptTool") 
    @Description("Converts a string containing HTML and CSS code into a descriptive image generation prompt.")
    public Function<PromptRequest, String> convertorHTMLToPromptTool(@Qualifier("chatClientConverter") ChatClient chatClient) {
		return (request) ->{ 
			try {
				ChatResponse response= chatClient.prompt()
	                .user(request.prompt())
	                .call()
	                .chatResponse();
	            return getTokenMessage(response);
			}catch(Exception e) {
				return "Fail to convert HTML and CSS to prompt:" +e.getMessage();
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
	        You are an expert Frontend Architect and Prompt Engineer.
	        Your task is to analyze HTML and CSS code and convert it into a detailed, descriptive prompt.
	        
	        REQUIREMENTS:
	        1. Identify the layout structure (Flexbox, Grid, etc.).
	        2. Describe the visual style (colors, typography, spacing, shadows).
	        3. Explain the functional intent of the UI components.
	        4. Output ONLY the generated prompt that would recreate this UI. Do not add conversational filler.
	        """;
}
