package com.majdoor.ovr.shramik.app.auth.presentation

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.majdoor.ovr.shramik.app.Activities.MainActivity
import com.majdoor.ovr.shramik.app.DataClasses.Constants
import com.majdoor.ovr.shramik.app.DataClasses.UserData
import com.majdoor.ovr.shramik.app.R
import com.majdoor.ovr.shramik.app.databinding.ActivityChooseBinding
import com.majdoor.ovr.shramik.app.utils.Response
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ChooseActivity : AppCompatActivity() {
    private val authViewModel by viewModels<AuthViewModel>()
    private lateinit var binding: ActivityChooseBinding
    lateinit var userData: UserData
    lateinit var appliedFor: String
    lateinit var sharedPreferences: SharedPreferences
    val SHARED_PREF_NAME = "my-pref"
    lateinit var const: Constants

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChooseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getIntentFromIntent()
        initialise()
        stateObserver()
        buttonListeners()
    }

    private fun getIntentFromIntent() {
        userData = (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(USER_DATA, UserData::class.java)
        } else {
            intent.getParcelableExtra<UserData>(USER_DATA)
        })!!
    }

    private fun stateObserver() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                authViewModel.saveUserState.collect { state ->
                    handleSaveUserStateState(state)
                }
            }
        }
    }

    private fun handleSaveUserStateState(state: Response<Boolean>) {
        when (state) {
            is Response.Loading -> {
                binding.progressBar.visibility = View.VISIBLE
            }

            is Response.Success -> {
                binding.progressBar.visibility = View.GONE
                val editor = sharedPreferences.edit()
                editor.putString(Constants.APPLIED_FOR, appliedFor)
                editor.putString(Constants.IMAGE, userData.image.toString())
                editor.putString(Constants.NAME, userData.name)
                editor.apply()
                val intent = Intent(this@ChooseActivity, MainActivity::class.java)
                startActivity(intent)
                finishAffinity()
                Toast.makeText(this, "Login SuccessFully", Toast.LENGTH_SHORT).show()
                authViewModel.resetSaveUserState()
            }

            is Response.Error -> {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                authViewModel.resetSaveUserState()
            }

            is Response.Init -> {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun setBuilderBtnSelected() {
        binding.builder.setBackgroundResource(R.drawable.selected_border)
        binding.worker.setBackgroundResource(R.drawable.border)
        appliedFor = "Builder"
        markButtonEnable(binding.next)
    }

    private fun setWorkerBtnSelected() {
        binding.worker.setBackgroundResource(R.drawable.selected_border)
        binding.builder.setBackgroundResource(R.drawable.border)
        appliedFor = "Worker"
        markButtonEnable(binding.next)
    }


    private fun buttonListeners() {
        binding.builder.setOnClickListener {
            setBuilderBtnSelected()
        }
        binding.worker.setOnClickListener {
            setWorkerBtnSelected()
        }
        binding.next.setOnClickListener {
            userData.appliedFor = appliedFor
            authViewModel.saveUserDetails(userData)
        }
    }

    private fun markButtonDisable(button: Button) {
        button.isEnabled = false
        button.setBackgroundResource(R.drawable.button_bg)
    }

    private fun markButtonEnable(button: Button) {
        button.isEnabled = true
        button.setBackgroundResource(R.drawable.next_btn)
    }

    private fun initialise() {
        const = Constants()
        sharedPreferences = getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE)
        markButtonDisable(binding.next)
    }

    companion object {
        private const val USER_DATA = "user_data"

        fun start(activity: Activity, userData: UserData) {
            val intent = Intent(activity, ChooseActivity::class.java).apply {
                putExtra(USER_DATA, userData)
            }
            activity.startActivity(intent)
        }
    }
}