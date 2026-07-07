package com.gp.radioregistry.devicetype.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.envers.Audited;

import java.time.OffsetDateTime;

import static com.gp.radioregistry.constant.ValidationConstants.DESCRIPTION_MAX_LENGTH;
import static com.gp.radioregistry.constant.ValidationConstants.NAME_MAX_LENGTH;

@Entity
@Table(name = "device_type")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Audited
public class DeviceType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    @Size(max = NAME_MAX_LENGTH)
    private String name;

    @Column(columnDefinition = "TEXT")
    @Size(max = DESCRIPTION_MAX_LENGTH)
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
