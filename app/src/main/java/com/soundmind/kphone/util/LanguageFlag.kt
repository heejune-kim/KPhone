package com.soundmind.kphone.util

import com.soundmind.kphone.R

class LanguageFlag {
    companion object {
        fun getInfoForLanguage(language: String): Pair<Int, String> {
            return when (language) {
                "en" -> Pair(R.drawable.lang_en, "English")
                "es" -> Pair(R.drawable.lang_es, "Spanish")
                "fr" -> Pair(R.drawable.lang_fr, "French")
                "de" -> Pair(R.drawable.lang_de, "German")
                "vi" -> Pair(R.drawable.lang_vi, "Vietnamese")
                "ja" -> Pair(R.drawable.lang_ja, "Japanese")
                "ko" -> Pair(R.drawable.lang_ko, "Korean")
                "zh" -> Pair(R.drawable.lang_zh, "Chinese")
                else -> Pair(R.drawable.lang_en, "Undefined")
            }
        }

        fun getFlagForLanguage(language: String): Int {
            return getInfoForLanguage(language).first
        }

        fun getFullNameForLanguage(language: String): String {
            return getInfoForLanguage(language).second
        }

        fun switchDestinationLanguage(language: String): String {
            return when (language) {
                "en" -> "ko"
                "ko" -> "en"
                else -> ""
            }
        }

        fun getCurrencyUnitForLanguage(language: String): String {
            return when (language) {
                "en" -> "$"
                "ko" -> "₩"
                "ja" -> "¥"
                "vi" -> "d"
                else -> ""
            }
        }

        fun getCurrencyCodeForLanguage(language: String): String {
            return when (language) {
                "en" -> "USD"
                "es" -> "EUR"
                "fr" -> "EUR"
                "de" -> "EUR"
                "vi" -> "VND"
                "ja" -> "JPY"
                "ko" -> "KRW"
                "zh" -> "CNY"
                else -> "USD"
            }
        }
    }
}