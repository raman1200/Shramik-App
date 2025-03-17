package com.majdoor.ovr.shramik.app.auth.presentation

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.PhoneAuthProvider
import com.majdoor.ovr.shramik.app.DataClasses.UserData
import com.majdoor.ovr.shramik.app.R
import com.majdoor.ovr.shramik.app.databinding.FragmentBottomSheetBinding
import com.majdoor.ovr.shramik.app.utils.Response
import com.majdoor.ovr.shramik.app.utils.hideKeyboard
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PhoneSignInFragment : BottomSheetDialogFragment() {

    private val authViewModel: AuthViewModel by viewModels()
    private var _binding: FragmentBottomSheetBinding? = null
    private val binding
        get() = _binding!!
    var storedVerificationId: String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentBottomSheetBinding.inflate(inflater, container, false)
        markButtonDisable(binding.submit)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        clickListeners()
        stateObserver()
        textChangedListeners()
    }

    private fun stateObserver() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                authViewModel.phoneSignInState.collect { state ->
                    handleSignInState(state)
                }
            }
        }
    }

    private fun handleSignInState(state: Response<UserData>) {
        when (state) {
            is Response.Loading -> {
                binding.progressBar.visibility = View.VISIBLE
            }

            is Response.Success -> {
                binding.progressBar.visibility = View.GONE
                ChooseActivity.start(requireActivity(), state.data!!)
                requireActivity().finish()
                authViewModel.resetPhoneSignInState()
            }

            is Response.Error -> {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                authViewModel.resetPhoneSignInState()
            }

            is Response.Init -> {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun textChangedListeners() {
        binding.apply {
            pinView.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {}
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (start == 5) {
                        markButtonEnable(binding.submit)
                        view?.hideKeyboard()
                    } else {
                        markButtonDisable(binding.submit)
                    }

                }
            })
            phone.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {}
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (start == 9) {
                        markButtonEnable(binding.submit)
                        view?.hideKeyboard()
                    } else {
                        binding.pinView.visibility = View.GONE
                        binding.progressBar.visibility = View.GONE
                        markButtonDisable(binding.submit)
                    }
                }
            })
            pinView.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {}
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (start == 5) {
                        view?.hideKeyboard()
                        markButtonEnable(binding.submit, "Verify")
                    }
                }
            })
        }
    }

    private fun clickListeners() {
        binding.apply {
            submit.setOnClickListener {
                val number = binding.phone.text.toString()
                if (number.length == 10) {
                    val phoneNumber = "+" + binding.countryCodePicker.selectedCountryCodeAsInt.toString() + number.trim()
                    if (binding.submit.text.toString().equals("Continue", ignoreCase = true)) {
                        sendOtp(phoneNumber)
                    } else {
                        verifyOtp()
                    }
                    binding.pinView.visibility = View.VISIBLE
                } else {
                    binding.phone.error = "Enter your 10 digit Number"
                }
            }
        }
    }

    private fun verifyOtp() {
        Log.i("PhoneSignInFragment", "StoredVerificationId : $storedVerificationId")
        val credential = PhoneAuthProvider.getCredential(storedVerificationId, binding.pinView.text.toString())
        authViewModel.signInWithPhoneAuthCredential(credential)
    }

    private fun sendOtp(phoneNumber: String) {
        markButtonDisable(binding.submit)
        Toast.makeText(context, "Otp Sending...", Toast.LENGTH_SHORT).show()
        authViewModel.sendVerificationCode(phoneNumber, requireActivity(), onCodeSent = { verificationId ->
            Log.i("PhoneSignInFragment", "VerificationId : $verificationId")
            binding.pinView.isEnabled = true
            storedVerificationId = verificationId
            binding.pinView.requestFocus()
        })
    }

    private fun markButtonDisable(button: Button) {
        button.isEnabled = false
        button.setTextColor(ContextCompat.getColor(this.requireActivity(), R.color.white))
        button.setBackgroundColor(ContextCompat.getColor(this.requireActivity(), R.color.grey))
    }

    private fun markButtonEnable(button: Button, text: String = "Continue") {
        button.isEnabled = true
        button.text = text
        button.setTextColor(ContextCompat.getColor(this.requireActivity(), R.color.black))
        button.setBackgroundColor(ContextCompat.getColor(this.requireActivity(), R.color.green))
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }

//    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
//        auth.signInWithCredential(credential).addOnCompleteListener(this.requireActivity()) { task ->
//            if (task.isSuccessful) {
//                binding.progressBar.visibility = View.GONE
//                // Sign in success, update UI with the signed-in user's information
//                val intent = Intent(activity?.applicationContext, ChooseActivity::class.java)
//                intent.putExtra("reference","phone")
//                startActivity(intent)
//                activity?.finish()
//                val user = task.result?.user
//            } else {
//                binding.progressBar.visibility = View.GONE
//                Toast.makeText(this.requireActivity(), "Failed ..",Toast.LENGTH_SHORT).show()
//                // Sign in failed, display a message and update the UI
//                if (task.exception is FirebaseAuthInvalidCredentialsException) {
//                    // The verification code entered was invalid
//                    binding.progressBar.visibility = View.GONE
//                    Toast.makeText(this.requireActivity(), "Invalid Code",Toast.LENGTH_SHORT).show()
//                }
//                // Update UI
//            }
//        }
//    }

//    private fun sendVerificationCode(number: String) {
//        val options = PhoneAuthOptions.newBuilder(auth)
//            .setPhoneNumber(number) // Phone number to verify
//            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
//            .setActivity(this.requireActivity()) // Activity (for callback binding)
//            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
//
//                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
//                    signInWithPhoneAuthCredential(credential)
//                }
//
//                override fun onVerificationFailed(e: FirebaseException) {
//                    binding.progressBar.visibility = View.GONE
//                    Toast.makeText(activity?.applicationContext, e.localizedMessage, Toast.LENGTH_LONG).show()
//                }
//
//                override fun onCodeSent(
//                    verificationId: String,
//                    token: PhoneAuthProvider.ForceResendingToken
//                ) {
//                    binding.pinView.isEnabled = true
//                    binding.progressBar.visibility = View.GONE
//                    storedVerificationId = verificationId
//                    resendToken = token
//                }
//            })
//            .build()
//        PhoneAuthProvider.verifyPhoneNumber(options)
//    }
}