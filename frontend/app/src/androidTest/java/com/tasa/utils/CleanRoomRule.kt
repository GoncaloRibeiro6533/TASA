package com.tasa.utils

import androidx.test.platform.app.InstrumentationRegistry
import com.tasa.DependenciesContainer
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

/**
 * A JUnit rule that clears the database before each test.
 */
class CleanRoomRule : TestRule {
    val db by lazy {
        val dependenciesContainer =
            InstrumentationRegistry
                .getInstrumentation()
                .targetContext
                .applicationContext as DependenciesContainer
        dependenciesContainer.clientDB
    }

    override fun apply(
        base: Statement,
        description: Description,
    ): Statement {
        return object : Statement() {
            override fun evaluate() {
                db.clearAllTables()
                base.evaluate()
            }
        }
    }
}
