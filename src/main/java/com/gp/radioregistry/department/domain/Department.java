package com.gp.radioregistry.department.domain;

import com.gp.radioregistry.device.domain.Device;
import com.gp.radioregistry.exception.InvalidEntityStateException;
import com.gp.radioregistry.organization.domain.Organization;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.*;
import org.hibernate.envers.Audited;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.gp.radioregistry.constant.ValidationConstants.*;

@Entity
@Table(name = "department",
        check = @CheckConstraint(
                name = "chk_department_parent_structure",
                constraint = "(organization_id IS NOT NULL AND parent_department_id IS NULL) "
                        + "OR (organization_id IS NULL AND parent_department_id IS NOT NULL)"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Audited
public class Department {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    @Size(max = NAME_MAX_LENGTH)
    private String name;

    @Column(nullable = false, columnDefinition = "TEXT")
    @Size(max = CODE_MAX_LENGTH)
    private String code;

    @Column(columnDefinition = "TEXT")
    @Size(max = DESCRIPTION_MAX_LENGTH)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    @OnDelete(action = OnDeleteAction.RESTRICT)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_department_id")
    @OnDelete(action = OnDeleteAction.SET_NULL)
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

    @PrePersist
    @PreUpdate
    private void validateParentStructure() {
        boolean hasOrganization = organization != null;
        boolean hasParent = parentDepartment != null;
        if (hasOrganization == hasParent) {
            throw new InvalidEntityStateException(
                "A department must reference exactly one parent: either an organization "
                    + "or a parent department, but not both and not neither.");
        }
    }
}
