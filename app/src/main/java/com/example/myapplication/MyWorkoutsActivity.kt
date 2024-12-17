package com.example.myapplication

import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

class MyWorkoutsActivity : AppCompatActivity() {

    private lateinit var viewPagerWorkouts: ViewPager2
    private lateinit var bottomNavigationView: BottomNavigationView
    private val savedWorkouts = mutableListOf<Workout>()
    private lateinit var workoutPagerAdapter: WorkoutPagerAdapter
    private lateinit var dbHelper: DatabaseHelper
    private var isExiting = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_workouts)

        dbHelper = DatabaseHelper(this)

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        viewPagerWorkouts = findViewById(R.id.viewPagerWorkouts)
        bottomNavigationView = findViewById(R.id.bottomNavigationView)
        bottomNavigationView.selectedItemId = R.id.bottom_add
        val buttonDocumentPDF: Button = findViewById(R.id.buttonDocumentPDF)

        buttonDocumentPDF.setOnClickListener {
            generatePDFReport()
        }

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.bottom_main -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    finish()
                    true
                }
                R.id.bottom_rating -> {
                    startActivity(Intent(this, WorkoutRating::class.java))
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    finish()
                    true
                }
                R.id.bottom_add -> true
                R.id.bottom_return -> {
                    loadWorkoutsFromBinaryFile()
                    true
                }
                else -> false
            }
        }

        loadWorkoutsFromDatabase()
        updateGroupedWorkouts()
    }

    private fun loadWorkoutsFromDatabase() {
        savedWorkouts.clear()
        val workoutsFromDb = dbHelper.getAllMyWorkouts()
        savedWorkouts.addAll(workoutsFromDb)
    }

    private fun updateGroupedWorkouts() {
        val groupedWorkouts = savedWorkouts.chunked(3)

        workoutPagerAdapter = WorkoutPagerAdapter(groupedWorkouts, this, { workout -> removeWorkout(workout) }, dbHelper)
        viewPagerWorkouts.adapter = workoutPagerAdapter
        viewPagerWorkouts.orientation = ViewPager2.ORIENTATION_VERTICAL
    }


    private fun removeWorkout(workout: Workout) {
        savedWorkouts.remove(workout)
        dbHelper.deleteMyWorkout(workout.id) // Удаляем по ID
        updateGroupedWorkouts()
    }


    private fun generatePDFReport() {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(300, 600, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        // Настройка текста для PDF
        val paint = Paint()
        paint.color = Color.BLACK
        paint.textSize = 12f

        // Добавление заголовка
        canvas.drawText("Отчет о тренировках", 10f, 25f, paint)

        // Добавление информации о тренировках
        var yPosition = 50f
        for (workout in savedWorkouts) {
            canvas.drawText("Название: ${workout.title}", 10f, yPosition, paint)
            yPosition += 15f
            canvas.drawText("Описание: ${workout.description}", 10f, yPosition, paint)
            yPosition += 30f // Отступ между тренировками
        }

        pdfDocument.finishPage(page)

        // Сохранение PDF в файл
        val filePath = File(getExternalFilesDir(null), "workout_report.pdf")
        try {
            pdfDocument.writeTo(FileOutputStream(filePath))
            Toast.makeText(this, "PDF создан: ${filePath.absolutePath}", Toast.LENGTH_LONG).show()
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Ошибка при создании PDF", Toast.LENGTH_SHORT).show()
        } finally {
            pdfDocument.close()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isTaskRoot) { // Проверяем, является ли это корневой задачей
            saveWorkoutsToBinaryFile() // Сохранение данных
        }
    }

    private fun saveWorkoutsToBinaryFile() {
        val file = File(getExternalFilesDir(null), "my_workouts.bin")
        try {
            ObjectOutputStream(FileOutputStream(file)).use { outputStream ->
                outputStream.writeObject(savedWorkouts)
            }
            Log.d("SaveWorkouts", "Тренировки сохранены в $file")
        } catch (e: IOException) {
            Log.e("SaveWorkouts", "Ошибка при сохранении в бинарный файл", e)
        }
    }

    private fun loadWorkoutsFromBinaryFile() {
        val file = File(getExternalFilesDir(null), "my_workouts.bin")
        if (!file.exists()) {
            Toast.makeText(this, "Файл не найден", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            ObjectInputStream(FileInputStream(file)).use { inputStream ->
                val loadedWorkouts = inputStream.readObject() as MutableList<Workout>
                savedWorkouts.clear() // Очищаем текущий список
                savedWorkouts.addAll(loadedWorkouts) // Добавляем загруженные тренировки
            }
            updateGroupedWorkouts()
            Log.d("LoadWorkouts", "Тренировки загружены из $file")
        } catch (e: IOException) {
            Log.e("LoadWorkouts", "Ошибка при загрузке из бинарного файла: IOException", e)
        } catch (e: ClassNotFoundException) {
            Log.e("LoadWorkouts", "Ошибка при загрузке из бинарного файла: ClassNotFoundException", e)
        }
    }
}
