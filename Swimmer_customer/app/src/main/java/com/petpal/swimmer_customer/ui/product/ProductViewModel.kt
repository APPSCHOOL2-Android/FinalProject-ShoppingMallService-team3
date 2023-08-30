package com.petpal.swimmer_customer.ui.product

import android.widget.ImageView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.firebase.storage.FirebaseStorage
import com.petpal.swimmer_customer.R
import com.petpal.swimmer_customer.data.model.Product
import java.text.NumberFormat
import java.util.Locale


class ProductViewModel() : ViewModel() {

    private val _productDetail: MutableLiveData<List<Product>> = MutableLiveData()
    private var _productDetailImage: MutableLiveData<String> = MutableLiveData()
    private var _rankingBrand: MutableLiveData<String> = MutableLiveData()
    private var _rankingTitle: MutableLiveData<String> = MutableLiveData()
    private var _rankingPrice: MutableLiveData<Int> = MutableLiveData()
    private var _bottomSheetItemCountTextView: MutableLiveData<Int> = MutableLiveData(1)

    private var _sizePosition: MutableLiveData<Int> = MutableLiveData()
    private var _colorPosition: MutableLiveData<Int> = MutableLiveData()
    private val _isButtonEnabled: MutableLiveData<Boolean> = MutableLiveData()
    val productDetail: LiveData<List<Product>>
        get() = _productDetail

    val productDetailImage: LiveData<String>
        get() = _productDetailImage

    val rankingBrand: LiveData<String>
        get() = _rankingBrand
    val rankingTitle: LiveData<String>
        get() = _rankingTitle

    val rankingPrice: LiveData<String> = _rankingPrice.map {

        val formattedPrice = NumberFormat.getNumberInstance(Locale.getDefault()).format(it)
        "$formattedPrice 원"
    }

    val bottomSheetItemCountTextView: LiveData<Int>
        get() = _bottomSheetItemCountTextView

    val sizePosition: LiveData<Int>
        get() = _sizePosition

    val colorPosition: LiveData<Int>
        get() = _colorPosition

    val isButtonEnabled: LiveData<Boolean>
        get() = _isButtonEnabled

    fun setProductDetailRanking(productList: List<Product>) {
        _productDetail.value = productList
    }

    fun productDetailImageUri(imageView: ImageView, imageUrl: String) {
        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.reference
        val pathRef = storageRef.child(imageUrl!!)

        pathRef.downloadUrl.addOnSuccessListener {
            Glide.with(imageView.context)
                .load(it)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .transition(DrawableTransitionOptions.withCrossFade())
                .centerCrop()
                .error(R.drawable.noimg)
                .into(imageView)
        }
    }

    fun rankingText(brandText: String, titleText: String, priceText: Int) {
        _rankingBrand.value = brandText
        _rankingTitle.value = titleText
        _rankingPrice.value = priceText
    }

    fun bottomSheetItemCountTextViewPlus() {
        _bottomSheetItemCountTextView.value = _bottomSheetItemCountTextView.value?.plus(1)
    }

    fun bottomSheetItemCountTextViewMinus() {
        _bottomSheetItemCountTextView.value = _bottomSheetItemCountTextView.value?.minus(1)
    }

    fun setSize(size: Int) {
        _sizePosition.value = size
        updateButtonState()
    }

    fun setColor(color: Int) {
        _colorPosition.value = color
        updateButtonState()
    }


    private fun updateButtonState() {
        val isSizeZero = sizePosition.value == 0
        val isColorZero = colorPosition.value == 0

        _isButtonEnabled.value = !isSizeZero && !isColorZero
    }

}