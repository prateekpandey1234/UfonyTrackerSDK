package com.ufony.trackersdk

import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.microsoft.signalr.HubConnection
import com.microsoft.signalr.HubConnectionBuilder
import com.microsoft.signalr.HubConnectionState
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.Timer
import java.util.TimerTask

class TrackerLocationListenerV1(val lifecycle: Lifecycle, private val signalRRootUrl: String?) : LifecycleObserver, ITrackerLocationListener {

    private var mHubConnection: HubConnection? = null
    // NOTE: mHubProxy is REMOVED. It does not exist in modern SignalR.

    override var onStateChangedCallback: StateChangedCallback? = null
    override var onConnectionIdChangedCallback: ConnectionIdCallback? = null
    override var onTrackerUpdateCallback: TrackerUpdateCallback? = null
    override var onRouteUpdateCallback: RouteUpdateCallback? = null
    override var onUpdateCallback: UpdateCallback? = null

    // We need to map Modern State to your Custom State Enum manually
    override var state: TrackerLocationListenerState = TrackerLocationListenerState.Disconnected
        set(value) {
            field = value
            onStateChangedCallback?.invoke(field)
        }

    var retiresCount = 0

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    override fun start() {
        retiresCount = 0
        if (mHubConnection != null) {
            updateInternalState(mHubConnection!!.connectionState)
        }
        GlobalScope.launch { startSignalR() }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    override fun stop() {
        GlobalScope.launch { stopSignalR() }
    }

    fun startSignalR() {
        if (mHubConnection?.connectionState == HubConnectionState.CONNECTED)
            return

        // Modern SignalR usually connects to /tripHub directly, not /signalr
        var rootUrl = signalRRootUrl
        if (!rootUrl.isNullOrBlank() && !rootUrl.endsWith("/")) {
            rootUrl += "/"
        }

        // CHECK THIS URL: Modern servers usually look like "https://base.url/tripHub"
        val serverUrl = "${rootUrl ?: ""}tripHub"
        Log.d("hub","SignalR url: $serverUrl")

        // 1. Build Connection (Modern Way)
        mHubConnection = HubConnectionBuilder.create(serverUrl).build()

        // 2. Handle State Changes (Modern Way: No explicit state change listener, we use closed callback)
        mHubConnection!!.onClosed {
            updateInternalState(HubConnectionState.DISCONNECTED)
            handleDisconnectRetry()
        }

        // 3. Register Callbacks (Directly on Connection, No Proxy)
        mHubConnection!!.on("updateDeviceLocation", { response ->
            onTrackerUpdateCallback?.invoke(response)
            onUpdateCallback?.invoke(response)
        }, TrackerDeviceLocationUpdateResponse::class.java)

        mHubConnection!!.on("updateLocation", { response ->
            onRouteUpdateCallback?.invoke(response)
            onUpdateCallback?.invoke(response)
        }, TripUpdateResponse::class.java)

        // 4. Start Connection
        try {
            mHubConnection!!.start().blockingAwait() // Blocking for simplicity in this thread
            updateInternalState(HubConnectionState.CONNECTED)
            onConnectionIdChangedCallback?.invoke(mHubConnection!!.connectionId)
            retiresCount = 0
        } catch (exception: Exception) {
            exception.printStackTrace()
            updateInternalState(HubConnectionState.DISCONNECTED)
        }
    }

    private fun updateInternalState(hubState: HubConnectionState) {
        // Map HubConnectionState to your Int/Enum
        val stateInt = when(hubState) {
            HubConnectionState.CONNECTED -> 1 // Assuming 1 is Connected
            HubConnectionState.DISCONNECTED -> 0 // Assuming 0 is Disconnected
            else -> 2 // Connecting/Reconnecting
        }
        state = TrackerLocationListenerState.fromInt(stateInt)
    }

    private fun handleDisconnectRetry() {
        if(lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED) && retiresCount < 1){
            Timer().schedule(object : TimerTask() {
                override fun run() {
                    startSignalR()
                }
            }, 2000)
            ++retiresCount
        }
    }

    fun stopSignalR() {
        mHubConnection?.stop()
        mHubConnection = null
    }
}