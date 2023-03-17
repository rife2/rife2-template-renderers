import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

plugins {
    `java-library`
    `maven-publish`
    pmd
    signing
    id("com.github.ben-manes.versions") version "0.46.0"
}

val rifeVersion by rootProject.extra { "1.5.0-SNAPSHOT" }

group = "com.uwyn.rife2"
version = "0.9.0-SNAPSHOT"

repositories {
    mavenLocal()
    mavenCentral()
    maven { url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots") } // only needed for SNAPSHOT
}

dependencies {
    implementation("com.uwyn.rife2:rife2:${rifeVersion}") {
        this.isChanging = true
    }
    runtimeOnly("com.uwyn.rife2:rife2:${rifeVersion}:agent") {
        this.isChanging = true
    }

    testImplementation(platform("org.junit:junit-bom:5.9.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.assertj:assertj-core:3.24.2")
}

configurations {
    all {
        resolutionStrategy.cacheChangingModulesFor(0, TimeUnit.SECONDS)
    }
}

java {
    withJavadocJar()
    withSourcesJar()
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

pmd {
    isIgnoreFailures = true
    ruleSetFiles = files("${projectDir}/config/pmd.xml")
    isConsoleOutput = true
}


tasks {
    withType<JavaCompile> {
        options.encoding = "UTF-8"
    }

    test {
        val apiKey = project.properties["testsBadgeApiKey"]
        useJUnitPlatform()
        testLogging {
            exceptionFormat = TestExceptionFormat.FULL
            events = setOf(TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.FAILED)
        }
        addTestListener(object : TestListener {
            override fun beforeTest(p0: TestDescriptor?) = Unit
            override fun beforeSuite(p0: TestDescriptor?) = Unit
            override fun afterTest(desc: TestDescriptor, result: TestResult) = Unit
            override fun afterSuite(desc: TestDescriptor, result: TestResult) {
                if (desc.parent != null) {
                    val output = result.run {
                        "Results: $resultType (" +
                                "$testCount tests, " +
                                "$successfulTestCount successes, " +
                                "$failedTestCount failures, " +
                                "$skippedTestCount skipped" +
                                ")"
                    }
                    val testResultLine = "|  $output  |"
                    val repeatLength = testResultLine.length
                    val separationLine = "-".repeat(repeatLength)
                    println()
                    println(separationLine)
                    println(testResultLine)
                    println(separationLine)
                }

                if (desc.parent == null) {
                    val passed = result.successfulTestCount
                    val failed = result.failedTestCount
                    val skipped = result.skippedTestCount

                    if (apiKey != null) {
                        val response: HttpResponse<String> = HttpClient.newHttpClient()
                            .send(
                                HttpRequest.newBuilder()
                                    .uri(
                                        URI(
                                            "https://rife2.com/tests-badge/update/com.uwyn.rife2/rife2-renderers?" +
                                                    "apiKey=$apiKey&" +
                                                    "passed=$passed&" +
                                                    "failed=$failed&" +
                                                    "skipped=$skipped"
                                        )
                                    )
                                    .POST(HttpRequest.BodyPublishers.noBody())
                                    .build(), HttpResponse.BodyHandlers.ofString()
                            )
                        println("RESPONSE: " + response.statusCode())
                        println(response.body())
                    }
                }
            }
        })
    }

    javadoc {
        title = "<a href=\"https://rife2.com\">RIFE2</a> Template Renderers"
        options {
            this as StandardJavadocDocletOptions
            keyWords(true)
            splitIndex(true)
            links("https://rife2.github.io/rife2/")
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = "rife2-renderers"
            from(components["java"])
            pom {
                name.set("RIFE2 Template Renderers")
                description.set("Template Renderers for the RIFE2 framework")
                url.set("https://github.com/rife2/rife2-template-renderers")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("ethauvin")
                        name.set("Erik C. Thauvin")
                        email.set("erik@thauvin.net")
                        url.set("https://erik.thauvin.net/")
                    }
                    developer {
                        id.set("gbevin")
                        name.set("Geert Bevin")
                        email.set("gbevin@uwyn.com")
                        url.set("https://github.com/gbevin")
                    }
                }
                scm {
                    connection.set("scm:git:https://github.com/rife2/rife2-template-renderers.git")
                    developerConnection.set("scm:git:git@github.com:rife2/rife2-template-renderers.git")
                    url.set("https://github.com/rife2/rife2-template-renderers")
                }
            }
            repositories {
                maven {
                    credentials {
                        username = System.getenv("SONATYPE_USER")
                        password = System.getenv("SONATYPE_PASSWORD")
                    }
                    val releasesRepoUrl = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
                    val snapshotsRepoUrl = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
                    url = if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl
                }
            }
        }
    }
}

signing {
    val signingKey: String? by project // ORG_GRADLE_PROJECT_signingKey
    val signingPassword: String? by project // ORG_GRADLE_PROJECT_signingPassword
    useInMemoryPgpKeys(signingKey, signingPassword)
    sign(publishing.publications["mavenJava"])
}