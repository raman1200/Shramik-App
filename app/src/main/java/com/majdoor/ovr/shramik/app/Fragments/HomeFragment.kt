package com.majdoor.ovr.shramik.app.Fragments

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.majdoor.ovr.shramik.app.Activities.ProfileActivity
import com.majdoor.ovr.shramik.app.Adapters.PostAdapter
import com.majdoor.ovr.shramik.app.Adapters.WorkerPostAdapter
import com.majdoor.ovr.shramik.app.DataClasses.Constants
import com.majdoor.ovr.shramik.app.DataClasses.PostData
import com.majdoor.ovr.shramik.app.DataClasses.WorkersData
import com.majdoor.ovr.shramik.app.R
import com.majdoor.ovr.shramik.app.databinding.FragmentHomeBinding
import java.util.*
import kotlin.collections.ArrayList


class HomeFragment : Fragment() {
    private lateinit var binding:FragmentHomeBinding
    private lateinit var fusedLocationProviderClient:FusedLocationProviderClient
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var list:ArrayList<PostData>
    private lateinit var workersList:ArrayList<WorkersData>
    private lateinit var type: String
    private lateinit var category: String
    private lateinit var editor: Editor
    private lateinit var sharedPref: SharedPreferences
    private lateinit var savedList: ArrayList<String>
    private var profileImage:String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(activity == null){
            return
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        init()
        clickListener()

        getData()
        getLocation()


        return binding.root
    }

    private fun getSavedList() {
        binding.progressBar.visibility = View.VISIBLE
        savedList = ArrayList()
        database.reference.child(Constants.SAVED_POSTS).child(type).child(auth.currentUser!!.uid).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                savedList = ArrayList()
                for(snapshot1 in snapshot.children){
                    val data = snapshot1.getValue(String::class.java)
                    if(data!=null) {
                        savedList.add(data)
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
            }
        })
    }
    private fun getData() {
        binding.progressBar.visibility= View.VISIBLE
        getSavedList()
        if(category.equals(Constants.AVAILABLE_JOBS)) {
            database.reference.child(Constants.POSTS).child(category)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        list = ArrayList()
                        for (snapshot1 in snapshot.children) {
                            val data: PostData? = snapshot1.getValue(PostData::class.java)
                            if (data != null) {
                                list.add(data)
                            }
                        }
                        binding.progressBar.visibility = View.GONE
                        if (list.isEmpty()) {
                            binding.noDrafts.visibility = View.VISIBLE
                            binding.itemRecylerView.visibility = View.GONE
                            return
                        }
                        binding.noDrafts.visibility = View.GONE
                        binding.itemRecylerView.visibility = View.VISIBLE

                        val adapter = context?.let { PostAdapter(it, list, savedList) }
                        adapter?.notifyDataSetChanged()
                        binding.itemRecylerView.adapter = adapter
                        val llm = LinearLayoutManager(context)
                        llm.reverseLayout = true
                        llm.stackFromEnd = true
                        binding.itemRecylerView.layoutManager = llm
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
                    }

                })
        }
        else{
            database.reference.child(Constants.POSTS).child(category)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        workersList = ArrayList()
                        for (snapshot1 in snapshot.children) {
                            val workersData : WorkersData? = snapshot1.getValue(WorkersData::class.java)
                            if (workersData != null) {
                                workersList.add(workersData)
                            }
                        }
                        binding.progressBar.visibility = View.GONE
                        if (workersList.isEmpty()) {
                            binding.noDrafts.visibility = View.VISIBLE
                            binding.itemRecylerView.visibility = View.GONE
                            return
                        }
                        binding.noDrafts.visibility = View.GONE
                        binding.itemRecylerView.visibility = View.VISIBLE

                        val adapter = context?.let { WorkerPostAdapter(it, workersList, savedList) }
                        adapter?.notifyDataSetChanged()
                        binding.itemRecylerView.adapter = adapter
                        val llm = LinearLayoutManager(context)
                        llm.reverseLayout = true
                        llm.stackFromEnd = true
                        binding.itemRecylerView.layoutManager = llm
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
                    }

                })
        }

    }

    private fun clickListener() {
        binding.profileImage.setOnClickListener {
            val intent = Intent(activity, ProfileActivity::class.java)
            intent.putExtra(Constants.NAME, sharedPref.getString(Constants.NAME, "Unknown"))
            intent.putExtra(Constants.IMAGE, sharedPref.getString(Constants.IMAGE, null))
            startActivity(intent)
        }

    }

    private fun init() {

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this.requireActivity())
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        sharedPref = context?.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE)!!
        type = sharedPref.getString(Constants.APPLIED_FOR, null).toString()
        category = Constants.category(type)
        binding.category.text = category
        profileImage = sharedPref.getString(Constants.IMAGE, null)
        Glide.with(this@HomeFragment).load(profileImage).placeholder(R.drawable.user).into(binding.profileImage)
    }

    private fun getLocation(){

            if (ActivityCompat.checkSelfPermission(
                    this.requireActivity(),
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this.requireActivity(),
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this.requireActivity(),
                    arrayOf(
                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION
                    ), 100
                )
                return
            }
        try {
            val location = fusedLocationProviderClient.lastLocation
            location.addOnSuccessListener { it ->
                if (it != null) {
                    val geocoder = context?.let { it1 -> Geocoder(it1, Locale.getDefault()) }
                    val addresses = geocoder?.getFromLocation(it.latitude, it.longitude, 10)
                    if (addresses != null) {
                        if (addresses.isNotEmpty()) {
                            binding.address.text = addresses[0].getAddressLine(0)
                        }
                    }
                }
            }
        }catch (e:Exception){

        }
    }

    override fun onResume() {
        super.onResume()
        if(activity==null){
            return
        }
    }

}