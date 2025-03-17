package com.majdoor.ovr.shramik.app.Activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.majdoor.ovr.shramik.app.DataClasses.Constants
import com.majdoor.ovr.shramik.app.R
import com.majdoor.ovr.shramik.app.auth.presentation.SignInActivity
import com.majdoor.ovr.shramik.app.databinding.ActivityProfileBinding
import java.util.*


class ProfileActivity : AppCompatActivity() {

    lateinit var binding: ActivityProfileBinding
    private var profileImage:String? = null
    lateinit var profileName:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setActionBar()
        getDataFromIntent()
        init()
        setData()
        clickListener()
    }

    private fun setData() {
        binding.name.text = profileName
        if(profileImage != null){
            Glide.with(this).load(profileImage).placeholder(R.drawable.user).into(binding.profileImage)
        }
    }

    private fun getDataFromIntent() {
        profileName = intent.getStringExtra(Constants.NAME).toString()
        profileImage = intent.getStringExtra(Constants.IMAGE)
    }

    private fun init() {
    }
    private fun clickListener() {
        binding.logOut.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, SignInActivity::class.java))
            finishAffinity()
        }
    }
    private fun setActionBar(){
        setSupportActionBar(binding.toolbar)
        Objects.requireNonNull(supportActionBar)?.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        binding.toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed() }
    }


}