package com.AMED.kerdoindex.model.Measuring

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "Measuring")
data class Measuring(
    @PrimaryKey(autoGenerate = true)
    var id: Int,
    val DAD: Double,
    val Pulse: Double,
    val KerdoIndex: Double,
    val number: Int,
    val date: String
): Parcelable