package com.majdoor.ovr.shramik.app.Activities

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.majdoor.ovr.shramik.app.R
import com.majdoor.ovr.shramik.app.databinding.ActivityPersonalDetailBinding

class PersonalDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPersonalDetailBinding
    private lateinit var auth:FirebaseAuth
    private lateinit var database:FirebaseDatabase
    lateinit var name: String
    lateinit var number: String
    lateinit var email: String
    lateinit var gender: String
    lateinit var appliedFor: String
    lateinit var address: String
    lateinit var district: String
    lateinit var state: String
    lateinit var pincode: String
    lateinit var age: String
    lateinit var uid: String



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPersonalDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        markButtonDisable(binding.NextButton)
        val cu = auth.currentUser
        number = cu?.phoneNumber.toString()
        name= cu?.displayName.toString()
        email = cu?.email.toString()
        uid = cu?.uid.toString()
        val reference = intent.getStringExtra("reference")

        if(reference.equals("phone",ignoreCase = true)){
            binding.countryCodePicker.visibility = View.GONE
            binding.number.setText(number)
            binding.number.isEnabled= false
            binding.textPhoneInputLayout.isCounterEnabled = false
            setEndIcon(binding.textPhoneInputLayout)
        }
        else if(reference.equals("google",ignoreCase = true)){
            binding.name.setText(name)
            binding.email.setText(email)
            binding.email.isEnabled= false
            setEndIcon(binding.textNameInputLayout)
            setEndIcon(binding.textEmailInputlayout)
        }
        allFocus()


        binding.NextButton.setOnClickListener {
            binding.progressBar.visibility = View.VISIBLE
            var phoneNumber=number
            if(number.length==10){
                phoneNumber = "+"+binding.countryCodePicker.selectedCountryCode+number
            }
//            val user1 = UserData(uid, name, email, address, phoneNumber, pincode, state, district, gender ,appliedFor)
//            database.reference.child("Users").child(appliedFor).child(uid).setValue(user1).addOnSuccessListener {
//                binding.progressBar.visibility = View.GONE
//                startActivity(Intent(this@PersonalDetailActivity, MainActivity::class.java))
//            }
        }

    }
    private fun allFocus(){
        nameFocusable()
        emailFocusable()
        numberFocusable()
        addressFocusable()
        ageFocusable()
        genderFocusable()
        appliedFocusable()
        pincodeFocusable()

    }
    private fun checkAll():Boolean{

        return (checkerGreen(binding.textNameInputLayout) &&
                checkerGreen(binding.textEmailInputlayout) &&
                checkerGreen(binding.textPhoneInputLayout) &&
                checkerGreen(binding.textAddressInputLayout) &&
                checkerGreen(binding.textPinCodeInputLayout) &&
                checkerGreen(binding.textAgeInputLayout) &&
                checkGreen(binding.genderHelper) &&
                checkGreen(binding.appliedHelper))


    }
    private fun checkGreen(id:TextView):Boolean{
        val b = "Ok"
        return (id.text.toString().equals(b,ignoreCase = true))
    }
    private fun checkerGreen(id:TextInputLayout):Boolean{
        val a = "Verified"
        val b = "Ok"
        return (id.helperText.toString().equals(a,ignoreCase = true) || id.helperText.toString().equals(b,ignoreCase = true))
    }
    private fun pincodeFocusable(){
        binding.pincode.setOnFocusChangeListener { view, focus ->
            if(!focus){
                pincode = binding.pincode.text.toString()
                if(pincode.isEmpty() && pincode.length!=6){
                    binding.pincode.error = "Please Enter 6 Digit Postal Code"
                    hideEndIcon(binding.textPinCodeInputLayout)
                }
            }
        }
        binding.pincode.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if(start==5){
                    closeKeyBoard()
                    pincode = s.toString()
                    getCityAndStateFromPincode(s.toString())

                }
                else{
                    hideEndIcon(binding.textPinCodeInputLayout)
                }
            }
        })

    }
    private fun appliedFocusable(){
        binding.appliedRadioGrp.setOnCheckedChangeListener { _, id ->

            when(id){
                R.id.labour ->{
                    appliedFor = "Labour"
                    helperText(binding.appliedHelper)

                }
                R.id.builder->{
                    appliedFor = "Builder"
                    helperText(binding.appliedHelper)
                }
            }
        }
    }
    private fun genderFocusable(){
        binding.genderRadioGrp.setOnCheckedChangeListener { _, id ->
            when(id){
                R.id.radio_male -> {
                    gender = "Male"
                    helperText(binding.genderHelper)
                }
                R.id.radio_female-> {
                    gender = "Female"
                    helperText(binding.genderHelper)
                }
            }
        }
    }
    private fun ageFocusable(){
        binding.age.setOnFocusChangeListener { view, focus ->
            if(!focus){
                age = binding.age.text.toString()
                if(age.isEmpty()){
                    binding.age.error = "Enter your Age"
                    hideEndIcon(binding.textAgeInputLayout)
                }
                else{
                    val userAge = Integer.parseInt(age)
                    if(userAge in 16..70){
                        setEndIcon(binding.textAgeInputLayout, "Ok")
                    }
                    else{
                        binding.age.error = "Enter age in between 16 to 70"
                        hideEndIcon(binding.textAgeInputLayout)
                    }
                }
            }
        }
        binding.age.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if(s?.isNotEmpty() == true){
                    val a =Integer.parseInt(s.toString())
                    if(a in 16..70){
                        binding.pincode.requestFocus()
                    }
                    else{
                        hideEndIcon(binding.textAgeInputLayout)
                        if(start>=1){
                            binding.age.error = "Enter age in between 16 to 70"
                        }
                    }
                }

            }
        })
    }
    private fun addressFocusable(){
        binding.address.setOnFocusChangeListener { view, focus ->
            if(!focus){
                address = binding.address.text.toString()
                if(address.length in 10..100){
                    setEndIcon(binding.textAddressInputLayout, "Ok")
                }
                else{
                    hideEndIcon(binding.textAddressInputLayout)
                    if(address.isEmpty()){
                        binding.address.error = "Enter Your Address"
                    }

                    else if(address.length<10){
                        binding.address.error = "Minimum address length is 10"
                    }
                    else{
                        binding.address.error = "Maximum address length is 100"
                    }
                }
            }
        }


    }
    private fun numberFocusable(){
        binding.number.setOnFocusChangeListener { view, focus ->
            if(!focus){
                number =binding.number.text.toString()
                if(!number.matches(".*[0-9].*".toRegex())){
                    binding.number.error = "Enter only digits"
                    hideEndIcon(binding.textPhoneInputLayout)
                }
                if(number.length==10){
                    setEndIcon(binding.textPhoneInputLayout, "Ok")
                }
                else{
                    hideEndIcon(binding.textPhoneInputLayout)
                    binding.number.error = "Enter 10 Digit Number"
                }
            }
        }
        binding.number.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if(start==9){
                    binding.address.requestFocus()
                }


            }
        })

    }
    private fun emailFocusable(){
        binding.email.setOnFocusChangeListener { view, focus ->
            if(!focus){
                email = binding.email.text.toString()
                if(Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    setEndIcon(binding.textEmailInputlayout, "Ok")
                }
                else{
                    hideEndIcon(binding.textEmailInputlayout)
                }
            }
        }
    }
    private fun nameFocusable(){
        binding.name.setOnFocusChangeListener { view, focus ->
            if(!focus){
                name = binding.name.text.toString()
                if(name.isEmpty()){
                    hideEndIcon(binding.textNameInputLayout)
                    binding.name.error = "Enter Your Name"
                }
                else{
                    setEndIcon(binding.textNameInputLayout, "Ok")
                }
            }
        }
    }

    private fun helperText(id:TextView){
        id.text = "Ok"
        id.setTextColor(ContextCompat.getColorStateList(this,R.color.green_dark))
        if(checkAll()){
            markButtonEnable(binding.NextButton)
        }
    }
    private fun setEndIcon(id:TextInputLayout, s:String= "Verified"){
        id.isEndIconVisible= true
        id.setEndIconDrawable(R.drawable.right)
        id.setEndIconTintList(ContextCompat.getColorStateList(this,R.color.green_dark))
        id.helperText = s
        id.setHelperTextColor(ContextCompat.getColorStateList(this,R.color.green_dark))
        if(checkAll()){
            markButtonEnable(binding.NextButton)
        }
    }
    private fun hideEndIcon(id:TextInputLayout){
        id.isEndIconVisible= false
        id.helperText = "Required"
        id.setHelperTextColor(ContextCompat.getColorStateList(this,R.color.red))
        if(!checkAll()){
            markButtonDisable(binding.NextButton)
        }
    }

    private fun markButtonDisable(button: Button) {
        button.isEnabled = false
        button.setTextColor(ContextCompat.getColor(this, R.color.white))
        button.setBackgroundColor(ContextCompat.getColor(this, R.color.grey))
    }
    private fun markButtonEnable(button: Button) {
        button.isEnabled = true
        button.setTextColor(ContextCompat.getColor(this, R.color.black))
        button.setBackgroundColor(ContextCompat.getColor(this, R.color.green))
    }

    private fun closeKeyBoard() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
    private fun getCityAndStateFromPincode(pinCode:String){
        // on below line we are creating
        // a variable for our url.

        val url = "http://www.postalpincode.in/api/pincode/$pinCode"

        // on below line we are creating a variable for
        // our request queue and initializing it.
        val queue: RequestQueue = Volley.newRequestQueue(this)

        // on below line we are creating a variable for request
        // and initializing it with json object request
        val request = JsonObjectRequest(Request.Method.GET, url, null, { response ->

            // this method is called when we get a
            // successful response from API.

            // on below line we are adding a try catch block.
            try {
                // on below line we are getting data from our response
                // and setting it in variables.
                val districtName: String = response.getJSONArray("PostOffice").getJSONObject(0).getString("District")
                val stateName: String = response.getJSONArray("PostOffice").getJSONObject(0).getString("State")

                // on below line we are setting
                // data to our variables which we have passed.


                    district = districtName
                    state = stateName
                    binding.city.setText(district)
                    binding.state.setText(state)
                    setEndIcon(binding.textPinCodeInputLayout)



            } catch (e: Exception) {
                // on below line we are
                // handling our exception.
                binding.pincode.error = "Enter Valid Pincode"
                binding.state.setText("")
                binding.city.setText("")
                hideEndIcon(binding.textPinCodeInputLayout)
                e.printStackTrace()
            }

        }, { error ->
            // this method is called when we get any error while
            // fetching data from our API
            // in this case we are simply displaying a toast message.
            Toast.makeText(this, "Fail to get response$error", Toast.LENGTH_SHORT)
                .show()
        })
        // at last we are adding
        // our request to our queue.
        queue.add(request)
    }
}