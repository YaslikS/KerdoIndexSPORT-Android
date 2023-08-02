package com.AMED.kerdoindex.fireBaseManagers

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.AMED.kerdoindex.model.json.SharedPreferencesManager
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import kotlin.math.log


class FireBaseAuthManager(context: Context) {

    private val TAG = "kerdoindex.FBAM"
    private var fbAuth: FirebaseAuth? = null
    private var context = context
    var authWas = false
    private var sharedPreferencesManager: SharedPreferencesManager? = null

    var emailUser: String? = null

    init {
        fbAuth = FirebaseAuth.getInstance()
        sharedPreferencesManager = SharedPreferencesManager(context)
    }

    // состояние авторизации
    fun stateAuth(): Boolean{
        return if (fbAuth?.currentUser != null){     // авторизован
            Log.i(TAG, "stateAuth: entrance: mAuth?.currentUser != null")
            emailUser = fbAuth?.currentUser?.email!!
            true
        } else {                                    //  не авторизован
            Log.i(TAG, "stateAuth: entrance: mAuth?.currentUser == null")
            false
        }
    }

    // повторная авторизация
    fun reAuth(resultReAuth: (Int, String) -> Unit){
        Log.i(TAG, "reAuth: entrance")
        fbAuth?.currentUser?.reauthenticate(EmailAuthProvider.getCredential(
            sharedPreferencesManager?.getYourEmail()!!,
            sharedPreferencesManager?.getPassword()!!
        ))?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.i(TAG, "reAuth: User re-authenticated" + fbAuth?.currentUser?.uid)
                resultReAuth(0, "")
            } else {
                val errorCode = (task.exception as FirebaseAuthException?)!!.errorCode
                when (errorCode) {
                    "ERROR_WRONG_PASSWORD" -> {
                        Log.i(TAG, "auth: ERROR_WRONG_PASSWORD : $errorCode")
                        logOut()
                        resultReAuth(2, "")
                    }
                    "ERROR_USER_NOT_FOUND" -> {
                        Log.i(TAG, "auth: ERROR_USER_NOT_FOUND : $errorCode")
                        logOut()
                        resultReAuth(3, "")
                    }
                    "ERROR_NETWORK_REQUEST_FAILED" -> {
                        Log.i(TAG, "auth: ERROR_NETWORK_REQUEST_FAILED : $errorCode")
                        resultReAuth(4, "")
                    }
                    else -> {
                        Log.i(TAG, "reAuth: User NOT re-authenticated: ${task.exception}")
                        logOut()
                        resultReAuth(1, "Error: ${task.exception}")
                    }
                }
            }
        }
    }

    // авторизация
    fun auth(email: String, pass: String, resultAuth: (Int, String) -> Unit) {
        Log.i(TAG, "auth: entrance")
        fbAuth!!.createUserWithEmailAndPassword(email, pass).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.i(TAG,
                    "auth: User auth Successful " + fbAuth?.currentUser?.uid
                )
                emailUser = fbAuth?.currentUser?.email!!
                sharedPreferencesManager?.saveIdUser(fbAuth?.currentUser?.uid!!)
                resultAuth(0, "")
            } else {
                Log.i(TAG, "auth: User auth failed : ${task.exception}")
                //Log.i(TAG, "auth: task.result : ${task.result}")
                val errorCode = (task.exception as FirebaseAuthException?)!!.errorCode
                when (errorCode) {
                    "ERROR_EMAIL_ALREADY_IN_USE" -> {
                        Log.i(TAG, "auth: ERROR_EMAIL_ALREADY_IN_USE : $errorCode")
                        resultAuth(2, "")
                    }
                    "ERROR_NETWORK_REQUEST_FAILED" -> {
                        Log.i(TAG, "auth: ERROR_NETWORK_REQUEST_FAILED : $errorCode")
                        resultAuth(3, "")
                    }
                    else -> {
                        Log.i(TAG, "auth: error : $errorCode")
                        resultAuth(1, "Error: $errorCode")
                    }
                }
            }
        }
        Log.i(TAG, "auth: exit")
    }

    // вход
    fun login(email: String, pass: String, resultLogin: (Int, String) -> Unit) {
        Log.i(TAG, "login: entrance")

//        fbAuth!!.signInWithEmailAndPassword(email, pass).addOnSuccessListener {task ->
//            Log.i(TAG, "deleteAccount: User delete Successful " + fbAuth?.currentUser?.uid)
//        }?.addOnFailureListener {
//            Log.i(TAG, "deleteAccount: it.localizedMessage: " + it.localizedMessage)
//
//        }

        fbAuth!!.signInWithEmailAndPassword(email, pass).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.i(TAG, "login: User login Successful " + fbAuth?.currentUser?.uid)
                sharedPreferencesManager?.saveIdUser(fbAuth?.currentUser?.uid!!)
                emailUser = fbAuth?.currentUser?.email!!
                resultLogin(0, "")
            } else {
                val errorCode = (task.exception as FirebaseAuthException?)!!.errorCode
                when (errorCode) {
                    "ERROR_WRONG_PASSWORD" -> {
                        Log.i(TAG, "auth: ERROR_WRONG_PASSWORD : $errorCode")
                        resultLogin(2, "")
                    }
                    "ERROR_USER_NOT_FOUND" -> {
                        Log.i(TAG, "auth: ERROR_USER_NOT_FOUND : $errorCode")
                        resultLogin(3, "")
                    }
                    "ERROR_NETWORK_REQUEST_FAILED" -> {
                        Log.i(TAG, "auth: ERROR_NETWORK_REQUEST_FAILED : $errorCode")
                        resultLogin(4, "")
                    }
                    else -> {
                        Log.i(TAG, "login: User login failed : ${task.exception}")
                        Log.i(TAG, "auth: task.result : ${task.result}")
                        resultLogin(1, "Error: ${task.exception}")
                    }
                }
            }
        }
    }

    // подтверждение по почте
    private fun sendEmailVerf() {
        val user = fbAuth?.currentUser
        user?.sendEmailVerification()?.addOnCompleteListener {
            if (it.isSuccessful) {
                Toast.makeText(context, "Проверьте ваш Email", Toast.LENGTH_SHORT)
            } else {
                Toast.makeText(context, "Send Email Failed", Toast.LENGTH_SHORT)
            }
        }
    }

    // выход из аккаунта
    fun logOut() {
        FirebaseAuth.getInstance().signOut()
        Log.i(TAG, "logOut: User logOut Successful " + fbAuth?.currentUser?.uid)
    }

    // удаление аккаунта
    fun deleteAccount(resultDel: (Int, String) -> Unit){
        Log.i(TAG, "deleteAccount: entrance " + fbAuth?.currentUser?.uid)
        fbAuth?.currentUser?.delete()?.addOnSuccessListener {task ->
            Log.i(TAG, "deleteAccount: User delete Successful " + fbAuth?.currentUser?.uid)
            resultDel(0, "")
        }?.addOnFailureListener {
            Log.i(TAG, "deleteAccount: it.localizedMessage: " + it.localizedMessage)
            resultDel(1, "Error: ${it.localizedMessage}")
        }
    }

}