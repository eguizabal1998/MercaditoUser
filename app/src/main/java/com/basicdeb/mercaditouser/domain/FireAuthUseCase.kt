package com.basicdeb.mercaditouser.domain

import com.basicdeb.mercaditouser.objects.User
import com.basicdeb.mercaditouser.vo.Resource


interface FireAuthUseCase {

    suspend fun register(email: String, password: String): Resource<Boolean>

    suspend fun login(email: String, password: String): Resource<User>

    suspend fun recovery(email: String): Resource<Boolean>

    fun currentUser(): Boolean

    fun closeSesion(): Resource<String>

    suspend fun googleRegister(): Resource<String>

    fun facebookRegister(): Resource<String>

    suspend fun getFase():Resource<Int>
}