package com.accompany.purchaseManagement

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.accompany.purchaseManagement.data.models.Location
import com.accompany.purchaseManagement.utils.LocationHelper
import kotlinx.coroutines.launch

/**
 * 사용자가 자신의 지점을 선택하는 Activity
 * 프로필 설정 또는 로그인 후 최초 1회 실행
 */
class LocationSelectionActivity : AppCompatActivity() {
    
    companion object {
        private const val TAG = "LocationSelectionActivity"
    }
    
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvNoLocations: TextView
    private lateinit var btnSkip: Button
    
    private val locationHelper = LocationHelper.getInstance()
    private val locations = mutableListOf<Location>()
    private lateinit var adapter: LocationAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location_selection)
        
        supportActionBar?.apply {
            title = "지점 선택"
            setDisplayHomeAsUpEnabled(true)
        }
        
        initializeViews()
        setupRecyclerView()
        loadLocations()
    }
    
    private fun initializeViews() {
        recyclerView = findViewById(R.id.recyclerViewLocations)
        progressBar = findViewById(R.id.progressBar)
        tvNoLocations = findViewById(R.id.tvNoLocations)
        btnSkip = findViewById(R.id.btnSkip)
        
        btnSkip.setOnClickListener {
            // 지점 선택하지 않고 계속 진행
            finish()
        }
    }
    
    private fun setupRecyclerView() {
        adapter = LocationAdapter(locations) { location ->
            onLocationSelected(location)
        }
        
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }
    
    private fun loadLocations() {
        progressBar.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        tvNoLocations.visibility = View.GONE
        
        lifecycleScope.launch {
            try {
                val result = locationHelper.getAllActiveLocations()
                
                if (result.isSuccess) {
                    val locationList = result.getOrNull() ?: emptyList()
                    
                    if (locationList.isNotEmpty()) {
                        locations.clear()
                        locations.addAll(locationList)
                        adapter.notifyDataSetChanged()
                        
                        recyclerView.visibility = View.VISIBLE
                        tvNoLocations.visibility = View.GONE
                    } else {
                        recyclerView.visibility = View.GONE
                        tvNoLocations.visibility = View.VISIBLE
                    }
                } else {
                    Toast.makeText(
                        this@LocationSelectionActivity,
                        "지점 목록을 불러올 수 없습니다: ${result.exceptionOrNull()?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    tvNoLocations.visibility = View.VISIBLE
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Error loading locations", e)
                Toast.makeText(
                    this@LocationSelectionActivity,
                    "오류가 발생했습니다: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                tvNoLocations.visibility = View.VISIBLE
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }
    
    private fun onLocationSelected(location: Location) {
        progressBar.visibility = View.VISIBLE
        
        lifecycleScope.launch {
            try {
                // 현재 사용자 정보 가져오기
                val prefs = getSharedPreferences(PurchaseManagementApp.PREFS_NAME, MODE_PRIVATE)
                val userId = prefs.getString(PurchaseManagementApp.KEY_USER_ID, null)
                
                if (userId != null) {
                    // Firestore에서 사용자 정보 업데이트
                    val updates = mapOf(
                        "locationId" to location.id,
                        "locationName" to location.name,
                        "updatedAt" to System.currentTimeMillis()
                    )
                    
                    PurchaseManagementApp.firestore
                        .collection(PurchaseManagementApp.USERS_COLLECTION)
                        .document(userId)
                        .update(updates)
                        .addOnSuccessListener {
                            // SharedPreferences에도 저장
                            prefs.edit().apply {
                                putString("locationId", location.id)
                                putString("locationName", location.name)
                                apply()
                            }
                            
                            Toast.makeText(
                                this@LocationSelectionActivity,
                                "${location.name} 지점이 선택되었습니다",
                                Toast.LENGTH_SHORT
                            ).show()
                            
                            finish()
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "Failed to update user location", e)
                            Toast.makeText(
                                this@LocationSelectionActivity,
                                "지점 설정에 실패했습니다: ${e.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                } else {
                    Toast.makeText(
                        this@LocationSelectionActivity,
                        "사용자 정보를 찾을 수 없습니다",
                        Toast.LENGTH_LONG
                    ).show()
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Error saving location selection", e)
                Toast.makeText(
                    this@LocationSelectionActivity,
                    "오류가 발생했습니다: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
    
    /**
     * RecyclerView 어댑터
     */
    private class LocationAdapter(
        private val locations: List<Location>,
        private val onLocationClick: (Location) -> Unit
    ) : RecyclerView.Adapter<LocationAdapter.LocationViewHolder>() {
        
        override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): LocationViewHolder {
            val view = android.view.LayoutInflater.from(parent.context)
                .inflate(android.R.layout.simple_list_item_2, parent, false)
            return LocationViewHolder(view)
        }
        
        override fun onBindViewHolder(holder: LocationViewHolder, position: Int) {
            holder.bind(locations[position], onLocationClick)
        }
        
        override fun getItemCount(): Int = locations.size
        
        class LocationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val text1: TextView = itemView.findViewById(android.R.id.text1)
            private val text2: TextView = itemView.findViewById(android.R.id.text2)
            
            fun bind(location: Location, onClick: (Location) -> Unit) {
                text1.text = location.name
                text2.text = "${location.region} | ${location.code}"
                
                itemView.setOnClickListener {
                    onClick(location)
                }
            }
        }
    }
}
