package cn.rwj.ai;

import cn.rwj.ai.utils.Utils.TokenTextSplitterWithContext;
import com.alibaba.fastjson.JSON;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.model.Media;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class OllamaTest {

    @Resource
    private OllamaChatModel ollamaChatModel;

    @Value("classpath:data/dog.png")
    private org.springframework.core.io.Resource imageResource;

    @Resource(name = "ollamaSimpleVectorStore")
    private SimpleVectorStore simpleVectorStore;

    @Resource(name = "ollamaPgVectorStore")
    private PgVectorStore pgVectorStore;

    @Test
    public void test_call_images() {
        // 构建请求信息
        UserMessage userMessage = new UserMessage("请描述这张图片的主要内容，并说明图中物品的可能用途。",
                new Media(MimeType.valueOf(MimeTypeUtils.IMAGE_PNG_VALUE),
                        imageResource));

        ChatResponse response = ollamaChatModel.call(
                new Prompt(
                        userMessage,
                        OllamaOptions.builder()
                                .model("deepseek-r1:1.5b")
                                .build()
                )
        );

        log.info("测试结果(images):{}", JSON.toJSONString(response));
    }

    @Test
    public void upload() {
        TikaDocumentReader reader = new TikaDocumentReader("./data/file.txt");

        List<Document> documents = reader.get();
        TokenTextSplitterWithContext splitter = new TokenTextSplitterWithContext(100, 20);
        List<Document> documentSplitterList = splitter.split(documents);

        documents.forEach(doc -> doc.getMetadata().put("knowledge", "ai知识库"));
        documentSplitterList.forEach(doc -> doc.getMetadata().put("knowledge", "ai知识库"));

        pgVectorStore.accept(documentSplitterList);

        log.info("上传完成");
    }


    @Test
    public void chat() {
        String message = "王大牛有什么代表作";

        String SYSTEM_PROMPT = """
                Use the information from the DOCUMENTS section to provide accurate answers but act as if you knew this information innately.
                If unsure, simply state that you don't know.
                Another thing you need to note is that your reply must be in Chinese!
                DOCUMENTS:
                    {documents}
                """;

        SearchRequest request = SearchRequest.builder()
                .query(message)
                .topK(5)
                .filterExpression("knowledge == 'ai知识库'")
                .build();

        List<Document> documents = pgVectorStore.similaritySearch(request);

        String documentsCollectors = documents.stream().map(Document::getText).collect(Collectors.joining());

        Message ragMessage = new SystemPromptTemplate(SYSTEM_PROMPT).createMessage(Map.of("documents", documentsCollectors));

        ArrayList<Message> messages = new ArrayList<>();
        messages.add(new UserMessage(message));
        messages.add(ragMessage);

        ChatResponse chatResponse = ollamaChatModel.call(
                new Prompt(
                        messages,
                        OllamaOptions.builder()
                                .model("deepseek-r1:1.5b")
                                .build()
                )
        );

        log.info("测试结果:{}", JSON.toJSONString(chatResponse));
    }

//[main            ] INFO  HikariDataSource       - HikariPool-1 - Starting...
//[main            ] INFO  HikariPool             - HikariPool-1 - Added connection org.postgresql.jdbc.PgConnection@7e2654f
//[main            ] INFO  HikariDataSource       - HikariPool-1 - Start completed.
//[main            ] INFO  OllamaTest             - 测试结果:{"metadata":{"empty":false,"id":"","model":"deepseek-r1:1.5b","rateLimit":{"requestsLimit":0,"requestsRemaining":0,"requestsReset":{"nano":0,"negative":false,"positive":false,"seconds":0,"units":["SECONDS","NANOS"],"zero":true},"tokensLimit":0,"tokensRemaining":0,"tokensReset":{"$ref":"$.metadata.rateLimit.requestsReset"}},"usage":{"promptTokens":667,"completionTokens":353,"totalTokens":1020,"generationTokens":353}},"result":{"metadata":{"contentFilters":[],"empty":true,"finishReason":"stop"},"output":{"media":[],"messageType":"ASSISTANT","metadata":{"messageType":"ASSISTANT"},"text":"<think>\n好的，我现在要处理这个用户的查询。用户问的是“王大牛有什么代表作”，并提供了一段关于王大牛的信息。\n\n首先，我需要分析用户的需求。用户明确询问了王大牛的代表作，这可能是在做学术研究或者了解王大牛的艺术成就。根据提供的 DOCUMENTS，里面提到了他的多个人物项目和奖项获奖情况，并且提到他在电影事业上的贡献，特别是《文字 头文字 d》。\n\n接下来，我需要确定用户是否真的知道这一点。从提供的信息来看，王大牛确实有一个电影专辑《最伟大作品》，并且他创建了威耳音乐公司的专属唱片公司。这些都属于代表作的内容。\n\n然后，考虑用户的潜在需求。他们可能是在学习音乐、电影或者文化相关的知识，并且对王大牛的兴趣非常浓。因此，用户不仅需要知道代表作是什么，还需要了解背后的背景和意义，比如他在华语乐坛的影响。\n\n最后，我需要确保在回答时准确反映这些信息，并且用中文表达出来，同时保持事实的准确性。这样用户就能获得全面且有用的信息来满足他们的查询需求。\n</think>\n\n王大牛在音乐方面的代表作包括：\n\n1. **《杰伦》（《Jenny》）**：这是他的早期热门专辑，发布于2000年。该专辑获得了华语乐坛的多项重要奖项。\n\n2. **《最伟大作品》**：这是一个国际著名的唱片系列，不仅在国内取得了巨大成功，也在全球多个国家和地区引起了关注。\n\n此外，在电影方面，王大牛还参与了电影《文字 头文字 d》，这被视为他在音乐与影视结合方面的代表作之一。"{"$ref":"$.metadata.rateLimit.requestsReset.usage.result.metadata.contentFilters.output.media"}}},"results":[{"$ref":"$.metadata.rateLimit.requestsReset.usage.result"}]}
//[SpringApplicationShutdownHook] INFO  HikariDataSource       - HikariPool-1 - Shutdown initiated...
//[SpringApplicationShutdownHook] INFO  HikariDataSource       - HikariPool-1 - Shutdown completed.



}
