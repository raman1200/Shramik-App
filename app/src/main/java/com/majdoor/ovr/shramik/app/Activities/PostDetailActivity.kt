package com.majdoor.ovr.shramik.app.Activities

import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.majdoor.ovr.shramik.app.DataClasses.*
import com.majdoor.ovr.shramik.app.DataClasses.Constants.Companion.CHATS
import com.majdoor.ovr.shramik.app.DataClasses.Constants.Companion.SHARED_PREF_NAME
import com.majdoor.ovr.shramik.app.R
import com.majdoor.ovr.shramik.app.databinding.ActivityPostDetailBinding
import java.text.SimpleDateFormat
import java.util.*

class PostDetailActivity : AppCompatActivity() {
    lateinit var binding:ActivityPostDetailBinding
    lateinit var data:PostData
    lateinit var auth:FirebaseAuth
    lateinit var database: FirebaseDatabase
    lateinit var userData:UserData
    lateinit var sharedPref:SharedPreferences
    lateinit var editor: Editor

    var image = null
    var flag = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setActionBar()
        init()
        getDataFromIntent()
        getDataFromDatabase()
        checkForRoom()

        clickListerners()
    }

    private fun clickListerners() {
        binding.applyBtn.setOnClickListener{
            createRoom()
        }
    }

    private fun checkForRoom(){
        val uid_1 = auth.currentUser?.uid.toString()
        val uid_2 = data.uploadedBy.toString()
        val room = uid_1 +"--1--" +data.title +"--2--"+ uid_2

        val databaseRef = database.reference.child(CHATS).child(uid_1).child(room)

        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    markButtonDisable(binding.applyBtn)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle error
            }
        })
        val databaseRef1 = database.reference.child(CHATS).child(uid_2).child(room)

        databaseRef1.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    markButtonDisable(binding.applyBtn)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle error
            }
        })


    }



    private fun createRoom() {
        editor = sharedPref.edit()
        editor.putBoolean(data.id, true)
        editor.apply()
        markButtonDisable(binding.applyBtn)
        val uid_1 = auth.currentUser?.uid.toString()
        val uid_2 = data.uploadedBy.toString()
        val room = uid_1 +"--1--" +data.title +"--2--"+ uid_2


        val d = Date()
        val sdf1 = SimpleDateFormat("dd/mm/yy")
        val sdf2 = SimpleDateFormat("hh:mm a")
        val date = sdf1.format(d)!!
        val time = sdf2.format(d)!!

        val test1 = ChatData("Hello , how are you?", uid_1, date = date,time =time)

        database.reference.child(CHATS).child(uid_1).child(room).push().setValue(test1)
        database.reference.child(CHATS).child(uid_2).child(room).push().setValue(test1)
    }

    private fun showPB() {
        binding.progressBar.visibility = View.VISIBLE
        binding.constraint.visibility = View.GONE
    }
    private fun hidePB() {
        binding.progressBar.visibility = View.GONE
        binding.constraint.visibility = View.VISIBLE
    }
    private fun init(){
        showPB()
        sharedPref =getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE)

        data = PostData()
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        userData = UserData()
        binding.image.visibility = View.GONE



    }
//    private fun checkForRoom(){
//        val state = sharedPref.getBoolean(data.id, false)
//        if(state){
//            markButtonDisable(binding.applyBtn)
//        }
//    }
    private fun getDataFromIntent() {
        flag = intent.getBooleanExtra(Constants.SAVED, false)
        data.id = intent.getStringExtra(Constants.POST_ID)
        data.title = intent.getStringExtra(Constants.TITLE)
        data.description = intent.getStringExtra(Constants.DESCRIPTION)
        data.category = intent.getStringExtra(Constants.CATEGORY)
        data.location = intent.getStringExtra(Constants.LOCATION)
        data.salary = intent.getStringExtra(Constants.SALARY)
        data.date = intent.getStringExtra(Constants.DATE)
        data.uploadedBy = intent.getStringExtra(Constants.UPLOADEDBY)
        data.image = intent.getStringExtra(Constants.IMAGE)
    }
    private fun setActionBar(){
        setSupportActionBar(binding.toolbar)
        Objects.requireNonNull(supportActionBar)!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        binding.toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed() }
    }
    private fun markButtonDisable(button: Button) {
        val text = if(flag){
            "Already Contacted"
        }else{
            "Already applied"
        }
        button.isEnabled = false
        button.text = text
        button.setTextColor(ContextCompat.getColor(applicationContext, R.color.white))
        button.setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.grey))
    }

    private fun setData() {
        if(flag){
            binding.image.visibility = View.VISIBLE
            binding.title.text = "Name"
            binding.description.text = "Work Experience"
            binding.type.text = "Work Type"
            binding.workerLocation.text = "Working Location"
            binding.applyBtn.text = "Hire me"
            if(data.image != null){
                Glide.with(this).load(data.image).placeholder(R.drawable.user).into(binding.image)
            }
        }
        binding.jobTitle.text = data.title
        binding.jobDescription.text = data.description
        binding.jobType.text = data.category
        binding.jobLocation.text = data.location
        binding.jobSalary.text = data.salary
        binding.date.text = data.date

        if(userData.image!=null){
            Glide.with(this).load(userData.image).placeholder(R.drawable.user).into(binding.profileImage)
        }
        val name:String = if(userData.name.equals("null")){
            "Unknown"
        } else{
            userData.name.toString()
        }
        val location:String = if(userData.district.equals(null)){
            "--"
        }else{
            userData.district.toString()
        }
        binding.managerName.text = name
        binding.location.text = location
        binding.companyName.text = "--"
    }

    private fun getDataFromDatabase() {
        val type = if(flag){
            Constants.WORKER
        } else {
            Constants.BUILDER
        }
        database.reference.child(Constants.USERS).child(type).child(data.uploadedBy.toString()).addListenerForSingleValueEvent(object:ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                hidePB()
                if(snapshot.getValue(UserData::class.java)==null){
                    return
                }
                userData = snapshot.getValue(UserData::class.java)!!
               setData()

            }

            override fun onCancelled(error: DatabaseError) {
                hidePB()
                Toast.makeText(this@PostDetailActivity, error.message, Toast.LENGTH_SHORT).show()
            }

        })
    }
}