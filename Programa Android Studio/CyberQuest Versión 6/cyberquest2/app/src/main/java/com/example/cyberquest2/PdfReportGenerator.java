package com.example.cyberquest2;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Genera el reporte PDF de resultados.
 *
 * <p>El documento tiene una pagina por cada bloque de resultados que quepa,
 * de modo que nunca se corta una evaluacion ni se pierde la recomendacion
 * final. Incluye un resumen (niveles aprobados, progreso y promedio) y una
 * fila por nivel con el mejor puntaje obtenido.
 */
public class PdfReportGenerator {

    private static final int ANCHO = 595;
    private static final int ALTO = 842;
    private static final int MARGEN = 40;

    /** Ultima altura util: debajo queda el pie de pagina. */
    private static final int LIMITE = ALTO - 80;

    private static final int ALTO_TARJETA = 60;
    private static final int ALTO_FILA = 70;

    private final PdfDocument documento = new PdfDocument();
    private final Paint paint = new Paint();

    private PdfDocument.Page pagina;
    private Canvas canvas;
    private int numeroPagina;
    private int y;

    public static File generateReport(Context context, String studentName,
                                      List<TestResult> results, int totalNiveles) throws IOException {
        return new PdfReportGenerator().generar(context, studentName, results, totalNiveles);
    }

    private File generar(Context context, String studentName, List<TestResult> results,
                         int totalNiveles) throws IOException {
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);

        nuevaPagina(true);
        dibujarTitulo(studentName);
        dibujarResumen(results, totalNiveles);
        dibujarFilas(results);
        dibujarRecomendacion();
        cerrarPagina();

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
        documento.writeTo(fos);
        fos.close();
        documento.close();
        return pdfFile;
    }

    // ------------------------------------------------------------------
    // Paginas y encabezado
    // ------------------------------------------------------------------

    private void nuevaPagina(boolean primera) {
        if (pagina != null) {
            cerrarPagina();
        }
        pagina = documento.startPage(
                new PdfDocument.PageInfo.Builder(ANCHO, ALTO, ++numeroPagina).create());
        canvas = pagina.getCanvas();

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        canvas.drawRect(0, 0, ANCHO, ALTO, paint);

        // Banda superior con la marca
        paint.setColor(Color.parseColor("#17092C"));
        canvas.drawRect(0, 0, ANCHO, 120, paint);

        paint.setColor(Color.parseColor("#00E0C6"));
        paint.setTextSize(26);
        paint.setFakeBoldText(true);
        paint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText("CyberQuest", MARGEN, 60, paint);

        paint.setColor(Color.parseColor("#AE9DCB"));
        paint.setTextSize(12);
        paint.setFakeBoldText(false);
        canvas.drawText("PLATAFORMA DE APRENDIZAJE Y SEGURIDAD DIGITAL", MARGEN, 85, paint);

        y = primera ? 150 : 150;
    }

    private void cerrarPagina() {
        if (pagina == null) {
            return;
        }
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.parseColor("#9E9E9E"));
        paint.setTextSize(10);
        paint.setFakeBoldText(false);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("Documento generado automáticamente por CyberQuest Android App",
                ANCHO / 2f, ALTO - 35, paint);
        paint.setTextAlign(Paint.Align.LEFT);

        documento.finishPage(pagina);
        pagina = null;
    }

    private void dibujarTitulo(String studentName) {
        paint.setColor(Color.parseColor("#1F0E3B"));
        paint.setTextSize(20);
        paint.setFakeBoldText(true);
        paint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText("Reporte Oficial de Resultados", MARGEN, y, paint);

        paint.setColor(Color.parseColor("#9B4DFF"));
        paint.setStrokeWidth(2.5f);
        canvas.drawLine(MARGEN, y + 15, ANCHO - MARGEN, y + 15, paint);

        // Caja con el estudiante y la fecha
        int cajaY = y + 30;
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.parseColor("#F7F4FD"));
        canvas.drawRoundRect(MARGEN, cajaY, ANCHO - MARGEN, cajaY + 70, 12, 12, paint);

        paint.setColor(Color.parseColor("#351D5A"));
        paint.setTextSize(14);
        paint.setFakeBoldText(true);
        canvas.drawText("Estudiante:", MARGEN + 20, cajaY + 30, paint);

        paint.setColor(Color.parseColor("#1F0E3B"));
        paint.setTextSize(16);
        canvas.drawText(ajustar(studentName != null ? studentName : "Usuario", 330),
                MARGEN + 110, cajaY + 30, paint);

        paint.setColor(Color.parseColor("#7C6A9C"));
        paint.setTextSize(12);
        paint.setFakeBoldText(false);
        String fecha = new SimpleDateFormat("dd 'de' MMMM 'de' yyyy, HH:mm", new Locale("es", "ES"))
                .format(new Date());
        canvas.drawText("Fecha de emisión: " + fecha, MARGEN + 20, cajaY + 52, paint);

        y = cajaY + 70 + 22;
    }

    // ------------------------------------------------------------------
    // Resumen
    // ------------------------------------------------------------------

    private void dibujarResumen(List<TestResult> results, int totalNiveles) {
        int aprobados = 0;
        int suma = 0;
        int evaluados = 0;
        if (results != null) {
            for (TestResult item : results) {
                evaluados++;
                suma += puntaje(item.getScore());
                if (!item.isLowScore()) {
                    aprobados++;
                }
            }
        }
        int total = Math.max(totalNiveles, evaluados);
        int promedio = evaluados > 0 ? Math.round(suma / (float) evaluados) : 0;
        int progreso = total > 0 ? Math.round(aprobados * 100f / total) : 0;

        paint.setColor(Color.parseColor("#1F0E3B"));
        paint.setTextSize(14);
        paint.setFakeBoldText(true);
        paint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText("Resumen de progreso", MARGEN, y, paint);

        int cajaY = y + 10;
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.parseColor("#F2ECFF"));
        canvas.drawRoundRect(MARGEN, cajaY, ANCHO - MARGEN, cajaY + 52, 10, 10, paint);

        paint.setColor(Color.parseColor("#5D55D7"));
        paint.setTextSize(12.5f);
        paint.setFakeBoldText(true);
        canvas.drawText("Niveles aprobados: " + aprobados + " de " + total,
                MARGEN + 18, cajaY + 21, paint);
        canvas.drawText("Progreso general: " + progreso + "%", MARGEN + 300, cajaY + 21, paint);

        paint.setColor(Color.parseColor("#351D5A"));
        paint.setTextSize(11.5f);
        paint.setFakeBoldText(false);
        canvas.drawText("Promedio de los mejores puntajes: " + promedio + "%",
                MARGEN + 18, cajaY + 39, paint);

        y = cajaY + 52 + 22;
    }

    // ------------------------------------------------------------------
    // Tabla de resultados
    // ------------------------------------------------------------------

    private void dibujarFilas(List<TestResult> results) {
        pintarSubtitulo("Evaluaciones Completadas", true);

        if (results == null || results.isEmpty()) {
            paint.setColor(Color.parseColor("#7C6A9C"));
            paint.setTextSize(11.5f);
            paint.setFakeBoldText(false);
            canvas.drawText("Todavía no hay evaluaciones completadas.", MARGEN, y + 20, paint);
            y += 40;
            return;
        }

        for (TestResult item : results) {
            if (y + ALTO_TARJETA > LIMITE) {
                nuevaPagina(false);
                pintarSubtitulo("Evaluaciones Completadas (continuación)", true);
            }

            // Fondo y borde de la tarjeta
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.parseColor("#F9F8FC"));
            canvas.drawRoundRect(MARGEN, y, ANCHO - MARGEN, y + ALTO_TARJETA, 8, 8, paint);

            paint.setColor(Color.parseColor("#E4DCF3"));
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(1f);
            canvas.drawRoundRect(MARGEN, y, ANCHO - MARGEN, y + ALTO_TARJETA, 8, 8, paint);
            paint.setStyle(Paint.Style.FILL);

            // Circulo con el puntaje
            int circuloX = MARGEN + 35;
            int circuloY = y + 30;
            paint.setColor(item.isLowScore()
                    ? Color.parseColor("#FFE5EB") : Color.parseColor("#EBE2FF"));
            canvas.drawCircle(circuloX, circuloY, 18, paint);

            paint.setColor(item.isLowScore()
                    ? Color.parseColor("#D32F2F") : Color.parseColor("#6200EE"));
            paint.setTextSize(12);
            paint.setFakeBoldText(true);
            paint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText(item.getScore(), circuloX, circuloY + 4, paint);
            paint.setTextAlign(Paint.Align.LEFT);

            // Nivel y detalle
            paint.setColor(Color.parseColor("#1F0E3B"));
            paint.setTextSize(14);
            paint.setFakeBoldText(true);
            canvas.drawText(ajustar(item.getTitle(), 400), MARGEN + 75, y + 26, paint);

            paint.setColor(Color.parseColor("#7C6A9C"));
            paint.setTextSize(11);
            paint.setFakeBoldText(false);
            canvas.drawText(ajustar(item.getDetail(), 400), MARGEN + 75, y + 45, paint);

            y += ALTO_FILA;
        }
    }

    private void pintarSubtitulo(String texto, boolean conEspacio) {
        if (conEspacio) {
            y += 2;
        }
        paint.setColor(Color.parseColor("#1F0E3B"));
        paint.setTextSize(14);
        paint.setFakeBoldText(true);
        paint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText(texto, MARGEN, y, paint);
        y += 18;
    }

    // ------------------------------------------------------------------
    // Recomendacion y utilidades
    // ------------------------------------------------------------------

    private void dibujarRecomendacion() {
        final int alto = 80;
        if (y + alto > LIMITE) {
            nuevaPagina(false);
        }

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.parseColor("#E6FAF7"));
        canvas.drawRoundRect(MARGEN, y, ANCHO - MARGEN, y + alto, 10, 10, paint);

        paint.setColor(Color.parseColor("#00897B"));
        paint.setTextSize(12);
        paint.setFakeBoldText(true);
        canvas.drawText("Recomendación de Seguridad CyberQuest:", MARGEN + 15, y + 28, paint);

        paint.setColor(Color.parseColor("#004D40"));
        paint.setTextSize(10.5f);
        paint.setFakeBoldText(false);
        canvas.drawText("Mantén tus contraseñas protegidas y únicas para cada servicio.",
                MARGEN + 15, y + 48, paint);
        canvas.drawText("Activa la autenticación de dos factores (2FA) en todas tus cuentas críticas.",
                MARGEN + 15, y + 64, paint);

        y += alto;
    }

    /** Recorta el texto con puntos suspensivos si no entra en el ancho dado. */
    private String ajustar(String texto, float anchoMaximo) {
        if (texto == null) {
            return "";
        }
        if (paint.measureText(texto) <= anchoMaximo) {
            return texto;
        }
        String recorte = texto;
        while (recorte.length() > 1 && paint.measureText(recorte + "…") > anchoMaximo) {
            recorte = recorte.substring(0, recorte.length() - 1);
        }
        return recorte + "…";
    }

    private static int puntaje(String score) {
        if (score == null) {
            return 0;
        }
        try {
            return Integer.parseInt(score.replace("%", "").trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
