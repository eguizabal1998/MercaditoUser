package com.basicdeb.mercaditouser.data

import android.util.Log
import com.basicdeb.mercaditouser.objects.*
import com.basicdeb.mercaditouser.vo.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.tasks.await

class FiredbRepoImpl : FiredbRepo {

    private val firebaseAuth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }


    private val firebaseFireStore: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    private val firebaseStorage: FirebaseStorage by lazy {
        FirebaseStorage.getInstance()
    }

    private val firebaseStorage2: FirebaseStorage by lazy {
        FirebaseStorage.getInstance("gs://mercadito-prod")
    }

    private val firedatabase: FirebaseDatabase by lazy {
        FirebaseDatabase.getInstance()
    }

    private val Uid = firebaseAuth.uid.toString()


    override suspend fun getPortada(Uidn: String): Resource<StorageReference> {
        val ImagesRef =
            firebaseStorage.getReferenceFromUrl("gs://mercadito-51eb6.appspot.com/$Uidn/portada_1920x700.jpg")

        return Resource.Success(ImagesRef)
    }

    override fun getImagen(URL: String): StorageReference {
        val ImagesRef = firebaseStorage2.getReferenceFromUrl(URL)

        return ImagesRef
    }

    override suspend fun getProductos(
        Uidn: String,
        departamento: String
    ): Resource<MutableList<Producto>> {
        val data = firebaseFireStore.collection("Productos")
            .whereEqualTo("negocioId", Uidn)
            .whereEqualTo("disponible", true)
            .get().await()

        val lista = mutableListOf<Producto>()
        for (documents in data) {
            val Uidn1 = documents.id
            val ImagesRef = "gs://mercadito-prod/" + Uidn1 + "_400x400.jpg"

//            val ImagesRef = firebaseStorage2.getReferenceFromUrl("gs://mercadito-prod/"+Uidn + "_400x400.jpg")

            lista.add(
                Producto(
                    documents.data.get("nombre").toString(),
                    documents.data.get("descripcion").toString(),
                    documents.data.get("precio").toString().toFloat(),
                    documents.data.get("unidad").toString(),
                    documents.id,
                    ImagesRef
                )
            )
        }

        return Resource.Success(lista)
    }


    override suspend fun getNegocios(departamento: String): Resource<MutableList<Negocio>> {

        val data:QuerySnapshot
        var timeCache:DocumentSnapshot? = null
        var timeOnline:DocumentSnapshot? = null

        try {
            timeCache = firebaseFireStore.collection("Modificacion")
                .document("Negocios").get(Source.CACHE).await()
            Log.i("negocios", timeCache.data.toString())

            timeOnline = firebaseFireStore.collection("Modificacion")
                .document("Negocios").get().await()
            Log.i("negocios", timeOnline.data.toString())

        }catch (e:Exception){
            timeOnline = firebaseFireStore.collection("Modificacion")
                .document("Negocios").get().await()
            Log.i("negocios", timeOnline.data.toString())
        }


        if (timeOnline?.data?.get("TimeStamp") != timeCache?.data?.get("TimeStamp")){
            data = firebaseFireStore.collection("Negocios")
                .whereArrayContains("departamento", departamento).get().await()
        }else{
            data = firebaseFireStore.collection("Negocios")
                .whereArrayContains("departamento", departamento).get(Source.CACHE).await()
        }
//
//        val data = firebaseFireStore.collection("Negocios")
//            .whereArrayContains("departamento", departamento).get().await()

        val lista = mutableListOf<Negocio>()

        Log.i("negocios", data.metadata.isFromCache.toString())

        for (documents in data) {
            val Uidn = documents.id
            Log.i("imagen", "referenicia")
            val ImagesRef =
                firebaseStorage.getReferenceFromUrl("gs://mercadito-51eb6.appspot.com/$Uidn/portada_1920x700.jpg")

            Log.i("imagen", ImagesRef.toString())

            lista.add(
                Negocio(
                    documents.data.get("nombre").toString(),
                    documents.data.get("descripcion").toString(),
                    ImagesRef,
                    documents.id,
                    documents.data.get("numero").toString().toInt(),
                    documents.data.get("facebook").toString()
                )
            )
        }

        return Resource.Success(lista)
    }

    override suspend fun saveOrder(
        UidNegocio: String,
        UidUser: String,
        lista: MutableList<Orden>,
        orden: OrdenInfo
    ): Resource<Boolean> {

        val usuario = mapOf<String, Any>(
            "usuario" to Uid,
            "direccion1" to orden.direccion1,
            "direccion2" to orden.direccion2,
            "direccion3" to orden.direccion3,
            "telefono" to orden.numero,
            "coordenadas" to orden.coordenadas.toString(),
            "total" to orden.total
        )

        val key = firedatabase.reference.child("Ordenes").child(UidNegocio).push().key
        firedatabase.reference.child("Ordenes").child(UidNegocio).child(key.toString())
            .child("productos").setValue(lista).await()
        firedatabase.reference.child("Ordenes").child(UidNegocio).child(key.toString())
            .child("info").setValue(usuario).await()

        val negocio = mapOf<String, String>("negocio" to UidNegocio, "orden" to key.toString())
        firedatabase.reference.child("usuarios").child(Uid).push().setValue(negocio).await()

        return Resource.Success(true)
    }

    override suspend fun savePerfil(userPerfil: userPerfil): Resource<Boolean> {

        val data = hashMapOf("Fase" to 1)

        firebaseFireStore.collection("Usuarios").document(Uid).
        collection("Perfil").document("datos").set(data,SetOptions.merge()).await()

        firebaseFireStore.collection("Usuarios").document(Uid).
        collection("Perfil").document("datos").set(userPerfil,SetOptions.merge()).await()

        return Resource.Success(true)
    }
}