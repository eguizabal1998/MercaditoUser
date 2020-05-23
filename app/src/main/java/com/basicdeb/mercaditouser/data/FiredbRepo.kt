package com.basicdeb.mercaditouser.data

import com.basicdeb.mercaditouser.objects.*
import com.basicdeb.mercaditouser.vo.Resource
import com.google.firebase.storage.StorageReference

interface FiredbRepo {

    suspend fun getProductos(Uidn:String,departamento:String):Resource<MutableList<Producto>>

    suspend fun getNegocios(departamento:String):Resource<MutableList<Negocio>>

    suspend fun getPortada(Uidn:String): Resource<StorageReference>

    fun getImagen(URL: String): StorageReference

    suspend fun saveOrder(
        UidNegocio: String,
        UidUser: String,
        lista: MutableList<Orden>,
        orden: OrdenInfo
    ): Resource<Boolean>

    suspend fun savePerfil(userPerfil: userPerfil): Resource<Boolean>

}