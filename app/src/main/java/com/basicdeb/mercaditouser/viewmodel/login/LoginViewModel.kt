package com.basicdeb.mercaditouser.viewmodel.login

import android.util.Log
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.basicdeb.mercaditouser.domain.FireAuthUseCase
import com.basicdeb.mercaditouser.vo.Resource
import kotlinx.coroutines.launch

class LoginViewModel(private val fireAuthUseCase: FireAuthUseCase) : ViewModel() {

    val correo = ObservableField<String>()
    val contra = ObservableField<String>()

    val eventoLogin: MutableLiveData<Resource<Any>> by lazy {
        MutableLiveData<Resource<Any>>()
    }

    val eventoGoogle: MutableLiveData<Resource<Any>> by lazy {
        MutableLiveData<Resource<Any>>()
    }

    val eventoFase: MutableLiveData<Resource<Int>> by lazy {
        MutableLiveData<Resource<Int>>()
    }

    fun getFase(){
        eventoFase.value = Resource.Loading()
        viewModelScope.launch {
            try {
                eventoFase.value = fireAuthUseCase.getFase()
                clean()
            }catch (e:Exception){
                eventoFase.value = Resource.Failure(e)
                clean()
            }
        }
    }

    fun checkField(){
        Log.i("login","check")
        eventoLogin.value = Resource.Loading()
        if(contra.get().isNullOrEmpty() || correo.get().isNullOrEmpty()){
            eventoLogin.value = Resource.Failure(Exception("Complete Los Campos"))
        }else{
            login()
        }
    }

    private fun login() {
        viewModelScope.launch {
            try {
                eventoLogin.value = fireAuthUseCase.login(correo.get().toString(),contra.get().toString())
                clean()
            }catch (e:Exception){
                eventoLogin.value = Resource.Failure(e)
                clean()
            }
        }
    }

    private fun clean(){
        contra.set("")
        correo.set("")
    }

    fun getUser():Boolean{
        return fireAuthUseCase.currentUser()
    }

    fun closeSesion():Resource<String>{
        return fireAuthUseCase.closeSesion()
    }

    fun google(){
        eventoGoogle.value = Resource.Loading()
        viewModelScope.launch {
            try {
                eventoGoogle.value = fireAuthUseCase.googleRegister()
            }catch (e:Exception){
                eventoGoogle.value = Resource.Failure(e)
            }
        }
    }
}

