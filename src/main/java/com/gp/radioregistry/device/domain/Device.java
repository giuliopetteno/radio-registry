package com.gp.radioregistry.device.domain;

import com.gp.radioregistry.department.domain.Department;
import com.gp.radioregistry.devicetype.domain.DeviceType;
import com.gp.radioregistry.organization.domain.Organization;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.envers.Audited;

import java.time.LocalDate;
import java.time.OffsetDateTime;

import static com.gp.radioregistry.constant.ValidationConstants.*;

@Entity
@Table(name = "device")
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

    @Column(nullable = false)
    @Size(max = NAME_MAX_LENGTH)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "device_type_id", nullable = false)
    private DeviceType deviceType;

    @Column(name = "serial_number", nullable = false)
    @Size(max = SERIAL_NUMBER_MAX_LENGTH)
    private String serialNumber;

    @Size(max = DESCRIPTION_MAX_LENGTH)
    private String description;

    @Column(name = "installation_date", nullable = false)
    private LocalDate installationDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}

