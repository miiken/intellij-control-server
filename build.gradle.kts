plugins {
    id("java")
    kotlin("jvm") version "1.9.23"
    id("org.jetbrains.intellij") version "1.17.2"
    id("idea")
}

kotlin {
    jvmToolchain(17)
}

group = "io.miiken.intellijcontrolserver"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.google.code.gson:gson:2.10.1")
    
    // JAX-RS API (Java standard for REST)
    implementation("jakarta.ws.rs:jakarta.ws.rs-api:3.1.0")
    
    // Swagger/OpenAPI annotations (industry standard)
    implementation("io.swagger.core.v3:swagger-annotations:2.2.20")
    implementation("io.swagger.core.v3:swagger-models:2.2.20")
    implementation("io.swagger.core.v3:swagger-core:2.2.20")
    
    // Test dependencies
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.1")
    testImplementation("org.junit.vintage:junit-vintage-engine:5.10.1")
    testImplementation("io.mockk:mockk:1.13.8")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("junit:junit:4.13.2")
    
    // Kotlinx coroutines - use version compatible with IntelliJ 2024.3
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.0")
}

intellij {
    version.set("2024.3")
    type.set("IC") // IntelliJ IDEA Community Edition
    plugins.set(listOf("tasks", "java"))
}

idea {
    module {
        isDownloadJavadoc = true
        isDownloadSources = true
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks {
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }
    
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }
    
    test {
        useJUnitPlatform {
            includeEngines("junit-jupiter", "junit-vintage")
        }
        
        systemProperty("idea.home.path", intellij.sandboxDir.get())
        systemProperty("idea.force.use.core.classloader", "true")
        systemProperty("idea.use.core.classloader.for.plugin.path", "true")
        systemProperty("idea.plugins.path", intellij.sandboxDir.get())
        systemProperty("idea.config.path", "${intellij.sandboxDir.get()}/config-test")
        systemProperty("idea.system.path", "${intellij.sandboxDir.get()}/system-test")
        systemProperty("java.awt.headless", "true")
        systemProperty("idea.classpath.index.enabled", "false")
        systemProperty("idea.is.internal", "true")
        systemProperty("junit.jupiter.extensions.autodetection.enabled", "true")
        
        jvmArgs(
            "-Xmx2048m",
            "-XX:+UseParallelGC",
            "-XX:SoftRefLRUPolicyMSPerMB=50",
            "-XX:ReservedCodeCacheSize=512m",
            "-XX:+HeapDumpOnOutOfMemoryError",
            "-ea",
            "-Djava.awt.headless=true",
            "-Djdk.module.illegalAccess.silent=true",
            "--add-opens=java.base/java.lang=ALL-UNNAMED",
            "--add-opens=java.base/java.util=ALL-UNNAMED",
            "--add-opens=java.desktop/java.awt=ALL-UNNAMED",
            "--add-opens=java.desktop/sun.awt=ALL-UNNAMED",
            "--add-opens=java.desktop/sun.font=ALL-UNNAMED",
            "--add-opens=jdk.httpserver/com.sun.net.httpserver=ALL-UNNAMED",
            "--add-opens=java.base/java.io=ALL-UNNAMED",
            "--add-opens=java.base/java.nio=ALL-UNNAMED"
        )
        
        testLogging {
            events("passed", "skipped", "failed")
            exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
            showStandardStreams = true
            showExceptions = true
        }
        
        isScanForTestClasses = true
        include("**/*Test.class", "**/*PlatformTest.class")
    }

    patchPluginXml {
        sinceBuild.set("243")
        untilBuild.set("253.*")
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }
}

