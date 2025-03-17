package com.majdoor.ovr.shramik.app.auth.presentation

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.majdoor.ovr.shramik.app.Activities.MainActivity
import com.majdoor.ovr.shramik.app.DataClasses.UserData
import com.majdoor.ovr.shramik.app.databinding.ActivitySignInBinding
import com.majdoor.ovr.shramik.app.utils.Response
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SignInActivity : AppCompatActivity() {
    lateinit var binding: ActivitySignInBinding
    lateinit var phoneSignInFragment: PhoneSignInFragment
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
        stateObserver()
        clickListeners()
    }

    private fun stateObserver() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                authViewModel.googleSignInState.collect { state ->
                    handleSignInState(state)
                }
            }
        }
    }

    private fun clickListeners() {
        binding.apply {
            signinWithPhone.setOnClickListener {
                phoneSignInFragment.show(supportFragmentManager, phoneSignInFragment.tag)
            }
            signinWithGoogle.setOnClickListener {
                authViewModel.googleSignIn(this@SignInActivity, launcher)
            }
        }
    }

    private fun init() {
        phoneSignInFragment = PhoneSignInFragment()
        getPermission()
        if (authViewModel.isUserLoggedIn() != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finishAffinity()
        }
    }

    private fun handleSignInState(state: Response<UserData>) {
        when (state) {
            is Response.Loading -> {
                binding.progressBar.visibility = View.VISIBLE
            }

            is Response.Success -> {
                binding.progressBar.visibility = View.GONE
                ChooseActivity.start(this, state.data!!)
                finish()
                authViewModel.resetGoogleSignInState()
            }

            is Response.Error -> {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this@SignInActivity, state.message, Toast.LENGTH_SHORT).show()
                authViewModel.resetGoogleSignInState()
            }

            is Response.Init -> {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun getPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ), 100
            )
        }
    }

    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        authViewModel.googleSignIn(this@SignInActivity, null)
    }
}