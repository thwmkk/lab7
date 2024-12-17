package com.example.myapplication

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatDialog

class AddWorkoutDialog(context: Context, private val listener: OnAddClickListener, private val existingWorkouts: List<Workout>) : AppCompatDialog(context) {

    private lateinit var editTextName: EditText
    private lateinit var editTextDescription: EditText
    private lateinit var buttonAdd: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_add_workout)

        editTextName = findViewById(R.id.editTextWorkoutName)!!
        editTextDescription = findViewById(R.id.editTextWorkoutDescription)!!
        buttonAdd = findViewById(R.id.buttonAdd)!!

        buttonAdd.setOnClickListener {
            val name = editTextName.text.toString().trim()
            val description = editTextDescription.text.toString().trim()

            // Проверка на пустые поля
            if (name.isEmpty()) {
                Toast.makeText(context, "Название не может быть пустым", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (description.isEmpty()) {
                Toast.makeText(context, "Описание не может быть пустым", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Проверка на существование тренировки с таким же названием
            if (existingWorkouts.any { it.title.equals(name, ignoreCase = true) }) {
                Toast.makeText(context, "Тренировка с таким названием уже существует", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Если проверки пройдены, добавляем тренировку
            listener.onAddClicked(name, description, "MainActivity")
            dismiss()
        }
    }
}
