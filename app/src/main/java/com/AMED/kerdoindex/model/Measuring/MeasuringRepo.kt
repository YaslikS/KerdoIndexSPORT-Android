package com.AMED.kerdoindex.model.Measuring

import androidx.lifecycle.LiveData

class MeasuringRepo(private val measuringDao: MeasuringDao) {
    val readAllMeasuring: LiveData<List<Measuring>> = measuringDao.allMeasuring()

    suspend fun insertMeasuring(measuring: Measuring){
        measuringDao.insertMeasuring(measuring)
    }

    suspend fun deleteMeasuring(measuring: Measuring){
        measuringDao.deleteMeasuring(measuring)
    }

    suspend fun deleteAllMeasuring(){
        measuringDao.deleteAllMeasuring()
    }

}