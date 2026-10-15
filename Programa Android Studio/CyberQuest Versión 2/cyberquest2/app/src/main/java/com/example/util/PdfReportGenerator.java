package com.example.util;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.pdf.PdfDocument;
import android.os.Environment;

import com.example.models.TestResult;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PdfReportGenerator {

    public static File generateReport(Context context, String studentName, List<TestResult> results) throws IOException {
        PdfDocument document = new PdfDocument();

        // Standard A4 page size in points: 595 x 842
        int pageWidth = 595;
        int pageHeight = 842;
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);

        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();
        paint.setAntiAlias(true);

        // Background
        paint.setColor(Color.WHITE);
        canvas.drawRect(0, 0, pageWidth, pageHeight, paint);

        // Header Background Banner
        paint.setColor(Color.parseColor("#17092C"));
        canvas.drawRect(0, 0, pageWidth, 120, paint);

        // Header Brand Title
        paint.setColor(Color.parseColor("#00E0C6"));
        paint.setTextSize(26);
        paint.setFakeBoldText(true);
        canvas.drawText("CyberQuest", 40, 60, paint);

        // Header Subtitle
        paint.setColor(Color.parseColor("#AE9DCB"));
        paint.setTextSize(12);
        paint.setFakeBoldText(false);
        canvas.drawText("PLATAFORMA DE APRENDIZAJE Y SEGURIDAD DIGITAL", 40, 85, paint);

        // Document Title
        paint.setColor(Color.parseColor("#1F0E3B"));
        paint.setTextSize(20);
        paint.setFakeBoldText(true);
        canvas.drawText("Reporte Oficial de Resultados", 40, 165, paint);

        // Divider
        paint.setColor(Color.parseColor("#9B4DFF"));
        paint.setStrokeWidth(2.5f);
        canvas.drawLine(40, 180, pageWidth - 40, 180, paint);

        // Student Info Box
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.parseColor("#F7F4FD"));
        canvas.drawRoundRect(40, 200, pageWidth - 40, 280, 12, 12, paint);

        paint.setColor(Color.parseColor("#351D5A"));
        paint.setTextSize(14);
        paint.setFakeBoldText(true);
        canvas.drawText("Estudiante:", 60, 235, paint);

        paint.setColor(Color.parseColor("#1F0E3B"));
        paint.setTextSize(16);
        canvas.drawText(studentName != null ? studentName : "Ana Torres", 150, 235, paint);

        paint.setColor(Color.parseColor("#7C6A9C"));
        paint.setTextSize(12);
        paint.setFakeBoldText(false);
        String currentDate = new SimpleDateFormat("dd 'de' MMMM 'de' yyyy, HH:mm", new Locale("es", "ES")).format(new Date());
        canvas.drawText("Fecha de emisión: " + currentDate, 60, 260, paint);

        // Results Section Header
        paint.setColor(Color.parseColor("#1F0E3B"));
        paint.setTextSize(16);
        paint.setFakeBoldText(true);
        canvas.drawText("Evaluaciones Completadas", 40, 320, paint);

        // Draw Results Table
        int yOffset = 345;
        if (results != null) {
            for (TestResult item : results) {
                // Item Card Background
                paint.setColor(Color.parseColor("#F9F8FC"));
                paint.setStyle(Paint.Style.FILL);
                canvas.drawRoundRect(40, yOffset, pageWidth - 40, yOffset + 65, 8, 8, paint);

                // Card Border
                paint.setColor(Color.parseColor("#E4DCF3"));
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(1f);
                canvas.drawRoundRect(40, yOffset, pageWidth - 40, yOffset + 65, 8, 8, paint);
                paint.setStyle(Paint.Style.FILL);

                // Score Badge Circle
                int circleX = 75;
                int circleY = yOffset + 32;
                paint.setColor(item.isLowScore() ? Color.parseColor("#FFE5EB") : Color.parseColor("#EBE2FF"));
                canvas.drawCircle(circleX, circleY, 20, paint);

                // Score Text
                paint.setColor(item.isLowScore() ? Color.parseColor("#D32F2F") : Color.parseColor("#6200EE"));
                paint.setTextSize(12);
                paint.setFakeBoldText(true);
                paint.setTextAlign(Paint.Align.CENTER);
                canvas.drawText(item.getScore(), circleX, circleY + 4, paint);
                paint.setTextAlign(Paint.Align.LEFT);

                // Title
                paint.setColor(Color.parseColor("#1F0E3B"));
                paint.setTextSize(14);
                paint.setFakeBoldText(true);
                canvas.drawText(item.getTitle(), 115, yOffset + 28, paint);

                // Detail
                paint.setColor(Color.parseColor("#7C6A9C"));
                paint.setTextSize(11);
                paint.setFakeBoldText(false);
                canvas.drawText(item.getDetail(), 115, yOffset + 48, paint);

                yOffset += 78;
            }
        }

        // Security Advice Box
        yOffset += 20;
        paint.setColor(Color.parseColor("#E6FAF7"));
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRoundRect(40, yOffset, pageWidth - 40, yOffset + 80, 10, 10, paint);

        paint.setColor(Color.parseColor("#00897B"));
        paint.setTextSize(12);
        paint.setFakeBoldText(true);
        canvas.drawText("Recomendación de Seguridad CyberQuest:", 55, yOffset + 30, paint);

        paint.setColor(Color.parseColor("#004D40"));
        paint.setTextSize(10.5f);
        paint.setFakeBoldText(false);
        canvas.drawText("Mantén tus contraseñas protegidas y únicas para cada servicio.", 55, yOffset + 50, paint);
        canvas.drawText("Activa la autenticación de dos factores (2FA) en todas tus cuentas críticas.", 55, yOffset + 66, paint);

        // Footer
        paint.setColor(Color.parseColor("#9E9E9E"));
        paint.setTextSize(10);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("Documento generado automáticamente por CyberQuest Android App", pageWidth / 2f, pageHeight - 35, paint);

        document.finishPage(page);

        // Save File
        File docsFolder = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        if (docsFolder == null) {
            docsFolder = context.getFilesDir();
        }
        if (!docsFolder.exists()) {
            docsFolder.mkdirs();
        }

        String safeName = (studentName != null ? studentName : "Usuario").replaceAll("[^a-zA-Z0-9_-]", "_");
        File pdfFile = new File(docsFolder, "CyberQuest_Reporte_" + safeName + ".pdf");
        FileOutputStream fos = new FileOutputStream(pdfFile);
        document.writeTo(fos);
        fos.close();
        document.close();

        return pdfFile;
    }
}
