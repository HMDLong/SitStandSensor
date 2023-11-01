package com.example.gravsensor.ui.main

import android.content.Context
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import com.example.gravsensor.repository.DataRepository
import com.example.gravsensor.session.RecordSession
import javax.inject.Inject

class SensorPresenter @Inject constructor(private val repository : DataRepository) : SensorContract.Presenter {
    private lateinit var session : RecordSession
    private var _view : SensorContract.View? = null
    private val resultListener by lazy { object : DataRepository.OnDataResultListener {
        override fun onSaveDataSuccess(itemCount: Int) {
            _view?.onSendDataSuccess(itemCount)
        }

        override fun onOperationFail(optTag: String, e: Exception) {
            _view?.onFail(e)
        }
    }}

    fun setView(view : SensorContract.View){
        _view = _view ?: view
    }

    override fun createSession(context : Context){
        session = RecordSession.getInstance(context.getSystemService(AppCompatActivity.SENSOR_SERVICE) as SensorManager)
        session.autoStopListener = object : RecordSession.AutoStopListener {
            override fun onAutoStop() {
                _view?.onAutoStopReached()
            }
        }
    }

    override fun startRecording() {
        session.startRecord()
    }

    override fun stopRecording() {
        session.stopRecord()
    }

    override fun isRecording() = session.recording

    override fun saveRecording() {
        session.saveRecord(repository.remoteSource, resultListener)
    }

    override fun changeRecordConfig(
        standOrSit: RecordSession.Activity?,
        isAutoStopEnabled: Boolean?
    ) {
        standOrSit?.let {
            session.standOrSit = standOrSit
        }
        isAutoStopEnabled?.let {
            session.autoStopEnabled = isAutoStopEnabled
        }
    }

    override fun registerSensor(sensorType: Int, listener: SensorEventListener) {
        session.registerSensor(sensorType, listener)
    }

    override fun clearSession() {
        session.reset()
    }

    override fun setTargetCollection(collectionName: String) {
        repository.setRemoteCollection(collectionName)
    }
}