package com.example.cyberquest2;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

/** Introduccion del Test Basico. */
public class intro extends AppCompatActivity {

    TextView tvTestIntroTitle, tvIntroCount, tvIntroDuration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        tvTestIntroTitle = findViewById(R.id.tvTestIntroTitle);
        tvIntroCount = findViewById(R.id.tvIntroCount);
        tvIntroDuration = findViewById(R.id.tvIntroDuration);

        updateTestIntro();

        findViewById(R.id.btnBackFromTestIntro).setOnClickListener(v -> finish());
        findViewById(R.id.btnStartQuiz).setOnClickListener(v -> {
            if (pregunta.iniciar(this)) {
                finish();
            }
        });
    }

    /** Ajusta la informacion que se muestra antes de comenzar el test. */
    private void updateTestIntro() {
        QuestionBank banco = Utilidades.banco;
        if (banco == null) {
            return;
        }
        tvTestIntroTitle.setText(banco.getLevelName(pregunta.nivelActual));
        tvIntroCount.setText(String.valueOf(banco.getTestSize()));
        tvIntroDuration.setText("~" + (banco.getTestSize() / 2) + " min");
    }
}
