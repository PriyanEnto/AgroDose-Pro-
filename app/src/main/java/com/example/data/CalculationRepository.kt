package com.example.data

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.Flow

class CalculationRepository(private val dao: CalculationDao) {
    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val inputAdapter = moshi.adapter(SavedCalculationInput::class.java)
    private val outputAdapter = moshi.adapter(SavedCalculationOutput::class.java)

    val allHistory: Flow<List<CalculationHistory>> = dao.getAllHistory()

    suspend fun saveCalculation(
        title: String,
        mode: CalculationMode,
        input: SavedCalculationInput,
        output: SavedCalculationOutput
    ) {
        val inputStr = inputAdapter.toJson(input) ?: ""
        val outputStr = outputAdapter.toJson(output) ?: ""
        val record = CalculationHistory(
            title = title,
            mode = mode.name,
            inputJson = inputStr,
            outputJson = outputStr
        )
        dao.insertHistory(record)
    }

    suspend fun deleteHistory(id: Int) {
        dao.deleteHistoryById(id)
    }

    suspend fun clearHistory() {
        dao.clearAllHistory()
    }

    fun deserializeInput(json: String): SavedCalculationInput? {
        return try {
            inputAdapter.fromJson(json)
        } catch (e: Exception) {
            null
        }
    }

    fun deserializeOutput(json: String): SavedCalculationOutput? {
        return try {
            outputAdapter.fromJson(json)
        } catch (e: Exception) {
            null
        }
    }
}
