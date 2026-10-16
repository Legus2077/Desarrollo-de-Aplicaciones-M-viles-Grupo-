package com.example.cyberquest2;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Funciones y datos compartidos por todas las actividades:
 * banner de mensajes, barra de navegacion inferior, dialogos,
 * banco de preguntas e historial de resultados del usuario.
 */
public class Utilidades {

    private static final Handler handler = new Handler(Looper.getMainLooper());

    /** Banco de preguntas cargado desde assets/banco_preguntas.json. */
    public static QuestionBank banco;

    /** Historial de resultados (solo tests realmente rendidos). */
    public static final ArrayList<TestResult> listaResultados = new ArrayList<>();

    /**
     * Mejor puntaje obtenido por el usuario en cada nivel (clave = id del
     * nivel). Se llena desde Firestore al iniciar sesion y se actualiza al
     * terminar cada test; con esto se calcula el progreso del Home.
     */
    public static final Map<String, Integer> mejorPuntaje = new LinkedHashMap<>();

    /** Lo ejecuta la pantalla visible para refrescarse cuando llegan los resultados. */
    public static Runnable alActualizar;

    // ---------------------------------------------------------------- banco

    /**
     * Carga el banco de preguntas desde assets/banco_preguntas.json.
     * Las preguntas ya no estan escritas dentro del codigo: viven en el
     * recurso, de modo que se puedan actualizar sin tocar la logica del test.
     */
    public static void cargarBanco(Context context) {
        try {
            banco = QuestionBank.fromAssets(context, QuestionBank.ARCHIVO_POR_DEFECTO);
        } catch (Exception e) {
            banco = null;
            Toast.makeText(context, "No se pudo cargar el banco de preguntas.", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Ubica el nivel al que pertenece un resultado. Los resultados nuevos
     * guardan el id del nivel; los anteriores solo el nombre visible, por lo
     * que se busca por nombre como respaldo.
     */
    private static String resolverIdNivel(String levelId, String levelName) {
        if (banco == null) {
            return levelId;
        }
        for (QuestionBank.Level level : banco.getLevels()) {
            if (level.getId().equals(levelId)) {
                return level.getId();
            }
        }
        if (levelName != null) {
            for (QuestionBank.Level level : banco.getLevels()) {
                if (levelName.equalsIgnoreCase(level.getName())) {
                    return level.getId();
                }
            }
        }
        return null;
    }

    private static void avisarCambio() {
        if (alActualizar != null) {
            alActualizar.run();
        }
    }

    /**
     * Lee el historial real del usuario desde Firestore y recalcula el Home.
     * Se ejecuta cada vez que el usuario inicia sesion, para que el progreso
     * y el historial no se pierdan al cerrar la aplicacion.
     */
    public static void cargarResultadosUsuario(FirebaseManager firebaseManager) {
        listaResultados.clear();
        mejorPuntaje.clear();
        avisarCambio();

        firebaseManager.loadResults(new FirebaseManager.ResultsCallback() {
            @Override
            public void onLoaded(List<FirebaseManager.ScoreRecord> results) {
                SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                listaResultados.clear();
                mejorPuntaje.clear();

                for (FirebaseManager.ScoreRecord record : results) {
                    String levelId = resolverIdNivel(record.getLevelId(), record.getLevelName());
                    int threshold = (levelId != null && banco != null)
                            ? banco.getPassThreshold(levelId) : 80;
                    boolean passed = record.getScore() >= threshold;

                    String levelName = record.getLevelName();
                    if (TextUtils.isEmpty(levelName) && banco != null && levelId != null) {
                        levelName = banco.getLevelName(levelId);
                    }
                    String date = record.getTimestamp() > 0
                            ? format.format(new Date(record.getTimestamp()))
                            : "Sin fecha";

                    listaResultados.add(new TestResult(
                            record.getScore() + "%",
                            levelName == null ? "Test" : levelName,
                            "Completado · " + date + " · " + (passed ? "Aprobado" : "No aprobado"),
                            !passed));

                    if (levelId != null) {
                        Integer previous = mejorPuntaje.get(levelId);
                        if (previous == null || record.getScore() > previous) {
                            mejorPuntaje.put(levelId, record.getScore());
                        }
                    }
                }
                avisarCambio();
            }

            @Override
            public void onError(String errorMessage) {
                // Sin conexion o sin resultados todavia: el Home queda en cero.
                avisarCambio();
            }
        });
    }

    /** Al cerrar sesion se limpia el progreso en memoria. */
    public static void limpiarProgreso() {
        listaResultados.clear();
        mejorPuntaje.clear();
    }

    // --------------------------------------------------------------- banner

    /** Muestra el banner superior (antes "welcomeToast") durante unos segundos. */
    public static void mostrarBanner(Activity activity, String mensaje) {
        LinearLayout banner = activity.findViewById(R.id.welcomeToast);
        TextView tv = activity.findViewById(R.id.tvToastMessage);
        if (banner == null || tv == null) {
            return;
        }
        tv.setText(mensaje);
        banner.animate().cancel();
        banner.setAlpha(0f);
        banner.setTranslationY(-30f);
        banner.setVisibility(View.VISIBLE);
        banner.animate().alpha(1f).translationY(0f).setDuration(250).setListener(null).start();

        handler.postDelayed(() -> banner.animate()
                .alpha(0f)
                .translationY(-30f)
                .setDuration(300)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        banner.setVisibility(View.GONE);
                    }
                })
                .start(), 2800);
    }

    /** Resalta la pestana activa de la barra inferior. */
    public static void marcarNav(Activity activity, boolean enInicio) {
        TextView tvHome = activity.findViewById(R.id.tvNavHome);
        TextView tvResults = activity.findViewById(R.id.tvNavResults);
        ImageView ivHome = activity.findViewById(R.id.ivNavHome);
        ImageView ivResults = activity.findViewById(R.id.ivNavResults);
        ImageView ivLogout = activity.findViewById(R.id.ivNavLogout);
        int activo = ContextCompat.getColor(activity, R.color.cq_cyan);
        int inactivo = ContextCompat.getColor(activity, R.color.cq_text_faint);

        tvHome.setTextColor(enInicio ? activo : inactivo);
        tvResults.setTextColor(!enInicio ? activo : inactivo);
        ivHome.setColorFilter(enInicio ? activo : inactivo);
        ivResults.setColorFilter(!enInicio ? activo : inactivo);
        ivLogout.setColorFilter(inactivo);
    }

    // ------------------------------------------------------------- dialogos

    /** Crea un dialogo con fondo transparente y ancho completo. */
    public static Dialog crearDialogo(Activity activity, int layout) {
        Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(layout);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT);
        }
        return dialog;
    }

    /** Dialogo de confirmacion para cerrar sesion; vuelve a la pantalla de login. */
    public static void mostrarDialogoCerrarSesion(Activity activity, FirebaseManager firebaseManager) {
        Dialog dialog = crearDialogo(activity, R.layout.dialog_logout_confirm);
        Button btnCancel = dialog.findViewById(R.id.btnLogoutCancel);
        Button btnConfirm = dialog.findViewById(R.id.btnLogoutConfirm);

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnConfirm.setOnClickListener(v -> {
            dialog.dismiss();
            firebaseManager.logout();
            // El siguiente usuario que entre vera su propio historial desde Firestore.
            limpiarProgreso();
            Intent i = new Intent(activity, MainActivity.class);
            i.putExtra("mensaje", "Sesión cerrada correctamente.");
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            activity.startActivity(i);
            activity.finish();
        });
        dialog.show();
    }

    /** Dialogo para generar y abrir el reporte PDF de resultados. */
    public static void mostrarDialogoDescarga(Activity activity, FirebaseManager firebaseManager) {
        final String userName = firebaseManager.getSavedUserName();
        Dialog dialog = crearDialogo(activity, R.layout.dialog_download_pdf);

        EditText etName = dialog.findViewById(R.id.etPdfFullName);
        etName.setText(userName);
        Button btnCancel = dialog.findViewById(R.id.btnPdfCancel);
        Button btnGenerate = dialog.findViewById(R.id.btnPdfGenerate);

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnGenerate.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            if (TextUtils.isEmpty(name)) {
                name = userName;
            }
            dialog.dismiss();
            try {
                File pdfFile = PdfReportGenerator.generateReport(activity, name, listaResultados);
                Uri pdfUri = FileProvider.getUriForFile(activity,
                        activity.getPackageName() + ".fileprovider", pdfFile);

                Intent viewIntent = new Intent(Intent.ACTION_VIEW);
                viewIntent.setDataAndType(pdfUri, "application/pdf");
                viewIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                try {
                    activity.startActivity(Intent.createChooser(viewIntent, "Abrir reporte PDF"));
                } catch (ActivityNotFoundException e) {
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("application/pdf");
                    shareIntent.putExtra(Intent.EXTRA_STREAM, pdfUri);
                    shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    activity.startActivity(Intent.createChooser(shareIntent, "Compartir reporte PDF"));
                }
                mostrarBanner(activity, "PDF guardado: " + pdfFile.getName());
            } catch (Exception ex) {
                mostrarBanner(activity, "Error al crear PDF: " + ex.getMessage());
            }
        });
        dialog.show();
    }
}
