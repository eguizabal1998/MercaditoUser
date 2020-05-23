package com.basicdeb.mercaditouser

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.basicdeb.mercaditouser.domain.FiredbUseCase

class DBFactory(private val firedbUseCase: FiredbUseCase): ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(FiredbUseCase::class.java).newInstance(firedbUseCase)
    }

}