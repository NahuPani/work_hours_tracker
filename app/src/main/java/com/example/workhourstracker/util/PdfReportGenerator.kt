package com.example.workhourstracker.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.example.workhourstracker.data.WorkShift
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfReportGenerator {

    fun generateReportPdf(
        context: Context,
        shifts: List<WorkShift>,
        periodTitle: String,
        currencySymbol: String
    ): Result<File> {
        return try {
            val pdfDocument = PdfDocument()
            val pageWidth = 595 // A4 standard width in points
            val pageHeight = 842 // A4 standard height in points

            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas: Canvas = page.canvas

            val paint = Paint()
            val titlePaint = Paint().apply {
                isAntiAlias = true
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textSize = 20f
                color = Color.rgb(15, 23, 42) // Dark Slate
            }
            val subtitlePaint = Paint().apply {
                isAntiAlias = true
                typeface = Typeface.DEFAULT
                textSize = 10f
                color = Color.rgb(100, 116, 139) // Slate Gray
            }
            val boldPaint = Paint().apply {
                isAntiAlias = true
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textSize = 10f
                color = Color.rgb(15, 23, 42)
            }
            val regularPaint = Paint().apply {
                isAntiAlias = true
                typeface = Typeface.DEFAULT
                textSize = 9f
                color = Color.rgb(51, 65, 85)
            }
            val moneyPaint = Paint().apply {
                isAntiAlias = true
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textSize = 9.5f
                color = Color.rgb(5, 150, 105) // Emerald Green
            }

            // 1. Top Decorative Brand Bar (Emerald)
            paint.color = Color.rgb(16, 185, 129)
            canvas.drawRect(0f, 0f, pageWidth.toFloat(), 8f, paint)

            var y = 40f
            // 2. Header
            canvas.drawText("Mi Turno", 40f, y, titlePaint)
            paint.color = Color.rgb(16, 185, 129)
            canvas.drawCircle(pageWidth - 50f, y - 6f, 12f, paint)
            boldPaint.color = Color.WHITE
            canvas.drawText("MT", pageWidth - 57f, y - 2f, boldPaint)
            boldPaint.color = Color.rgb(15, 23, 42)

            y += 16f
            canvas.drawText("REPORTE OFICIAL DE JORNADAS LABORALES", 40f, y, subtitlePaint)
            y += 14f
            val dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
            canvas.drawText("Período: $periodTitle  •  Emitido: $dateStr", 40f, y, subtitlePaint)

            y += 18f
            // 3. Summary Box
            val totalHours = shifts.sumOf { it.durationHours }
            val totalRegHours = shifts.sumOf { it.regularHours }
            val totalExtraHours = shifts.sumOf { it.overtimeHours }
            val totalEarnings = shifts.sumOf { it.totalEarnings }

            paint.color = Color.rgb(241, 245, 249) // Soft Slate Background
            canvas.drawRoundRect(40f, y, pageWidth - 40f, y + 60f, 10f, 10f, paint)
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1f
            paint.color = Color.rgb(203, 213, 225)
            canvas.drawRoundRect(40f, y, pageWidth - 40f, y + 60f, 10f, 10f, paint)
            paint.style = Paint.Style.FILL

            val boxY = y + 24f
            canvas.drawText("HORAS TOTALES", 60f, boxY, subtitlePaint)
            canvas.drawText("${String.format(Locale.US, "%.1f", totalHours)}h", 60f, boxY + 20f, titlePaint.apply { textSize = 16f })
            titlePaint.textSize = 20f

            canvas.drawText("NORMALES / EXTRAS", 200f, boxY, subtitlePaint)
            canvas.drawText("${String.format(Locale.US, "%.1f", totalRegHours)}h / ${String.format(Locale.US, "%.1f", totalExtraHours)}h", 200f, boxY + 20f, boldPaint.apply { textSize = 13f })
            boldPaint.textSize = 10f

            canvas.drawText("TOTAL A LIQUIDAR", 380f, boxY, subtitlePaint)
            moneyPaint.textSize = 16f
            canvas.drawText("$currencySymbol${String.format(Locale.getDefault(), "%,.2f", totalEarnings)}", 380f, boxY + 20f, moneyPaint)
            moneyPaint.textSize = 9.5f

            y += 85f

            // 4. Shifts Table
            // Table Header Bar
            paint.color = Color.rgb(15, 23, 42) // Dark Header
            canvas.drawRoundRect(40f, y, pageWidth - 40f, y + 24f, 6f, 6f, paint)
            boldPaint.color = Color.WHITE
            boldPaint.textSize = 9f

            val colXDate = 50f
            val colXTime = 160f
            val colXDuration = 250f
            val colXNormal = 320f
            val colXExtra = 385f
            val colXTotal = 450f

            canvas.drawText("FECHA", colXDate, y + 15f, boldPaint)
            canvas.drawText("HORARIO", colXTime, y + 15f, boldPaint)
            canvas.drawText("DURACIÓN", colXDuration, y + 15f, boldPaint)
            canvas.drawText("NORMALES", colXNormal, y + 15f, boldPaint)
            canvas.drawText("EXTRAS", colXExtra, y + 15f, boldPaint)
            canvas.drawText("SUBTOTAL", colXTotal, y + 15f, boldPaint)

            y += 24f

            // Table Rows
            boldPaint.color = Color.rgb(15, 23, 42)
            var rowIndex = 0

            for (shift in shifts.take(24)) { // Up to 24 shifts per single sheet
                rowIndex++
                val rowHeight = 22f

                if (rowIndex % 2 == 1) {
                    paint.color = Color.rgb(248, 250, 252) // Zebra stripe
                    canvas.drawRect(40f, y, pageWidth - 40f, y + rowHeight, paint)
                }

                val rowTextY = y + 15f
                canvas.drawText(shift.formattedShortDate(), colXDate, rowTextY, regularPaint)
                canvas.drawText(shift.formattedTimeRange(), colXTime, rowTextY, regularPaint)
                canvas.drawText(shift.formattedDuration(), colXDuration, rowTextY, regularPaint)
                canvas.drawText("${String.format(Locale.US, "%.1f", shift.regularHours)}h", colXNormal, rowTextY, regularPaint)

                if (shift.overtimeHours > 0.0) {
                    paint.color = Color.rgb(245, 158, 11)
                    canvas.drawText("+${String.format(Locale.US, "%.1f", shift.overtimeHours)}h", colXExtra, rowTextY, boldPaint.apply { color = Color.rgb(217, 119, 6) })
                    boldPaint.color = Color.rgb(15, 23, 42)
                } else {
                    canvas.drawText("-", colXExtra, rowTextY, regularPaint)
                }

                val shiftEarnings = "$currencySymbol${String.format(Locale.getDefault(), "%,.2f", shift.totalEarnings)}"
                canvas.drawText(shiftEarnings, colXTotal, rowTextY, moneyPaint)

                y += rowHeight
            }

            // 5. Signature Footer
            y = pageHeight - 90f
            paint.color = Color.rgb(203, 213, 225)
            paint.strokeWidth = 1f
            canvas.drawLine(40f, y, pageWidth - 40f, y, paint)

            y += 25f
            canvas.drawText("Comprobante generado automáticamente por la aplicación Mi Turno.", 40f, y, subtitlePaint)
            canvas.drawText("Firma del Supervisor: _______________________________", 300f, y, subtitlePaint)

            pdfDocument.finishPage(page)

            val fileName = "Reporte_Mi_Turno_${periodTitle.replace(" ", "_").replace("(", "").replace(")", "")}.pdf"
            val file = File(context.cacheDir, fileName)
            val outputStream = FileOutputStream(file)
            pdfDocument.writeTo(outputStream)
            outputStream.close()
            pdfDocument.close()

            Result.success(file)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
