package com.example.gravsensor.session

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import com.example.gravsensor.repository.DataRepository
import com.example.gravsensor.repository.sources.DataSource

class RecordSession private constructor(private val sensorManager: SensorManager) {
    private val _sensors = mutableListOf<DataRecorder>()
    val sensors
        get() = _sensors.toList()
    var standOrSit = 0
    var recording = false
    var categoryData = mutableListOf<Int>()

    fun registerSensor(sensorType : Int, listener: SensorEventListener){
        if(_sensors.isEmpty()){
            _sensors.add(DataRecorder(sensorType, listener, recordCategory = true))
        } else {
            _sensors.add(DataRecorder(sensorType, listener, ))
        }
    }

    fun reset(){
        for (sensor in _sensors) {
            sensor.destroy()
        }
        _sensors.clear()
        recording = false
    }

    fun startRecord(){
        if(_sensors.isEmpty()) return
        clear()
        recording = true
    }

    private fun clear() {
        for(sensor in _sensors){
            sensor.clearData()
        }
    }

    fun stopRecord(source : DataSource.Remote, listener : DataRepository.OnDataResultListener){
        if(!recording) return
        recording = false
        //saveRecord(source, listener)
    }

    fun saveRecord(source : DataSource.Remote, listener : DataRepository.OnDataResultListener){
        try {
            val mergeData = mutableListOf<FloatArray>().apply {
                for (i in 0 until _sensors[0].data.size) {
                    add(
                        floatArrayOf(
                            _sensors[0].data[i].x,
                            _sensors[0].data[i].y,
                            _sensors[0].data[i].z,
                            _sensors[1].data[i].x,
                            _sensors[1].data[i].y,
                            _sensors[1].data[i].z,
                            categoryData[i].toFloat(),
                            _sensors[0].data[i].timestamp.toFloat()
                        )
                    )
                }
            }
            source.saveData(mergeData, listener)
        } catch(e : Exception){
            e.printStackTrace()
        }
    }

    inner class DataRecorder(
        sensorType : Int,
        listener: SensorEventListener,
        recordCategory: Boolean = false,
    ) {
        private val sensor by lazy { sensorManager.getDefaultSensor(sensorType) }
        val data by lazy { mutableListOf<DataEntry>() }
        private val sensorListener by lazy {
            object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent?) {
                    if(!recording)
                        return
                    event?.values?.let {
                        data.add(DataEntry(it[0], it[1], it[2], System.currentTimeMillis()))
                    }
                    if(recordCategory){
                        categoryData.add(standOrSit)
                    }
                    listener.onSensorChanged(event)
                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                    listener.onAccuracyChanged(sensor, accuracy)
                }
            }
        }

        init {
            sensorManager.registerListener(sensorListener, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        }

        fun clearData(){
            data.clear()
        }

        fun destroy(){
            sensorManager.unregisterListener(sensorListener)
        }
    }

    companion object {
        private var instance : RecordSession? = null
        fun getInstance(sensorManager: SensorManager) =
            instance ?: RecordSession(sensorManager).also { instance = it }
    }
}

data class DataEntry(
    val x : Float,
    val y : Float,
    val z : Float,
    val timestamp: Long
)