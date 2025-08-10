package com.accompany.purchaseManagement.utils

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import android.util.LruCache
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.lang.ref.WeakReference

/**
 * 메모리 관리 및 성능 최적화를 위한 유틸리티 클래스
 */
class MemoryManager private constructor() : LifecycleObserver {
    
    companion object {
        private const val TAG = "MemoryManager"
        private const val MAX_IMAGE_SIZE = 1024 * 1024 // 1MB
        private const val CACHE_SIZE = 50 // 최대 50개 이미지 캐시
        
        @Volatile
        private var INSTANCE: MemoryManager? = null
        
        fun getInstance(): MemoryManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: MemoryManager().also { INSTANCE = it }
            }
        }
    }
    
    // 이미지 캐시
    private val imageCache = object : LruCache<String, Bitmap>(CACHE_SIZE) {
        override fun sizeOf(key: String, bitmap: Bitmap): Int {
            return bitmap.byteCount / 1024 // KB 단위
        }
        
        override fun entryRemoved(evicted: Boolean, key: String, oldValue: Bitmap, newValue: Bitmap?) {
            if (evicted && !oldValue.isRecycled) {
                Log.d(TAG, "Bitmap evicted from cache: $key")
            }
        }
    }
    
    // Activity 참조 관리
    private val activityReferences = mutableSetOf<WeakReference<Activity>>()
    private val fragmentReferences = mutableSetOf<WeakReference<Fragment>>()
    private val coroutineJobs = mutableSetOf<Job>()
    
    /**
     * Activity 등록 (메모리 누수 추적용)
     */
    fun registerActivity(activity: Activity) {
        cleanupDeadReferences()
        activityReferences.add(WeakReference(activity))
        Log.d(TAG, "Activity registered: ${activity::class.simpleName}")
    }
    
    /**
     * Fragment 등록 (메모리 누수 추적용)
     */
    fun registerFragment(fragment: Fragment) {
        cleanupDeadReferences()
        fragmentReferences.add(WeakReference(fragment))
        Log.d(TAG, "Fragment registered: ${fragment::class.simpleName}")
    }
    
    /**
     * 코루틴 Job 등록 (취소 관리용)
     */
    fun registerJob(job: Job) {
        coroutineJobs.add(job)
    }
    
    /**
     * 죽은 참조들 정리
     */
    private fun cleanupDeadReferences() {
        activityReferences.removeAll { it.get() == null }
        fragmentReferences.removeAll { it.get() == null }
        coroutineJobs.removeAll { it.isCompleted || it.isCancelled }
    }
    
    /**
     * 현재 메모리 사용량 확인
     */
    fun getCurrentMemoryUsage(context: Context): MemoryInfo {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        
        val runtime = Runtime.getRuntime()
        val totalMemory = runtime.totalMemory()
        val freeMemory = runtime.freeMemory()
        val usedMemory = totalMemory - freeMemory
        val maxMemory = runtime.maxMemory()
        
        return MemoryInfo(
            totalMemory = totalMemory,
            usedMemory = usedMemory,
            freeMemory = freeMemory,
            maxMemory = maxMemory,
            availableSystemMemory = memoryInfo.availMem,
            totalSystemMemory = memoryInfo.totalMem,
            isLowMemory = memoryInfo.lowMemory
        )
    }
    
    /**
     * 메모리 사용량 로그
     */
    fun logMemoryUsage(context: Context, tag: String = TAG) {
        val memInfo = getCurrentMemoryUsage(context)
        Log.d(tag, """
            Memory Usage:
            - Used: ${memInfo.usedMemory / 1024 / 1024} MB
            - Free: ${memInfo.freeMemory / 1024 / 1024} MB
            - Max: ${memInfo.maxMemory / 1024 / 1024} MB
            - System Available: ${memInfo.availableSystemMemory / 1024 / 1024} MB
            - Low Memory: ${memInfo.isLowMemory}
        """.trimIndent())
    }
    
    /**
     * 이미지 캐시에서 비트맵 가져오기
     */
    fun getBitmapFromCache(key: String): Bitmap? {
        return imageCache.get(key)
    }
    
    /**
     * 이미지 캐시에 비트맵 저장
     */
    fun putBitmapToCache(key: String, bitmap: Bitmap) {
        if (bitmap.byteCount < MAX_IMAGE_SIZE) {
            imageCache.put(key, bitmap)
            Log.d(TAG, "Bitmap cached: $key")
        } else {
            Log.w(TAG, "Bitmap too large to cache: $key (${bitmap.byteCount} bytes)")
        }
    }
    
    /**
     * 안전한 비트맵 로딩 (메모리 효율적)
     */
    suspend fun loadBitmapSafely(
        context: Context,
        uri: Uri,
        maxWidth: Int = 800,
        maxHeight: Int = 600
    ): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val key = uri.toString()
            
            // 캐시에서 먼저 확인
            getBitmapFromCache(key)?.let { return@withContext it }
            
            // 이미지 크기 먼저 확인
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            
            context.contentResolver.openInputStream(uri)?.use { input ->
                BitmapFactory.decodeStream(input, null, options)
            }
            
            // 샘플링 비율 계산
            options.inSampleSize = calculateInSampleSize(options, maxWidth, maxHeight)
            options.inJustDecodeBounds = false
            options.inPreferredConfig = Bitmap.Config.RGB_565 // 메모리 절약
            
            // 비트맵 로드
            val bitmap = context.contentResolver.openInputStream(uri)?.use { input ->
                BitmapFactory.decodeStream(input, null, options)
            }
            
            bitmap?.let {
                putBitmapToCache(key, it)
                Log.d(TAG, "Bitmap loaded safely: ${it.width}x${it.height}")
            }
            
            bitmap
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load bitmap safely", e)
            null
        }
    }
    
    /**
     * 샘플링 비율 계산
     */
    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1
        
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            
            while ((halfHeight / inSampleSize) >= reqHeight && 
                   (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }
        
        return inSampleSize
    }
    
    /**
     * 임시 파일 정리
     */
    fun cleanupTempFiles(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val tempDir = context.cacheDir
                val externalCacheDir = context.externalCacheDir
                
                cleanupDirectory(tempDir)
                externalCacheDir?.let { cleanupDirectory(it) }
                
                Log.d(TAG, "Temp files cleaned up")
                
            } catch (e: Exception) {
                Log.e(TAG, "Failed to cleanup temp files", e)
            }
        }
    }
    
    private fun cleanupDirectory(directory: File) {
        if (!directory.exists()) return
        
        val files = directory.listFiles() ?: return
        val now = System.currentTimeMillis()
        val oneWeekAgo = now - (7 * 24 * 60 * 60 * 1000) // 1주일 전
        
        files.forEach { file ->
            if (file.isFile && file.lastModified() < oneWeekAgo) {
                if (file.delete()) {
                    Log.d(TAG, "Deleted old temp file: ${file.name}")
                }
            }
        }
    }
    
    /**
     * 메모리 정리 (강제 GC 호출)
     */
    fun forceGarbageCollection() {
        System.gc()
        Log.d(TAG, "Forced garbage collection")
    }
    
    /**
     * 이미지 캐시 정리
     */
    fun clearImageCache() {
        imageCache.evictAll()
        Log.d(TAG, "Image cache cleared")
    }
    
    /**
     * 모든 리소스 정리
     */
    fun cleanup() {
        clearImageCache()
        
        // 진행 중인 코루틴 취소
        coroutineJobs.forEach { job ->
            if (!job.isCompleted) {
                job.cancel()
            }
        }
        coroutineJobs.clear()
        
        // 참조 정리
        activityReferences.clear()
        fragmentReferences.clear()
        
        Log.d(TAG, "All resources cleaned up")
    }
    
    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        cleanup()
    }
    
    /**
     * 메모리 정보를 담는 데이터 클래스
     */
    data class MemoryInfo(
        val totalMemory: Long,
        val usedMemory: Long,
        val freeMemory: Long,
        val maxMemory: Long,
        val availableSystemMemory: Long,
        val totalSystemMemory: Long,
        val isLowMemory: Boolean
    )
}