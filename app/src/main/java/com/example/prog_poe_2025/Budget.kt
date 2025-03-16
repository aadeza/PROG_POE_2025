data class Budget(
    val name: String,
    val totalBudget: Float,
    val spentAmounts: Map<String, Float> // Category -> Amount spent
)

