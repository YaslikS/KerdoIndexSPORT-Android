package com.AMED.kerdoindex.view

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.AMED.kerdoindex.BuildConfig
import com.AMED.kerdoindex.databinding.FragmentAboutBinding


class AboutFragment : Fragment() {

    private val TAG = "kerdoindex.AboutFragment"
    private var binding: FragmentAboutBinding? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.i(TAG, "onCreateView: entrance")
        binding = FragmentAboutBinding.inflate(inflater, container, false)

        settingsViews()

        Log.i(TAG, "onCreateView: exit")
        return binding?.root
    }


    // слушатели нажатий
    private fun clickListeners() {
        // прослушивает кнопку назад
        binding?.backTVInProfileFragment?.setOnClickListener {
            Log.i(TAG, "buttonListeners: backTVInProfileFragment: entrance")
            activity?.supportFragmentManager?.popBackStack()
        }
    }

    // настройка view
    private fun settingsViews() {
        val pInfo = requireContext().packageManager.getPackageInfo(requireContext().packageName, 0)
        val version = pInfo.versionName
        binding?.nameAppTextView?.text = "KerdoIndexSPORT $version"

        binding?.infoAppTextView?.text =
            "KerdoIndexSPORT $version (" +
                    BuildConfig.VERSION_CODE +
                    ") / " + Build.MODEL +
                    " / Android " +
                    Build.VERSION.RELEASE +
                    "\n" + "OOO \"A-MED\" " +
                    "https://amed-rus.com/"

        clickListeners()    // запуск слушателей нажатий
    }


    override fun onDestroyView() {
        Log.i(TAG, "onDestroyView: entrance")
        binding = null
        super.onDestroyView()
    }

}