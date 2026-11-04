package com.data.seed

import org.springframework.core.io.ClassPathResource
import org.springframework.util.FileCopyUtils
import java.io.IOException
import java.nio.charset.StandardCharsets

@Throws(IOException::class)
fun readClassPathResourceAsString(path: String): String {
    val cpr = ClassPathResource(path)
    val bytes: ByteArray = FileCopyUtils.copyToByteArray(cpr.inputStream)
    return String(bytes, StandardCharsets.UTF_8)
}