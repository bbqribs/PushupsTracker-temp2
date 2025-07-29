// File: app/src/main/java/com/github/bbqribs/pushupstracker/model/PushupPlanEntry.kt
package com.github.bbqribs.pushupstracker.model

/**
 * Represents one session entry from the CSV:
 * - week/day/column identify it
 * - sets is the raw list of strings (e.g. ["10","12","MAX≥15"])
 * - rest is the RecommendedRest string (e.g. "60-90s", "90s+")
 */
data class PushupPlanEntry(
    val week: Int,
    val day: Int,
    val column: String, // ✅ CORRECTED: Changed from Int to String
    val sets: List<String>,
    val rest: String
)