package com.basicdeb.mercaditouser.viewmodel.listado

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.basicdeb.mercaditouser.domain.FiredbUseCase
import com.basicdeb.mercaditouser.objects.Negocio
import com.basicdeb.mercaditouser.objects.Producto
import com.basicdeb.mercaditouser.vo.Resource
import kotlinx.coroutines.launch

class ListadoViewModel(private val firedbUseCase: FiredbUseCase) : ViewModel() {

    val lista = mutableListOf<Negocio>()

    lateinit var departamento:String

    val eventoGetNegocios:MutableLiveData<Resource<Any>> by lazy {
        MutableLiveData<Resource<Any>>()
    }

    val eventoPortada:MutableLiveData<Resource<Any>> by lazy {
        MutableLiveData<Resource<Any>>()
    }


    fun getNegocios(){
        lista.clear()
        eventoGetNegocios.value = Resource.Loading()
        viewModelScope.launch {
            try {
                eventoGetNegocios.value = firedbUseCase.getNegocios(departamento)
            }catch (e:Exception){
                eventoGetNegocios.value = Resource.Failure(e)
            }
        }
    }

    fun getPortada(negocio: Negocio) : String{
        var uri:String = ""
        viewModelScope.launch {
            try {
                //uri = firedbUseCase.getPortada(Uid)
                Log.i("portas",uri)
            }catch (e:Exception){
                eventoGetNegocios.value = Resource.Failure(e)
            }
        }
        return uri
    }

}
