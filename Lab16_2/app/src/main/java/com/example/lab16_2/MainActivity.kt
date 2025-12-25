package com.example.lab16_2

import android.content.ContentValues
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    private val items = arrayListOf<String>()
    private lateinit var adapter: ArrayAdapter<String>

    private lateinit var edBook: EditText
    private lateinit var edPrice: EditText

    // Provider Uri（要和你 Manifest 的 authorities 一致）
    private val uri: Uri = Uri.parse("content://com.example.lab16")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        edBook = findViewById(R.id.edBook)
        edPrice = findViewById(R.id.edPrice)

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, items)
        findViewById<ListView>(R.id.listView).adapter = adapter

        setListener()
    }

    private fun setListener() {
        findViewById<Button>(R.id.btnInsert).setOnClickListener { insertBook() }
        findViewById<Button>(R.id.btnUpdate).setOnClickListener { updateBook() }
        findViewById<Button>(R.id.btnDelete).setOnClickListener { deleteBook() }
        findViewById<Button>(R.id.btnQuery).setOnClickListener { queryBooks() }
    }

    private fun insertBook() {
        val name = edBook.text.toString().trim()
        val price = edPrice.text.toString().trim().toIntOrNull()

        if (name.isEmpty() || price == null) {
            showToast("書名不可空、價格需為數字")
            return
        }

        val values = ContentValues().apply {
            put("book", name)
            put("price", price)
        }

        try {
            val contentUri = contentResolver.insert(uri, values)
            if (contentUri != null) {
                showToast("新增：$name，價格：$price")
                cleanEditText()
            } else {
                showToast("新增失敗")
            }
        } catch (e: Exception) {
            showToast("新增失敗：${e.message}")
        }
    }

    private fun updateBook() {
        val name = edBook.text.toString().trim()
        val price = edPrice.text.toString().trim().toIntOrNull()

        if (name.isEmpty() || price == null) {
            showToast("書名不可空、價格需為數字")
            return
        }

        val values = ContentValues().apply { put("price", price) }

        try {
            val count = contentResolver.update(uri, values, name, null)
            if (count > 0) {
                showToast("更新：$name，價格：$price")
                cleanEditText()
            } else {
                showToast("更新失敗（找不到書名）")
            }
        } catch (e: Exception) {
            showToast("更新失敗：${e.message}")
        }
    }

    private fun deleteBook() {
        val name = edBook.text.toString().trim()
        if (name.isEmpty()) {
            showToast("書名請勿留空")
            return
        }

        try {
            val count = contentResolver.delete(uri, name, null)
            if (count > 0) {
                showToast("刪除：$name")
                cleanEditText()
            } else {
                showToast("刪除失敗（找不到書名）")
            }
        } catch (e: Exception) {
            showToast("刪除失敗：${e.message}")
        }
    }

    private fun queryBooks() {
        val name = edBook.text.toString().trim()
        val selection = name.ifEmpty { null }

        items.clear()

        try {
            contentResolver.query(uri, null, selection, null, null)?.use { c ->
                showToast("共有 ${c.count} 筆資料")
                while (c.moveToNext()) {
                    val book = c.getString(0)
                    val price = c.getInt(1)
                    items.add("書名：$book\t\t\t\t價格：$price")
                }
            } ?: showToast("查詢失敗（Cursor 為 null）")

            adapter.notifyDataSetChanged()
        } catch (e: Exception) {
            showToast("查詢失敗：${e.message}")
        }
    }

    private fun showToast(text: String) =
        Toast.makeText(this, text, Toast.LENGTH_LONG).show()

    private fun cleanEditText() {
        edBook.setText("")
        edPrice.setText("")
    }
}
