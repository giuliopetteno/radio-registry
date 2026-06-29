package com.gp.radioregistry.department.domain;

import com.gp.radioregistry.device.domain.Device;
import com.gp.radioregistry.organization.domain.Organization;
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
@Table(name = "department")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Department {
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
    @JoinColumn(name = "parent_department_id")
    private Department parentDepartment;

    @Builder.Default
    @OneToMany(mappedBy = "parentDepartment", fetch = FetchType.LAZY)
    private List<Department> childDepartments = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "department", fetch = FetchType.LAZY)
    private List<Device> devices = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}

