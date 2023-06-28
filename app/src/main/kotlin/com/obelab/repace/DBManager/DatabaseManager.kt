package com.obelab.repace.DBManager

import android.annotation.SuppressLint
import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import com.google.gson.Gson
import com.obelab.repace.core.functional.Functions
import com.obelab.repace.core.platform.BaseActivity
import com.obelab.repace.model.DayExerciseModel
import com.obelab.repace.model.ExerciseResultModel

class DatabaseManager : SQLiteOpenHelper(BaseActivity.instance, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "RepaceDatabase"
        private const val TABLE_EXERCISE_IN_DAY = "DayExerciseTable"
        private const val KEY_EXERCISE_IN_DAY_ID = "id"
        private const val KEY_EXERCISE_IN_DAY_DATE = "date"
        private const val KEY_EXERCISE_IN_DAY_MONTH = "month"
        private const val KEY_EXERCISE_IN_DAY_YEAR = "year"
        private const val KEY_EXERCISE_IN_DAY_EXERCISE = "exercise"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        // creating table with fields
        val createDayExerciseTable = ("CREATE TABLE " + TABLE_EXERCISE_IN_DAY + "("
                + KEY_EXERCISE_IN_DAY_ID + " TEXT PRIMARY KEY,"
                + KEY_EXERCISE_IN_DAY_DATE + " TEXT,"
                + KEY_EXERCISE_IN_DAY_MONTH + " TEXT,"
                + KEY_EXERCISE_IN_DAY_YEAR + " TEXT,"
                + KEY_EXERCISE_IN_DAY_EXERCISE + " TEXT"
                + ")")
        db?.execSQL(createDayExerciseTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db!!.execSQL("DROP TABLE IF EXISTS $TABLE_EXERCISE_IN_DAY")
        onCreate(db)
    }

    fun addDayExercise(value: DayExerciseModel): Long {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(KEY_EXERCISE_IN_DAY_ID, value.id)
        contentValues.put(KEY_EXERCISE_IN_DAY_DATE, value.date)
        contentValues.put(KEY_EXERCISE_IN_DAY_MONTH, value.month)
        contentValues.put(KEY_EXERCISE_IN_DAY_YEAR, value.year)
        contentValues.put(KEY_EXERCISE_IN_DAY_EXERCISE, Functions.toJsonString(value.exercise))
        // Inserting Row
        val success = db.insert(TABLE_EXERCISE_IN_DAY, null, contentValues)
        // 2nd argument is String containing nullColumnHack
        db.close() // Closing database connection
        return success
    }

    @SuppressLint("Range")
    fun getDayExercises(): List<DayExerciseModel> {
        val dataList: ArrayList<DayExerciseModel> = ArrayList<DayExerciseModel>()
        val selectQuery = "SELECT  * FROM $TABLE_EXERCISE_IN_DAY"
        val db = this.readableDatabase
        var cursor: Cursor? = null
        try {
            cursor = db.rawQuery(selectQuery, null)
        } catch (e: SQLiteException) {
            db.execSQL(selectQuery)
            return ArrayList()
        }
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getString(cursor.getColumnIndex(KEY_EXERCISE_IN_DAY_ID))
                val date = cursor.getInt(cursor.getColumnIndex(KEY_EXERCISE_IN_DAY_DATE))
                val month = cursor.getInt(cursor.getColumnIndex(KEY_EXERCISE_IN_DAY_MONTH))
                val year = cursor.getInt(cursor.getColumnIndex(KEY_EXERCISE_IN_DAY_YEAR))
                val exerciseStr = cursor.getString(cursor.getColumnIndex(KEY_EXERCISE_IN_DAY_EXERCISE))
                val exercise = Gson().fromJson(exerciseStr, Array<ExerciseResultModel>::class.java).toMutableList()
                val data = DayExerciseModel(id, date, month, year, exercise)
                dataList.add(data)
            } while (cursor.moveToNext())
        }
        return dataList
    }

    @SuppressLint("Range")
    fun getDayExercisesById(id: String): List<DayExerciseModel> {
        val dataList: ArrayList<DayExerciseModel> = ArrayList<DayExerciseModel>()
        val selectQuery = "SELECT  * FROM $TABLE_EXERCISE_IN_DAY WHERE id='$id'"
        val db = this.readableDatabase
        var cursor: Cursor? = null
        try {
            cursor = db.rawQuery(selectQuery, null)
        } catch (e: SQLiteException) {
            db.execSQL(selectQuery)
            return ArrayList()
        }
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getString(cursor.getColumnIndex(KEY_EXERCISE_IN_DAY_ID))
                val date = cursor.getInt(cursor.getColumnIndex(KEY_EXERCISE_IN_DAY_DATE))
                val month = cursor.getInt(cursor.getColumnIndex(KEY_EXERCISE_IN_DAY_MONTH))
                val year = cursor.getInt(cursor.getColumnIndex(KEY_EXERCISE_IN_DAY_YEAR))
                val exerciseStr = cursor.getString(cursor.getColumnIndex(KEY_EXERCISE_IN_DAY_EXERCISE))
                val exercise = Gson().fromJson(exerciseStr, Array<ExerciseResultModel>::class.java).toMutableList()
                val data = DayExerciseModel(id, date, month, year, exercise)
                dataList.add(data)
            } while (cursor.moveToNext())
        }
        return dataList
    }

    @SuppressLint("Range")
    fun getDayExercisesByMonthYear(month: Int, year: Int): List<DayExerciseModel> {
        val dataList: ArrayList<DayExerciseModel> = ArrayList<DayExerciseModel>()
        val selectQuery = "SELECT  * FROM $TABLE_EXERCISE_IN_DAY WHERE $KEY_EXERCISE_IN_DAY_MONTH=$month AND $KEY_EXERCISE_IN_DAY_YEAR=$year"
        val db = this.readableDatabase
        var cursor: Cursor? = null
        try {
            cursor = db.rawQuery(selectQuery, null)
        } catch (e: SQLiteException) {
            db.execSQL(selectQuery)
            return ArrayList()
        }
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getString(cursor.getColumnIndex(KEY_EXERCISE_IN_DAY_ID))
                val date = cursor.getInt(cursor.getColumnIndex(KEY_EXERCISE_IN_DAY_DATE))
                val month = cursor.getInt(cursor.getColumnIndex(KEY_EXERCISE_IN_DAY_MONTH))
                val year = cursor.getInt(cursor.getColumnIndex(KEY_EXERCISE_IN_DAY_YEAR))
                val exerciseStr = cursor.getString(cursor.getColumnIndex(KEY_EXERCISE_IN_DAY_EXERCISE))
                val exercise = Gson().fromJson(exerciseStr, Array<ExerciseResultModel>::class.java).toMutableList()
                val data = DayExerciseModel(id, date, month, year, exercise)
                data.exercise = exercise
//                Functions.showLog("Exercise History Item -> ${Functions.toJsonString(data)}")
                dataList.add(data)
            } while (cursor.moveToNext())
        }
        return dataList
    }

    @SuppressLint("Range")
    fun getTodayExercise(): DayExerciseModel? {
        val dataList: ArrayList<DayExerciseModel> = ArrayList<DayExerciseModel>()
        val selectQuery = "SELECT  * FROM $TABLE_EXERCISE_IN_DAY WHERE id='${Functions.getTodayId()}'"
        val db = this.readableDatabase
        var cursor: Cursor? = null
        try {
            cursor = db.rawQuery(selectQuery, null)
        } catch (e: SQLiteException) {
            db.execSQL(selectQuery)
            return null
        }
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getString(cursor.getColumnIndex(KEY_EXERCISE_IN_DAY_ID))
                val date = cursor.getInt(cursor.getColumnIndex(KEY_EXERCISE_IN_DAY_DATE))
                val month = cursor.getInt(cursor.getColumnIndex(KEY_EXERCISE_IN_DAY_MONTH))
                val year = cursor.getInt(cursor.getColumnIndex(KEY_EXERCISE_IN_DAY_YEAR))
                val exerciseStr = cursor.getString(cursor.getColumnIndex(KEY_EXERCISE_IN_DAY_EXERCISE))
                try {
                    val exercise = Gson().fromJson(exerciseStr, Array<ExerciseResultModel>::class.java).toMutableList()
                    val data = DayExerciseModel(id, date, month, year, exercise)
                    dataList.add(data)
                } catch (e: Exception){

                }

            } while (cursor.moveToNext())
        }
        return if(dataList.isNotEmpty()){
            dataList[0]
        } else {
            return null
        }
    }

    fun updateTodayExercise(value: DayExerciseModel): Long {
        Functions.showLog("updateTodayExerciseParams -> ${Functions.toJsonString(value)}")
        val todayExercise = getTodayExercise()
        return if(todayExercise == null){
            val row = addDayExercise(value)
            Functions.showLog("Add Today Exercise To Database: 1 row")
            row
        } else {
            todayExercise.exercise.addAll(value.exercise)
            val row = updateDayExercise(todayExercise).toLong()
            Functions.showLog("Update Today Exercise To Database: $row row")
            row
        }
    }

    private fun updateDayExercise(value: DayExerciseModel): Int {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(KEY_EXERCISE_IN_DAY_ID, value.id)
        contentValues.put(KEY_EXERCISE_IN_DAY_DATE, value.date)
        contentValues.put(KEY_EXERCISE_IN_DAY_MONTH, value.month)
        contentValues.put(KEY_EXERCISE_IN_DAY_YEAR, value.year)
        contentValues.put(KEY_EXERCISE_IN_DAY_EXERCISE, Functions.toJsonString(value.exercise))
        // Updating Row
        val success = db.update(TABLE_EXERCISE_IN_DAY, contentValues, "id='${value.id}'", null)
        //2nd argument is String containing nullColumnHack
        db.close() // Closing database connection
        return success
    }

    fun deleteDayExercise(value: DayExerciseModel): Int {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(KEY_EXERCISE_IN_DAY_ID, value.id) // EmpModelClass UserId
        // Deleting Row
        val success = db.delete(TABLE_EXERCISE_IN_DAY, "id=" + value.id, null)
        //2nd argument is String containing nullColumnHack
        db.close() // Closing database connection
        return success
    }

    fun deleteAllDayExercises(){
        val db = this.writableDatabase
        db.delete(TABLE_EXERCISE_IN_DAY, null, null)
        db.close()
    }
}