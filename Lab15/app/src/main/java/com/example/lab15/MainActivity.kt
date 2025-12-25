package com.example.lab15

import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    private val items = ArrayList<String>()
    private lateinit var adapter: ArrayAdapter<String>
    private lateinit var dbrw: SQLiteDatabase

    private lateinit var edBook: EditText
    private lateinit var edPrice: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // View
        edBook = findViewById(R.id.edBook)
        edPrice = findViewById(R.id.edPrice)

        // DB
        dbrw = MyDBHelper(this).writableDatabase

        // ListView
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, items)
        findViewById<ListView>(R.id.listView).adapter = adapter

        setListener()
    }

    override fun onDestroy() {
        super.onDestroy()
        dbrw.close()
    }

    private fun setListener() {

        findViewById<Button>(R.id.btnInsert).setOnClickListener {
            val book = edBook.text.toString().trim()
            val price = edPrice.text.toString().trim()

            if (book.isEmpty() || price.isEmpty()) {
                showToast("欄位請勿留空")
                return@setOnClickListener
            }

            try {
                dbrw.execSQL(
                    "INSERT INTO myTable(book, price) VALUES(?, ?)",
                    arrayOf(book, price)
                )
                showToast("新增：$book，價格：$price")
                cleanEditText()
            } catch (e: Exception) {
                showToast("新增失敗：$e")
            }
        }

        findViewById<Button>(R.id.btnUpdate).setOnClickListener {
            val book = edBook.text.toString().trim()
            val price = edPrice.text.toString().trim()

            if (book.isEmpty() || price.isEmpty()) {
                showToast("欄位請勿留空")
                return@setOnClickListener
            }

            try {
                dbrw.execSQL(
                    "UPDATE myTable SET price = ? WHERE book = ?",
                    arrayOf(price, book)
                )
                showToast("更新：$book，價格：$price")
                cleanEditText()
            } catch (e: Exception) {
                showToast("更新失敗：$e")
            }
        }

        findViewById<Button>(R.id.btnDelete).setOnClickListener {
            val book = edBook.text.toString().trim()

            if (book.isEmpty()) {
                showToast("書名請勿留空")
                return@setOnClickListener
            }

            try {
                dbrw.execSQL(
                    "DELETE FROM myTable WHERE book = ?",
                    arrayOf(book)
                )
                showToast("刪除：$book")
                cleanEditText()
            } catch (e: Exception) {
                showToast("刪除失敗：$e")
            }
        }

        findViewById<Button>(R.id.btnQuery).setOnClickListener {
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

            val c = dbrw.rawQuery(sql, args)
            items.clear()

            showToast("共有 ${c.count} 筆資料")

            while (c.moveToNext()) {
                val b = c.getString(0)
                val p = c.getInt(1)
                items.add("書名：$b\t\t\t價格：$p")
            }

            adapter.notifyDataSetChanged()
            c.close()
        }
    }

    private fun showToast(text: String) =
        Toast.makeText(this, text, Toast.LENGTH_LONG).show()

    private fun cleanEditText() {
        edBook.setText("")
        edPrice.setText("")
    }
}
