package com.ufony.trackersdk

import java.io.Serializable


data  class RouteStop (var id : Long,
                       var name : String,
                       var stopLocation : Coordinate,
                       var stopIndex : Long
) : Serializable


data class RouteStopAlertDialogPojo(var routeName : String,
                                    var isOutRoute : Boolean,
                                    var stopName : String,
                                    var childrenIds : ArrayList<Long>,
                                    var alertRadius : String,
                                    val inRouteAlertDistance : Long,
                                    val outRouteAlertDistance : Long,
                                    val stopLocation : Coordinate) :Serializable