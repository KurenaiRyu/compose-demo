package io.github.kurenairyu

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch

/**
 * @author Kurenai
 * @since 2023/2/24 8:28
 */

class FLowTest {



}

suspend fun main() {
    val flow = flow<Int> {
        repeat(20) { i ->
            emit(i)
            println("emit $i")
            delay(1000)
        }
    }

    val job = CoroutineScope(Dispatchers.Default).launch {
        flow.collect { i ->
            println("consume $i")
            delay(1000)
        }
        println("Done.")
        delay(20)
    }

    job.join()
}
