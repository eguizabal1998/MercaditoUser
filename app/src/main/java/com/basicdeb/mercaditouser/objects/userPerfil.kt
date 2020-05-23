package com.basicdeb.mercaditouser.objects

import com.google.android.gms.maps.model.LatLng


data class userPerfil (val nombre:String? = "",
val telefono:String? = "",
val direccion1:String? = "",
val direccion2:String? = "",
val coordenadas: LatLng?= LatLng(0.0,0.0))