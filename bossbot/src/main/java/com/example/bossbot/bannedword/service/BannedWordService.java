package com.example.bossbot.bannedword.service;

import com.example.bossbot.bannedword.dto.BannedWordResponse;
import com.example.bossbot.bannedword.dto.CreateBannedWordRequest;
import com.example.bossbot.bannedword.dto.UpdateBannedWordRequest;

import java.util.List;

public interface BannedWordService {

    BannedWordResponse create(CreateBannedWordRequest request);

    BannedWordResponse getById(Long id);

    List<BannedWordResponse> getAll();

    List<BannedWordResponse> getAllActive();

    List<BannedWordResponse> getByCategory(String category);

    BannedWordResponse update(Long id, UpdateBannedWordRequest request);

    void delete(Long id);
}