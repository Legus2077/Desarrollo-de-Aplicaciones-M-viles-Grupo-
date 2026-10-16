package com.example.cyberquest2;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

public class Splash extends AppCompatActivity {

    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable irAMain;
    private boolean yaNavego = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // El banco de preguntas se carga al abrir la aplicacion
        Utilidades.cargarBanco(this);

        irAMain = new Runnable() {
            @Override
            public void run() {
                if (yaNavego) {
                    return;
                }
                yaNavego = true;
                startActivity(new Intent(Splash.this, MainActivity.class));
                finish();
            }
        };

        // Toca la pantalla para saltar el splash
        findViewById(R.id.screenSplash).setOnClickListener(v -> {
            handler.removeCallbacks(irAMain);
            irAMain.run();
        });

        handler.postDelayed(irAMain, 1800);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(irAMain);
    }
}
