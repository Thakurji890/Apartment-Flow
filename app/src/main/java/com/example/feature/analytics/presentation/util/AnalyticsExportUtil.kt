package com.example.feature.analytics.presentation.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.feature.analytics.presentation.AnalyticsState
import java.io.File
import java.io.FileWriter

object AnalyticsExportUtil {

    fun exportToCsv(context: Context, state: AnalyticsState) {
        val fileName = "ApartmentFlow_Analytics_${state.selectedPeriod.name}.csv"
        val file = File(context.cacheDir, fileName)

        try {
            val writer = FileWriter(file)
            
            // Header
            writer.append("Analytics Report: ${state.selectedPeriod.displayName}\n\n")
            
            // Summary
            writer.append("--- SUMMARY ---\n")
            writer.append("Total Spending,${state.summary.totalSpending}\n")
            writer.append("Average Expense,${state.summary.averageExpense}\n")
            writer.append("Highest Expense,${state.summary.highestExpense}\n")
            writer.append("Total Settled,${state.summary.totalSettled}\n")
            writer.append("Expense Count,${state.summary.expenseCount}\n\n")
            
            // Categories
            writer.append("--- CATEGORIES ---\n")
            writer.append("Category,Amount,Percentage,Count\n")
            state.categorySpending.forEach {
                writer.append("${it.categoryId},${it.amount},${it.percentage}%,${it.count}\n")
            }
            writer.append("\n")
            
            // Members
            writer.append("--- MEMBERS ---\n")
            writer.append("Member,Amount Paid,Actual Share,Net Balance,Expense Count\n")
            state.memberSpending.forEach {
                writer.append("${it.displayName},${it.amountPaid},${it.actualShare},${it.netBalance},${it.expenseCount}\n")
            }
            
            writer.flush()
            writer.close()
            
            // Share Intent
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_SUBJECT, "ApartmentFlow Analytics")
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            
            context.startActivity(Intent.createChooser(intent, "Share Analytics CSV"))
            
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
