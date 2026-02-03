package com.example.campaign.entity;

import com.example.campaign.enums.MessageStatus;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.Entity;
import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Entity
@Data
@Table(name = "messages")
public class Message {

    @Id
    private Long id;

    @ManyToOne
    @JoinColumn(name = "campaign_id")
    @JsonBackReference
    private Campaign campaign;

    private Long customerId;
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    private MessageStatus status;

    private Instant sentAt;
    private Instant deliveredAt;
    private Instant readAt;
    private String errorReason;
}
