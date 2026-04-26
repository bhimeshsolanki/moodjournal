package com.moodjournal.demo.repository;

import com.moodjournal.demo.model.JournalEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JournalRepository extends JpaRepository<JournalEntry, Long> {
    List<JournalEntry> findAllByOrderByCreatedAtDesc();
}