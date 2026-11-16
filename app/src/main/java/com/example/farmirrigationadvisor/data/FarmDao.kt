package com.example.farmirrigationadvisor.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface FarmDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertFarm(farm: Farm): Long

    @Query("SELECT * FROM farms ORDER BY id DESC LIMIT 1")
    fun getLatestFarm(): Farm?

    @Query("SELECT * FROM farms ORDER BY id DESC")
    fun getAllFarms(): List<Farm>
}