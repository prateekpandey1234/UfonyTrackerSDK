package com.ufony.trackersdk

import com.google.android.gms.maps.model.LatLng

typealias StateChangedCallback = ((TrackerLocationListenerState) -> Unit)

typealias TrackerUpdateCallback = (TrackerDeviceLocationUpdateResponse) -> Unit
typealias RouteUpdateCallback = (TripUpdateResponse) -> Unit
typealias UpdateCallback = (MapData) -> Unit
typealias ConnectionIdCallback = (String) -> Unit

interface ITrackerLocationListener {
    var onStateChangedCallback: StateChangedCallback?
    var onConnectionIdChangedCallback: ConnectionIdCallback?
    var onTrackerUpdateCallback: TrackerUpdateCallback?
    var onRouteUpdateCallback: RouteUpdateCallback?
    var onUpdateCallback: UpdateCallback?
    var state: TrackerLocationListenerState

    fun start();
    fun stop();
}

enum class TrackerLocationListenerState {
    Connecting,
    Connected,
    Reconnecting,
    Disconnected;

    companion object {
        fun fromInt(value: Int) = values().first { it.ordinal == value }
    }
}

class TrackerDeviceLocationUpdateResponse(val trackerDeviceId: Long, override val title: String = "", var routeId: Long? = null, var tripId: Long? = null, override var location: CoordinateResponse?, var routeName: String = "") : MapData {
    override val uniqueId: Long
        get() = this.trackerDeviceId

    override val markerType: GoogleMapMarkerType
        get() = GoogleMapMarkerType.Device

    override val isActive: Boolean
        get() = routeId != null
}

interface MapData {
    val uniqueId: Long

    val title: String

    val markerType: GoogleMapMarkerType

    var location: CoordinateResponse?

    val isActive: Boolean
}

enum class GoogleMapMarkerType{
    Device,
    Driver
}

data class CoordinateResponse(val latitude: Double, val longitude: Double){
    val latLng
        get() = LatLng(latitude, longitude)
}

data class TripUpdateResponse(val id: Long, val routeId: Long, override var location: CoordinateResponse?) : MapData {
    override val uniqueId: Long
        get() = this.routeId

    override val title: String
        get() = ""

    override val markerType: GoogleMapMarkerType
        get() = GoogleMapMarkerType.Driver

    override val isActive: Boolean
        get() = true
}