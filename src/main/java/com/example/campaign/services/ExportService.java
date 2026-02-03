package com.example.campaign.services;

import com.example.campaign.entity.ExportJob;
import com.example.campaign.entity.Message;
import com.example.campaign.enums.ExportStatus;
import com.example.campaign.repo.ExportJobRepo;
import com.example.campaign.repo.MessageRepo;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
public class ExportService {

    private static final int BATCH_SIZE = 10_000;
    private static final Path EXPORT_DIR = Paths.get("exports");

    private final ExportJobRepo exportJobRepository;
    private final MessageRepo messageRepo;

    public ExportService(ExportJobRepo exportJobRepository, MessageRepo messageRepo) {
        this.exportJobRepository = exportJobRepository;
        this.messageRepo = messageRepo;
    }

    public void runExport(UUID jobId) {
        ExportJob job = exportJobRepository.findById(jobId).orElseThrow();
        job.setStatus(ExportStatus.RUNNING);
        exportJobRepository.save(job);

        try {
            Files.createDirectories(EXPORT_DIR);
        } catch (IOException e) {
            failJob(job, "Failed to create export directory");
            return;
        }

        Path csvPath = EXPORT_DIR.resolve(jobId + ".csv");

        try (Writer writer = Files.newBufferedWriter(csvPath);
             CSVPrinter csv = new CSVPrinter(writer, CSVFormat.DEFAULT
                     .withHeader("campaign_id", "customer_id", "phone",
                             "status", "sent_at", "delivered_at", "read_at"))) {

            Long lastId = 0L;

            while (true) {
                List<Message> batch =
                        messageRepo.findBatch(
                                job.getCampaignId(),
                                lastId,
                                PageRequest.of(0, BATCH_SIZE)
                        );


                if (batch.isEmpty()) break;

                for (Message msg : batch) {
                    csv.printRecord(
                            msg.getCampaign().getId(),
                            msg.getCustomerId(),
                            msg.getPhoneNumber(),
                            msg.getStatus(),
                            msg.getSentAt(),
                            msg.getDeliveredAt(),
                            msg.getReadAt()
                    );
                    lastId = msg.getId();
                }
            }

            job.setStatus(ExportStatus.COMPLETED);
            job.setFilePath(csvPath.toString());
            exportJobRepository.save(job);

        } catch (Exception e) {
            failJob(job, e.getMessage());
        }
    }

    private void failJob(ExportJob job, String reason) {
        job.setStatus(ExportStatus.FAILED);
        job.setErrorMessage(reason);
        exportJobRepository.save(job);
    }
}
