package com.example.progettolam.fragment

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Chronometer
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.progettolam.R
import com.example.progettolam.database.ActivityRecordDbHelper
import com.example.progettolam.struct.Record
import java.text.SimpleDateFormat
import java.util.Date

class homeFragment : Fragment(), SensorEventListener {
    private var btnStart: Button? = null
    private var btnStop: Button? = null
    private var chronometer: Chronometer? = null
    private var activityList: Spinner? = null
    private var tvActiviting: TextView? = null
    private var tvStep: TextView? = null
    private var selectedActivity: String? = null
    private var pauseOffset: Long = 0
    private var dbHelper: ActivityRecordDbHelper? = null

    private var sensorManager: SensorManager? = null
    private var stepSensor: Sensor? = null
    private var stepsAtStart: Long = 0
    private var numberSteps: Long = 0
    private var finalNumSteps: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            requireArguments().getString(ARG_PARAM1)
            requireArguments().getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_time, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        initClickListeners()
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACTIVITY_RECOGNITION
            )
            == PackageManager.PERMISSION_GRANTED
        ) {
            // Il permesso è stato concesso, procedi con il sensore
            initializeSensorManager()
        } else {
            // Richiedi il permesso
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
                PERMISSION_REQUEST_ACTIVITY_RECOGNITION
            )
        }
    }

    private fun initViews(view: View) {
        dbHelper = ActivityRecordDbHelper(view.context)
        btnStart = view.findViewById(R.id.startBtn)
        btnStop = view.findViewById(R.id.stopBtn)
        chronometer = view.findViewById(R.id.chrmter)
        activityList = view.findViewById(R.id.list_activity)
        tvActiviting = view.findViewById(R.id.tv_activiting)
        tvStep = view.findViewById(R.id.stepNum)
        pauseOffset = 0
    }

    private fun initClickListeners() {
        btnStart!!.setOnClickListener { view: View? -> handleStartButtonClick() }
        btnStop!!.setOnClickListener { view: View? -> handleStopButtonClick() }
    }

    private fun initializeSensorManager() {
        sensorManager = requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        if (sensorManager != null) {
            stepSensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
            if (stepSensor == null) {
                Toast.makeText(
                    requireContext(),
                    "Step counter sensor is not available on this device",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                Toast.makeText(requireContext(), "Avengeeeer", Toast.LENGTH_LONG).show()
            }
        } else {
            Log.e(TAG, "SensorManager initialization failed.")
        }
    }

    private fun handleStartButtonClick() {
        selectedActivity = activityList!!.selectedItem.toString()
        if (!chronometer!!.isActivated) {
            chronometer!!.base = SystemClock.elapsedRealtime() - pauseOffset
            chronometer!!.start()
            chronometer!!.isActivated = true
            btnStart!!.setText(R.string.cmeter_pause)
            tvActiviting!!.text = selectedActivity

            if (stepSensor != null && selectedActivity == "Walking") {
                sensorManager!!.registerListener(
                    this,
                    stepSensor,
                    SensorManager.SENSOR_DELAY_FASTEST
                )
            }
        } else {
            chronometer!!.stop()
            chronometer!!.isActivated = false
            pauseOffset = SystemClock.elapsedRealtime() - chronometer!!.base
            btnStart!!.setText(R.string.cmeter_continue)

            if (sensorManager != null) {
                sensorManager!!.unregisterListener(this)
            }
            // registra passo fatto
            finalNumSteps += numberSteps
            // inizia valori
            numberSteps = 0
            stepsAtStart = numberSteps
        }
    }

    private fun handleStopButtonClick() {
        if (chronometer!!.isActivated || pauseOffset != 0L) {
            selectedActivity = activityList!!.selectedItem.toString()
            chronometer!!.stop()
            val record = createRecord(System.currentTimeMillis(), SystemClock.elapsedRealtime())
            chronometer!!.base = SystemClock.elapsedRealtime()
            chronometer!!.isActivated = false
            pauseOffset = 0
            btnStart!!.setText(R.string.cmeter_start)
            tvActiviting!!.text = ""

            if (sensorManager != null) {
                sensorManager!!.unregisterListener(this)
            }
            // registra passo fatto
            finalNumSteps += numberSteps

            val newRowID = dbHelper!!.insertData(record)
            // inizializza tutti i valori dopo aver inserito i dati
            finalNumSteps = 0
            numberSteps = finalNumSteps
            stepsAtStart = numberSteps

            if (newRowID != -1L) {
                Toast.makeText(requireContext(), record.toString(), Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(requireContext(), "Insert error", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun createRecord(currentTime: Long, relativeTime: Long): Record {
        val duration = correctBias(relativeTime)
        val startTime = currentTime - duration

        val timeFormat = SimpleDateFormat("HH:mm:ss")
        val dayFormat = SimpleDateFormat("dd/MM/yyyy")

        val record = Record()
        record.nameActivity = selectedActivity
        record.duration = (duration / 1000).toInt()
        if (selectedActivity == "Walking") {
            record.step = finalNumSteps.toInt()
        } else {
            record.step = null
        }
        record.startTime = timeFormat.format(Date(startTime))
        record.endTime = timeFormat.format(Date(currentTime))
        record.startDay = dayFormat.format(Date(startTime))
        record.endDay = dayFormat.format(Date(currentTime))

        return record
    }

    private fun correctBias(relativeTime: Long): Long {
        val text = chronometer!!.text.toString()
        val bias = Character.getNumericValue(text[4])
        val noCorrectDuration = relativeTime - chronometer!!.base

        val lastDigit = (noCorrectDuration / 1000 % 60 % 10).toInt()
        if (lastDigit > bias || (bias == 9 && lastDigit == 0)) {
            return noCorrectDuration - 1000
        }
        return noCorrectDuration
    }

    override fun onResume() {
        super.onResume()
        //        if (stepSensor != null) {
//            sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI);
//        }
    }

    override fun onPause() {
        super.onPause()
        if (sensorManager != null) {
            sensorManager!!.unregisterListener(this)
        }
    }

    // come avere i passi registrati. quando ricevere passi registrati.
    override fun onSensorChanged(event: SensorEvent?) {
        Log.d("tag", "dentro on change di register qua clash")
        if (event != null && event.sensor.type == Sensor.TYPE_STEP_COUNTER) {
            if (chronometer!!.isActivated) {
                // stepsAtStart = 0 quindi assegno quello corrente, ovvero event.valus[0]
                if (stepsAtStart == 0L) {
                    stepsAtStart = event.values[0].toLong()
                }

                // ogni volta che cambia, registra quanti passi son stati fatti
                numberSteps = event.values[0].toLong() - stepsAtStart
                tvStep!!.text = numberSteps.toString()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        Log.d(TAG, "Sensor accuracy changed: $accuracy")
    }

    companion object {
        private const val TAG = "DynamicFragment"
        private const val ARG_PARAM1 = "param1"
        private const val ARG_PARAM2 = "param2"
        private const val PERMISSION_REQUEST_ACTIVITY_RECOGNITION = 1

        fun newInstance(param1: String?, param2: String?): homeFragment {
            val fragment = homeFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            fragment.arguments = args
            return fragment
        }
    }
}
