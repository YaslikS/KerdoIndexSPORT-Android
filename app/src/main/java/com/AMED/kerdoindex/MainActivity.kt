package com.AMED.kerdoindex

//import com.AMED.kerdoindex.R
//import com.AMED.kerdoindex.databinding.ActivityMainBinding
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.AMED.kerdoindex.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private val mainFragment = MainFragment()
    private var binding: ActivityMainBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(LayoutInflater.from(this))
        setContentView(binding?.root)

        openFragment(mainFragment)
    }

    private fun openFragment(twoFrag: Fragment) {
        val fragmentManager: FragmentManager = supportFragmentManager
        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.apply {
            replace(R.id.container, twoFrag)
            commit()
        }
    }

//    binding?.nameUserEditText?.addTextChangedListener(object : TextWatcher{
//        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
//        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
//        override fun afterTextChanged(s: Editable) {
//            nameUser = binding?.nameUserEditText?.text.toString()
//        }
//    })
}