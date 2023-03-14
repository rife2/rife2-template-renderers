import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    `java-library`
    id("com.github.ben-manes.versions") version "0.46.0"
}

version = "0.9.0-SNAPSHOT"

repositories {
    mavenLocal()
    mavenCentral()
    maven { url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots") } // only needed for SNAPSHOT
}

dependencies {
	implementation("com.uwyn.rife2:rife2:1.5.0-SNAPSHOT") {
		this.isChanging = true
	}
	runtimeOnly("com.uwyn.rife2:rife2:1.5.0-SNAPSHOT:agent") {
		this.isChanging = true
	}

	testImplementation(platform("org.junit:junit-bom:5.9.2"))
	testImplementation("org.junit.jupiter:junit-jupiter")
}

configurations {
	all {
        resolutionStrategy.cacheChangingModulesFor(0, TimeUnit.SECONDS);
    }
}

java {
    withJavadocJar()
    withSourcesJar()
    toolchain {
    	languageVersion.set(JavaLanguageVersion.of(17))
    }
}

tasks {
    test {
        useJUnitPlatform()
        testLogging {
            exceptionFormat = TestExceptionFormat.FULL
            events = setOf(TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.FAILED)
        }
    }
}
