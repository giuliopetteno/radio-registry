package com.gp.radioregistry.security.jwt.config;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.OctetSequenceKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

import static com.gp.radioregistry.security.constant.SecurityConstants.*;

@Configuration
public class JwtConfig {

	@Value("${jwt.secret}")
	private String jwtSecret;

	@Bean
	public JwtEncoder jwtEncoder() {
		var secretKey = jwtSecret.getBytes(StandardCharsets.UTF_8);

		var jwk = new OctetSequenceKey.Builder(secretKey)
			.algorithm(JWSAlgorithm.HS256)
			.build();

		return new NimbusJwtEncoder(new ImmutableJWKSet<>(new com.nimbusds.jose.jwk.JWKSet(jwk)));
	}

	@Bean
	public JwtDecoder jwtDecoder() {
		var secretKeySpec = new SecretKeySpec(jwtSecret.getBytes(StandardCharsets.UTF_8), ACCESS_TOKEN_SIGNING_ALGORITHM);
		return NimbusJwtDecoder.withSecretKey(secretKeySpec).build();
	}

	@Bean
	public JwtAuthenticationConverter jwtAuthenticationConverter() {
		var grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
		grantedAuthoritiesConverter.setAuthoritiesClaimName(JWT_ROLES_CLAIM);
		grantedAuthoritiesConverter.setAuthorityPrefix(ROLE_PREFIX);

		var jwtAuthenticationConverter = new JwtAuthenticationConverter();
		jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
		return jwtAuthenticationConverter;
	}
}
