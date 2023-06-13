package io.github.kurenairyu

import java.nio.file.Path
import java.util.*
import kotlin.io.path.inputStream

/**
 * @author Kurenai
 * @since 2023/2/24 0:43
 */

val localProperties by lazy {
    Properties().also { p ->
        Path.of("local.properties").inputStream().use(p::load)
    }
}

fun getProp(key: String) = localProperties.getProperty(key) ?: System.getProperty(key) ?: System.getenv(key)