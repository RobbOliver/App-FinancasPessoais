package com.robson.financas.ui.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocalGroceryStore
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Curated icon set shared by accounts and categories. Keys are stored in Room
 * (String), so the mapping to an [ImageVector] can change freely without a migration.
 */
object IconCatalog {
    val icons: Map<String, ImageVector> = mapOf(
        "restaurant" to Icons.Filled.Restaurant,
        "fastfood" to Icons.Filled.Fastfood,
        "local_cafe" to Icons.Filled.LocalCafe,
        "local_grocery_store" to Icons.Filled.LocalGroceryStore,
        "directions_car" to Icons.Filled.DirectionsCar,
        "local_gas_station" to Icons.Filled.LocalGasStation,
        "flight" to Icons.Filled.Flight,
        "home" to Icons.Filled.Home,
        "movie" to Icons.Filled.Movie,
        "fitness_center" to Icons.Filled.FitnessCenter,
        "local_hospital" to Icons.Filled.LocalHospital,
        "school" to Icons.Filled.School,
        "shopping_bag" to Icons.Filled.ShoppingBag,
        "receipt_long" to Icons.AutoMirrored.Filled.ReceiptLong,
        "pets" to Icons.Filled.Pets,
        "payments" to Icons.Filled.Payments,
        "work" to Icons.Filled.Work,
        "trending_up" to Icons.AutoMirrored.Filled.TrendingUp,
        "attach_money" to Icons.Filled.AttachMoney,
        "account_balance" to Icons.Filled.AccountBalance,
        "savings" to Icons.Filled.Savings,
        "wallet" to Icons.Filled.Wallet,
        "credit_card" to Icons.Filled.CreditCard,
        "swap_horiz" to Icons.Filled.SwapHoriz,
        "more_horiz" to Icons.Filled.MoreHoriz,
    )

    val defaultKey: String = "more_horiz"

    fun resolve(key: String?): ImageVector = icons[key] ?: icons.getValue(defaultKey)
}
