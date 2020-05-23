package com.basicdeb.mercaditouser.ui.listado

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.basicdeb.mercadito.domain.FiredbUseCaseImpl
import com.basicdeb.mercaditouser.DBFactory

import com.basicdeb.mercaditouser.R
import com.basicdeb.mercaditouser.data.FiredbRepoImpl
import com.basicdeb.mercaditouser.databinding.ListadoFragmentBinding
import com.basicdeb.mercaditouser.objects.Negocio
import com.basicdeb.mercaditouser.viewmodel.listado.ListadoViewModel
import com.basicdeb.mercaditouser.vo.Resource
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.negociorow.view.*
import kotlinx.android.synthetic.main.productorow.view.*

class ListadoFragment : Fragment(), NegocioAdapter.onItemClickListener {

    private lateinit var viewModel: ListadoViewModel
    private lateinit var binding: ListadoFragmentBinding
    private lateinit var adapter: NegocioAdapter



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val fatory = DBFactory(FiredbUseCaseImpl(FiredbRepoImpl()))
        binding = DataBindingUtil.inflate(inflater,R.layout.listado_fragment,container,false)
        viewModel = ViewModelProvider(this,fatory).get(ListadoViewModel::class.java)

        binding.viewModel = viewModel
        binding.scrollNegocios.isSmoothScrollingEnabled = true

        observers()
        listeners()

        return binding.root
    }

    private fun listeners() {
        binding.btnListadoGuardar.setOnClickListener {
            val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE) ?: return@setOnClickListener
            with (sharedPref.edit()) {
                Log.i("departamento",binding.spinerDepartamentos.selectedItem.toString())
                putString("departamento", binding.spinerDepartamentos.selectedItem.toString())
                commit()
            }
            binding.spinerDepartamentos.visibility = View.GONE
            binding.btnListadoGuardar.visibility = View.GONE
            binding.textViewDepartamento.text = binding.spinerDepartamentos.selectedItem.toString()

            viewModel.departamento = binding.textViewDepartamento.text.toString()

            viewModel.getNegocios()
        }

        binding.btnListadoCambiar.setOnClickListener {
            binding.spinerDepartamentos.visibility = View.VISIBLE
            binding.btnListadoGuardar.visibility = View.VISIBLE
        }
    }

    private fun observers() {

        viewModel.eventoGetNegocios.observe(this.viewLifecycleOwner, Observer {
            when(it){
                is Resource.Loading -> {
                    //Snackbar.make(this.view!!, "Cargando", Snackbar.LENGTH_LONG).show()
                    binding.shimmerViewContainer.startShimmer()
                }
                is Resource.Success -> {
                    binding.shimmerViewContainer.stopShimmer()
                    binding.shimmerViewContainer.visibility = View.GONE
                    viewModel.lista.clear()
                    viewModel.lista.addAll(it.data as Collection<Negocio>)
                    adapter.setListData(viewModel.lista)
                    adapter.notifyDataSetChanged()
                    binding.rvListado.smoothScrollToPosition(binding.rvListado.adapter!!.itemCount)

                }
                is Resource.Failure -> {
                    Snackbar.make(this.view!!, it.exception.message.toString(), Snackbar.LENGTH_LONG).show()
                }
            }
        })
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        adapter = NegocioAdapter( this.context!!, this,this)

        val RecyclerView = binding.rvListado

        RecyclerView.layoutManager = LinearLayoutManager(this.context)
        RecyclerView.adapter = adapter

        val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE) ?: return
        val departamento = sharedPref.getString("departamento","Santa Ana")
        binding.textViewDepartamento.text = departamento
        viewModel.departamento = departamento.toString()

        viewModel.getNegocios()
    }

    override fun onItemClick(item: Negocio, position: Int) {
        val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE) ?: return
        val departamento = sharedPref.getString("departamento","Santa Ana")

        this.findNavController().navigate(ListadoFragmentDirections.actionListadoFragmentToVistaProductosFragment(item.Uid,
            item.nombre,item.descripcion,item.numero,item.facebook,
            item.portada.toString(),departamento.toString()))
    }
}
