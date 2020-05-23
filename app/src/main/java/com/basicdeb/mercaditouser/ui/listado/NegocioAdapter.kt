package com.basicdeb.mercaditouser.ui.listado

import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.basicdeb.mercaditouser.GlideApp
import com.basicdeb.mercaditouser.R
import com.basicdeb.mercaditouser.objects.Negocio
import com.basicdeb.mercaditouser.viewmodel.listado.ListadoViewModel
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.negociorow.view.*

class NegocioAdapter(private val context: Context, var clickListener: onItemClickListener, var fragment:Fragment): RecyclerView.Adapter<NegocioAdapter.MainViewHolder>(){

    private var dataList = mutableListOf<Negocio>()
    var nombre = ""

    fun setListData(data:MutableList<Negocio>){
        dataList = data
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainViewHolder {
        val view = LayoutInflater.from(context).inflate( R.layout.negociorow,parent,false)

        return MainViewHolder(view)
    }


    override fun getItemCount(): Int {
        return if(dataList.size > 0){
            dataList.size
        }else{
            0
        }
    }

    override fun onBindViewHolder(holder: MainViewHolder, position: Int) {
        val product = dataList[position]
        holder.bindView(product, clickListener,"".toUri())
    }

    inner class MainViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){

        fun bindView(negocio: Negocio, action: onItemClickListener,portada: Uri){
            itemView.tv_negociorow_nombre.text = negocio.nombre
            itemView.tv_negociorow_descripcion.text = negocio.descripcion

            GlideApp.with(fragment).load(negocio.portada)
                .fitCenter()
                .apply(RequestOptions.bitmapTransform(RoundedCorners(60)))
                .into(itemView.imgv_negociorow_portada)



            //itemView.imgv_negociorow_portada.scaleType = ImageView.ScaleType.FIT_XY
            //itemView.imgv_negociorow_portada.setImageURI(negocio.portada.toUri())
            itemView.setOnClickListener {
                action.onItemClick(negocio, adapterPosition)
            }
        }

    }

    interface onItemClickListener{
        fun onItemClick(item: Negocio, position: Int)
    }

}