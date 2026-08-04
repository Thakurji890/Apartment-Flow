package com.example.feature.expense.domain.model

data class ExpenseCategory(
    val id: String,
    val name: String,
    val icon: String, // You might map this to a Material Icon string name or enum
    val color: String // Hex color string
)

object DefaultCategories {
    val list = listOf(
        ExpenseCategory("rent", "Rent", "Home", "#4CAF50"),
        ExpenseCategory("electricity", "Electricity", "Bolt", "#FFC107"),
        ExpenseCategory("water", "Water", "WaterDrop", "#2196F3"),
        ExpenseCategory("gas", "Gas", "LocalFireDepartment", "#FF5722"),
        ExpenseCategory("internet", "Internet", "Wifi", "#9C27B0"),
        ExpenseCategory("grocery", "Grocery", "ShoppingCart", "#8BC34A"),
        ExpenseCategory("food", "Food", "Restaurant", "#FF9800"),
        ExpenseCategory("transport", "Transport", "DirectionsCar", "#607D8B"),
        ExpenseCategory("cleaning", "Cleaning", "CleaningServices", "#00BCD4"),
        ExpenseCategory("furniture", "Furniture", "Chair", "#795548"),
        ExpenseCategory("entertainment", "Entertainment", "Movie", "#E91E63"),
        ExpenseCategory("medical", "Medical", "LocalHospital", "#F44336"),
        ExpenseCategory("shopping", "Shopping", "LocalMall", "#3F51B5"),
        ExpenseCategory("education", "Education", "School", "#FFEB3B"),
        ExpenseCategory("pets", "Pets", "Pets", "#8D6E63"),
        ExpenseCategory("mobile", "Mobile Recharge", "PhoneAndroid", "#009688"),
        ExpenseCategory("subscription", "Subscription", "CardMembership", "#673AB7"),
        ExpenseCategory("others", "Others", "Category", "#9E9E9E")
    )
    
    fun getCategoryById(id: String): ExpenseCategory {
        return list.find { it.id == id } ?: list.last()
    }
}
