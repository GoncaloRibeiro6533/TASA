package com.tasa.utils

import android.content.Context

interface StringResourceResolver {
    /**
     * Resolves a string resource by its ID.
     *
     * @param resId The resource ID of the string to resolve.
     * @return The resolved string.
     */
    fun getString(resId: Int): String
}

class DefaultStringResourceResolver(
    private val context: Context,
) : StringResourceResolver {
    override fun getString(resId: Int): String {
        return context.getString(resId)
    }
}
