package com.example.lab17

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.gson.Gson
import okhttp3.*
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var btnQuery: Button

    private val client: OkHttpClient by lazy { OkHttpClient() }
    private val gson: Gson by lazy { Gson() }

    private val url = "https://api.italkutalk.com/api/air"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        btnQuery = findViewById(R.id.btnQuery)
        btnQuery.setOnClickListener {
            btnQuery.isEnabled = false
            requestAir()
        }
    }

    private fun requestAir() {
        val req = Request.Builder().url(url).build()

        client.newCall(req).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                response.use { res ->
                    if (!res.isSuccessful) {
                        uiFail("HTTP 錯誤：${res.code}")
                        return
                    }

                    val json = res.body?.string()
                    if (json.isNullOrBlank()) {
                        uiFail("回傳資料為空")
                        return
                    }

                    try {
                        val myObject = gson.fromJson(json, MyObject::class.java)
                        showResult(myObject)
                    } catch (e: Exception) {
                        uiFail("解析失敗：$e")
                    }
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                uiFail("查詢失敗：$e")
            }
        })
    }

    private fun showResult(myObject: MyObject) {
        val list = myObject.result.records
            .map { "地區：${it.SiteName}, 狀態：${it.Status}" }

        runOnUiThread {
            btnQuery.isEnabled = true
            if (list.isEmpty()) {
                Toast.makeText(this, "沒有資料", Toast.LENGTH_SHORT).show()
                return@runOnUiThread
            }
            AlertDialog.Builder(this)
                .setTitle("臺北市空氣品質")
                .setItems(list.toTypedArray(), null)
                .setPositiveButton("OK", null)
                .show()
        }
    }

    private fun uiFail(msg: String) {
        runOnUiThread {
            btnQuery.isEnabled = true
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }
    }
}
