package com.slavko.annotationsgenerator

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class DatabaseModuleApplication

fun main(args: Array<String>) {
    runApplication<DatabaseModuleApplication>(*args)
}
