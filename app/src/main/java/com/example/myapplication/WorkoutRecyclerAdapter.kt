package com.example.myapplication

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

class WorkoutRecyclerAdapter(
    private val context: Context,
    private var workouts: List<Workout>,
    private val itemClick: (Workout) -> Unit
) : RecyclerView.Adapter<WorkoutRecyclerAdapter.ViewHolder>() {

    private val dbHelper = DatabaseHelper(context)

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.textViewWorkoutName)
        val descriptionTextView: TextView = itemView.findViewById(R.id.textViewWorkoutDescription)
        val ratingBar: RatingBar = itemView.findViewById(R.id.ratingBar)
        val buttonSave: Button = itemView.findViewById(R.id.buttonSubmit)

        fun bind(workout: Workout) {
            titleTextView.text = workout.title
            descriptionTextView.text = workout.description
            ratingBar.rating = workout.rating

            Log.d("WorkoutRecyclerAdapter", "ID тренировки: ${workout.id}")

            ratingBar.setOnRatingBarChangeListener { _, rating, _ ->
                workout.rating = rating // Обновляем оценку в объекте Workout
            }

            buttonSave.setOnClickListener {
                saveRating(workout)
            }

            itemView.setOnClickListener { itemClick(workout) }
        }

        private fun saveRating(workout: Workout) {
            Log.d("WorkoutRecyclerAdapter", "Сохранение оценки для '${workout.title}' с оценкой: ${workout.rating}")
            dbHelper.saveWorkoutRating(workout.id, workout.rating)
            Toast.makeText(context, "Оценка для '${workout.title}' сохранена!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_rating_workout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(workouts[position])
    }

    override fun getItemCount(): Int = workouts.size
}
