package com.capo.bill_factory_agent_chat.configuration;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AgentOrchestratorConfiguration {
	
	@Bean
    public ChatMemoryRepository chatMemoryRepositoryOrchestrator() {
        return new InMemoryChatMemoryRepository();
    }

	@Bean
    public ChatMemory chatMemoryOrchestrator(@Qualifier("chatMemoryRepositoryOrchestrator") ChatMemoryRepository chatMemoryRepository) {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(chatMemoryRepository)
                .maxMessages(10)
                .build();
    }

	@Bean
    public ChatClient chatClientOrchestrator(ChatClient.Builder builder, 
    		@Qualifier("chatMemoryOrchestrator") ChatMemory chatMemory) {
        MessageChatMemoryAdvisor memoryAdvisor = MessageChatMemoryAdvisor.builder(chatMemory)
                .build();
        return builder
            .defaultAdvisors(memoryAdvisor)
            .defaultToolNames("convertorHTMLToPromptTool","improvePromptTool")
            .defaultOptions(OpenAiChatOptions.builder()
                    .parallelToolCalls(false) 
                    .build())
            .build();
    }
	
}
