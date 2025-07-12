package com.tasa.utils

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
import com.tasa.TasaTestApplication

@Suppress("unused")
class TasaInstrumentationTestRunner : AndroidJUnitRunner() {
    override fun newApplication(
        cl: ClassLoader?,
        className: String?,
        context: Context?,
    ): Application {
        return super.newApplication(cl, TasaTestApplication::class.java.name, context)
    }
}
