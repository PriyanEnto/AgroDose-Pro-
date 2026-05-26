package com.example.utils

import kotlin.math.round

object CalculatorUtils {

    const val HA_TO_ACRE = 2.5

    /**
     * Converts area from hectares to acres.
     */
    fun haToAcre(ha: Double): Double = ha * HA_TO_ACRE

    /**
     * Converts area from acres to hectares.
     */
    fun acreToHa(acre: Double): Double = acre / HA_TO_ACRE

    /**
     * Converts dose per hectare to dose per acre.
     */
    fun doseHaToAcre(doseHa: Double): Double = doseHa / HA_TO_ACRE

    /**
     * Converts dose per acre to dose per hectare.
     */
    fun doseAcreToHa(doseAcre: Double): Double = doseAcre * HA_TO_ACRE

    /**
     * Rounds active ingredient percentage based on selected mode:
     * - "standard": Standard mathematical rounding (e.g. 5.66 -> 6, 28.3 -> 28)
     * - "multiple_10": Rounds to the nearest multiple of 10 (e.g. 28.3 -> 30, 5.66 -> 10)
     * - "industry": Standard integer for <10%, nearest 10 for >=10% (e.g. 5.66 -> 6, 28.3 -> 30)
     */
    fun roundAiPct(value: Double, mode: String): Int {
        if (value <= 0.0) return 0
        return when (mode) {
            "standard" -> round(value).toInt().coerceIn(1, 100)
            "multiple_10" -> {
                val rounded = (round(value / 10.0) * 10).toInt()
                rounded.coerceIn(1, 100)
            }
            "industry" -> {
                if (value >= 10.0) {
                    val rounded = (round(value / 10.0) * 10).toInt()
                    rounded.coerceIn(10, 100)
                } else {
                    round(value).toInt().coerceIn(1, 9)
                }
            }
            else -> round(value).toInt().coerceIn(1, 100)
        }
    }

    /**
     * Computes active ingredient delivery per hectare.
     * AI delivery per ha = Formulation dose per ha * (rounded AI % / 100)
     */
    fun calculateAiDeliveryHa(formulationDoseHa: Double, roundedAiPct: Int): Double {
        return formulationDoseHa * (roundedAiPct / 100.0)
    }

    /**
     * Computes formulation dose per hectare from required active ingredient delivery per hectare.
     * Formulation dose per ha = (Required AI delivery per ha * 100) / rounded AI %
     */
    fun calculateFormDoseHa(requiredAiHa: Double, roundedAiPct: Int): Double {
        if (roundedAiPct <= 0) return 0.0
        return (requiredAiHa * 100.0) / roundedAiPct
    }

    /**
     * Formats positive double outputs to two decimal places.
     */
    fun formatDoubleValue(value: Double): String {
        return String.format(java.util.Locale.US, "%.2f", value)
    }
}
