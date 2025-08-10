package com.accompany.purchaseManagement.network

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import java.net.SocketTimeoutException

/**
 * 네트워크 요청 재시도를 위한 Interceptor
 */
class RetryInterceptor(private val maxRetries: Int = 3) : Interceptor {
    
    companion object {
        private const val TAG = "RetryInterceptor"
    }
    
    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        var response: Response? = null
        var exception: IOException? = null
        
        for (i in 0 until maxRetries) {
            try {
                response?.close() // 이전 응답 리소스 정리
                response = chain.proceed(request)
                
                // 성공적인 응답이거나 재시도하지 않을 오류 코드
                if (response.isSuccessful || !shouldRetry(response.code)) {
                    return response
                }
                
                Log.w(TAG, "Request failed with code ${response.code}, attempt ${i + 1}/$maxRetries")
                
            } catch (e: SocketTimeoutException) {
                exception = e
                Log.w(TAG, "Socket timeout on attempt ${i + 1}/$maxRetries", e)
                
            } catch (e: IOException) {
                exception = e
                Log.w(TAG, "IO Exception on attempt ${i + 1}/$maxRetries", e)
                
                // 네트워크 관련 오류가 아니면 재시도하지 않음
                if (!isNetworkError(e)) {
                    throw e
                }
            }
            
            // 마지막 시도가 아니면 잠시 대기
            if (i < maxRetries - 1) {
                try {
                    Thread.sleep(calculateDelay(i))
                } catch (e: InterruptedException) {
                    Thread.currentThread().interrupt()
                    throw IOException("Interrupted during retry delay", e)
                }
            }
        }
        
        // 모든 재시도 실패
        response?.let { return it }
        exception?.let { throw it }
        
        throw IOException("Max retries exceeded")
    }
    
    /**
     * 재시도할 HTTP 상태 코드인지 확인
     */
    private fun shouldRetry(code: Int): Boolean {
        return when (code) {
            408, // Request Timeout
            429, // Too Many Requests
            500, // Internal Server Error
            502, // Bad Gateway
            503, // Service Unavailable
            504  // Gateway Timeout
            -> true
            else -> false
        }
    }
    
    /**
     * 네트워크 관련 오류인지 확인
     */
    private fun isNetworkError(exception: IOException): Boolean {
        return exception is SocketTimeoutException ||
                exception.message?.contains("timeout", ignoreCase = true) == true ||
                exception.message?.contains("connection", ignoreCase = true) == true ||
                exception.message?.contains("network", ignoreCase = true) == true
    }
    
    /**
     * 재시도 간격 계산 (exponential backoff)
     */
    private fun calculateDelay(attempt: Int): Long {
        return minOf(1000L * (1L shl attempt), 10000L) // 최대 10초
    }
}