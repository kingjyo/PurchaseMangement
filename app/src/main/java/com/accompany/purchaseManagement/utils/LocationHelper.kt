package com.accompany.purchaseManagement.utils

import android.util.Log
import com.accompany.purchaseManagement.PurchaseManagementApp
import com.accompany.purchaseManagement.data.models.Location
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * 지점/현장 관리를 위한 헬퍼 클래스
 */
class LocationHelper {
    
    companion object {
        private const val TAG = "LocationHelper"
        private const val LOCATIONS_COLLECTION = "locations"
        
        @Volatile
        private var INSTANCE: LocationHelper? = null
        
        fun getInstance(): LocationHelper {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: LocationHelper().also { INSTANCE = it }
            }
        }
    }
    
    private val firestore: FirebaseFirestore = PurchaseManagementApp.firestore
    
    /**
     * 지점 정보 저장
     */
    suspend fun saveLocation(location: Location): Result<String> = withContext(Dispatchers.IO) {
        try {
            val locationRef = if (location.id.isNotEmpty()) {
                firestore.collection(LOCATIONS_COLLECTION).document(location.id)
            } else {
                firestore.collection(LOCATIONS_COLLECTION).document()
            }
            
            val locationWithId = location.copy(id = locationRef.id)
            locationRef.set(locationWithId.toFirestoreMap()).await()
            
            Log.d(TAG, "Location saved successfully: ${locationRef.id}")
            Result.success(locationRef.id)
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save location", e)
            Result.failure(e)
        }
    }
    
    /**
     * 지점 정보 조회
     */
    suspend fun getLocation(locationId: String): Result<Location?> = withContext(Dispatchers.IO) {
        try {
            val document = firestore.collection(LOCATIONS_COLLECTION)
                .document(locationId)
                .get()
                .await()
            
            if (document.exists()) {
                val location = document.toObject(Location::class.java)
                Log.d(TAG, "Location retrieved successfully: $locationId")
                Result.success(location)
            } else {
                Log.d(TAG, "Location not found: $locationId")
                Result.success(null)
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get location: $locationId", e)
            Result.failure(e)
        }
    }
    
    /**
     * 모든 활성 지점 목록 조회
     */
    suspend fun getAllActiveLocations(): Result<List<Location>> = withContext(Dispatchers.IO) {
        try {
            val querySnapshot = firestore.collection(LOCATIONS_COLLECTION)
                .whereEqualTo("isActive", true)
                .orderBy("name")
                .get()
                .await()
            
            val locations = querySnapshot.documents.mapNotNull { it.toObject(Location::class.java) }
            Log.d(TAG, "Retrieved ${locations.size} active locations")
            Result.success(locations)
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get active locations", e)
            Result.failure(e)
        }
    }
    
    /**
     * 모든 지점 목록 조회 (비활성 포함)
     */
    suspend fun getAllLocations(): Result<List<Location>> = withContext(Dispatchers.IO) {
        try {
            val querySnapshot = firestore.collection(LOCATIONS_COLLECTION)
                .orderBy("name")
                .get()
                .await()
            
            val locations = querySnapshot.documents.mapNotNull { it.toObject(Location::class.java) }
            Log.d(TAG, "Retrieved ${locations.size} locations")
            Result.success(locations)
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get all locations", e)
            Result.failure(e)
        }
    }
    
    /**
     * 지역별 지점 조회
     */
    suspend fun getLocationsByRegion(region: String): Result<List<Location>> = withContext(Dispatchers.IO) {
        try {
            val querySnapshot = firestore.collection(LOCATIONS_COLLECTION)
                .whereEqualTo("region", region)
                .whereEqualTo("isActive", true)
                .orderBy("name")
                .get()
                .await()
            
            val locations = querySnapshot.documents.mapNotNull { it.toObject(Location::class.java) }
            Log.d(TAG, "Retrieved ${locations.size} locations for region: $region")
            Result.success(locations)
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get locations by region: $region", e)
            Result.failure(e)
        }
    }
    
    /**
     * 지점 코드로 조회
     */
    suspend fun getLocationByCode(code: String): Result<Location?> = withContext(Dispatchers.IO) {
        try {
            val querySnapshot = firestore.collection(LOCATIONS_COLLECTION)
                .whereEqualTo("code", code)
                .limit(1)
                .get()
                .await()
            
            val location = querySnapshot.documents.firstOrNull()?.toObject(Location::class.java)
            Log.d(TAG, "Location retrieved by code: $code")
            Result.success(location)
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get location by code: $code", e)
            Result.failure(e)
        }
    }
    
    /**
     * 지점 정보 업데이트
     */
    suspend fun updateLocation(location: Location): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val updates = location.toFirestoreMap()
            
            firestore.collection(LOCATIONS_COLLECTION)
                .document(location.id)
                .update(updates)
                .await()
            
            Log.d(TAG, "Location updated successfully: ${location.id}")
            Result.success(Unit)
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update location: ${location.id}", e)
            Result.failure(e)
        }
    }
    
    /**
     * 지점 담당자 추가
     */
    suspend fun addSecondaryManager(
        locationId: String,
        managerEmail: String,
        managerName: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val location = getLocation(locationId).getOrNull()
            if (location == null) {
                return@withContext Result.failure(Exception("Location not found"))
            }
            
            val updatedEmails = location.secondaryManagerEmails.toMutableList()
            val updatedNames = location.secondaryManagerNames.toMutableList()
            
            if (!updatedEmails.contains(managerEmail)) {
                updatedEmails.add(managerEmail)
                updatedNames.add(managerName)
                
                firestore.collection(LOCATIONS_COLLECTION)
                    .document(locationId)
                    .update(
                        mapOf(
                            "secondaryManagerEmails" to updatedEmails,
                            "secondaryManagerNames" to updatedNames,
                            "updatedAt" to System.currentTimeMillis()
                        )
                    )
                    .await()
                
                Log.d(TAG, "Secondary manager added: $managerEmail to location $locationId")
                Result.success(Unit)
            } else {
                Result.failure(Exception("Manager already exists"))
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to add secondary manager", e)
            Result.failure(e)
        }
    }
    
    /**
     * 지점 상급자 추가
     */
    suspend fun addSupervisor(
        locationId: String,
        supervisorEmail: String,
        supervisorName: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val location = getLocation(locationId).getOrNull()
            if (location == null) {
                return@withContext Result.failure(Exception("Location not found"))
            }
            
            val updatedEmails = location.supervisorEmails.toMutableList()
            val updatedNames = location.supervisorNames.toMutableList()
            
            if (!updatedEmails.contains(supervisorEmail)) {
                updatedEmails.add(supervisorEmail)
                updatedNames.add(supervisorName)
                
                firestore.collection(LOCATIONS_COLLECTION)
                    .document(locationId)
                    .update(
                        mapOf(
                            "supervisorEmails" to updatedEmails,
                            "supervisorNames" to updatedNames,
                            "updatedAt" to System.currentTimeMillis()
                        )
                    )
                    .await()
                
                Log.d(TAG, "Supervisor added: $supervisorEmail to location $locationId")
                Result.success(Unit)
            } else {
                Result.failure(Exception("Supervisor already exists"))
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to add supervisor", e)
            Result.failure(e)
        }
    }
    
    /**
     * 지점 비활성화
     */
    suspend fun deactivateLocation(locationId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            firestore.collection(LOCATIONS_COLLECTION)
                .document(locationId)
                .update(
                    mapOf(
                        "isActive" to false,
                        "updatedAt" to System.currentTimeMillis()
                    )
                )
                .await()
            
            Log.d(TAG, "Location deactivated: $locationId")
            Result.success(Unit)
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to deactivate location: $locationId", e)
            Result.failure(e)
        }
    }
    
    /**
     * 지점 삭제 (실제로는 비활성화)
     */
    suspend fun deleteLocation(locationId: String): Result<Unit> {
        return deactivateLocation(locationId)
    }
}
