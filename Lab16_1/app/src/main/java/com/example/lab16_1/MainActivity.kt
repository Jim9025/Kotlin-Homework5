package com.example.lab16_1

import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    private lateinit var dbrw: SQLiteDatabase
    private val items = ArrayList<String>()
    private lateinit var adapter: ArrayAdapter<String>

    private lateinit var edBook: EditText
    private lateinit var edPrice: EditText
    private lateinit var listView: ListView
    private lateinit var btnInsert: Button
    private lateinit var btnUpdate: Button
    private lateinit var btnDelete: Button
    private lateinit var btnQuery: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        initDb()
        initList()
        setListeners()
    }

    override fun onDestroy() {
        super.onDestroy()
        dbrw.close()
    }

    private fun initViews() {
        edBook = findViewById(R.id.edBook)
        edPrice = findViewById(R.id.edPrice)
        listView = findViewById(R.id.listView)

        btnInsert = findViewById(R.id.btnInsert)
        btnUpdate = findViewById(R.id.btnUpdate)
        btnDelete = findViewById(R.id.btnDelete)
        btnQuery = findViewById(R.id.btnQuery)
    }

    private fun initDb() {
        dbrw = MyDBHelper(this).writableDatabase
    }

    private fun initList() {
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, items)
        listView.adapter = adapter
    }

    private fun setListeners() {
        btnInsert.setOnClickListener { insertBook() }
        btnUpdate.setOnClickListener { updateBook() }
        btnDelete.setOnClickListener { deleteBook() }
        btnQuery.setOnClickListener { queryBooks() }
    }

    private fun insertBook() {
        val book = edBook.text.toString().trim()
        val priceStr = edPrice.text.toString().trim()

        if (book.isEmpty() || priceStr.isEmpty()) {
            showToast("欄位請勿留空")
            return
        }

        val price = priceStr.toIntOrNull()
        if (price == null) {
            showToast("價格必須是數字")
            return
        }

        runCatching {
            dbrw.execSQL(
                "INSERT INTO myTable(book, price) VALUES(?, ?)",
                arrayOf(book, price)
            )
        }.onSuccess {
            showToast("新增：$book，價格：$price")
            clearInputs()
        }.onFailure { e ->
            showToast("新增失敗：${e.message}")
        }
    }

    private fun updateBook() {
        val book = edBook.text.toString().trim()
        val priceStr = edPrice.text.toString().trim()

        if (book.isEmpty() || priceStr.isEmpty()) {
            showToast("欄位請勿留空")
            return
        }

        val price = priceStr.toIntOrNull()
        if (price == null) {
            showToast("價格必須是數字")
            return
        }

        runCatching {
            // 參數化更新（避免拼接出錯）
            dbrw.execSQL(
                "UPDATE myTable SET price = ? WHERE book = ?",
                arrayOf(price, book)
            )
        }.onSuccess {
            showToast("更新：$book，價格：$price")
            clearInputs()
        }.onFailure { e ->
            showToast("更新失敗：${e.message}")
        }
    }

    private fun deleteBook() {
        val book = edBook.text.toString().trim()
        if (book.isEmpty()) {
            showToast("書名請勿留空")
            return
        }

        runCatching {
            // 參數化刪除
            dbrw.execSQL(
                "DELETE FROM myTable WHERE book = ?",
                arrayOf(book)
            )
        }.onSuccess {
            showToast("刪除：$book")
            clearInputs()
        }.onFailure { e ->
            showToast("刪除失敗：${e.message}")
        }
    }

    private fun queryBooks() {
        val book = edBook.text.toString().trim()

        val sql: String
        val args: Array<String>?

        if (book.isEmpty()) {
            sql = "SELECT book, price FROM myTable"
            args = null
        } else {
            sql = "SELECT book, price FROM myTable WHERE book = ?"
            args = arrayOf(book)
        }

        items.clear()

        dbrw.rawQuery(sql, args).use { c ->
            showToast("共有 ${c.count} 筆資料")

            if (c.count == 0) {
                adapter.notifyDataSetChanged()
                return
            }

            while (c.moveToNext()) {
                val b = c.getString(0)
                val p = c.getInt(1)
                items.add("書名:$b\t\t\t\t價格:$p")
            }
        }

        adapter.notifyDataSetChanged()
    }

    private fun showToast(text: String) {
        Toast.makeText(this, text, Toast.LENGTH_LONG).show()
    }

    private fun clearInputs() {
        edBook.setText("")
        edPrice.setText("")
    }
}
