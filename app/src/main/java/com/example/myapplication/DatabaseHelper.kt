package com.example.myapplication

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "workouts.db"
        private const val DATABASE_VERSION = 6

        const val TABLE_MAIN_WORKOUTS = "mainworkouts"
        const val TABLE_MY_WORKOUTS = "myworkouts"
        const val TABLE_WORKOUT_RATING = "workoutrating"

        const val COLUMN_ID = "id"
        const val COLUMN_TITLE = "title"
        const val COLUMN_DESCRIPTION = "description"
        const val COLUMN_MAIN_WORKOUT_ID = "mainworkouts_id"
        const val COLUMN_RATING = "rating"
    }

    override fun onCreate(db: SQLiteDatabase) {
        Log.d("DatabaseHelper", "onCreate called")
        val createMainWorkoutsTable = ("CREATE TABLE $TABLE_MAIN_WORKOUTS (" +
                "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "$COLUMN_TITLE TEXT," +
                "$COLUMN_DESCRIPTION TEXT)")

        val createMyWorkoutsTable = ("CREATE TABLE $TABLE_MY_WORKOUTS (" +
                "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "$COLUMN_MAIN_WORKOUT_ID INTEGER," +
                "FOREIGN KEY($COLUMN_MAIN_WORKOUT_ID) REFERENCES $TABLE_MAIN_WORKOUTS($COLUMN_ID))")

        val createWorkoutRatingTable = ("CREATE TABLE $TABLE_WORKOUT_RATING (" +
                "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "$COLUMN_MAIN_WORKOUT_ID INTEGER," +
                "$COLUMN_RATING REAL," +
                "FOREIGN KEY($COLUMN_MAIN_WORKOUT_ID) REFERENCES $TABLE_MAIN_WORKOUTS($COLUMN_ID))")

        db.execSQL(createMainWorkoutsTable)
        db.execSQL(createMyWorkoutsTable)
        db.execSQL(createWorkoutRatingTable)

        // Добавляем несколько тренировок
        addInitialWorkouts(db)
    }

    private fun addInitialWorkouts(db: SQLiteDatabase) {
        val workouts = listOf(
            Pair("Бег", "30 минут бега на улице"),
            Pair("Силовая тренировка", "Тренировка с весами в зале"),
            Pair("Йога", "45 минут йоги для расслабления")
        )

        for (workout in workouts) {
            val values = ContentValues().apply {
                put(COLUMN_TITLE, workout.first)
                put(COLUMN_DESCRIPTION, workout.second)
            }
            db.insert(TABLE_MAIN_WORKOUTS, null, values)
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        Log.d("DatabaseHelper", "onUpgrade called from version $oldVersion to $newVersion")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_WORKOUT_RATING")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_MY_WORKOUTS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_MAIN_WORKOUTS")
        onCreate(db)
    }

    fun getAllMainWorkoutsWithRatings(): List<Workout> {
        val workouts = mutableListOf<Workout>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT mw.*, wr.$COLUMN_RATING FROM $TABLE_MAIN_WORKOUTS mw LEFT JOIN $TABLE_WORKOUT_RATING wr ON mw.$COLUMN_ID = wr.$COLUMN_MAIN_WORKOUT_ID", null)

        if (cursor.moveToFirst()) {
            do {
                val idIndex = cursor.getColumnIndex(COLUMN_ID)
                val titleIndex = cursor.getColumnIndex(COLUMN_TITLE)
                val descriptionIndex = cursor.getColumnIndex(COLUMN_DESCRIPTION)
                val ratingIndex = cursor.getColumnIndex(COLUMN_RATING)

                if (idIndex != -1 && titleIndex != -1 && descriptionIndex != -1) {
                    val id = cursor.getInt(idIndex)
                    val title = cursor.getString(titleIndex)
                    val description = cursor.getString(descriptionIndex)
                    val rating = cursor.getFloat(ratingIndex) // Получаем оценку (может быть null)

                    workouts.add(Workout(id, title, description, rating)) // Создаем объект Workout с оценкой
                }
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return workouts
    }

    // Остальные методы класса остаются без изменений
    fun saveWorkoutRating(mainWorkoutId: Int, rating: Float) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_MAIN_WORKOUT_ID, mainWorkoutId)
            put(COLUMN_RATING, rating)
        }

        // Проверяем, существует ли уже запись с этим ID
        val result = db.update(TABLE_WORKOUT_RATING, values, "$COLUMN_MAIN_WORKOUT_ID = ?", arrayOf(mainWorkoutId.toString()))
        if (result == 0) {
            val newRowId = db.insert(TABLE_WORKOUT_RATING, null, values) // Если нет, добавляем новую запись
            if (newRowId == -1L) {
                Log.e("DatabaseHelper", "Ошибка вставки новой оценки")
            } else {
                Log.d("DatabaseHelper", "Оценка успешно добавлена для ID: $mainWorkoutId")
            }
        } else {
            Log.d("DatabaseHelper", "Оценка успешно обновлена для ID: $mainWorkoutId")
        }
        db.close()
    }



    // Методы для работы с основной таблицей тренировок
    fun addMainWorkout(title: String, description: String) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TITLE, title)
            put(COLUMN_DESCRIPTION, description)
        }
        db.insert(TABLE_MAIN_WORKOUTS, null, values)
        db.close()
    }

    fun updateMainWorkout(oldTitle: String, newTitle: String, newDescription: String) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TITLE, newTitle)
            put(COLUMN_DESCRIPTION, newDescription)
        }
        db.update(TABLE_MAIN_WORKOUTS, values, "$COLUMN_TITLE = ?", arrayOf(oldTitle))
        db.close()
    }

    fun getAllMainWorkouts(): List<Workout> {
        val workouts = mutableListOf<Workout>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_MAIN_WORKOUTS", null)

        if (cursor.moveToFirst()) {
            do {
                val idIndex = cursor.getColumnIndex(COLUMN_ID) // Получаем индекс ID
                val titleIndex = cursor.getColumnIndex(COLUMN_TITLE)
                val descriptionIndex = cursor.getColumnIndex(COLUMN_DESCRIPTION)

                // Проверяем, что индексы действительны
                if (idIndex != -1 && titleIndex != -1 && descriptionIndex != -1) {
                    val id = cursor.getInt(idIndex) // Получаем ID
                    val title = cursor.getString(titleIndex)
                    val description = cursor.getString(descriptionIndex)
                    workouts.add(Workout(id, title, description)) // Создаем объект Workout с ID
                }
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return workouts
    }


    fun deleteMainWorkout(title: String) {
        val db = this.writableDatabase
        db.delete(TABLE_MAIN_WORKOUTS, "$COLUMN_TITLE = ?", arrayOf(title))
        db.close()
    }

    // Методы для работы с таблицей "Мои тренировки"
    fun addMyWorkout(title: String) {
        val mainWorkoutId = getMainWorkoutIdByTitle(title)
        if (mainWorkoutId != null) {
            val db = this.writableDatabase
            val values = ContentValues().apply {
                put(COLUMN_MAIN_WORKOUT_ID, mainWorkoutId)
            }
            db.insert(TABLE_MY_WORKOUTS, null, values)
            db.close()
        }
    }

    private fun getMainWorkoutIdByTitle(title: String): Int? {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT $COLUMN_ID FROM $TABLE_MAIN_WORKOUTS WHERE $COLUMN_TITLE = ?", arrayOf(title))
        return if (cursor.moveToFirst()) {
            val idIndex = cursor.getColumnIndex(COLUMN_ID)
            cursor.getInt(idIndex).also { cursor.close() }
        } else {
            cursor.close()
            null // -1 возвращать
        }
    }


    fun deleteMyWorkout(id: Int) {
        val db = this.writableDatabase
        db.delete(TABLE_MY_WORKOUTS, "$COLUMN_ID = ?", arrayOf(id.toString()))
        db.close()
    }


    fun getAllMyWorkouts(): List<Workout> {
        val myWorkouts = mutableListOf<Workout>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_MY_WORKOUTS", null)

        if (cursor.moveToFirst()) {
            do {
                val mainWorkoutIdIndex = cursor.getColumnIndex(COLUMN_MAIN_WORKOUT_ID)
                if (mainWorkoutIdIndex != -1) { // Проверяем, что индекс действителен
                    val mainWorkoutId = cursor.getInt(mainWorkoutIdIndex)

                    // Получаем объект Workout по ID
                    val workout = getMainWorkoutById(mainWorkoutId)
                    if (workout != null) {
                        myWorkouts.add(workout)
                    }
                }
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return myWorkouts
    }

    private fun getMainWorkoutById(id: Int): Workout? {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_MAIN_WORKOUTS WHERE $COLUMN_ID = ?", arrayOf(id.toString()))

        return if (cursor.moveToFirst()) {
            val titleIndex = cursor.getColumnIndex(COLUMN_TITLE)
            val descriptionIndex = cursor.getColumnIndex(COLUMN_DESCRIPTION)

            // Проверяем, что индексы действительны
            if (titleIndex != -1 && descriptionIndex != -1) {
                val title = cursor.getString(titleIndex)
                val description = cursor.getString(descriptionIndex)
                cursor.close()
                db.close()
                Workout(id,title, description)
            } else {
                cursor.close()
                db.close()
                null
            }
        } else {
            cursor.close()
            db.close()
            null
        }
    }
    fun deleteMyWorkoutByMainWorkoutId(mainWorkoutId: Int) {
        val db = this.writableDatabase
        db.delete(TABLE_MY_WORKOUTS, "$COLUMN_MAIN_WORKOUT_ID = ?", arrayOf(mainWorkoutId.toString()))
        db.close()
    }


}
