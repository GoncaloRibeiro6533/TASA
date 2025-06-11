package com.tasa.domain

data class Language(val code: String, val name: String) {
    companion object {
        val ENGLISH = Language("en", "English")
        val SPANISH = Language("es", "Spanish")
        val FRENCH = Language("fr", "French")
        val GERMAN = Language("de", "German")
        val ITALIAN = Language("it", "Italian")

        val ALL_LANGUAGES = listOf(ENGLISH, SPANISH, FRENCH, GERMAN, ITALIAN)
    }
}
