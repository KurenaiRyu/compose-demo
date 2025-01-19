import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.1.0"
    kotlin("plugin.serialization") version "2.1.0"
    kotlin("plugin.compose") version "2.1.0"
    id("org.jetbrains.compose") version "1.7.3"
}

group = "io.github.kurenairyu"
version = "1.0"

repositories {

    maven { url = uri("https://maven.aliyun.com/repository/public/") }
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
    mavenLocal {
        content {
            includeGroupByRegex(".*\\.kurenai.*")
        }
    }
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.1")

    implementation("com.arkivanov.decompose:decompose:1.0.0-alpha-04")

    val ktor = "2.3.6"
    implementation("io.ktor:ktor-client-core:$ktor")
    implementation("io.ktor:ktor-client-okhttp:$ktor")
    implementation("io.ktor:ktor-client-content-negotiation:$ktor")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktor")

//    implementation("com.github.kurenairyu:bangumi-sdk:0.0.1")

    //cache
    implementation("com.sksamuel.aedile:aedile-core:1.2.0")

    //zip
    implementation("org.apache.commons:commons-compress:1.22")
    implementation("net.lingala.zip4j:zip4j:2.11.5")

    //logging
    val log4j = "2.20.0"
    implementation("org.apache.logging.log4j:log4j-core:$log4j")
    implementation("org.apache.logging.log4j:log4j-api:$log4j")
    implementation("org.apache.logging.log4j:log4j-slf4j2-impl:$log4j")
    implementation("com.lmax:disruptor:3.4.4")


    //test
    testImplementation(kotlin("test-junit"))
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_21
    }
}

compose.desktop {
    application {
        mainClass = "io/github/kurenairyu/comic/ComicDemoKt"
        nativeDistributions {
            modules(
                "jdk.unsupported", // sun.misc.Unsafe used by androidx datastore
                "java.management", // javax.management.MBeanRegistrationException
                "java.net.http",
            )
            targetFormats(TargetFormat.AppImage)
            packageName = "comic-demo"
            packageVersion = "1.0.0"
        }
    }
}