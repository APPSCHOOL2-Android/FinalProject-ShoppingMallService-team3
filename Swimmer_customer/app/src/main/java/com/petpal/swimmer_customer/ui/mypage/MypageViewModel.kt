package com.petpal.swimmer_customer.ui.mypage

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.petpal.swimmer_customer.data.model.User
import com.petpal.swimmer_customer.data.repository.CustomerUserRepository

class MypageViewModel(private val customerUserRepository: CustomerUserRepository) : ViewModel() {

    fun getCurrentUser(uid:String): LiveData<User?> {
        return customerUserRepository.getCurrentUser(uid)
    }
    fun signOut() {
        customerUserRepository.signOut()
    }
}