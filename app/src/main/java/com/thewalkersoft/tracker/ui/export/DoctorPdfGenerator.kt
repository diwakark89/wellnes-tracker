package com.thewalkersoft.tracker.ui.export

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.thewalkersoft.tracker.data.local.entity.SymptomLogEntity
import com.thewalkersoft.tracker.domain.model.CycleRecord
import com.thewalkersoft.tracker.domain.model.PredictionResult
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class DoctorPdfGenerator(private val context: Context) {

    fun generateClinicalReport(
        cycles: List<CycleRecord>,
        symptoms: List<SymptomLogEntity>,
        prediction: PredictionResult?
    ): File {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // Standard A4 (points)
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        val titlePaint = Paint().apply {
            color = Color.rgb(180, 40, 75)
            textSize = 18f
            isFakeBoldText = true
            isAntiAlias = true
        }

        val subTitlePaint = Paint().apply {
            color = Color.DKGRAY
            textSize = 10f
            isAntiAlias = true
        }

        val headerPaint = Paint().apply {
            color = Color.rgb(60, 60, 60)
            textSize = 12f
            isFakeBoldText = true
            isAntiAlias = true
        }

        val bodyPaint = Paint().apply {
            color = Color.BLACK
            textSize = 10f
            isAntiAlias = true
        }

        val smallPaint = Paint().apply {
            color = Color.GRAY
            textSize = 8f
            isAntiAlias = true
        }

        val linePaint = Paint().apply {
            color = Color.LTGRAY
            strokeWidth = 1f
        }

        val tableHeaderPaint = Paint().apply {
            color = Color.rgb(240, 240, 245)
            style = Paint.Style.FILL
        }

        var y = 45f
        val margin = 40f
        val rightMargin = 555f
        val dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")

        // 1. Report Title & Privacy Note
        canvas.drawText("MENSTRUAL CYCLE & HEALTH REPORT", margin, y, titlePaint)
        y += 15f
        canvas.drawText("Confidential Medical Summary • Generated on ${LocalDate.now().format(dateFormatter)}", margin, y, subTitlePaint)
        y += 8f
        canvas.drawLine(margin, y, rightMargin, y, linePaint)
        y += 20f

        // 2. Executive Analytics Summary
        canvas.drawText("1. STATISTICAL CYCLE SUMMARY", margin, y, headerPaint)
        y += 15f

        val completedCycles = cycles.drop(1)
        val avgLength = if (completedCycles.isNotEmpty()) "${completedCycles.map { it.cycleLengthDays }.average().toInt()} days" else "N/A"
        val minLength = if (completedCycles.isNotEmpty()) "${completedCycles.minOf { it.cycleLengthDays }} days" else "N/A"
        val maxLength = if (completedCycles.isNotEmpty()) "${completedCycles.maxOf { it.cycleLengthDays }} days" else "N/A"

        canvas.drawText("• Total Cycles Recorded: ${cycles.size}", margin + 10, y, bodyPaint)
        canvas.drawText("• Average Cycle Duration: $avgLength", margin + 260, y, bodyPaint)
        y += 14f
        canvas.drawText("• Shortest Cycle: $minLength", margin + 10, y, bodyPaint)
        canvas.drawText("• Longest Cycle: $maxLength", margin + 260, y, bodyPaint)
        y += 14f

        if (prediction != null) {
            val predText = "• Current Adaptive Prediction: Peak on ${prediction.targetPeakDate.format(dateFormatter)} (${prediction.earliestLikelyDate.format(dateFormatter)} - ${prediction.latestLikelyDate.format(dateFormatter)}, ±${prediction.meanAbsoluteError}d MAE buffer)"
            canvas.drawText(predText, margin + 10, y, bodyPaint)
            y += 14f
        }

        y += 10f
        canvas.drawLine(margin, y, rightMargin, y, linePaint)
        y += 20f

        // 3. Cycle History Table
        canvas.drawText("2. RECORDED CYCLE HISTORY", margin, y, headerPaint)
        y += 12f

        // Table Header background
        canvas.drawRect(margin, y, rightMargin, y + 18f, tableHeaderPaint)
        y += 13f
        canvas.drawText("Start Date", margin + 8, y, headerPaint.apply { textSize = 9f })
        canvas.drawText("End Date", margin + 110, y, headerPaint)
        canvas.drawText("Gap (Days)", margin + 210, y, headerPaint)
        canvas.drawText("Flow", margin + 300, y, headerPaint)
        canvas.drawText("Baseline Reset", margin + 390, y, headerPaint)
        y += 10f

        val sortedCycles = cycles.sortedByDescending { it.startDate }.take(12)
        for (cycle in sortedCycles) {
            val endStr = cycle.endDate?.format(dateFormatter) ?: "Ongoing"
            canvas.drawText(cycle.startDate.format(dateFormatter), margin + 8, y + 10, bodyPaint)
            canvas.drawText(endStr, margin + 110, y + 10, bodyPaint)
            canvas.drawText("${cycle.cycleLengthDays} days", margin + 210, y + 10, bodyPaint)
            canvas.drawText(cycle.flowIntensity ?: "Standard", margin + 300, y + 10, bodyPaint)
            canvas.drawText(if (cycle.isPostpartumBaselineReset) "Yes (Postpartum)" else "No", margin + 390, y + 10, bodyPaint)

            y += 18f
            canvas.drawLine(margin, y, rightMargin, y, linePaint)
        }

        y += 15f

        // 4. Symptoms & Biomarkers Summary
        if (symptoms.isNotEmpty()) {
            canvas.drawText("3. RECENT RECORDED SYMPTOMS & BIOMARKERS", margin, y, headerPaint.apply { textSize = 12f })
            y += 12f

            canvas.drawRect(margin, y, rightMargin, y + 18f, tableHeaderPaint)
            y += 13f
            canvas.drawText("Date", margin + 8, y, headerPaint.apply { textSize = 9f })
            canvas.drawText("BBT (°C/°F)", margin + 110, y, headerPaint)
            canvas.drawText("Cramps (1-5)", margin + 210, y, headerPaint)
            canvas.drawText("Mood", margin + 300, y, headerPaint)
            canvas.drawText("LH Ovulation", margin + 390, y, headerPaint)
            y += 10f

            val recentSymptoms = symptoms.sortedByDescending { it.logDate }.take(8)
            for (sym in recentSymptoms) {
                val bbtStr = sym.basalBodyTemp?.toString() ?: "--"
                val crampStr = sym.crampsSeverity?.let { "$it / 5" } ?: "--"
                val moodStr = sym.mood ?: "--"
                val ovStr = sym.ovulationTestResult ?: "--"

                canvas.drawText(sym.logDate.format(dateFormatter), margin + 8, y + 10, bodyPaint)
                canvas.drawText(bbtStr, margin + 110, y + 10, bodyPaint)
                canvas.drawText(crampStr, margin + 210, y + 10, bodyPaint)
                canvas.drawText(moodStr, margin + 300, y + 10, bodyPaint)
                canvas.drawText(ovStr, margin + 390, y + 10, bodyPaint)

                y += 18f
                canvas.drawLine(margin, y, rightMargin, y, linePaint)
            }
        }

        // 5. Clinical Footer
        y = 800f
        canvas.drawLine(margin, y, rightMargin, y, linePaint)
        y += 15f
        canvas.drawText("Notice: This report is generated on-device for informational and clinical review. Privacy-first, zero telemetry.", margin, y, smallPaint)

        pdfDocument.finishPage(page)

        // Save PDF to cache/reports directory
        val reportsDir = File(context.cacheDir, "reports")
        if (!reportsDir.exists()) reportsDir.mkdirs()

        val reportFile = File(reportsDir, "Cycle_Health_Report_${System.currentTimeMillis()}.pdf")
        FileOutputStream(reportFile).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()

        return reportFile
    }
}
