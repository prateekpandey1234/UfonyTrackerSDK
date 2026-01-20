package com.ufony.trackersdk

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.Response
import retrofit2.Call
import retrofit2.Callback
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume

class ChildrenServiceTask (val retrofit: Retrofit) : CoroutineScope {
    val sJob = SupervisorJob()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + sJob

    fun getChildrenFromIds(forUserId: Long, listener: NullableRetrofitCallResponse<ArrayList<Child>>, vararg ids: Long) {
        val childrenService = retrofit.create(ChildrenService::class.java)
        launch(Dispatchers.Main, CoroutineStart.DEFAULT) {
            val result = childrenService.getChildrenByIds(forUserId, ids.toList()).awaitResultNullable()
            withContext(Dispatchers.Main) {
                listener.generate(forUserId, result)
            }
        }
    }

    suspend fun getChildrenFromIds(forUserId: Long, vararg ids: Long): NullableResult<ArrayList<Child>?> {
        val childrenService = retrofit.create(ChildrenService::class.java)
        val result = childrenService.getChildrenByIds(forUserId, ids.toList()).awaitResultNullable()
        return result

    }
}

suspend fun <T : Any?> Call<T>.awaitResultNullable(): NullableResult<T?> {

    return suspendCancellableCoroutine { continuation ->
        enqueue(object : Callback<T> {
            override fun onResponse(call: Call<T>, response: retrofit2.Response<T>) {
                if(response.isSuccessful)
                    continuation.resume(NullableResult.Ok(response.body(), response.raw()))
                else
                    continuation.resume(NullableResult.Error(HttpException(response), response.raw()))
            }

            override fun onFailure(call: Call<T>, t: Throwable) {
                 // Don't bother with resuming the continuation if it is already cancelled.
                if (continuation.isCancelled) return
                continuation.resume(NullableResult.Exception(t))
            }
        })

        registerOnCompletion(continuation)
    }
}

abstract class NullableRetrofitCallResponse<T : Any?>{
    var forUserId: Long = 0

    fun generate(forUserId: Long, result: NullableResult<T?>){
        this.forUserId = forUserId
        when(result){
            is NullableResult.Ok<*> -> {
                ok(result.value as T, result.response)
            }
            is NullableResult.Error -> {
                error(result.exception, result.response)
            }
            is NullableResult.Exception -> {
                exception(result.exception)
            }
        }
    }

    open fun ok(value: T?, response: Response){

    }

    open fun error(exception: HttpException,
                   response: Response
    ){

    }

    open fun exception(exception: Throwable){

    }
}

interface ChildrenService {
    @POST( "school/children")
    fun getChildrenByIds(@Header("Authorization") forUserId: Long, @Body ids: List<Long>): Call<ArrayList<Child>>
}

private fun Call<*>.registerOnCompletion(continuation: CancellableContinuation<*>) {
    continuation.invokeOnCancellation {
        if (continuation.isCancelled)
            try {
                cancel()
            } catch (ex: Throwable) {
                //Ignore cancel exception
            }
    }
}