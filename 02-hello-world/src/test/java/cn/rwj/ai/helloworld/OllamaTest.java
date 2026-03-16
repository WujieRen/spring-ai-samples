package cn.rwj.ai.helloworld;

import com.alibaba.fastjson2.JSON;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Flux;

import java.util.concurrent.CountDownLatch;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class OllamaTest {

    @Resource
    private OllamaChatModel ollamaChatModel;

    @Test
    public void test_model() {
        ChatOptions defaultOptions = ollamaChatModel.getDefaultOptions();
    }

    @Test
    public void test_call() {
        ChatResponse response = ollamaChatModel.call(new Prompt(
                "1+1",
                OllamaOptions.builder().model("deepseek-r1:1.5b").build()));

        log.info("测试结果(call):{}", JSON.toJSONString(response));
    }


    @Test
    public void test_stream() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);

        Flux<ChatResponse> stream = ollamaChatModel.stream(new Prompt(
                "1+1",
                OllamaOptions.builder().model("deepseek-r1:1.5b").build()));

        stream.subscribe(
                chatResponse -> {
                    AssistantMessage output = chatResponse.getResult().getOutput();
                    log.info("测试结果(stream): {}", JSON.toJSONString(output));
                },
                Throwable::printStackTrace,
                () -> {
                    countDownLatch.countDown();
                    log.info("测试结果(stream): done!");
                }
        );

        countDownLatch.await();
    }



}
