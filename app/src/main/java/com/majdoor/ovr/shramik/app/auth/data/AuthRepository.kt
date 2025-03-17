package com.majdoor.ovr.shramik.app.auth.data

import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.majdoor.ovr.shramik.app.DataClasses.UserData
import com.majdoor.ovr.shramik.app.utils.Response
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun googleSignIn(activity: Activity, launcher: ActivityResultLauncher<Intent>?): Flow<Response<UserData>>
    suspend fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential): Flow<Response<UserData>>
    suspend fun saveUserDetails(userData: UserData): Flow<Response<Boolean>>
    fun sendVerificationCode(number: String, activity: Activity, callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks)
    fun isUserLoggedIn(): FirebaseUser?
}