package com.example.campaign.repo;

import com.example.campaign.entity.ExportJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ExportJobRepo extends JpaRepository<ExportJob, UUID> {
}
