package com.example.cyberquest2;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

/** Flujo de preguntas del test. */
public class pregunta extends AppCompatActivity {

    /** Nivel que se esta rindiendo. */
    public static String nivelActual = "basico";

    /** Preguntas del test en curso (las arma {@link #iniciar(Activity)}). */
    public static List<Question> preguntasActuales;

    ProgressBar pbQuestionProgress;
    TextView tvQuestionProgressCounter, tvQuestionText, tvQuestionModule, tvScenarioText;
    LinearLayout cardScenario;
    LinearLayout optContainerA, optContainerB, optContainerC, optContainerD;
    TextView optBadgeA, optBadgeB, optBadgeC, optBadgeD;
    TextView optTextA, optTextB, optTextC, optTextD;
    Button btnNextQuestion;

    private List<Question> questionList;
    private int currentQuestionIndex = 0;
    private int selectedOptionIndex = -1;
    private int correctAnswersCount = 0;
    private FirebaseManager firebaseManager;

    /**
     * Arma el test del nivel actual y abre la pantalla de preguntas.
     * Devuelve false (con un mensaje) si no se pudo armar el test.
     */
    public static boolean iniciar(Activity activity) {
        QuestionBank banco = Utilidades.banco;
        if (banco == null) {
            Toast.makeText(activity, "No se pudo cargar el banco de preguntas.", Toast.LENGTH_LONG).show();
            return false;
        }

        // Se eligen 2, 2, 3 y 3 preguntas al azar, una por modulo (RF04 y RF09).
        try {
            preguntasActuales = banco.buildTest(nivelActual);
        } catch (IllegalStateException e) {
            Toast.makeText(activity, e.getMessage(), Toast.LENGTH_LONG).show();
            return false;
        }

        activity.startActivity(new Intent(activity.getApplicationContext(), pregunta.class));
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pregunta);

        firebaseManager = new FirebaseManager(this);
        questionList = preguntasActuales;

        pbQuestionProgress = findViewById(R.id.pbQuestionProgress);
        tvQuestionProgressCounter = findViewById(R.id.tvQuestionProgressCounter);
        tvQuestionText = findViewById(R.id.tvQuestionText);
        tvQuestionModule = findViewById(R.id.tvQuestionModule);
        cardScenario = findViewById(R.id.cardScenario);
        tvScenarioText = findViewById(R.id.tvScenarioText);
        optContainerA = findViewById(R.id.optContainerA);
        optContainerB = findViewById(R.id.optContainerB);
        optContainerC = findViewById(R.id.optContainerC);
        optContainerD = findViewById(R.id.optContainerD);
        optBadgeA = findViewById(R.id.optBadgeA);
        optBadgeB = findViewById(R.id.optBadgeB);
        optBadgeC = findViewById(R.id.optBadgeC);
        optBadgeD = findViewById(R.id.optBadgeD);
        optTextA = findViewById(R.id.optTextA);
        optTextB = findViewById(R.id.optTextB);
        optTextC = findViewById(R.id.optTextC);
        optTextD = findViewById(R.id.optTextD);
        btnNextQuestion = findViewById(R.id.btnNextQuestion);

        if (questionList == null || questionList.isEmpty()) {
            finish();
            return;
        }

        findViewById(R.id.btnBackFromQuestion).setOnClickListener(v -> showExitTestDialog());
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                showExitTestDialog();
            }
        });

        optContainerA.setOnClickListener(v -> selectOption(0));
        optContainerB.setOnClickListener(v -> selectOption(1));
        optContainerC.setOnClickListener(v -> selectOption(2));
        optContainerD.setOnClickListener(v -> selectOption(3));

        btnNextQuestion.setOnClickListener(v -> {
            if (selectedOptionIndex == -1) {
                Toast.makeText(this, "Selecciona una opción para continuar", Toast.LENGTH_SHORT).show();
                return;
            }

            Question currentQ = questionList.get(currentQuestionIndex);
            if (selectedOptionIndex == currentQ.getCorrectOptionIndex()) {
                correctAnswersCount++;
            }

            if (currentQuestionIndex < questionList.size() - 1) {
                currentQuestionIndex++;
                renderQuestion();
            } else {
                finishQuiz();
            }
        });

        currentQuestionIndex = 0;
        correctAnswersCount = 0;
        selectedOptionIndex = -1;
        renderQuestion();
    }

    private void renderQuestion() {
        selectedOptionIndex = -1;
        resetOptionViews();

        Question q = questionList.get(currentQuestionIndex);

        // Modulo al que pertenece la pregunta (RF04).
        tvQuestionModule.setText(q.getModuleName().toUpperCase());

        // Escenario del caso: solo las preguntas de phishing lo traen.
        if (q.hasScenario()) {
            cardScenario.setVisibility(View.VISIBLE);
            tvScenarioText.setText(q.getScenario());
        } else {
            cardScenario.setVisibility(View.GONE);
            tvScenarioText.setText("");
        }

        tvQuestionText.setText(q.getText());

        List<String> opts = q.getOptions();
        optTextA.setText(opts.size() > 0 ? opts.get(0) : "");
        optTextB.setText(opts.size() > 1 ? opts.get(1) : "");
        optTextC.setText(opts.size() > 2 ? opts.get(2) : "");
        optTextD.setText(opts.size() > 3 ? opts.get(3) : "");

        // Las preguntas del banco tienen 3 opciones: se oculta la opcion D
        // para no dejar una alternativa vacia en pantalla.
        optContainerD.setVisibility(opts.size() > 3 ? View.VISIBLE : View.GONE);

        int currentNumber = currentQuestionIndex + 1;
        int total = questionList.size();
        tvQuestionProgressCounter.setText("Pregunta " + currentNumber + " de " + total);
        pbQuestionProgress.setProgress((int) (((float) currentNumber / total) * 100));

        btnNextQuestion.setText(currentNumber == total ? "Finalizar test" : "Siguiente");
    }

    private void resetOptionViews() {
        optContainerA.setBackgroundResource(R.drawable.bg_option_default);
        optContainerB.setBackgroundResource(R.drawable.bg_option_default);
        optContainerC.setBackgroundResource(R.drawable.bg_option_default);
        optContainerD.setBackgroundResource(R.drawable.bg_option_default);

        optBadgeA.setBackgroundResource(R.drawable.bg_option_letter);
        optBadgeB.setBackgroundResource(R.drawable.bg_option_letter);
        optBadgeC.setBackgroundResource(R.drawable.bg_option_letter);
        optBadgeD.setBackgroundResource(R.drawable.bg_option_letter);
    }

    private void selectOption(int index) {
        selectedOptionIndex = index;
        resetOptionViews();

        switch (index) {
            case 0:
                optContainerA.setBackgroundResource(R.drawable.bg_option_selected);
                optBadgeA.setBackgroundResource(R.drawable.bg_option_letter_selected);
                break;
            case 1:
                optContainerB.setBackgroundResource(R.drawable.bg_option_selected);
                optBadgeB.setBackgroundResource(R.drawable.bg_option_letter_selected);
                break;
            case 2:
                optContainerC.setBackgroundResource(R.drawable.bg_option_selected);
                optBadgeC.setBackgroundResource(R.drawable.bg_option_letter_selected);
                break;
            case 3:
                optContainerD.setBackgroundResource(R.drawable.bg_option_selected);
                optBadgeD.setBackgroundResource(R.drawable.bg_option_letter_selected);
                break;
        }
    }

    private void finishQuiz() {
        QuestionBank banco = Utilidades.banco;

        // Cada pregunta vale 10% (RF05): 10 preguntas suman 100%.
        int percent = (int) (((float) correctAnswersCount / questionList.size()) * 100);
        int threshold = banco != null ? banco.getPassThreshold(nivelActual) : 80;
        boolean passed = percent >= threshold;

        String levelName = banco != null
                ? banco.getLevelName(nivelActual)
                : "Test Básico";

        // Guarda el resultado en la nube (HU14) y en la cache local.
        firebaseManager.saveScore(percent, nivelActual, levelName);

        // Actualiza el progreso del Home con el resultado recien obtenido.
        Integer mejorPrevio = Utilidades.mejorPuntaje.get(nivelActual);
        if (mejorPrevio == null || percent > mejorPrevio) {
            Utilidades.mejorPuntaje.put(nivelActual, percent);
        }

        // El historial se llena solo con tests realmente rendidos.
        String scoreStr = percent + "%";
        String statusStr = passed ? "Aprobado" : "No aprobado";
        Utilidades.listaResultados.add(0, new TestResult(scoreStr, levelName,
                "Completado · Hoy · " + statusStr, !passed));

        Intent i = new Intent(getApplicationContext(), calificacion.class);
        i.putExtra("percent", percent);
        i.putExtra("passed", passed);
        i.putExtra("threshold", threshold);
        i.putExtra("levelName", levelName);
        startActivity(i);
        finish();
    }

    private void showExitTestDialog() {
        Dialog dialog = Utilidades.crearDialogo(this, R.layout.dialog_exit_test_confirm);
        Button btnStay = dialog.findViewById(R.id.btnStayInTest);
        Button btnExit = dialog.findViewById(R.id.btnExitTestConfirm);

        btnStay.setOnClickListener(v -> dialog.dismiss());
        btnExit.setOnClickListener(v -> {
            dialog.dismiss();
            finish();
        });
        dialog.show();
    }
}
