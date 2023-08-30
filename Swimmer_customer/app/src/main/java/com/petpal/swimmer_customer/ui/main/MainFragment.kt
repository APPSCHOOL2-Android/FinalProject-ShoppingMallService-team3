package com.petpal.swimmer_customer.ui.main

import android.os.Bundle

import android.util.Log

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.firebase.auth.FirebaseAuth
import com.petpal.swimmer_customer.R
import com.petpal.swimmer_customer.databinding.FragmentMainBinding
import com.petpal.swimmer_customer.util.AutoLoginUtil

class MainFragment : Fragment() {
    lateinit var fragmentMainBinding: FragmentMainBinding
    lateinit var navController:NavController
    private var currentTab: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentMainBinding = FragmentMainBinding.inflate(layoutInflater)

        val navHostFragment = childFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        navController = navHostFragment.navController


        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
               R.id.MainFragment, R.id.LoginFragment, R.id.RegisterFragment, R.id.FindInfoFragment -> {
                    fragmentMainBinding.bottomNavigation.visibility = View.GONE
                }
                else -> {
                    fragmentMainBinding.bottomNavigation.visibility = View.VISIBLE
                }
            }
        }

        fragmentMainBinding.bottomNavigation.run {
            setupWithNavController(navController)
            //선택된 아이템을 nextTab에 부여
            setOnItemSelectedListener {
                val nextTab = when (it.itemId) {
                    R.id.item_home -> 2
                    R.id.item_category -> 0
                    R.id.item_favorite -> 1
                    R.id.item_history -> 3
                    R.id.item_mypage -> 4
                    else -> currentTab
                }

                //currentTab nextTab 이 높으면 오->왼
                val navOptions = if (nextTab > currentTab) {
                    NavOptions.Builder()
                        .setEnterAnim(R.anim.slide_in_right)
                        .setExitAnim(R.anim.slide_out_left)
                        .setPopEnterAnim(R.anim.slide_in_left)
                        .setPopExitAnim(R.anim.slide_out_right)
                        .build()
                } else {
                    //아니면 왼->오
                    NavOptions.Builder()
                        .setEnterAnim(R.anim.slide_in_left)
                        .setExitAnim(R.anim.slide_out_right)
                        .setPopEnterAnim(R.anim.slide_in_right)
                        .setPopExitAnim(R.anim.slide_out_left)
                        .build()
                }
                //현재 텝에 선택한 탭의 값 저장
                currentTab = nextTab
                when (it.itemId) {
                    R.id.item_home -> {
                        // 홈 이동
                        navController.navigate(R.id.item_home, null, navOptions)
                        true
                    }

                    R.id.item_category -> {
                        //카테고리 페이지
                        navController.navigate(R.id.item_category, null, navOptions)
                        true
                    }

                    R.id.item_favorite -> {
                        //찜 페이지 이동
                        navController.navigate(R.id.item_favorite, null, navOptions)
                        true
                    }

                    R.id.item_history -> {
                        //히스토리 페이지 이동
                        navController.navigate(R.id.item_history, null, navOptions)
                        true
                    }

                    R.id.item_mypage -> {
                        // 마이페이지 이동

                        navController.navigate(R.id.item_mypage, null, navOptions)
                        true
                    }

                    else -> false
                }
            }
        }

        return fragmentMainBinding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        autoLogin()
    }
    private fun autoLogin(){
        val isAutoLogin = AutoLoginUtil.getAutoLogin(requireContext())
        if (!isAutoLogin) {
            navController.navigate(R.id.LoginFragment)
        }else{
            Toast.makeText(context, getString(R.string.auto_login_success), Toast.LENGTH_SHORT).show()
        }
    }
}