package com.gp.radioregistry.organization.domain;

import com.gp.radioregistry.department.domain.Department;
import com.gp.radioregistry.device.domain.Device;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.gp.radioregistry.constant.ValidationConstants.*;

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
    @Size(max = NAME_MAX_LENGTH)
    private String name;

    @Column(nullable = false)
    @Size(max = CODE_MAX_LENGTH)
    private String code;

    @Size(max = DESCRIPTION_MAX_LENGTH)
    private String description;

    @Builder.Default
    @OneToMany(mappedBy = "organization", fetch = FetchType.LAZY)
    private List<Department> departments = new ArrayList<>();

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

