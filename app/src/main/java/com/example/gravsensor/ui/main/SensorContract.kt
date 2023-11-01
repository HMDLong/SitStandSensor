package com.example.gravsensor.ui.main

import android.content.Context
import android.hardware.SensorEventListener
import com.example.gravsensor.session.RecordSession

interface SensorContract {
    interface View {
        fun onSendDataSuccess(itemCount : Int)
        fun onFail(e : Exception)
        fun onAutoStopReached()
    }

    interface Presenter {
        fun createSession(context: Context)
        fun startRecording()
        fun stopRecording()
        fun isRecording() : Boolean
        fun saveRecording()
        fun changeRecordConfig(
            standOrSit : RecordSession.Activity? = null,
            isAutoStopEnabled : Boolean? = null
        )

        fun registerSensor(sensorType : Int, listener : SensorEventListener)
        fun clearSession()

        fun setTargetCollection(collectionName: String)
    }
}