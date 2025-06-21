package com.tasa.utils

import android.content.Context
import java.util.Properties

object PropertiesConfigLoader {
    /**
     * Loads properties from the assets/config.properties file.
     *
     * @param context The application context.
     * @return A Properties object containing the loaded properties.
     */
    fun load(context: Context): Properties {
        val props = Properties()
        context.assets.open("config.properties").use {
            props.load(it)
        }
        return props
    }
}
