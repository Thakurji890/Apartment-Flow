package com.example.feature.apartmentmanager.model

/**
 * Represents world currencies conforming to ISO 4217 specifications,
 * providing the 3-letter code, display symbol, country/region description,
 * and standardized formatting hints.
 */
data class SupportedCurrency(
    val code: String,
    val symbol: String,
    val name: String,
    val flagEmoji: String
) {
    val displayLabel: String
        get() = "$flagEmoji $code ($symbol) - $name"
}

object CurrencyCatalog {
    val allCurrencies: List<SupportedCurrency> = listOf(
        SupportedCurrency("USD", "$", "United States Dollar", "🇺🇸"),
        SupportedCurrency("EUR", "€", "Eurozone Euro", "🇪🇺"),
        SupportedCurrency("GBP", "£", "British Pound Sterling", "🇬🇧"),
        SupportedCurrency("INR", "₹", "Indian Rupee", "🇮🇳"),
        SupportedCurrency("JPY", "¥", "Japanese Yen", "🇯🇵"),
        SupportedCurrency("CAD", "CA$", "Canadian Dollar", "🇨🇦"),
        SupportedCurrency("AUD", "A$", "Australian Dollar", "🇦🇺"),
        SupportedCurrency("CHF", "CHF", "Swiss Franc", "🇨🇭"),
        SupportedCurrency("CNY", "CN¥", "Chinese Yuan Renminbi", "🇨🇳"),
        SupportedCurrency("SGD", "S$", "Singapore Dollar", "🇸🇬"),
        SupportedCurrency("AED", "AED", "United Arab Emirates Dirham", "🇦🇪"),
        SupportedCurrency("SAR", "SAR", "Saudi Riyal", "🇸🇦"),
        SupportedCurrency("BRL", "R$", "Brazilian Real", "🇧🇷"),
        SupportedCurrency("MXN", "Mex$", "Mexican Peso", "🇲🇽"),
        SupportedCurrency("KRW", "₩", "South Korean Won", "🇰🇷"),
        SupportedCurrency("SEK", "kr", "Swedish Krona", "🇸🇪"),
        SupportedCurrency("NOK", "kr", "Norwegian Krone", "🇳🇴"),
        SupportedCurrency("DKK", "kr", "Danish Krone", "🇩🇰"),
        SupportedCurrency("NZD", "NZ$", "New Zealand Dollar", "🇳🇿"),
        SupportedCurrency("ZAR", "R", "South African Rand", "🇿🇦"),
        SupportedCurrency("TRY", "₺", "Turkish Lira", "🇹🇷"),
        SupportedCurrency("PLN", "zł", "Polish Zloty", "🇵🇱"),
        SupportedCurrency("THB", "฿", "Thai Baht", "🇹🇭"),
        SupportedCurrency("IDR", "Rp", "Indonesian Rupiah", "🇮🇩"),
        SupportedCurrency("MYR", "RM", "Malaysian Ringgit", "🇲🇾"),
        SupportedCurrency("PHP", "₱", "Philippine Peso", "🇵🇭"),
        SupportedCurrency("VND", "₫", "Vietnamese Dong", "🇻🇳"),
        SupportedCurrency("EGP", "E£", "Egyptian Pound", "🇪🇬"),
        SupportedCurrency("HKD", "HK$", "Hong Kong Dollar", "🇭🇰"),
        SupportedCurrency("ILS", "₪", "Israeli New Shekel", "🇮🇱"),
        SupportedCurrency("CLP", "CLP$", "Chilean Peso", "🇨🇱"),
        SupportedCurrency("COP", "COL$", "Colombian Peso", "🇨🇴"),
        SupportedCurrency("PKR", "₨", "Pakistani Rupee", "🇵🇰"),
        SupportedCurrency("NGN", "₦", "Nigerian Naira", "🇳🇬"),
        SupportedCurrency("KES", "KSh", "Kenyan Shilling", "🇰🇪"),
        SupportedCurrency("QAR", "QAR", "Qatari Riyal", "🇶🇦"),
        SupportedCurrency("KWD", "KWD", "Kuwaiti Dinar", "🇰🇼")
    )

    fun findByCode(code: String): SupportedCurrency? {
        return allCurrencies.find { it.code.equals(code.trim(), ignoreCase = true) }
    }

    fun findBySymbolOrCode(input: String): SupportedCurrency {
        val trimmed = input.trim()
        return allCurrencies.find {
            it.symbol.equals(trimmed, ignoreCase = true) || it.code.equals(trimmed, ignoreCase = true)
        } ?: SupportedCurrency(trimmed.ifBlank { "USD" }, trimmed.ifBlank { "$" }, "Custom Currency", "🌐")
    }
}
