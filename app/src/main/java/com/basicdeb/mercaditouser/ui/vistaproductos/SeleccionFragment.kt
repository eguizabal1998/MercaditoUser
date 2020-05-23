package com.basicdeb.mercaditouser.ui.vistaproductos

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.basicdeb.mercaditouser.R
import com.basicdeb.mercaditouser.databinding.SeleccionFragmentBinding
import com.basicdeb.mercaditouser.viewmodel.vistaproductos.SeleccionViewModel

class SeleccionFragment : Fragment() {

    private lateinit var viewModel: SeleccionViewModel
    private lateinit var binding: SeleccionFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater,R.layout.seleccion_fragment,container,false)
        viewModel = ViewModelProvider(this).get(SeleccionViewModel::class.java)

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // TODO: Use the ViewModel
    }

}
