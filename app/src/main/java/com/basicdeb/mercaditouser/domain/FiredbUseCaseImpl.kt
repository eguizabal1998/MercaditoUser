package com.basicdeb.mercadito.domain

import com.basicdeb.mercaditouser.data.FiredbRepo
import com.basicdeb.mercaditouser.domain.FiredbUseCase
import com.basicdeb.mercaditouser.objects.*
import com.basicdeb.mercaditouser.vo.Resource
import com.google.firebase.storage.StorageReference

class FiredbUseCaseImpl(private val firedbRepo: FiredbRepo): FiredbUseCase {


    override suspend fun getProductos(Uidn:String,departamento:String): Resource<MutableList<Producto>> = firedbRepo.getProductos(Uidn,departamento)

    override suspend fun getNegocios(departamento:String): Resource<MutableList<Negocio>> = firedbRepo.getNegocios(departamento)

    override suspend fun getPortada(Uidn: String): Resource<StorageReference> = firedbRepo.getPortada(Uidn)

    override fun getImagen(URL: String): StorageReference = firedbRepo.getImagen(URL)

    override suspend fun saveOrder(
        UidNegocio: String,
        UidUser: String,
        lista: MutableList<Orden>,
        orden: OrdenInfo
    ): Resource<Boolean> = firedbRepo.saveOrder(UidNegocio,UidUser,lista,orden)

    override suspend fun savePerfil(userPerfil: userPerfil): Resource<Boolean> = firedbRepo.savePerfil(userPerfil)
}