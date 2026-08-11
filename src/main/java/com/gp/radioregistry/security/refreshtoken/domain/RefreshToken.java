package com.gp.radioregistry.security.refreshtoken.domain;

import com.gp.radioregistry.user.domain.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.OffsetDateTime;

@Entity
@Table(name = "refresh_token")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "token_hash", nullable = false, unique = true, columnDefinition = "TEXT")
	private String tokenHash;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	@OnDelete(action = OnDeleteAction.CASCADE)
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "replaced_by_token_id")
	@OnDelete(action = OnDeleteAction.SET_NULL)
	private RefreshToken replacedByToken;

	@CreationTimestamp
	@Column(name = "issued_at", nullable = false, updatable = false)
	private OffsetDateTime issuedAt;

	@Column(name = "expires_at", nullable = false)
	private OffsetDateTime expiresAt;

	@Builder.Default
	@Column(name = "revoked", nullable = false)
	private boolean revoked = false;

	@Column(name = "revoked_at")
	private OffsetDateTime revokedAt;
}
