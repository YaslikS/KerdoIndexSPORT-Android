package com.AMED.kerdoindex.model.Measuring

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.AMED.kerdoindex.model.MeasureDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MeasuringViewModel(application: Application): AndroidViewModel(application) {
    val readAllMeasuring: LiveData<List<Measuring>>
    private val repository: MeasuringRepo

    init {
        val measuringDao = MeasureDatabase.getDatabase(application).measuringDao()
        repository = MeasuringRepo(measuringDao)
        readAllMeasuring = repository.readAllMeasuring
    }

    fun insertMeasuring(measuring: Measuring){
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertMeasuring(measuring)
        }
    }

    fun deleteAllMeasuring(){
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAllMeasuring()
        }
    }

    fun deleteMeasuring(measuring: Measuring){
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteMeasuring(measuring)
        }
    }
}