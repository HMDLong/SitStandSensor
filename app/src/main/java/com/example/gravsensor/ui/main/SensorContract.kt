package com.example.gravsensor.ui.main

import android.content.Context
import android.hardware.SensorEventListener

interface SensorContract {
    interface View {
        fun onSendDataSuccess(itemCount : Int)
        fun onFail(e : Exception)
        fun onBatchLimitReached()
    }

    interface Presenter {
        fun createSession(context: Context)
        fun startRecording()
        fun stopRecording()
        fun isRecording() : Boolean
        fun saveRecording()
        fun changeRecordConfig(standOrSit : Int? = null)

        fun registerSensor(sensorType : Int, listener : SensorEventListener)
        fun clearSession()

        fun setTargetCollection(collectionName: String)
    }
}