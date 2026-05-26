package com.example.data

import com.squareup.moshi.JsonClass

enum class CalculationMode {
    DOSE_TO_AI,
    AI_TO_DOSE,
    FARMER_PRACTICE,
    UNIT_CONVERTER
}

@JsonClass(generateAdapter = true)
data class ActiveIngredientInput(
    val name: String = "",
    val concentration: Double? = null,
    val requiredDeliveryHa: Double? = null
)

@JsonClass(generateAdapter = true)
data class SavedCalculationInput(
    val productName: String = "",
    val formulationType: String = "", // Solo or Premix
    val doseUnit: String = "ml/ha", // ml/ha, g/ha, ml/acre, g/acre
    val formulationDose: Double? = null,
    val ais: List<ActiveIngredientInput> = emptyList(),
    // Farmer Practice specific
    val farmerDosePerL: Double? = null,
    val tankSize: Double? = null,
    val tanksPerAcre: Double? = null,
    // Unit Converter specific
    val doseValue: Double? = null,
    val conversionSource: String = "ha" // ha or acre
)

@JsonClass(generateAdapter = true)
data class SavedCalculationOutput(
    val formDoseHa: Double = 0.0,
    val formDoseAcre: Double = 0.0,
    val results: List<ActiveIngredientResult> = emptyList(),
    val formDoseHaUnit: String = "ml/ha",
    val formDoseAcreUnit: String = "ml/acre",
    // Farmer practice specific
    val formDosePerTank: Double = 0.0,
    val formDosePerTankUnit: String = "ml/tank",
    // Info matching
    val dosesMatching: Boolean = true,
    val roundedAiUsedNote: String = ""
)

@JsonClass(generateAdapter = true)
data class ActiveIngredientResult(
    val name: String,
    val enteredConcentration: Double,
    val roundedConcentration: Int,
    val deliveryHa: Double,
    val deliveryAcre: Double,
    val requiredFormDoseHa: Double = 0.0,
    val requiredFormDoseAcre: Double = 0.0
)
