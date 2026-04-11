package com.example.bossbot.stampanswer.service;

import com.example.bossbot.stampanswer.entity.StampAnswer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("KeywordMatchUtils Tests")
class KeywordMatchUtilsTest {

    private static final int MIN_INPUT_LENGTH = 3;
    private static final double KEYWORD_THRESHOLD = 0.5;

    private StampAnswer buildStampAnswer(Long id, String question, String keywords, int priority) {
        return StampAnswer.builder()
                .id(id)
                .question(question)
                .keywords(keywords)
                .priority(priority)
                .isActive(true)
                .build();
    }

    @Test
    @DisplayName("Should return empty for single letter input")
    void testSingleLetter_returnsEmpty() {
        StampAnswer sa = buildStampAnswer(1L, "What is Spring Boot?", "spring, boot", 5);
        Optional<StampAnswer> result = KeywordMatchUtils.findBestKeywordMatch("k", List.of(sa), MIN_INPUT_LENGTH, KEYWORD_THRESHOLD);
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should return empty for two character input")
    void testTwoCharInput_returnsEmpty() {
        StampAnswer sa = buildStampAnswer(1L, "What is Spring Boot?", "spring, boot", 5);
        Optional<StampAnswer> result = KeywordMatchUtils.findBestKeywordMatch("hi", List.of(sa), MIN_INPUT_LENGTH, KEYWORD_THRESHOLD);
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should return empty for null input")
    void testNullInput_returnsEmpty() {
        StampAnswer sa = buildStampAnswer(1L, "What is Spring Boot?", "spring, boot", 5);
        Optional<StampAnswer> result = KeywordMatchUtils.findBestKeywordMatch(null, List.of(sa), MIN_INPUT_LENGTH, KEYWORD_THRESHOLD);
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should return empty for blank input")
    void testBlankInput_returnsEmpty() {
        StampAnswer sa = buildStampAnswer(1L, "What is Spring Boot?", "spring, boot", 5);
        Optional<StampAnswer> result = KeywordMatchUtils.findBestKeywordMatch("   ", List.of(sa), MIN_INPUT_LENGTH, KEYWORD_THRESHOLD);
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should return match when input has good word overlap")
    void testGoodOverlap_returnsMatch() {
        StampAnswer sa = buildStampAnswer(1L, "What is Spring Boot?", "spring, boot, framework", 5);
        Optional<StampAnswer> result = KeywordMatchUtils.findBestKeywordMatch("What is Spring Boot", List.of(sa), MIN_INPUT_LENGTH, KEYWORD_THRESHOLD);
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Should return match when input matches keywords")
    void testMatchesKeywords() {
        StampAnswer sa = buildStampAnswer(1L, "opening hours question", "hours, schedule, time", 5);
        Optional<StampAnswer> result = KeywordMatchUtils.findBestKeywordMatch("schedule time", List.of(sa), MIN_INPUT_LENGTH, KEYWORD_THRESHOLD);
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Should return empty when word overlap is too low")
    void testLowOverlap_returnsEmpty() {
        StampAnswer sa = buildStampAnswer(1L, "What is Spring Boot?", "spring, boot", 5);
        Optional<StampAnswer> result = KeywordMatchUtils.findBestKeywordMatch("hello world today friend", List.of(sa), MIN_INPUT_LENGTH, KEYWORD_THRESHOLD);
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should prefer higher priority when scores are equal")
    void testPriorityBreaksTies() {
        StampAnswer low = buildStampAnswer(1L, "What is Spring?", "spring", 1);
        StampAnswer high = buildStampAnswer(2L, "What is Spring?", "spring", 10);
        Optional<StampAnswer> result = KeywordMatchUtils.findBestKeywordMatch("What is Spring", List.of(low, high), MIN_INPUT_LENGTH, KEYWORD_THRESHOLD);
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("Should return empty for empty stamp answer list")
    void testEmptyList_returnsEmpty() {
        Optional<StampAnswer> result = KeywordMatchUtils.findBestKeywordMatch("What is Spring", List.of(), MIN_INPUT_LENGTH, KEYWORD_THRESHOLD);
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should handle null keywords gracefully")
    void testNullKeywords() {
        StampAnswer sa = buildStampAnswer(1L, "What is Spring Boot?", null, 5);
        Optional<StampAnswer> result = KeywordMatchUtils.findBestKeywordMatch("What is Spring Boot", List.of(sa), MIN_INPUT_LENGTH, KEYWORD_THRESHOLD);
        assertThat(result).isPresent();
    }
}
