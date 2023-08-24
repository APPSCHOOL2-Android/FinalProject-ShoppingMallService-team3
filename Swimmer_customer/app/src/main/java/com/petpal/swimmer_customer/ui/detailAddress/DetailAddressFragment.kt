package com.petpal.swimmer_customer.ui.detailAddress

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.petpal.swimmer_customer.R
import com.petpal.swimmer_customer.data.model.Address
import com.petpal.swimmer_customer.data.repository.CustomerUserRepository
import com.petpal.swimmer_customer.databinding.FragmentDetailAddressBinding

class DetailAddressFragment : Fragment() {
    lateinit var fragmentDetailAddressBinding: FragmentDetailAddressBinding
    lateinit var viewModel: DetailAddressViewModel
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentDetailAddressBinding= FragmentDetailAddressBinding.inflate(layoutInflater)
        // 받아온 데이터 처리
        val address = arguments?.getString("address")
        val postcode= arguments?.getString("postcode")

        val factory = DetailAddressModelFactory(CustomerUserRepository())
        viewModel = ViewModelProvider(this, factory).get(DetailAddressViewModel::class.java)

        fragmentDetailAddressBinding.toolbarDetailAddress.run{
            title = getString(R.string.detail_address_toolbar)
            inflateMenu(R.menu.mypage_toolbar_menu)
            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.item_notification -> {

                    }

                    R.id.item_shopping_cart -> {

                    }

                }
                false
            }
            setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material)
            setNavigationOnClickListener {
                Toast.makeText(context, getString(R.string.delevery_point_cancel), Toast.LENGTH_LONG).show()
                findNavController().navigate(R.id.DeliveryPointManageFragment)
            }
        }
        fragmentDetailAddressBinding.run{
            textView2.text=address

            ButtonSubmitAddress.setOnClickListener {
                val name=textInputEditDetailAddressName.text.toString()
                val detailAddress=textInputEditDetailAddress.text.toString()
                val phone=textInputEditDetailAddressPhone.text.toString()
                val uid=FirebaseAuth.getInstance().currentUser?.uid
                val address1=Address(null, postcode?.toLong(),"$address $detailAddress",name,phone)

                viewModel.addAddressForUser(uid!!, address1)

            }
            ButtCancelAddress.setOnClickListener {
                Toast.makeText(context, getString(R.string.delevery_point_cancel), Toast.LENGTH_LONG).show()
                findNavController().navigate(R.id.DeliveryPointManageFragment)
            }

            viewModel.updateResult.observe(viewLifecycleOwner, Observer { isSuccess ->
                if (isSuccess == true) {
                    Toast.makeText(context, "배송지가 추가되었습니다.", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.DeliveryPointManageFragment)
                } else {
                    Toast.makeText(context, "배송지 정보 추가에 실패했습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }
            })

        }

        return fragmentDetailAddressBinding.root
    }


}
class DetailAddressModelFactory(private val repository: CustomerUserRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DetailAddressViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DetailAddressViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}