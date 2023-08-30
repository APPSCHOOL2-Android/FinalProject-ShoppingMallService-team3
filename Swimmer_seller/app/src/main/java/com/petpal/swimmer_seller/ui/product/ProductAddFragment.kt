package com.petpal.swimmer_seller.ui.product

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.TooltipCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.petpal.swimmer_seller.MainActivity
import com.petpal.swimmer_seller.R
import com.petpal.swimmer_seller.data.model.Category
import com.petpal.swimmer_seller.data.model.Image
import com.petpal.swimmer_seller.data.model.Product
import com.petpal.swimmer_seller.data.model.Review
import com.petpal.swimmer_seller.databinding.FragmentProductAddBinding
import com.petpal.swimmer_seller.databinding.ItemImageviewDeleteBinding
import com.petpal.swimmer_seller.ui.user.UserViewModel
import com.petpal.swimmer_seller.ui.user.UserViewModelFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProductAddFragment : Fragment() {
    lateinit var productViewModel: ProductViewModel
    lateinit var userViewModel: UserViewModel

    private var _fragmentProductAddBinding: FragmentProductAddBinding? = null
    private val fragmentProductAddBinding get() = _fragmentProductAddBinding!!
    lateinit var mainActivity: MainActivity

    // 대표 이미지 최대 5개, 설명 이미지 1개
    private var mainImageList = mutableListOf<Pair<Image, Bitmap>>()
    private var descriptionImage: Image? = null

    // 메인이미지, 상세이미지용 갤러리 실행 설정
    lateinit var mainGalleryLauncher: ActivityResultLauncher<Intent>
    lateinit var descriptionGalleryLauncher: ActivityResultLauncher<Intent>

    // 화면에 추가된 태그 목록
    private val addHashTagList = mutableListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _fragmentProductAddBinding = FragmentProductAddBinding.inflate(inflater)
        mainActivity = activity as MainActivity

        return fragmentProductAddBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        productViewModel = ViewModelProvider(this, ProductViewModelFactory())[ProductViewModel::class.java]
        userViewModel = ViewModelProvider(this, UserViewModelFactory())[UserViewModel::class.java]

        // 로그인 판매자의 브랜드명 가져오기 목적
        userViewModel.getCurrentSellerInfo()

        mainGalleryLauncher = mainImageGallerySetting()
        descriptionGalleryLauncher = descriptionImageGallerySetting()

        addHashTagList.clear()

        fragmentProductAddBinding.run {
            TooltipCompat.setTooltipText(infoIconMain, getString(R.string.tooltip_main_image))
            TooltipCompat.setTooltipText(infoIconDescription, getString(R.string.tooltip_description_image))

            toolbarProductAdd.setNavigationOnClickListener {
                findNavController().popBackStack()
            }
            buttonCancel.setOnClickListener {
                findNavController().popBackStack()
            }

            recyclerViewMainImage.run {
                adapter = MainImageRecyclerViewAdapter()
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            }

            // 대 → 중 → 소 선택에 따라 드롭박스 단계별 활성화, 세팅
            dropdownCategoryMain.addTextChangedListener {
                // 유효성 검사
                validateEditText(it.toString(), textInputLayoutCategoryMain, "대분류를 선택해 주세요")
                textInputLayoutCategoryMid.isEnabled = true

                // 선택한 대분류에 따라 중분류 다르게 표시, 대분류 먼저 선택해야 중분류 선택 활성화. 소분류는 아직 비활성화
                when (it.toString()) {
                    getString(R.string.female) -> dropdownCategoryMid.setSimpleItems(resources.getStringArray(R.array.categoryMid1))
                    getString(R.string.male) -> dropdownCategoryMid.setSimpleItems(resources.getStringArray(R.array.categoryMid2))
                    getString(R.string.child) -> dropdownCategoryMid.setSimpleItems(resources.getStringArray(R.array.categoryMid3))
                    getString(R.string.supplies) -> dropdownCategoryMid.setSimpleItems(resources.getStringArray(R.array.categoryMid4))
                    else -> textInputLayoutCategoryMid.isEnabled = false
                }
            }
            dropdownCategoryMid.addTextChangedListener {
                validateEditText(it.toString(), textInputLayoutCategoryMid, "중분류를 선택해 주세요")
                // 선택한 중분류에 따라 소분류 다르게 표시
                val categoryMain = dropdownCategoryMain.text.toString()
                val categoryMid = it.toString()
                textInputLayoutCategorySub.isEnabled = true

                when (categoryMain) {
                    getString(R.string.female) -> {
                        when (categoryMid) {
                            getString(R.string.swimsuit) -> dropdownCategorySub.setSimpleItems(resources.getStringArray(R.array.categorySub1_1))
                            getString(R.string.bikini) -> dropdownCategorySub.setSimpleItems(resources.getStringArray(R.array.categorySub1_2))
                            getString(R.string.rashguard) -> dropdownCategorySub.setSimpleItems(resources.getStringArray(R.array.categorySub1_3))
                            getString(R.string.beachwear) -> dropdownCategorySub.setSimpleItems(resources.getStringArray(R.array.categorySub1_4))
                        }
                    }
                    getString(R.string.male) -> {
                        when (categoryMid) {
                            getString(R.string.swimsuit) -> dropdownCategorySub.setSimpleItems(resources.getStringArray(R.array.categorySub2_1))
                            getString(R.string.rashguard) -> dropdownCategorySub.setSimpleItems(resources.getStringArray(R.array.categorySub2_2))
                            getString(R.string.beachwear) -> dropdownCategorySub.setSimpleItems(resources.getStringArray(R.array.categorySub2_3))
                        }
                    }
                    getString(R.string.child) -> {
                        when (categoryMid) {
                            getString(R.string.swimsuit) -> dropdownCategorySub.setSimpleItems(resources.getStringArray(R.array.categorySub3_1))
                            getString(R.string.rashguard) -> dropdownCategorySub.setSimpleItems(resources.getStringArray(R.array.categorySub3_2))
                        }
                    }
                    getString(R.string.supplies) -> {
                        when(categoryMid){
                            getString(R.string.goggles) -> dropdownCategorySub.setSimpleItems(resources.getStringArray(R.array.categorySub4_1))
                            getString(R.string.cap) -> dropdownCategorySub.setSimpleItems(resources.getStringArray(R.array.categorySub4_2))
                            getString(R.string.flippers) -> dropdownCategorySub.setSimpleItems(resources.getStringArray(R.array.categorySub4_3))
                            getString(R.string.bag) -> dropdownCategorySub.setSimpleItems(resources.getStringArray(R.array.categorySub4_4))
                            getString(R.string.water_toy) -> dropdownCategorySub.setSimpleItems(resources.getStringArray(R.array.categorySub4_5))
                        }
                    }
                }
            }
            dropdownCategorySub.addTextChangedListener {
                validateEditText(it.toString(), textInputLayoutCategorySub, "소분류를 선택해 주세요")
            }

            // 입력시 실시간 유효성 검사
            textInputEditTextProductName.addTextChangedListener {
                validateEditText(it.toString(), textInputLayoutName, "상품명을 입력해 주세요")
            }
            textInputEditTextPrice.addTextChangedListener {
                validateEditText(it.toString(), textInputLayoutPrice, "가격을 입력해 주세요")
            }
            textInputEditTextDescription.addTextChangedListener {
                validateEditText(it.toString(), textInputLayoutDescription, "상품 설명을 입력해 주세요")
            }

            // 해시태그 Chip 추가
            textInputEditTextHashTag.setOnEditorActionListener { _, _, _ ->
                addHashTag()
                true
            }
            buttonAddHashTag.setOnClickListener{
                addHashTag()
            }

            // 대표 이미지 추가
            buttonAddMainImage.setOnClickListener {
                val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
                    type = "image/*"
                    putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/*"))
                }
                mainGalleryLauncher.launch(galleryIntent)
            }

            // 설명 이미지 추가
            buttonAddDescImage.setOnClickListener {
                val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
                    type = "image/*"
                    putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/*"))
                }
                descriptionGalleryLauncher.launch(galleryIntent)
            }

            buttonNext.setOnClickListener {
                // 최종 유효성 검사
                if (mainImageList.size < 1) {
                    buttonAddMainImage.requestFocus()
                    scrollViewProductAdd.scrollTo(0, buttonAddMainImage.top)
                    Snackbar.make(fragmentProductAddBinding.root, "상품 이미지를 1개 이상 등록해 주세요", Snackbar.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                if (!validateEditText(textInputEditTextProductName.text.toString(), textInputLayoutName, "상품명을 입력해 주세요")){
                    return@setOnClickListener
                }
                if (!validateEditText(dropdownCategoryMain.text.toString(), textInputLayoutCategoryMain, "대분류를 선택해 주세요")){
                    return@setOnClickListener
                }
                if (!validateEditText(dropdownCategoryMid.text.toString(), textInputLayoutCategoryMid, "중분류를 선택해 주세요")){
                    return@setOnClickListener
                }
                if (!validateEditText(dropdownCategorySub.text.toString(), textInputLayoutCategorySub, "소분류를 선택해 주세요")){
                    return@setOnClickListener
                }
                if(!validateEditText(textInputEditTextPrice.text.toString(), textInputLayoutPrice, "가격을 입력해 주세요")){
                    return@setOnClickListener
                }
                if (!validateEditText(textInputEditTextDescription.text.toString(), textInputLayoutDescription, "상품 설명을 입력해 주세요")){
                    return@setOnClickListener
                }
                if (descriptionImage == null) {
                    buttonAddDescImage.requestFocus()
                    scrollViewProductAdd.scrollTo(0, buttonAddDescImage.bottom)
                    Snackbar.make(fragmentProductAddBinding.root, "상품 설명 이미지를 등록해 주세요", Snackbar.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                // 상품 등록 진행중 표시
                progressBar.visibility = View.VISIBLE

                val category = Category(
                    dropdownCategoryMain.text.toString(),
                    dropdownCategoryMid.text.toString(),
                    dropdownCategorySub.text.toString()
                )
                
                // 로그인 판매자 브랜드 이름
                val brandName = userViewModel.currentUser.value?.brandName
                
                // 상품 식별, 파일명에 사용할 고유 코드
                val code = System.currentTimeMillis().toString()

                // DB에 저장할 이미지들 파일명 설정
                for (idx in mainImageList.indices) {
                    mainImageList[idx].first.fileName = "image/${code}_main_image${idx+1}.jpg"
                }
                descriptionImage!!.fileName = "image/${code}_description_image.jpg"

                val product = Product(
                    "",
                    code,
                    textInputEditTextProductName.text.toString(),
                    textInputEditTextPrice.text.toString().toLong(),
                    mainImageList.map { it.first.fileName.toString() },
                    textInputEditTextDescription.text.toString(),
                    descriptionImage!!.fileName,
                    Firebase.auth.currentUser?.uid,
                    null,
                    null,
                    addHashTagList,
                    category,
                    mutableListOf<Review>(),
                    0L,
                    SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault()).format(Date()),
                    brandName
                )

                // DB에 저장할 product, image 리스트 전달
                val imageArrayList = arrayListOf<Image>()
                imageArrayList.addAll(mainImageList.map { it.first })
                imageArrayList.add(descriptionImage!!)

                // Safe Args 방식 전달
                val action = ProductAddFragmentDirections.actionItemProductAddToItemProductOption(product, imageArrayList.toTypedArray())
                findNavController().navigate(action)
            }
        }
    }

    private fun addHashTag() {
        fragmentProductAddBinding.run {
            // 태그는 최대 10개까지 등록 가능
            if(addHashTagList.size < 10){
                // 입력 문자열 태그로 분리, 중복 태그 & 이미 chip 생성된 태그 제외
                val inputHashTagList = textInputEditTextHashTag.text.toString()
                    .split(",")
                    .map(String::trim)
                    .distinct()
                    .filter { !addHashTagList.contains(it) }

                // 추가된 태그 저장
                addHashTagList.addAll(inputHashTagList)

                for (inputHashTag in inputHashTagList) {
                    val chip = layoutInflater.inflate(R.layout.item_chip_input, chipGroupHashTag, false) as Chip
                    chip.apply {
                        text = inputHashTag
                        setOnCloseIconClickListener {
                            chipGroupHashTag.removeView(it)
                            addHashTagList.remove(inputHashTag)
                        }
                    }
                    chipGroupHashTag.addView(chip)
                }
            }
            textInputEditTextHashTag.setText("")
        }
    }

    // EditText 유효성 검사
    private fun validateEditText(input: String, textInputLayout: TextInputLayout, errorMessage: String): Boolean {
        return if (input.isNotBlank()) {
            textInputLayout.isErrorEnabled = false
            textInputLayout.error = null
            true
        } else {
            textInputLayout.isErrorEnabled = true
            textInputLayout.error = errorMessage
            mainActivity.showSoftInput(textInputLayout)
            false
        }
    }

    // 메인 이미지 갤러리 설정
    private fun mainImageGallerySetting(): ActivityResultLauncher<Intent>{
        val galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            // 메인 이미지 최대 5개까지만 첨부 가능
            if (mainImageList.count() < 5){
                if (it.resultCode == AppCompatActivity.RESULT_OK && it.data?.data != null) {
                    val uri = it.data?.data!!
                    var bitmap: Bitmap? = null

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        val source = ImageDecoder.createSource(mainActivity.contentResolver, uri)
                        bitmap = ImageDecoder.decodeBitmap(source)
                    } else {
                        val cursor = mainActivity.contentResolver.query(uri, null, null, null, null)
                        if (cursor != null) {
                            cursor.moveToNext()
                            val colIdx = cursor.getColumnIndex(MediaStore.Images.Media.DATA)
                            val source = cursor.getString(colIdx)
                            bitmap = BitmapFactory.decodeFile(source)
                        }
                    }
                    // 메인 이미지 리스트에 추가
                    val pairImageInfo = Image(uri.toString(), "") to bitmap!!
                    mainImageList.add(pairImageInfo)
                    fragmentProductAddBinding.buttonAddMainImage.text = "${mainImageList.size}/5"
                    fragmentProductAddBinding.recyclerViewMainImage.adapter?.notifyDataSetChanged()
                }
            }
        }
        return galleryLauncher
    }

    // 설명 이미지 갤러리 설정
    private fun descriptionImageGallerySetting(): ActivityResultLauncher<Intent>{
        val galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {

            // 갤러리에서 사진을 선택했고, 이미 등록된 설명 이미지가 없을 경우
            if (descriptionImage == null) {
                if (it.resultCode == AppCompatActivity.RESULT_OK && it.data?.data != null){
                    val uri = it.data?.data!!
                    // RecyclerView 표시용 저장
                    var bitmap: Bitmap? = null

                    // 이미지 카드뷰 추가
                    val previewLinearLayout = layoutInflater.inflate(R.layout.item_imageview_delete, fragmentProductAddBinding.linearDescriptionImage, false) as LinearLayout
                    val previewImageView = previewLinearLayout.findViewById<ImageView>(R.id.imageViewDelete)
                    val previewButton = previewLinearLayout.findViewById<Button>(R.id.buttonDelete)
                    previewButton.setOnClickListener {
                        // 이미지 카드뷰 삭제, 리스트에서 제거
                        fragmentProductAddBinding.linearDescriptionImage.removeView(previewLinearLayout)
                        descriptionImage = null
                        fragmentProductAddBinding.buttonAddDescImage.text = "0/1"
                    }
                    fragmentProductAddBinding.linearDescriptionImage.addView(previewLinearLayout)

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        val source = ImageDecoder.createSource(mainActivity.contentResolver, uri)
                        bitmap = ImageDecoder.decodeBitmap(source)
                        previewImageView.setImageBitmap(bitmap)
                    } else {
                        val cursor = mainActivity.contentResolver.query(uri, null, null, null, null)
                        if (cursor != null) {
                            cursor.moveToNext()

                            val colIdx = cursor.getColumnIndex(MediaStore.Images.Media.DATA)
                            val source = cursor.getString(colIdx)

                            bitmap = BitmapFactory.decodeFile(source)
                            previewImageView.setImageBitmap(bitmap)
                        }
                    }

                    // 이미지 정보 저장 해두기
                    descriptionImage = Image(uri.toString(), "")
                    fragmentProductAddBinding.buttonAddDescImage.text = "1/1"
                }
            }
        }

        return galleryLauncher
    }

    // 메인 이미지 리사이클러뷰 어댑터
    inner class MainImageRecyclerViewAdapter: RecyclerView.Adapter<MainImageRecyclerViewAdapter.MainImageViewHolder>(){
        inner class MainImageViewHolder(itemImageviewDeleteBinding: ItemImageviewDeleteBinding):RecyclerView.ViewHolder(itemImageviewDeleteBinding.root) {
            val imageViewMain: ImageView = itemImageviewDeleteBinding.imageViewDelete
            val textViewIsMain : TextView = itemImageviewDeleteBinding.textViewIsMain
            private val buttonDeleteMain: Button = itemImageviewDeleteBinding.buttonDelete

            init {
                // 우측 상단 X버튼 클릭시 이미지 삭제
                buttonDeleteMain.setOnClickListener {
                    mainImageList.removeAt(adapterPosition)
                    fragmentProductAddBinding.buttonAddMainImage.text = "${mainImageList.count()}/5"
                    notifyDataSetChanged()
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainImageViewHolder {
            val imageViewBinding = ItemImageviewDeleteBinding.inflate(layoutInflater)
            // imageViewBinding.root.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT)
            return MainImageViewHolder(imageViewBinding)
        }

        override fun getItemCount(): Int {
            return mainImageList.size
        }

        override fun onBindViewHolder(holder: MainImageViewHolder, position: Int) {
            // 메인 이미지 리스트에 저장된 Bitmap을 커스텀 뷰에 표시
            holder.imageViewMain.setImageBitmap(mainImageList[position].second)
            // 첫번째 이미지를 대표 이미지로 지정
            holder.textViewIsMain.visibility = if (position == 0) { View.VISIBLE } else { View.INVISIBLE }
        }
    }
}