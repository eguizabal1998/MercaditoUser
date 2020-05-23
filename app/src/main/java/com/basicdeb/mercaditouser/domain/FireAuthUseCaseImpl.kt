package com.basicdeb.mercaditouser.domain

import com.basicdeb.mercaditouser.data.FireAuthRepo
import com.basicdeb.mercaditouser.domain.FireAuthUseCase
import com.basicdeb.mercaditouser.objects.User
import com.basicdeb.mercaditouser.vo.Resource

class FireAuthUseCaseImpl(private val fireAuthRepo: FireAuthRepo): FireAuthUseCase {

    override suspend fun register(email: String, password: String): Resource<Boolean> = fireAuthRepo.register(email,password)

    override suspend fun login(email: String, password: String): Resource<User> = fireAuthRepo.login(email,password)

    override suspend fun recovery(email: String): Resource<Boolean> = fireAuthRepo.recovery(email)

    override fun currentUser(): Boolean = fireAuthRepo.currentUser()

    override fun closeSesion(): Resource<String> = fireAuthRepo.closeSesion()

    override suspend fun googleRegister(): Resource<String> = fireAuthRepo.googleRegister()

    override fun facebookRegister(): Resource<String> = fireAuthRepo.facebookRegister()

    override suspend fun getFase(): Resource<Int> = fireAuthRepo.getFase()
}