package com.gp.radioregistry.device.domain;

import com.gp.radioregistry.department.domain.Department;
import com.gp.radioregistry.device.enums.DeviceStatus;
import com.gp.radioregistry.devicetype.domain.DeviceType;
import com.gp.radioregistry.exception.InvalidEntityStateException;
import com.gp.radioregistry.organization.domain.Organization;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.*;
import org.hibernate.envers.Audited;

import java.time.LocalDate;
import java.time.OffsetDateTime;

import static com.gp.radioregistry.constant.ValidationConstants.*;

@Entity
@Table(name = "device",
        check = {
            @CheckConstraint(
                name = "chk_device_parent_structure",
                constraint = "(organization_id IS NOT NULL AND department_id IS NULL) "
                    + "OR (organization_id IS NULL AND department_id IS NOT NULL)"),
            @CheckConstraint(
                name = "chk_device_decommission_date",
                constraint = "(device_status IN ('DECOMMISSIONED', 'PENDING_DECOMMISSIONING') "
                    + "AND decommission_date IS NOT NULL) "
                    + "OR (device_status NOT IN ('DECOMMISSIONED', 'PENDING_DECOMMISSIONING') "
                    + "AND decommission_date IS NULL)")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Audited
public class Device {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    @Size(max = NAME_MAX_LENGTH)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "device_type_id", nullable = false)
    @OnDelete(action = OnDeleteAction.RESTRICT)
    private DeviceType deviceType;

    @Column(name = "serial_number", nullable = false, columnDefinition = "TEXT")
    @Size(max = SERIAL_NUMBER_MAX_LENGTH)
    private String serialNumber;

    @Column(columnDefinition = "TEXT")
    @Size(max = DESCRIPTION_MAX_LENGTH)
    private String description;

    @Column(name = "installation_date", nullable = false)
    private LocalDate installationDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "device_status", nullable = false)
    private DeviceStatus deviceStatus = DeviceStatus.ACTIVE;

    @Column(name = "decommission_date")
    private LocalDate decommissionDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    @OnDelete(action = OnDeleteAction.RESTRICT)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    @OnDelete(action = OnDeleteAction.RESTRICT)
    private Department department;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    @PreUpdate
    private void validateDevice() {
        validateParentStructure();
        validateDecommissionStatusAndDate();
    }

    private void validateParentStructure() {
        boolean hasOrganization = organization != null;
        boolean hasDepartment = department != null;
        if (hasOrganization == hasDepartment) {
            throw new InvalidEntityStateException(
                "A device must reference exactly one owner: either an organization "
                    + "or a department, but not both and not neither.");
        }
    }

    private void validateDecommissionStatusAndDate() {
        boolean isDecommissionState = deviceStatus == DeviceStatus.DECOMMISSIONED
            || deviceStatus == DeviceStatus.PENDING_DECOMMISSIONING;
        boolean hasDecommissionDate = decommissionDate != null;

        if (isDecommissionState != hasDecommissionDate) {
            throw new InvalidEntityStateException(
                "A device must have a decommission date if its status is DECOMMISSIONED or PENDING_DECOMMISSIONING.");
        }
    }
}
