package com.example.cyberquest2;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

/** Pantalla principal: evaluador de contraseñas y tests por nivel. */
public class inicio extends AppCompatActivity {

    TextView tvHomeUserName, tvAvatarInitials, tvStatProgress, tvStatLevels, tvHomeGreeting;
    private FirebaseManager firebaseManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio);

        firebaseManager = new FirebaseManager(this);
        tvHomeUserName = findViewById(R.id.tvHomeUserName);
        tvAvatarInitials = findViewById(R.id.tvAvatarInitials);
        tvStatProgress = findViewById(R.id.tvStatProgress);
        tvStatLevels = findViewById(R.id.tvStatLevels);
        tvHomeGreeting = findViewById(R.id.tvHomeGreeting);

        actualizarSaludo(firebaseManager.getSavedUserName());
        Utilidades.marcarNav(this, true);

        findViewById(R.id.cardPasswordEvaluator).setOnClickListener(v ->
                startActivity(new Intent(getApplicationContext(), evaluador.class)));

        findViewById(R.id.cardTestBasico).setOnClickListener(v ->
                startActivity(new Intent(getApplicationContext(), intro.class)));

        findViewById(R.id.cardTestIntermedio).setOnClickListener(v ->
                showLockDialog("Intermedio", "Básico", 80));
        findViewById(R.id.cardTestAvanzado).setOnClickListener(v ->
                showLockDialog("Avanzado", "Intermedio", 85));
        findViewById(R.id.cardTestExperto).setOnClickListener(v ->
                showLockDialog("Experto", "Avanzado", 90));

        // Barra de navegación inferior
        findViewById(R.id.tabNavResults).setOnClickListener(v ->
                startActivity(new Intent(getApplicationContext(), resultados.class)));
        findViewById(R.id.tabNavLogout).setOnClickListener(v ->
                Utilidades.mostrarDialogoCerrarSesion(this, firebaseManager));

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Utilidades.mostrarDialogoCerrarSesion(inicio.this, firebaseManager);
            }
        });

        String mensaje = getIntent().getStringExtra("mensaje");
        if (mensaje != null) {
            Utilidades.mostrarBanner(this, mensaje);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Utilidades.alActualizar = this::updateHomeStats;
        updateHomeStats();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Utilidades.alActualizar = null;
    }

    private void actualizarSaludo(String userName) {
        if (userName == null) {
            userName = "";
        }
        tvHomeUserName.setText(userName);
        String initials;
        String[] parts = userName.trim().split("\\s+");
        if (parts.length >= 2) {
            initials = ("" + parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase();
        } else if (parts.length == 1 && parts[0].length() > 0) {
            initials = ("" + parts[0].charAt(0)).toUpperCase();
        } else {
            initials = "CQ";
        }
        tvAvatarInitials.setText(initials);
    }

    /**
     * Progreso general y niveles superados a partir de los resultados reales:
     * un nivel cuenta como superado cuando el mejor puntaje del usuario
     * alcanza el umbral definido para ese nivel en el banco.
     */
    private void updateHomeStats() {
        if (tvStatProgress == null || tvStatLevels == null) {
            return;
        }
        if (Utilidades.banco == null || Utilidades.banco.getLevels().isEmpty()) {
            tvStatProgress.setText("0%");
            tvStatLevels.setText("0/4");
            return;
        }

        List<QuestionBank.Level> levels = Utilidades.banco.getLevels();
        int passedLevels = 0;
        for (QuestionBank.Level level : levels) {
            Integer best = Utilidades.mejorPuntaje.get(level.getId());
            if (best != null && best >= level.getThreshold()) {
                passedLevels++;
            }
        }

        int percent = Math.round(passedLevels * 100f / levels.size());
        tvStatProgress.setText(percent + "%");
        tvStatLevels.setText(passedLevels + "/" + levels.size());

        // Recien llegado (sin tests rendidos) vs. usuario que ya rindio antes.
        if (tvHomeGreeting != null) {
            tvHomeGreeting.setText(Utilidades.listaResultados.isEmpty() ? "Hola" : "Hola de nuevo");
        }
    }

    private void showLockDialog(String toLevel, String fromLevel, int requiredPct) {
        Dialog dialog = Utilidades.crearDialogo(this, R.layout.dialog_lock_info);

        TextView tvTitle = dialog.findViewById(R.id.tvLockTitle);
        TextView tvMsg = dialog.findViewById(R.id.tvLockMessage);
        Button btnDismiss = dialog.findViewById(R.id.btnLockDismiss);

        tvTitle.setText("Test " + toLevel + " bloqueado");
        tvMsg.setText("Necesitas obtener al menos " + requiredPct + "% en el Test " + fromLevel
                + " para desbloquear el Test " + toLevel + ".");

        btnDismiss.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }
}
