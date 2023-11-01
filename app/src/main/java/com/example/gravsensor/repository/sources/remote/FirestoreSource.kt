package com.example.gravsensor.repository.sources.remote

import com.example.gravsensor.repository.DataRepository
import com.example.gravsensor.repository.sources.DataSource
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class FirestoreSource : DataSource.Remote {
    private val db = Firebase.firestore
    private var targetCollection = "shirt_front_pocket"

    override fun saveData(
        data: List<FloatArray>,
        listener: DataRepository.OnDataResultListener
    ){
        val dataBatches = data.chunked(REC_PER_BATCH) { batch ->
            ArrayList<HashMap<String, Float>>().apply {
                for(entry in batch){
                    add(hashMapOf(
                        // accel
                        ACCEL_X to entry[0],
                        ACCEL_Y to entry[1],
                        ACCEL_Z to entry[2],
                        // orientation
                        EARTH_AZIMUTH to entry[3],
                        EARTH_PITCH to entry[4],
                        EARTH_ROLL to entry[5],
                        // gravity
                        GRAV_X_KEY to entry[6],
                        GRAV_Y_KEY to entry[7],
                        GRAV_Z_KEY to entry[8],
                        // linear accel
                        LIN_X_KEY to entry[9],
                        LIN_Y_KEY to entry[10],
                        LIN_Z_KEY to entry[11],
                        // label
                        CATEGORY_KEY to entry[12],
                        // timestamp
                        TIMESTAMP_KEY to entry[13],
                    ))
                }
            }
        }
        for(dataBatch in dataBatches){
            db.runBatch { writeBatch ->
                val newDoc = db.collection(TRAIN_COLLECTION)
                    .document(targetCollection)
                    .collection(CATE_DOC)
                    .document()
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
        const val TRAIN_COLLECTION = "pockets"
        const val CATE_DOC = "fix" //""mix" // "with_move"

        const val DATA_KEY = "data"
        const val ACCEL_X = "accel_x"
        const val ACCEL_Y = "accel_y"
        const val ACCEL_Z = "accel_z"
        const val EARTH_AZIMUTH = "e_azimuth"
        const val EARTH_PITCH = "e_pitch"
        const val EARTH_ROLL = "e_roll"
        const val GRAV_X_KEY = "grav_x"
        const val GRAV_Y_KEY = "grav_y"
        const val GRAV_Z_KEY = "grav_z"
        const val LIN_X_KEY = "lin_x"
        const val LIN_Y_KEY = "lin_y"
        const val LIN_Z_KEY = "lin_z"
//        const val ROT_X_KEY = "rot_x"
//        const val ROT_Y_KEY = "rot_y"
//        const val ROT_Z_KEY = "rot_z"
        const val CATEGORY_KEY = "category"
        const val TIMESTAMP_KEY = "timestamp"

        const val REC_PER_BATCH = 500
    }
}