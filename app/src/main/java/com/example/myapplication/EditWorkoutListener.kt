package com.example.myapplication

interface EditWorkoutListener {
    fun onWorkoutUpdated(updatedTitle: String, updatedDescription: String)
}