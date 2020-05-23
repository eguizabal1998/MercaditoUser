package com.basicdeb.mercaditouser.ui.login

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.basicdeb.mercaditouser.viewmodel.login.RegisterViewModel
import com.basicdeb.mercaditouser.R
import com.basicdeb.mercaditouser.data.FireAuthRepoImpl
import com.basicdeb.mercaditouser.databinding.RegisterFragmentBinding
import com.basicdeb.mercaditouser.domain.FireAuthUseCaseImpl
import com.basicdeb.mercaditouser.ui.menu.MenuActivity
import com.basicdeb.mercaditouser.viewmodel.login.AuthFactory
import com.basicdeb.mercaditouser.vo.Resource
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.IdpResponse

import com.google.android.material.snackbar.Snackbar

class RegisterFragment : Fragment() {

    companion object{
        private const val RC_SIGN_IN = 123
    }

    private lateinit var viewModel: RegisterViewModel
    private lateinit var binding: RegisterFragmentBinding
    private lateinit var factory: AuthFactory

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.register_fragment,container,false)
        factory = AuthFactory(
            FireAuthUseCaseImpl(FireAuthRepoImpl())
        )
        viewModel = ViewModelProvider(this,factory).get(RegisterViewModel::class.java)

        binding.viewModel = viewModel

        binding.btnRegisterYatengocuenta.setOnClickListener {
            this.findNavController().navigate(RegisterFragmentDirections.actionRegisterFragmentToLoginFragment())
        }

        binding.btnRegisterBack.setOnClickListener {
            this.findNavController().navigate(RegisterFragmentDirections.actionRegisterFragmentToLoginFragment())
        }

        binding.btnRegisterGoogle.setOnClickListener {
            loginGoogle()
        }

        observers()

        return binding.root
    }

    private fun loginGoogle() {
        val providers = arrayListOf(
            AuthUI.IdpConfig.GoogleBuilder().build()
        )

        startActivityForResult(
            AuthUI.getInstance().createSignInIntentBuilder()
            .setAvailableProviders(providers).build(), RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val response = IdpResponse.fromResultIntent(data)

            if(resultCode == RESULT_OK){
                if (response!!.isNewUser){
                    viewModel.google()
                }else{
                    val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE) ?: return
                    val fase = sharedPref.getInt("Fase",0)

                    if (fase == 1){
                        //this.findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToMenuFragment())
                        val intent  = Intent(this.context, MenuActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        startActivity(intent)
                        this.activity?.finish()
                    }else if(fase == 0){
                        this.findNavController().navigate(RegisterFragmentDirections.actionRegisterFragmentToInfoFragment())
                    }
                }
            }else {
                // Sign in failed
                if (response == null) {
                    // User pressed back button
                    Snackbar.make(this.requireView(),"Cancelado",Snackbar.LENGTH_SHORT).show()
                    return
                }

                if (response.error!!.errorCode == ErrorCodes.NO_NETWORK) {
                    Snackbar.make(this.requireView(),"Sin Conexion",Snackbar.LENGTH_SHORT).show()
                    return
                }
                Snackbar.make(this.requireView(),"Error",Snackbar.LENGTH_SHORT).show()
                Log.e("GoogleSingIn", "Sign-in error: ", response.getError())
            }
        }
    }

    fun observers(){
        viewModel.eventoRegistro.observe(this.viewLifecycleOwner, Observer {result ->
            when(result) {
                is Resource.Loading ->{
                    val inputMethodManager = this.context?.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
                    inputMethodManager.hideSoftInputFromWindow(this.view!!.windowToken, 0)
                    binding.cirRegisterButtonRegister.visibility = View.VISIBLE
                    binding.btnRegisterRegistro.visibility = View.GONE
                }
                is Resource.Success ->{
                    binding.cirRegisterButtonRegister.visibility = View.GONE
                    binding.btnRegisterRegistro.visibility = View.VISIBLE
                    Snackbar.make(this.view!!,"Se ha enviado un correo para verificacion", Snackbar.LENGTH_LONG).show()
                }
                is Resource.Failure ->{
                    binding.cirRegisterButtonRegister.visibility = View.GONE
                    binding.btnRegisterRegistro.visibility = View.VISIBLE
                    Snackbar.make(this.view!!, result.exception.message.toString(), Snackbar.LENGTH_LONG).show()
                }
            }
        })

        viewModel.eventoGoogle.observe(this.viewLifecycleOwner, Observer {result ->
            when(result) {
                is Resource.Loading ->{
                    binding.cirRegisterButtonRegister.visibility = View.VISIBLE
                }
                is Resource.Success ->{
                    binding.cirRegisterButtonRegister.visibility = View.GONE
                    Snackbar.make(this.view!!,result.data.toString(), Snackbar.LENGTH_LONG).show()
                    val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE) ?: return@Observer
                    val fase = sharedPref.getInt("Fase",0)

                    if (fase == 1){
                        //this.findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToMenuFragment())
                        val intent  = Intent(this.context, MenuActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        startActivity(intent)
                        this.activity?.finish()
                    }else if(fase == 0){
                        this.findNavController().navigate(RegisterFragmentDirections.actionRegisterFragmentToInfoFragment())
                    }
                }
                is Resource.Failure ->{
                    binding.cirRegisterButtonRegister.visibility = View.GONE
                    Snackbar.make(this.view!!, result.exception.message.toString(), Snackbar.LENGTH_LONG).show()
                }
            }
        })

        viewModel.eventoFacebook.observe(this.viewLifecycleOwner, Observer {result ->
            when(result) {
                is Resource.Loading ->{
                    binding.cirRegisterButtonRegister.visibility = View.VISIBLE
                }
                is Resource.Success ->{
                    binding.cirRegisterButtonRegister.visibility = View.GONE
                    Snackbar.make(this.view!!,result.data.toString(), Snackbar.LENGTH_LONG).show()
                }
                is Resource.Failure ->{
                    binding.cirRegisterButtonRegister.visibility = View.GONE
                    Snackbar.make(this.view!!, result.exception.message.toString(), Snackbar.LENGTH_LONG).show()
                }
            }
        })
    }


}
