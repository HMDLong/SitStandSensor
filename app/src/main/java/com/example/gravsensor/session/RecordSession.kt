package com.example.gravsensor.session

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.example.gravsensor.repository.DataRepository
import com.example.gravsensor.repository.sources.DataSource
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class RecordSession private constructor(private val sensorManager: SensorManager) {
    private val _sensors = mutableListOf<DataRecorder>()
    val sensors
        get() = _sensors.toList()
    private val unixTimer = Calendar.getInstance()
    var standOrSit = Activity.SIT
    var recording = false
    var autoStopEnabled = false
    var autoStopListener : AutoStopListener? = null
    var categoryData = mutableListOf<Int>()

    fun registerSensor(sensorType : Int, listener: SensorEventListener){
        if(_sensors.isEmpty()){
            _sensors.add(DataRecorder(sensorType, listener, recordCategory = true))
        } else {
            _sensors.add(DataRecorder(sensorType, listener))
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
        clearRecords()
        recording = true
        if(autoStopEnabled) {
            Executors.newScheduledThreadPool(1).schedule(
                {
                    stopRecord()
                    autoStopListener?.onAutoStop()
                },
                20000L,
                TimeUnit.MILLISECONDS
            )
        }
    }

    private fun clearRecords() {
        categoryData.clear()
        for(sensor in _sensors){
            sensor.clearData()
        }
    }

    fun stopRecord() {
        if(!recording) return
        recording = false
    }

    fun saveRecord(source : DataSource.Remote, listener : DataRepository.OnDataResultListener){
        try {
            val mergeData = mutableListOf<FloatArray>().apply {
                for (i in 0 until _sensors[0].data.size) {
                    val oriMat = getOrientation(_sensors[2].data[i].toFloatArray(), _sensors[1].data[i].toFloatArray())
                    add(
                        floatArrayOf(
                            // accelerometer
                            _sensors[0].data[i].x,
                            _sensors[0].data[i].y,
                            _sensors[0].data[i].z,
                            // orientation
                            oriMat[0], //azimuth
                            oriMat[1], //pitch
                            oriMat[2], //roll
                            // gravity
                            _sensors[2].data[i].x,
                            _sensors[2].data[i].y,
                            _sensors[2].data[i].z,
                            // linear accelerometer
                            _sensors[3].data[i].x,
                            _sensors[3].data[i].y,
                            _sensors[3].data[i].z,
                            // label
                            categoryData[i].toFloat(),
                            // timestamp for syncing
                            _sensors[0].data[i].timestamp.toFloat()
                        )
                    )
                }
            }
//            Log.d("orientation", "${mergeData[10][3]} ${mergeData[10][4]} ${mergeData[10][5]}")
            source.saveData(mergeData, listener)
        } catch(e : Exception){
            listener.onOperationFail("saveData", e)
        }
    }

    private fun getOrientation(accelReading : FloatArray, magnetReading: FloatArray): FloatArray {
        val oriMat = FloatArray(3)
        val rotMat = FloatArray(9)
        SensorManager.getRotationMatrix(rotMat, null, accelReading, magnetReading)
        SensorManager.getOrientation(rotMat, oriMat)
//        oriMat[0] = oriMat[0] * 180 / Math.PI.toFloat() // azimuth, z
//        oriMat[1] = oriMat[1] * 180 / Math.PI.toFloat() // pitch, x
//        oriMat[2] = oriMat[2] * 180 / Math.PI.toFloat() // roll, y
        return oriMat
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
                        data.add(DataEntry(it[0], it[1], it[2], unixTimer.timeInMillis))
                    }
                    if(recordCategory){
                        categoryData.add(standOrSit.value)
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

    enum class Activity(val value: Int) {
        SIT(0),
        STAND(1),
        MOVE(2)
//        STAND_UP(2),
//        SIT_DOWN(3);
    }

    interface AutoStopListener {
        fun onAutoStop()
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
) {
    fun toFloatArray() : FloatArray {
        return floatArrayOf(x, y, z)
    }
}