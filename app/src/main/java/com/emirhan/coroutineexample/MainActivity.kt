package com.emirhan.coroutineexample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import kotlin.system.measureTimeMillis

class MainActivity : AppCompatActivity() {

    companion object {
        const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        GlobalScope.launch {
            //different thread
            delay(3000)
            Log.e(TAG, "Coroutine thread: ${Thread.currentThread().name}")
        }

        Log.e(TAG, "Thread: ${Thread.currentThread().name}")


        GlobalScope.launch {
            val networkAnswer = doNetworkCall()
            val networkAnswer2 = doNetworkCall2()

            Log.e(TAG, networkAnswer)
            Log.e(TAG, networkAnswer2)

            // one suspend function influences other suspend functions which they call in same thread
            // so we had 6 seconds delay
        }

        GlobalScope.launch(Dispatchers.IO) {
            // Dispatchers.Main for UI changes
            // Dispatchers.IO for data operations
            // Dispatchers.Default for big calculation operations
            // Dispatchers.Unconfined

            Log.d(TAG, "Starting coroutine in thread ${Thread.currentThread().name}")

            val answer = doNetworkCall()
            withContext(Dispatchers.Main) {
                // we want to update textView and switch to main thread for UI operation

                Log.d(TAG, "Continue coroutine in thread ${Thread.currentThread().name}")
                textView.text = answer
            }
        }


        Log.d(TAG, "Before runBlocking")
        runBlocking {
            // Delaying main thread

            // These launch coroutines work asynchronous
            launch(Dispatchers.IO) {
                delay(3000)
                Log.d(TAG, "Finished IO Coroutine 1")
            }
            launch(Dispatchers.IO) {
                // we can call suspend function asynchronous
                delay(3000)
                Log.d(TAG, "Finished IO Coroutine 2")
            }

            Log.d(TAG, "Start of runBlocking")
            delay(5000)
            Log.d(TAG, "End of runBlocking")
        }
        Log.d(TAG, "After runBlocking")


        val job = GlobalScope.launch(Dispatchers.Default) {
            repeat(5){
                Log.d(TAG, "Coroutine is still working...")
                delay(1000)
            }
        }

        runBlocking {
            //*job.join()
            // waiting job's finishing
            Log.d(TAG, "Main Thread is continuing")//*

            //*delay(2000)
            job.cancel()
            Log.d(TAG, "Main Thread is continuing")//*
        }


        // Asynchronous jobs
        GlobalScope.launch(Dispatchers.IO) {
            val time = measureTimeMillis {  // get time for job
                val answer1 = async { doNetworkCall() }
                val answer2 = async { doNetworkCall2() }

                // await method for waiting async job's finishing
                Log.d(TAG, "Answer1 is: ${answer1.await()}")
                Log.d(TAG, "Answer2 is: ${answer2.await()}")
            }
            Log.d(TAG, "Requests took $time ms")
        }
    }

    // suspend functions have to use in coroutine
    private suspend fun doNetworkCall(): String {
        delay(3000)
        return "This is the answer 1"
    }

    private suspend fun doNetworkCall2(): String {
        delay(3000)
        return "This is the answer 2"
    }
}