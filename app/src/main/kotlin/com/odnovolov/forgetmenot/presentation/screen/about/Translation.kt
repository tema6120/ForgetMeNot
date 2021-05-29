package com.odnovolov.forgetmenot.presentation.screen.about

import com.odnovolov.forgetmenot.R

enum class Translation(
    val flagEmoji: String,
    val languageNameRes: Int,
    val progress: String,
    val translators: List<Translator>
) {
    FRENCH(
        flagEmoji = "\uD83C\uDDEB\uD83C\uDDF7",
        languageNameRes = R.string.language_french,
        progress = "47%",
        translators = listOf(
            Translator(
                name = "Mr B",
                link = "https://crowdin.com/profile/mrb7"
            ),
            Translator(
                name = "Sitavi",
                link = "https://crowdin.com/profile/sitavi"
            ),
            Translator(
                name = "QuantiQia",
                link = "https://crowdin.com/profile/quantiqia"
            ),
            Translator(
                name = "Ryu1845",
                link = "https://crowdin.com/profile/ryu1845"
            ),
        )
    ),

    German(
        flagEmoji = "\uD83C\uDDE9\uD83C\uDDEA",
        languageNameRes = R.string.language_german,
        progress = "7%",
        translators = listOf(
            Translator(
                name = "Yushin Washio",
                link = "https://crowdin.com/profile/yuwash"
            ),
            Translator(
                name = "baschi29",
                link = "https://crowdin.com/profile/baschi29"
            ),
            Translator(
                name = "Unwoven",
                link = "https://crowdin.com/profile/unwovencrestless"
            )
        )
    ),

    Greek(
        flagEmoji = "\uD83C\uDDEC\uD83C\uDDF7",
        languageNameRes = R.string.language_greek,
        progress = "7%",
        translators = listOf(
            Translator(
                name = "Vasilis Ioannidis",
                link = "https://crowdin.com/profile/vioannidis"
            )
        )
    ),

    Russian(
        flagEmoji = "\uD83C\uDDF7\uD83C\uDDFA",
        languageNameRes = R.string.language_russian,
        progress = "100%",
        translators = listOf(
            Translator(
                name = "Odnovolov Artem",
                link = null
            )
        )
    ),

    Spanish(
        flagEmoji = "\uD83C\uDDEA\uD83C\uDDF8",
        languageNameRes = R.string.language_spanish,
        progress = "99%",
        translators = listOf(
            Translator(
                name = "sekinfo.org",
                link = "https://www.sekinfo.org"
            ),
            Translator(
                name = "Edwins0",
                link = "https://crowdin.com/profile/edwins0"
            ),
            Translator(
                name = "Cod3Radar",
                link = "https://crowdin.com/profile/cod3radar"
            )
        )
    ),

    Ukrainian(
        flagEmoji = "\uD83C\uDDFA\uD83C\uDDE6",
        languageNameRes = R.string.language_ukrainian,
        progress = "100%",
        translators = listOf(
            Translator(
                name = "Odnovolov Artem",
                link = null
            )
        )
    )
}

data class Translator(
    val name: String,
    val link: String?
)