package com.basicdeb.mercaditouser.ui.vistaproductos

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.basicdeb.mercaditouser.R
import com.basicdeb.mercaditouser.objects.Orden
import com.basicdeb.mercaditouser.viewmodel.vistaproductos.VistaProductosViewModel
import kotlinx.android.synthetic.main.orden_row.view.*

class OrdenAdapter(private val context: Context, var clickListener: onItemClickListener, var viewModel: VistaProductosViewModel): RecyclerView.Adapter<OrdenAdapter.MainViewHolder>(){

    private var dataList = mutableListOf<Orden>()


    fun setListData(data:MutableList<Orden>){
        dataList = data
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainViewHolder {
        val view = LayoutInflater.from(context).inflate( R.layout.orden_row,parent,false)

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
        val orden = dataList[position]
        holder.bindView(orden, clickListener)
    }

    inner class MainViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){

        fun bindView(orden: Orden, action: onItemClickListener){
            itemView.orden_row_nombre.text = orden.nombre
            itemView.orden_row_cantidad.text = orden.Cantidad.toString()
            itemView.orden_row_total.text = orden.Precio.toString()

            itemView.btn_orden_row_eliminar.setOnClickListener {
                action.onItemClick(orden,adapterPosition,it)
            }
            itemView.btn_orden_row_mas.setOnClickListener {
                action.onMasClick(orden)
            }
            itemView.btn_orden_row_menos.setOnClickListener {
                action.onMenosClick(orden)
            }
        }

    }

    interface onItemClickListener{
        fun onItemClick(item: Orden, position: Int, view: View)
        fun onMasClick(item: Orden)
        fun onMenosClick(item: Orden)
    }

}