package com.example.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.utils.CalculatorUtils
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.abs

sealed class Screen {
    object Home : Screen()
    data class Input(val mode: CalculationMode) : Screen()
    data class Result(
        val title: String,
        val mode: CalculationMode,
        val input: SavedCalculationInput,
        val output: SavedCalculationOutput,
        val historyId: Int? = null
    ) : Screen()
    object History : Screen()
}

class MainViewModel(private val repository: CalculationRepository) : ViewModel() {

    // Reactive lists of saved histories
    val historyState: StateFlow<List<CalculationHistory>> = repository.allHistory
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Manual State-Based Navigation backstack
    private val backstack = mutableStateListOf<Screen>(Screen.Home)
    var currentScreen by mutableStateOf<Screen>(Screen.Home)
        private set

    fun navigateTo(screen: Screen) {
        backstack.add(screen)
        currentScreen = screen
    }

    fun navigateBack(): Boolean {
        if (backstack.size > 1) {
            backstack.removeAt(backstack.lastIndex)
            currentScreen = backstack.last()
            return true
        }
        return false
    }

    fun navigateToHome() {
        backstack.clear()
        backstack.add(Screen.Home)
        currentScreen = Screen.Home
    }

    // --- State Variables for Calculation Forms ---
    var productName by mutableStateOf("")
    var formulationType by mutableStateOf("Solo") // Solo / Premix
    var doseUnit by mutableStateOf("ml/ha") // ml/ha, g/ha, ml/acre, g/acre
    var formulationDose by mutableStateOf("")
    var roundingMode by mutableStateOf("industry") // "standard", "industry", "multiple_10"

    // Multi Active Ingredients (max 3)
    val aiInputs = mutableStateListOf(
        ActiveIngredientInput("AI 1", null, null),
        ActiveIngredientInput("AI 2", null, null),
        ActiveIngredientInput("AI 3", null, null)
    )

    // Farmer practice
    var farmerDosePerL by mutableStateOf("")
    var farmerDoseUnit by mutableStateOf("ml/L") // ml/L or g/L
    var tankSize by mutableStateOf("15") // pre-filled with 15 L
    var tanksPerAcre by mutableStateOf("")

    // Unit Converter
    var converterDoseValue by mutableStateOf("")
    var converterSourceUnit by mutableStateOf("ha") // "ha" or "acre"

    // Error list for validation UI
    var validationError by mutableStateOf<String?>(null)

    fun resetForm() {
        productName = ""
        formulationType = "Solo"
        doseUnit = "ml/ha"
        formulationDose = ""
        roundingMode = "industry"
        aiInputs[0] = ActiveIngredientInput("", null, null)
        aiInputs[1] = ActiveIngredientInput("", null, null)
        aiInputs[2] = ActiveIngredientInput("", null, null)
        farmerDosePerL = ""
        farmerDoseUnit = "ml/L"
        tankSize = "15"
        tanksPerAcre = ""
        converterDoseValue = ""
        converterSourceUnit = "ha"
        validationError = null
    }

    // Populate fields from history
    fun loadHistoryRecord(record: CalculationHistory) {
        val input = repository.deserializeInput(record.inputJson) ?: return
        val output = repository.deserializeOutput(record.outputJson) ?: return
        
        // Setup state representatively
        productName = input.productName
        formulationType = input.formulationType
        doseUnit = input.doseUnit
        formulationDose = input.formulationDose?.toString() ?: ""
        roundingMode = "industry" // default to industry but allow editing
        
        for (i in 0..2) {
            if (i < input.ais.size) {
                aiInputs[i] = input.ais[i]
            } else {
                aiInputs[i] = ActiveIngredientInput("", null, null)
            }
        }
        
        farmerDosePerL = input.farmerDosePerL?.toString() ?: ""
        tankSize = input.tankSize?.toString() ?: "15"
        tanksPerAcre = input.tanksPerAcre?.toString() ?: ""
        converterDoseValue = input.doseValue?.toString() ?: ""
        converterSourceUnit = input.conversionSource

        navigateTo(
            Screen.Result(
                title = record.title,
                mode = CalculationMode.valueOf(record.mode),
                input = input,
                output = output,
                historyId = record.id
            )
        )
    }

    fun deleteHistoryRecord(id: Int) {
        viewModelScope.launch {
            repository.deleteHistory(id)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    // --- Core Action: Trigger Calculations ---
    fun runCalculation(mode: CalculationMode) {
        validationError = null
        val err = validate(mode)
        if (err != null) {
            validationError = err
            return
        }

        val input = createCalculationInput(mode)
        val output = executeMath(mode, input)

        val title = when (mode) {
            CalculationMode.DOSE_TO_AI -> if (productName.isNotBlank()) "AI Delivery: $productName" else "AI Delivery Report"
            CalculationMode.AI_TO_DOSE -> if (productName.isNotBlank()) "Dose Req: $productName" else "Required Dose Report"
            CalculationMode.FARMER_PRACTICE -> if (productName.isNotBlank()) "Farmer Practice: $productName" else "Farmer Practice Report"
            CalculationMode.UNIT_CONVERTER -> "Dose Converter Report"
        }

        navigateTo(Screen.Result(title = title, mode = mode, input = input, output = output))
    }

    // --- Core Action: Persist Result ---
    fun saveActiveCalculation(
        title: String,
        mode: CalculationMode,
        input: SavedCalculationInput,
        output: SavedCalculationOutput
    ) {
        viewModelScope.launch {
            repository.saveCalculation(title, mode, input, output)
        }
    }

    // --- Private Math Executor ---
    private fun executeMath(mode: CalculationMode, input: SavedCalculationInput): SavedCalculationOutput {
        return when (mode) {
            CalculationMode.DOSE_TO_AI -> {
                val dose = input.formulationDose ?: 0.0
                val isHa = input.doseUnit.endsWith("/ha")
                val isLiquid = input.doseUnit.startsWith("ml")
                
                val doseHa = if (isHa) dose else CalculatorUtils.doseAcreToHa(dose)
                val doseAcre = if (!isHa) dose else CalculatorUtils.doseHaToAcre(dose)

                val results = input.ais.map { ai ->
                    val concentration = ai.concentration ?: 0.0
                    val roundedPct = CalculatorUtils.roundAiPct(concentration, roundingMode)
                    val deliveryHa = CalculatorUtils.calculateAiDeliveryHa(doseHa, roundedPct)
                    val deliveryAcre = deliveryHa / 2.5
                    
                    ActiveIngredientResult(
                        name = ai.name.ifBlank { "Active Ingredient" },
                        enteredConcentration = concentration,
                        roundedConcentration = roundedPct,
                        deliveryHa = deliveryHa,
                        deliveryAcre = deliveryAcre
                    )
                }

                SavedCalculationOutput(
                    formDoseHa = doseHa,
                    formDoseAcre = doseAcre,
                    results = results,
                    formDoseHaUnit = if (isLiquid) "ml/ha" else "g/ha",
                    formDoseAcreUnit = if (isLiquid) "ml/acre" else "g/acre",
                    roundedAiUsedNote = "AI % has been rounded to the nearest 1-2% as industry standard."
                )
            }

            CalculationMode.AI_TO_DOSE -> {
                val results = input.ais.map { ai ->
                    val concentration = ai.concentration ?: 0.0
                    val roundedPct = CalculatorUtils.roundAiPct(concentration, roundingMode)
                    val reqDeliveryHa = ai.requiredDeliveryHa ?: 0.0
                    val reqFormDoseHa = CalculatorUtils.calculateFormDoseHa(reqDeliveryHa, roundedPct)
                    val reqFormDoseAcre = reqFormDoseHa / 2.5
                    
                    ActiveIngredientResult(
                        name = ai.name.ifBlank { "Active Ingredient" },
                        enteredConcentration = concentration,
                        roundedConcentration = roundedPct,
                        deliveryHa = reqDeliveryHa,
                        deliveryAcre = reqDeliveryHa / 2.5,
                        requiredFormDoseHa = reqFormDoseHa,
                        requiredFormDoseAcre = reqFormDoseAcre
                    )
                }

                // Check comparison matching
                var matching = true
                if (results.size > 1) {
                    val baseDose = results[0].requiredFormDoseHa
                    for (i in 1 until results.size) {
                        if (abs(results[i].requiredFormDoseHa - baseDose) > 0.05) {
                            matching = false
                            break
                        }
                    }
                }

                val primaryFormDoseHa = results.firstOrNull()?.requiredFormDoseHa ?: 0.0
                val primaryFormDoseAcre = results.firstOrNull()?.requiredFormDoseAcre ?: 0.0

                SavedCalculationOutput(
                    formDoseHa = primaryFormDoseHa,
                    formDoseAcre = primaryFormDoseAcre,
                    results = results,
                    formDoseHaUnit = if (doseUnit.startsWith("ml")) "ml/ha" else "g/ha",
                    formDoseAcreUnit = if (doseUnit.startsWith("ml")) "ml/acre" else "g/acre",
                    dosesMatching = matching,
                    roundedAiUsedNote = "AI % has been rounded to the nearest 1-2% as industry standard."
                )
            }

            CalculationMode.FARMER_PRACTICE -> {
                val doseL = input.farmerDosePerL ?: 0.0
                val vol = input.tankSize ?: 15.0
                val tanks = input.tanksPerAcre ?: 0.0
                val isLiquid = farmerDoseUnit == "ml/L"

                val perfTank = doseL * vol
                val perfAcre = perfTank * tanks
                val perfHa = perfAcre * 2.5

                val results = input.ais.map { ai ->
                    val concentration = ai.concentration ?: 0.0
                    val roundedPct = CalculatorUtils.roundAiPct(concentration, roundingMode)
                    val delAcre = perfAcre * (roundedPct / 100.0)
                    val delHa = delAcre * 2.5
                    
                    ActiveIngredientResult(
                        name = ai.name.ifBlank { "Active Ingredient" },
                        enteredConcentration = concentration,
                        roundedConcentration = roundedPct,
                        deliveryHa = delHa,
                        deliveryAcre = delAcre
                    )
                }

                SavedCalculationOutput(
                    formDoseHa = perfHa,
                    formDoseAcre = perfAcre,
                    formDosePerTank = perfTank,
                    results = results,
                    formDoseHaUnit = if (isLiquid) "ml/ha" else "g/ha",
                    formDoseAcreUnit = if (isLiquid) "ml/acre" else "g/acre",
                    formDosePerTankUnit = if (isLiquid) "ml/tank" else "g/tank",
                    roundedAiUsedNote = "AI % has been rounded to the nearest 1-2% as industry standard."
                )
            }

            CalculationMode.UNIT_CONVERTER -> {
                val valIn = input.doseValue ?: 0.0
                val isHaMap = input.conversionSource == "ha"
                val outHa = if (isHaMap) valIn else CalculatorUtils.doseAcreToHa(valIn)
                val outAcre = if (!isHaMap) valIn else CalculatorUtils.doseHaToAcre(valIn)

                SavedCalculationOutput(
                    formDoseHa = outHa,
                    formDoseAcre = outAcre,
                    results = emptyList(),
                    formDoseHaUnit = if (doseUnit.startsWith("ml")) "ml/ha" else "g/ha",
                    formDoseAcreUnit = if (doseUnit.startsWith("ml")) "ml/acre" else "g/acre"
                )
            }
        }
    }

    // --- Private Form Translators ---
    private fun createCalculationInput(mode: CalculationMode): SavedCalculationInput {
        val activeAIs = aiInputs.filter { it.name.isNotBlank() || it.concentration != null || it.requiredDeliveryHa != null }
        return SavedCalculationInput(
            productName = productName.trim(),
            formulationType = formulationType,
            doseUnit = doseUnit,
            formulationDose = formulationDose.toDoubleOrNull(),
            ais = activeAIs,
            farmerDosePerL = farmerDosePerL.toDoubleOrNull(),
            tankSize = tankSize.toDoubleOrNull(),
            tanksPerAcre = tanksPerAcre.toDoubleOrNull(),
            doseValue = converterDoseValue.toDoubleOrNull(),
            conversionSource = converterSourceUnit
        )
    }

    // --- Form Validators ---
    private fun validate(mode: CalculationMode): String? {
        when (mode) {
            CalculationMode.DOSE_TO_AI -> {
                val dose = formulationDose.toDoubleOrNull()
                if (dose == null || dose <= 0) {
                    return "Please enter formulation dose."
                }
                
                // At least one valid AI must be present
                val activeAIs = aiInputs.filter { it.name.isNotBlank() || it.concentration != null }
                if (activeAIs.isEmpty()) {
                    return "Please enter at least one AI component."
                }

                // Any AI with concentration or name must be complete & valid
                for (ai in activeAIs) {
                    if (ai.concentration == null) {
                        return "Please enter AI concentration."
                    }
                    if (ai.concentration <= 0 || ai.concentration > 100) {
                        return "AI concentration must be between 0 and 100%."
                    }
                }
            }

            CalculationMode.AI_TO_DOSE -> {
                val activeAIs = aiInputs.filter { it.name.isNotBlank() || it.concentration != null || it.requiredDeliveryHa != null }
                if (activeAIs.isEmpty()) {
                    return "Please enter at least one AI component."
                }

                for (ai in activeAIs) {
                    if (ai.concentration == null) {
                        return "Please enter AI concentration."
                    }
                    if (ai.concentration <= 0 || ai.concentration > 100) {
                        return "AI concentration must be between 0 and 100%."
                    }
                    if (ai.requiredDeliveryHa == null || ai.requiredDeliveryHa <= 0) {
                        return "Please enter required AI delivery."
                    }
                }
            }

            CalculationMode.FARMER_PRACTICE -> {
                val doseVal = farmerDosePerL.toDoubleOrNull()
                if (doseVal == null || doseVal <= 0) {
                    return "Please enter formulation dose per L."
                }
                val tSize = tankSize.toDoubleOrNull()
                if (tSize == null || tSize <= 0) {
                    return "Please enter tank size."
                }
                val tPerAcre = tanksPerAcre.toDoubleOrNull()
                if (tPerAcre == null || tPerAcre <= 0) {
                    return "Please enter number of tanks."
                }

                val activeAIs = aiInputs.filter { it.name.isNotBlank() || it.concentration != null }
                if (activeAIs.isEmpty()) {
                    return "Please enter at least one AI component."
                }

                for (ai in activeAIs) {
                    if (ai.concentration == null) {
                        return "Please enter AI concentration."
                    }
                    if (ai.concentration <= 0 || ai.concentration > 100) {
                        return "AI concentration must be between 0 and 100%."
                    }
                }
            }

            CalculationMode.UNIT_CONVERTER -> {
                val doseVal = converterDoseValue.toDoubleOrNull()
                if (doseVal == null || doseVal <= 0) {
                    return "Please enter dose value."
                }
            }
        }
        return null
    }
}
