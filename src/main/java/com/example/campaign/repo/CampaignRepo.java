package com.example.campaign.repo;

import com.example.campaign.entity.Campaign;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CampaignRepo extends JpaRepository<Campaign, Long> {

    @Query("SELECT c FROM Campaign c LEFT JOIN FETCH c.messages WHERE c.id = :id")
    Optional<Campaign> findByIdWithMessages(@Param("id") Long id);

}
