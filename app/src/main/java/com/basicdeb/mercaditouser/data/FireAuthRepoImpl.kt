package com.basicdeb.mercaditouser.data

import android.util.Log
import com.basicdeb.mercaditouser.objects.User
import com.basicdeb.mercaditouser.vo.Resource
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class FireAuthRepoImpl: FireAuthRepo {

    private val firebaseAuth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    private val firebaseFireStore: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    override suspend fun googleRegister(): Resource<String> {

        val data = hashMapOf("Fase" to 0)

        firebaseFireStore.collection("Usuarios").document(firebaseAuth.currentUser!!.uid).collection("Perfil")
            .document("datos").set(data,SetOptions.merge()).await()

        return Resource.Success("hola soy Google")
    }

    override fun facebookRegister(): Resource<String> {

        return Resource.Success("hola soy Facebook")
    }

    override suspend fun register(email: String, password: String): Resource<Boolean> {
        val user = firebaseAuth.createUserWithEmailAndPassword(email,password).await()

        val data = hashMapOf("Fase" to 0)

        firebaseFireStore.collection("Usuarios").document(user.user!!.uid).collection("Perfil")
            .document("datos").set(data, SetOptions.merge()).await()

        user.user?.sendEmailVerification()?.await()
        firebaseAuth.signOut()

        return Resource.Success(true)
    }

    override suspend fun login(email: String, password: String): Resource<User> {
        Log.i("login","fireLogin")
        var data = User("")
        val user = firebaseAuth.signInWithEmailAndPassword(email,password).await()
        val userId = user.user?.uid.toString()
        data.uid = userId

        val fase = firebaseFireStore.collection("Usuarios")
            .document(user.user!!.uid).collection("Perfil")
            .document("datos").get().await()

        Log.i("login",fase.get("Fase").toString())

        if(user.user!!.isEmailVerified && fase.get("Fase") == 1){
            return Resource.Success(data)
        }else if(user.user!!.isEmailVerified && fase.get("Fase").toString() == "0"){
            Log.i("login","fase0")
            return Resource.Failure(Exception("0"))
        }else{
            firebaseAuth.signOut()
            return Resource.Failure(Exception("Verifica tu correo para activar la cuenta"))
        }

//        return if (user.user?.isEmailVerified == true){
//            Resource.Success(data)
//        }else{
//            firebaseAuth.signOut()
//            Resource.Failure(Exception("Verifica tu correo para activar la cuenta"))
//        }


    }

    override suspend fun recovery(email: String): Resource<Boolean> {
        firebaseAuth.sendPasswordResetEmail(email).await()

        return Resource.Success(true)
    }

    override fun currentUser(): Boolean {
        val Uid = firebaseAuth.currentUser?.uid

        return !Uid.isNullOrEmpty()
    }

    override fun closeSesion(): Resource<String> {
        firebaseAuth.signOut()

        return Resource.Success("Completado")
    }

    override suspend fun getFase(): Resource<Int> {
        val data = firebaseFireStore.collection("Usuarios")
            .document(firebaseAuth.currentUser!!.uid)
            .collection("Perfil")
            .document("datos")
            .get().await()

        return Resource.Success(data.get("Fase").toString().toInt())
    }
}