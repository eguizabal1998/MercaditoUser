package com.basicdeb.mercaditouser.objects

import com.google.firebase.storage.StorageReference

data class Negocio(
    val nombre:String,
    val descripcion:String,
    val portada: StorageReference,
    val Uid: String,
    val numero:Int,
    val facebook:String)