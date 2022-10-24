package com.AMED.kerdoindex

import android.animation.ObjectAnimator
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.AMED.kerdoindex.databinding.FragmentMainBinding
import com.AMED.kerdoindex.model.Measuring.Measuring
import com.AMED.kerdoindex.model.Measuring.MeasuringViewModel
import com.AMED.kerdoindex.model.Measuring2.Measuring2
import com.AMED.kerdoindex.model.Measuring2.Measuring2ViewModel
import com.jjoe64.graphview.ValueDependentColor
import com.jjoe64.graphview.series.BarGraphSeries
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import com.jjoe64.graphview.series.PointsGraphSeries
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class MainFragment : Fragment() {
    private var binding: FragmentMainBinding? = null
    private val profileSportsmanFragment = ProfileSportsmanFragment()
    private var index: Double? = null
    private var index2: Double? = null
    private var measurings: LiveData<List<Measuring>>? = null
    private var measurings2: LiveData<List<Measuring2>>? = null
    private var mMeasuringViewModel: MeasuringViewModel? = null
    private var mMeasuring2ViewModel: Measuring2ViewModel? = null
    private var timer: CountDownTimer? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMainBinding.inflate(inflater, container, false)

        editTextsHandler()
        clickListeners()

        mMeasuringViewModel = ViewModelProvider(this)[MeasuringViewModel::class.java]
        mMeasuringViewModel?.readAllMeasuring?.observe(viewLifecycleOwner, Observer {
            measurings = mMeasuringViewModel?.readAllMeasuring
        })
        mMeasuring2ViewModel = ViewModelProvider(this)[Measuring2ViewModel::class.java]
        mMeasuring2ViewModel?.readAllMeasuring2?.observe(viewLifecycleOwner, Observer {
            measurings2 = mMeasuring2ViewModel?.readAllMeasuring2
        })

        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        CoroutineScope(Dispatchers.IO).launch {
            delay(800)
            launch { createGraths() }
        }
    }

    // слушатели нажатий
    private fun clickListeners(){
        binding?.setKerdoIndexButton?.setOnClickListener { saveMeasuring() }
        binding?.clearAllIndexButton?.setOnClickListener { clearAll() }
        binding?.KerdoIndexTVvalue?.setOnClickListener {
            hideSoftKeyboard()
            scroll(true)
        }
        binding?.KerdoIndexTVvalue2?.setOnClickListener {
            hideSoftKeyboard()
            scroll(true)
        }
        binding?.backToEnter?.setOnClickListener {
            hideSoftKeyboard()
            scroll(false)
        }
        binding?.switchViewMode?.setOnCheckedChangeListener { buttonView, isChecked ->
            clearGraths()
            createGraths()
        }
        binding?.GoToProfileButton?.setOnClickListener {
            openFragment(profileSportsmanFragment)
        }
    }

    // сохранить измерения
    private fun saveMeasuring() {
        // если все 4 поля не пустые
        if (!(binding?.DADEnterET?.text.toString() == "" && binding?.PulseEnterET?.text.toString() == ""
                    && binding?.DADEnterET2?.text.toString() == "" && binding?.PulseEnterET2?.text.toString() == ""
                    )) {
            var flagEmpty1: Boolean = true
            var flagEmpty2: Boolean = true

            // если оба из полей 1го индекса не пустые или пустые
            if (binding?.DADEnterET?.text.toString() == "" && binding?.PulseEnterET?.text.toString() == ""
                || binding?.DADEnterET?.text.toString() != "" && binding?.PulseEnterET?.text.toString() != ""
            ) {     //////////////////
                flagEmpty1 = true
            } else {
                Toast.makeText(activity, "Введите все данные 1го индекса!", Toast.LENGTH_SHORT).show()
                flagEmpty1 = false
            }
            // если оба из полей 2го индекса не пустые или пустые
            if (binding?.DADEnterET2?.text.toString() == "" && binding?.PulseEnterET2?.text.toString() == ""
                || binding?.DADEnterET2?.text.toString() != "" && binding?.PulseEnterET2?.text.toString() != ""
            ) {
                flagEmpty2 = true
            } else {
                Toast.makeText(activity, "Введите все данные 2го индекса!", Toast.LENGTH_SHORT).show()
                flagEmpty2 = false
            }

            if (flagEmpty1 && flagEmpty2) {
                var DAD: Double
                var Pulse: Double
                var DAD2: Double
                var Pulse2: Double
                var flagRange1: Boolean = true
                var flagRange2: Boolean = true

                // если оба поля 1го индекса пустые
                if (binding?.DADEnterET?.text.toString() == "" && binding?.PulseEnterET?.text.toString() == "") {
                    DAD = 0.0
                    Pulse = 0.0
                    index = 0.0
                } else {
                    DAD = binding?.DADEnterET?.text.toString().toDouble()
                    Pulse = binding?.PulseEnterET?.text.toString().toDouble()
                    if (DAD >= 40 && DAD <= 120 && Pulse >= 40 && Pulse <= 230) flagRange1 = true
                    else {
                        Toast.makeText(
                            activity,
                            "Введите корректные данные 1го индекса!",
                            Toast.LENGTH_SHORT
                        ).show()
                        flagRange1 = false
                    }
                }
                // если оба поля 2го индекса пустые
                if (binding?.DADEnterET2?.text.toString() == "" && binding?.PulseEnterET2?.text.toString() == "") {
                    DAD2 = 0.0
                    Pulse2 = 0.0
                    index2 = 0.0
                } else {
                    DAD2 = binding?.DADEnterET2?.text.toString().toDouble()
                    Pulse2 = binding?.PulseEnterET2?.text.toString().toDouble()
                    if (DAD2 >= 40 && DAD2 <= 120 && Pulse2 >= 40 && Pulse2 <= 230)
                        flagRange2 = true
                    else {
                        Toast.makeText(
                            activity,
                            "Введите корректные данные 2го индекса!",
                            Toast.LENGTH_SHORT
                        ).show()
                        flagRange2 = false
                    }
                }

                if (flagRange1 && flagRange2) {
                    AlertDialog.Builder(requireActivity())
                        .setTitle("Сохранение измерения")
                        .setMessage("Вы уверены, что хотите сохранить измерение?")
                        .setPositiveButton("Сохранить") { dialog, which ->
                            val increment: Int = if (measurings?.value?.isNotEmpty()!!)
                                measurings?.value?.size!! + 1
                            else 1
                            val increment2: Int = if (measurings2?.value?.isNotEmpty()!!)
                                measurings2?.value?.size!! + 1
                            else 1

                            val currentDate = Date()
                            val dateFormat: DateFormat =
                                SimpleDateFormat("dd.MM / HH:mm", Locale.getDefault())
                            val dateText = dateFormat.format(currentDate)

                            val newMeasuring = Measuring(
                                0,
                                DAD,
                                Pulse,
                                index!!,
                                increment,
                                dateText
                            )
                            val newMeasuring2 = Measuring2(
                                0,
                                DAD2,
                                Pulse2,
                                index2!!,
                                increment2,
                                dateText
                            )
                            mMeasuringViewModel?.insertMeasuring(newMeasuring)
                            mMeasuring2ViewModel?.insertMeasuring2(newMeasuring2)
                            CoroutineScope(Dispatchers.IO).launch {
                                delay(400)
                                launch {
                                    clearGraths()
                                    createGraths()
                                }
                            }
                            hideSoftKeyboard()
                            clearEditTexts()
                            clearKerdoIndexValue()
                            clearKerdoIndexValue2()
                        }.setNegativeButton("Отмена", null).show()
                }
            } else Toast.makeText(activity, "Введите данные!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(activity, "Введите данные!", Toast.LENGTH_SHORT).show()
        }
    }

    // очистить историю
    private fun clearAll() {
        AlertDialog.Builder(requireActivity())
            .setTitle("Удалить историю")
            .setMessage("Вы уверены, что хотите удалить историю?")
            .setPositiveButton("Удалить") { dialog, which ->
                mMeasuringViewModel?.deleteAllMeasuring()
                mMeasuring2ViewModel?.deleteAllMeasuring2()
                clearGraths()
                clearEditTexts()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    // очистить EditTexts
    private fun clearEditTexts() {
        binding?.DADEnterET?.setText("")
        binding?.PulseEnterET?.setText("")
        binding?.DADEnterET2?.setText("")
        binding?.PulseEnterET2?.setText("")
    }

    // очистить графы
    private fun clearGraths() {
        binding?.graphKedro?.removeAllSeries()
        binding?.graphKedro2?.removeAllSeries()
        binding?.graphDAD?.removeAllSeries()
        binding?.graphPulse?.removeAllSeries()
    }

    // создание и настройка графов
    private fun createGraths() {
        val valuesKedro = measurings?.value?.size?.let { arrayOfNulls<DataPoint>(it) }
        val valuesKedro2 = measurings2?.value?.size?.let { arrayOfNulls<DataPoint>(it) }
        val valuesDAD = measurings?.value?.size?.let { arrayOfNulls<DataPoint>(it) }
        val valuesPulse = measurings?.value?.size?.let { arrayOfNulls<DataPoint>(it) }

        if (measurings?.value?.isNotEmpty() == true && measurings2?.value?.isNotEmpty() == true) {
            var i = 0
            while (i < measurings?.value?.size!!) {
                val vKedro: DataPoint = DataPoint(
                    measurings?.value?.get(i)?.number!!.toDouble(),
                    measurings?.value?.get(i)?.KerdoIndex!!.toDouble()
                )
                valuesKedro?.set(i, vKedro)
                val vDAD: DataPoint =
                    DataPoint(measurings?.value?.get(i)!!.number.toDouble(), measurings?.value?.get(i)!!.DAD.toDouble())
                valuesDAD?.set(i, vDAD)
                val vPulse: DataPoint =
                    DataPoint(measurings?.value?.get(i)!!.number.toDouble(), measurings?.value?.get(i)!!.Pulse.toDouble())
                valuesPulse?.set(i, vPulse)
                ++i
            }
            i = 0
            while (i < measurings2?.value?.size!!) {
                val vKedro: DataPoint = DataPoint(
                    measurings2?.value?.get(i)?.number!!.toDouble(),
                    measurings2?.value?.get(i)?.KerdoIndex!!.toDouble()
                )
                valuesKedro2?.set(i, vKedro)
                ++i
            }

            binding?.graphKedro?.viewport?.isYAxisBoundsManual = true
            binding?.graphKedro2?.viewport?.isYAxisBoundsManual = true
            binding?.graphDAD?.viewport?.isYAxisBoundsManual = true
            binding?.graphPulse?.viewport?.isYAxisBoundsManual = true
            binding?.graphKedro?.viewport?.isXAxisBoundsManual = true
            binding?.graphKedro2?.viewport?.isXAxisBoundsManual = true
            binding?.graphDAD?.viewport?.isXAxisBoundsManual = true
            binding?.graphPulse?.viewport?.isXAxisBoundsManual = true

            if (binding?.switchViewMode?.isChecked != true) {
                val kedroSeries = BarGraphSeries(valuesKedro)
                val kedroSeries2 = BarGraphSeries(valuesKedro2)
                val kedroSeries2Point = PointsGraphSeries(valuesKedro2)

                kedroSeries.valueDependentColor = ValueDependentColor { data ->
                    if (-15.0 <= data?.y!! && data.y <= 15.0)
                        ContextCompat.getColor(requireActivity(), R.color.yellowGraph)
                    else
                        if (data.y < -15.0)
                            ContextCompat.getColor(requireActivity(), R.color.greenGraph)
                        else
                            ContextCompat.getColor(requireActivity(), R.color.redGraph)
                }
                kedroSeries2.valueDependentColor = ValueDependentColor { data ->
                    if (-15.0 <= data?.y!! && data.y <= 15.0)
                        ContextCompat.getColor(requireActivity(), R.color.yellowGraph)
                    else
                        if (data.y < -15.0)
                            ContextCompat.getColor(requireActivity(), R.color.greenGraph)
                        else
                            ContextCompat.getColor(requireActivity(), R.color.redGraph)
                }

                binding?.graphKedro?.addSeries(kedroSeries)
                binding?.graphKedro2?.addSeries(kedroSeries2)
                binding?.graphKedro?.addSeries(kedroSeries2Point)
                kedroSeries2Point.shape = PointsGraphSeries.Shape.POINT
                kedroSeries2Point.color = Color.CYAN

                binding?.graphDAD?.addSeries(BarGraphSeries(valuesDAD))
                binding?.graphPulse?.addSeries(BarGraphSeries(valuesPulse))

                // максимальный и минимальный X
                binding?.graphKedro?.viewport?.setMinX(kedroSeries.lowestValueX - 1)
                binding?.graphKedro?.viewport?.setMaxX(kedroSeries.highestValueX + 1)
                binding?.graphKedro2?.viewport?.setMinX(kedroSeries2.lowestValueX - 1)
                binding?.graphKedro2?.viewport?.setMaxX(kedroSeries2.highestValueX + 1)
                binding?.graphDAD?.viewport?.setMinX(BarGraphSeries(valuesDAD).lowestValueX - 1)
                binding?.graphDAD?.viewport?.setMaxX(BarGraphSeries(valuesDAD).highestValueX + 1)
                binding?.graphPulse?.viewport?.setMinX(BarGraphSeries(valuesPulse).lowestValueX - 1)
                binding?.graphPulse?.viewport?.setMaxX(BarGraphSeries(valuesPulse).highestValueX + 1)

                kedroSeries.setOnDataPointTapListener { _, dataPoint ->         // нажатие на столбец
                    visibleInfoMeasuring(dataPoint.x.toInt())
                }
                kedroSeries2Point.setOnDataPointTapListener { _, dataPoint ->    // нажатие на точку
                    visibleInfoMeasuring(dataPoint.x.toInt())
                }
            } else {
                binding?.graphKedro2?.addSeries(LineGraphSeries(valuesKedro2))
                binding?.graphKedro?.addSeries(LineGraphSeries(valuesKedro))
                binding?.graphDAD?.addSeries(LineGraphSeries(valuesDAD))
                binding?.graphPulse?.addSeries(LineGraphSeries(valuesPulse))
                // максимальный и минимальный X
                binding?.graphKedro?.viewport?.setMinX(LineGraphSeries(valuesKedro).lowestValueX - 1)
                binding?.graphKedro?.viewport?.setMaxX(LineGraphSeries(valuesKedro).highestValueX + 1)
                binding?.graphDAD?.viewport?.setMinX(LineGraphSeries(valuesDAD).lowestValueX - 1)
                binding?.graphDAD?.viewport?.setMaxX(LineGraphSeries(valuesDAD).highestValueX + 1)
                binding?.graphPulse?.viewport?.setMinX(LineGraphSeries(valuesPulse).lowestValueX - 1)
                binding?.graphPulse?.viewport?.setMaxX(LineGraphSeries(valuesPulse).highestValueX + 1)
                binding?.graphKedro?.gridLabelRenderer
            }

            // выставление заголовков
            binding?.graphKedro?.gridLabelRenderer?.horizontalAxisTitle = "Номер измерения"
            binding?.graphKedro?.gridLabelRenderer?.verticalAxisTitle = "Индекс Кедро"
            binding?.graphKedro2?.gridLabelRenderer?.horizontalAxisTitle = "Номер измерения"
            binding?.graphKedro2?.gridLabelRenderer?.verticalAxisTitle = "Индекс Кедро"
            binding?.graphDAD?.gridLabelRenderer?.horizontalAxisTitle = "Номер измерения"
            binding?.graphDAD?.gridLabelRenderer?.verticalAxisTitle = "ДАД"
            binding?.graphPulse?.gridLabelRenderer?.horizontalAxisTitle = "Номер измерения"
            binding?.graphPulse?.gridLabelRenderer?.verticalAxisTitle = "Пульс"

            // разрешение прокрутки и зуминга
            //binding?.graphKedro?.viewport?.isScalable = true
            //binding?.graphKedro?.viewport?.isScrollable = true
            binding?.graphDAD?.viewport?.isScalable = true
            binding?.graphDAD?.viewport?.isScrollable = true
            binding?.graphPulse?.viewport?.isScalable = true
            binding?.graphPulse?.viewport?.isScrollable = true

            // максимальный и минимальный Y
            binding?.graphKedro?.viewport?.setMinY((-60).toDouble())
            binding?.graphKedro?.viewport?.setMaxY((90).toDouble())
            binding?.graphKedro2?.viewport?.setMinY((-60).toDouble())
            binding?.graphKedro2?.viewport?.setMaxY((90).toDouble())
            binding?.graphDAD?.viewport?.setMinY(measurings?.value?.minByOrNull { it.DAD }!!.DAD.toDouble() - 2)
            binding?.graphDAD?.viewport?.setMaxY(measurings?.value?.maxByOrNull { it.DAD }!!.DAD.toDouble() + 2)
            binding?.graphPulse?.viewport?.setMinY(measurings?.value?.minByOrNull { it.Pulse }!!.Pulse.toDouble() - 2)
            binding?.graphPulse?.viewport?.setMaxY(measurings?.value?.maxByOrNull { it.Pulse }!!.Pulse.toDouble() + 2)
        } else {
            //Toast.makeText(this, "ERROR", Toast.LENGTH_SHORT).show()
        }
    }

    // нажатие на столбец - отображение окна с инфо столбца
    private fun visibleInfoMeasuring(X: Int) {
        // measurings!![dataPoint.x.toInt() - 1].KerdoIndex
        val Y = measurings?.value?.get(X - 1)?.KerdoIndex
        val Y2 = measurings2?.value?.get(X - 1)?.KerdoIndex
        binding?.dateMeasuringCardView?.visibility = CardView.VISIBLE
        binding?.dateMeasuringTV?.text = measurings?.value?.get(X - 1)?.date

        binding?.indexMeasuringTV?.text = String.format("%.2f", Y)
        binding?.indexMeasuringTV2?.text = String.format("%.2f", Y2)

        if (-15.0 <= Y!! && Y <= 15.0)
            binding?.cardView?.setCardBackgroundColor(ContextCompat.getColor(requireActivity(), R.color.yellowGraph))
        else
            if (Y < -15.0)
                binding?.cardView?.setCardBackgroundColor(ContextCompat.getColor(requireActivity(), R.color.greenGraph))
            else
                binding?.cardView?.setCardBackgroundColor(ContextCompat.getColor(requireActivity(), R.color.redGraph))

        if (-15.0 <= Y2!! && Y2 <= 15.0)
            binding?.cardView2?.setCardBackgroundColor(ContextCompat.getColor(requireActivity(), R.color.yellowGraph))
        else
            if (Y2 < -15.0)
                binding?.cardView2?.setCardBackgroundColor(ContextCompat.getColor(requireActivity(), R.color.greenGraph))
            else
                binding?.cardView2?.setCardBackgroundColor(ContextCompat.getColor(requireActivity(), R.color.redGraph))

        if (timer != null) (timer as CountDownTimer).cancel()
        timer = object : CountDownTimer(3500, 1000) {
            override fun onTick(millisUntilFinished: Long) {}
            override fun onFinish() {
                binding?.dateMeasuringCardView?.visibility = CardView.INVISIBLE
            }
        }.start()
    }

    // обработчик EditTexts
    private fun editTextsHandler() {
        binding?.DADEnterET?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                checkKerdoValue()
            }
        })

        binding?.PulseEnterET?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                checkKerdoValue()
            }
        })

        binding?.DADEnterET2?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                checkKerdoValue2()
            }
        })

        binding?.PulseEnterET2?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                checkKerdoValue2()
            }
        })

//    binding?.nameUserEditText?.addTextChangedListener(object : TextWatcher{
//        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
//        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
//        override fun afterTextChanged(s: Editable) {
//            nameUser = binding?.nameUserEditText?.text.toString()
//        }
//    })
    }

    // проверка значений в EditTexts
    private fun checkKerdoValue() {
        try {
            if (binding?.DADEnterET?.text.toString() != "" && binding?.PulseEnterET?.text.toString() != "") {
                if (binding?.DADEnterET?.text.toString().toDouble() in 30.0..130.0
                    && binding?.PulseEnterET?.text.toString().toDouble() in 40.0..230.0
                ) {
                    calcKerdoIndexValue(
                        true,
                        binding?.DADEnterET?.text.toString().toDouble(),
                        binding?.PulseEnterET?.text.toString().toDouble()
                    )
                    installKerdoIndexValue()
                } else
                    clearKerdoIndexValue()
            } else
                clearKerdoIndexValue()
        } catch (e: Exception) {
        }
    }

    // проверка значений в EditTexts2
    private fun checkKerdoValue2() {
        try {
            if (binding?.DADEnterET2?.text.toString() != "" && binding?.PulseEnterET2?.text.toString() != "") {
                if (binding?.DADEnterET?.text.toString().toDouble() in 30.0..130.0
                    && binding?.PulseEnterET?.text.toString().toDouble() in 40.0..230.0
                ) {
                    calcKerdoIndexValue(
                        false,
                        binding?.DADEnterET2?.text.toString().toDouble(),
                        binding?.PulseEnterET2?.text.toString().toDouble()
                    )
                    installKerdoIndexValue2()
                } else
                    clearKerdoIndexValue2()
            } else
                clearKerdoIndexValue2()
        } catch (e: Exception) {
        }
    }

    // очистить данные 1го индекса
    private fun clearKerdoIndexValue() {
        binding?.KerdoIndexTVvalue?.text = ""
        binding?.KerdoIndexTVvalue?.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
        binding?.descriptionValueTV?.text = "Индекс 1: Введите правильные данные в поля ДАД и Пульс"
        //binding?.rightCV?.visibility = CardView.INVISIBLE
    }

    // очистить данные 2го индекса
    private fun clearKerdoIndexValue2() {
        binding?.KerdoIndexTVvalue2?.text = ""
        binding?.KerdoIndexTVvalue2?.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
        binding?.descriptionValueTV2?.text =
            "Индекс 2: Введите правильные данные в поля ДАД и Пульс"
        //binding?.rightCV?.visibility = CardView.INVISIBLE
    }

    // скролл
    private fun scroll(flag: Boolean) {
        if (flag) {
            val objectAnimator = ObjectAnimator.ofInt(
                binding?.horizontalScrollView,
                "scrollX",
                binding?.horizontalScrollView?.right!!
            ).setDuration(550.toLong())
            objectAnimator.start()
            binding?.horizontalScrollView?.scrollBy(binding?.horizontalScrollView?.right!!, 0)
        } else {
            val objectAnimator = ObjectAnimator.ofInt(
                binding?.horizontalScrollView,
                "scrollX",
                binding?.horizontalScrollView?.left!!
            ).setDuration(550.toLong())
            objectAnimator.start()
            binding?.horizontalScrollView?.scrollBy(binding?.horizontalScrollView?.left!!, 0)
        }
    }

    // считаем индекс / numIndex : true - 1ый, false - 2ый
    private fun calcKerdoIndexValue(numIndex: Boolean, DAD: Double, Pulse: Double) {
        if (numIndex) index = 100 * (1 - DAD / Pulse)
        else index2 = 100 * (1 - DAD / Pulse)
    }

    // установка значений 1го индекса
    private fun installKerdoIndexValue() {
        if (binding?.DADEnterET?.text.toString() != "" && binding?.PulseEnterET?.text.toString() != "") {
            binding?.KerdoIndexTVvalue?.text = String.format("%.2f", index)
            binding?.KerdoIndexTVvalue?.setCompoundDrawablesWithIntrinsicBounds(
                0, 0,
                R.drawable.ic_baseline_keyboard_arrow_right_35, 0
            )
        }

        if (-15.0 <= index!! && index!! <= 15.0)
            binding?.descriptionValueTV?.text =
                "Индекс 1: Полное вегетативное равновесие(значение от -15 до 15) - эйтония - уравновешенность симпатических и парасимпатических влияний"
        if (index!! < -15.0)
            binding?.descriptionValueTV?.text =
                "Индекс 1: Преобладание парасимпатических влияний(значение меньше -15) - умеренная ваготония"
        if (index!! < -30.0)
            binding?.descriptionValueTV?.text =
                "Индекс 1: Преобладание парасимпатических влияний(значение меньше -30) - выраженная ваготония"
        if (15.0 < index!!)
            binding?.descriptionValueTV?.text =
                "Индекс 1: Преобладание симпатических влияний(значение выше 15) - умеренная симпатикотония"
        if (30.0 < index!!)
            binding?.descriptionValueTV?.text =
                "Индекс 1: Преобладание симпатических влияний(значение выше 30) - выраженная симпатикотония"
    }

    // установка значений 2го индекса
    private fun installKerdoIndexValue2() {
        if (binding?.DADEnterET2?.text.toString() != "" && binding?.PulseEnterET2?.text.toString() != "") {
            binding?.KerdoIndexTVvalue2?.text = String.format("%.2f", index2)
            binding?.KerdoIndexTVvalue2?.setCompoundDrawablesWithIntrinsicBounds(
                0, 0,
                R.drawable.ic_baseline_keyboard_arrow_right_35, 0
            )
        }

        if (-15.0 <= index2!! && index2!! <= 15.0)
            binding?.descriptionValueTV2?.text =
                "Индекс 2: Полное вегетативное равновесие(значение от -15 до 15) - эйтония - уравновешенность симпатических и парасимпатических влияний"
        if (index2!! < -15.0)
            binding?.descriptionValueTV2?.text =
                "Индекс 2: Преобладание парасимпатических влияний(значение меньше -15) - умеренная ваготония"
        if (index2!! < -30.0)
            binding?.descriptionValueTV2?.text =
                "Индекс 2: Преобладание парасимпатических влияний(значение меньше -30) - выраженная ваготония"
        if (15.0 < index2!!)
            binding?.descriptionValueTV2?.text =
                "Индекс 2: Преобладание симпатических влияний(значение выше 15) - умеренная симпатикотония"
        if (30.0 < index2!!)
            binding?.descriptionValueTV2?.text =
                "Индекс 2: Преобладание симпатических влияний(значение выше 30) - выраженная симпатикотония"
    }

    // скрытие клавиатуры
    private fun hideSoftKeyboard() {
        val inputMethodManager: InputMethodManager = requireActivity().getSystemService(
            AppCompatActivity.INPUT_METHOD_SERVICE
        ) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(
            requireActivity().currentFocus?.windowToken, 0
        )
    }

    private fun openFragment(twoFrag: Fragment) {
        val fragmentManager: FragmentManager? = activity?.supportFragmentManager
        val fragmentTransaction: FragmentTransaction = fragmentManager!!.beginTransaction()
        fragmentTransaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_in_right)
        fragmentTransaction.replace(R.id.container, twoFrag)
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }
}