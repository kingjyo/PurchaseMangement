package com.accompany.purchaseManagement.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit

/**
 * 네트워크 상태 관리 및 안정적인 API 통신을 위한 매니저
 */
class NetworkManager private constructor(private val context: Context) {
    
    companion object {
        private const val TAG = "NetworkManager"
        private const val BASE_URL_PLACEHOLDER = "https://api.example.com/"
        
        @Volatile
        private var INSTANCE: NetworkManager? = null
        
        fun getInstance(context: Context): NetworkManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: NetworkManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    
    private val _isNetworkAvailable = MutableLiveData<Boolean>()
    val isNetworkAvailable: LiveData<Boolean> = _isNetworkAvailable
    
    private val _networkType = MutableLiveData<NetworkType>()
    val networkType: LiveData<NetworkType> = _networkType
    
    // OkHttpClient with retry mechanism
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .addInterceptor(RetryInterceptor(maxRetries = 3))
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = if (android.util.Log.isLoggable(TAG, Log.DEBUG)) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        })
        .build()
    
    // Retrofit instance
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL_PLACEHOLDER)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            Log.d(TAG, "Network available: $network")
            updateNetworkStatus()
        }
        
        override fun onLost(network: Network) {
            Log.d(TAG, "Network lost: $network")
            updateNetworkStatus()
        }
        
        override fun onCapabilitiesChanged(
            network: Network,
            networkCapabilities: NetworkCapabilities
        ) {
            Log.d(TAG, "Network capabilities changed: $network")
            updateNetworkStatus()
        }
    }
    
    enum class NetworkType {
        WIFI,
        CELLULAR,
        ETHERNET,
        NONE
    }
    
    init {
        registerNetworkCallback()
        updateNetworkStatus()
    }
    
    private fun registerNetworkCallback() {
        try {
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .addCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                .build()
            
            connectivityManager.registerNetworkCallback(request, networkCallback)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to register network callback", e)
        }
    }
    
    private fun updateNetworkStatus() {
        scope.launch {
            val isConnected = isNetworkConnected()
            val type = getCurrentNetworkType()
            
            _isNetworkAvailable.postValue(isConnected)
            _networkType.postValue(type)
            
            Log.d(TAG, "Network status updated - Connected: $isConnected, Type: $type")
        }
    }
    
    fun isNetworkConnected(): Boolean {
        return try {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        } catch (e: Exception) {
            Log.e(TAG, "Error checking network connection", e)
            false
        }
    }
    
    private fun getCurrentNetworkType(): NetworkType {
        return try {
            val network = connectivityManager.activeNetwork ?: return NetworkType.NONE
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return NetworkType.NONE
            
            when {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NetworkType.WIFI
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> NetworkType.CELLULAR
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> NetworkType.ETHERNET
                else -> NetworkType.NONE
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error determining network type", e)
            NetworkType.NONE
        }
    }
    
    /**
     * 안전한 API 호출을 위한 래퍼 함수
     */
    suspend fun <T> safeApiCall(
        apiCall: suspend () -> T,
        retryCount: Int = 3,
        delayMs: Long = 1000
    ): ApiResult<T> {
        repeat(retryCount) { attempt ->
            try {
                if (!isNetworkConnected()) {
                    return ApiResult.Error("네트워크 연결을 확인해주세요", null)
                }
                
                val result = apiCall()
                return ApiResult.Success(result)
                
            } catch (e: HttpException) {
                val errorMessage = when (e.code()) {
                    401 -> "인증이 필요합니다"
                    403 -> "접근 권한이 없습니다"
                    404 -> "요청한 리소스를 찾을 수 없습니다"
                    408 -> "요청 시간이 초과되었습니다"
                    429 -> "너무 많은 요청입니다. 잠시 후 다시 시도해주세요"
                    500 -> "서버 오류가 발생했습니다"
                    502, 503, 504 -> "서버에 일시적인 문제가 있습니다"
                    else -> "네트워크 오류가 발생했습니다 (${e.code()})"
                }
                
                Log.e(TAG, "HTTP Error ${e.code()}: ${e.message()}", e)
                
                // 5xx 오류나 타임아웃은 재시도
                if (e.code() >= 500 || e.code() == 408) {
                    if (attempt < retryCount - 1) {
                        delay(delayMs * (attempt + 1))
                        return@repeat
                    }
                }
                
                return ApiResult.Error(errorMessage, e)
                
            } catch (e: SocketTimeoutException) {
                Log.e(TAG, "Socket timeout on attempt ${attempt + 1}", e)
                
                if (attempt < retryCount - 1) {
                    delay(delayMs * (attempt + 1))
                    return@repeat
                }
                
                return ApiResult.Error("연결 시간이 초과되었습니다", e)
                
            } catch (e: IOException) {
                Log.e(TAG, "IO Exception on attempt ${attempt + 1}", e)
                
                if (attempt < retryCount - 1) {
                    delay(delayMs * (attempt + 1))
                    return@repeat
                }
                
                return ApiResult.Error("네트워크 연결에 실패했습니다", e)
                
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error on attempt ${attempt + 1}", e)
                return ApiResult.Error("예기치 않은 오류가 발생했습니다", e)
            }
        }
        
        return ApiResult.Error("최대 재시도 횟수를 초과했습니다", null)
    }
    
    /**
     * Retrofit 서비스 인스턴스 생성
     */
    inline fun <reified T> createService(): T {
        return retrofit.create(T::class.java)
    }
    
    fun unregisterNetworkCallback() {
        try {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to unregister network callback", e)
        }
    }
}

/**
 * API 호출 결과를 나타내는 sealed class
 */
sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val message: String, val exception: Throwable?) : ApiResult<Nothing>()
    
    val isSuccess: Boolean get() = this is Success
    val isError: Boolean get() = this is Error
    
    fun getOrNull(): T? = if (this is Success) data else null
    
    fun exceptionOrNull(): Throwable? = if (this is Error) exception else null
}