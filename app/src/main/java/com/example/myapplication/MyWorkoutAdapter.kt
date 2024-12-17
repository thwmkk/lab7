package com.example.myapplication

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

class MyWorkoutAdapter(
    private val context: Context,
    private var workouts: MutableList<Workout>, // Измените на var для возможности обновления
    private val onDeleteClick: (Workout) -> Unit,
    private val dbHelper: DatabaseHelper
) : RecyclerView.Adapter<MyWorkoutAdapter.WorkoutViewHolder>() {

    // ViewHolder для представления каждого элемента списка
    class WorkoutViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewTitle: TextView = itemView.findViewById(R.id.textViewWorkoutName)
        val textViewDescription: TextView = itemView.findViewById(R.id.textViewWorkoutDescription)
        val buttonDelete: Button = itemView.findViewById(R.id.buttonDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkoutViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_my_workouts, parent, false)
        return WorkoutViewHolder(view)
    }

    override fun onBindViewHolder(holder: WorkoutViewHolder, position: Int) {
        val workout = workouts[position]
        holder.textViewTitle.text = workout.title
        holder.textViewDescription.text = workout.description

        holder.buttonDelete.setOnClickListener {
            removeWorkout(workout) // Удаляем элемент
            onDeleteClick(workout) // Вызываем обработчик клика
        }
    }

    override fun getItemCount(): Int {
        return workouts.size
    }

    // Метод для удаления тренировки из списка
    private fun removeWorkout(workout: Workout) {
        val position = workouts.indexOf(workout)
        if (position != -1) {
            // Удаляем тренировку из базы данных только из myworkouts
            val mainWorkoutId = workout.id // Предполагается, что у вас есть id тренировки
            dbHelper.deleteMyWorkoutByMainWorkoutId(mainWorkoutId) // Удаляем только из myworkouts

            // Удаляем элемент из списка
            workouts.removeAt(position)
            notifyItemRemoved(position) // Уведомляем адаптер о том, что элемент удален
            notifyItemRangeChanged(position, workouts.size) // Обновляем оставшиеся элементы

            Toast.makeText(context, "Тренировка удалена из моих тренировок", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Не удалось найти тренировку для удаления", Toast.LENGTH_SHORT).show()
        }
    }



}
