package com

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class CompatibilityServiceApplication

fun main(args: Array<String>) = runApplication<CompatibilityServiceApplication>(*args)