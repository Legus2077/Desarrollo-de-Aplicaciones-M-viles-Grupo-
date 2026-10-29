package com.example.cyberquest2;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

public class Splash extends AppCompatActivity {

    /** Duracion del splash: coincide con el audio y con el llenado de la barra. */
    private static final int DURACION_SPLASH = 2400;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable irAMain;
    private boolean yaNavego = false;
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // El banco de preguntas se carga al abrir la aplicacion
        Utilidades.cargarBanco(this);

        animarBarra();
        reproducirAudio();

        irAMain = new Runnable() {
            @Override
            public void run() {
                if (yaNavego) {
                    return;
                }
                yaNavego = true;
                detenerAudio();
                startActivity(new Intent(Splash.this, MainActivity.class));
                finish();
            }
        };

        // Toca la pantalla para saltar el splash
        findViewById(R.id.screenSplash).setOnClickListener(v -> {
            handler.removeCallbacks(irAMain);
            irAMain.run();
        });

        handler.postDelayed(irAMain, DURACION_SPLASH);
    }

    /** Llena la barra de 0% a 100% mientras dura el splash. */
    private void animarBarra() {
        final ProgressBar pbSplash = findViewById(R.id.pbSplash);
        pbSplash.setProgress(0);

        Animation llenar = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                pbSplash.setProgress((int) (interpolatedTime * 100));
            }
        };
        llenar.setDuration(DURACION_SPLASH);
        llenar.setInterpolator(new LinearInterpolator());
        pbSplash.startAnimation(llenar);
    }

    /** Reproduce el sonido de intro; si el recurso falla, la app sigue igual. */
    private void reproducirAudio() {
        try {
            mediaPlayer = MediaPlayer.create(this, R.raw.fx_effect_intro_1);
            if (mediaPlayer != null) {
                mediaPlayer.start();
            }
        } catch (Exception e) {
            mediaPlayer = null;
        }
    }

    /** Libera el audio para que no siga sonando ni quede el reproductor abierto. */
    private void detenerAudio() {
        if (mediaPlayer == null) {
            return;
        }
        try {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
        } catch (IllegalStateException e) {
            // El reproductor ya estaba detenido.
        }
        mediaPlayer.release();
        mediaPlayer = null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(irAMain);
        detenerAudio();
    }
}
