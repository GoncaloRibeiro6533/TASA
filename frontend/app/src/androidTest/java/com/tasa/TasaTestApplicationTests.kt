package com.tasa

import androidx.test.platform.app.InstrumentationRegistry
import junit.framework.TestCase
import org.junit.Test

class TasaTestApplicationTests {
    @Test
    fun instrumented_tests_use_application_test_context() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        TestCase.assertEquals("com.tasa", context.packageName)
        assert(context.applicationContext is TasaTestApplication) {
            "Make sure the tests runner is correctly configured in build.gradle\n" +
                "defaultConfig { testInstrumentationRunner <test runner class name> }"
        }
    }

    @Test
    fun application_context_contains_dependencies() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        assert(context.applicationContext is DependenciesContainer) {
            "Make sure TASA implements DependenciesContainer"
        }
    }
}
