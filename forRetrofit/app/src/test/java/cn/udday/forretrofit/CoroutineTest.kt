package cn.udday.forretrofit

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import kotlinx.coroutines.flow.*
import org.junit.Test
import java.util.concurrent.Flow
import kotlin.ArithmeticException
import kotlin.system.measureTimeMillis

class CoroutineTest {

    @Test
    fun `test channel`() = runBlocking<Unit> {
        val sendChannel:SendChannel<Int> = GlobalScope.actor {
            while (true){
                println("接受 ${receive()}")
            }
        }
        val producer = GlobalScope.launch {
            for (i in 0..3){
                sendChannel.send(i)
            }
        }
        producer.join()
    }


    @Test
    fun `test broadCast`() = runBlocking<Unit> {

    }
}