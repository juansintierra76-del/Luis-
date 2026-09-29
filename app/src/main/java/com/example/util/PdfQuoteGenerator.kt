package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.models.CurrencyMode
import com.example.data.models.School
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

data class QuoteDetails(
    val studentName: String = "Juan Carlos Mendoza",
    val studentEmail: String = "juansintierra76@gmail.com",
    val studentPhone: String = "+51 987 654 321",
    val studentRegion: String = "Lima, Perú",
    val programName: String = "General English Intensive",
    val weeks: Int = 12,
    val includeAccommodation: Boolean = true,
    val includeInsurance: Boolean = true,
    val discountPercent: Double = 0.10 // 10% promo descuento LatAm
)

object PdfQuoteGenerator {

    private const val EXCHANGE_RATE = CurrencyMode.USD_TO_PEN_RATE

    data class PdfGenerationResult(
        val file: File,
        val uri: Uri,
        val totalUsd: Double,
        val totalPen: Double,
        val quoteRef: String
    )

    fun generateQuotePdf(
        context: Context,
        school: School,
        details: QuoteDetails
    ): Result<PdfGenerationResult> {
        return try {
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 (595 x 842 pt)
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            // Colors
            val navyDark = Color.rgb(10, 25, 47)
            val navyPrimary = Color.rgb(30, 58, 138)
            val gold = Color.rgb(217, 119, 6)
            val textDark = Color.rgb(30, 41, 59)
            val textMuted = Color.rgb(100, 116, 139)
            val emerald = Color.rgb(5, 150, 105)
            val peruRed = Color.rgb(220, 38, 38)
            val lightBg = Color.rgb(241, 245, 249)
            val white = Color.WHITE

            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = textDark
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            }

            val nf = NumberFormat.getNumberInstance(Locale.US).apply {
                maximumFractionDigits = 2
                minimumFractionDigits = 2
            }

            val quoteRef = "COT-2026-PER-" + UUID.randomUUID().toString().take(6).uppercase()
            val currentDateStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())

            // 1. TOP HEADER BANNER
            paint.color = navyDark
            canvas.drawRect(0f, 0f, 595f, 95f, paint)

            paint.color = gold
            canvas.drawRect(0f, 95f, 595f, 98f, paint)

            // Header Title
            textPaint.color = white
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textPaint.textSize = 20f
            canvas.drawText("English4everyone 🇵🇪", 36f, 38f, textPaint)

            textPaint.textSize = 10f
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textPaint.color = Color.rgb(226, 232, 240)
            canvas.drawText("Plataforma Oficial de Prematrícula y Asesoría para Estudiantes Peruanos", 36f, 54f, textPaint)

            textPaint.color = gold
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textPaint.textSize = 11f
            canvas.drawText("PRESUPUESTO ACADÉMICO OFICIAL 2026", 36f, 75f, textPaint)

            // Right header: Folio & Date
            textPaint.color = white
            textPaint.textSize = 9f
            textPaint.textAlign = Paint.Align.RIGHT
            canvas.drawText("Ref: $quoteRef", 559f, 35f, textPaint)
            canvas.drawText("Fecha: $currentDateStr", 559f, 50f, textPaint)
            canvas.drawText("Tipo de Cambio: S/. $EXCHANGE_RATE PEN/USD", 559f, 65f, textPaint)
            textPaint.textAlign = Paint.Align.LEFT

            // 2. STUDENT & SCHOOL INFO BOXES (Two Columns)
            var currentY = 118f

            // Box Left: Estudiante Peruano
            paint.color = lightBg
            canvas.drawRoundRect(RectF(36f, currentY, 290f, currentY + 90f), 8f, 8f, paint)

            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1f
            paint.color = Color.rgb(203, 213, 225)
            canvas.drawRoundRect(RectF(36f, currentY, 290f, currentY + 90f), 8f, 8f, paint)
            paint.style = Paint.Style.FILL

            textPaint.color = navyPrimary
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textPaint.textSize = 11f
            canvas.drawText("DATOS DEL ESTUDIANTE", 46f, currentY + 20f, textPaint)

            textPaint.color = textDark
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textPaint.textSize = 10f
            canvas.drawText("Nombre: ${details.studentName}", 46f, currentY + 36f, textPaint)
            canvas.drawText("Email: ${details.studentEmail}", 46f, currentY + 50f, textPaint)
            canvas.drawText("Teléfono: ${details.studentPhone}", 46f, currentY + 64f, textPaint)
            canvas.drawText("Ciudad: ${details.studentRegion}", 46f, currentY + 78f, textPaint)

            // Box Right: Institución & Representante
            paint.color = lightBg
            canvas.drawRoundRect(RectF(305f, currentY, 559f, currentY + 90f), 8f, 8f, paint)

            paint.style = Paint.Style.STROKE
            paint.color = Color.rgb(203, 213, 225)
            canvas.drawRoundRect(RectF(305f, currentY, 559f, currentY + 90f), 8f, 8f, paint)
            paint.style = Paint.Style.FILL

            textPaint.color = navyPrimary
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textPaint.textSize = 11f
            canvas.drawText("ESCUELA Y REPRESENTANTE OFICIAL", 315f, currentY + 20f, textPaint)

            textPaint.color = textDark
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textPaint.textSize = 10f
            canvas.drawText("Escuela: ${school.name}", 315f, currentY + 36f, textPaint)
            canvas.drawText("Sede: ${school.city}, ${school.country} ${school.flagEmoji}", 315f, currentY + 50f, textPaint)
            canvas.drawText("Representante: ${school.representativeName}", 315f, currentY + 64f, textPaint)
            canvas.drawText("Acreditación: ${school.accreditation.take(38)}", 315f, currentY + 78f, textPaint)

            // 3. PROGRAM SUMMARY BANNER
            currentY += 105f
            paint.color = Color.rgb(239, 246, 255)
            canvas.drawRoundRect(RectF(36f, currentY, 559f, currentY + 32f), 6f, 6f, paint)

            textPaint.color = navyDark
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textPaint.textSize = 10.5f
            canvas.drawText("Programa: ${details.programName} • Duración: ${details.weeks} Semanas (${details.weeks * 24} hrs lectivas)", 48f, currentY + 20f, textPaint)

            // 4. COST BREAKDOWN TABLE
            currentY += 46f

            // Table Header Bar
            paint.color = navyPrimary
            canvas.drawRoundRect(RectF(36f, currentY, 559f, currentY + 24f), 4f, 4f, paint)

            textPaint.color = white
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textPaint.textSize = 10f
            canvas.drawText("Concepto / Servicio Académico", 46f, currentY + 16f, textPaint)

            textPaint.textAlign = Paint.Align.RIGHT
            canvas.drawText("Cant.", 370f, currentY + 16f, textPaint)
            canvas.drawText("USD ($)", 460f, currentY + 16f, textPaint)
            canvas.drawText("PEN (S/.)", 545f, currentY + 16f, textPaint)
            textPaint.textAlign = Paint.Align.LEFT

            currentY += 26f

            // Row calculation data
            data class QuoteRow(val title: String, val qty: String, val usd: Double)

            val tuitionUsd = details.weeks * school.priceWeeklyUsd
            val enrollmentFeeUsd = 150.0
            val materialsFeeUsd = 80.0
            val accommodationUsd = if (details.includeAccommodation) details.weeks * 220.0 else 0.0
            val insuranceUsd = if (details.includeInsurance) 130.0 else 0.0

            val rows = mutableListOf<QuoteRow>()
            rows.add(QuoteRow("Colegiatura (${school.name})", "${details.weeks} sem", tuitionUsd))
            rows.add(QuoteRow("Tarifa de Inscripción y Matrícula Oficial", "1 pago", enrollmentFeeUsd))
            rows.add(QuoteRow("Material Didáctico y Plataforma Digital", "1 pack", materialsFeeUsd))

            if (details.includeAccommodation) {
                rows.add(QuoteRow("Alojamiento Homestay (Hab. indiv. + Media pensión)", "${details.weeks} sem", accommodationUsd))
            }
            if (details.includeInsurance) {
                rows.add(QuoteRow("Seguro Médico Estudiantil Obligatorio (OSHC/Guard.me)", "Póliza", insuranceUsd))
            }

            var subtotalUsd = tuitionUsd + enrollmentFeeUsd + materialsFeeUsd + accommodationUsd + insuranceUsd
            val discountUsd = if (details.weeks >= 12) subtotalUsd * details.discountPercent else 0.0
            val finalTotalUsd = subtotalUsd - discountUsd
            val finalTotalPen = finalTotalUsd * EXCHANGE_RATE

            // Draw Table Rows
            rows.forEachIndexed { index, row ->
                val rowHeight = 22f
                if (index % 2 == 1) {
                    paint.color = Color.rgb(248, 250, 252)
                    canvas.drawRect(36f, currentY, 559f, currentY + rowHeight, paint)
                }

                // Bottom border
                paint.color = Color.rgb(226, 232, 240)
                canvas.drawLine(36f, currentY + rowHeight, 559f, currentY + rowHeight, paint)

                textPaint.color = textDark
                textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                textPaint.textSize = 9.5f
                canvas.drawText(row.title, 46f, currentY + 15f, textPaint)

                textPaint.textAlign = Paint.Align.RIGHT
                canvas.drawText(row.qty, 370f, currentY + 15f, textPaint)
                canvas.drawText("$ " + nf.format(row.usd), 460f, currentY + 15f, textPaint)
                canvas.drawText("S/. " + nf.format(row.usd * EXCHANGE_RATE), 545f, currentY + 15f, textPaint)
                textPaint.textAlign = Paint.Align.LEFT

                currentY += rowHeight
            }

            // Subtotal Row
            currentY += 4f
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textPaint.color = textMuted
            textPaint.textSize = 9.5f
            canvas.drawText("Subtotal:", 280f, currentY + 14f, textPaint)

            textPaint.textAlign = Paint.Align.RIGHT
            canvas.drawText("$ " + nf.format(subtotalUsd), 460f, currentY + 14f, textPaint)
            canvas.drawText("S/. " + nf.format(subtotalUsd * EXCHANGE_RATE), 545f, currentY + 14f, textPaint)
            textPaint.textAlign = Paint.Align.LEFT
            currentY += 18f

            // Discount Row (if applicable)
            if (discountUsd > 0) {
                textPaint.color = emerald
                canvas.drawText("Descuento Promoción Estudiantes Perú (10%):", 200f, currentY + 14f, textPaint)
                textPaint.textAlign = Paint.Align.RIGHT
                canvas.drawText("- $ " + nf.format(discountUsd), 460f, currentY + 14f, textPaint)
                canvas.drawText("- S/. " + nf.format(discountUsd * EXCHANGE_RATE), 545f, currentY + 14f, textPaint)
                textPaint.textAlign = Paint.Align.LEFT
                currentY += 20f
            }

            // TOTAL BANNER (Dual Currency USD & PEN Highlighted)
            currentY += 6f
            paint.color = navyDark
            canvas.drawRoundRect(RectF(36f, currentY, 559f, currentY + 38f), 6f, 6f, paint)

            textPaint.color = white
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textPaint.textSize = 12f
            canvas.drawText("TOTAL OFICIAL ESTIMADO:", 46f, currentY + 24f, textPaint)

            textPaint.textAlign = Paint.Align.RIGHT
            textPaint.color = gold
            textPaint.textSize = 13f
            canvas.drawText("$ " + nf.format(finalTotalUsd) + " USD", 440f, currentY + 24f, textPaint)

            textPaint.color = Color.rgb(254, 202, 202) // Peru accent
            textPaint.textSize = 13f
            canvas.drawText("S/. " + nf.format(finalTotalPen) + " PEN", 545f, currentY + 24f, textPaint)
            textPaint.textAlign = Paint.Align.LEFT

            // 5. WORK & VISA BENEFIT BOX
            currentY += 50f
            paint.color = if (school.visaAllowsWork) Color.rgb(236, 253, 245) else Color.rgb(254, 243, 199)
            canvas.drawRoundRect(RectF(36f, currentY, 559f, currentY + 44f), 6f, 6f, paint)

            paint.style = Paint.Style.STROKE
            paint.color = if (school.visaAllowsWork) Color.rgb(167, 243, 208) else Color.rgb(253, 230, 138)
            canvas.drawRoundRect(RectF(36f, currentY, 559f, currentY + 44f), 6f, 6f, paint)
            paint.style = Paint.Style.FILL

            textPaint.color = if (school.visaAllowsWork) emerald else gold
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textPaint.textSize = 10f
            val visaNoteTitle = if (school.visaAllowsWork) "💼 PERMISO DE TRABAJO LEGAL AUTORIZADO (${school.country.uppercase()}):"
            else "🛂 INFORMACIÓN CONSULAR Y TRÁMITE DE VISA (${school.country.uppercase()}):"
            canvas.drawText(visaNoteTitle, 46f, currentY + 18f, textPaint)

            textPaint.color = textDark
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textPaint.textSize = 8.8f
            val visaDetail = if (school.visaAllowsWork) school.workDetails
            else "La escuela emite la Carta de Aceptación oficial (LOA/I-20) requerida para la postulación en la embajada en Lima."
            canvas.drawText(visaDetail.take(95), 46f, currentY + 32f, textPaint)

            // 6. OFFICIAL SIGNATURE & VALIDITY SECTION
            currentY += 58f

            // Left: Payment & Terms
            textPaint.color = textMuted
            textPaint.textSize = 8.5f
            canvas.drawText("CONDICIONES DEL PRESUPUESTO:", 36f, currentY + 12f, textPaint)
            canvas.drawText("• Validez de la cotización: 30 días calendario desde su emisión.", 36f, currentY + 24f, textPaint)
            canvas.drawText("• Pagos directos a la institución educativa en el exterior o transferencia autorizada.", 36f, currentY + 36f, textPaint)
            canvas.drawText("• Asesoría de visado y emisión de Carta de Aceptación (LOA/CoE) incluida sin costo adicional.", 36f, currentY + 48f, textPaint)

            // Right: Representative Signature Block
            paint.color = Color.rgb(226, 232, 240)
            canvas.drawLine(380f, currentY + 36f, 545f, currentY + 36f, paint)

            textPaint.color = navyDark
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textPaint.textSize = 9.5f
            textPaint.textAlign = Paint.Align.CENTER
            canvas.drawText(school.representativeName, 462f, currentY + 48f, textPaint)

            textPaint.color = textMuted
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textPaint.textSize = 8f
            canvas.drawText("${school.representativeRole}", 462f, currentY + 58f, textPaint)
            canvas.drawText("Oficina de Admisiones • ${school.city}", 462f, currentY + 68f, textPaint)
            textPaint.textAlign = Paint.Align.LEFT

            // 7. FOOTER
            paint.color = navyDark
            canvas.drawRect(0f, 810f, 595f, 842f, paint)

            textPaint.color = Color.rgb(226, 232, 240)
            textPaint.textSize = 8f
            textPaint.textAlign = Paint.Align.CENTER
            canvas.drawText("English4everyone • Lima, Perú • Consultas: juansintierra76@gmail.com • www.english4everyone.pe", 297f, 828f, textPaint)

            pdfDocument.finishPage(page)

            // Write PDF to External Documents directory
            val docsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) ?: context.filesDir
            if (!docsDir.exists()) docsDir.mkdirs()

            val sanitizedSchool = school.city.lowercase().replace(" ", "_")
            val fileName = "Presupuesto_${sanitizedSchool}_${details.weeks}sem_${System.currentTimeMillis()}.pdf"
            val pdfFile = File(docsDir, fileName)

            val outputStream = FileOutputStream(pdfFile)
            pdfDocument.writeTo(outputStream)
            outputStream.flush()
            outputStream.close()
            pdfDocument.close()

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                pdfFile
            )

            Result.success(
                PdfGenerationResult(
                    file = pdfFile,
                    uri = uri,
                    totalUsd = finalTotalUsd,
                    totalPen = finalTotalPen,
                    quoteRef = quoteRef
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    fun openPdf(context: Context, uri: Uri) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(Intent.createChooser(intent, "Abrir Presupuesto en PDF"))
        } catch (e: Exception) {
            Toast.makeText(context, "No se encontró un visor de PDF. El archivo se guardó en tus Documentos.", Toast.LENGTH_LONG).show()
        }
    }

    fun sharePdf(context: Context, uri: Uri, schoolName: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Cotización Oficial de Inglés - $schoolName")
            putExtra(Intent.EXTRA_TEXT, "Adjunto mi presupuesto oficial de estudios de inglés con desglose en USD y PEN para $schoolName.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        try {
            context.startActivity(Intent.createChooser(intent, "Compartir Presupuesto PDF"))
        } catch (e: Exception) {
            Toast.makeText(context, "Error al compartir archivo PDF.", Toast.LENGTH_SHORT).show()
        }
    }
}
