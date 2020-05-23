package com.basicdeb.mercaditouser.viewmodel.login

import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.basicdeb.mercaditouser.domain.FireAuthUseCase
import com.basicdeb.mercaditouser.vo.Resource
import kotlinx.coroutines.launch

class RecoveryViewModel(private val fireAuthUseCase: FireAuthUseCase) : ViewModel() {

    val correo = ObservableField<String>()

    val eventoRecovery: MutableLiveData<Resource<Any>> by lazy {
        MutableLiveData<Resource<Any>>()
    }

    fun checkField(){
        eventoRecovery.value = Resource.Loading()
        if(correo.get().isNullOrEmpty()){
            eventoRecovery.value = Resource.Failure(Exception("Complete los campos"))
        }else{
            recuperar()
        }
    }

    private fun recuperar() {
        try {
            viewModelScope.launch {
                eventoRecovery.value = fireAuthUseCase.recovery(correo.get().toString())
                clean()
            }
        }catch (e:Exception){
            eventoRecovery.value = Resource.Failure(e)
            clean()
        }
    }

    private fun clean() {
        correo.set("")
    }
}