package com.AMED.kerdoindex.fireBaseManagers

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.AMED.kerdoindex.model.SharedPreferencesManager
import com.AMED.kerdoindex.model.User
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class FireBaseCloudManager(context: Context) {

    private val TAG = "kerdoindex.FBCM"
    private val db = Firebase.firestore
    private var sharedPreferencesManager: SharedPreferencesManager? = null

    init {
        sharedPreferencesManager = SharedPreferencesManager(context)
    }

    // создание пользователя
    fun addUserInCloudData() {
        Log.i(TAG, "addUserInCloudData: entrance")
        var jsonForUpdate = if (sharedPreferencesManager?.getJson() == "empty") { ""
        } else { sharedPreferencesManager?.getJson()!! }
        var user = User(
            sharedPreferencesManager?.getIdUser(),
            "s",
            sharedPreferencesManager?.getYourName(),
            sharedPreferencesManager?.getYourEmail(),
            sharedPreferencesManager?.getYourImageURL(),
            "",
            sharedPreferencesManager?.getLastDate(),
            jsonForUpdate,
            "", "", "", "", "", ""
        )
        Log.i(
            TAG,
            "addUserInCloudData: user.id = " + user.id + " user.name = " + user.name + " user.email " + user.email
        )

        db.collection("users")
            .document(sharedPreferencesManager?.getIdUser()!!)
            .set(user)
            .addOnSuccessListener { documentReference ->
                Log.i(
                    TAG,
                    "addUserInCloudData: DocumentSnapshot added with ID: ${documentReference}"
                )
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "addUserInCloudData: Error add user.", exception)
            }
    }

    // обновление json с измерениями
    fun updateJsonInCloudData() {
        Log.i(
            TAG,
            "updateJsonInCloudData: userId = " + sharedPreferencesManager?.getIdUser()!!
                    + " jsonUser = { " + sharedPreferencesManager?.getJson()!! + " }"
        )
        var jsonForUpdate = if (sharedPreferencesManager?.getJson() == "empty") {
            ""
        } else {
            sharedPreferencesManager?.getJson()!!
        }
        db.collection("users")
            .document(sharedPreferencesManager?.getIdUser()!!)
            .update("json", jsonForUpdate)
            .addOnSuccessListener { documentReference ->
                Log.d(TAG, "updateJsonInCloudDataЖ DocumentSnapshot added with ID: ${documentReference}")
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "updateJsonInCloudDataЖ Error editing user info.", exception)
            }
    }

    // обновление даты последнего обновления бд
    fun updateLastDateInCloudData() {
        Log.i(
            TAG,
            "updateLastDateInCloudData: userId = " + sharedPreferencesManager?.getIdUser()!!
                    + " lastDateUser = " + sharedPreferencesManager?.getLastDate()!!
        )
        db.collection("users")
            .document(sharedPreferencesManager?.getIdUser()!!)
            .update("lastDate", sharedPreferencesManager?.getLastDate())
            .addOnSuccessListener { documentReference ->
                Log.d(TAG, "updateLastDateInCloudData: DocumentSnapshot added with ID: ${documentReference}")
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "updateLastDateInCloudData: Error editing user info.", exception)
            }
    }

    // обновление имени
    fun updateNameInCloudData() {
        Log.i(
            TAG,
            "updateNameInCloudData: userId = " + sharedPreferencesManager?.getIdUser()!!
                    + " nameUser = " + sharedPreferencesManager?.getYourName()!!
        )
        db.collection("users")
            .document(sharedPreferencesManager?.getIdUser()!!)
            .update("name", sharedPreferencesManager?.getYourName())
            .addOnSuccessListener { documentReference ->
                Log.d(TAG, "updateNameInCloudData: DocumentSnapshot added with ID: ${documentReference}")
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "updateNameInCloudData: Error update Name info.", exception)
            }
    }

    // обновление url иконки
    fun updateUrlIconInCloudData() {
        Log.i(
            TAG,
            "addUserInCloudData: userId = " + sharedPreferencesManager?.getIdUser()!!
                    + " iconUrlUser = { " + sharedPreferencesManager?.getYourImageURL()!! + " }"
        )
        db.collection("users")
            .document(sharedPreferencesManager?.getIdUser()!!)
            .update("iconUrl", sharedPreferencesManager?.getYourImageURL())
            .addOnSuccessListener { documentReference ->
                Log.d("editInCloudData", "DocumentSnapshot added with ID: ${documentReference}")
            }
            .addOnFailureListener { exception ->
                Log.w("editInCloudData", "Error update Url Icon info.", exception)
            }
    }

    // удаление пользователя
    fun deleteInCloudData() {
        Log.i(TAG, "deleteInCloudData: entrance")
        db.collection("users")
            .document(sharedPreferencesManager?.getIdUser()!!)
            .delete()
            .addOnSuccessListener { documentReference ->
                Log.d(
                    TAG,
                    "deleteInCloudData: DocumentSnapshot added with ID: ${documentReference}"
                )
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "deleteInCloudData: Error delete documents.", exception)
            }
    }

    // получение данных пользователя
    fun getCloudData() {
        Log.i(TAG, "getCloudData: entrance")
        db.collection("users")
            .document(sharedPreferencesManager?.getIdUser()!!)
            .get()
            .addOnSuccessListener { result ->
                Log.i(TAG, "getCloudData: getted document = " + result.get("lastDate"))
                syncData(result)
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "getCloudData: Error getting documents.", exception)
            }
    }

    // синхронизация данных между сервером и телефоном
    private fun syncData(result: DocumentSnapshot){
        if (result.get("lastDate") == "" || result.get("json") == ""){  //  если облако пустое
            Log.i(TAG, "syncData: cloud is empty")
            updateJsonInCloudData()
            updateLastDateInCloudData()
        } else {    //  если облако НЕ пустое
            if (sharedPreferencesManager?.getLastDate()!! != ""){    //  телефон НЕ пуст
                val resultOfComparison = dataComparison(result)
                if (resultOfComparison > 0){        //  облако актуальнее телефона
                    Log.i(TAG, "syncData: resultOfComparison > 0")
                    sharedPreferencesManager?.saveLastDate(result.get("lastDate") as String)
                    sharedPreferencesManager?.saveJson(result.get("json") as String)
                } else if (resultOfComparison < 0){ //  телефон актуальнее облака
                    Log.i(TAG, "syncData: resultOfComparison < 0")
                    updateJsonInCloudData()
                    updateLastDateInCloudData()
                } else {
                    Log.i(TAG, "syncData: dates equal")
                }
            } else {    //  телефон пуст
                Log.i(TAG, "syncData: phone is empty")
                sharedPreferencesManager?.saveLastDate(result.get("lastDate") as String)
                sharedPreferencesManager?.saveJson(result.get("json") as String)
            }
        }
    }

    // сравнение дат
    private fun dataComparison(result: DocumentSnapshot) : Int {
        Log.i(TAG, "dataComparison: entrance")
        val cloudDateStr = result.get("lastDate") as String
        val phoneDateStr = sharedPreferencesManager?.getLastDate()

        Log.i(TAG, "dataComparison: cloudDateStr = $cloudDateStr")
        val pattern = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")
        val cloudDate = LocalDateTime.parse(cloudDateStr, pattern)
        val phoneDate = LocalDateTime.parse(phoneDateStr, pattern)

        Log.i(TAG, "dataComparison: exit")
        return cloudDate.compareTo(phoneDate)   //  положительное, если облако актуальнее телефона
    }

    // получение типа пользователя
    fun getTypeUser(email: String, resultGetTypeUser: (Int, String?) -> Unit){
        Log.i(TAG, "getTypeUser: entrance")
        db.collection("users")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { documents ->
                Log.i(TAG, "getTypeUser: type user = " + documents.documents[0].get("type"))
                val typeStr = documents.documents[0].get("type") as String
                resultGetTypeUser(1, typeStr)
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "getTypeUser: Error getting documents.", exception)
                resultGetTypeUser(0, null)
            }
    }

}