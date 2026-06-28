package com.gp.radioregistry.response;

import com.gp.radioregistry.domain.User;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.stream.Collectors;

public record UserResponse(
    @Schema(description = "Unique user ID")
    Long id,

    @Schema(description = "The name of the user")
    String username,

    @Schema(description = "The email of the user")
    String email,

    @Schema(description = "The roles of the user")
    Set<RoleResponse> roles,

    @Schema(description = "Indicates if the user is enabled")
    Boolean enabled,

    @Schema(description = "Indicates if the user's account is enabled")
    Boolean accountNonLocked,

    @Schema(description = "Record creation date and time")
    OffsetDateTime createdAt,

    @Schema(description = "Record update date and time")
    OffsetDateTime updatedAt
) {
    public static UserResponse fromEntity(User user) {
        if (user == null) {
            return null;
        }

        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRoles().stream()
                    .map(RoleResponse::fromEntity)
                    .collect(Collectors.toSet()),
                user.isEnabled(),
                user.isAccountNonLocked(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}


