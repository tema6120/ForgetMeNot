package com.odnovolov.forgetmenot.presentation.screen.about

import com.odnovolov.forgetmenot.R

enum class Translation(
    val flagEmoji: String,
    val languageNameRes: Int,
    val progress: String,
    val translators: List<Translator>
) {
    Basque(
        flagEmoji = "\uD83C\uDFF4\uDB40\uDC65\uDB40\uDC73\uDB40\uDC70\uDB40\uDC76\uDB40\uDC7F",
        languageNameRes = R.string.language_basque,
        progress = "99%",
        translators = listOf(
            Translator(
                name = "soplatnik",
                link = "https://crowdin.com/profile/soplatnik"
            ),
            Translator(
                name = "avtkal",
                link = "https://crowdin.com/profile/avtkal"
            )
        )
    ),

    ChineseSimplified(
        flagEmoji = "\uD83C\uDDE8\uD83C\uDDF3",
        languageNameRes = R.string.language_chinese_simplified,
        progress = "11%",
        translators = listOf(
            Translator(
                name = "crown538",
                link = "https://crowdin.com/profile/crown538"
            )
        )
    ),

    Dutch(
        flagEmoji = "\uD83C\uDDF3\uD83C\uDDF1",
        languageNameRes = R.string.language_dutch,
        progress = "4%",
        translators = listOf(
            Translator(
                name = "patilla",
                link = "https://crowdin.com/profile/patilla"
            ),
            Translator(
                name = "NLvertaler",
                link = "https://crowdin.com/profile/nlvertaler"
            )
        )
    ),

    FRENCH(
        flagEmoji = "\uD83C\uDDEB\uD83C\uDDF7",
        languageNameRes = R.string.language_french,
        progress = "99%",
        translators = listOf(
            Translator(
                name = "Mr B",
                link = "https://crowdin.com/profile/mrb7"
            ),
            Translator(
                name = "PhirosWolf",
                link = "https://crowdin.com/profile/phiroswolf"
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
                name = "eiryelio",
                link = "https://crowdin.com/profile/eiryelio"
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
        progress = "87%",
        translators = listOf(
            Translator(
                name = "avtkal",
                link = "https://crowdin.com/profile/avtkal"
            ),
            Translator(
                name = "Tsef",
                link = "https://crowdin.com/profile/tsef"
            ),
            Translator(
                name = "Zonrad_Kuse",
                link = "https://crowdin.com/profile/zonrad_kuse"
            ),
            Translator(
                name = "statoquant",
                link = "https://crowdin.com/profile/statoquant"
            ),
            Translator(
                name = "B14CK313",
                link = "https://crowdin.com/profile/b14ck313"
            ),
            Translator(
                name = "trailingstock",
                link = "https://crowdin.com/profile/trailingstock"
            ),
            Translator(
                name = "Yushin Washio",
                link = "https://crowdin.com/profile/yuwash"
            ),
            Translator(
                name = "BlueFox4",
                link = "https://crowdin.com/profile/bluefox4"
            ),
            Translator(
                name = "baschi29",
                link = "https://crowdin.com/profile/baschi29"
            ),
            Translator(
                name = "park",
                link = "https://crowdin.com/profile/thebugmenot"
            ),
            Translator(
                name = "Unwoven",
                link = "https://crowdin.com/profile/unwovencrestless"
            ),
            Translator(
                name = "Nausika",
                link = "https://crowdin.com/profile/nausika"
            )
        )
    ),

    Greek(
        flagEmoji = "\uD83C\uDDEC\uD83C\uDDF7",
        languageNameRes = R.string.language_greek,
        progress = "6%",
        translators = listOf(
            Translator(
                name = "Vasilis Ioannidis",
                link = "https://crowdin.com/profile/vioannidis"
            )
        )
    ),

    Hindi(
        flagEmoji = "\uD83C\uDDEE\uD83C\uDDF3",
        languageNameRes = R.string.language_hindi,
        progress = "3%",
        translators = listOf(
            Translator(
                name = "Swarnendu Maiti",
                link = "https://crowdin.com/profile/swarnendu"
            )
        )
    ),

    Indonesian(
        flagEmoji = "\uD83C\uDDEE\uD83C\uDDE9",
        languageNameRes = R.string.language_indonesian,
        progress = "14%",
        translators = listOf(
            Translator(
                name = "liimee",
                link = "https://crowdin.com/profile/liimee"
            )
        )
    ),

    Polish(
        flagEmoji = "\uD83C\uDDF5\uD83C\uDDF1",
        languageNameRes = R.string.language_polish,
        progress = "34%",
        translators = listOf(
            Translator(
                name = "Waldemar Stoczkowski",
                link = "https://crowdin.com/profile/waldist"
            ),
            Translator(
                name = "F_I",
                link = "https://crowdin.com/profile/f_i"
            ),
            Translator(
                name = "ggngnn",
                link = "https://crowdin.com/profile/prubart"
            )
        )
    ),

    Portuguese(
        flagEmoji = "\uD83C\uDDF5\uD83C\uDDF9",
        languageNameRes = R.string.language_portuguese,
        progress = "21%",
        translators = listOf(
            Translator(
                name = "Hierotsu",
                link = "https://crowdin.com/profile/hierotsu"
            )
        )
    ),

    PortugueseBrazilian(
        flagEmoji = "\uD83C\uDDE7\uD83C\uDDF7",
        languageNameRes = R.string.language_portuguese_brazilian,
        progress = "31%",
        translators = listOf(
            Translator(
                name = "Marcos Maciel",
                link = "https://crowdin.com/profile/marcos_maciel_lima"
            ),
            Translator(
                name = "Lucas França",
                link = "https://crowdin.com/profile/lucasnomad5g"
            ),
            Translator(
                name = "aleguiss",
                link = "https://crowdin.com/profile/aleguiss"
            ),
            Translator(
                name = "dropdan",
                link = "https://crowdin.com/profile/dropdan"
            )
        )
    ),

    Russian(
        flagEmoji = "",
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
                name = "avtkal",
                link = "https://crowdin.com/profile/avtkal"
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

    Turkish(
        flagEmoji = "\uD83C\uDDF9\uD83C\uDDF7",
        languageNameRes = R.string.language_turkish,
        progress = "3%",
        translators = listOf(
            Translator(
                name = "TolDYuThad",
                link = "https://crowdin.com/profile/toldyuthad"
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
    ),

    Vietnamese(
        flagEmoji = "\uD83C\uDDFB\uD83C\uDDF3",
        languageNameRes = R.string.language_vietnamese,
        progress = "57%",
        translators = listOf(
            Translator(
                name = "bruhwut",
                link = "https://crowdin.com/profile/bruhwut"
            )
        )
    )
}

data class Translator(
    val name: String,
    val link: String?
)