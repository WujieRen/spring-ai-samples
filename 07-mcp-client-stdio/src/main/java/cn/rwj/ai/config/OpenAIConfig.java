package cn.rwj.ai.config;

import io.micrometer.observation.ObservationRegistry;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.DefaultChatClientBuilder;
import org.springframework.ai.chat.client.observation.ChatClientObservationConvention;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAIConfig {

    @Bean
    public OpenAiApi openAiApi(@Value("${spring.ai.openai.base-url}") String baseUrl, @Value("${spring.ai.openai.api-key}") String apikey) {
        return OpenAiApi.builder()
                .baseUrl(baseUrl)
                .apiKey(apikey)
                .build();
    }

    @Bean
    public ChatClient chatClient(OpenAiChatModel openAiChatModel,
                                 ToolCallbackProvider tools) {
        DefaultChatClientBuilder defaultChatClientBuilder = new DefaultChatClientBuilder(
                openAiChatModel, ObservationRegistry.NOOP, (ChatClientObservationConvention) null
        );
        return defaultChatClientBuilder
                .defaultTools(tools)
                .defaultOptions(OpenAiChatOptions.builder()
                        .model("gpt-4o")
                        .build())
                .build();
    }

}
