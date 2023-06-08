import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

plugins {
    id("org.springframework.boot") version "3.1.0" apply false
    id("io.spring.dependency-management") version "1.1.0" apply false
    kotlin("jvm") version "1.8.21"
    kotlin("plugin.spring") version "1.8.21" apply false
    kotlin("kapt") version "1.8.21"
}

fun moveFiles(sourceDir: File, targetDir: File) {
    if (sourceDir.isDirectory) {
        sourceDir.listFiles()?.forEach { file ->
            val targetFile = File(targetDir, file.name)
            if (targetFile.exists()) {
                println("Skipping file ${file.name} - target file already exists")
            } else {
                Files.copy(file.toPath(), targetFile.toPath())
                println("Copied file ${file.name} to ${targetFile.absolutePath}")
                modifyFirstLine(targetFile)
            }
        }
    } else {
        println("Source directory ${sourceDir.absolutePath} does not exist or is not a directory")
    }
}

fun modifyFirstLine(file: File) {
    val tempFile = File.createTempFile("temp", null)
    val packageLine = "package com.slavko.annotationsgenerator.generated.sources;"
    tempFile.bufferedWriter().use { writer ->
        writer.write(packageLine)
        writer.newLine()
        file.bufferedReader().forEachLine { line ->
            if (!line.startsWith("package")) {
                writer.write(line)
                writer.newLine()
            }
        }
    }
    Files.move(tempFile.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING)
}

// Функция для дополнения содержимого файла
fun appendFileContents(sourceFile: File, destinationFile: File) {
    val sourceContent = sourceFile.readText()
    val destinationContent = destinationFile.readText()
    val mergedContent = "$destinationContent\n$sourceContent"
    destinationFile.writeText(mergedContent)
}







allprojects {
    group = "com.slavko"
    version = "0.0.1-SNAPSHOT"

    repositories {
        mavenCentral()
    }



    tasks.withType<KotlinCompile> {
        doLast {
            val sourceFolderPath = File("database-module/build/generated/source/kapt/main/com/slavko/annotationsgenerator/generated")
            val destinationFolderPath = File("database-module/src/main/kotlin/com/slavko/annotationsgenerator/generated/sources")

            moveFiles(sourceFolderPath, destinationFolderPath)
        }
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
        compileOnly("org.projectlombok:lombok:1.18.24")

    }

    kapt {
        arguments {
            arg("kapt.kotlin.generated", "$buildDir/generated/kapt/main")
        }
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
