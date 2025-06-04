package io.github.kurenairyu

import kotlinx.coroutines.*
import kotlin.random.Random

/**
 * @author Kurenai
 * @since 2023/2/27 9:03
 */

class CoroutineTest {
}

suspend fun main() {

    val asyncJob = CoroutineScope(Dispatchers.Default).async {
        delay(1000)
        "test"
    }

    println(asyncJob.await())

    val promise = CompletableDeferred<String>()
    CoroutineScope(Dispatchers.Default).launch {
        delay(1000)
        if (Random(10).nextBoolean()) {
            promise.complete("test2")
        } else {
            promise.completeExceptionally(IllegalStateException("TestEx"))
        }
    }

    println(runCatching {
        promise.await()
    }.getOrDefault("111111"))

}