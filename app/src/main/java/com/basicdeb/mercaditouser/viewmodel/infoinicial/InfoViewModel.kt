package com.basicdeb.mercaditouser.viewmodel.infoinicial

import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.basicdeb.mercaditouser.domain.FiredbUseCase
import com.basicdeb.mercaditouser.objects.userPerfil
import com.basicdeb.mercaditouser.vo.Resource
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.launch

class InfoViewModel(private val firedbUseCase: FiredbUseCase) : ViewModel() {

    val nombre = ObservableField<String>()
    val telefono = ObservableField<String>()
    val direccion1 = ObservableField<String>()
    val direccion2 = ObservableField<String>()
    lateinit var coordenadas: LatLng

    val eventoGuardar:MutableLiveData<Resource<Boolean>> by lazy {
        MutableLiveData<Resource<Boolean>>()
    }

    fun checkCampos(){
        eventoGuardar.value = Resource.Loading()
        if (nombre.get().isNullOrEmpty() || telefono.get().isNullOrEmpty() || direccion1.get()
                .isNullOrEmpty() || direccion2.get().isNullOrEmpty()
        ) {
            eventoGuardar.value = Resource.Failure(Exception("Complete los campos"))
        }else{
            guardarPerfil()
        }
    }

    private fun guardarPerfil() {
        val perfil = userPerfil(nombre.get(),telefono.get(),direccion1.get(),direccion2.get(),coordenadas)
        viewModelScope.launch {
            try {
                eventoGuardar.value = firedbUseCase.savePerfil(perfil)
            }catch (e:Exception){
                eventoGuardar.value = Resource.Failure(e)
            }
        }
    }
}
