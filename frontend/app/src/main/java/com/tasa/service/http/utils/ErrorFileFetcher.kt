package com.tasa.service.http.utils

import java.net.URI

fun fetchErrorFile(url: URI): String = url.toString().split("/").last().split("-").joinToString(" ").replaceFirstChar { it.uppercase() }
