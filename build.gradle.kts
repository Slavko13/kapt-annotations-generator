import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "3.1.0" apply false
    id("io.spring.dependency-management") version "1.1.0" apply false
    kotlin("jvm") version "1.8.21"
    kotlin("plugin.spring") version "1.8.21" apply false
    kotlin("kapt") version "1.8.21"
}


allprojects {
    group = "com.slavko"
    version = "0.0.1-SNAPSHOT"

    repositories {
        mavenCentral()
    }

}



subprojects {

    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.kotlin.plugin.spring")
    apply(plugin = "org.springframework.boot")
    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "org.jetbrains.kotlin.kapt")

    dependencies {
        kapt("com.google.auto.service:auto-service:1.0-rc2")
        implementation("com.google.auto.service:auto-service:1.0-rc2")
    }

    repositories {
        mavenCentral()
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = "17"
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }



}
