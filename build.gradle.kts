plugins {
	java
	id("org.springframework.boot") version "4.1.0"
	id("io.spring.dependency-management") version "1.1.7"
	id("io.freefair.lombok") version "9.5.0"
}

group = "com.gp"
version = "0.0.1-SNAPSHOT"

val mockitoAgent by configurations.creating

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(25)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-webmvc")
	implementation("org.springframework.boot:spring-boot-starter-security:4.1.0")
	implementation("org.springframework.boot:spring-boot-starter-actuator:4.1.0")
	implementation("org.springframework.boot:spring-boot-starter-aop:4.0.0-M2")
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.0.3")
	implementation("jakarta.validation:jakarta.validation-api:3.0.2")
	testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
	testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
	testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.1")
	runtimeOnly("org.postgresql:postgresql")
	developmentOnly("org.springframework.boot:spring-boot-devtools:4.1.0")
	testCompileOnly("org.projectlombok:lombok")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	mockitoAgent("org.mockito:mockito-core:5.23.0") { isTransitive = false }
}

tasks.test {
	useJUnitPlatform()
	jvmArgs("-javaagent:${mockitoAgent.asPath}", "-Xshare:off")
}

springBoot {
	buildInfo()
}