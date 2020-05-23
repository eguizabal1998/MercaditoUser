package com.basicdeb.mercaditouser.ui.vistaproductos

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.ResultReceiver
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.basicdeb.mercadito.domain.FiredbUseCaseImpl
import com.basicdeb.mercaditouser.DBFactory
import com.basicdeb.mercaditouser.R
import com.basicdeb.mercaditouser.data.FetchAddressIntentService
import com.basicdeb.mercaditouser.data.FiredbRepoImpl
import com.basicdeb.mercaditouser.databinding.VistaProductosFragmentBinding
import com.basicdeb.mercaditouser.objects.Orden
import com.basicdeb.mercaditouser.objects.Producto
import com.basicdeb.mercaditouser.viewmodel.vistaproductos.VistaProductosViewModel
import com.basicdeb.mercaditouser.vo.Resource
import com.bumptech.glide.Glide
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar


class VistaProductosFragment : Fragment(), ProductosAdapter.onItemClickListener,
    OrdenAdapter.onItemClickListener, OnMapReadyCallback{

    private lateinit var viewModel: VistaProductosViewModel
    private lateinit var binding: VistaProductosFragmentBinding
    private lateinit var adapter: ProductosAdapter
    private lateinit var adapter2: OrdenAdapter

    private lateinit var googleMap: GoogleMap
    private lateinit var googleMap2: GoogleMap

    private lateinit var mapView:MapView
    private lateinit var mapView2:MapView

    var mapa2 = false
    var cambio = false

    private var lastLocation: Location? = null
    private lateinit var resultReceiver: ResultReceiver

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val factory = DBFactory(FiredbUseCaseImpl(FiredbRepoImpl()))
        binding = DataBindingUtil.inflate(inflater,R.layout.vista_productos_fragment,container,false)
        viewModel = ViewModelProvider(this,factory).get(VistaProductosViewModel::class.java)

        resultReceiver = AddressResultReceiver(Handler())
        binding.viewModel = viewModel
        //binding.scrollVistaproductos.isSmoothScrollingEnabled = true

        asignar()
        observers()

        listeners()


        return binding.root
    }

    private fun asignar() {
        viewModel.nombre.set(arguments?.getString("nombre"))
        viewModel.descripcion.set(arguments?.getString("descripcion"))
        viewModel.Uid.set(arguments?.getString("Uid"))
        val telefono =arguments?.getInt("numero").toString()
        viewModel.numero.set(telefono)
        viewModel.facebook.set(arguments?.getString("facebook"))
        //binding.imagePortadaVistaProd.setImageURI(arguments?.getString("uriPortada")?.toUri())
        viewModel.departamento = arguments?.getString("departamento").toString()

        val path = arguments?.getString("uriPortada")
        viewModel.eventoOrden.value = 0

        viewModel.getPortada()

    }

    private fun listeners(){
        binding.imgVistaProdFacebook.setOnClickListener {
            val facebook = "fb://facewebmodal/f?href=https://www.facebook.com/" + viewModel.facebook.get()
            val urlPage = "http://www.facebook.com/" + viewModel.facebook.get() + ""

            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(facebook)))
            } catch (e: Exception) {
                Log.i("facebook", "Aplicación no instalada.")
                //Abre url de pagina.
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(urlPage)))
            }
        }

        binding.btnVistaProdCompletarOrden.setOnClickListener {
            adapter2.setListData(viewModel.ordenlist)
            adapter2.notifyDataSetChanged()
            viewModel.evento = 2
            binding.scrollVistaproductos.visibility = View.GONE
            binding.scrollOrden.visibility = View.VISIBLE

            mapView = binding.mapView

            if (ContextCompat.checkSelfPermission(activity!!.baseContext, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
                // Permission is not granted
                val permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
                requestPermissions(permissions,1001)
            }else{
                mapView.onCreate(null)
                mapView.onResume()
                mapView.getMapAsync(this)
            }
        }

        binding.btnVistaProdAgregar.setOnClickListener {
            Snackbar.make(
                this.view!!,
                "Funcion aun no disponible, Solo contactar con Facebook o WhatsApp",
                Snackbar.LENGTH_LONG
            ).show()

//            viewModel.agregarOrden()
//            binding.scrollVistaproductos.visibility =View.VISIBLE
//            binding.vistaProdAgregar.visibility = View.GONE
        }

        binding.textLayoutOrdenDireccion1.setOnClickListener {
            binding.scrollOrden.visibility =View.GONE
            binding.llOrdenModMapa.visibility = View.VISIBLE
            viewModel.evento = 3
            mapView2 = binding.mapViewMod
            mapa2 = true
            mapView2.onCreate(null)
            mapView2.onResume()
            mapView2.getMapAsync(this)
        }

        binding.btnOrdenBuscarDireccion.setOnClickListener {
            findDireccion(googleMap2.cameraPosition.target)
        }

        binding.btnOrdenGuardarDireccion.setOnClickListener {
            viewModel.direccion1.set(viewModel.direccionTemporal.get())
            viewModel.latitud = googleMap2.cameraPosition.target.latitude
            viewModel.longitud = googleMap2.cameraPosition.target.longitude
            binding.scrollOrden.visibility =View.VISIBLE
            binding.llOrdenModMapa.visibility = View.GONE
            viewModel.evento = 2
            mapa2 = false
            cambio = true
            mapView.onCreate(null)
            mapView.onResume()
            mapView.getMapAsync(this)
        }

        binding.tvVistaProdNumero.setOnClickListener {
            val url = "https://api.whatsapp.com/send?phone=503${viewModel.numero.get()}"
            val intent = Intent(Intent.ACTION_VIEW, url.toUri())
            startActivity(intent)
        }

    }

    private fun observers() {
        viewModel.eventoObtenerLista.observe(this.viewLifecycleOwner, Observer {
            when (it) {
                is Resource.Loading -> {
                    binding.shimerProd.startShimmer()
                }
                is Resource.Success -> {
                    binding.shimerProd.stopShimmer()
                    binding.shimerProd.visibility = View.GONE
                    viewModel.lista.clear()
                    viewModel.lista.addAll(it.data)
                    adapter.setListData(viewModel.lista)
                    adapter.notifyDataSetChanged()
                }
                is Resource.Failure -> {
                    Snackbar.make(
                        this.view!!,
                        it.exception.message.toString(),
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }
        })

        viewModel.eventoObtenerPortada.observe(this.viewLifecycleOwner, Observer {
            when (it) {
                is Resource.Loading -> {
                }
                is Resource.Success -> {
                    Log.i("imagen",it.data.toString())
                    Glide.with(this).load(it.data)
                        .centerCrop()
                        .into(binding.imagePortadaVistaProd)

                    binding.imagePortadaVistaProd.scaleType = ImageView.ScaleType.FIT_XY
                }
                is Resource.Failure -> {
                    Snackbar.make(
                        this.view!!,
                        it.exception.message.toString(),
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }
        })

        viewModel.eventoOrden.observe(this.viewLifecycleOwner, Observer {
            if(it >= 1 && viewModel.evento == 0){
                binding.btnVistaProdCompletarOrden.visibility = View.VISIBLE
            }else if (it == 0){
                binding.btnVistaProdCompletarOrden.visibility = View.GONE
            }
            Log.i("orden",it.toString())
        })

        viewModel.eventoSaveOrder.observe(this.viewLifecycleOwner, Observer {
            when (it){
                is Resource.Loading ->{
                    Toast.makeText(this.context,"cargando",Toast.LENGTH_SHORT).show()
                }
                is Resource.Success ->{
                    this.findNavController().navigate(VistaProductosFragmentDirections.actionVistaProductosFragmentToListadoFragment())
                }
                is Resource.Failure ->{
                    Toast.makeText(this.context,it.exception.message,Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        adapter = ProductosAdapter( this.context!!, this,this,viewModel)

        val RecyclerView = binding.rvVistaProd

        RecyclerView.layoutManager = LinearLayoutManager(this.context)
        RecyclerView.adapter = adapter

        viewModel.getProductos()

        adapter2 = OrdenAdapter(this.context!!, this,viewModel)

        val RecyclerView2 = binding.rvOrdenes

        RecyclerView2.layoutManager = LinearLayoutManager(this.context)
        RecyclerView2.adapter = adapter2
    }

    override fun onItemClick(item: Producto, position: Int, view: View) {
        binding.vistaProdAgregar.visibility = View.VISIBLE
        binding.scrollVistaproductos.visibility = View.GONE

        Glide.with(this).load(viewModel.getImagen(item.imagen)).into(binding.imgVistaProdProducto)

        viewModel.cantidad.set("1")
        viewModel.nombreProd.set(item.nombre)
        binding.tvVistaProdDescripcionProd.text = item.descripcion
        viewModel.precio.set(item.precio.toString())
        viewModel.textoBoton()
        viewModel.evento = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val callback = requireActivity().onBackPressedDispatcher.addCallback(this) {
            if(viewModel.evento == 0){
                this@VistaProductosFragment.findNavController().navigate(VistaProductosFragmentDirections.actionVistaProductosFragmentToListadoFragment())
            }
            if(viewModel.evento == 1){
                binding.scrollVistaproductos.visibility =View.VISIBLE
                binding.vistaProdAgregar.visibility = View.GONE
                viewModel.evento = 0
                viewModel.eventoOrden.value = viewModel.eventoOrden.value
            }
            if(viewModel.evento == 2){
                binding.scrollVistaproductos.visibility =View.VISIBLE
                binding.scrollOrden.visibility = View.GONE
                viewModel.evento = 0
                viewModel.eventoOrden.value = viewModel.eventoOrden.value
            }
            if(viewModel.evento == 3){
                binding.scrollOrden.visibility =View.VISIBLE
                binding.llOrdenModMapa.visibility = View.GONE
                viewModel.evento = 2
                mapa2 = false
                mapView.onCreate(null)
                mapView.onResume()
                mapView.getMapAsync(this@VistaProductosFragment)
            }
        }
    }

    override fun onItemClick(item: Orden, position: Int, view: View) {
        viewModel.ordenlist.remove(item)
        viewModel.borrarOrden()
        adapter2.setListData(viewModel.ordenlist)
        adapter2.notifyDataSetChanged()
    }

    override fun onMasClick(item: Orden) {
        val ordenIndex:Int = viewModel.ordenlist.indexOf(item)
        val cantidadPrev = viewModel.ordenlist[ordenIndex].Cantidad
        val precioPrev = viewModel.ordenlist[ordenIndex].Precio
        val precioIndividual:Float = precioPrev/cantidadPrev
        val nuevaCantidad = cantidadPrev + 1
        val nuevoPrecio = precioPrev + precioIndividual
        val nuevaOrden = Orden(item.nombre,nuevaCantidad,nuevoPrecio)
        viewModel.ordenlist.set(ordenIndex,nuevaOrden)
        viewModel.updateList()
        adapter2.notifyDataSetChanged()
    }

    override fun onMenosClick(item: Orden) {
        val ordenIndex:Int = viewModel.ordenlist.indexOf(item)
        val cantidadPrev = viewModel.ordenlist[ordenIndex].Cantidad
        if (cantidadPrev == 1){
            return
        }
        val precioPrev = viewModel.ordenlist[ordenIndex].Precio
        val precioIndividual:Float = precioPrev/cantidadPrev
        val nuevaCantidad = cantidadPrev - 1
        val nuevoPrecio = precioPrev - precioIndividual
        val nuevaOrden = Orden(item.nombre,nuevaCantidad,nuevoPrecio)
        viewModel.ordenlist.set(ordenIndex,nuevaOrden)
        viewModel.updateList()
        adapter2.notifyDataSetChanged()
    }

    override fun onMapReady(p0: GoogleMap?) {
        MapsInitializer.initialize(context)
        if(!mapa2){
            if(!cambio){
                googleMap = p0!!
                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this.activity!!)
                fusedLocationClient.lastLocation.addOnSuccessListener {
                    setMarkerMap1(it.latitude,it.longitude)
                }
                googleMap.uiSettings.isScrollGesturesEnabled = false
                googleMap.uiSettings.isZoomControlsEnabled = true
            }else{
                googleMap = p0!!
                setMarkerMap1(viewModel.latitud,viewModel.longitud)
                googleMap.uiSettings.isScrollGesturesEnabled = false
            }
        }else  {
            googleMap2 = p0!!
            googleMap2.uiSettings.isScrollGesturesEnabled = true
            googleMap2.isMyLocationEnabled = true
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this.activity!!)
            fusedLocationClient.lastLocation.addOnSuccessListener {
                setMarkerMap2(it.latitude,it.longitude)
                googleMap2.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(it.latitude,it.longitude),15f))
            }
            googleMap2.setOnCameraMoveListener {
                setMarkerMap2(googleMap2.cameraPosition.target.latitude,googleMap2.cameraPosition.target.longitude)
            }
        }

    }


    fun setMarkerMap1(latitud:Double, longitud:Double){
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(latitud,longitud),20f))
        val marker1 = googleMap.addMarker(MarkerOptions().position(LatLng(latitud,longitud)))
        googleMap.isMyLocationEnabled = true

        if (!cambio){
            fetchAddressButtonHander()
        }

    }

    fun setMarkerMap2(latitud:Double, longitud:Double){
        googleMap2.clear()
        val marker1 = googleMap2.addMarker(MarkerOptions().position(LatLng(latitud,longitud)))
        marker1.position
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when(requestCode){
            1001 -> {
                if (grantResults.size >0 && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED){
                    //permission from popup granted
                    mapView.onCreate(null)
                    mapView.onResume()
                    mapView.getMapAsync(this)
                }
                else{
                    //permission from popup denied
                    Toast.makeText(this.context, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun startIntentService() {
        Log.i("resultdata",resultReceiver.toString())
        val intent = Intent(this.context, FetchAddressIntentService::class.java).apply {
            putExtra(FetchAddressIntentService.Constants.RECEIVER, resultReceiver)
            putExtra(FetchAddressIntentService.Constants.LOCATION_DATA_EXTRA, LatLng(lastLocation!!.latitude,lastLocation!!.longitude))
        }
        this.activity?.startService(intent)
    }

    private fun startIntentService2(location: LatLng) {
        Log.i("resultdata",resultReceiver.toString())
        val intent = Intent(this.context, FetchAddressIntentService::class.java).apply {
            putExtra(FetchAddressIntentService.Constants.RECEIVER, resultReceiver)
            putExtra(FetchAddressIntentService.Constants.LOCATION_DATA_EXTRA, location)
        }
        this.activity?.startService(intent)
    }

    fun fetchAddressButtonHander() {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this.activity!!)
        fusedLocationClient.lastLocation?.addOnSuccessListener { location: Location? ->
            lastLocation = location
            Log.i("resulta",location.toString())
            if (lastLocation == null) return@addOnSuccessListener

            if (!Geocoder.isPresent()) {
                Toast.makeText(this.context,
                    "no disponible",
                    Toast.LENGTH_LONG).show()
                return@addOnSuccessListener
            }

            // Start service and update UI to reflect new location
            startIntentService()
            //updateUI()
        }
    }

    fun findDireccion(location: LatLng){

        if (lastLocation == null) return

        if (!Geocoder.isPresent()) {
            Toast.makeText(this.context,
                "no disponible",
                Toast.LENGTH_LONG).show()
            return
        }

        // Start service and update UI to reflect new location
        startIntentService2(location)
    }

    internal inner class AddressResultReceiver(handler: Handler) : ResultReceiver(handler) {

        override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
            Log.i("resultdata","recibido")
            Log.i("resultdata",resultData.toString())
            // Display the address string
            // or an error message sent from the intent service.
            val addressOutput = resultData?.getString(FetchAddressIntentService.Constants.RESULT_DATA_KEY) ?: ""

            if(!mapa2){
                viewModel.direccion1.set(addressOutput)
            }else{
                viewModel.direccionTemporal.set(addressOutput)
            }
            // Show a toast message if an address was found.
            if (resultCode == FetchAddressIntentService.Constants.SUCCESS_RESULT) {

            }

        }
    }

}


