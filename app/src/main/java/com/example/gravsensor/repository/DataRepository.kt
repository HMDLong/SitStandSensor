package com.example.gravsensor.repository

import com.example.gravsensor.repository.sources.DataSource

class DataRepository private constructor(
    private var remote : DataSource.Remote?,
    private var local : DataSource.Local?,
) : DataSource.Remote, DataSource.Local {

    val remoteSource
        get() = remote ?: throw Exception("No remote assigned")

    override fun saveData(
        data: List<FloatArray>,
        listener: OnDataResultListener
    ) {
        remote?.saveData(data, listener)
    }

    override fun setRemoteCollection(collectionName: String) {
        remote?.setRemoteCollection(collectionName)
    }

    interface OnDataResultListener {
        fun onSaveDataSuccess(itemCount : Int)
        fun onOperationFail(optTag: String, e : Exception)
    }

    companion object {
        private var instance : DataRepository? = null
        fun getInstance(remote: DataSource.Remote?, local: DataSource.Local?) =
            instance ?: DataRepository(remote, local).also { instance = it }
    }
}