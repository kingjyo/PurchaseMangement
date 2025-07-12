package com.accompany.purchaseManagement

import android.os.Bundle
import android.widget.EditText
import android.widget.ListView
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*
import android.widget.Toast


class CattleStatusActivity : AppCompatActivity() {
    private lateinit var adapter: CattleAdapter
    private val allList = mutableListOf<Cattle>() // 전체 데이터
    private val displayList = mutableListOf<Cattle>() // 검색된 데이터

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cattle_status)

        val listView = findViewById<ListView>(R.id.cattle_listview)
        val searchInput = findViewById<EditText>(R.id.search_input)

        adapter = CattleAdapter(this, displayList, null)
        listView.adapter = adapter

        // 검색 기능
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val q = s?.toString()?.trim()?.lowercase() ?: ""
                displayList.clear()
                if (q.isBlank()) {
                    displayList.addAll(allList)
                } else {
                    displayList.addAll(allList.filter {
                        it.tagNumber.contains(q, true)      // 개체번호
                                || it.id.contains(q, true)          // 관리번호
                                || it.eartagNumber?.contains(q, true) == true // 이표번호
                    })
                }
                adapter.notifyDataSetChanged()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // 네트워크 데이터 불러오기 (코루틴)
        GlobalScope.launch(Dispatchers.Main) {
            try {
                val cattleList = CattleStatusRetrofit.api.getCattleList()
                allList.clear()
                allList.addAll(cattleList)
                displayList.clear()
                displayList.addAll(cattleList)
                adapter.notifyDataSetChanged()
            } catch (e: Exception) {
                Toast.makeText(this@CattleStatusActivity, "불러오기 실패: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}