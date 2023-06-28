package com.AMED.kerdoindex.view

import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.AMED.kerdoindex.R
import com.AMED.kerdoindex.databinding.FragmentProfileSportsmanBinding
import com.AMED.kerdoindex.fireBaseManagers.FireBaseAuthManager
import com.AMED.kerdoindex.fireBaseManagers.FireBaseCloudManager
import com.AMED.kerdoindex.fireBaseManagers.hasConnection
import com.AMED.kerdoindex.model.SharedPreferencesManager
import com.AMED.kerdoindex.model.sha256
import kotlinx.coroutines.*

class ProfileSportsmanFragment : Fragment() {

    private val TAG = "kerdoindex.ProfileSport"
    private var binding: FragmentProfileSportsmanBinding? = null
    private var sharedPreferencesManager: SharedPreferencesManager? = null
    private var fireBaseAuthManager: FireBaseAuthManager? = null
    private var fireBaseCloudManager: FireBaseCloudManager? = null
    private var emailValid = false
    private var passValid = false
    private var checkingReachability = CoroutineScope(Dispatchers.IO)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.i(TAG, "onCreateView: entrance")
        binding = FragmentProfileSportsmanBinding.inflate(inflater, container, false)

        sharedPreferencesManager = SharedPreferencesManager(requireActivity())
        fireBaseAuthManager = FireBaseAuthManager(requireActivity())
        fireBaseCloudManager = FireBaseCloudManager(requireActivity())

        Log.i(TAG, "onCreateView: exit")
        return binding?.root
    }

    // состояние интернета
    // наблюдение за ним
    private fun startCheckingReachability() {
        Log.i(TAG, "startCheckingReachability: entrance")
        checkingReachability = CoroutineScope(Dispatchers.IO)
        checkingReachability?.launch(Dispatchers.IO) {
            Log.i(TAG, "startCheckingReachability: checkingReachability?.launch")
            while (true) {
                when (hasConnection(requireActivity())) {
                    true -> {
                        launch(Dispatchers.Main) { binding?.loginButton?.isClickable = true }
                    }
                    false -> {
                        launch(Dispatchers.Main) { binding?.loginButton?.isClickable = false }
                    }
                }
                delay(1000)
            }
        }
    }

    // слушатели EditTexts
    private fun checkEmailETs() {
        //  changed textfield yourEmailTF
        binding?.yourEmailTF?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                binding?.yourEmailTF?.error = null
                Log.i(
                    TAG,
                    "checkEmailETs: yourEmailTF: sharedPreferencesManage!!.getPassword() = " + sharedPreferencesManager!!.getYourEmail()
                )
                emailETsError(binding?.yourEmailTF!!)
            }
        })
        //  changed textfield yourPassTF
        binding?.yourPassTF?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(p0: Editable?) {
                binding?.yourPassTF?.error = null
                Log.i(
                    TAG,
                    "checkEmailETs: yourPassTF: sharedPreferencesManage!!.getPassword() = " + sharedPreferencesManager!!.getPassword()
                )
                if (binding?.yourPassTF?.text?.isNotEmpty()!! && binding?.yourPassTF?.text?.length!! >= 8) {
                    passValid = true
                    binding?.yourPassTF?.error = null
                } else {
                    passValid = false
                    binding?.yourPassTF?.error = "The password must be at least 8 characters"
                }
            }
        })
        //  changed textfield yourNameTF
        binding?.yourNameTF?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(p0: Editable?) {
                binding?.yourNameTF?.error = null
                sharedPreferencesManager!!.saveYourName(binding?.yourNameTF?.text.toString())
                if (fireBaseAuthManager?.stateAuth()!!)
                    fireBaseCloudManager?.updateNameInCloudData()
                Log.i(
                    TAG,
                    "checkEmailETs: yourNameTF: sharedPreferencesManage!!.getYourName() = " + sharedPreferencesManager!!.getYourName()
                )
            }
        })
    }

    // если адрес неправильный, выводим ошибки
    private fun emailETsError(edt: EditText) {
        Log.i(TAG, "emailETsError: entrance")
        if (!isEmailValid(edt.text.toString())!!) { //  если почта невалидная
            Log.i(TAG, "emailETsError: email is not valid")
            edt.error = "Неправильный адрес"
            when (edt.id) {
                R.id.yourEmailTF -> {
                    emailValid = false
                    Log.i(TAG, "yourEmailTF: email is not valid")
                }
            }
        } else {                                    //  если почта валидная
            Log.i(TAG, "emailETsError: email is valid")
            when (edt.id) {
                R.id.yourEmailTF -> {
                    emailValid = true
                    Log.i(TAG, "yourEmailTF: email is valid")
                }
            }
        }
    }

    // проверяем адрес
    private fun isEmailValid(email: CharSequence?): Boolean? {
        Log.i(TAG, "isEmailValid: entrance")
        Log.i(TAG, "isEmailValid: email: $email")
        Log.i(
            TAG,
            "isEmailValid: result: ${email?.let { Patterns.EMAIL_ADDRESS.matcher(it).matches() }}"
        )
        return email?.let { Patterns.EMAIL_ADDRESS.matcher(it).matches() }
    }

    // слушатели кнопок
    private fun clickListeners() {
        Log.i(TAG, "clickListeners: entrance")
        // прослушивает кнопку логина
        binding?.loginButton?.setOnClickListener {
            Log.i(TAG, "buttonListeners: loginButton: entrance")
            if (emailValid) {
                Log.i(TAG, "clickListeners: emailValid == true")
                sharedPreferencesManager!!.saveYourEmail(binding?.yourEmailTF?.text.toString())
                sharedPreferencesManager!!.savePassword(/////////////////////////////////
                    binding?.yourPassTF?.text.toString().sha256()
                )
                sharedPreferencesManager!!.saveYourName(binding?.yourNameTF?.text.toString())
                loginAction()
            } else {
                Log.i(TAG, "clickListeners: emailValid == false")
                sharedPreferencesManager!!.saveYourEmail("0")
            }
        }
        // прослушивает кнопку назад
        binding?.backTVInProfileFragment?.setOnClickListener {
            Log.i(TAG, "buttonListeners: backTVInProfileFragment: entrance")
            activity?.supportFragmentManager?.popBackStack()
        }
    }

    // действия при нажатии кнопки логина
    private fun loginAction() {
        Log.i(TAG, "loginAction: entrance")
        if (fireBaseAuthManager?.stateAuth() == true) {
            Log.i(TAG, "loginAction: logout: stateAuth() == true")
            AlertDialog.Builder(requireActivity())
                .setTitle("Log out of your account")
                .setMessage("Are you sure you want logout?")
                .setNegativeButton("Logout") { _, _ ->
                    Log.i(TAG, "loginAction: logout: entrance")
                    fireBaseAuthManager?.logOut()
                    binding?.loginMainTV?.text = "Log in to your kerdoIndex account:"
                    binding?.loginButton?.text = "login"
                    binding?.loginButton?.setBackgroundColor(
                        ContextCompat.getColor(
                            requireActivity(),
                            R.color.accent_color
                        )
                    )
                    deleteUserInfo()
                }.setNeutralButton("Delete Account") { _, _ ->
                    Log.i(TAG, "loginAction: delete Account: entrance")
                    fireBaseAuthManager?.deleteAccount()
                    binding?.loginMainTV?.text = "Log in to your kerdoIndex account:"
                    binding?.loginButton?.text = "login"
                    binding?.loginButton?.setBackgroundColor(
                        ContextCompat.getColor(
                            requireActivity(),
                            R.color.accent_color
                        )
                    )
                    fireBaseCloudManager?.deleteInCloudData()
                    deleteUserInfo()
                }
                .setPositiveButton("Cancel", null).show()
        } else {
            Log.i(TAG, "loginAction: stateAuth() == false")
            if ((!TextUtils.isEmpty(binding?.yourEmailTF?.text.toString())
                        && !TextUtils.isEmpty(binding?.yourPassTF?.text.toString())
                        && !TextUtils.isEmpty(binding?.yourNameTF?.text.toString()))
                        && passValid
            ) {
                Log.i(TAG, "loginAction: TF is not null")
                binding?.progressCL?.visibility = ConstraintLayout.VISIBLE
                fireBaseAuthManager?.login(
                    binding?.yourEmailTF?.text.toString(),
                    sharedPreferencesManager?.getPassword()!!,/////////////////////////////////
                    ::resultAuth
                )
            } else {
                Log.i(TAG, "loginAction: TF is null!")
                if (TextUtils.isEmpty(binding?.yourEmailTF?.text.toString())) {
                    binding?.yourEmailTF?.error = "Empty field"
                }
                if (TextUtils.isEmpty(binding?.yourPassTF?.text.toString())) {
                    binding?.yourPassTF?.error = "Empty field"
                }
                if (TextUtils.isEmpty(binding?.yourNameTF?.text.toString())) {
                    binding?.yourNameTF?.error = "Empty field"
                }
            }
        }
    }

    // результат авторизации
    private fun resultAuth(state: Int) {
        Log.i(TAG, "resultAuth: entrance")
        when (state) {
            0 -> {  //  неудачная авторизация
                Log.i(TAG, "resultAuth: entrance: state = $state")
                AlertDialog.Builder(requireActivity())
                    .setTitle("Invalid Email or password")
                    .setPositiveButton("OK") { _, _ ->
                        binding?.progressCL?.visibility = ConstraintLayout.INVISIBLE
                        Log.i(TAG, "resultAuth: AlertDialog: OK")
                    }.show()
            }
            1 -> {  //  удачная авторизация
                Log.i(TAG, "resultAuth: state = $state")
                if (fireBaseAuthManager?.authWas!!) {   // пользователь НЕ существует
                    binding?.loginButton?.text = "logout from " + fireBaseAuthManager?.emailUser
                    binding?.loginButton?.setBackgroundColor(
                        ContextCompat.getColor(requireActivity(), R.color.redGraph)
                    )
                    binding?.loginMainTV?.text =
                        "You loggined with " + fireBaseAuthManager?.emailUser
                    Log.i(TAG, "resultAuth: state = 1: authWas = true")
                    fireBaseCloudManager?.addUserInCloudData()
                    binding?.progressCL?.visibility = ConstraintLayout.INVISIBLE
                } else {    // пользователь существует
                    fireBaseCloudManager?.getTypeUser(
                        binding?.yourEmailTF?.text.toString(),
                        ::resultTypeUser
                    )
                }
            }
        }
    }

    // результат проверки на тип пользователя
    private fun resultTypeUser(state: Int, typeUser: String?) {
        Log.i(TAG, "resultTypeUser: entrance")
        when (state) {
            1 -> {
                Log.i(TAG, "resultTypeUser: state = 1")
                if (typeUser == "s") {
                    Log.i(TAG, "resultTypeUser: entrance")
                    binding?.loginButton?.text = "logout from " + fireBaseAuthManager?.emailUser
                    binding?.loginButton?.setBackgroundColor(
                        ContextCompat.getColor(requireActivity(), R.color.redGraph)
                    )
                    binding?.loginMainTV?.text =
                        "You loggined with " + fireBaseAuthManager?.emailUser
                    if (fireBaseAuthManager?.authWas!!) {   // пользователь НЕ существует
                        Log.i(TAG, "resultAuth: state = 1: authWas = true")
                        fireBaseCloudManager?.addUserInCloudData()
                    } else {    // пользователь существует
                        Log.i(TAG, "resultAuth: state = 1: authWas = false")
                        fireBaseCloudManager?.getCloudData()
                        fireBaseCloudManager?.updateNameInCloudData()
                    }
                    binding?.progressCL?.visibility = ConstraintLayout.INVISIBLE
                } else {
                    fireBaseAuthManager?.logOut()
                    deleteUserInfo()
                    binding?.progressCL?.visibility = ConstraintLayout.INVISIBLE
                    AlertDialog.Builder(requireActivity())
                        .setTitle("You are already registered as a trainer")
                        .setPositiveButton("OK") { _, _ ->
                            Log.i(TAG, "resultAuth: AlertDialog: OK")
                        }.show()
                }
            }
            0 -> {
                Log.i(TAG, "resultTypeUser: state = 1")
                fireBaseAuthManager?.logOut()
                deleteUserInfo()
                binding?.progressCL?.visibility = ConstraintLayout.INVISIBLE
                AlertDialog.Builder(requireActivity())
                    .setTitle("Unable to verify if you are a sportsman")
                    .setPositiveButton("OK") { _, _ ->
                        Log.i(TAG, "resultAuth: AlertDialog: OK")
                    }.show()
            }
        }
    }

    // удаление всех данных
    private fun deleteUserInfo() {
        Log.i(TAG, "deleteUserInfo: entrance")
        sharedPreferencesManager?.savePassword("0")
        sharedPreferencesManager?.saveYourEmail("0")
        sharedPreferencesManager?.saveYourName("0")
        sharedPreferencesManager?.saveIdUser("")
        sharedPreferencesManager?.saveYourImageURL("")
    }

    // настройка view
    private fun settingsViews() {
        Log.i(TAG, "settingsViews: entrance")
        binding?.yourEmailTF?.setText("")
        binding?.yourNameTF?.setText("")
        binding?.yourPassTF?.setText("")
        startCheckingReachability()
        checkEmailETs()     // запуск слушателей editText
        clickListeners()    // запуск слушателей нажатий
        Log.i(
            TAG,
            "settingsViews: sharedPreferencesManage!!.getYourName() = " + sharedPreferencesManager!!.getYourName()
        )
        if (sharedPreferencesManager!!.getYourName() != "0" && sharedPreferencesManager!!.getYourName() != "") {
            binding?.yourNameTF?.setText(sharedPreferencesManager!!.getYourName())
        }
        Log.i(
            TAG,
            "settingsViews: sharedPreferencesManage!!.getPassword() = " + sharedPreferencesManager!!.getPassword()
        )
        if (sharedPreferencesManager!!.getPassword() != "0") {
            binding?.yourPassTF?.setText("")
            binding?.yourPassTF?.hint = "******** - Your password"
            binding?.yourPassTF?.error = null
        }
        Log.i(
            TAG,
            "settingsViews: sharedPreferencesManage!!.getYourEmail() = " + sharedPreferencesManager!!.getYourEmail()
        )
        if (sharedPreferencesManager!!.getYourEmail() != "0") {
            binding?.yourEmailTF?.setText(sharedPreferencesManager!!.getYourEmail())
        }

        if (fireBaseAuthManager?.stateAuth() == true) {
            Log.i(TAG, "settingsViews: stateAuth() == true")
            binding?.loginMainTV?.text = "You loggined with " + fireBaseAuthManager?.emailUser
            binding?.loginButton?.text = "logout from " + fireBaseAuthManager?.emailUser
            binding?.loginButton?.setBackgroundColor(
                ContextCompat.getColor(
                    requireActivity(),
                    R.color.redGraph
                )
            )
        } else {
            Log.i(TAG, "settingsViews: stateAuth() == false")
            binding?.loginMainTV?.text = "Log in to your kerdoIndex account:"
            binding?.loginButton?.text = "login"
            binding?.loginButton?.setBackgroundColor(
                ContextCompat.getColor(
                    requireActivity(),
                    R.color.accent_color
                )
            )
        }
    }

    override fun onResume() {
        super.onResume()
        settingsViews()
    }

    override fun onDestroyView() {
        Log.i(TAG, "onDestroyView: entrance")
        checkingReachability.cancel()
        binding = null
        super.onDestroyView()
    }
}