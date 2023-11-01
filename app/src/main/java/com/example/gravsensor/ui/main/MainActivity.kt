package com.example.gravsensor.ui.main

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gravsensor.R
import com.example.gravsensor.databinding.ActivityMainBinding
import com.example.gravsensor.session.RecordSession
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.math.absoluteValue

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), SensorContract.View {
    var gravCount = 0
    var linCount = 0
    var gravX = 0F
    var gravY = 0F
    var gravZ = 0F
    var linX = 0F
    var linY = 0F
    var linZ = 0F
    var rotX = 0F
    var rotY = 0F
    var rotZ = 0F

    private val linSensorListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            updateLin(event?.values)
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            //TODO("Not yet implemented")
        }
    }
    private val sensorListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            update(event?.values)
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            //TODO("Not yet implemented")
        }
    }
    private val rotSensorListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            updateRot(event?.values)
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            //TODO("Not yet implemented")
        }
    }
    private val dummySensorListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {}
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    @Inject
    lateinit var presenter : SensorPresenter

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initData()
        initView()
    }

    private fun initData(){
        presenter.setView(this)
    }

    private fun initView(){
        binding.apply {
            txtStandSit.text = getString(R.string.btn_sit_label)
            btnStand.setOnClickListener {
                txtStandSit.text = getString(R.string.btn_stand_label)
                presenter.changeRecordConfig(standOrSit = RecordSession.Activity.STAND)
            }
            btnSit.setOnClickListener {
                txtStandSit.text = getString(R.string.btn_sit_label)
                presenter.changeRecordConfig(standOrSit = RecordSession.Activity.SIT)
            }
            btnStandUp.setOnClickListener {
                txtStandSit.text = getString(R.string.btn_move_label)
                presenter.changeRecordConfig(standOrSit = RecordSession.Activity.MOVE)
            }
//            btnSitDown.setOnClickListener {
//                txtStandSit.text = getString(R.string.btn_sit_down_label)
//                presenter.changeRecordConfig(standOrSit = RecordSession.Activity.SIT_DOWN)
//            }
            btnStartStop.apply {
                text = "Start"
                setOnClickListener {
                    if(presenter.isRecording()){
                        setElementsEnabled(true)
                        text = "Start"
                        presenter.stopRecording()
                    } else {
                        setElementsEnabled(false)
                        resetDataCount()
                        text = "Stop"
                        presenter.startRecording()
                    }
                }
            }
            btnSave.setOnClickListener {
                if(!presenter.isRecording()){
                    presenter.saveRecording()
                }
            }
            spnCollections.apply {
                ArrayAdapter.createFromResource(
                    this@MainActivity,
                    R.array.collections,
                    android.R.layout.simple_spinner_item
                ).also { adapter ->
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    this.adapter = adapter
                    this.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(
                            parent: AdapterView<*>?,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {
                            val selectedItem = adapter.getItem(position)
                            selectedItem?.let {
                                presenter.setTargetCollection(it.toString())
                            }
                        }

                        override fun onNothingSelected(parent: AdapterView<*>?) {
                            //TODO("Not yet implemented")
                        }
                    }
                    this.setSelection(adapter.getPosition("sit"))
                }
            }
            tgbAutoStop.apply {
                isChecked = false
                setOnCheckedChangeListener { _, isChecked ->
                    presenter.changeRecordConfig(isAutoStopEnabled = isChecked)
                }
            }
        }
        resetDataCount()
    }


    override fun onStart() {
        super.onStart()
        presenter.apply {
            createSession(this@MainActivity.applicationContext)
            registerSensor(Sensor.TYPE_ACCELEROMETER, dummySensorListener)
            registerSensor(Sensor.TYPE_MAGNETIC_FIELD, dummySensorListener)
            registerSensor(Sensor.TYPE_GRAVITY, sensorListener)
            registerSensor(Sensor.TYPE_LINEAR_ACCELERATION, linSensorListener)
//            registerSensor(Sensor.TYPE_ROTATION_VECTOR, rotSensorListener)
        }
    }

    override fun onStop() {
        super.onStop()
        presenter.clearSession()
    }

    override fun onSendDataSuccess(itemCount: Int) {
        Toast.makeText(this.applicationContext, "$itemCount items saved", Toast.LENGTH_SHORT).show()
    }

    override fun onFail(e: Exception) {
        Toast.makeText(this.applicationContext, e.message, Toast.LENGTH_LONG).show()
    }

    override fun onAutoStopReached() {
        runOnUiThread {
            setElementsEnabled(true)
            binding.btnStartStop.text = "Start"
        }
    }

    private fun update(values: FloatArray?) {
        gravCount += 1
        values?.let { gravValues ->
            gravX = gravValues[0]
            gravY = gravValues[1]
            gravZ = gravValues[2]
        }
        updateText()
    }

    private fun updateLin(values: FloatArray?) {
        linCount += 1
        values?.let { linValues ->
            linX = linValues[0]
            linY = linValues[1]
            linZ = linValues[2]
        }
        updateText()
    }

    private fun updateRot(values: FloatArray?) {
        values?.let { rotValues ->
            rotX = rotValues[0]
            rotY = rotValues[1]
            rotZ = rotValues[2]
        }
        updateText()
    }

    private fun updateText(){
        binding.counts.text = "grav=$gravCount\nlin=$linCount"
        binding.stats.text = "gravX=$gravX gravY=$gravY gravZ=$gravZ\n" +
                             "linX=$linX linY=$linY linz=$linZ\n" +
                             "rotX=$rotX rotY=$rotY rotZ=$rotZ"
    }

    private fun resetDataCount(){
        linCount = 0
        gravCount = 0
        updateText()
    }

    private fun setElementsEnabled(isEnabled : Boolean){
        binding.apply {
            spnCollections.isEnabled = isEnabled
            btnSave.isEnabled = isEnabled
            tgbAutoStop.isEnabled = isEnabled
        }
    }
}
