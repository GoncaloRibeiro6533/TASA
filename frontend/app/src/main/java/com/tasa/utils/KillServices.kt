package com.tasa.utils

import android.content.Context
import android.content.Intent
import kotlin.jvm.java
import kotlin.reflect.KClass

interface ServiceKiller {
    fun killServices(ref: KClass<*>)
}

class ServiceKillerImpl(
    private val context: Context,
) : ServiceKiller {
    override fun killServices(ref: KClass<*>) {
        val serviceIntent = Intent(context, ref.java)
        context.stopService(serviceIntent)
    }
}
