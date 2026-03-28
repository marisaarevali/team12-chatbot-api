package com.example.bossbot.bannedword.service;

import com.example.bossbot.bannedword.dto.BannedWordResponse;
import com.example.bossbot.bannedword.dto.CreateBannedWordRequest;
import com.example.bossbot.bannedword.dto.UpdateBannedWordRequest;
import com.example.bossbot.bannedword.entity.BannedWord;
import com.example.bossbot.bannedword.repository.BannedWordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BannedWordServiceImpl implements BannedWordService {

    private static final String BANNED_WORD_NOT_FOUND = "Banned word not found with ID: ";

    private final BannedWordRepository repository;

    @Override
    @Transactional
    public BannedWordResponse create(CreateBannedWordRequest request) {
        log.info("Creating new banned word: {}", request.getWord());

        if (repository.existsByWordIgnoreCase(request.getWord())) {
            throw new IllegalArgumentException("Banned word already exists");
        }

        Long currentUserId = 1L; // placeholder until Spring Security

        BannedWord entity = BannedWord.builder()
                .word(request.getWord())
                .category(request.getCategory())
                .severity(request.getSeverity())
                .isActive(request.getIsActive() != null ? request.getIsActive() : Boolean.TRUE)
                .createdBy(currentUserId)
                .build();

        BannedWord saved = repository.save(entity);
        log.info("Created banned word with ID: {}", saved.getId());

        return BannedWordResponse.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public BannedWordResponse getById(Long id) {
        log.info("Fetching banned word with ID: {}", id);

        BannedWord entity = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(BANNED_WORD_NOT_FOUND + id));

        return BannedWordResponse.fromEntity(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BannedWordResponse> getAll() {
        log.info("Fetching all banned words");

        return repository.findAll().stream()
                .map(BannedWordResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BannedWordResponse> getAllActive() {
        log.info("Fetching all active banned words");

        return repository.findByIsActiveTrue().stream()
                .map(BannedWordResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BannedWordResponse> getByCategory(String category) {
        log.info("Fetching banned words by category: {}", category);

        return repository.findByCategoryAndIsActiveTrue(category).stream()
                .map(BannedWordResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional
    public BannedWordResponse update(Long id, UpdateBannedWordRequest request) {
        log.info("Updating banned word with ID: {}", id);

        BannedWord entity = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(BANNED_WORD_NOT_FOUND + id));

        Long currentUserId = 1L; // placeholder until Spring Security

        if (request.getWord() != null) {
            entity.setWord(request.getWord());
        }
        if (request.getCategory() != null) {
            entity.setCategory(request.getCategory());
        }
        if (request.getSeverity() != null) {
            entity.setSeverity(request.getSeverity());
        }
        if (request.getIsActive() != null) {
            entity.setIsActive(request.getIsActive());
        }

        entity.setUpdatedBy(currentUserId);

        BannedWord updated = repository.save(entity);
        log.info("Updated banned word with ID: {}", id);

        return BannedWordResponse.fromEntity(updated);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        log.info("Soft deleting banned word with ID: {}", id);

        BannedWord entity = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(BANNED_WORD_NOT_FOUND + id));

        entity.setIsActive(false);
        repository.save(entity);

        log.info("Soft deleted banned word with ID: {}", id);
    }
}