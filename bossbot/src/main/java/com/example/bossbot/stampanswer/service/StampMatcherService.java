package com.example.bossbot.stampanswer.service;

import com.example.bossbot.stampanswer.dto.StampAnswerResponse;

import java.util.Optional;

public interface StampMatcherService {

    Optional<StampAnswerResponse> findBestMatch(String userInput);

    void refreshCache();
}
