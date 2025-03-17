package com.majdoor.ovr.shramik.app.db.firebase

import android.app.Activity
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.majdoor.ovr.shramik.app.DataClasses.UserData
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class FirebaseDB @Inject constructor(private val auth: FirebaseAuth, private val firestore: FirebaseFirestore) {

    val database = FirebaseDatabase.getInstance()
    val userRef = database.reference.child("Users")

    //    // login / register / user info
//    fun createNewUser(email: String, password: String) = auth.createUserWithEmailAndPassword(email, password)
//
//    suspend fun saveUserInformation(user: UserData) = usersCollectionRef.document(user.uid).set(user).await()
//
//    fun checkUserByMobile(mobile:String) = usersCollectionRef.whereEqualTo("number", mobile).get()
//
//    fun checkUserByEmail(email: String) = usersCollectionRef.whereEqualTo("email", email).get()
//
//    suspend fun getUserData(uid : String) = usersCollectionRef.document(uid).get().await()
//
//    fun resetPassword(email: String) = auth.sendPasswordResetEmail(email)
//
//    suspend fun loginUser(email: String, password: String) = auth.signInWithEmailAndPassword(email, password).await()

    suspend fun saveUserDetails(userData: UserData) {
        userRef.child(userData.appliedFor!!).child(userData.uid!!).setValue(userData).await()
    }

    suspend fun signInWithCredential(credential: AuthCredential) = auth.signInWithCredential(credential).await()

    fun sendVerificationCode(number: String, activity: Activity, callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(number)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    fun isUserLoggedIn() = auth.currentUser

    // logout
    fun logout() = auth.signOut()


    companion object {
        private const val TAG = "FirebaseDB"
    }
}