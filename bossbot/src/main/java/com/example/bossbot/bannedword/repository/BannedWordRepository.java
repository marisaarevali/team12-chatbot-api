package com.example.bossbot.bannedword.repository;

import com.example.bossbot.bannedword.entity.BannedWord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BannedWordRepository extends JpaRepository<BannedWord, Long> {

    List<BannedWord> findByIsActiveTrue();

    List<BannedWord> findByCategoryAndIsActiveTrue(String category);

    Optional<BannedWord> findByWordIgnoreCaseAndIsActiveTrue(String word);

    boolean existsByWordIgnoreCase(String word);
}