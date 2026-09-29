package com.example.data.models

import java.text.NumberFormat
import java.util.Locale

enum class CurrencyMode {
    USD,
    PEN;

    companion object {
        const val USD_TO_PEN_RATE = 3.78

        fun formatPrice(amountUsd: Double, mode: CurrencyMode): String {
            val format = NumberFormat.getNumberInstance(Locale.US)
            format.maximumFractionDigits = 0
            format.minimumFractionDigits = 0

            return when (mode) {
                USD -> "$ " + format.format(amountUsd) + " USD"
                PEN -> "S/. " + format.format(amountUsd * USD_TO_PEN_RATE) + " PEN"
            }
        }

        fun formatDual(amountUsd: Double): String {
            val format = NumberFormat.getNumberInstance(Locale.US)
            format.maximumFractionDigits = 0
            val solAmount = amountUsd * USD_TO_PEN_RATE
            return "$ ${format.format(amountUsd)} USD ≈ S/. ${format.format(solAmount)}"
        }
    }
}
