package com.basicdeb.mercaditouser.viewmodel.cerrar

import androidx.lifecycle.ViewModel
import com.basicdeb.mercaditouser.domain.FireAuthUseCase

class CerrarViewModel(private val fireAuthUseCase: FireAuthUseCase) : ViewModel() {

    fun cerrarSesion(){
        fireAuthUseCase.closeSesion()
    }
}