package com.example.cyberquest2;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.List;

/** Pantalla principal: evaluador de contraseñas y tests por nivel. */
public class inicio extends AppCompatActivity {

    TextView tvHomeUserName, tvAvatarInitials, tvStatProgress, tvStatLevels, tvHomeGreeting;
    private FirebaseManager firebaseManager;

    /** Niveles que se pueden rendir desde el Home. */
    private static final String[] NIVELES_TARJETA = {"intermedio", "avanzado", "experto"};

    /** Vistas de estado de cada tarjeta: requisito, badge y candado. */
    private static final int[][] VISTAS_TARJETA = {
            {R.id.tvRequisitoIntermedio, R.id.badgeIntermedio, R.id.ivBadgeIntermedio},
            {R.id.tvRequisitoAvanzado, R.id.badgeAvanzado, R.id.ivBadgeAvanzado},
            {R.id.tvRequisitoExperto, R.id.badgeExperto, R.id.ivBadgeExperto},
    };

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

        findViewById(R.id.cardTestBasico).setOnClickListener(v -> abrirNivel("basico"));
        findViewById(R.id.cardTestIntermedio).setOnClickListener(v -> abrirNivel("intermedio"));
        findViewById(R.id.cardTestAvanzado).setOnClickListener(v -> abrirNivel("avanzado"));
        findViewById(R.id.cardTestExperto).setOnClickListener(v -> abrirNivel("experto"));

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
        Utilidades.alActualizar = this::actualizarHome;
        actualizarHome();
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

    /** Refresca el progreso y el estado (abierto/bloqueado) de las tarjetas. */
    private void actualizarHome() {
        updateHomeStats();
        actualizarTarjetas();
    }

    /**
     * HU08, HU09 y HU10: abre el nivel si el usuario ya cumplio el umbral del
     * nivel anterior; si no, muestra el dialogo con lo que le falta.
     */
    private void abrirNivel(String nivel) {
        if (Progreso.estaDesbloqueado(Utilidades.banco, Utilidades.mejorPuntaje, nivel)) {
            pregunta.nivelActual = nivel;
            startActivity(new Intent(getApplicationContext(), intro.class));
            return;
        }
        QuestionBank.Level actual = Progreso.nivel(Utilidades.banco, nivel);
        QuestionBank.Level anterior = Progreso.nivelAnterior(Utilidades.banco, nivel);
        showLockDialog(
                actual != null ? actual.getName() : nivel,
                anterior != null ? anterior.getName() : "",
                anterior != null ? anterior.getThreshold() : 80);
    }

    /**
     * Pinta el candado de cada tarjeta: cerrado y con el requisito mientras
     * esta bloqueada, abierto y "Disponible" cuando ya se puede rendir.
     */
    private void actualizarTarjetas() {
        for (int i = 0; i < NIVELES_TARJETA.length; i++) {
            String nivel = NIVELES_TARJETA[i];
            TextView requisito = findViewById(VISTAS_TARJETA[i][0]);
            View badge = findViewById(VISTAS_TARJETA[i][1]);
            ImageView icono = findViewById(VISTAS_TARJETA[i][2]);
            if (requisito == null || badge == null || icono == null) {
                continue;
            }

            if (Progreso.estaDesbloqueado(Utilidades.banco, Utilidades.mejorPuntaje, nivel)) {
                requisito.setText("Disponible");
                requisito.setTextColor(ContextCompat.getColor(this, R.color.cq_success));
                badge.setBackgroundResource(R.drawable.bg_check_circle_ok);
                icono.setImageResource(R.drawable.ic_check_mini);
                icono.setContentDescription("Disponible");
            } else {
                QuestionBank.Level anterior = Progreso.nivelAnterior(Utilidades.banco, nivel);
                int umbral = anterior != null ? anterior.getThreshold() : 0;
                String nombre = anterior != null ? anterior.getName().replace("Test ", "") : "";
                requisito.setText("Requiere ≥" + umbral + "% en " + nombre);
                requisito.setTextColor(ContextCompat.getColor(this, R.color.cq_warn));
                badge.setBackgroundResource(R.drawable.bg_lock_badge);
                icono.setImageResource(R.drawable.ic_lock);
                icono.setContentDescription("Bloqueado");
            }
        }
    }

    private void showLockDialog(String nombreNivel, String nombreAnterior, int requiredPct) {
        Dialog dialog = Utilidades.crearDialogo(this, R.layout.dialog_lock_info);

        TextView tvTitle = dialog.findViewById(R.id.tvLockTitle);
        TextView tvMsg = dialog.findViewById(R.id.tvLockMessage);
        Button btnDismiss = dialog.findViewById(R.id.btnLockDismiss);

        tvTitle.setText(nombreNivel + " bloqueado");
        tvMsg.setText("Necesitas obtener al menos " + requiredPct + "% en el " + nombreAnterior
                + " para desbloquear el " + nombreNivel + ".");

        btnDismiss.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }
}
