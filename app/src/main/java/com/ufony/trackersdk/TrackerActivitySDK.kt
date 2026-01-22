package com.ufony.trackersdk

import android.animation.ObjectAnimator
import android.animation.TypeEvaluator
import android.app.Application
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.Property
import android.view.View
import android.view.Window
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.Observer
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener
import com.google.android.gms.maps.MapFragment
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.microsoft.signalr.HubConnection
import com.microsoft.signalr.HubConnectionBuilder
import com.microsoft.signalr.HubConnectionState
import com.rey.material.widget.ProgressView
import com.ufony.trackersdk.databinding.TrackerActivitySdkBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class TrackerActivitySDK( ) :AppCompatActivity(), OnMapReadyCallback, CoroutineScope,
    OnInfoWindowClickListener {
    val sJob = SupervisorJob()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + sJob


    private var googleMap: GoogleMap? = null
    private lateinit var binding: TrackerActivitySdkBinding
    private var marker: Marker? = null
    private lateinit var statusContainerFrame: RelativeLayout
    private lateinit var trackerActivityViewModel: TrackerActivitySDKViewModel
    private lateinit var trackerLocationListener: ITrackerLocationListener
    private var existingRouteMarkers = HashMap<Long, Marker>()
    private val alreadyDrawnRoutes = ArrayList<Long>()
    private val markerAnimators = HashMap<Long, ObjectAnimator>()
    private var firstLoad: Boolean = true
    private var selectedRouteId: Long? = null
    private var hasSingleRoute: Boolean = false
    private var dialogData = ArrayList<RouteStopAlertDialogPojo>()
    private lateinit var alertDialogAdapter: RouteStopAlertDialogAdapter
    private var alertDistanceDialog: AlertDialog? = null
    private val forUserId :Long  = 0
     private lateinit var tv_trips: TextView
    private lateinit var tv_trips1: TextView
    private var singleTrip: String? = null
    private lateinit var optionMenu_btn: ImageButton
    private lateinit var progressBar: ProgressBar
    private lateinit var progress_layout:RelativeLayout
    private lateinit var progressView: ProgressView
    private  var loggedInUserId:Long = 0L


    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
        binding = TrackerActivitySdkBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        supportActionBar?.hide()
        tv_trips = findViewById(R.id.tv_trips)
        tv_trips1 = findViewById(R.id.tv_trips1)
        optionMenu_btn = findViewById(R.id.btn_navMore)
        progress_layout= findViewById(R.id.progress_layout)
        progressBar= findViewById(R.id.progress_fetching_trip)
        val pref = UserPreferenceManager.forUser(forUserId, this)
        loggedInUserId = intent.getLongExtra(UserPreferenceMangerKeys.SDK_LIBRARY_USER_ID,0L)
        val authorisation = intent.getStringExtra(UserPreferenceMangerKeys.SDK_LIBRARY_AUTH_HEADER)
        if(authorisation==null || authorisation.isBlank() || authorisation.isEmpty() || loggedInUserId==0L){
            Toast.makeText(this,"Login failed try again !",Toast.LENGTH_SHORT).show()
            finish()
        }

        val factory = TrackerViewModelFactory(application, loggedInUserId, authorisation!!)

        trackerActivityViewModel = ViewModelProvider(this,factory).get(TrackerActivitySDKViewModel::class.java)
        Log.d("OnResumeCalled", "ACTIVITYLIFECYCLE===OnCreate")
        statusContainerFrame = findViewById(R.id.statusContainerFrame)
        statusContainerFrame.createCircularReveal(1000)
         trackerActivityViewModel.routes.observe(this, Observer { routesHaspMap ->
            val BLUE = -16537100
            routesHaspMap!!.filter { route -> !alreadyDrawnRoutes.contains(route.key) && route.value.isNotEmpty() }
                .forEach { route ->
                    val routePolyline = PolylineOptions()
                    routePolyline.addAll(route.value)
                    routePolyline.width(10.0F)
                    routePolyline.color(BLUE)
                    googleMap?.addPolyline(routePolyline)
                    alreadyDrawnRoutes.add(route.key)
                }
        })
        val prefs = UserPreferenceManager.forUser(forUserId, this)
        prefs.authorisation = authorisation
        val i = intent
        val bundle = i.extras
        if (bundle != null) {
            singleTrip = i.getStringExtra("activity")
            prefs.singleTrip = singleTrip

        }

        trackerActivityViewModel.trips.observe(this, Observer { trips ->
            hasSingleRoute = trips?.size == 1

            if (hasSingleRoute) {
                selectedRouteId = trips[0].routeId
            }

            val routeIds = trips!!.map { it.routeId }
            val routesToRemove = existingRouteMarkers.filter { !routeIds.contains(it.key) }
            routesToRemove.forEach {
                existingRouteMarkers[it.key]!!.remove()
                existingRouteMarkers.remove(it.key)
            }

            val intent = intent
            val bundles = intent.extras
            try {
                if (bundles == null && prefs.trips == null) {
                    trips.forEach {
                        if (it.lastKnownLocation != null) {
                            addOrUpdateMarker(
                                it.lastKnownLocation!!.latitude,
                                it.lastKnownLocation!!.longitude,
                                it.routeId,
                                it.name,
                                it.caretakerName,
                                it.caretakerNumber
                            )
                        }
                    }
                } else {
                    googleMap?.clear()
                    marker?.remove()
                    existingRouteMarkers?.clear()
                    val trip = bundles!!.getSerializable("tripSubsCription") as Trip?
                    prefs.trips = trip
                    tv_trips.text = prefs.trips!!.name
                    tv_trips1.text = prefs.trips!!.name
                    addOrUpdateMarker(
                        prefs.trips!!.lastKnownLocation!!.latitude,
                        prefs.trips!!.lastKnownLocation!!.longitude,
                        prefs.trips!!.routeId,
                        prefs.trips!!.name
                    )
                }
            } catch (e: Exception) {

            }
            if (firstLoad && existingRouteMarkers.size > 0) {
                firstLoad = false
                recenterMap()
            }
        })

        val mapFragment = fragmentManager.findFragmentById(R.id.map) as MapFragment
        mapFragment.getMapAsync(this)

        val currentUser = UserPreferenceManager.forUser(loggedInUserId, this).currentUser;

        trackerLocationListener = if (!currentUser?.signalRCoreUrl.isNullOrBlank()) {
            Log.d("track","CallUpdatedSignalr==")
            TrackerLocationListener(this.lifecycle, currentUser!!.signalRCoreUrl!!)
        } else
            TrackerLocationListenerV1(this.lifecycle, currentUser?.signalRRootUrl)


        trackerLocationListener.onRouteUpdateCallback = { update ->
            if (update.location != null) {
                launch {
                    Log.d("tracker","onRouteUpdateCallback==" + update.routeId)
                    addOrUpdateMarker(
                        update.location!!.latitude,
                        update.location!!.longitude,
                        update.routeId,
                        null
                    )
                }
            }
        }



        trackerLocationListener.onStateChangedCallback = { connectionState ->
            Log.d("traker","onStateChangedCallback==")
            stateChanged(connectionState)
        }

        trackerLocationListener.onConnectionIdChangedCallback = { connectionId ->
            launch {
                prefs.connectionId = connectionId
                try {
                    if (prefs.trips == null) {
                        trackerActivityViewModel.subscribeForParent(
                            loggedInUserId,
                            connectionId, null,
                            trackerLocationListener is TrackerLocationListener
                        )
                        trackerActivityViewModel.showProgress
                    } else {
                        trackerActivityViewModel.subscribeForParent(
                            loggedInUserId,
                            connectionId, prefs.trips!!.routeId,
                            trackerLocationListener is TrackerLocationListener
                        )
                    }
                } catch (e: Exception) {

                }

            }
        }

        this.lifecycle.addObserver(trackerLocationListener as LifecycleObserver)


        configMap()

        trackerActivityViewModel.showDialog.observe(this, Observer {
            var profiles = it
            dialogData = trackerActivityViewModel.dataListForRoute(it, this)
            if (dialogData.size != 0) {
                alertDialogAdapter.dialogData = dialogData
                alertDialogAdapter.notifyDataSetChanged()
            } else
                alertDistanceDialog?.dismiss()
            progressView.stop()
            progressView.visibility = View.GONE
        })

        trackerActivityViewModel!!.showProgress.observe(this) { it ->
            if (it) {
                if (progressBar!= null&& progress_layout != null)
                    progress_layout.visibility= View.VISIBLE
                progressBar.visibility= View.VISIBLE
            } else {
                if (progressBar!= null && progressBar!!.isVisible)
                    progress_layout.visibility= View.GONE
                progressBar.visibility= View.GONE
            }
        }

        binding.trackerBackBtn.setOnClickListener {
            prefs.singleTrip = null
//            startActivity(
//                Intent(
//                    this@TrackerActivity,
//                    HomeActivity::class.java
//                ).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)   1 3 2 3 3
//            )
            finish()
        }

        binding.tvToolbar.setOnClickListener {

            intent = Intent(this, TripListActivity::class.java)
            startActivity(intent)
            finish()
        }

    }

    private fun configMap() {
        val maxZoomLevel =17.0f
        googleMap?.setMaxZoomPreference(maxZoomLevel)
    }

    override fun onMapReady(p0: GoogleMap?) {
        this.googleMap = googleMap

        if (googleMap == null) {
            Toast.makeText(applicationContext, "Sorry! unable to access map.", Toast.LENGTH_SHORT).show()
        } else {
            val location = LatLng(21.0000, 78.0000)

            val cameraPosition = CameraPosition.Builder()
                .target(location).zoom(4f).build()

            googleMap!!.uiSettings.isMapToolbarEnabled = false

            googleMap!!.animateCamera(
                CameraUpdateFactory
                    .newCameraPosition(cameraPosition)
            )


            googleMap!!.setOnCameraMoveStartedListener { reason ->
                if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE)
                    selectedRouteId = null
            }

            googleMap!!.setOnMapClickListener {
                selectedRouteId = null
            }

            try {
                val success = googleMap!!.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                        this, R.raw.gmap_style_json
                    )
                )

                if (!success) {
                    Log.d("tracker","Transport: Style parsing failed.")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            googleMap?.setOnMarkerClickListener { marker ->
                if (marker == null)
                    false

                val markerContext = marker.tag as GoogleMarkerContext
                selectedRouteId = markerContext.routeId

                false
            }

            configMap()
        }
        val fragmentManager = supportFragmentManager
        googleMap?.setInfoWindowAdapter(PopupAdapter(layoutInflater,fragmentManager,this@TrackerActivitySDK))
        googleMap?.setOnInfoWindowClickListener(this@TrackerActivitySDK)
    }

    override fun onInfoWindowClick(p0: Marker?) {
        TODO("Not yet implemented")
    }

    private fun stateChanged(connectionState: TrackerLocationListenerState) {
        Log.d("tRACKER","Tracker: $connectionState status changed")

        launch {
            val signarRStatusText = findViewById<TextView>(R.id.signalr_status_text)

            signarRStatusText.text = connectionState.toString()

            when (connectionState) {
                TrackerLocationListenerState.Connected -> {
                    if (signarRStatusText.visibility == View.VISIBLE) {
                        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                            val expandCollapseTransition = AutoTransition().apply {
                                interpolator =
                                    AnimUtils.getFastOutSlowInInterpolator(this@TrackerActivitySDK)
                                startDelay = 2000
                                Log.d("tracker","Tracker: TrackerLocationListenerStateConnected")
                            }
                            TransitionManager.beginDelayedTransition(
                                statusContainerFrame,
                                expandCollapseTransition
                            )
                        }
                        signarRStatusText.visibility = View.GONE
                    }
                }
                else -> {
                    if (signarRStatusText.visibility != View.VISIBLE) {
                        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                            val expandCollapseTransition = AutoTransition().apply {
                                interpolator =
                                    AnimUtils.getFastOutSlowInInterpolator(this@TrackerActivitySDK)
                                startDelay = 0
                            }
                            TransitionManager.beginDelayedTransition(
                                statusContainerFrame,
                                expandCollapseTransition
                            )
                        }
                        signarRStatusText.visibility = View.VISIBLE
                    }
                }
            }

            val drawable = when (connectionState) {
                TrackerLocationListenerState.Connected -> {
                    R.drawable.signalr_map_state_connected
                }
                TrackerLocationListenerState.Disconnected -> {
                    R.drawable.signalr_map_state_disconnected
                }
                else -> {
                    R.drawable.signalr_map_state_connecting
                }
            }

            if (connectionState==TrackerLocationListenerState.Connected){
                progress_layout.visibility=View.VISIBLE
                progressBar.visibility=View.VISIBLE
            }
            val sdk = android.os.Build.VERSION.SDK_INT

            var ellipse = findViewById<View>(R.id.signalr_status_inner)

            if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                ellipse.setBackgroundDrawable(getResources().getDrawable(drawable))
            } else {
                ellipse.background =
                    if (sdk < android.os.Build.VERSION_CODES.LOLLIPOP) getResources().getDrawable(
                        drawable
                    ) else getDrawable(drawable)
            }
        }
    }

    fun addOrUpdateMarker(lat: Double, lng: Double, routeId: Long, title: String? = null, caretakerName: String? = null, caretakerNumber: String? = null) {
        Log.d("TrackerActivity", "tripComponent===3=" + googleMap)
        if (googleMap == null)
            return

        val position = LatLng(lat, lng)
        if (!existingRouteMarkers.containsKey(routeId)) {
            val newMarkerOptions = MarkerOptions()
                .anchor(0.5f, 0.93f)
                .position(position)

            if (!title.isNullOrEmpty()) {
                newMarkerOptions.title(title)
            }

            val bitmap = BitmapFactory.decodeResource(resources, R.drawable.marker_bus_idle)
            newMarkerOptions.icon(BitmapDescriptorFactory.fromBitmap(bitmap))

            marker = googleMap!!.addMarker(newMarkerOptions)
            marker!!.tag = GoogleMarkerContext(routeId, position.latitude, position.longitude, title,caretakerName, caretakerNumber)
            existingRouteMarkers.put(routeId, marker!!)

        } else {
            if (title?.isNotEmpty() == true) {
                existingRouteMarkers[routeId]!!.title = title
            }
            Log.d("TrackerActivity", "UdateOldRouteID===" + existingRouteMarkers[routeId]?.tag)
            val context = (existingRouteMarkers[routeId]!!.tag as GoogleMarkerContext)
            Log.d("TrackerActivity", "UdateOldRouteID===" + existingRouteMarkers[routeId])
            if (distance(
                    context.lat.toFloat(),
                    context.lng.toFloat(),
                    position.latitude.toFloat(),
                    position.longitude.toFloat()
                ) > 20
            ) {
                animateMarkerToICS(
                    existingRouteMarkers[routeId]!!,
                    position,
                    LatLngInterpolator.Linear()
                )
                context.lat = position.latitude
                context.lng = position.longitude
            }
        }
    }

    fun distance(lat_a: Float, lng_a: Float, lat_b: Float, lng_b: Float): Float {
        val earthRadius = 3958.75
        val latDiff = Math.toRadians((lat_b - lat_a).toDouble())
        val lngDiff = Math.toRadians((lng_b - lng_a).toDouble())
        val a =
            sin(latDiff / 2) * sin(latDiff / 2) + cos(Math.toRadians(lat_a.toDouble())) * cos(
                Math.toRadians(
                    lat_b.toDouble()
                )
            ) *
                    sin(lngDiff / 2) * sin(lngDiff / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        val distance = earthRadius * c

        val meterConversion = 1609

        return (distance * meterConversion).toFloat()
    }
    fun onRecenterClick(view: View?) {
        recenterMap()
    }

    fun recenterMap() {

        selectedRouteId = null

        if (existingRouteMarkers.size > 1) {
            var bounds = LatLngBounds.Builder()
            existingRouteMarkers.forEach {
                bounds.include(it.value.position)
            }

            googleMap?.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 100))
        } else if (existingRouteMarkers.size == 1) {
            val position = existingRouteMarkers.values.first().position
            val cameraPosition =
                CameraPosition.Builder().target(LatLng(position.latitude, position.longitude))
                    .zoom(14f).build()
            googleMap?.animateCamera(
                CameraUpdateFactory
                    .newCameraPosition(cameraPosition)
            )
        }
    }

    override fun onDestroy() {
        sJob.cancelChildren()
        val prefs = UserPreferenceManager.forUser(forUserId, this)
        prefs.trips = null
        super.onDestroy()
    }

    fun animateMarkerToICS(
        marker: Marker,
        finalPosition: LatLng,
        latLngInterpolator: LatLngInterpolator,
    ) {
        val markerContext = marker.tag as GoogleMarkerContext
        val hasExistingAnimation = markerAnimators.containsKey(markerContext.routeId)

        val typeEvaluator = TypeEvaluator<LatLng> { fraction, startValue, endValue ->
            latLngInterpolator.interpolate(fraction, startValue, endValue)
        }

        val property = Property.of(Marker::class.java, LatLng::class.java, "position")
        val animator = ObjectAnimator.ofObject(marker, property, typeEvaluator, finalPosition)
        animator.duration = 4000
        animator.addUpdateListener {
            if (selectedRouteId != markerContext.routeId || googleMap == null)
                return@addUpdateListener

            val currentMarkerPosition = it.animatedValue as LatLng
            val cameraPosition = CameraPosition.Builder()
                .target(currentMarkerPosition).zoom(googleMap!!.cameraPosition.zoom).build()

            googleMap!!.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
        }

        animator.start()

        if (hasExistingAnimation) {
            markerAnimators[markerContext.routeId]!!.cancel()
            markerAnimators[markerContext.routeId]!!.removeAllListeners()
            markerAnimators.remove(markerContext.routeId)
        }

        markerAnimators[markerContext.routeId] = animator
    }



}

data class GoogleMarkerContext(val routeId: Long, var lat: Double, var lng: Double,val title: String? = null ,val caretakerName: String? = null, val caretakerNumber: String? = null)

class TrackerLocationListener(val lifecycle: Lifecycle, private val signalRRootUrl: String) :
    LifecycleObserver, ITrackerLocationListener, CoroutineScope {
    val TAG = "TrackerLocationListener3";

    val sJob = SupervisorJob()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + sJob
    private var mHubConnection: HubConnection? = null
    override var onStateChangedCallback: StateChangedCallback? = null
    override var onConnectionIdChangedCallback: ConnectionIdCallback? = null
    override var onTrackerUpdateCallback: TrackerUpdateCallback? = null
    override var onRouteUpdateCallback: RouteUpdateCallback? = null
    override var onUpdateCallback: UpdateCallback? = null
    private var retiresCount = 0

    var connectionId: String? = null;

    override var state: TrackerLocationListenerState = TrackerLocationListenerState.Disconnected
        set(value) {
            field = value
            onStateChangedCallback?.invoke(field)
        }

    val status: HubConnectionState?
        get() {
            return mHubConnection?.connectionState
        }


    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    override fun start() {
        retiresCount = 0
        if (mHubConnection != null) {
            state = state
        }

        try {
            Log.d(TAG, "start before launch")
            launch {
                Log.d(TAG, "before startSignalR()")
                startSignalR()
                Log.d(TAG, "after startSignalR()")
            }
        } catch (e: Exception) {
             Log.d("TrackerLocationListener", "exception2=" + e.message)

        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    override fun stop() {
        launch {
            Log.d(TAG, "before stopSignalR()")
            stopSignalR()
            Log.d(TAG, "after stopSignalR()")
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun destroy() {
        Log.d(TAG, "before sJob.cancelChildren()")
        sJob.cancelChildren()
        Log.d(TAG, "after sJob.cancelChildren()")
    }

    fun startSignalR() {
        if (mHubConnection?.connectionState == HubConnectionState.CONNECTED) {
            Log.d(TAG, "mHubConnection already connected")
            return
        }
        Log.d(TAG,"SignalR url: $signalRRootUrl")

        state = TrackerLocationListenerState.Connecting

        mHubConnection = HubConnectionBuilder.create(signalRRootUrl).build()

        mHubConnection?.run {
            on<String>(
                "myConnectionId",
                { response ->
                    Log.d(TAG,"Connection id updated: $response")
                    onConnectionIdChangedCallback?.invoke(response)
                }, String::class.java
            )

            on<TrackerDeviceLocationUpdateResponse>(
                "updateDeviceLocation",
                { response ->
                    onTrackerUpdateCallback?.invoke(response)
                    onUpdateCallback?.invoke(response)
                }, TrackerDeviceLocationUpdateResponse::class.java
            )

            on<TripUpdateResponse>(
                "updateLocation",
                { response ->
                    onRouteUpdateCallback?.invoke(response)
                    onUpdateCallback?.invoke(response)
                }, TripUpdateResponse::class.java
            )

            onClosed {
                Log.d(TAG,"onClosed")
                state = TrackerLocationListenerState.Disconnected

                launch {
                    Log.d(TAG,"onClosed:150")
                    if(!lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED))
                        return@launch

                    Log.d(TAG,"onClosed:150= stoping")
                    stopSignalR()
                    delay(1000L * Math.min(retiresCount + 3, 7))

                    if(lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED) && (mHubConnection == null || mHubConnection!!.connectionState == HubConnectionState.DISCONNECTED)){
                        Log.d(TAG,"onClosed:151")
                        retiresCount++
                        Log.d(TAG, "onClosed:152 retiresCount = " + retiresCount.toString())
                        startSignalR()
                    }
                }

//                if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED) && retiresCount < 1) {
//                    Logger.logger(TAG,"onClosed:150")
//                    stopSignalR()
//                    Logger.logger(TAG,"onClosed:152")
//                    val timer = Timer()
//                    timer.scheduleAtFixedRate(
//                        object : TimerTask() {
//                            override fun run() {
//                                Logger.logger(TAG,"onClosed:timer:157")
//                                if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED) && (mHubConnection == null || mHubConnection!!.connectionState == HubConnectionState.DISCONNECTED)) {
//                                    Logger.logger(TAG,"onClosed:timer:159")
//                                    startSignalR()
//                                    Logger.logger(TAG,"onClosed:timer:161")
//                                    seconds -= 1
//                                    if (seconds == 0) timer.cancel()
//                                }
//                                Logger.logger(TAG,"onClosed:timer:165")
//                            }
//                        },
//                        1000, 6000
//                    )
//
//                    ++retiresCount
//
//                }
            }

            launch(Dispatchers.IO) {
                Log.d(TAG, "startSignalR -> launch:171 before start")
                try {
                    Log.d(TAG, "startSignalR -> starting")
                    start().blockingAwait()
                    retiresCount = 0
                    withContext(Dispatchers.Main){
                        state = TrackerLocationListenerState.Connected
                    }
                    Log.d(TAG, "startSignalR -> started")
                }
                catch (t: Throwable){
                    Log.d(TAG, "startSignalR -> error")
                    t.printStackTrace()
                    state = TrackerLocationListenerState.Disconnected

                     withContext(Dispatchers.Main){
                        state = TrackerLocationListenerState.Disconnected
                    }
                }
                Log.d(TAG, "startSignalR -> launch:171 after start")
            }
        }
    }

    fun stopSignalR() {
        Log.d(TAG, "stopSignalR:204")
        if (state == TrackerLocationListenerState.Disconnected)
            return

        Log.d(TAG, "stopSignalR before mHubConnection?.stop()")
        mHubConnection?.stop()
        mHubConnection = null
        state = TrackerLocationListenerState.Disconnected
        Log.d(TAG, "stopSignalR after mHubConnection?.stop()")
    }
}

class TrackerViewModelFactory(
    private val application: Application,
    private val userId: Long,
    private val authorisation: String
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TrackerActivitySDKViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            // This is where we manually create the instance
            return TrackerActivitySDKViewModel(userId, authorisation, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}