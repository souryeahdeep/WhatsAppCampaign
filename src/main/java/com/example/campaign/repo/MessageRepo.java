package com.example.campaign.repo;

import com.example.campaign.entity.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
@Repository
public interface MessageRepo extends JpaRepository<Message, Long> {

    @Query("""
        SELECT m FROM Message m
        WHERE m.campaign.id = :campaignId
          AND m.id > :lastId
        ORDER BY m.id ASC
    """)
    List<Message> findBatch(
            @Param("campaignId") Long campaignId,
            @Param("lastId") Long lastId,
            Pageable pageable
    );
}






