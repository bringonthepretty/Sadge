package com.wah.sadge.model.exchangerate

import java.util.Date

data class ExchangeRate(var currencyId: Int,
                   var currencyAbbreviation: String,
                   var currencyScale: Int,
                   var currencyName: String,
                   var currencyOfficialRate: Double,
                   var date: Date = Date())