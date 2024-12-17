package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.FileReader
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

class MainActivity : AppCompatActivity(), OnAddClickListener {

    private lateinit var adapter: WorkoutAdapter
    private var workouts: MutableList<Workout> = mutableListOf()
    private lateinit var recyclerViewWorkouts: RecyclerView
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dbHelper = DatabaseHelper(this)
        workouts = dbHelper.getAllMainWorkouts().toMutableList()

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        supportActionBar?.title = ""
        setSupportActionBar(toolbar)

        bottomNavigationView = findViewById(R.id.bottomNavigationView)
        bottomNavigationView.selectedItemId = R.id.bottom_main

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.bottom_main -> true
                R.id.bottom_rating -> {
                    if (workouts.isNotEmpty()) {
                        openWorkoutRating() // Измените на нужный индекс или логику
                    }
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    true
                }
                R.id.bottom_add -> {
                    openMyWorkoutsActivity()
                    true
                }
                R.id.bottom_return -> {
                    loadWorkoutsFromBinaryFile()
                    Toast.makeText(this, "Состояние восстановлено", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }

        recyclerViewWorkouts = findViewById(R.id.recyclerViewWorkouts)
        val editTextSearch: EditText = findViewById(R.id.editTextSearch)
        val buttonSearch: Button = findViewById(R.id.buttonSearch)

        // Обновленный вызов конструктора адаптера
        adapter = WorkoutAdapter(this, workouts, { selectedWorkout ->
            openWorkoutRating()
        }, {
            showAddWorkoutDialog()
        }, this, dbHelper) // Передаем dbHelper

        recyclerViewWorkouts.layoutManager = LinearLayoutManager(this)
        recyclerViewWorkouts.adapter = adapter

        buttonSearch.setOnClickListener {
            val query = editTextSearch.text.toString().lowercase().trim()
            filterWorkouts(query)
            hideKeyboard()
        }
    }

    private fun openMyWorkoutsActivity() {
        val intent = Intent(this, MyWorkoutsActivity::class.java)
        startActivity(intent)
    }

    private fun showAddWorkoutDialog() {
        val dialog = AddWorkoutDialog(this, this, workouts) // Передаем текущий экземпляр MainActivity
        dialog.show()
    }

    private fun filterWorkouts(query: String) {
        val filteredList = workouts.filter { workout ->
            workout.title.lowercase().contains(query)
        }
        adapter.updateWorkouts(filteredList)
        if (filteredList.isEmpty()) {
            Toast.makeText(this, "Нет подходящих тренировок", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openWorkoutRating() {
        val intent = Intent(this, WorkoutRating::class.java)
        intent.putParcelableArrayListExtra("WORKOUT_LIST", ArrayList(workouts)) // Передаем весь список
        startActivity(intent)
    }


    private fun hideKeyboard() {
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_main -> true
            R.id.action_favorite -> {
                openMyWorkoutsActivity()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()
        // Обновление списка тренировок при возврате в активность
        workouts.clear()
        workouts.addAll(dbHelper.getAllMainWorkouts())
        adapter.notifyDataSetChanged()
    }

    // Реализация метода onAddClicked из интерфейса OnAddClickListener
    override fun onAddClicked(workoutName: String, workoutDescription: String, source: String) {
        val newId = workouts.size + 1 // Простой способ получения нового ID
        dbHelper.addMainWorkout(workoutName, workoutDescription)
        workouts.add(Workout(newId, workoutName, workoutDescription)) // Передаем ID
        adapter.notifyItemInserted(workouts.size - 1)
        Toast.makeText(this, "Тренировка добавлена", Toast.LENGTH_SHORT).show()
    }

    private fun saveWorkoutsToCSV() {
        val fileName = "workouts.csv"
        val file = File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)

        try {
            FileOutputStream(file).use { outputStream ->
                outputStream.write("Название,Описание\n".toByteArray()) // Заголовки CSV
                for (workout in workouts) {
                    val line = "${workout.title},${workout.description}\n"
                    outputStream.write(line.toByteArray())
                }
            }
            Toast.makeText(this, "Тренировки сохранены в $fileName", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e("CSVError", "Ошибка при сохранении CSV", e)
            Toast.makeText(this, "Ошибка при сохранении CSV", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadWorkoutsFromCSV() {
        val fileName = "workouts.csv"
        val file = File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)

        if (!file.exists()) {
            Toast.makeText(this, "Файл не найден", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            BufferedReader(FileReader(file)).use { reader ->
                reader.readLine() // Пропускаем заголовки
                workouts.clear() // Очищаем текущий список тренировок
                var newId = workouts.size // Начинаем с текущего размера списка
                reader.forEachLine { line ->
                    val parts = line.split(",")
                    if (parts.size == 2) {
                        val title = parts[0]
                        val description = parts[1]
                        newId++ // Увеличиваем ID для каждой новой тренировки
                        workouts.add(Workout(newId, title, description)) // Передаем ID
                    }
                }
            }
            adapter.notifyDataSetChanged() // Уведомляем адаптер об изменениях
            Toast.makeText(this, "Тренировки загружены из $fileName", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e("CSVError", "Ошибка при загрузке CSV", e)
            Toast.makeText(this, "Ошибка при загрузке CSV", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveWorkoutsToBinaryFile() {
        val file = File(getExternalFilesDir(null), "workouts.bin")
        try {
            ObjectOutputStream(FileOutputStream(file)).use { outputStream ->
                outputStream.writeObject(workouts)
            }
            Log.d("SaveWorkouts", "Тренировки сохранены в $file")
        } catch (e: IOException) {
            Log.e("SaveWorkouts", "Ошибка при сохранении в бинарный файл", e)
        }
    }

    private fun loadWorkoutsFromBinaryFile() {
        val file = File(getExternalFilesDir(null), "workouts.bin")
        if (!file.exists()) {
            Toast.makeText(this, "Файл не найден", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            ObjectInputStream(FileInputStream(file)).use { inputStream ->
                val loadedWorkouts = inputStream.readObject() as MutableList<Workout>
                workouts.clear() // Очищаем текущий список
                workouts.addAll(loadedWorkouts) // Добавляем загруженные тренировки
            }
            adapter.notifyDataSetChanged() // Уведомляем адаптер об изменениях
            Log.d("LoadWorkouts", "Тренировки загружены из $file")
        } catch (e: IOException) {
            Log.e("LoadWorkouts", "Ошибка при загрузке из бинарного файла", e)
            Toast.makeText(this, "Ошибка при загрузке из бинарного файла", Toast.LENGTH_SHORT).show()
        }
    }
}
