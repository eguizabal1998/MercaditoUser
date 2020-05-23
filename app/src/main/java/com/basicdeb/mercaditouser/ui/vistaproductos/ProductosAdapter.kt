package com.basicdeb.mercaditouser.ui.vistaproductos

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.basicdeb.mercaditouser.R
import com.basicdeb.mercaditouser.objects.Producto
import com.basicdeb.mercaditouser.viewmodel.vistaproductos.VistaProductosViewModel
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.orden_row.view.*
import kotlinx.android.synthetic.main.productorow.view.*

class ProductosAdapter(
    private val context: Context,
    var clickListener: onItemClickListener,
    var fragment: Fragment,
    var viewModel: VistaProductosViewModel
) : RecyclerView.Adapter<ProductosAdapter.MainViewHolder>() {

    private var dataList = mutableListOf<Producto>()


    fun setListData(data: MutableList<Producto>) {
        dataList = data
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.productorow, parent, false)

        return MainViewHolder(view)
    }


    override fun getItemCount(): Int {
        return if (dataList.size > 0) {
            dataList.size
        } else {
            0
        }
    }

    override fun onBindViewHolder(holder: MainViewHolder, position: Int) {
        val product = dataList[position]
        holder.bindView(product, clickListener)
    }

    inner class MainViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bindView(producto: Producto, action: onItemClickListener) {
            itemView.textView_productorow_nombre.text = producto.nombre
            itemView.textView_productorow_precio.text = producto.precio.toString()
            itemView.textView_productorow_descripcion.text = producto.descripcion
            itemView.textView_productorow_unidad.text = producto.unidad

            Glide.with(fragment).load(viewModel.getImagen(producto.imagen)).centerCrop()
                .into(itemView.imageView_producto)

            itemView.imageView_producto.scaleType = ImageView.ScaleType.FIT_XY

            itemView.constrain_productorow_info.setOnClickListener {
                action.onItemClick(producto, adapterPosition, it)
            }

        }

    }

    interface onItemClickListener {
        fun onItemClick(item: Producto, position: Int, view: View)

    }

}