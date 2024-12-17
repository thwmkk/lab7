package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView

class WorkoutRating : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var workoutAdapter: WorkoutRecyclerAdapter
    private lateinit var workouts: List<Workout>
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rating)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        supportActionBar?.title = ""
        setSupportActionBar(toolbar)

        bottomNavigationView = findViewById(R.id.bottomNavigationView)
        bottomNavigationView.selectedItemId = R.id.bottom_rating
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.bottom_main -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }

        // Инициализация базы данных
        dbHelper = DatabaseHelper(this)

        // Инициализация RecyclerView
        recyclerView = findViewById(R.id.recyclerViewWorkouts)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Загружаем тренировки с оценками
        loadWorkoutsWithRatings()
    }

    private fun loadWorkoutsWithRatings() {
        workouts = dbHelper.getAllMainWorkoutsWithRatings() // Получаем все тренировки с оценками

        workoutAdapter = WorkoutRecyclerAdapter(this, workouts) { workout ->
            showWorkoutDetails(workout)
        }

        recyclerView.adapter = workoutAdapter
    }

    private fun showWorkoutDetails(workout: Workout) {
        // Здесь можно открыть новое окно или показать Toast с дополнительной информацией
        Toast.makeText(this, workout.description, Toast.LENGTH_SHORT).show()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_main -> {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()
        loadWorkoutsWithRatings() // Перезагружаем данные при возвращении
    }
}
