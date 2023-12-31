package com.petpal.swimmer_customer.ui.payment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.petpal.swimmer_customer.R
import com.petpal.swimmer_customer.data.model.ItemsForCustomer
import com.petpal.swimmer_customer.data.model.Order
import com.petpal.swimmer_customer.databinding.FragmentPaymentBinding
import com.petpal.swimmer_customer.databinding.PaymentItemRowBinding
import com.petpal.swimmer_customer.ui.main.MainActivity
import com.petpal.swimmer_customer.ui.payment.repository.PaymentRepository
import com.petpal.swimmer_customer.ui.payment.vm.PaymentViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PaymentFragment : Fragment() {

    lateinit var fragmentPaymentBinding: FragmentPaymentBinding
    lateinit var mainActivity: MainActivity
    lateinit var paymentViewModel: PaymentViewModel
    private var deliveryName: String? = null
    private var deliveryAddress: String? = null
    private var deliveryPhoneNumber: String? = null


    // mvvm 패턴으로 변경 시 vm으로 이동 시킬 변수들
    lateinit var totalFee: String
    lateinit var customerUid: String
    private lateinit var orderItemList: MutableList<ItemsForCustomer>
    lateinit var spinnerSelect: String
    var chipSelect: Long = 0

    // -R&D-
    // spinner, chipgroup, button -> mvvm 패턴을 위해 vm으로 메서드 이전 후 데이터 처리
    // 사용자로부터 값을 받지 못한 부분에 대한 error dialog 혹은 null 값 처리


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // spinner array
        // getResources 활용 하려면 oncreate 이후에 실행 가능 기억하기
        val spinnerItems = resources.getStringArray(R.array.spinner_items)
        val spinnerList = arrayOf(
            spinnerItems[0],
            spinnerItems[1],
            spinnerItems[2],
            spinnerItems[3],
            spinnerItems[4]
        )

        fragmentPaymentBinding = FragmentPaymentBinding.inflate(inflater)
        mainActivity = activity as MainActivity

        handleBackPress()
        paymentViewModel = ViewModelProvider(mainActivity)[PaymentViewModel::class.java]
        //기본 배송지 있으면 표시
        //setDefaultAddressToTextView()

        paymentViewModel.run {

            itemList.observe(mainActivity) {
                fragmentPaymentBinding.paymentViewPager.adapter?.notifyDataSetChanged()
                orderItemList = it
            }

            paymentFee.observe(mainActivity) {
                fragmentPaymentBinding.paymentConfirmButton.text = "${paymentViewModel.formatPriceForCustomer(it.toInt())} 결제하기"
                fragmentPaymentBinding.paymentCheck.text = "총 상품 금액 : ${paymentViewModel.formatPriceForCustomer(it.toInt())}"
                totalFee = it
            }

            uid.observe(mainActivity) {
                customerUid = it
            }

        }
        fragmentPaymentBinding.run {

            //배송지 선택 button
            //배송지 관리 페이지 -> 배송지 선택-> 텍스트뷰 띄우기 구현을 했는데
            //결제하기 버튼 클릭시 앱이 종료되서 테스트를 못해봤습니다.
            paymentDeliveryButton.setOnClickListener {
                val bundle = Bundle()
                bundle.putString("FromOrder", "FromOrder")
                //val action=PaymentFragmentDirections.actionPaymentFragmentToDeliveryPointManageFragment()
                findNavController().navigate(R.id.action_paymentFragment_to_DeliveryPointManageFragment, bundle)
            }


            // spinner
            paymentSpinner.run {
                val spinnerAdapter = ArrayAdapter<String>(
                    context,
                    android.R.layout.simple_spinner_item,
                    spinnerList
                )
                spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                adapter = spinnerAdapter

                onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        // 선택된 item -> Order.message 에 넣어주기
                        spinnerSelect = selectedItem.toString()
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {
                        // dialog 를 통해 사용자에게 알려준다.
                    }
                }
            }

            // 상단 툴바
            toolbarPayment.run {
                setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material)
                val navigationIcon = navigationIcon
                navigationIcon?.setTint(ContextCompat.getColor(context, R.color.black))
                setNavigationOnClickListener {
                    // 백 버튼 문제 해결하기 -> 완료
                    if(deliveryName!=null){
                        Navigation.findNavController(fragmentPaymentBinding.root).popBackStack()
                        Navigation.findNavController(fragmentPaymentBinding.root).popBackStack()
                    }else {
                        Navigation.findNavController(fragmentPaymentBinding.root).popBackStack()
                    }
                }
            }

            // 버튼
            paymentConfirmButton.run {
                setOnClickListener {
                    // 결제 완료 버튼

                    // seller한테 넘겨줄 order객체 서버로 전송하는 메서드 구현 예정
                    val sdfDate = SimpleDateFormat("yyyy.MM.dd hh:mm", Locale.getDefault())
                    val orderDate = sdfDate.format(Date(System.currentTimeMillis()))

                    val sdfUid = SimpleDateFormat("MMddhhmmss", Locale.getDefault())
                    val orderUid = sdfUid.format(Date(System.currentTimeMillis()))

                    val order = Order(1, orderUid, Firebase.auth.currentUser?.uid!!, orderDate, spinnerSelect, chipSelect,
                        totalFee.toLong(), orderItemList, paymentDeliveryPoinAddress.text.toString(), "test_coupon_item", 1000)

                    PaymentRepository.sendOrderToSeller(order) {

                        it.addOnCanceledListener {
                            // canceled -> error dialog
                        }
                        it.addOnCompleteListener {
                            // complete -> 주문 완료 화면
                            // 주문 완료 화면으로 이동하기
                            // 해당 상품 itemsforcustomer에서 삭제
                            PaymentRepository.deleteCartItems(Firebase.auth.currentUser?.uid!!)

                            val action=PaymentFragmentDirections.actionPaymentFragmentToCompleteFragment()
                            findNavController().navigate(action)
                        }

                    }
                }
            }

            // repos -> vm -> item 목록 받기
            paymentViewModel.getItemAndCalculatePrice()
            paymentViewModel.getCustomerUid()

            // 상품 정보 viewPager2
            paymentViewPager.apply {
                adapter = ItemRecyclerAdapter()
            }
            // indicater 구성 tabLayout
            TabLayoutMediator(paymentTab, paymentViewPager) { tab, position ->
                paymentViewPager.setCurrentItem(tab.position)
            }.attach()

            // chipGroup 제어 부분
            paymentChipGroup.run {
                setOnCheckedStateChangeListener { group, checkedIds ->
                    when (checkedChipId) {
                        // 해당 chipId를 통해서 long 타입 전환
                        R.id.paymentNaver -> chipSelect = 1
                        R.id.paymentKakao -> chipSelect = 2
                        R.id.paymentCard -> chipSelect = 3
                        R.id.paymentAccount -> chipSelect = 4
                        R.id.paymentMoblie -> chipSelect = 5
                        R.id.paymentCash -> chipSelect = 6
                    }
                }
            }

        }

        return fragmentPaymentBinding.root
    }
    private fun handleBackPress() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().popBackStack()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }

    // viewPager2에 붙여줄 recyclerAdapter
    inner class ItemRecyclerAdapter : RecyclerView.Adapter<ItemRecyclerAdapter.ItemViewHolder>() {
        inner class ItemViewHolder(paymentItemRowBinding: PaymentItemRowBinding) :
            RecyclerView.ViewHolder(paymentItemRowBinding.root) {
            val paymentItemImage: ImageView
            val paymentItemName: TextView
            val paymentItemPrice: TextView
            val paymentItemQuantity: TextView
            val paymentItemColor: TextView
            val paymentItemSize: TextView

            init {
                paymentItemImage = paymentItemRowBinding.paymentItemImage
                paymentItemName = paymentItemRowBinding.paymentItemName
                paymentItemPrice = paymentItemRowBinding.paymentItemPrice
                paymentItemQuantity = paymentItemRowBinding.paymentItemQuantity
                paymentItemColor = paymentItemRowBinding.paymentItemColor
                paymentItemSize = paymentItemRowBinding.paymentItemSize
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
            val paymentItemRowBinding = PaymentItemRowBinding.inflate(layoutInflater)
            val itemViewHolder = ItemViewHolder(paymentItemRowBinding)
            paymentItemRowBinding.root.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )

            return itemViewHolder
        }

        override fun getItemCount(): Int {

            return paymentViewModel.itemList.value?.size!!

        }

        override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
            holder.paymentItemName.text =
                paymentViewModel.itemList.value?.get(position)?.name.toString()
            holder.paymentItemPrice.text =
                "가격 : ${paymentViewModel.itemList.value?.get(position)?.price.toString()}"

            holder.paymentItemQuantity.text =
                "수량 : ${paymentViewModel.itemList.value?.get(position)?.quantity.toString()}"

            // option -> color, size로 분할
            holder.paymentItemColor.text =
                "색상 : ${paymentViewModel.itemList.value?.get(position)?.color}"
            holder.paymentItemSize.text =
                "사이즈 : ${paymentViewModel.itemList.value?.get(position)?.size}"
            PaymentRepository.getItemImage(
                holder.paymentItemImage,
                paymentViewModel.itemList.value?.get(position)?.mainImage
            )
        }
    }

    override fun onResume() {
        super.onResume()
        deliveryName = arguments?.getString("name")
        deliveryAddress = arguments?.getString("address")
        deliveryPhoneNumber= arguments?.getString("phoneNumber")
        if (deliveryName != null && deliveryAddress != null && deliveryPhoneNumber != null) {
            fragmentPaymentBinding.paymentDeliveryPointLayout.visibility=View.VISIBLE
            fragmentPaymentBinding.paymentDeliveryButton.visibility = View.GONE
            fragmentPaymentBinding.paymentDeliveryPointName.text ="이름 : "+ deliveryName
            fragmentPaymentBinding.paymentDeliveryPoinAddress.text = deliveryAddress
            fragmentPaymentBinding.paymentDeliveryPointPhone.text = "휴대폰 번호 : "+deliveryPhoneNumber
        }
    }
}
