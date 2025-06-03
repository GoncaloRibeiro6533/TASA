package com.tasa.domain

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
enum class Action(val value: String) : Parcelable {
    MUTE("mute"),
    UNMUTE("unmute"),
}
