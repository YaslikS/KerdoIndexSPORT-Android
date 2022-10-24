package com.AMED.kerdoindex.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.AMED.kerdoindex.model.Measuring.Measuring
import com.AMED.kerdoindex.model.Measuring.MeasuringDao
import com.AMED.kerdoindex.model.Measuring2.Measuring2
import com.AMED.kerdoindex.model.Measuring2.Measuring2Dao

@Database(
    entities = [Measuring::class, Measuring2::class],
    version = 6,
    exportSchema = false
) abstract class MeasureDatabase : RoomDatabase() {
    abstract fun measuringDao(): MeasuringDao
    abstract fun measuring2Dao(): Measuring2Dao

    companion object {
        @Volatile
        private var INSTANCE: MeasureDatabase? = null

        fun getDatabase(context: Context): MeasureDatabase{
            val tempInstance = INSTANCE
            if(tempInstance != null){
                return tempInstance
            }
            synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MeasureDatabase::class.java,
                    "measuring_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                return instance
            }
        }
    }

}


/*
@Database(
    entities = [Measuring::class],
    version = 1
)
abstract class MeasureDatabase: RoomDatabase() {
    abstract fun getMeasureDao(): MeasuringDao

    companion object{
        @Volatile private var instance : MeasureDatabase? = null
        private val LOCK = Any()

        operator fun invoke(context: Context) = instance ?: synchronized(LOCK){
            instance ?: buildDatabase(context).also {
                instance = it
            }
        }

        private fun buildDatabase(context: Context) = Room.databaseBuilder(
            context.applicationContext,
            MeasureDatabase::class.java,
            "measuredatabase"
        ).build()
    }

}
*/