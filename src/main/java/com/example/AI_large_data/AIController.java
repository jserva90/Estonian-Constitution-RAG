package com.example.AI_large_data;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class AIController {
    private final VectorStore vectorStore;
    private final ChatClient chatClient;

    public AIController(VectorStore vectorStore, ChatClient chatClient) {
        this.vectorStore = vectorStore;
        this.chatClient = chatClient;
    }

    @GetMapping("/ask-ai")
    public String askAQuestion(@RequestParam(value = "question",
            defaultValue = "List all the Articles in the Estonian Constitution") String question) {
        String prompt = """
            Your task is to answer the questions about Estonian Constitution. Use the information from the DOCUMENTS
            section to provide accurate answers. If unsure or if the answer isn't found in the DOCUMENTS section,
            simply state that you don't know the answer.
                        
            QUESTION:
            {input}
                        
            DOCUMENTS:
            {documents}
                        
            """;

        PromptTemplate promptTemplate = new PromptTemplate(prompt);
        Map<String, Object> promptParameters = new HashMap<>();
        promptParameters.put("question", question);
        promptParameters.put("documents", getSimilarData(question));

        return chatClient.prompt(promptTemplate.create(promptParameters)).call().content();
    }

    private String getSimilarData(String question) {
        List<Document> documents = vectorStore.similaritySearch(SearchRequest.query(question).withTopK(3));
        return documents.stream().map(Document::getContent).collect(Collectors.joining());
    }
}

