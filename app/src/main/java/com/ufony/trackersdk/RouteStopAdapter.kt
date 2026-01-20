package com.ufony.trackersdk

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.io.Serializable

class
RouteStopAlertDialogAdapter(var context: Context, var dialogData: ArrayList<RouteStopAlertDialogPojo>, var children: ArrayList<Child>) : RecyclerView.Adapter<RouteStopAlertDialogAdapter.ContentViewHolder>() {
    lateinit var intent: Intent
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.activity_route_alert_dialog_adapter, parent, false)
        return ContentViewHolder(view)
    }

    override fun getItemCount(): Int {
        return dialogData.size
    }

    override fun onBindViewHolder(holder: ContentViewHolder, position: Int) {
        val routesData = dialogData.get(position)

        holder.routeName.setText(routesData.routeName + " - " + routesData.stopName)
        var childrenNamesFromIds= ArrayList<String>()
        for(i in 0 until routesData.childrenIds.size)
            children.firstOrNull { it.id == routesData.childrenIds[i] }?.let { childrenNamesFromIds.add(it.fullName) }
        var childrenNames = ""
        for (i in 0 until childrenNamesFromIds.size) {
            if (!routesData.isOutRoute)
                childrenNames += childrenNamesFromIds[i] + " (In Route)" + "\n"
            else
                childrenNames += childrenNamesFromIds[i] + " (Out Route)" + "\n"
        }

        holder.childrenName.setText(childrenNames)

        holder.alertRadius.setText("" + routesData.alertRadius + " Km")
        if (dialogData.size == position)
            holder.divider.visibility = View.GONE

        var childDetails = ArrayList<ChildDetails>()
        for (i in 0..children.size - 1)
            childDetails.add(ChildDetails(children[i].id, children[i].fullName))

        holder.editBtn.setOnClickListener {

            if (!routesData.isOutRoute) {
                intent = RouteAlertRadiusSelectorActivity.createIntent(context,
                    routesData.inRouteAlertDistance.toDouble(),
                    routesData.stopLocation.latitude, routesData.stopLocation.longitude, routesData, childDetails)
            } else if (routesData.isOutRoute) {
                intent = RouteAlertRadiusSelectorActivity.createIntent(context,
                    routesData.outRouteAlertDistance.toDouble(),
                    routesData.stopLocation.latitude,
                    routesData.stopLocation.longitude,
                    routesData,
                    childDetails)
            }
            context.startActivity(intent
                .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP))
        }
    }

    inner class ContentViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val routeName = view.findViewById<TextView>(R.id.routeName)
        val childrenName = view.findViewById<TextView>(R.id.childrenName)
        val alertRadius = view.findViewById<TextView>(R.id.alertRadius)
        val editBtn = view.findViewById<TextView>(R.id.btnEdit)
        val divider = view.findViewById<View>(R.id.divider)
    }
}

data class ChildDetails(var childId: Long, var childName: String) : Serializable