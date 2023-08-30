package com.petpal.swimmer_customer.ui.login

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.widget.ConstraintSet.Motion
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.petpal.swimmer_customer.R
import com.petpal.swimmer_customer.data.repository.CustomerUserRepository
import com.petpal.swimmer_customer.databinding.FragmentLoginBinding
import com.petpal.swimmer_customer.ui.main.MainActivity
import com.petpal.swimmer_customer.ui.main.MainFragment
import com.petpal.swimmer_customer.util.AutoLoginUtil

//로그인의 유효성검사를 실시하고 로그인 메서드 실행
//util의 AutoLoginUtil을 이용하여 자동 로그인 처리

class LoginFragment : Fragment() {
    lateinit var fragmentLoginBinding: FragmentLoginBinding
    lateinit var viewModel: LoginViewModel
    lateinit var mainActivity: MainActivity
    private var lastBackPressedTime: Long = 0
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mainActivity=activity as MainActivity
        fragmentLoginBinding = FragmentLoginBinding.inflate(layoutInflater)

        val mainFragment = parentFragment?.parentFragment as? MainFragment
        mainFragment?.fragmentMainBinding?.bottomNavigation?.visibility = View.GONE

        setupUI()
        setupViewModel()
        handleBackPress()

        return fragmentLoginBinding.root
    }
    private fun handleBackPress() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (System.currentTimeMillis() - lastBackPressedTime < 2000) {
                    requireActivity().finish()
                } else {
                    Toast.makeText(context, "뒤로가기 버튼을 한 번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show()
                    lastBackPressedTime = System.currentTimeMillis()
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }
    private fun showError(textInputLayout: TextInputLayout, textInputEditText: TextInputEditText, errorMessage: String) {
        textInputLayout.error = errorMessage
        Handler(Looper.getMainLooper()).postDelayed({
            textInputLayout.error = ""
            textInputEditText.text?.clear()
            showKeyboard(textInputEditText)
        }, 2000)
    }
    private fun validateCheck(email:String,password:String):Boolean {
        //이메일 비었음
        if (viewModel.isEmailEmpty(email)) {
            showError(
                fragmentLoginBinding.textInputLayoutLoginEmail,
                fragmentLoginBinding.textInputEditTextLoginEmail,
                getString(R.string.error_email_required)
            )
            return false
        }
        //이메일 형식이 올바르지 않은 경우
        if (!viewModel.isValidEmailFormat(email)) {
            showError(
                fragmentLoginBinding.textInputLayoutLoginEmail,
                fragmentLoginBinding.textInputEditTextLoginEmail,
                getString(R.string.error_invalid_email_format)
            )
            return false
        }
        //비밀번호 유효성 검사
        if (!viewModel.isValidPassword(password)) {
            showError(
                fragmentLoginBinding.textInputLayoutLoginPassword,
                fragmentLoginBinding.textInputEditTextLoginPassword,
                getString(R.string.error_password_length)
            )
            return false
        }
        return true
    }
    private fun setupUI(){
        fragmentLoginBinding.textViewCopyrightVerison.text=getString(R.string.copyright_and_version)
        fragmentLoginBinding.ButtonLogin.setOnClickListener {
            val email = fragmentLoginBinding.textInputEditTextLoginEmail.text.toString()
            val password = fragmentLoginBinding.textInputEditTextLoginPassword.text.toString()

            //유효성 검사
            if(!validateCheck(email,password)) {return@setOnClickListener}


            viewModel.signIn(email, password)?.observe(viewLifecycleOwner, Observer { success ->
                handleLoginResult(success == true)

            })
        }
        fragmentLoginBinding.ButtonRegister.setOnClickListener {
            val action = LoginFragmentDirections.actionLoginFragmentToRegisterFragment()
            findNavController().navigate(action)
        }
        //아이디찾기 눌렀을 때
        fragmentLoginBinding.ButtonFindId.setOnClickListener {
            val bundle= bundleOf(getString(R.string.findinfokey) to getString(R.string.findid))
            findNavController().navigate(R.id.action_loginFragment_to_findInfoFragment, bundle)
        }
        //비밀번호찾기 눌렀을 때
        fragmentLoginBinding.ButtonResetPassword.setOnClickListener {
            val bundle= bundleOf(getString(R.string.findinfokey) to getString(R.string.resetpassword))
            findNavController().navigate(R.id.action_loginFragment_to_findInfoFragment, bundle)

        }
        //카카오 로그인 눌렀을 때
        fragmentLoginBinding.ButtonKakaoLogin.setOnClickListener {

        }
    }
    private fun setupViewModel(){
        val factory = LoginViewModelFactory(CustomerUserRepository())
        viewModel = ViewModelProvider(this, factory).get(LoginViewModel::class.java)


    }
    //키보드 올리기
        fun showKeyboard(view: View) {
        if (view.requestFocus()) {
            val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
        }
    }
    //로그인 메서드 관리
    private fun handleLoginResult(success: Boolean) {
        if (success) {
            // 로그인 성공하면 현재 유저 받아와서 role이 사용자인지 검사
            // 사용자가 아니면 토스트로 에러 띄우고 로그아웃
            // 사용자라면 원래 로직대로 토스트 띄우고 홈화면으로 이동
            viewModel.getCurrentUser()?.observe(viewLifecycleOwner, Observer {
                if(!it?.role.equals(getString(R.string.role_customer))){
                    Toast.makeText(context, getString(R.string.login_role_error), Toast.LENGTH_LONG).show()
                    FirebaseAuth.getInstance().signOut()
                }else {
                    Toast.makeText(context, getString(R.string.login_success), Toast.LENGTH_LONG).show()
                    val navController = findNavController()
                    navController.navigate(R.id.action_LoginFragment_to_item_home)
                    val isAutoLoginChecked = fragmentLoginBinding.checkboxAutoLogin.isChecked
                    AutoLoginUtil.setAutoLogin(requireContext(), isAutoLoginChecked)
                }
            })

        } else {
            // 로그인 실패
            Toast.makeText(context, getString(R.string.login_failure), Toast.LENGTH_LONG).show()
            fragmentLoginBinding.textInputLayoutLoginEmail.error = ""
            fragmentLoginBinding.textInputEditTextLoginEmail.text?.clear()
            fragmentLoginBinding.textInputEditTextLoginPassword.text?.clear()
        }
    }


}

//뷰모델 팩토리
class LoginViewModelFactory(private val repository: CustomerUserRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LoginViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
