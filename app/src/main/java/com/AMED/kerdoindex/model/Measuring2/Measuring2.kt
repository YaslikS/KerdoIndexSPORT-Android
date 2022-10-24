package com.AMED.kerdoindex.model.Measuring2

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "Measuring2")
data class Measuring2(
    @PrimaryKey(autoGenerate = true)
    var id: Int,
    val DAD: Double,
    val Pulse: Double,
    val KerdoIndex: Double,
    val number: Int,
    val date: String
): Parcelable