package com.codepath.articlesearch

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.codepath.articlesearch.databinding.ActivityMainBinding
import com.codepath.asynchttpclient.AsyncHttpClient
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.Headers
import org.json.JSONException

fun createJson() = Json {
    isLenient = true
    ignoreUnknownKeys = true
    useAlternativeNames = false
}

private const val TAG = "MainActivity/"




class MainActivity : AppCompatActivity() {
    private lateinit var waterRecyclerView: RecyclerView
    private lateinit var binding: ActivityMainBinding
    private val entriesWater = mutableListOf<DisplayWater>()
    private lateinit var waterDao: WaterDao
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        waterDao = AppDatabase.getInstance(this).waterDao()

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        waterRecyclerView = findViewById(R.id.entries)
        val waterAdapter = WaterAdapter(this, entriesWater)
        waterRecyclerView.adapter = waterAdapter

        lifecycleScope.launch {
            (application as WaterApplication).db.waterDao().getAll().collect { databaseList ->
                databaseList.map { entity ->
                    DisplayWater(
                        entity.date,
                        entity.quantity,
                        entity.unit,
                    )
                }.also { mappedList ->
                    entriesWater.clear()
                    entriesWater.addAll(mappedList)
                    waterAdapter.notifyDataSetChanged()
                }
            }
        }

        waterRecyclerView.layoutManager = LinearLayoutManager(this).also {
            val dividerItemDecoration = DividerItemDecoration(this, it.orientation)
            waterRecyclerView.addItemDecoration(dividerItemDecoration)
        }

        val addButton: Button = findViewById(R.id.add_button)
        addButton.setOnClickListener {
            val intent = Intent(this@MainActivity, AddItemActivity::class.java)
            startActivity(intent)
            waterAdapter.notifyDataSetChanged()
        }

        val deleteButton: Button = findViewById(R.id.deleteButton)
        deleteButton.setOnClickListener {
            // Use a coroutine to delete all entries asynchronously
            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    waterDao.deleteAll()
                }
            }
        }



    }
}