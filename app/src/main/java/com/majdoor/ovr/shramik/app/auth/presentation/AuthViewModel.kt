package com.majdoor.ovr.shramik.app.auth.presentation

import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.majdoor.ovr.shramik.app.DataClasses.UserData
import com.majdoor.ovr.shramik.app.auth.data.AuthRepository
import com.majdoor.ovr.shramik.app.utils.Response
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class AuthViewModel @Inject constructor(private val authRepository: AuthRepository) : ViewModel() {

    private val _googleSignInState = MutableStateFlow<Response<UserData>>(Response.Init())
    val googleSignInState: StateFlow<Response<UserData>> = _googleSignInState.asStateFlow()

    private val _phoneSignInState = MutableStateFlow<Response<UserData>>(Response.Init())
    val phoneSignInState: StateFlow<Response<UserData>> = _phoneSignInState.asStateFlow()

    private val _saveUserState = MutableStateFlow<Response<Boolean>>(Response.Init())
    val saveUserState: StateFlow<Response<Boolean>> = _saveUserState.asStateFlow()


    fun saveUserDetails(userData: UserData) {
        viewModelScope.launch {
            authRepository.saveUserDetails(userData).collect { response ->
                _saveUserState.value = response
            }
        }
    }


    fun googleSignIn(activity: Activity, launcher: ActivityResultLauncher<Intent>?) {
        viewModelScope.launch {
            authRepository.googleSignIn(activity, launcher).collect { response ->
                _googleSignInState.value = response
            }
        }
    }

    fun isUserLoggedIn() = authRepository.isUserLoggedIn()

    fun sendVerificationCode(number: String, activity: Activity, onCodeSent: (String) -> Unit) {
        _phoneSignInState.value = Response.Loading()
        authRepository.sendVerificationCode(number, activity, callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                _phoneSignInState.value = Response.Error(e.localizedMessage ?: "Verification Failed")
            }

            override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                _phoneSignInState.value = Response.Init()
                onCodeSent(verificationId)
                super.onCodeSent(verificationId, token)
            }
        })

    }

    fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        viewModelScope.launch(Dispatchers.IO) {
            authRepository.signInWithPhoneAuthCredential(credential).collect { response ->
                _phoneSignInState.value = response
            }
        }
    }

    fun resetGoogleSignInState() {
        viewModelScope.launch {
            delay(1000)
            _googleSignInState.value = Response.Init()
        }
    }

    fun resetSaveUserState() {
        viewModelScope.launch {
            delay(1000)
            _saveUserState.value = Response.Init()
        }
    }

    fun resetPhoneSignInState() {
        viewModelScope.launch {
            delay(1000)
            _phoneSignInState.value = Response.Init()
        }
    }
}