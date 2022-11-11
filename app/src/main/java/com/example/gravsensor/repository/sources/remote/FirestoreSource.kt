package com.example.gravsensor.repository.sources.remote

import com.example.gravsensor.repository.DataRepository
import com.example.gravsensor.repository.sources.DataSource
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class FirestoreSource : DataSource.Remote {
    private val db = Firebase.firestore
    private var targetCollection = STAND_COLLECTION

    override fun saveData(
        data: List<FloatArray>,
        listener: DataRepository.OnDataResultListener
    ){
        val dataBatches = data.chunked(REC_PER_BATCH) { batch ->
            ArrayList<HashMap<String, Float>>().apply {
                for(entry in batch){
                    add(hashMapOf(
                        GRAV_X_KEY to entry[0],
                        GRAV_Y_KEY to entry[1],
                        GRAV_Z_KEY to entry[2],
                        LIN_X_KEY to entry[3],
                        LIN_Y_KEY to entry[4],
                        LIN_Z_KEY to entry[5],
                        CATEGORY_KEY to entry[6],
                        TIMESTAMP_KEY to entry[7],
                    ))
                }
            }
        }
        for(dataBatch in dataBatches){
            db.runBatch { writeBatch ->
                val newDoc = db.collection(TRAIN_COLLECTION).document(CATE_DOC).collection(
                    targetCollection).document()
                writeBatch.set(newDoc, hashMapOf(DATA_KEY to dataBatch))
            }.addOnSuccessListener {
                listener.onSaveDataSuccess(dataBatch.size)
            }.addOnFailureListener { e ->
                listener.onOperationFail("Firestore/saveData", e)
            }
        }
    }

    override fun setRemoteCollection(collectionName: String) {
        targetCollection = collectionName
    }

    companion object {
        const val NAMES_COLLECTION = "collections"
        const val NAMES_DOCUMENT = "names"

        const val TRAIN_COLLECTION = "train_data"
        const val CATE_DOC = "states"
        const val STAND_COLLECTION = "stand"
        const val SIT_COLLECTION = "sit"
        const val STAND_UP_COLLECTION = "stand_up"
        const val SIT_DOWN_COLLECTION = "sit_down"

        const val DATA_KEY = "data"
        const val GRAV_X_KEY = "grav_x"
        const val GRAV_Y_KEY = "grav_y"
        const val GRAV_Z_KEY = "grav_z"
        const val LIN_X_KEY = "lin_x"
        const val LIN_Y_KEY = "lin_y"
        const val LIN_Z_KEY = "lin_z"
        const val CATEGORY_KEY = "category"
        const val TIMESTAMP_KEY = "timestamp"

        const val REC_PER_BATCH = 500
    }
}