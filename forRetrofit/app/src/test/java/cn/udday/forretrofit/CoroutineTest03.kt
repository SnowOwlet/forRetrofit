package cn.udday.forretrofit

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.sync.withPermit
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger


/**
 *
 * @author ningchuanqi
 * @version V1.0
 */
class CoroutineTest03 {

    @Test
    fun `test not safe concurrent`() = runBlocking<Unit> {
        var count = 0
        List(2000) {
            GlobalScope.launch { count++ }
        }.joinAll()
        println(count)
    }

    @Test
    fun `test safe concurrent`() = runBlocking<Unit> {
        var count = AtomicInteger(0)
        List(1000) {
            GlobalScope.launch { count.incrementAndGet() }
        }.joinAll()
        println(count.get())
    }


    //kill -9 pid
    @Test
    fun `test safe concurrent tools`() = runBlocking<Unit> {
        var count = 0
        val mutex = Mutex()
        List(1000) {
            GlobalScope.launch {
                mutex.withLock {
                    count++
                }
            }
        }.joinAll()
        println(count)
    }

    @Test
    fun `test safe concurrent tools2`() = runBlocking<Unit> {
        var count = 0
        val semaphore = Semaphore(1)
        List(10000) {
            GlobalScope.launch {
                semaphore.withPermit {
                    count++
                }
            }
        }.joinAll()
        println(count)
    }

    @Test
    fun `test avoid access outer variable`() = runBlocking<Unit> {
        var count = 0
        val result = count + List(1000){
            GlobalScope.async { 1 }
        }.map { it.await() }.sum()
        println(result)
    }

}
