package com.ufony.trackersdk

import android.app.Application
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import kotlin.coroutines.CoroutineContext

class TrackerActivitySDKViewModel(val userId:Long,val authorisation: String,val context: Application):AndroidViewModel(context) {

    private val sJob = SupervisorJob()
     val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + sJob
    protected  var loggedInUserId :Long = 0L
     val showProgress: MutableLiveData<Boolean> = MutableLiveData()
    val isTripEmpty: MutableLiveData<Boolean> = MutableLiveData()
    var forUserId: Long? = null
    private var trackerTask: ITrackerTask = TrackerTask(RetrofitClient.getClient(context, authorisation ))
    private var _routes: MutableLiveData<HashMap<Long, List<LatLng>>>? = null
    val routes: MutableLiveData<HashMap<Long, List<LatLng>>>
        get() {
            if (_routes == null) {
                _routes = MutableLiveData()
            }
            return _routes!!
        }

    private var _trips: MutableLiveData<ArrayList<TripSubscription>>? = null

    val trips: MutableLiveData<ArrayList<TripSubscription>>
        get() {
            if (_trips == null) {
                _trips = MutableLiveData()
                _trips!!.value = ArrayList()
            }
            return _trips!!
        }
    private var _activeTrips: MutableLiveData<ArrayList<Trip>>? = null

    val activeTrips: MutableLiveData<ArrayList<Trip>>
        get() {
            if (_activeTrips == null) {
                _activeTrips = MutableLiveData()
                _activeTrips!!.value = ArrayList()
            }

            return _activeTrips!!
        }

    private var _showDialog: MutableLiveData<ArrayList<Child>>? = null


    val showDialog: LiveData<ArrayList<Child>>
        get() {
            if (_showDialog == null) {
                _showDialog = MutableLiveData();
            }

            return _showDialog!!
        }
    @Deprecated("No longer used. It is hold for future used.")
    fun loadRoute(coordinates: List<Coordinate>, routeId: Long) {
        Log.d("Log","Tracker: before check routes Value is null or not==")
        if (routes.value != null && routes.value?.containsKey(routeId) == true) {
            Log.d("Log","Tracker: When routes Value not null loadRoute==")
            routes.postValue(routes.value)
            return
        }

        GlobalScope.launch(Dispatchers.Main, CoroutineStart.DEFAULT) {
            val result = trackerTask.getRouteCords(coordinates)

            when (result) {
                is Result.Ok -> {
                    val cordsPoints = result.value.routes?.firstOrNull()?.overviewPolyline?.points

                    if (!cordsPoints.isNullOrEmpty()) {
                        val routeValues = if (routes.value == null) HashMap<Long, List<LatLng>>() else routes.value!!

                        if (!routeValues.contains(routeId)) {
                            routeValues.put(routeId, cordsPoints!!.decodePoly())
                            routes.postValue(routeValues)
                        }
                    }
                }

                else -> {}
            }
        }
    }

    fun loadRoutePolyline(routeId: Long, drawnRoute: String?) {
        Log.d("Log","Tracker loadRoutePolyline: before check routes Value is null or not==")
        if (routes.value != null && routes.value?.containsKey(routeId) == true) {
            routes.postValue(routes.value)
            Log.d("Log","Tracker loadRoutePolyline: after check routes Value is null or not==")
            return
        }

        val routeValues = if (routes.value == null) HashMap<Long, List<LatLng>>() else routes.value!!

        routeValues.put(routeId, if (drawnRoute == null) ArrayList<LatLng>() else drawnRoute.decodePoly())

        routes.value = routeValues

        routes.postValue(routes.value)
    }

    /**
     * Method to decode polyline points
     * Courtesy : http://jeffreysambells.com/2010/05/27/decoding-polylines-from-google-maps-direction-api-with-java
     */
    private fun String.decodePoly(): List<LatLng> {

        val poly = ArrayList<LatLng>()
        var index = 0
        val len = this.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = this[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat

            shift = 0
            result = 0
            do {
                b = this[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng

            val p = LatLng(lat.toDouble() / 1E5,
                lng.toDouble() / 1E5)
            poly.add(p)
        }
        return poly
    }

    suspend fun subscribeForParent(forUserId: Long, connectionId: String,routeId:Long?, useSignalRCore: Boolean) {
        Log.d("Log","Tracker: Subscribing for parent")
        showProgress.postValue(true)

        val result = trackerTask.subscribeForParent(forUserId, connectionId,routeId,useSignalRCore)

        Log.d("Log","Tracker: Subscribing for parent finished")


        when (result) {
            is Result.Ok -> {
                Log.d("Log","Tracker: Result ok")
                saveTripSubsriptions(result.value.trips)
                result.value.trips?.filter { !it.drawnRoute.isNullOrEmpty() }?.forEach {
                    loadRoutePolyline(it.routeId, it.drawnRoute)
                }
                val prefs = UserPreferenceManager.forUser(userId,context)
                if ((result.value.trips == null || result.value.trips!!.isEmpty())&& prefs.trips==null) {
                    Toast.makeText(context, "No active trips", Toast.LENGTH_LONG).show()
                    showProgress.postValue(false)
                }
                showProgress.postValue(false)
            }
            else -> {
                Toast.makeText(context, "An error occurred while fetching active trips", Toast.LENGTH_SHORT).show()
                showProgress.postValue(false)
            }
        }
    }
    suspend fun getActiveTrips(forUserId: Long) {
        Log.d("Log","Tracker: Subscribing for parent")
        showProgress.postValue(true)
        val result=trackerTask.subscribeForActiveRoutes(loggedInUserId)


        when(result){
            is Result.Ok -> {
                if (result.value.trips == null || result.value.trips.isEmpty()) {
                    //Toast.makeText(getApplication(), "No active routes", Toast.LENGTH_SHORT).show()
                    isTripEmpty.postValue(true)
                    showProgress.postValue(false)
                }else{
                    saveActiveTripSubsriptions(result.value)
                    showProgress.postValue(false)
                }
            }
            is Result.Error -> {
                if (result.response.code() == 401) {
                    Toast.makeText(context, "UnAuthorized", Toast.LENGTH_LONG).show()
                    showProgress.postValue(false)
//                         PreferenceManagerKt.setUnAuthorized(true, getApplication())
//                         _unAuthorized?.postValue(Unit)
                } else {
                    Toast.makeText(
                        context,
                        "An error occurred while fetching active trips",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            else -> {}
        }

    }
    fun saveActiveTripSubsriptions(activeTrip: ActiveRoutes) {
        var tripSubscriptions = activeTrip.trips
        if (tripSubscriptions == null)
            tripSubscriptions = ArrayList()

//        forUserId?.let {
//            UserPreferenceManager.forUser(it, getApplication()).setTripSubScription(tripSubscriptions.toJson())
//        }
        Log.d("TrsckerViewModel", "activeTrips==="+tripSubscriptions.toJson())
        isTripEmpty.postValue(false)
        activeTrips.postValue(tripSubscriptions)
    }
    fun saveTripSubsriptions(tripSubscriptions: ArrayList<TripSubscription>?) {
        var tripSubscriptions = tripSubscriptions

        if (tripSubscriptions == null)
            tripSubscriptions = ArrayList()

        forUserId?.let {
            UserPreferenceManager.forUser(it, context).setTripSubScription(tripSubscriptions.toJson())
        }

        trips.postValue(tripSubscriptions)
    }








    fun fetchdata(child: ArrayList<Child>) {



    }

    fun dataListForRoute(profiles: ArrayList<Child>,context: Context): ArrayList<RouteStopAlertDialogPojo> {
        val dialogData = ArrayList<RouteStopAlertDialogPojo>()

        if (profiles.any{ it.inRouteStop != null || it.outRouteStop != null}) {
            //In Route
            val filter = { child: Child, isOutRoute: Boolean ->
                val routeStop = if(isOutRoute) child.outRouteStop else child.inRouteStop

                routeStop?.let {
                    val route = if(isOutRoute) child.outRoute else child.inRoute

                    val alertDistance = if(isOutRoute) child.outRouteAlertDistance else child.inRouteAlertDistance

                    val existingStop = dialogData.firstOrNull { it.stopName == routeStop.name && it.isOutRoute == isOutRoute }

                    if(existingStop == null){
                        val group = RouteStopAlertDialogPojo(route.name,
                            isOutRoute,
                            routeStop.name,
                            arrayListOf(child.id),
                            "Alert Radius : " + (alertDistance / 100).toDouble() / 10,
                            child.inRouteAlertDistance,
                            child.outRouteAlertDistance,
                            routeStop.stopLocation)
                        dialogData.add(group)
                    }else{
                        existingStop.childrenIds.add(child.id)
                    }
                }
            }

            profiles.forEach { profile ->
                filter(profile, false)
                filter(profile, true)
            }
        } else
            Toast.makeText(context, "No data", Toast.LENGTH_LONG).show()

        Log.d("Log",  dialogData.toJson())

        return dialogData
    }

    override fun onCleared() {
        sJob.cancelChildren()
        super.onCleared()
    }
}


object RetrofitClient {

    // Define your Base URL here
    private const val BASE_URL = "https://web.zoment.com/euro/api/"
      private const val API_VERSION: String = "api-version"
     private const val API_VERSION_VALUE: String = "21"

    // Hold the singleton instance
    @Volatile
    private var retrofit: Retrofit? = null

    fun getClient(context: Context, authorisation:String): Retrofit {
        return retrofit ?: synchronized(this) {
            retrofit ?: buildRetrofit(context,authorisation).also { retrofit = it }
        }
    }

    private fun buildRetrofit(context: Context,authorisation:String): Retrofit {
        val gson: Gson = GsonBuilder().create() // You can customize Gson here if needed

        // 1. Headers Interceptor
        val headersInterceptor = Interceptor { chain ->
            var requestBuilder = chain.request()
                .newBuilder()
                .addHeader("Content-Type", "application/json")

            // Use the passed context to get the token

                 requestBuilder = requestBuilder.addHeader("Authorization", authorisation)


            requestBuilder = requestBuilder.addHeader( API_VERSION,  API_VERSION_VALUE)

            val request = requestBuilder.build()
            chain.proceed(request)
        }

        // 2. PUT/POST Body Fix Interceptor
        val putInterceptor = Interceptor { chain ->
            val originalRequest = chain.request()

            if ((originalRequest.method() == "PUT" || originalRequest.method() == "POST") &&
                (originalRequest.body() == null || originalRequest.body()?.contentLength() == 0L)) {

                // --- CHANGE STARTS HERE ---

                // 1. Define Media Type standard way
                val mediaType = okhttp3.MediaType.parse("application/json")

                // 2. Create RequestBody standard way
                // Note: If you are on very old OkHttp 3, swap arguments to: create(mediaType, "{}")
                val fixedBody = okhttp3.RequestBody.create(mediaType, "{}")

                // --- CHANGE ENDS HERE ---

                val newRequest = originalRequest.newBuilder()
                    .method(originalRequest.method(), fixedBody)
                    .build()

                chain.proceed(newRequest)
            }
            else chain.proceed(originalRequest)
        }

        // 3. Build OkHttp Client
        val okHttpClientBuilder = OkHttpClient.Builder()
            .addInterceptor(putInterceptor)
            .addInterceptor(headersInterceptor)
            .connectTimeout(120, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.MINUTES)
            .writeTimeout(5, TimeUnit.MINUTES)

        // 4. Logging (Debug only)


        // 5. Build Retrofit
        return Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create(gson))
            .baseUrl(BASE_URL) // Use the variable defined at the top
            .client(okHttpClientBuilder.build())
            .build()
    }
}