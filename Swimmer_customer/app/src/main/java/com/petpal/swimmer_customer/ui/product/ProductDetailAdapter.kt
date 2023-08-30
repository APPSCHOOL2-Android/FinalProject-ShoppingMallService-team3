package com.petpal.swimmer_customer.ui.product

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.firebase.storage.FirebaseStorage
import com.petpal.swimmer_customer.R
import com.petpal.swimmer_customer.data.model.ProductDetailModel
import com.skydoves.androidveil.VeilLayout


class ProductDetailAdapter(private val context: Context, private val imagePaths: MutableList<ProductDetailModel>) :
    ListAdapter<ProductDetailModel, ProductDetailAdapter.ItemViewHolder>(
        differ
    ) {

    inner class ItemViewHolder(val view: View) : RecyclerView.ViewHolder(view) {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ItemViewHolder(
            inflater.inflate(
                R.layout.activity_product_detail_viewpage2,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val imageView = holder.itemView.findViewById<ImageView>(R.id.productDetailImage)
        val imagePath = imagePaths[position]
        val veilLayout =holder.itemView.findViewById<VeilLayout>(R.id.viewPager2VeilLayout)
        veilLayout.veil()
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            veilLayout.unVeil()
        }, 1500)

        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.reference
        val pathRef = storageRef.child(imagePath.image)

        pathRef.downloadUrl.addOnSuccessListener {
            Glide.with(context)
                .load(it)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .transition(DrawableTransitionOptions.withCrossFade())
                .error(R.drawable.noimg)
                .fitCenter()
                .into(imageView)
        }
    }

    companion object {
        val differ = object : DiffUtil.ItemCallback<ProductDetailModel>() {
            override fun areItemsTheSame(
                oldItem: ProductDetailModel,
                newItem: ProductDetailModel
            ): Boolean {
                return oldItem.image == newItem.image
            }

            override fun areContentsTheSame(
                oldItem: ProductDetailModel,
                newItem: ProductDetailModel
            ): Boolean {
                return oldItem == newItem
            }
        }
    }
}