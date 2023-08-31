package com.petpal.swimmer_seller.ui.order

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.google.android.material.divider.MaterialDividerItemDecoration
import com.petpal.swimmer_seller.data.model.Item
import com.petpal.swimmer_seller.data.model.Order
import com.petpal.swimmer_seller.data.model.getOrderState
import com.petpal.swimmer_seller.databinding.FragmentOrderDetailBinding
import com.petpal.swimmer_seller.databinding.RowOrderItemBinding

class OrderDetailFragment : Fragment() {
    private lateinit var orderViewModel: OrderViewModel
    private var _fragmentOrderDetailBinding: FragmentOrderDetailBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val fragmentOrderDetailBinding get() = _fragmentOrderDetailBinding!!
    private lateinit var order: Order

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _fragmentOrderDetailBinding = FragmentOrderDetailBinding.inflate(layoutInflater)

        //argument로 넘어온 order 받아오기
        order = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable("order", Order::class.java)!!
        } else {
            arguments?.get("order") as Order
        }

        orderViewModel =
            ViewModelProvider(this, OrderViewModelFactory())[OrderViewModel::class.java]
        Log.d("orderViewModel", orderViewModel.toString())
        orderViewModel.setOrder(order)
        orderViewModel.getCustomerByUid(order.userUid)

        return fragmentOrderDetailBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        fragmentOrderDetailBinding.run {
            reyclerViewOrderProductList.run {
                adapter = OrderItemRecyclerViewAdapter()
                layoutManager = LinearLayoutManager(requireContext())
                addItemDecoration(
                    MaterialDividerItemDecoration(
                        context,
                        MaterialDividerItemDecoration.VERTICAL
                    )
                )
            }
            toolbarOrderDetail.setNavigationOnClickListener {
                findNavController().popBackStack()
            }


        }

        orderViewModel.run {
            customer.observe(viewLifecycleOwner) {
                fragmentOrderDetailBinding.run {
                    textViewOrderUserContact.text = it.contact
                    textViewOrderReceiver.text = it.name
                    textViewOrderReceiverContact.text = it.contact
                    textViewOrderUserId.text =it.email
                }
            }

            order.observe(viewLifecycleOwner) {
                fragmentOrderDetailBinding.run {
                    textViewOrderState2.text = getOrderState(it.state).str
                    textViewOrderUid.text = it.orderUid
                    textViewOrderDate.text = it.orderDate

                    textViewOrderTotalCount.text = "${it.itemList.size}개"
                    textViewOrderTotalPrice.text = "${calculateTotalPrice(it.itemList)}원"

                    textViewOrderShippingAddress1.text = it.address
                    textViewOrderShippingMessage.text = it.message
                }
            }
        }

    }

    private fun calculateTotalPrice(itemList: List<Item>): Long {
        var totalPrice = 0L

        for (item in itemList) {
            totalPrice += item.price * item.quantity
        }

        return totalPrice
    }

    inner class OrderItemRecyclerViewAdapter :
        Adapter<OrderItemRecyclerViewAdapter.OrderItemViewHolder>() {
        inner class OrderItemViewHolder(rowOrderItemBinding: RowOrderItemBinding) :
            ViewHolder(rowOrderItemBinding.root) {
            val textViewOrderProductUid = rowOrderItemBinding.textViewOrderProductUid
            val textViewOrderProductCount = rowOrderItemBinding.textViewOrderProductCount
            val textViewOrderProductName = rowOrderItemBinding.textViewOrderProductName
            val textViewOrderProductOption = rowOrderItemBinding.textViewOrderProductOption
            val textViewOrderProductPrice = rowOrderItemBinding.textViewOrderProductPrice
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderItemViewHolder {
            val rowOrderItemBinding = RowOrderItemBinding.inflate(layoutInflater)
            rowOrderItemBinding.root.layoutParams = RecyclerView.LayoutParams(
                RecyclerView.LayoutParams.MATCH_PARENT,
                RecyclerView.LayoutParams.WRAP_CONTENT
            )
            return OrderItemViewHolder(rowOrderItemBinding)
        }

        override fun getItemCount(): Int {
            return orderViewModel.order.value?.itemList?.size ?: 0
        }

        override fun onBindViewHolder(holder: OrderItemViewHolder, position: Int) {
            val item = orderViewModel.order.value!!.itemList[position]
            holder.textViewOrderProductUid.text =
                item.productUid
            holder.textViewOrderProductCount.text =
                "${item.quantity}개"
            holder.textViewOrderProductName.text =
                item.name
            holder.textViewOrderProductOption.text =
                "${item.color}, ${item.size}"
            holder.textViewOrderProductPrice.text =
                "${item.price}원"

        }
    }


}
