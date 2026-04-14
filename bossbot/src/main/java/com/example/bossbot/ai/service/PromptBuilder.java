package com.example.bossbot.ai.service;

import com.example.bossbot.chat.config.ChatConfig;
import com.example.bossbot.message.dto.MessageResponse;
import com.example.bossbot.message.entity.MessageRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PromptBuilder {

    private final ChatConfig chatConfig;

    public record ChatMessage(String role, String content) {}

    public List<ChatMessage> buildMessages(List<MessageResponse> history, String userMessage) {
        String systemPrompt = chatConfig.getSystemPrompt();
        int maxHistory = chatConfig.getMaxHistoryMessages();

        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new ChatMessage("system", systemPrompt));

        List<MessageResponse> recentHistory = history.size() > maxHistory
                ? history.subList(history.size() - maxHistory, history.size())
                : history;

        for (MessageResponse msg : recentHistory) {
            String role = msg.getRole() == MessageRole.USER ? "user" : "assistant";
            messages.add(new ChatMessage(role, msg.getContent()));
        }

        // Ensure the conversation ends with the user's latest message.
        // History already includes it (saved before calling AI), but if maxHistory
        // truncation cut it off, or history was empty, re-add it as a safety net.
        boolean endsWithUserMessage = messages.size() > 1
                && "user".equals(messages.getLast().role());
        if (!endsWithUserMessage) {
            messages.add(new ChatMessage("user", userMessage));
        }

        return messages;
    }
}
