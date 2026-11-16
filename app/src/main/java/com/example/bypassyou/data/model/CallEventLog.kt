package com.example.bypassyou.data.model

data class CallEventLog(
    val id: String,
    val timestamp: Long,
    val phoneNumber: String,
    val contactName: String,
    val isBypassed: Boolean,
    val reason: String,
    val volumePercent: Int
)
