package com.majdoor.ovr.shramik.app.auth.data.impl

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.credentials.CredentialManager
import androidx.credentials.CredentialOption
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.majdoor.ovr.shramik.app.DataClasses.UserData
import com.majdoor.ovr.shramik.app.R
import com.majdoor.ovr.shramik.app.auth.data.AuthRepository
import com.majdoor.ovr.shramik.app.db.firebase.FirebaseDB
import com.majdoor.ovr.shramik.app.utils.Response
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(private val firebaseDB: FirebaseDB) : AuthRepository {

    override suspend fun googleSignIn(activity: Activity, launcher: ActivityResultLauncher<Intent>?): Flow<Response<UserData>> {
        return flow {
            val credentialManager = CredentialManager.create(activity)
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(getCredentialOptions(activity))
                .build()
            try {
                val result = credentialManager.getCredential(activity, request)
                when (result.credential) {
                    is CustomCredential -> {
                        if (result.credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(result.credential.data)
                            val googleTokenId = googleIdTokenCredential.idToken
                            val credential = GoogleAuthProvider.getCredential(googleTokenId, null)
                            val authResult = firebaseDB.signInWithCredential(credential)
                            if (authResult != null) {
                                val userData = extractUserData(authResult.user!!)
                                emit(Response.Success(userData))
                            } else {
                                emit(Response.Error("Login Failed"))
                            }
                        } else {
                            emit(Response.Error("Login Failed"))
                        }
                    }
                    else -> {
                        emit(Response.Error("Credential Error"))
                    }
                }
            } catch (e: NoCredentialException) {
                launcher?.launch(getAddAccountIntent())
            } catch (e: Exception) {
                Log.e(TAG, "${e.message}")
                emit(Response.Error("Something went wrong"))
                e.printStackTrace()
            }
        }.catch {
            Log.e(TAG, "${it.message}")
            emit(Response.Error("Something went wrong"))
        }
    }

    override suspend fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential): Flow<Response<UserData>> {
        return flow<Response<UserData>> {
            emit(Response.Loading())
            try {
                val authResult = firebaseDB.signInWithCredential(credential)
                if (authResult != null) {
                    val userData = extractUserData(authResult.user!!)
                    emit(Response.Success(userData))
                } else {
                    emit(Response.Error("Login Failed"))
                }
            } catch (e: FirebaseAuthInvalidCredentialsException) {
                emit(Response.Error("Invalid OTP"))
            } catch (e: Exception) {
                Log.e(TAG, "${e.localizedMessage}")
                emit(Response.Error("Authentication failed"))
            }
        }.catch {
            Log.e(TAG, "${it.localizedMessage}")
            emit(Response.Error("Error Occurred"))
        }
    }

    override suspend fun saveUserDetails(userData: UserData): Flow<Response<Boolean>> {
        return flow {
            emit(Response.Loading())
            try {
                firebaseDB.saveUserDetails(userData)
                emit(Response.Success(true))
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e(TAG, "${e.localizedMessage}")
                emit(Response.Error("Error while saving the details"))
            }
        }.catch {
            it.printStackTrace()
            Log.e(TAG, "${it.localizedMessage}")
            emit(Response.Error("Error Occurred"))
        }
    }

    override fun sendVerificationCode(number: String, activity: Activity, callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks) {
        try {
            firebaseDB.sendVerificationCode(number, activity, callbacks)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun isUserLoggedIn(): FirebaseUser? = firebaseDB.isUserLoggedIn()

    private fun getAddAccountIntent(): Intent {
        return Intent(Settings.ACTION_ADD_ACCOUNT).apply {
            putExtra(Settings.EXTRA_ACCOUNT_TYPES, arrayOf("com.google"))
        }
    }

    private fun extractUserData(user: FirebaseUser) = UserData(uid = user.uid, name = user.displayName, email = user.email, number = user.phoneNumber, image = user.photoUrl.toString())

    private fun getCredentialOptions(context: Context): CredentialOption {
        return GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setAutoSelectEnabled(false)
            .setServerClientId(context.getString(R.string.default_web_client_id))
            .build()
    }

    companion object {
        private const val TAG = "AuthRepository"
    }
}