package com.AMED.kerdoindex.model.Measuring2

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface Measuring2Dao {

    @Insert
    suspend fun insertMeasuring2(measuring2: Measuring2)

    @Delete
    suspend fun deleteMeasuring2(measuring2: Measuring2)

    @Query("DELETE FROM measuring2")
    suspend fun deleteAllMeasuring2()

    @Query("select * from measuring2 order by number asc")
    fun allMeasuring2(): LiveData<List<Measuring2>>

}