package com.example.farmirrigationadvisor.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface IrrigationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertRecord(record: IrrigationRecord): Long

    @Query("SELECT * FROM irrigation_records ORDER BY id DESC")
    fun getAllRecords(): List<IrrigationRecord>
}