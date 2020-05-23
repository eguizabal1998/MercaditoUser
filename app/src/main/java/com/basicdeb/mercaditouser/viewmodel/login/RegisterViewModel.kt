package com.basicdeb.mercaditouser.viewmodel.login

import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.basicdeb.mercaditouser.domain.FireAuthUseCase
import com.basicdeb.mercaditouser.vo.Resource
import kotlinx.coroutines.launch

class RegisterViewModel(private val fireAuthUseCase: FireAuthUseCase) : ViewModel() {

    val correo =  ObservableField<String>()
    val contra1 = ObservableField<String>()
    val contra2 = ObservableField<String>()

    val eventoRegistro: MutableLiveData<Resource<Any>> by lazy {
        MutableLiveData<Resource<Any>>()
    }

    val eventoGoogle: MutableLiveData<Resource<Any>> by lazy {
        MutableLiveData<Resource<Any>>()
    }

    val eventoFacebook: MutableLiveData<Resource<Any>> by lazy {
        MutableLiveData<Resource<Any>>()
    }


    fun checkFields(){
        eventoRegistro.value = Resource.Loading()
        if (correo.get().isNullOrEmpty() || contra1.get().isNullOrEmpty() || contra2.get().isNullOrEmpty()){
            eventoRegistro.value = Resource.Failure(Exception("Complete Los Campos"))
        }
        else if (contra1.get() != contra2.get()){
            eventoRegistro.value = Resource.Failure(Exception("Contraseñas no coinciden"))
        }else{
            registro()
        }

    }

    private fun registro() {
        viewModelScope.launch {
            try {
                fireAuthUseCase.register(correo.get().toString(), contra1.get().toString())
                eventoRegistro.value = Resource.Success(true)
                clean()
            }catch (e:Exception){
                eventoRegistro.value = Resource.Failure(e)
                clean()
            }
        }
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

    fun facebook(){
        eventoFacebook.value = Resource.Loading()
        try {
            eventoFacebook.value = fireAuthUseCase.facebookRegister()
        }catch (e:Exception){
            eventoFacebook.value = Resource.Failure(e)
        }
    }

    private fun clean(){
        correo.set("")
        contra1.set("")
        contra2.set("")
    }

}

