package com.ufony.trackersdk

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.content.Intent
import android.graphics.Point
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewAnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.ufony.trackersdk.databinding.ActivityRouteAlertRadiusSelectorBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.PUT
import kotlin.coroutines.CoroutineContext

class RouteAlertRadiusSelectorActivity : AppCompatActivity(), OnMapReadyCallback, CoroutineScope {
    val sJob = SupervisorJob()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + sJob

    companion object {
        const val ARG_DISTANCE = "DISTANCE"
        const val ARG_LOCATION_LATITUDE = "LOCATION_LATITUDE"
        const val ARG_LOCATION_LONGITUDE = "LOCATION_LONGITUDE"
        const val ARG_ROUTE_DATA = "ROUTE_DATA"
        const val ARG_CHILDREN_DATA = "CHILDREN_DATA"
        private const val EARTH_RADIUS = 6371009.0

        fun createIntent(context: Context, distance: Double, locationLat: Double, locationLong: Double, routeData: RouteStopAlertDialogPojo, children: ArrayList<ChildDetails>): Intent {
            return Intent(context, RouteAlertRadiusSelectorActivity::class.java).apply {
                putExtra(ARG_DISTANCE, distance)
                putExtra(ARG_LOCATION_LATITUDE, locationLat)
                putExtra(ARG_LOCATION_LONGITUDE, locationLong)
                putExtra(ARG_ROUTE_DATA, routeData)
                putExtra(ARG_CHILDREN_DATA, children)
            }
        }
    }

    private var gMap: GoogleMap? = null
    private var defaultDistance: Double = 3000.0
    private var MIN_DISTANCE: Double = 1000.0
    private var MAX_DISTANCE: Double = 10000.0
    private lateinit var centerLocation: LatLng
    private lateinit var routeData: RouteStopAlertDialogPojo
    private lateinit var children: ArrayList<ChildDetails>
     protected var loggedInUserId :Long = 0
    private var routeStopDetailsChild: ChildRouteDetails = ChildRouteDetails(0, 0, 0)
    private lateinit var binding:ActivityRouteAlertRadiusSelectorBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRouteAlertRadiusSelectorBinding.inflate(layoutInflater)
        setContentView(binding.root)





        binding.distanceButton.setOnClickListener {
            if (binding.kmOptionsCard.visibility != View.VISIBLE)
                binding.kmOptionsCard.createCircularReveal()
        }

        binding.twoKm.setOnClickListener {
            binding.kmOptionsCard.visibility = View.INVISIBLE
            centerMap(2000.0)
        }

        binding.threeKm.setOnClickListener {
            binding.kmOptionsCard.visibility = View.INVISIBLE
            centerMap(3000.0)
        }

        binding.fiveKm.setOnClickListener {
            binding.kmOptionsCard.visibility = View.INVISIBLE
            centerMap(5000.0)
        }

        binding.frameOverlay.setOnTouchListener { _, _ ->
            if (binding.kmOptionsCard.visibility == View.VISIBLE) {
                binding.kmOptionsCard.visibility = View.INVISIBLE
            }
            return@setOnTouchListener false
        }

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment

        mapFragment.getMapAsync(this)

        with(intent) {
            defaultDistance = getDoubleExtra(ARG_DISTANCE, 3000.0)
            val locationLat = getDoubleExtra(ARG_LOCATION_LATITUDE, 0.0)
            val locationLong = getDoubleExtra(ARG_LOCATION_LONGITUDE, 0.0)
            centerLocation = LatLng(locationLat, locationLong)
            routeData = extras!!.get(ARG_ROUTE_DATA) as RouteStopAlertDialogPojo
            children = extras!!.get(ARG_CHILDREN_DATA) as ArrayList<ChildDetails>
        }

        binding.btnSave.setOnClickListener {

            var distance = calculateCenterDistance()
            var postDataArray = ArrayList<ChildRouteDetails>()
            if (!routeData.isOutRoute) {
                for (i in 0 until routeData.childrenIds.size) {
                    routeStopDetailsChild = ChildRouteDetails(routeData.childrenIds[i], inRouteAlertDistance = distance.toLong(), outRouteAlertDistance = null)
                    postDataArray.add(routeStopDetailsChild)
                    dataSubmitMethod(postDataArray)
                }

            } else if (routeData.isOutRoute) {

                for (i in 0 until routeData.childrenIds.size) {
                    routeStopDetailsChild = ChildRouteDetails(routeData.childrenIds[i], inRouteAlertDistance = null, outRouteAlertDistance = distance.toLong())
                    postDataArray.add(routeStopDetailsChild)
                    dataSubmitMethod(postDataArray)
                }
            }
        }

        binding.btnCancel.setOnClickListener {
            finish()
        }
    }

    override fun onMapReady(map: GoogleMap?) {
        gMap = map

        gMap?.run {
            addMarker(MarkerOptions().position(centerLocation).title("Bus Stop"))

            uiSettings.isMapToolbarEnabled = false

            uiSettings.isScrollGesturesEnabled = false

            setOnCameraMoveListener(::onCameraChange)

            setOnCameraIdleListener(::onCameraIdle)

            binding.circleOverlay.post {
                binding.circleOverlay.createCircularReveal(200, 600)
                binding.circleOverlay.postDelayed({
                    centerMap(defaultDistance, 1)
                }, 1000)
            }
        }
    }

    private fun dataSubmitMethod(postDataArray: ArrayList<ChildRouteDetails>) {
        var childRouteAlertTask = ChildRouteAlertTask(RetrofitClient.getClient(application, authorisation = UserPreferenceManager.forUser(loggedInUserId,application).authorisation!!) )
        var check = 0
        launch {
            for (i in 0..postDataArray.size - 1) {
                val result = childRouteAlertTask.postAlertRequest(postDataArray[i])
                if (result) {
                     check++
                } else {
                    Toast.makeText(this@RouteAlertRadiusSelectorActivity, "Server Error ..", Toast.LENGTH_SHORT).show()
                }
            }
            if (check > 0) {
                finish()
            }
        }
    }

    private fun onCameraChange() {
        gMap?.run {
            showDistance()
        }
    }

    private fun onCameraIdle() {
        gMap?.run {
            if (cameraPosition.target.distanceTo(centerLocation) > 10) {
                animateCamera(CameraUpdateFactory.newLatLng(centerLocation), 400, null)
            } else {
                val distance = calculateCenterDistance()

                if (distance < MIN_DISTANCE) {
                    centerMap(MIN_DISTANCE)
                } else if (distance > MAX_DISTANCE) {
                    centerMap(MAX_DISTANCE)
                }
            }
            showDistance()
        }
    }

    fun LatLng.distanceTo(to: LatLng): Float {
        return Location("").apply {
            latitude = this@distanceTo.latitude
            longitude = this@distanceTo.longitude
        }.distanceTo(Location("").apply {
            latitude = to.latitude
            longitude = to.longitude
        })
    }
    private fun calculateCenterDistance(): Double {
        val circleXOffset = gMap!!.projection.fromScreenLocation(Point(binding.circleOverlay.radiusGap, binding.circleOverlay.height / 2))

        var distance = 0.toLong()
        var finalDistance = centerLocation.distanceTo(circleXOffset)
        if (finalDistance.toLong() % 100 != 0.toLong()) {
            distance = (finalDistance.toLong() / 100) * 100
        } else {
            distance = finalDistance.toLong()
        }
        return distance.toDouble()
    }

    private fun showDistance(distanceParam: Double? = null) {
        var distance = distanceParam ?: calculateCenterDistance()

        if (distance > MAX_DISTANCE) {
            distance = MAX_DISTANCE
        } else if (distance < MIN_DISTANCE) {
            distance = MIN_DISTANCE
        }


        binding.distanceButton.text = String.format("DISTANCE: %.1f KM", (distance) / 1000F)
    }

    private fun centerMap(distance: Double, animateDuration: Int = 400) {
        gMap?.run {
            val latLngBounds = LatLngBounds.Builder().include(generateLatLongFromHeadingDistance(distance, 0.0))
                .include(generateLatLongFromHeadingDistance(distance, 90.0))
                .include(generateLatLongFromHeadingDistance(distance, 180.0))
                .include(generateLatLongFromHeadingDistance(distance, 270.0)).build()

            setPadding(binding.circleOverlay.radiusGap, binding.circleOverlay.radiusGap, binding.circleOverlay.radiusGap,binding. circleOverlay.radiusGap)

            val cameraUpdate = CameraUpdateFactory.newLatLngBounds(latLngBounds, 0)
            animateCamera(cameraUpdate, animateDuration, null)
        }
    }

    private fun generateLatLongFromHeadingDistance(distanceParam: Double, headingParam: Double): LatLng {
        var distance = distanceParam
        distance /= EARTH_RADIUS
        val heading = Math.toRadians(headingParam)
        val fromLat = Math.toRadians(centerLocation.latitude)
        val fromLng = Math.toRadians(centerLocation.longitude)
        val cosDistance = Math.cos(distance)
        val sinDistance = Math.sin(distance)
        val sinFromLat = Math.sin(fromLat)
        val cosFromLat = Math.cos(fromLat)
        val sinLat = cosDistance * sinFromLat + sinDistance * cosFromLat * Math.cos(heading)
        val dLng = Math.atan2(
            sinDistance * cosFromLat * Math.sin(heading),
            cosDistance - sinFromLat * sinLat)
        return LatLng(Math.toDegrees(Math.asin(sinLat)), Math.toDegrees(fromLng + dLng))
    }

}

fun View.createCircularReveal(startDelay: Long? = null, duration: Long? = null) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
        this.visibility = View.VISIBLE
        return
    }

    val myView = this

    myView.post {
        val cx = myView.width / 2
        val cy = myView.height / 2

        val finalRadius = Math.hypot(cx.toDouble(), cy.toDouble()).toFloat()

        val anim = ViewAnimationUtils.createCircularReveal(myView, cx, cy, 0f, finalRadius)

        if (startDelay != null) {
            anim.startDelay = startDelay
        }

        if (duration != null) {
            anim.duration = duration
        }

        anim.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(p0: Animator) {
                myView.visibility = View.VISIBLE
            }
        })

        anim.start()
    }
}

interface ChildRouteAlert{
    suspend fun postAlertRequest(childRouteDetails: ChildRouteDetails) : Boolean
}
class ChildRouteAlertTask( val retrofit : Retrofit) : ChildRouteAlert {
    val alertService = retrofit.create(ChildRouteAlertService::class.java)

    override suspend fun postAlertRequest(childRouteDetails: ChildRouteDetails): Boolean {
        val result = alertService.postAlertRequest(childRouteDetails).awaitResultNullable()
        var success = when(result){
            is NullableResult.Ok -> {
                true
            }
            else ->{
                false
            }
        }
        return success
    }
}


interface ChildRouteAlertService {

    @PUT("child/route")
    fun postAlertRequest(@Body alertResponse : ChildRouteDetails) : Call<Any>
}

data class ChildRouteDetails (val childId : Long,
                              val inRouteAlertDistance : Long?,
                              val outRouteAlertDistance : Long?)