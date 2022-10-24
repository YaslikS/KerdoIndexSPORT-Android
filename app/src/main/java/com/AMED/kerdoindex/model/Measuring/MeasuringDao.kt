package com.AMED.kerdoindex.model.Measuring

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface MeasuringDao {

   @Insert
   suspend fun insertMeasuring(measuring: Measuring)

   @Delete
   suspend fun deleteMeasuring(measuring: Measuring)

   @Query("DELETE FROM measuring")
   suspend fun deleteAllMeasuring()

   @Query("select * from measuring order by number asc")
   fun allMeasuring(): LiveData<List<Measuring>>

}