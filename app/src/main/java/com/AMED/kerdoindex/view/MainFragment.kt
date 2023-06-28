package com.AMED.kerdoindex.view

import android.animation.ObjectAnimator
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.AMED.kerdoindex.R
import com.AMED.kerdoindex.databinding.FragmentMainBinding
import com.AMED.kerdoindex.fireBaseManagers.FireBaseAuthManager
import com.AMED.kerdoindex.fireBaseManagers.FireBaseCloudManager
import com.AMED.kerdoindex.fireBaseManagers.hasConnection
import com.AMED.kerdoindex.model.Measure
import com.AMED.kerdoindex.model.MeasureJsonManager
import com.AMED.kerdoindex.model.SharedPreferencesManager
import com.AMED.kerdoindex.model.sha256
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import kotlinx.coroutines.*
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

class MainFragment : Fragment(), OnChartValueSelectedListener {

    private val TAG = "kerdoindex.MainFragment"
    private var binding: FragmentMainBinding? = null
    private val profileSportsmanFragment = ProfileSportsmanFragment()
    private var index1: Double? = null
    private var index2: Double? = null
    private var dad1: Double? = null
    private var dad2: Double? = null
    private var pulse1: Double? = null
    private var pulse2: Double? = null
    private var measures1 = mutableListOf<Measure>()
    private var measures2 = mutableListOf<Measure>()
    var dad1flag = false
    var dad2flag = false
    var pulse1flag = false
    var pulse2flag = false
    private var fireBaseAuthManager: FireBaseAuthManager? = null
    private var sharedPreferencesManager: SharedPreferencesManager? = null
    private var measureJsonManager: MeasureJsonManager? = null
    private var fireBaseCloudManager: FireBaseCloudManager? = null
    private var checkingReachability = CoroutineScope(Dispatchers.IO)

    // при запуске экрана
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.i(TAG, "onCreateView: entrance")
        binding = FragmentMainBinding.inflate(inflater, container, false)

        print("Hello, world!".sha256())
        Log.i(TAG, "onCreateView: " + "Hello, world!".sha256())

        editTextsHandler()  // запуск слушателей editText
        clickListeners()    // запуск слушателей нажатий

        sharedPreferencesManager = SharedPreferencesManager(requireActivity())
        fireBaseAuthManager = FireBaseAuthManager(requireActivity())
        measureJsonManager = MeasureJsonManager()
        fireBaseCloudManager = FireBaseCloudManager(requireActivity())

        Log.i(TAG, "onCreateView: exit")
        return binding?.root
    }

    override fun onStart() {
        Log.i(TAG, "onStart: entrance")
        super.onStart()
        tryAuth()
        installNameUser()
        gettingJson()
        Log.i(TAG, "onStart: exit")
    }

    override fun onResume() {
        super.onResume()
        startCheckingReachability()
    }

    override fun onPause() {
        super.onPause()
        checkingReachability.cancel()
    }

    // состояние интернета
    // наблюдение за ним
    private fun startCheckingReachability(){
        Log.i(TAG, "startCheckingReachability: entrance")
        checkingReachability = CoroutineScope(Dispatchers.IO)
        checkingReachability?.launch(Dispatchers.IO) {
            while (true){
                when (hasConnection(requireActivity())){
                    true -> {
                        //Log.i(TAG, "startCheckingReachability: true")
                        launch(Dispatchers.Main) {binding?.offlineModeButton?.visibility = Button.INVISIBLE}
                    }
                    false -> {
                        //Log.i(TAG, "startCheckingReachability: false")
                        launch(Dispatchers.Main) {binding?.offlineModeButton?.visibility = Button.VISIBLE}
                    }
                }
                delay(500)
            }
        }
    }

    // попытка авторизации
    private fun tryAuth(){
        Log.i(TAG, "tryAuth: entrance")
        if (!TextUtils.isEmpty(sharedPreferencesManager?.getYourEmail())
            && !TextUtils.isEmpty(sharedPreferencesManager?.getPassword())
        ) {
            Log.i(TAG, "tryAuth: getYourEmail && getPassword != empty")
            fireBaseAuthManager!!.login(
                sharedPreferencesManager?.getYourEmail()!!,
                sharedPreferencesManager?.getPassword()!!,
                ::resultAuth
            )
        }
        Log.i(TAG, "tryAuth: exit")
    }

    // слушатели нажатий
    private fun clickListeners() {
        Log.i(TAG, "clickListeners: entrance")
        // прослушивает кнопку сохранения измерения
        binding?.setKerdoIndexButton?.setOnClickListener {
            Log.i(TAG, "clickListeners: setKerdoIndexButton")
            AlertDialog.Builder(requireActivity())
                .setTitle("Saving a measuring")
                .setMessage("Are you sure you want to keep the measurement?")
                .setPositiveButton("Save") { dialog, which ->
                    Log.i(TAG, "clickListeners: setPositiveButton: entrance")
                    saveMeasure()
                    Log.i(TAG, "clickListeners: setPositiveButton: exit")
                }.setNegativeButton("Cancel", null).show()
        }
        // прослушивает кнопку очистки
        binding?.clearAllIndexButton?.setOnClickListener {
            Log.i(TAG, "clickListeners: clearAllIndexButton: entrance")
            clearAll()
        }
        // прослушивает кнопку перемещения к панели информации измерения
        binding?.KerdoIndexTVvalue?.setOnClickListener {
            Log.i(TAG, "clickListeners: KerdoIndexTVvalue: entrance")
            hideSoftKeyboard()
            scroll(true)
        }
        // прослушивает кнопку перемещения к панели информации измерения
        binding?.KerdoIndexTVvalue2?.setOnClickListener {
            Log.i(TAG, "clickListeners: KerdoIndexTVvalue2: entrance")
            hideSoftKeyboard()
            scroll(true)
        }
        // прослушивает кнопку перемещения к панели добавления измерения
        binding?.backToEnter?.setOnClickListener {
            Log.i(TAG, "clickListeners: backToEnter: entrance")
            hideSoftKeyboard()
            scroll(false)
        }
        // прослушивает кнопку перехода к экрану профиля
        binding?.GoToProfileButton?.setOnClickListener {
            Log.i(TAG, "clickListeners: GoToProfileButton: entrance")
            openFragment(profileSportsmanFragment)
        }
        // прослушивает кнопку скрытия панели информации измерения
        binding?.closeInfoMeasuringButton?.setOnClickListener {
            Log.i(TAG, "clickListeners: closeInfoMeasuringButton: entrance")
            binding?.dateMeasuringCardView?.visibility = CardView.INVISIBLE
        }
        Log.i(TAG, "clickListeners: exit")
    }

    // сохранить измерение
    private fun saveMeasure() {
        Log.i(TAG, "saveMeasure: entrance")
        var isDad1Pulse1Empty = false
        var isDad2Pulse2Empty = false
        //  проверка на пустые textField 1го измерения
        if (binding?.DADEnterET?.text.toString() == "" && binding?.PulseEnterET?.text.toString() == "") {
            dad1flag = true
            pulse1flag = true
            dad1 = 0.0
            pulse1 = 0.0
            index1 = 0.0
            isDad1Pulse1Empty = true
        }
        //  проверка на пустые textField 1го измерения
        if (binding?.DADEnterET2?.text.toString() == "" && binding?.PulseEnterET2?.text.toString() == "") {
            dad2flag = true
            pulse2flag = true
            dad2 = 0.0
            pulse2 = 0.0
            index2 = 0.0
            isDad2Pulse2Empty = true
        }
        Log.i(
            TAG,
            "saveMeasure: dad1flag:$dad1flag pulse1flag:$pulse1flag dad2flag:$dad2flag pulse2flag:$pulse2flag"
        )
        Log.i(TAG, "saveMeasure: dad1:$dad1 pulse1:$pulse1 dad2:$dad2 pulse2:$pulse2")
        //  проверка на все пустые textField
        if (dad1flag && dad2flag && pulse1flag && pulse2flag) {
            Log.i(TAG, "saveMeasure: all flag true")

            CoroutineScope(Dispatchers.IO).launch {
                val currentDate = Date()
                val dateFormat: DateFormat =
                    SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())
                val dateText = dateFormat.format(currentDate)

                val newMeasure1 =
                    Measure(
                        "", dad1.toString(), pulse1.toString(),
                        index1.toString(), date = dateText
                    )
                measures1.add(newMeasure1)
                val newMeasure2 =
                    Measure(
                        "", dad2.toString(), pulse2.toString(),
                        index2.toString(), date = dateText
                    )
                measures2.add(newMeasure2)
                val json = measureJsonManager?.createJson(measures1, measures2)
                sharedPreferencesManager?.saveJson(json!!)
                sharedPreferencesManager?.saveLastDate(dateText)
                if (fireBaseAuthManager?.stateAuth()!!) {
                    fireBaseCloudManager?.updateLastDateInCloudData()
                    fireBaseCloudManager?.updateJsonInCloudData()
                }
                launch(Dispatchers.Main) {
                    Log.i(TAG, "saveMeasure: Measures saved")
                    clearGraths()
                    gettingJson()
                    Log.i(TAG, "saveMeasure: graths updated")

                    hideSoftKeyboard()
                    if (isDad1Pulse1Empty) {
                        dad1flag = false
                        pulse1flag = false
                    }
                    if (isDad2Pulse2Empty) {
                        dad2flag = false
                        pulse2flag = false
                    }
                }
            }
        }
        Log.i(TAG, "saveMeasure: exit")
    }

    // очистить историю
    private fun clearAll() {
        Log.i(TAG, "clearAll: entrance")
        //  вывод alertDialog
        AlertDialog.Builder(requireActivity())
            .setTitle("Delete history")
            .setMessage("Are you sure you want to delete the history?")
            .setPositiveButton("Delete") { dialog, which ->
                //  очистка графика и данных из бд
                Log.i(TAG, "clearAll: setPositiveButton: entrance")
                measures1.clear()
                measures2.clear()
                sharedPreferencesManager?.saveJson("empty")
                sharedPreferencesManager?.saveLastDate("")
                clearGraths()
                jsonIsEmpty()
                if (fireBaseAuthManager?.stateAuth()!!) {
                    Log.i(TAG, "clearAll: setPositiveButton: stateAuth == true")
                    fireBaseCloudManager?.updateJsonInCloudData()
                }
                //clearEditTexts()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // очистить EditTexts
    private fun clearEditTexts() {
        Log.i(TAG, "clearEditTexts: entrance")
        binding?.DADEnterET?.setText("")
        binding?.PulseEnterET?.setText("")
        binding?.DADEnterET2?.setText("")
        binding?.PulseEnterET2?.setText("")
    }

    // очистить графы
    private fun clearGraths() {
        Log.i(TAG, "clearGraths: entrance")
        binding?.graphKedro?.invalidate()
        binding?.graphKedro?.clear()
        binding?.graphDAD?.invalidate()
        binding?.graphDAD?.clear()
        binding?.graphPulse?.invalidate()
        binding?.graphPulse?.clear()
    }

    // нажатие на столбец
    override fun onValueSelected(e: Entry?, h: Highlight?) {
        Log.i(TAG, "onValueSelected: column: " + e!!.x)
        // отображение панели добавления измерения
        visibleInfoMeasuring(e.x.toInt())
    }

    override fun onNothingSelected() {
        Log.i(TAG, "onNothingSelected")
    }

    // обработка результата авторизации
    private fun resultAuth(state: Int) {
        when (state) {
            0 -> {  //  неудачная авторизация
                Log.i(TAG, "resultAuth: entrance: state = $state")
                binding?.hatCL?.setBackgroundColor(ContextCompat.getColor(requireActivity(),
                    R.color.redGraph))
                binding?.appTitle?.text = "You not logged in!"
                CoroutineScope(Dispatchers.IO).launch {
                    delay(3000)
                    binding?.hatCL?.setBackgroundColor(ContextCompat.getColor(requireActivity(),
                        R.color.backgroundLinear))
                    delay(6000)
                    launch(Dispatchers.Main){ binding?.appTitle?.text = "KerdoIndexSPORT" }
                }
            }
            1 -> {  //  удачная авторизация
                Log.i(TAG, "resultAuth: entrance: state = $state")
                if (fireBaseAuthManager?.authWas!!) {
                    Log.i(TAG, "resultAuth: authWas == true")
                    fireBaseCloudManager?.addUserInCloudData()
                } else {
                    Log.i(TAG, "resultAuth: authWas == false")
                    fireBaseCloudManager?.getCloudData()
                }
            }
        }
    }

    // получение json с измерениями
    private fun gettingJson() {
        CoroutineScope(Dispatchers.IO).launch {
            Log.i(TAG, "gettingJson: entrance")
            val json = sharedPreferencesManager?.getJson()!!
            if (json != "empty" && json != "") {   //  проверка на отсутствие измерений
                Log.i(TAG, "gettingJson: json != empty")
                launch(Dispatchers.Main) {
                    jsonIsNotEmpty()
                }
                var js = measureJsonManager?.parcingJson(json)
                measures1 = js?.measures1!!
                measures2 = js?.measures2!!
                launch(Dispatchers.Main) { createGraths() }
            } else {
                Log.i(TAG, "gettingJson: json == empty")
                launch(Dispatchers.Main) {
                    jsonIsEmpty()
                }
            }
        }
        Log.i(TAG, "gettingJson: exit")
    }

    // json НЕ пустой
    private fun jsonIsNotEmpty(){
        Log.i(TAG, "jsonIsNotEmpty: entrance")
        binding?.graphKedro?.visibility = BarChart.VISIBLE
        binding?.graphDAD?.visibility = BarChart.VISIBLE
        binding?.graphPulse?.visibility = BarChart.VISIBLE
        binding?.jsonIsEmptyCL?.visibility = ConstraintLayout.INVISIBLE
    }

    // если json пустой
    private fun jsonIsEmpty(){
        Log.i(TAG, "jsonIsEmpty: entrance")
        binding?.graphKedro?.visibility = BarChart.INVISIBLE
        binding?.graphDAD?.visibility = BarChart.INVISIBLE
        binding?.graphPulse?.visibility = BarChart.INVISIBLE
        binding?.jsonIsEmptyCL?.visibility = ConstraintLayout.VISIBLE
    }

    // создание и настройка графов
    private fun createGraths() {
        Log.i(TAG, "createGraths: entrance")
        // если бд не пустая
        if (measures1.isNotEmpty() && measures2.isNotEmpty()) {
            Log.i(TAG, "createGraths: DB is not empty")
            createKedroChart()  // график кедро
            createPulseChart()  // график пульса
            createDadChart()    // график ДАД
        } else {
            Log.w(TAG, "createGraths: error: DB is empty")
        }
    }

    // заполнение графика кедро
    private fun createKedroChart() {
        Log.i(TAG, "createKedroChart: entrance")
        val values = ArrayList<BarEntry>()
        //  заполнение массива с данными для графика
        for (index in measures1.indices) {
            val entry = measures1[index]
            val entry2 = measures2[index]
            values.add(BarEntry((index + 1).toFloat(), entry.KerdoIndex?.toFloat()!!))
            values.add(BarEntry((index + 1).toFloat(), entry2.KerdoIndex?.toFloat()!!))
        }
        //  заполнение массива с цветами
        val colors = ArrayList<Int>()
        for (index in values) {
            if (index.y < -15.0) {
                colors.add(ContextCompat.getColor(requireActivity(), R.color.greenGraph))
                Log.i(TAG, "createKedroChart: R.color.greenGraph")
            } else if (15.0 < index.y) {
                colors.add(ContextCompat.getColor(requireActivity(), R.color.redGraph))
                Log.i(TAG, "createKedroChart: R.color.redGraph")
            } else {
                colors.add(ContextCompat.getColor(requireActivity(), R.color.yellowGraph))
                Log.i(TAG, "createKedroChart: R.color.yellowGraph")
            }
        }
        //  настройка данных графика
        val set = BarDataSet(values, "Values")
        set.colors = colors
        //  настройка отображения графика
        val data = BarData(set)
        data.setValueTextSize(12f)
        data.barWidth = 0.8f
        binding?.graphKedro?.isDragYEnabled = false
        binding?.graphKedro?.legend?.isEnabled = false
        binding?.graphKedro?.description?.isEnabled = false
        binding?.graphKedro?.isDoubleTapToZoomEnabled = false
        binding?.graphKedro?.data = data
        binding?.graphKedro?.xAxis?.isGranularityEnabled = true
        binding?.graphKedro?.xAxis?.granularity = 1f
        binding?.graphKedro?.setOnChartValueSelectedListener(this)
        binding?.graphKedro?.invalidate()
        binding?.graphKedro?.barData?.barWidth = 0.5F
        if (measures1.isNotEmpty()) {
            binding?.graphKedro?.moveViewToX(measures1.size.toFloat())
            binding?.graphKedro?.setVisibleXRangeMaximum(12F)
        }
        Log.i(TAG, "createKedroChart: exit")
    }

    // заполнение графика пульса
    private fun createPulseChart() {
        Log.i(TAG, "createPulseChart: entrance")
        val values = ArrayList<BarEntry>()
        //  заполнение массива с данными для графика
        for (index in measures1.indices) {
            val entry = measures1[index]
            val entry2 = measures2[index]
            values.add(BarEntry((index + 1).toFloat(), entry.Pulse?.toFloat()!!))
            values.add(BarEntry((index + 1).toFloat(), entry2.Pulse?.toFloat()!!))
        }
        //  настройка данных графика
        val set = BarDataSet(values, "Values")
        //  настройка отображения графика
        val data = BarData(set)
        data.setValueTextSize(12f)
        data.barWidth = 0.8f

        binding?.graphPulse?.isDragYEnabled = false
        binding?.graphPulse?.legend?.isEnabled = false
        binding?.graphPulse?.description?.isEnabled = false
        binding?.graphPulse?.isDoubleTapToZoomEnabled = false
        binding?.graphPulse?.data = data
        binding?.graphPulse?.setTouchEnabled(false)
        binding?.graphPulse?.xAxis?.isGranularityEnabled = true
        binding?.graphPulse?.xAxis?.granularity = 1f
        binding?.graphPulse?.invalidate()
        binding?.graphPulse?.barData?.barWidth = 0.5F
        if (measures1.isNotEmpty()) {
            binding?.graphPulse?.moveViewToX(measures1.size.toFloat())
            binding?.graphPulse?.setVisibleXRangeMaximum(12F)
        }
        Log.i(TAG, "createPulseChart: exit")
    }

    // заполнение графика дад
    private fun createDadChart() {
        Log.i(TAG, "createDadChart: entrance")
        val values = ArrayList<BarEntry>()
        //  заполнение массива с данными для графика
        for (index in measures1.indices) {
            val entry = measures1[index]
            val entry2 = measures2[index]
            values.add(BarEntry((index + 1).toFloat(), entry.DAD?.toFloat()!!))
            values.add(BarEntry((index + 1).toFloat(), entry2.DAD?.toFloat()!!))
        }
        //  настройка данных графика
        val set = BarDataSet(values, "Values")
        //  настройка отображения графика
        val data = BarData(set)
        data.setValueTextSize(12f)
        data.barWidth = 0.8f
        binding?.graphDAD?.isDragYEnabled = false
        binding?.graphDAD?.legend?.isEnabled = false
        binding?.graphDAD?.description?.isEnabled = false
        binding?.graphDAD?.isDoubleTapToZoomEnabled = false
        binding?.graphDAD?.data = data
        binding?.graphDAD?.setTouchEnabled(false)
        binding?.graphDAD?.xAxis?.isGranularityEnabled = true
        binding?.graphDAD?.xAxis?.granularity = 1f
        binding?.graphDAD?.invalidate()
        binding?.graphDAD?.barData?.barWidth = 0.5F
        if (measures1.isNotEmpty()) {
            binding?.graphDAD?.moveViewToX(measures1.size.toFloat())
            binding?.graphDAD?.setVisibleXRangeMaximum(12F)
        }
        Log.i(TAG, "createDadChart: exit")
    }

    // нажатие на столбец - отображение окна с инфо столбца
    private fun visibleInfoMeasuring(X: Int) {
        Log.i(TAG, "visibleInfoMeasuring: entrance")
        //  получение данных, которые будут выведен
        val Y1 = measures1?.get(X - 1)?.KerdoIndex?.toFloat()
        val Y2 = measures2?.get(X - 1)?.KerdoIndex?.toFloat()
        binding?.dateMeasuringTV?.text = measures1[X - 1].date
        binding?.indexMeasuringTV?.text = String.format("%.2f", Y1)
        binding?.indexMeasuringTV2?.text = String.format("%.2f", Y2)
        // лог
        Log.i(TAG, "onValueSelected: kerdoIndex1: $Y1")
        Log.i(TAG, "onValueSelected: kerdoIndex2: $Y2")
        Log.i(TAG, "onValueSelected: date: " + measures1[X - 1]?.date)

        if (-15.0 <= Y1!! && Y1 <= 15.0) {
            binding?.cardView?.setCardBackgroundColor(
                ContextCompat.getColor(requireActivity(), R.color.yellowGraph)
            )
            Log.i(TAG, "visibleInfoMeasuring: R.color.yellowGraph for Y1")
        } else
            if (Y1 < -15.0) {
                binding?.cardView?.setCardBackgroundColor(
                    ContextCompat.getColor(requireActivity(), R.color.greenGraph)
                )
                Log.i(TAG, "visibleInfoMeasuring: R.color.greenGraph for Y1")
            } else {
                binding?.cardView?.setCardBackgroundColor(
                    ContextCompat.getColor(requireActivity(), R.color.redGraph)
                )
                Log.i(TAG, "visibleInfoMeasuring: R.color.redGraph for Y1")
            }

        if (-15.0 <= Y2!! && Y2 <= 15.0) {
            binding?.cardView2?.setCardBackgroundColor(
                ContextCompat.getColor(requireActivity(), R.color.yellowGraph)
            )
            Log.i(TAG, "visibleInfoMeasuring: R.color.yellowGraph for Y2")
        } else
            if (Y2 < -15.0) {
                binding?.cardView2?.setCardBackgroundColor(
                    ContextCompat.getColor(requireActivity(), R.color.greenGraph)
                )
                Log.i(TAG, "visibleInfoMeasuring: R.color.greenGraph for Y2")
            } else {
                binding?.cardView2?.setCardBackgroundColor(
                    ContextCompat.getColor(requireActivity(), R.color.redGraph)
                )
                Log.i(TAG, "visibleInfoMeasuring: R.color.redGraph for Y2")
            }

        binding?.dateMeasuringCardView?.visibility = CardView.VISIBLE
        Log.i(TAG, "visibleInfoMeasuring: exit")
    }

    // обработчик EditTexts
    private fun editTextsHandler() {
        // changed textField DAD 1
        binding?.DADEnterET?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                var dad1s: Double? = null
                if (s.toString() == "") {
                    return
                }
                dad1s = try {
                    s.toString().toDouble()
                } catch (e: Exception) {
                    binding?.DADEnterET?.setText("")
                    null
                }
                if (dad1s != null && dad1s in 30.0..130.0) {
                    binding?.DADEnterET?.setBackgroundColor(
                        ContextCompat.getColor(requireActivity(), R.color.whiteBlack)
                    )
                    dad1flag = true
                    dad1 = dad1s
                    if (pulse1flag) {
                        calcKerdoIndexValue(
                            true,
                            dad1!!, pulse1!!
                        )
                        binding?.KerdoIndexTVvalue?.text = index1.toString()
                        binding?.KerdoIndexTVvalue?.setCompoundDrawablesWithIntrinsicBounds(
                            0, 0,
                            R.drawable.ic_baseline_keyboard_arrow_right_35, 0
                        )
                        binding?.setKerdoIndexButton?.visibility = Button.VISIBLE
                        installInfoAboutKerdo1()
                    }
                } else {
                    binding?.DADEnterET?.setBackgroundColor(
                        ContextCompat.getColor(requireActivity(), R.color.redGraph)
                    )
                    dad1flag = false
                    binding?.setKerdoIndexButton?.visibility = Button.INVISIBLE
                    binding?.KerdoIndexTVvalue?.text = ""
                    binding?.KerdoIndexTVvalue?.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, 0, 0
                    )
                }
            }
        })
        // changed textField pulse 1
        binding?.PulseEnterET?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                var pulse1s: Double? = null
                if (s.toString() == "") {
                    return
                }
                pulse1s = try {
                    s.toString().toDouble()
                } catch (e: Exception) {
                    binding?.PulseEnterET?.setText("")
                    null
                }
                if (pulse1s != null && pulse1s in 40.0..230.0) {
                    binding?.PulseEnterET?.setBackgroundColor(
                        ContextCompat.getColor(requireActivity(), R.color.whiteBlack)
                    )
                    pulse1flag = true
                    pulse1 = pulse1s
                    if (dad1flag) {
                        calcKerdoIndexValue(
                            true,
                            dad1!!, pulse1!!
                        )
                        binding?.KerdoIndexTVvalue?.text = index1.toString()
                        binding?.KerdoIndexTVvalue?.setCompoundDrawablesWithIntrinsicBounds(
                            0, 0,
                            R.drawable.ic_baseline_keyboard_arrow_right_35, 0
                        )
                        binding?.setKerdoIndexButton?.visibility = Button.VISIBLE
                        installInfoAboutKerdo1()
                    }
                } else {
                    binding?.PulseEnterET?.setBackgroundColor(
                        ContextCompat.getColor(requireActivity(), R.color.redGraph)
                    )
                    pulse1flag = false
                    binding?.setKerdoIndexButton?.visibility = Button.INVISIBLE
                    binding?.KerdoIndexTVvalue?.text = ""
                    binding?.KerdoIndexTVvalue?.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, 0, 0
                    )
                }
            }
        })
        // changed textField DAD 2
        binding?.DADEnterET2?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                var dad2s: Double? = null
                if (s.toString() == "") {
                    return
                }
                dad2s = try {
                    s.toString().toDouble()
                } catch (e: Exception) {
                    binding?.DADEnterET2?.setText("")
                    null
                }
                if (dad2s != null && dad2s in 30.0..130.0) {
                    binding?.DADEnterET2?.setBackgroundColor(
                        ContextCompat.getColor(requireActivity(), R.color.whiteBlack)
                    )
                    dad2flag = true
                    dad2 = dad2s
                    if (pulse2flag) {
                        calcKerdoIndexValue(
                            false,
                            dad2!!, pulse2!!
                        )
                        binding?.KerdoIndexTVvalue2?.text = index2.toString()
                        binding?.KerdoIndexTVvalue2?.setCompoundDrawablesWithIntrinsicBounds(
                            0, 0,
                            R.drawable.ic_baseline_keyboard_arrow_right_35, 0
                        )
                        binding?.setKerdoIndexButton?.visibility = Button.VISIBLE
                        installInfoAboutKerdo2()
                    }
                } else {
                    binding?.DADEnterET2?.setBackgroundColor(
                        ContextCompat.getColor(requireActivity(), R.color.redGraph)
                    )
                    dad2flag = false
                    binding?.setKerdoIndexButton?.visibility = Button.INVISIBLE
                    binding?.KerdoIndexTVvalue2?.text = ""
                    binding?.KerdoIndexTVvalue2?.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, 0, 0
                    )
                }
            }
        })
        // changed textField pulse 2
        binding?.PulseEnterET2?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                var pulse2s: Double? = null
                if (s.toString() == "") {
                    return
                }
                pulse2s = try {
                    s.toString().toDouble()
                } catch (e: Exception) {
                    binding?.PulseEnterET2?.setText("")
                    null
                }
                if (pulse2s != null && pulse2s in 40.0..230.0) {
                    binding?.PulseEnterET2?.setBackgroundColor(
                        ContextCompat.getColor(requireActivity(), R.color.whiteBlack)
                    )
                    pulse2flag = true
                    pulse2 = pulse2s
                    if (dad2flag) {
                        calcKerdoIndexValue(
                            false,
                            dad2!!, pulse2!!
                        )
                        binding?.KerdoIndexTVvalue2?.text = index2.toString()
                        binding?.KerdoIndexTVvalue2?.setCompoundDrawablesWithIntrinsicBounds(
                            0, 0,
                            R.drawable.ic_baseline_keyboard_arrow_right_35, 0
                        )
                        binding?.setKerdoIndexButton?.visibility = Button.VISIBLE
                        installInfoAboutKerdo2()
                    }
                } else {
                    binding?.PulseEnterET2?.setBackgroundColor(
                        ContextCompat.getColor(requireActivity(), R.color.redGraph)
                    )
                    pulse2flag = false
                    binding?.setKerdoIndexButton?.visibility = Button.INVISIBLE
                    binding?.KerdoIndexTVvalue2?.text = ""
                    binding?.KerdoIndexTVvalue2?.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, 0, 0
                    )
                }
            }
        })
    }

    // скролл
    private fun scroll(flag: Boolean) {
        Log.i(TAG, "scroll: entrance")
        if (flag) { // скролл на панель информации измерения
            Log.i(TAG, "scroll: flag is $flag")
            val objectAnimator = ObjectAnimator.ofInt(
                binding?.horizontalScrollView,
                "scrollX",
                binding?.horizontalScrollView?.right!!
            ).setDuration(550.toLong())
            objectAnimator.start()
            binding?.horizontalScrollView?.scrollBy(binding?.horizontalScrollView?.right!!, 0)
        } else {    // скролл на панель добавления измерения
            Log.i(TAG, "scroll: flag is $flag")
            val objectAnimator = ObjectAnimator.ofInt(
                binding?.horizontalScrollView,
                "scrollX",
                binding?.horizontalScrollView?.left!!
            ).setDuration(550.toLong())
            objectAnimator.start()
            binding?.horizontalScrollView?.scrollBy(binding?.horizontalScrollView?.left!!, 0)
        }
    }

    // считаем индекс
    // numIndex : true - 1ый, false - 2ый
    private fun calcKerdoIndexValue(numIndex: Boolean, DAD: Double, Pulse: Double) {
        Log.i(TAG, "calcKerdoIndexValue: DAD:$DAD Pulse:$Pulse")
        if (numIndex) {
            index1 = 100 * (1 - DAD / Pulse)
            index1 = (index1!! * 100.0).roundToInt() / 100.0
            Log.i(TAG, "calcKerdoIndexValue: index1:$index1")
        } else {
            index2 = 100 * (1 - DAD / Pulse)
            index2 = (index2!! * 100.0).roundToInt() / 100.0
            Log.i(TAG, "calcKerdoIndexValue: index2:$index2")
        }
    }

    // 1ое измерение: установка описания
    private fun installInfoAboutKerdo1() {
        Log.i(TAG, "installInfoAboutKerdo1: index1:$index1")
        if (-15.0 <= index1!! && index1!! <= 15.0)
            binding?.descriptionValueTV?.text =
                "Index 1: Complete vegetative equilibrium (value from -15 to 15) - eitonia - balance of sympathetic and parasympathetic influences"
        if (index1!! < -15.0)
            binding?.descriptionValueTV?.text =
                "Index 1: Predominance of parasympathetic influences (value less than -15) - moderate vagotonia"
        if (index1!! < -30.0)
            binding?.descriptionValueTV?.text =
                "Index 1: Predominance of parasympathetic influences (value less than -30) - pronounced vagotonia"
        if (15.0 < index1!!)
            binding?.descriptionValueTV?.text =
                "Index 1: Predominance of sympathetic influences (value above 15) - moderate sympathicotonia"
        if (30.0 < index1!!)
            binding?.descriptionValueTV?.text =
                "Index 1: Predominance of sympathetic influences (value above 30) - pronounced sympathicotonia"
    }

    // 2ое измерение: установка описания
    private fun installInfoAboutKerdo2() {
        Log.i(TAG, "calcKerdoIndexValue: index1:$index2")
        if (-15.0 <= index2!! && index2!! <= 15.0)
            binding?.descriptionValueTV2?.text =
                "Index 1: Complete vegetative equilibrium (value from -15 to 15) - eitonia - balance of sympathetic and parasympathetic influences"
        if (index2!! < -15.0)
            binding?.descriptionValueTV2?.text =
                "Index 2: Predominance of parasympathetic influences (value less than -15) - moderate vagotonia"
        if (index2!! < -30.0)
            binding?.descriptionValueTV2?.text =
                "Index 2: Predominance of parasympathetic influences (value less than -30) - pronounced vagotonia"
        if (15.0 < index2!!)
            binding?.descriptionValueTV2?.text =
                "Index 2: Predominance of sympathetic influences (value above 15) - moderate sympathicotonia"
        if (30.0 < index2!!)
            binding?.descriptionValueTV2?.text =
                "Index 2: Predominance of sympathetic influences (value above 30) - pronounced sympathicotonia"
    }

    // установка имени поль-ля
    private fun installNameUser() {
        binding?.appTitle?.text = "KerdoIndexSPORT"
        if (fireBaseAuthManager?.stateAuth() == true) {
            Log.i(TAG, "installNameUser: entrance")
            CoroutineScope(Dispatchers.IO).launch {
                delay(3000)
                launch(Dispatchers.Main){
                    binding?.appTitle?.text = sharedPreferencesManager?.getYourName()
                }
            }
        }
    }

    // скрытие клавиатуры
    private fun hideSoftKeyboard() {
        Log.i(TAG, "hideSoftKeyboard: entrance")
        val inputMethodManager: InputMethodManager = requireActivity().getSystemService(
            AppCompatActivity.INPUT_METHOD_SERVICE
        ) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(
            requireActivity().currentFocus?.windowToken, 0
        )
    }

    // работа с фрагментами
    private fun openFragment(fragment: Fragment) {
        Log.i(TAG, "openFragment: entrance: $fragment")
        val fragmentManager: FragmentManager? = activity?.supportFragmentManager
        val fragmentTransaction: FragmentTransaction = fragmentManager!!.beginTransaction()
        fragmentTransaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_in_right)
        fragmentTransaction.replace(R.id.container, fragment)
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
    }

    // после уничтожение экрана...
    override fun onDestroyView() {
        Log.i(TAG, "onDestroyView: entrance")
        binding = null  //  ...уничтожаем объектов view-элементов
        super.onDestroyView()
    }
}