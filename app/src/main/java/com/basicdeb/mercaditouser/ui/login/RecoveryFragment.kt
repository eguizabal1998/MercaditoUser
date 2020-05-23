package com.basicdeb.mercaditouser.ui.login

import android.app.Activity
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.basicdeb.mercaditouser.R
import com.basicdeb.mercaditouser.data.FireAuthRepoImpl
import com.basicdeb.mercaditouser.databinding.RecoveryFragmentBinding
import com.basicdeb.mercaditouser.domain.FireAuthUseCaseImpl
import com.basicdeb.mercaditouser.viewmodel.login.AuthFactory
import com.basicdeb.mercaditouser.viewmodel.login.RecoveryViewModel
import com.basicdeb.mercaditouser.vo.Resource

import com.google.android.material.snackbar.Snackbar

class RecoveryFragment : Fragment() {

    private lateinit var viewModel: RecoveryViewModel
    private lateinit var binding: RecoveryFragmentBinding
    private lateinit var factory: AuthFactory

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.recovery_fragment,container,false)
        factory = AuthFactory(
            FireAuthUseCaseImpl(FireAuthRepoImpl())
        )
        viewModel = ViewModelProvider(this,factory).get(RecoveryViewModel::class.java)

        binding.viewModel = viewModel

        observers()

        listeners()

        return binding.root
    }

    private fun listeners() {
        binding.btnRecoveryBack.setOnClickListener {
            this.findNavController().navigate(RecoveryFragmentDirections.actionRecoveryFragmentToLoginFragment())
        }
    }

    private fun observers() {
        viewModel.eventoRecovery.observe(this.viewLifecycleOwner, Observer {
            when(it){
                is Resource.Loading ->{
                    val inputMethodManager = this.context?.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
                    inputMethodManager.hideSoftInputFromWindow(this.view!!.windowToken, 0)
                    binding.cirRegisterButtonRecovery.visibility = View.VISIBLE
                    binding.btnRecoveryRecuperar.isEnabled = false

                }
                is Resource.Success ->{
                    binding.cirRegisterButtonRecovery.visibility = View.GONE
                    binding.btnRecoveryRecuperar.isEnabled = true
                    Snackbar.make(this.view!!, "Correo enviado", Snackbar.LENGTH_LONG).show()
                }
                is Resource.Failure ->{
                    binding.cirRegisterButtonRecovery.visibility = View.GONE
                    binding.btnRecoveryRecuperar.isEnabled = true

                }
            }
        })
    }

}