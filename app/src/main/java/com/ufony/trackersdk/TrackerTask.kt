package com.ufony.trackersdk

import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import retrofit2.Call
import retrofit2.Callback
import retrofit2.HttpException
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.QueryMap
import java.io.Serializable
import kotlin.coroutines.resume

class Coordinate : Serializable {
    var longitude: Double = 0.0
    var latitude: Double = 0.0

    companion object {
        private const val serialVersionUID = 1L
    }
}


data class OverviewPolylineResponse(val points: String)

data class GoogleRouteResponse(@SerializedName("overview_polyline") val overviewPolyline: OverviewPolylineResponse? = null)

data class GoogleDirectionsResponse(val routes: List<GoogleRouteResponse>? = null)

data class ActiveRoutes(
    val trips: ArrayList<Trip>
): Serializable


data class Trip(
    val inProgress: Boolean,
    val lastKnownLocation: LastKnownLocation,
    val name: String,
    val routeId: Long,
    val trackerDeviceId: Long,
    val tripId: Long
): Serializable

data class LastKnownLocation(
    val latitude: Double,
    val longitude: Double
): Serializable

interface ITrackerTask {

    suspend fun getRouteCords(coordinates: List<Coordinate>) : Result<GoogleDirectionsResponse>

    suspend fun subscribeForParent(forUserId: Long, signalRConnectionId: String, routeId:Long?,useSignalRCore: Boolean) : Result<Subscription>
    suspend fun subscribeForActiveRoutes(forUserId: Long) : Result<ActiveRoutes>
}

interface TripsService {

    @POST("trip/subscribe/{connectionId}")
    fun postSubscribe(@Header("user-id") forUserId: Long, @Path("connectionId") connectionId: String, @Query("routeIds") routeId: Long?, @Query("useSignalRCore") useSignalRCore: Boolean): Call<Subscription>
    @GET("trip/activetrips")
    fun getActiveRoutes(@Header("user-id") forUserId: Long): Call<ActiveRoutes>
}

interface GoogleDirectionsService {
    @GET("https://maps.googleapis.com/maps/api/directions/json")
    fun getDirections(@QueryMap query: HashMap<String, String>): Call<GoogleDirectionsResponse>
}

class Subscription : Serializable {
    /**
     * @return the trips
     */
    /**
     * @param trips the trips to set
     */
    var trips: java.util.ArrayList<TripSubscription>? = null

    companion object {
        private const val serialVersionUID = 1L
    }
}

class TripSubscription : Serializable {

    var tripId: Long = 0
    var routeId: Long = 0
    var lastKnownLocation: Coordinate? = null

    companion object {
        private const val serialVersionUID = 1L
    }

    var stops: List<Coordinate>? = null
    var drawnRoute: String? = null
    var name: String? = null
    val caretakerNumber: String?=null
    val caretakerName:String?=null
}

class TrackerTask(val retrofit: Retrofit) : ITrackerTask{


        override suspend fun getRouteCords(coordinates: List<Coordinate>): Result<GoogleDirectionsResponse> {
            val directionsService = retrofit.create(GoogleDirectionsService::class.java)

            val options = HashMap<String, String>()

            var waypointsQuery = ""

            for (i in coordinates.indices){
                when (i) {
                    0 -> {
                        options["origin"] = "${coordinates[i].latitude},${coordinates[i].longitude}"
                    }
                    coordinates.size - 1 -> {
                        options["destination"] = "${coordinates[i].latitude},${coordinates[i].longitude}"
                    }
                    else -> {
                        waypointsQuery += "${coordinates[i].latitude},${coordinates[i].longitude}|"
                    }
                }
            }

            if(waypointsQuery.isNotEmpty()){
                options["waypoints"] = waypointsQuery.trimEnd('|')
            }

            options["sensor"] = "false"

            val result = directionsService.getDirections(options).awaitResult()

            return result
        }

        override suspend fun subscribeForParent(forUserId: Long, signalRConnectionId: String, routeId:Long?,useSignalRCore: Boolean): Result<Subscription> {
            val tripsService = retrofit.create(TripsService::class.java)
            val result = tripsService.postSubscribe(forUserId, signalRConnectionId,routeId, useSignalRCore).awaitResult()
            return result
        }
        override suspend fun subscribeForActiveRoutes(forUserId: Long): Result<ActiveRoutes> {
            val tripsService = retrofit.create(TripsService::class.java)
            val result = tripsService.getActiveRoutes(forUserId).awaitResult()
            return result
        }
    }


suspend fun <T : Any> Call<T>.awaitResult(): Result<T> {
    return suspendCancellableCoroutine { continuation ->
        enqueue(object : Callback<T> {
            override fun onResponse(call: Call<T>?, response: Response<T>) {
                continuation.resume(
                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body == null) {
                            Result.Exception(NullPointerException("Response body is null"))
                        } else {
                            Result.Ok(body, response.raw())
                        }
                    } else {
                        Result.Error(HttpException(response), response.raw())
                    }
                )
            }

            override fun onFailure(call: Call<T>, t: Throwable) {
                // Don't bother with resuming the continuation if it is already cancelled.
                if (continuation.isCancelled) return
                continuation.resume(Result.Exception(t))
            }
        })

        registerOnCompletion(continuation)
    }
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

interface ServiceResultThrowable{
    fun getThrowable(): Throwable
}

/**
 * Sealed class of HTTP result
 */
@Suppress("unused")
public sealed class Result<out T : Any> {
    /**
     * Successful result of request without errors
     */
    public class Ok<out T : Any>(
        public val value: T,
        override val response: okhttp3.Response
    ) : Result<T>(), ResponseResult {
        override fun toString() = "Result.Ok{value=$value, response=$response}"
    }

    /**
     * HTTP error
     */
    public class Error(
        override val exception: HttpException,
        override val response: okhttp3.Response
    ) : Result<Nothing>(), ErrorResult, ResponseResult, ServiceResultThrowable {
        override fun toString() = "Result.Error{exception=$exception}"

        override fun getThrowable(): Throwable {
            return exception
        }
    }

    /**
     * Network exception occurred talking to the server or when an unexpected
     * exception occurred creating the request or processing the response
     */
    public class Exception(
        override val exception: Throwable
    ) : Result<Nothing>(), ErrorResult, ServiceResultThrowable {
        override fun toString() = "Result.Exception{$exception}"

        override fun getThrowable(): Throwable {
            return exception
        }
    }

}

@Suppress("unused")
public sealed class NullableResult<out T : Any?> {
    /**
     * Successful result of request without errors
     */
    public class Ok<out T : Any?>(
        public val value: T?,
        override val response: okhttp3.Response
    ) : NullableResult<T?>(), ResponseResult {
        override fun toString() = "Result.Ok{value=$value, response=$response}"
    }

    /**
     * HTTP error
     */
    public class Error(
        override val exception: HttpException,
        override val response: okhttp3.Response
    ) : NullableResult<Nothing>(), ErrorResult, ResponseResult {
        override fun toString() = "Result.Error{exception=$exception}"
    }

    /**
     * Network exception occurred talking to the server or when an unexpected
     * exception occurred creating the request or processing the response
     */
    public class Exception(
        override val exception: Throwable
    ) : NullableResult<Nothing>(), ErrorResult {
        override fun toString() = "Result.Exception{$exception}"
    }
}

/**
 * Interface for [Result] classes with [okhttp3.Response]: [Result.Ok] and [Result.Error]
 */
public interface ResponseResult {
    val response: okhttp3.Response
}

/**
 * Interface for [Result] classes that contains [Throwable]: [Result.Error] and [Result.Exception]
 */
public interface ErrorResult {
    val exception: Throwable
}

/**
 * Returns [Result.Ok.value] or `null`
 */
public fun <T : Any> Result<T>.getOrNull(): T? =
    if (this is Result.Ok) this.value else null

/**
 * Returns [Result.Ok.value] or [default]
 */
public fun <T : Any> Result<T>.getOrDefault(default: T): T =
    getOrNull() ?: default

/**
 * Returns [Result.Ok.value] or throw [throwable] or [ErrorResult.exception]
 */
public fun <T : Any> Result<T>.getOrThrow(throwable: Throwable? = null): T {
    return when (this) {
        is Result.Ok -> value
        is Result.Error -> throw throwable ?: exception
        is Result.Exception -> throw throwable ?: exception
    }
}