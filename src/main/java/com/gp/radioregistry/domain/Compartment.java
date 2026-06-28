package com.gp.radioregistry.domain;

import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.gp.radioregistry.constant.AppConstants.Validation.*;

@Entity
@Table(name = "compartment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Compartment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Size(max = NAME_MAX_LENGTH)
    private String name;

    @Column(nullable = false)
    @Size(max = CODE_MAX_LENGTH)
    private String code;

    @Size(max = DESCRIPTION_MAX_LENGTH)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_compartment_id")
    private Compartment parentCompartment;

    @Builder.Default
    @OneToMany(mappedBy = "parentCompartment", fetch = FetchType.LAZY)
    private List<Compartment> childCompartments = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "compartment", fetch = FetchType.LAZY)
    private List<Device> devices = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

}

