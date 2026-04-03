package com.uchi.mascostos.ui.desktop.ui

import javafx.util.StringConverter

class DoubleStringConverter : StringConverter<Double>() {

    override fun toString(value: Double?): String {
        if (value == null) return ""
        return if (value == value.toLong().toDouble()) {
            value.toLong().toString()
        } else {
            value.toString()
        }
    }

    override fun fromString(text: String?): Double {
        val value = text?.trim().orEmpty()
        if (value.isBlank()) return 0.0
        return value.replace(",", ".").toDoubleOrNull() ?: 0.0
    }
}