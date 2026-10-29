package com.example.cyberquest2;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.Transformation;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class calificacion extends AppCompatActivity {

    /** Tiempo que se muestra la animacion de carga antes de revelar el resultado (HU11). */
    private static final long DURACION_CARGA = 1500;

    /** Tiempo del conteo del puntaje, de 0 al valor final (HU11). */
    private static final long DURACION_CONTEO = 700;

    FrameLayout resultScoreCircle;
    TextView tvFinalScore, tvFinalStatus, tvFinalLevel, tvFinalThreshold, tvFinalMessage;

    private FrameLayout loadingResult;
    private FrameLayout loadingLogo;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable revelar;
    private int percentFinal;

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
        loadingResult = findViewById(R.id.loadingResult);
        loadingLogo = findViewById(R.id.loadingLogo);

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

        iniciarCarga(percent);
    }

    /** HU11: muestra el logo girando mientras se calcula el resultado. */
    private void iniciarCarga(int percent) {
        percentFinal = percent;
        tvFinalScore.setText("0%");

        RotateAnimation giro = new RotateAnimation(0f, 360f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        giro.setDuration(900);
        giro.setRepeatCount(Animation.INFINITE);
        giro.setInterpolator(new LinearInterpolator());
        loadingLogo.startAnimation(giro);

        revelar = new Runnable() {
            @Override
            public void run() {
                ocultarCarga();
            }
        };
        handler.postDelayed(revelar, DURACION_CARGA);
    }

    /** Oculta el overlay de carga con un fundido y revela el puntaje. */
    private void ocultarCarga() {
        loadingResult.animate()
                .alpha(0f)
                .setDuration(300)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        loadingResult.setVisibility(View.GONE);
                        loadingResult.setAlpha(1f);
                        contarPuntaje();
                    }
                })
                .start();
    }

    /** Anima el puntaje de 0% al valor obtenido. */
    private void contarPuntaje() {
        Animation conteo = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                int valor = (int) (interpolatedTime * percentFinal);
                tvFinalScore.setText(valor + "%");
            }
        };
        conteo.setDuration(DURACION_CONTEO);
        conteo.setInterpolator(new LinearInterpolator());
        tvFinalScore.startAnimation(conteo);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (revelar != null) {
            handler.removeCallbacks(revelar);
        }
        if (loadingLogo != null) {
            loadingLogo.clearAnimation();
        }
    }
}
