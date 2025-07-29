// File: app/src/main/java/com/github/bbqribs/pushupstracker/repository/PlanRepository.kt
package com.github.bbqribs.pushupstracker.repository

import android.content.Context
import com.github.bbqribs.pushupstracker.model.PushupPlanEntry
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton // 1. Tells Hilt to create only one instance of this class
class PlanRepository @Inject constructor(@ApplicationContext private val context: Context) {
    // 2. The @Inject constructor tells Hilt how to create this class.
    //    It automatically receives the application context.

    // 3. The plan is loaded once using a lazy delegate and cached.
    val plan: List<PushupPlanEntry> by lazy { loadPlan() }

    fun findPlanEntry(week: Int, day: Int, column: String): PushupPlanEntry? {
        return plan.find { it.week == week && it.day == day && it.column == column }
    }

    private fun loadPlan(): List<PushupPlanEntry> {
        // 4. This method now uses the context provided by Hilt, no parameter needed.
        val entries = mutableListOf<PushupPlanEntry>()
        val resId = context.resources.getIdentifier(
            "hundred_pushups_plan", "raw", context.packageName
        )
        context.resources.openRawResource(resId)
            .bufferedReader()
            .useLines { lines ->
                lines.drop(1)
                    .filter { it.isNotBlank() }
                    .forEach { line ->
                        val cols = line.split(",").map(String::trim)
                        if (cols.size >= 5) {
                            val week = cols[0].toInt()
                            val day = cols[1].toInt()
                            val column = cols[2]
                            val sets = cols.subList(3, cols.size - 1)
                                .filter { it.isNotEmpty() }
                            val rest = cols.last()
                            entries.add(PushupPlanEntry(week, day, column, sets, rest))
                        }
                    }
            }
        return entries
    }
}