package com.example.cyberquest2;

import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;


public class calificacion extends AppCompatActivity {

    FrameLayout resultScoreCircle;
    TextView tvFinalScore, tvFinalStatus, tvFinalLevel, tvFinalThreshold, tvFinalMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calificacion);

        FirebaseManager firebaseManager = new FirebaseManager(this);

        resultScoreCircle = findViewById(R.id.resultScoreCircle);
        tvFinalScore = findViewById(R.id.tvFinalScore);
        tvFinalStatus = findViewById(R.id.tvFinalStatus);
        tvFinalLevel = findViewById(R.id.tvFinalLevel);
        tvFinalThreshold = findViewById(R.id.tvFinalThreshold);
        tvFinalMessage = findViewById(R.id.tvFinalMessage);

        int percent = getIntent().getIntExtra("percent", 0);
        boolean passed = getIntent().getBooleanExtra("passed", false);
        int threshold = getIntent().getIntExtra("threshold", 80);
        String levelName = getIntent().getStringExtra("levelName");
        showTestResult(percent, passed, threshold, levelName == null ? "" : levelName);

        findViewById(R.id.btnBackFromTestResult).setOnClickListener(v -> finish());
        findViewById(R.id.btnGoHomeFromResult).setOnClickListener(v -> finish());
        findViewById(R.id.btnRetryTest).setOnClickListener(v -> {
            if (pregunta.iniciar(this)) {
                finish();
            }
        });
        findViewById(R.id.btnDownloadFromResult).setOnClickListener(v ->
                Utilidades.mostrarDialogoDescarga(this, firebaseManager));
    }

    private void showTestResult(int percent, boolean passed, int threshold, String levelName) {
        tvFinalScore.setText(percent + "%");
        tvFinalStatus.setText(passed ? "Aprobado" : "No aprobado");
        tvFinalStatus.setTextColor(ContextCompat.getColor(this,
                passed ? R.color.cq_success : R.color.cq_danger));
        resultScoreCircle.setBackgroundResource(passed
                ? R.drawable.bg_score_circle
                : R.drawable.bg_score_circle_danger);
        tvFinalLevel.setText(levelName);
        tvFinalThreshold.setText("Puntaje mínimo para aprobar: " + threshold + "%");
        tvFinalMessage.setText(passed
                ? "¡Bien! Alcanzaste el puntaje mínimo de este nivel."
                : "No alcanzaste el puntaje mínimo. Puedes volver a intentarlo las veces que quieras.");
    }
}
