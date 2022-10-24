package com.AMED.kerdoindex

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.AMED.kerdoindex.databinding.FragmentProfileSportsmanBinding

class ProfileSportsmanFragment : Fragment() {
    private var binding: FragmentProfileSportsmanBinding? = null
    private var yourEmail: String? = null
    private var trinerEmail: String? = null
    private var yourNickname: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileSportsmanBinding.inflate(inflater, container, false)

        buttonListeners()
        checkEmailETs()

        return binding?.root
    }

    // слушатели EditTexts
    private fun checkEmailETs(){
        binding?.nameInAppET?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                yourNickname = binding?.nameInAppET?.text.toString()
            }
        })

        binding?.yourEmailET?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                emailETsError(binding?.yourEmailET!!)
            }
        })

        binding?.trainerEmailET?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                emailETsError(binding?.trainerEmailET!!)
            }
        })
    }

    // если адрес неправильный, выводим ошибки
    private fun emailETsError(edt: EditText) {
        if (!isEmailValid(edt.text.toString())!!) {
            edt.error = "Неправильный адрес"
            when (edt.id){
                R.id.yourEmailET -> yourEmail = null
                R.id.trainerEmailET -> trinerEmail = null
            }
        } else {
            when (edt.id){
                R.id.yourEmailET -> yourEmail = edt.text.toString()
                R.id.trainerEmailET -> trinerEmail = edt.text.toString()
            }
        }
    }

    // проверяем адрес
    private fun isEmailValid(email: CharSequence?): Boolean? {
        return email?.let { Patterns.EMAIL_ADDRESS.matcher(it).matches() }
    }

    // слушатели кнопок
    private fun buttonListeners(){
        binding?.backTVInProfileFragment?.setOnClickListener {
            activity?.onBackPressedDispatcher?.onBackPressed()
        }
    }

    override fun onDestroyView() {
        binding?.nameInAppET?.setText("")
        binding?.yourEmailET?.setText("")
        binding?.trainerEmailET?.setText("")
        //binding = null
        super.onDestroyView()
    }
}