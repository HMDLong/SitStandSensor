package com.example.gravsensor.repository.sources

import com.example.gravsensor.repository.DataRepository

interface DataSource {
    interface Remote {
        fun saveData(data : List<FloatArray>, listener : DataRepository.OnDataResultListener)
        fun setRemoteCollection(collectionName: String)
    }

    interface Local {}
}