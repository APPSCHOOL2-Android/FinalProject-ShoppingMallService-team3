package com.petpal.swimmer_customer.data.model

import java.io.Serializable


data class Address(
    var addressIdx: String? = null,
    val postCode: Long? = null,
    val address: String? = null,
    val name: String? = null,
    val phoneNumber: String? = null
): Serializable

data class Item(
    val productIdx: Long? = null,
    val size: Long? = null,
    val color: Long? = null,
    val quantity: Long? = null
)

data class User(
    var role:String? =null,
    var uid: String? = null,
    var email: String? = null,
    var nickName: String? = null,
    var password: String? = null,
    var phoneNumber: String? = null,
    var swimExp: String? = null,
    var point: Long? = null,
    var couponIdxList: List<Long>? = null,
    var profileImage: String? = null,
    var address: Address? = null,
    var likeList: List<Long>? = null,
    var recentProductList: List<Long>? = null,
    var cartItemList: List<Item>? = null,
    var reviewIdxList: List<Long>? = null,
    var orderIdxList: List<Long>? = null,
    var inquiryIdxList: List<Long>? = null
)
