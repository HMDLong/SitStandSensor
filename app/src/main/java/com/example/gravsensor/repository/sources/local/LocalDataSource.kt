package com.example.gravsensor.repository.sources.local

import android.content.Context
import android.content.Context.MODE_APPEND
import java.io.OutputStreamWriter
import java.lang.StringBuilder

class LocalDataSource {
    fun saveData(context : Context, data : List<FloatArray>){
        OutputStreamWriter(context.openFileOutput(FILENAME, MODE_APPEND)).apply {
            for(entry in data){
                write(entry.toEntryString())
            }
            flush()
            close()
        }
    }

    companion object {
        const val FILENAME = "data.txt"
    }
}

fun FloatArray.toEntryString() : String {
    val stringBuilder = StringBuilder()
    for(element in this){
        stringBuilder.append(element).append(',')
    }
    return stringBuilder.append('\n').toString()
}