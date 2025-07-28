import com.github.spotbugs.snom.Effort
import com.github.spotbugs.snom.Confidence

plugins {
	java
	id("org.springframework.boot") version "3.5.3"
	id("io.spring.dependency-management") version "1.1.7"
	id("org.owasp.dependencycheck") version "8.4.3"
	id("com.github.spotbugs") version "6.0.4"
	id("pmd")
}

group = "fintech2"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-data-redis")
	implementation("org.springframework.boot:spring-boot-starter-cache")
	implementation("org.flywaydb:flyway-core")
	
	// JWT
	implementation("io.jsonwebtoken:jjwt-api:0.12.3")
	implementation("io.jsonwebtoken:jjwt-impl:0.12.3")
	implementation("io.jsonwebtoken:jjwt-jackson:0.12.3")
	
	// Database
	runtimeOnly("com.h2database:h2")
	runtimeOnly("org.postgresql:postgresql")
	
	// Swagger
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.2.0")
	
	// BCrypt
	implementation("org.springframework.security:spring-security-crypto")
	
	// Monitoring
	implementation("io.micrometer:micrometer-registry-prometheus")
	
	// Code quality tools
	spotbugsPlugins("com.h3xstream.findsecbugs:findsecbugs-plugin:1.12.0")
	
	compileOnly("org.projectlombok:lombok")
	developmentOnly("org.springframework.boot:spring-boot-devtools")
	annotationProcessor("org.projectlombok:lombok")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.security:spring-security-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
	useJUnitPlatform()
}

// SpotBugs 설정
spotbugs {
	toolVersion.set("4.8.3")
	effort.set(Effort.MAX)
	reportLevel.set(Confidence.MEDIUM)
	excludeFilter.set(file("$projectDir/config/spotbugs/exclude.xml"))
}

// PMD 설정
pmd {
	toolVersion = "6.55.0"
	ruleSetFiles = files("$projectDir/config/pmd/ruleset.xml")
	ruleSets = listOf()
}

// OWASP Dependency Check 설정
dependencyCheck {
    formats = listOf("HTML", "JSON")
    outputDirectory = "$buildDir/reports/dependency-check"
    scanSet = listOf(file("$projectDir"))
    suppressionFile = "$projectDir/config/dependency-check/suppressions.xml"
}

// Docker 이미지 빌드 태스크
tasks.register("dockerBuild") {
	dependsOn("build")
	doLast {
		exec {
			commandLine("docker", "build", "-t", "easypay:latest", ".")
		}
	}
}

// Docker Compose 실행 태스크
tasks.register("dockerComposeUp") {
	doLast {
		exec {
			commandLine("docker-compose", "up", "-d")
		}
	}
}

tasks.register("dockerComposeDown") {
	doLast {
		exec {
			commandLine("docker-compose", "down")
		}
	}
}
