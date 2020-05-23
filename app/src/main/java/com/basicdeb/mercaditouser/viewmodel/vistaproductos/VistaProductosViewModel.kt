package com.basicdeb.mercaditouser.viewmodel.vistaproductos

import android.util.Log
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.basicdeb.mercaditouser.domain.FiredbUseCase
import com.basicdeb.mercaditouser.objects.Orden
import com.basicdeb.mercaditouser.objects.OrdenInfo
import com.basicdeb.mercaditouser.objects.Producto
import com.basicdeb.mercaditouser.vo.Resource
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.launch

class VistaProductosViewModel(private val firedbUseCase: FiredbUseCase) : ViewModel() {

    val nombre = ObservableField<String>()
    val descripcion = ObservableField<String>()
    val Uid = ObservableField<String>()
    val numero = ObservableField<String>()
    val facebook = ObservableField<String>()

    val lista = mutableListOf<Producto>()

    lateinit var departamento:String

    val nombreProd = ObservableField<String>()
    val cantidad = ObservableField<String>()
    val precio = ObservableField<String>()
    val textoBoton = ObservableField<String>()

    var totalIndividual = 0.00f

    val ordenlist = mutableListOf<Orden>()

    var elementos = ObservableField<Int>()

    var evento = 0

    val subTotal = ObservableField<String>()
    var subTotal0 = 0.00f

    val direccion1 = ObservableField<String>()
    val direccionTemporal = ObservableField<String>()
    val direccion2 = ObservableField<String>()
    val direccion3 = ObservableField<String>()
    val telefono = ObservableField<String>()

    var latitud : Double = 0.0
    var longitud : Double = 0.0

    val eventoOrden:MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }

    var ordenes = 0

    val eventoObtenerLista: MutableLiveData<Resource<MutableList<Producto>>> by lazy {
        MutableLiveData<Resource<MutableList<Producto>>>()
    }
    val eventoObtenerPortada: MutableLiveData<Resource<StorageReference>> by lazy {
        MutableLiveData<Resource<StorageReference>>()
    }
    val eventoSaveOrder:MutableLiveData<Resource<Boolean>> by lazy {
        MutableLiveData<Resource<Boolean>>()
    }

    fun getProductos(){
        eventoObtenerLista.value = Resource.Loading()
        Log.i("lista",departamento)
        viewModelScope.launch {
            try {
                eventoObtenerLista.value = firedbUseCase.getProductos(Uid.get().toString(),departamento)
            }catch (e:Exception){
                eventoObtenerLista.value = Resource.Failure(e)
            }
        }
    }

    fun getPortada() {
        viewModelScope.launch {
            try {
                eventoObtenerPortada.value = firedbUseCase.getPortada(Uid.get().toString())
            }catch (e:Exception){
                eventoObtenerPortada.value = Resource.Failure(e)
            }
        }
    }

    fun getImagen(URL:String):StorageReference{
        val storage = firedbUseCase.getImagen(URL)
        return storage
    }

    fun suma(){
        val actual = cantidad.get()!!.toInt()
        val newVal = actual + 1
        cantidad.set(newVal.toString())
        textoBoton()
    }

    fun restar(){
        if (cantidad.get()?.toInt() == 1){
            return
        }
        val actual = cantidad.get()!!.toInt()
        val newVal = actual - 1
        cantidad.set(newVal.toString())
        textoBoton()
    }

    fun textoBoton(){
        totalIndividual = precio.get()!!.toFloat().times(cantidad.get()!!.toInt())
        val cadena = "Agregar ${cantidad.get()} a la orden $ $totalIndividual"
        textoBoton.set(cadena)
    }


    fun agregarOrden(){
        val orden = Orden(nombreProd.get().toString(),cantidad.get()!!.toInt(),totalIndividual)
        ordenlist.forEach {
            if(orden.nombre == it.nombre){
                val ordenIndex:Int = ordenlist.indexOf(it)
                val cantidadPrev = ordenlist[ordenIndex].Cantidad
                val precioPrev = ordenlist[ordenIndex].Precio
                val precioIndividual:Float = totalIndividual
                val nuevaCantidad = cantidadPrev + cantidad.get()!!.toInt()
                val nuevoPrecio = precioPrev + precioIndividual
                val nuevaOrden = Orden(it.nombre,nuevaCantidad,nuevoPrecio)
                ordenlist[ordenIndex] = nuevaOrden
                evento = 0
                updateList()
                return
            }
        }
        ordenes += 1
        ordenlist.add(orden)
        evento = 0
        updateList()
    }

    fun borrarOrden(){
        ordenes -= 1
        updateList()
    }

    fun updateList(){
        subTotal0 = 0.00f
        for (elements in ordenlist){
            subTotal0 += elements.Precio
        }
        subTotal.set(subTotal0.toString())
        totalIndividual = 0.00f
        eventoOrden.value = ordenes
    }

    fun checkCampos(){
        eventoSaveOrder.value = Resource.Loading()
        if(direccion1.get().isNullOrEmpty() || direccion2.get().isNullOrEmpty()){
            eventoSaveOrder.value = Resource.Failure(Exception("Complete los campos de direccion"))
            return
        }
        if(telefono.get().isNullOrEmpty()){
            telefono.set("telefono no compartido")
        }
        if(direccion3.get().isNullOrEmpty()){
            direccion3.set("Sin informacion adicional")
        }
        saveOrde()
    }

    fun saveOrde(){
        val orden = OrdenInfo(direccion1.get()!!
            ,direccion2.get()!!
            ,direccion3.get()!!
            , LatLng(latitud,longitud)
            ,telefono.get()!!,
            subTotal.get()!!.toDouble())
        viewModelScope.launch {
            try {
                eventoSaveOrder.value =  firedbUseCase.saveOrder(Uid.get().toString(),"",ordenlist,orden)
            }catch (e:Exception){
                Log.i("error",e.message.toString())
                eventoSaveOrder.value = Resource.Failure(e)
            }
        }
    }

}
