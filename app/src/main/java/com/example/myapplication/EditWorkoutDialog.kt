package com.example.myapplication

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatDialog

class EditWorkoutDialog(
    context: Context,
    private val workout: Workout,
    private val listener: EditWorkoutListener
) : AppCompatDialog(context) {

    private lateinit var editTextTitle: EditText
    private lateinit var editTextDescription: EditText
    private lateinit var buttonSave: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_edit_workout)

        // Используем явное приведение типов и проверку на null
        editTextTitle = findViewById(R.id.editTextTitle)!!
        editTextDescription = findViewById(R.id.editTextDescription)!!
        buttonSave = findViewById(R.id.buttonSave)!!

        // Заполнение полей текущими значениями
        editTextTitle.setText(workout.title)
        editTextDescription.setText(workout.description)

        buttonSave.setOnClickListener {
            val updatedTitle = editTextTitle.text.toString()
            val updatedDescription = editTextDescription.text.toString()
            listener.onWorkoutUpdated(updatedTitle, updatedDescription)
            dismiss()
        }
    }
}
