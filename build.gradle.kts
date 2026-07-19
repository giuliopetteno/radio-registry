plugins {
	java
	id("org.springframework.boot") version "4.1.0"
	id("io.spring.dependency-management") version "1.1.7"
	id("io.freefair.lombok") version "9.5.0"
}

group = "com.gp"
version = "0.0.1-SNAPSHOT"

val mockitoAgent: Configuration = configurations.create("mockitoAgent")

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(25)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-webmvc")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-kafka")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-aop:4.0.0-M2")
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.0.3")
	implementation("org.hibernate.orm:hibernate-envers")
	implementation("org.apache.commons:commons-lang3")
	runtimeOnly("org.postgresql:postgresql")
	developmentOnly("org.springframework.boot:spring-boot-devtools")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
	testImplementation("org.springframework.boot:spring-boot-restclient-test")
	testImplementation("org.springframework.boot:spring-boot-starter-security-test")
	testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
	testImplementation("org.springframework.boot:spring-boot-starter-kafka-test")
	testImplementation(platform("org.testcontainers:testcontainers-bom:2.0.5"))
	testImplementation("org.springframework.boot:spring-boot-testcontainers")
	testImplementation("org.testcontainers:testcontainers-junit-jupiter")
	testImplementation("org.testcontainers:testcontainers-postgresql")
	testImplementation("org.testcontainers:testcontainers-kafka")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	mockitoAgent("org.mockito:mockito-core:5.23.0") {
		isTransitive = false
	}
}

tasks.test {
	useJUnitPlatform()
	environment("PROFILE_ACTIVE", "test")
	jvmArgs("-javaagent:${mockitoAgent.asPath}", "-Xshare:off")
	filter {
		excludeTestsMatching("*IntegrationTest")
	}
}

tasks.register<Test>("integrationTest") {
	description = "Runs full @SpringBootTest end-to-end tests"
	group = "verification"
	useJUnitPlatform()
	filter {
		includeTestsMatching("*IntegrationTest")
	}
	shouldRunAfter(tasks.test)
}

tasks.named("check") {
	dependsOn(tasks.named("integrationTest"))
}

springBoot {
	buildInfo()
}