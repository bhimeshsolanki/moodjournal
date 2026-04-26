package com.moodjournal.demo.controller;

import com.moodjournal.demo.model.JournalEntry;
import com.moodjournal.demo.repository.JournalRepository;
import com.moodjournal.demo.service.HuggingFaceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/journal")
@CrossOrigin(origins = "${app.cors.allowed-origins}")
public class JournalController {

    private final JournalRepository repository;
    private final HuggingFaceService huggingFaceService;

    public JournalController(JournalRepository repository, HuggingFaceService huggingFaceService) {
        this.repository = repository;
        this.huggingFaceService = huggingFaceService;
    }

    // GET all entries
    @GetMapping
    public List<JournalEntry> getAll() {
        return repository.findAllByOrderByCreatedAtDesc();
    }

    // POST a new entry — calls AI then saves to DB
    @PostMapping
    public ResponseEntity<JournalEntry> create(@RequestBody Map<String, String> body) {
        String text = body.get("text");
        if (text == null || text.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        // Call Hugging Face AI
        HuggingFaceService.MoodResult moodResult = huggingFaceService.analyzeMood(text);

        // Save to database
        JournalEntry entry = new JournalEntry();
        entry.setText(text);
        entry.setMood(moodResult.mood());
        entry.setConfidence(moodResult.confidence());

        return ResponseEntity.ok(repository.save(entry));
    }

    // DELETE an entry
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}