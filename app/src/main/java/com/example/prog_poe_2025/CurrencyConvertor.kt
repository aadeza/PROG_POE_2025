package com.example.prog_poe_2025

object CurrencyConverter {

    private val conversionRates = mapOf(
        "USD" to 1.0,
        "ZAR" to 18.69,
        "EUR" to 0.92,
        "GBP" to 0.78
    )

    /**
     * Converts the amount from one currency to another.
     * @param amount The amount to convert.
     * @param from The original currency (e.g., USD, EUR).
     * @param to The target currency (e.g., ZAR, GBP).
     * @return The converted amount or a default value if conversion fails.
     */
    fun convertAmount(amount: Long, from: String, to: String): Double {
        val fromRate = conversionRates[from] ?: throw IllegalArgumentException("Unsupported 'from' currency: $from")
        val toRate = conversionRates[to] ?: throw IllegalArgumentException("Unsupported 'to' currency: $to")
        return (amount / fromRate) * toRate
    }
}
