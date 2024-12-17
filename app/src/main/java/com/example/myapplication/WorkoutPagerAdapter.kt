package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class WorkoutPagerAdapter(
    private var groupedWorkouts: List<List<Workout>>, // Изменено на var
    private val activity: AppCompatActivity, // Передаем контекст активности
    private val onDeleteClick: (Workout) -> Unit,
    private val dbHelper: DatabaseHelper // Добавляем DatabaseHelper
) : RecyclerView.Adapter<WorkoutPagerAdapter.WorkoutPageViewHolder>() {

    class WorkoutPageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val recyclerView: RecyclerView = itemView.findViewById(R.id.recyclerView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkoutPageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.page_workouts, parent, false)
        return WorkoutPageViewHolder(view)
    }

    fun updateWorkouts(newGroupedWorkouts: List<List<Workout>>) {
        groupedWorkouts = newGroupedWorkouts
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: WorkoutPageViewHolder, position: Int) {
        val workouts = groupedWorkouts[position]
        holder.recyclerView.layoutManager = LinearLayoutManager(holder.itemView.context)

        val adapter = MyWorkoutAdapter(activity, workouts.toMutableList(), onDeleteClick, dbHelper)

        holder.recyclerView.adapter = adapter
    }

    override fun getItemCount(): Int {
        return groupedWorkouts.size
    }
}
