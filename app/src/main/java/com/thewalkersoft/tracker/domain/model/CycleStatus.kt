package com.thewalkersoft.tracker.domain.model

enum class CycleStatus(val displayName: String, val description: String) {
    FOLLICULAR("Follicular Phase", "Low chance of conception, body is preparing egg"),
    OVULATION_WINDOW("Fertile Window", "High likelihood of ovulation and conception"),
    LUTEAL("Luteal Phase", "Post-ovulation phase leading up to expected period"),
    PREDICTION_WINDOW_ACTIVE("Due Soon / Window Active", "Period is expected within the predicted arrival window"),
    OVERDUE("Cycle Overdue", "Period has exceeded the calculated confidence window")
}
