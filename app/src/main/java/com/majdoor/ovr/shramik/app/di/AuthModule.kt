package com.majdoor.ovr.shramik.app.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.majdoor.ovr.shramik.app.auth.data.AuthRepository
import com.majdoor.ovr.shramik.app.auth.data.impl.AuthRepositoryImpl
import com.majdoor.ovr.shramik.app.db.firebase.FirebaseDB
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AuthModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseDB(
        auth: FirebaseAuth,
        firestore: FirebaseFirestore
    ): FirebaseDB = FirebaseDB(auth, firestore)

    @Provides
    @Singleton
    fun provideAuthRepository(
        firebaseDB: FirebaseDB
    ): AuthRepository = AuthRepositoryImpl(firebaseDB)
}