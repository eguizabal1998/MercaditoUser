package com.basicdeb.mercaditouser.ui.infoinicial

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.ResultReceiver
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.basicdeb.mercadito.domain.FiredbUseCaseImpl
import com.basicdeb.mercaditouser.DBFactory
import com.basicdeb.mercaditouser.R
import com.basicdeb.mercaditouser.data.FetchAddressIntentService
import com.basicdeb.mercaditouser.data.FiredbRepoImpl
import com.basicdeb.mercaditouser.databinding.InfoFragmentBinding
import com.basicdeb.mercaditouser.ui.menu.MenuActivity
import com.basicdeb.mercaditouser.viewmodel.infoinicial.InfoViewModel
import com.basicdeb.mercaditouser.vo.Resource
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar


class InfoFragment : Fragment(), OnMapReadyCallback {

    private lateinit var viewModel: InfoViewModel
    private lateinit var binding: InfoFragmentBinding

    private lateinit var googleMap: GoogleMap
    private lateinit var mapView: MapView

    var eventoUbicacion = false

    private lateinit var resultReceiver: ResultReceiver


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val factory = DBFactory(FiredbUseCaseImpl(FiredbRepoImpl()))
        binding = DataBindingUtil.inflate(inflater,R.layout.info_fragment,container,false)
        viewModel = ViewModelProvider(this,factory).get(InfoViewModel::class.java)

        binding.viewModel = viewModel
        resultReceiver = AddressResultReceiver(Handler())

        Log.i("mapa","onCreateView")
        mapView = binding.mapInfo

        binding.btnInfoGenerar.setOnClickListener {
            if (googleMap != null){
                BuscarDireccion(googleMap.cameraPosition.target)
            }
        }

        observers()

        return binding.root
    }

    private fun observers() {
        viewModel.eventoGuardar.observe(this.viewLifecycleOwner, Observer {
            when (it) {
                is Resource.Loading -> {
                    Snackbar.make(this.view!!,"Cargando", Snackbar.LENGTH_LONG).show()
                }
                is Resource.Success -> {
                    Snackbar.make(this.view!!,"Guardado", Snackbar.LENGTH_LONG).show()
                    val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE) ?: return@Observer
                    sharedPref.edit().putInt("Fase",1).apply()
                    val intent  = Intent(this.context, MenuActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    startActivity(intent)
                    this.activity?.finish()
                }
                is Resource.Failure -> {
                    Snackbar.make(this.view!!, it.exception.message.toString(), Snackbar.LENGTH_LONG).show()
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        Log.i("mapa","onResume")
        if(isLocationEnabled(this.context!!) == true && eventoUbicacion){
            Log.i("mapa","Ubicacion Activada")
            mapView.onCreate(null)
            mapView.onResume()
            mapView.getMapAsync(this)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        Log.i("mapa","onActivityCreated")
        super.onActivityCreated(savedInstanceState)

        if (ContextCompat.checkSelfPermission(activity!!.baseContext, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            val permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
            requestPermissions(permissions,1001)
        }else{
            if(isLocationEnabled(this.context!!) == false){
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                eventoUbicacion = true
            }else{
                mapView.onCreate(null)
                mapView.onResume()
                mapView.getMapAsync(this)
            }
        }
    }

    fun isLocationEnabled(context: Context): Boolean? {
        Log.i("mapa","isLocationEnabled")
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // This is new method provided in API 28
            val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            lm.isLocationEnabled
        } else {
            // This is Deprecated in API 28
            val mode: Int = Settings.Secure.getInt(
                context.contentResolver, Settings.Secure.LOCATION_MODE,
                Settings.Secure.LOCATION_MODE_OFF
            )
            mode != Settings.Secure.LOCATION_MODE_OFF
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        Log.i("mapa","onRequesPermission")
        when(requestCode){
            1001 -> {
                if (grantResults.size >0 && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED){
                    //permission from popup granted
                    if (isLocationEnabled(this.context!!) == true) {
                        mapView.onCreate(null)
                        mapView.onResume()
                        mapView.getMapAsync(this)
                    } else {
                        eventoUbicacion = true
                        startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                    }
                }
                else{
                    //permission from popup denied
                    Toast.makeText(this.context, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onMapReady(p0: GoogleMap?) {
        Log.i("mapa","onMapaReady")
        MapsInitializer.initialize(context)
        if(p0 != null){
            googleMap = p0
            googleMap.uiSettings.isMyLocationButtonEnabled = true
            googleMap.isMyLocationEnabled = true
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this.activity!!)
            fusedLocationClient.lastLocation.addOnSuccessListener {
                if(it != null){
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(it.latitude,it.longitude),17f))
                    googleMap.addMarker(MarkerOptions().position(LatLng(it.latitude,it.longitude)))
                    viewModel.coordenadas = googleMap.cameraPosition.target
                }
            }

            googleMap.setOnCameraMoveListener {
                googleMap.clear()
                googleMap.addMarker(MarkerOptions().position(googleMap.cameraPosition.target))
                viewModel.coordenadas = googleMap.cameraPosition.target
            }
        }
    }

    fun BuscarDireccion(location1: LatLng) {

        if(location1 == null){
            Toast.makeText(this.context,
                "Error obteniendo direccion",
                Toast.LENGTH_LONG).show()
            return
        }

        if (!Geocoder.isPresent()) {
            Toast.makeText(this.context,
                "no disponible",
                Toast.LENGTH_LONG).show()
            return
        }

        // Start service and update UI to reflect new location
        startIntentService(location1)
    }


    private fun startIntentService(location:LatLng) {
        Log.i("resultdata",resultReceiver.toString())
        val intent = Intent(this.context, FetchAddressIntentService::class.java).apply {
            putExtra(FetchAddressIntentService.Constants.RECEIVER, resultReceiver)
            putExtra(FetchAddressIntentService.Constants.LOCATION_DATA_EXTRA, location)
        }
        this.activity?.startService(intent)
    }

    internal inner class AddressResultReceiver(handler: Handler) : ResultReceiver(handler) {

        override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
            Log.i("resultdata","recibido")
            Log.i("resultdata",resultData.toString())
            // Display the address string
            // or an error message sent from the intent service.
            val addressOutput = resultData?.getString(FetchAddressIntentService.Constants.RESULT_DATA_KEY) ?: ""

            viewModel.direccion1.set(addressOutput)
            // Show a toast message if an address was found.
            if (resultCode == FetchAddressIntentService.Constants.SUCCESS_RESULT) {

            }

        }
    }
}
