package com.basicdeb.mercaditouser.ui.misordenes

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.basicdeb.mercaditouser.R
import com.basicdeb.mercaditouser.viewmodel.misordenes.MisOrdenesViewModel

class MisOrdenes : Fragment() {

    companion object {
        fun newInstance() = MisOrdenes()
    }

    private lateinit var viewModel: MisOrdenesViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.mis_ordenes_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(MisOrdenesViewModel::class.java)
        // TODO: Use the ViewModel
    }

}
