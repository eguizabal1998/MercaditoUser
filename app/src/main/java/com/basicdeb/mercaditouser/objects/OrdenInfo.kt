package com.basicdeb.mercaditouser.objects

import com.google.android.gms.maps.model.LatLng

data class OrdenInfo (val direccion1:String,
                      val direccion2:String,
                      val direccion3:String,
                      val coordenadas:LatLng,
                      val numero:String,
                      val total:Double)