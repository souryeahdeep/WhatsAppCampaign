package com.example.campaign.entity;


import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Entity
@Table(name = "campaigns")
public class Campaign {
    @Id
    private Long id;
    private Long businessId;
    private String name;
    private Instant createdAt;
    @OneToMany(mappedBy = "campaign", fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<Message> messages;
}


