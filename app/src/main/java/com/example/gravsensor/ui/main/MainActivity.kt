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
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

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

    @Inject
    lateinit var presenter : SensorPresenter

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        presenter.setView(this)
        binding.apply {
            txtStandSit.text = "Sit"
            btnStand.setOnClickListener {
                txtStandSit.text = "Stand"
                presenter.changeRecordConfig(standOrSit = 1)
            }
            btnSit.setOnClickListener {
                txtStandSit.text = "Sit"
                presenter.changeRecordConfig(standOrSit = 0)
            }
            btnStartStop.text = "Start"
            btnStartStop.setOnClickListener {
                if(presenter.isRecording()){
                    spnCollections.isEnabled = true
                    btnStartStop.text = "Start"
                    presenter.stopRecording()
                } else {
                    spnCollections.isEnabled = false
                    btnStartStop.text = "Stop"
                    resetDataCount()
                    presenter.startRecording()
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
                    this.setSelection(adapter.getPosition("stand"))
                }
            }
        }
        resetDataCount()
    }

    override fun onStart() {
        super.onStart()
        presenter.apply {
            createSession(this@MainActivity.applicationContext)
            registerSensor(Sensor.TYPE_GYROSCOPE, sensorListener)
            registerSensor(Sensor.TYPE_LINEAR_ACCELERATION, linSensorListener)
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

    override fun onBatchLimitReached() {
        binding.btnStartStop.text = "Start"
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

    private fun updateText(){
        binding.counts.text = "grav=$gravCount\nlin=$linCount"
        binding.stats.text = "gravx=$gravX\ngravy=$gravY\ngravz=$gravZ\nlinx=$linX\nliny=$linY\nlinz=$linZ"
    }

    private fun resetDataCount(){
        linCount = 0
        gravCount = 0
        updateText()
    }
}
