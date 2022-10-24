package com.AMED.kerdoindex.model.Measuring2

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.AMED.kerdoindex.model.MeasureDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Measuring2ViewModel(application: Application): AndroidViewModel(application) {
    val readAllMeasuring2: LiveData<List<Measuring2>>
    private val repository: Measuring2Repo

    init {
        val measuring2Dao = MeasureDatabase.getDatabase(application).measuring2Dao()
        repository = Measuring2Repo(measuring2Dao)
        readAllMeasuring2 = repository.readAllMeasuring2
    }

    fun insertMeasuring2(measuring2: Measuring2){
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertMeasuring2(measuring2)
        }
    }

    fun deleteAllMeasuring2(){
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAllMeasuring2()
        }
    }

    fun deleteMeasuring2(measuring2: Measuring2){
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteMeasuring2(measuring2)
        }
    }
}