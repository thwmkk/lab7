package com.example.myapplication

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

class WorkoutAdapter(
    private val context: Context,
    private var workouts: MutableList<Workout>,
    private val onWorkoutClick: (Workout) -> Unit,
    private val onLongClick: (Workout) -> Unit,
    private val onAddClickListener: OnAddClickListener,
    private val dbHelper: DatabaseHelper // Передаем DatabaseHelper через конструктор
) : RecyclerView.Adapter<WorkoutAdapter.WorkoutViewHolder>() {

    private val originalWorkouts: MutableList<Workout> = workouts.toMutableList()

    inner class WorkoutViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.textViewWorkout)
        val descriptionTextView: TextView = itemView.findViewById(R.id.textViewDescription)
        val buttonAdd: Button = itemView.findViewById(R.id.buttonAdd)
        val buttonEdit: Button = itemView.findViewById(R.id.buttonEdit)
        val buttonDelete: Button = itemView.findViewById(R.id.buttonDelete)

        init {
            itemView.setOnClickListener {
                onWorkoutClick(workouts[adapterPosition])
            }

            buttonAdd.setOnClickListener {
                val workout = workouts[adapterPosition]

                // Добавляем тренировку в базу данных
                dbHelper.addMyWorkout(workout.title
                )
            }

            buttonDelete.setOnClickListener {
                val workout = workouts[adapterPosition]
                removeWorkout(workout)
            }

            buttonEdit.setOnClickListener {
                val workout = workouts[adapterPosition]
                showEditDialog(workout)
            }
        }

        fun bind(workout: Workout) {
            titleTextView.text = workout.title
            descriptionTextView.text = workout.description
        }
    }

    // Метод для удаления тренировки из списка
    private fun removeWorkout(workout: Workout) {
        val position = workouts.indexOf(workout)
        if (position != -1) {
            workouts.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, workouts.size)

            // Удаляем тренировку из базы данных
            dbHelper.deleteMainWorkout(workout.title)

            Toast.makeText(context, "Тренировка удалена", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Не удалось найти тренировку для удаления", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showEditDialog(workout: Workout) {
        val dialog = EditWorkoutDialog(context, workout, object : EditWorkoutListener {
            override fun onWorkoutUpdated(updatedTitle: String, updatedDescription: String) {
                dbHelper.updateMainWorkout(workout.title, updatedTitle, updatedDescription) // Обновление в базе данных
                workout.title = updatedTitle
                workout.description = updatedDescription
                notifyDataSetChanged()

                saveWorkoutsToPreferences() // Сохранение изменений в SharedPreferences
                Toast.makeText(context, "Тренировка обновлена", Toast.LENGTH_SHORT).show()
            }
        })
        dialog.show()
    }

    private fun saveWorkoutsToPreferences() {
        val sharedPreferences = context.getSharedPreferences("MainWorkouts", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.clear() // Очищаем предыдущие данные

        for (workout in workouts) {
            editor.putString(workout.title, workout.description)
        }
        editor.apply() // Используем apply() для асинхронного сохранения
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkoutViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.main_item_activity, parent, false)
        return WorkoutViewHolder(view)
    }

    override fun onBindViewHolder(holder: WorkoutViewHolder, position: Int) {
        val workout = workouts[position]
        holder.bind(workout)

        // Обработка долгого нажатия
        holder.itemView.setOnLongClickListener {
            onLongClick(workout)
            true
        }
    }

    override fun getItemCount(): Int {
        return workouts.size
    }

    fun updateWorkouts(filteredWorkouts: List<Workout>) {
        workouts.clear()
        workouts.addAll(filteredWorkouts)
        notifyDataSetChanged()
    }

    fun getOriginalWorkouts(): List<Workout> {
        return originalWorkouts
    }
}
