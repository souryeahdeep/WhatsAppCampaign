package com.example.campaign.controllers;

import com.example.campaign.entity.ExportJob;
import com.example.campaign.enums.ExportStatus;
import com.example.campaign.repo.ExportJobRepo;
import jakarta.annotation.Resource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"})
@RestController
@RequestMapping("/exports")
public class ExportController {

    private final ExportJobRepo exportJobRepository;

    public ExportController(ExportJobRepo exportJobRepository) {
        this.exportJobRepository = exportJobRepository;
    }

    @GetMapping("/{jobId}/status")
    public ResponseEntity<Map<String, String>> getStatus(@PathVariable String jobId) {
        UUID id;
        try {
            id = UUID.fromString(jobId);
        } catch (IllegalArgumentException e) {
            Map<String, String> body = new HashMap<>();
            body.put("status", "INVALID_ID");
            return ResponseEntity.badRequest().body(body);
        }

        return getMapResponseEntity(id, exportJobRepository);
    }

    static ResponseEntity<Map<String, String>> getMapResponseEntity(UUID id, ExportJobRepo exportJobRepository) {
        Optional<ExportJob> jobOpt = exportJobRepository.findById(id);
        if (jobOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        ExportJob job = jobOpt.get();
        Map<String, String> body = new HashMap<>();
        body.put("status", job.getStatus() == null ? "UNKNOWN" : job.getStatus().name());
        return ResponseEntity.ok(body);
    }

}
