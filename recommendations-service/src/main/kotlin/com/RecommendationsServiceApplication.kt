package com

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class RecommendationsServiceApplication

fun main(args: Array<String>) = runApplication<RecommendationsServiceApplication>(*args)