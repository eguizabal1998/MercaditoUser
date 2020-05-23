package com.basicdeb.mercaditouser.ui.login

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.basicdeb.mercaditouser.R
import com.basicdeb.mercaditouser.data.FireAuthRepoImpl
import com.basicdeb.mercaditouser.databinding.LoginFragmentBinding
import com.basicdeb.mercaditouser.domain.FireAuthUseCaseImpl
import com.basicdeb.mercaditouser.ui.menu.MenuActivity
import com.basicdeb.mercaditouser.viewmodel.login.AuthFactory
import com.basicdeb.mercaditouser.viewmodel.login.LoginViewModel
import com.basicdeb.mercaditouser.vo.Resource
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.IdpResponse
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings

class LoginFragment : Fragment() {

    companion object{
        private const val RC_SIGN_IN = 123
    }

    private lateinit var viewModel: LoginViewModel
    private lateinit var bindign: LoginFragmentBinding
    private lateinit var factory: AuthFactory

    private val firebaseFireStore: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    val settings = FirebaseFirestoreSettings.Builder()
        .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
        .build()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        bindign = DataBindingUtil.inflate(inflater, R.layout.login_fragment,container,false)
        factory = AuthFactory(
            FireAuthUseCaseImpl(FireAuthRepoImpl())
        )
        viewModel = ViewModelProvider(this,factory).get(LoginViewModel::class.java)

        firebaseFireStore.firestoreSettings = settings

        bindign.viewModel = viewModel

        checkUser()

        clikListeners()
        observers()

        return bindign.root
    }

    private fun checkUser() {
        val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE) ?: return
        val fase = sharedPref.getInt("Fase",0)

        if (viewModel.getUser() && fase == 1){
            //this.findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToMenuFragment())
            Log.i("Login","Pase Rapido")
            val intent  = Intent(this.context, MenuActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
            this.activity?.finish()
        }else if(viewModel.getUser() && fase == 0){
            Log.i("Login","Pase Internet")
            viewModel.getFase()
        }
    }

    private fun clikListeners() {
        bindign.btnLoginRegistrarse1.setOnClickListener {
            this.findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToRegisterFragment())
        }
        bindign.btnLoginRegistrarse2.setOnClickListener {
            this.findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToRegisterFragment())
        }
        bindign.btnLoginContraolvidada.setOnClickListener {
            this.findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToRecoveryFragment())
        }

        bindign.btnLoginGoogle.setOnClickListener {
            loginGoogle()
        }
    }

    private fun loginGoogle() {
        val providers = arrayListOf(
            AuthUI.IdpConfig.GoogleBuilder().build()
        )

        startActivityForResult(
            AuthUI.getInstance().createSignInIntentBuilder()
                .setAvailableProviders(providers).build(), RC_SIGN_IN
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val response = IdpResponse.fromResultIntent(data)

            if(resultCode == RESULT_OK){
                if (response!!.isNewUser){
                    viewModel.google()
                }else{
                    checkUser()
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

    private fun observers(){
        viewModel.eventoLogin.observe(this.viewLifecycleOwner, Observer {result ->
            when (result){
                is Resource.Loading -> {
                    val inputMethodManager = this.context?.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
                    inputMethodManager.hideSoftInputFromWindow(this.view!!.windowToken, 0)
                    bindign.cirLoginButtonLogin.visibility = View.VISIBLE
                    bindign.btnLoginIniciar.isEnabled = false
                }
                is Resource.Success -> {
                    bindign.cirLoginButtonLogin.visibility = View.GONE
                    bindign.btnLoginIniciar.isEnabled = true
                    //this.findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToMenuFragment())
                    checkUser()
                }
                is Resource.Failure -> {
                    bindign.cirLoginButtonLogin.visibility = View.GONE
                    bindign.btnLoginIniciar.isEnabled = true
                    if(result.exception.message == "0"){
                        this.findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToInfoFragment())
                    }else{
                        Snackbar.make(this.view!!, result.exception.message.toString(), Snackbar.LENGTH_LONG).show()
                    }

                }
            }
        })

        viewModel.eventoGoogle.observe(this.viewLifecycleOwner, Observer {result ->
            when(result) {
                is Resource.Loading ->{
                    bindign.cirLoginButtonLogin.visibility = View.VISIBLE
                }
                is Resource.Success ->{
                    bindign.cirLoginButtonLogin.visibility = View.GONE
                    Snackbar.make(this.view!!,result.data.toString(), Snackbar.LENGTH_LONG).show()
                    checkUser()
                }
                is Resource.Failure ->{
                    bindign.cirLoginButtonLogin.visibility = View.GONE
                    Snackbar.make(this.view!!, result.exception.message.toString(), Snackbar.LENGTH_LONG).show()
                }
            }
        })

        viewModel.eventoFase.observe(this.viewLifecycleOwner, Observer {result ->
            when(result) {
                is Resource.Loading ->{
                    bindign.cirLoginButtonLogin.visibility = View.VISIBLE
                    bindign.btnLoginGoogle.isEnabled = false
                }
                is Resource.Success ->{
                    bindign.cirLoginButtonLogin.visibility = View.GONE
                    if (result.data == 0){
                        this.findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToInfoFragment())
                    }else{
                        val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE) ?: return@Observer
                        sharedPref.edit().putInt("Fase",1).apply()
                        val intent  = Intent(this.context, MenuActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        startActivity(intent)
                        this.activity?.finish()
                    }
                }
                is Resource.Failure ->{
                    bindign.cirLoginButtonLogin.visibility = View.GONE
                    bindign.btnLoginGoogle.isEnabled = true
                    Snackbar.make(this.view!!, result.exception.message.toString(), Snackbar.LENGTH_LONG).show()
                }
            }
        })


    }


}
