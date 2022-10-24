package com.AMED.kerdoindex.model.Measuring2

import androidx.lifecycle.LiveData

class Measuring2Repo(private val measuring2Dao: Measuring2Dao) {
    val readAllMeasuring2: LiveData<List<Measuring2>> = measuring2Dao.allMeasuring2()

    suspend fun insertMeasuring2(measuring2: Measuring2){
        measuring2Dao.insertMeasuring2(measuring2)
    }

    suspend fun deleteMeasuring2(measuring2: Measuring2){
        measuring2Dao.deleteMeasuring2(measuring2)
    }

    suspend fun deleteAllMeasuring2(){
        measuring2Dao.deleteAllMeasuring2()
    }

}