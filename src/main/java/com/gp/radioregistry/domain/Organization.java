package com.gp.radioregistry.domain;

import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "organization")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Organization {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Size(max = 50)
    private String name;

    @Column(nullable = false)
    @Size(max = 20)
    private String code;

    @Size(max = 200)
    private String description;

    @Builder.Default
    @OneToMany(mappedBy = "organization", fetch = FetchType.LAZY)
    private List<Compartment> compartments = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "organization", fetch = FetchType.LAZY)
    private List<Device> devices = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}

