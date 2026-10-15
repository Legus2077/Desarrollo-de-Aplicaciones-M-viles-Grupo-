package com.example;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.adapters.ResultsAdapter;
import com.example.models.PasswordEvaluation;
import com.example.models.Question;
import com.example.models.TestResult;
import com.example.util.FirebaseManager;
import com.example.util.PdfReportGenerator;
import com.example.util.QuestionBank;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    public enum Screen {
        SPLASH, AUTH, HOME, PWD_EVALUATOR, TEST_INTRO, QUESTION, RESULTS, TEST_RESULT
    }

    private Screen currentScreen = Screen.SPLASH;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private Runnable splashRunnable;

    // User State
    private String userName = "Ana Torres";
    private boolean isLoginMode = true;
    private FirebaseManager firebaseManager;

    // Screens Views
    private View screenSplash;
    private View screenAuth;
    private View screenHome;
    private View screenPwdEvaluator;
    private View screenTestIntro;
    private View screenQuestion;
    private View screenResults;
    private View screenTestResult;

    // Global Components
    private LinearLayout bottomNavBar;
    private LinearLayout tabNavHome;
    private LinearLayout tabNavResults;
    private LinearLayout tabNavLogout;
    private ImageView ivNavHome;
    private ImageView ivNavResults;
    private ImageView ivNavLogout;
    private TextView tvNavHome;
    private TextView tvNavResults;
    private TextView tvNavLogout;
    private LinearLayout welcomeToast;
    private TextView tvToastMessage;

    // Auth screen elements
    private TextView tvAuthTitle;
    private TextView tvAuthSub;
    private TextView tabLogin;
    private TextView tabRegister;
    private LinearLayout layoutFieldName;
    private LinearLayout layoutFieldConfirm;
    private EditText etFullName;
    private EditText etEmail;
    private EditText etPassword;
    private EditText etConfirmPassword;
    private TextView tvForgotPassword;
    private Button btnAuthSubmit;
    private TextView tvAuthFoot;

    // Home screen elements
    private TextView tvHomeUserName;
    private TextView tvAvatarInitials;

    // Password evaluator elements
    private EditText etPwdTest;
    private ProgressBar pbPwdMeter;
    private TextView tvStrengthLabel;
    private TextView tvStrengthPct;
    private TextView iconChkLen;
    private TextView textChkLen;
    private TextView iconChkUpper;
    private TextView textChkUpper;
    private TextView iconChkNum;
    private TextView textChkNum;
    private TextView iconChkSym;
    private TextView textChkSym;

    // Question flow elements
    private ProgressBar pbQuestionProgress;
    private TextView tvQuestionProgressCounter;
    private TextView tvQuestionText;
    private LinearLayout optContainerA;
    private LinearLayout optContainerB;
    private LinearLayout optContainerC;
    private LinearLayout optContainerD;
    private TextView optBadgeA;
    private TextView optBadgeB;
    private TextView optBadgeC;
    private TextView optBadgeD;
    private TextView optTextA;
    private TextView optTextB;
    private TextView optTextC;
    private TextView optTextD;
    private Button btnNextQuestion;

    private TextView tvQuestionModule;
    private LinearLayout cardScenario;
    private TextView tvScenarioText;
    private TextView tvTestIntroTitle;
    private TextView tvIntroCount;
    private TextView tvIntroDuration;
    private TextView tvStatProgress;
    private TextView tvStatLevels;
    private TextView tvHomeGreeting;
    private FrameLayout resultScoreCircle;
    private TextView tvFinalScore;
    private TextView tvFinalStatus;
    private TextView tvFinalLevel;
    private TextView tvFinalThreshold;
    private TextView tvFinalMessage;

    private List<Question> questionList;
    private QuestionBank questionBank;
    private String currentLevel = "basico";
    private int currentQuestionIndex = 0;
    private int selectedOptionIndex = -1;
    private int correctAnswersCount = 0;

    // Results screen elements
    private RecyclerView rvResults;
    private ResultsAdapter resultsAdapter;
    private final List<TestResult> resultsData = new ArrayList<>();

    /**
     * Mejor puntaje obtenido por el usuario en cada nivel (clave = id del
     * nivel). Se llena desde Firestore al iniciar sesion y se actualiza al
     * terminar cada test; con esto se calcula el progreso del Home.
     */
    private final Map<String, Integer> bestScoreByLevel = new LinkedHashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseManager = new FirebaseManager(this);
        userName = firebaseManager.getSavedUserName();

        initViews();
        setupResultsData();
        loadQuestionBank();
        setupListeners();
        setupBackNavigation();

        showScreen(Screen.SPLASH);

        // Auto transition from Splash after 1.8 seconds
        splashRunnable = () -> {
            if (currentScreen == Screen.SPLASH) {
                showScreen(Screen.AUTH);
            }
        };
        mainHandler.postDelayed(splashRunnable, 1800);
    }

    private void initViews() {
        screenSplash = findViewById(R.id.screenSplash);
        screenAuth = findViewById(R.id.screenAuth);
        screenHome = findViewById(R.id.screenHome);
        screenPwdEvaluator = findViewById(R.id.screenPwdEvaluator);
        screenTestIntro = findViewById(R.id.screenTestIntro);
        screenQuestion = findViewById(R.id.screenQuestion);
        screenResults = findViewById(R.id.screenResults);
        screenTestResult = findViewById(R.id.screenTestResult);

        bottomNavBar = findViewById(R.id.bottomNavBar);
        tabNavHome = findViewById(R.id.tabNavHome);
        tabNavResults = findViewById(R.id.tabNavResults);
        tabNavLogout = findViewById(R.id.tabNavLogout);
        ivNavHome = findViewById(R.id.ivNavHome);
        ivNavResults = findViewById(R.id.ivNavResults);
        ivNavLogout = findViewById(R.id.ivNavLogout);
        tvNavHome = findViewById(R.id.tvNavHome);
        tvNavResults = findViewById(R.id.tvNavResults);
        tvNavLogout = findViewById(R.id.tvNavLogout);

        welcomeToast = findViewById(R.id.welcomeToast);
        tvToastMessage = findViewById(R.id.tvToastMessage);

        // Auth
        tvAuthTitle = findViewById(R.id.tvAuthTitle);
        tvAuthSub = findViewById(R.id.tvAuthSub);
        tabLogin = findViewById(R.id.tabLogin);
        tabRegister = findViewById(R.id.tabRegister);
        layoutFieldName = findViewById(R.id.layoutFieldName);
        layoutFieldConfirm = findViewById(R.id.layoutFieldConfirm);
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        btnAuthSubmit = findViewById(R.id.btnAuthSubmit);
        tvAuthFoot = findViewById(R.id.tvAuthFoot);

        // Home
        tvHomeUserName = findViewById(R.id.tvHomeUserName);
        tvAvatarInitials = findViewById(R.id.tvAvatarInitials);
        tvStatProgress = findViewById(R.id.tvStatProgress);
        tvStatLevels = findViewById(R.id.tvStatLevels);
        tvHomeGreeting = findViewById(R.id.tvHomeGreeting);

        // Password Evaluator
        etPwdTest = findViewById(R.id.etPwdTest);
        pbPwdMeter = findViewById(R.id.pbPwdMeter);
        tvStrengthLabel = findViewById(R.id.tvStrengthLabel);
        tvStrengthPct = findViewById(R.id.tvStrengthPct);
        iconChkLen = findViewById(R.id.iconChkLen);
        textChkLen = findViewById(R.id.textChkLen);
        iconChkUpper = findViewById(R.id.iconChkUpper);
        textChkUpper = findViewById(R.id.textChkUpper);
        iconChkNum = findViewById(R.id.iconChkNum);
        textChkNum = findViewById(R.id.textChkNum);
        iconChkSym = findViewById(R.id.iconChkSym);
        textChkSym = findViewById(R.id.textChkSym);

        // Test Intro
        tvTestIntroTitle = findViewById(R.id.tvTestIntroTitle);
        tvIntroCount = findViewById(R.id.tvIntroCount);
        tvIntroDuration = findViewById(R.id.tvIntroDuration);

        // Question
        pbQuestionProgress = findViewById(R.id.pbQuestionProgress);
        tvQuestionProgressCounter = findViewById(R.id.tvQuestionProgressCounter);
        tvQuestionModule = findViewById(R.id.tvQuestionModule);
        cardScenario = findViewById(R.id.cardScenario);
        tvScenarioText = findViewById(R.id.tvScenarioText);
        tvQuestionText = findViewById(R.id.tvQuestionText);
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

        // Results
        rvResults = findViewById(R.id.rvResults);
        rvResults.setLayoutManager(new LinearLayoutManager(this));
        resultsAdapter = new ResultsAdapter(resultsData);
        rvResults.setAdapter(resultsAdapter);

        // Test Result (HU07)
        resultScoreCircle = findViewById(R.id.resultScoreCircle);
        tvFinalScore = findViewById(R.id.tvFinalScore);
        tvFinalStatus = findViewById(R.id.tvFinalStatus);
        tvFinalLevel = findViewById(R.id.tvFinalLevel);
        tvFinalThreshold = findViewById(R.id.tvFinalThreshold);
        tvFinalMessage = findViewById(R.id.tvFinalMessage);
    }

    /**
     * Historial de resultados. Se llena unicamente con los tests que el
     * usuario rinde durante la sesion; ya no hay resultados de ejemplo.
     */
    private void setupResultsData() {
        resultsData.clear();
    }

    /**
     * Lee el historial real del usuario desde Firestore y recalcula el Home.
     * Se ejecuta cada vez que el usuario inicia sesion, para que el progreso
     * y el historial no se pierdan al cerrar la aplicacion.
     */
    private void loadUserResults() {
        resultsData.clear();
        bestScoreByLevel.clear();
        resultsAdapter.notifyDataSetChanged();
        updateHomeStats();

        firebaseManager.loadResults(new FirebaseManager.ResultsCallback() {
            @Override
            public void onLoaded(List<FirebaseManager.ScoreRecord> results) {
                SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                resultsData.clear();
                bestScoreByLevel.clear();

                for (FirebaseManager.ScoreRecord record : results) {
                    String levelId = resolveLevelId(record.getLevelId(), record.getLevelName());
                    int threshold = (levelId != null && questionBank != null)
                            ? questionBank.getPassThreshold(levelId) : 80;
                    boolean passed = record.getScore() >= threshold;

                    String levelName = record.getLevelName();
                    if (TextUtils.isEmpty(levelName) && questionBank != null && levelId != null) {
                        levelName = questionBank.getLevelName(levelId);
                    }
                    String date = record.getTimestamp() > 0
                            ? format.format(new Date(record.getTimestamp()))
                            : "Sin fecha";

                    resultsData.add(new TestResult(
                            record.getScore() + "%",
                            levelName == null ? "Test" : levelName,
                            "Completado · " + date + " · " + (passed ? "Aprobado" : "No aprobado"),
                            !passed));

                    if (levelId != null) {
                        Integer previous = bestScoreByLevel.get(levelId);
                        if (previous == null || record.getScore() > previous) {
                            bestScoreByLevel.put(levelId, record.getScore());
                        }
                    }
                }
                resultsAdapter.notifyDataSetChanged();
                updateHomeStats();
            }

            @Override
            public void onError(String errorMessage) {
                // Sin conexion o sin resultados todavia: el Home queda en cero.
                updateHomeStats();
            }
        });
    }

    /**
     * Ubica el nivel al que pertenece un resultado. Los resultados nuevos
     * guardan el id del nivel; los anteriores solo el nombre visible, por lo
     * que se busca por nombre como respaldo.
     */
    private String resolveLevelId(String levelId, String levelName) {
        if (questionBank == null) {
            return levelId;
        }
        for (QuestionBank.Level level : questionBank.getLevels()) {
            if (level.getId().equals(levelId)) {
                return level.getId();
            }
        }
        if (levelName != null) {
            for (QuestionBank.Level level : questionBank.getLevels()) {
                if (levelName.equalsIgnoreCase(level.getName())) {
                    return level.getId();
                }
            }
        }
        return null;
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
        if (questionBank == null || questionBank.getLevels().isEmpty()) {
            tvStatProgress.setText("0%");
            tvStatLevels.setText("0/4");
            return;
        }

        List<QuestionBank.Level> levels = questionBank.getLevels();
        int passedLevels = 0;
        for (QuestionBank.Level level : levels) {
            Integer best = bestScoreByLevel.get(level.getId());
            if (best != null && best >= level.getThreshold()) {
                passedLevels++;
            }
        }

        int percent = Math.round(passedLevels * 100f / levels.size());
        tvStatProgress.setText(percent + "%");
        tvStatLevels.setText(passedLevels + "/" + levels.size());

        // Recien llegado (sin tests rendidos) vs. usuario que ya rindio antes.
        if (tvHomeGreeting != null) {
            tvHomeGreeting.setText(resultsData.isEmpty() ? "Hola" : "Hola de nuevo");
        }
    }

    /**
     * Carga el banco de preguntas desde assets/banco_preguntas.json.
     * Las preguntas ya no estan escritas dentro del codigo: viven en el
     * recurso, de modo que se puedan actualizar sin tocar la logica del test.
     */
    private void loadQuestionBank() {
        try {
            questionBank = QuestionBank.fromAssets(this, QuestionBank.ARCHIVO_POR_DEFECTO);
        } catch (Exception e) {
            questionBank = null;
            Toast.makeText(this, "No se pudo cargar el banco de preguntas.", Toast.LENGTH_LONG).show();
        }
    }

    private void setupListeners() {
        // Splash tap to skip
        screenSplash.setOnClickListener(v -> {
            mainHandler.removeCallbacks(splashRunnable);
            showScreen(Screen.AUTH);
        });

        // Auth Tabs
        tabLogin.setOnClickListener(v -> setAuthMode(true));
        tabRegister.setOnClickListener(v -> setAuthMode(false));
        tvAuthFoot.setOnClickListener(v -> setAuthMode(!isLoginMode));

        tvForgotPassword.setOnClickListener(v -> showForgotPasswordDialog());

        btnAuthSubmit.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String pwd = etPassword.getText().toString();

            if (isLoginMode) {
                setAuthLoading(true);
                firebaseManager.signIn(email, pwd, new FirebaseManager.AuthCallback() {
                    @Override
                    public void onSuccess(String name, String email) {
                        setAuthLoading(false);
                        userName = TextUtils.isEmpty(name) ? FirebaseManager.nameFromEmail(email) : name;
                        etPassword.setText("");
                        updateUserGreeting();
                        loadUserResults();
                        showScreen(Screen.HOME);
                        showWelcomeToast("¡Bienvenido de nuevo, " + userName + "!");
                    }

                    @Override
                    public void onError(String errorMessage) {
                        setAuthLoading(false);
                        Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
            } else {
                String inputName = etFullName.getText().toString().trim();
                String confirm = etConfirmPassword.getText().toString();

                if (TextUtils.isEmpty(inputName)) {
                    Toast.makeText(this, "Ingresa tu nombre completo.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!pwd.equals(confirm)) {
                    Toast.makeText(this, "Las contraseñas no coinciden.", Toast.LENGTH_SHORT).show();
                    return;
                }

                setAuthLoading(true);
                firebaseManager.register(inputName, email, pwd, new FirebaseManager.AuthCallback() {
                    @Override
                    public void onSuccess(String name, String email) {
                        setAuthLoading(false);
                        userName = name;
                        etPassword.setText("");
                        etConfirmPassword.setText("");
                        updateUserGreeting();
                        loadUserResults();
                        showScreen(Screen.HOME);
                        showWelcomeToast("¡Cuenta creada! Bienvenido, " + userName);
                    }

                    @Override
                    public void onError(String errorMessage) {
                        setAuthLoading(false);
                        Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
            }
        });

        // Home Actions
        findViewById(R.id.cardPasswordEvaluator).setOnClickListener(v -> {
            etPwdTest.setText("");
            evaluatePassword("");
            showScreen(Screen.PWD_EVALUATOR);
        });

        findViewById(R.id.cardTestBasico).setOnClickListener(v -> showScreen(Screen.TEST_INTRO));

        findViewById(R.id.cardTestIntermedio).setOnClickListener(v ->
                showLockDialog("Intermedio", "Básico", 80));

        findViewById(R.id.cardTestAvanzado).setOnClickListener(v ->
                showLockDialog("Avanzado", "Intermedio", 85));

        findViewById(R.id.cardTestExperto).setOnClickListener(v ->
                showLockDialog("Experto", "Avanzado", 90));

        // Bottom Navigation Tabs
        tabNavHome.setOnClickListener(v -> {
            updateNavHighlight(true);
            showScreen(Screen.HOME);
        });

        tabNavResults.setOnClickListener(v -> {
            updateNavHighlight(false);
            showScreen(Screen.RESULTS);
        });

        tabNavLogout.setOnClickListener(v -> showLogoutDialog());

        // Password Evaluator
        findViewById(R.id.btnBackFromPwd).setOnClickListener(v -> showScreen(Screen.HOME));

        etPwdTest.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                evaluatePassword(s != null ? s.toString() : "");
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Test Intro
        findViewById(R.id.btnBackFromTestIntro).setOnClickListener(v -> showScreen(Screen.HOME));
        findViewById(R.id.btnStartQuiz).setOnClickListener(v -> startQuiz());

        // Question Screen
        findViewById(R.id.btnBackFromQuestion).setOnClickListener(v -> showExitTestDialog());

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

        // Results Screen
        findViewById(R.id.btnBackFromResults).setOnClickListener(v -> showScreen(Screen.HOME));
        findViewById(R.id.btnDownloadResults).setOnClickListener(v -> showDownloadDialog());

        // Test Result Screen (HU07)
        findViewById(R.id.btnBackFromTestResult).setOnClickListener(v -> showScreen(Screen.HOME));
        findViewById(R.id.btnGoHomeFromResult).setOnClickListener(v -> showScreen(Screen.HOME));
        findViewById(R.id.btnRetryTest).setOnClickListener(v -> startQuiz());
        findViewById(R.id.btnDownloadFromResult).setOnClickListener(v -> showDownloadDialog());
    }

    private void setupBackNavigation() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                switch (currentScreen) {
                    case PWD_EVALUATOR:
                    case TEST_INTRO:
                    case RESULTS:
                    case TEST_RESULT:
                        showScreen(Screen.HOME);
                        break;
                    case QUESTION:
                        showExitTestDialog();
                        break;
                    case HOME:
                        showLogoutDialog();
                        break;
                    case AUTH:
                        finish();
                        break;
                    case SPLASH:
                        finish();
                        break;
                }
            }
        });
    }

    public void showScreen(Screen screen) {
        currentScreen = screen;

        screenSplash.setVisibility(screen == Screen.SPLASH ? View.VISIBLE : View.GONE);
        screenAuth.setVisibility(screen == Screen.AUTH ? View.VISIBLE : View.GONE);
        screenHome.setVisibility(screen == Screen.HOME ? View.VISIBLE : View.GONE);
        screenPwdEvaluator.setVisibility(screen == Screen.PWD_EVALUATOR ? View.VISIBLE : View.GONE);
        screenTestIntro.setVisibility(screen == Screen.TEST_INTRO ? View.VISIBLE : View.GONE);
        screenQuestion.setVisibility(screen == Screen.QUESTION ? View.VISIBLE : View.GONE);
        screenResults.setVisibility(screen == Screen.RESULTS ? View.VISIBLE : View.GONE);
        screenTestResult.setVisibility(screen == Screen.TEST_RESULT ? View.VISIBLE : View.GONE);

        if (screen == Screen.TEST_INTRO) {
            updateTestIntro();
        }

        if (screen == Screen.HOME) {
            updateHomeStats();
        }

        boolean showBottomNav = (screen == Screen.HOME || screen == Screen.RESULTS);
        bottomNavBar.setVisibility(showBottomNav ? View.VISIBLE : View.GONE);

        if (screen == Screen.HOME) {
            updateNavHighlight(true);
        } else if (screen == Screen.RESULTS) {
            updateNavHighlight(false);
        }
    }

    private void updateNavHighlight(boolean isHome) {
        tvNavHome.setTextColor(ContextCompat.getColor(this, isHome ? R.color.cq_cyan : R.color.cq_text_faint));
        tvNavResults.setTextColor(ContextCompat.getColor(this, !isHome ? R.color.cq_cyan : R.color.cq_text_faint));
        ivNavHome.setColorFilter(ContextCompat.getColor(this, isHome ? R.color.cq_cyan : R.color.cq_text_faint));
        ivNavResults.setColorFilter(ContextCompat.getColor(this, !isHome ? R.color.cq_cyan : R.color.cq_text_faint));
        ivNavLogout.setColorFilter(ContextCompat.getColor(this, R.color.cq_text_faint));
    }

    private void setAuthMode(boolean login) {
        isLoginMode = login;
        // Limpia credenciales al cambiar entre Iniciar sesión / Registrarme.
        etPassword.setText("");
        etConfirmPassword.setText("");
        if (login) {
            tabLogin.setBackgroundResource(R.drawable.bg_tab_active);
            tabLogin.setTextColor(ContextCompat.getColor(this, R.color.white));
            tabRegister.setBackgroundResource(0);
            tabRegister.setTextColor(ContextCompat.getColor(this, R.color.cq_text_dim));

            layoutFieldName.setVisibility(View.GONE);
            layoutFieldConfirm.setVisibility(View.GONE);
            tvForgotPassword.setVisibility(View.VISIBLE);

            tvAuthTitle.setText(R.string.welcome_back);
            tvAuthSub.setText(R.string.auth_subtitle_login);
            btnAuthSubmit.setText(R.string.btn_login);
            tvAuthFoot.setText(R.string.prompt_no_account);
        } else {
            tabRegister.setBackgroundResource(R.drawable.bg_tab_active);
            tabRegister.setTextColor(ContextCompat.getColor(this, R.color.white));
            tabLogin.setBackgroundResource(0);
            tabLogin.setTextColor(ContextCompat.getColor(this, R.color.cq_text_dim));

            layoutFieldName.setVisibility(View.VISIBLE);
            layoutFieldConfirm.setVisibility(View.VISIBLE);
            tvForgotPassword.setVisibility(View.GONE);

            tvAuthTitle.setText(R.string.create_account);
            tvAuthSub.setText(R.string.auth_subtitle_register);
            btnAuthSubmit.setText(R.string.btn_register);
            tvAuthFoot.setText(R.string.prompt_has_account);
        }
    }

    /** Muestra/oculta el estado de carga del botón de autenticación. */
    private void setAuthLoading(boolean loading) {
        btnAuthSubmit.setEnabled(!loading);
        btnAuthSubmit.setAlpha(loading ? 0.6f : 1f);
        if (loading) {
            btnAuthSubmit.setText("Procesando…");
        } else {
            btnAuthSubmit.setText(isLoginMode ? R.string.btn_login : R.string.btn_register);
        }
    }

    private void updateUserGreeting() {
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

    private void showWelcomeToast(String message) {
        tvToastMessage.setText(message);
        welcomeToast.setAlpha(0f);
        welcomeToast.setTranslationY(-30f);
        welcomeToast.setVisibility(View.VISIBLE);
        welcomeToast.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(250)
                .start();

        mainHandler.postDelayed(() -> {
            welcomeToast.animate()
                    .alpha(0f)
                    .translationY(-30f)
                    .setDuration(300)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            welcomeToast.setVisibility(View.GONE);
                        }
                    })
                    .start();
        }, 2800);
    }

    // Password Evaluation Logic
    private void evaluatePassword(String val) {
        boolean len = val.length() >= 8;
        boolean upper = val.matches(".*[A-Z].*");
        boolean num = val.matches(".*[0-9].*");
        boolean sym = val.matches(".*[^A-Za-z0-9].*");

        updateCheckView(iconChkLen, textChkLen, len);
        updateCheckView(iconChkUpper, textChkUpper, upper);
        updateCheckView(iconChkNum, textChkNum, num);
        updateCheckView(iconChkSym, textChkSym, sym);

        int count = (len ? 1 : 0) + (upper ? 1 : 0) + (num ? 1 : 0) + (sym ? 1 : 0);
        int pct = val.length() == 0 ? 0 : (count * 25);

        pbPwdMeter.setProgress(pct);
        tvStrengthPct.setText(pct + "%");

        String label;
        int color;
        if (val.length() == 0) {
            label = "Esperando entrada…";
            color = ContextCompat.getColor(this, R.color.cq_text_faint);
        } else if (count <= 1) {
            label = "Muy débil";
            color = ContextCompat.getColor(this, R.color.cq_danger);
        } else if (count == 2) {
            label = "Débil";
            color = ContextCompat.getColor(this, R.color.cq_warn);
        } else if (count == 3) {
            label = "Aceptable";
            color = ContextCompat.getColor(this, R.color.cq_purple_bright);
        } else {
            label = "Fuerte";
            color = ContextCompat.getColor(this, R.color.cq_success);
        }

        tvStrengthLabel.setText(label);
        tvStrengthLabel.setTextColor(color);
    }

    private void updateCheckView(TextView icon, TextView text, boolean ok) {
        if (ok) {
            icon.setBackgroundResource(R.drawable.bg_check_circle_ok);
            icon.setText("✓");
            icon.setTextColor(ContextCompat.getColor(this, R.color.cq_success));
            text.setTextColor(ContextCompat.getColor(this, R.color.cq_text));
        } else {
            icon.setBackgroundResource(R.drawable.bg_check_circle);
            icon.setText("•");
            icon.setTextColor(ContextCompat.getColor(this, R.color.cq_text_faint));
            text.setTextColor(ContextCompat.getColor(this, R.color.cq_text_dim));
        }
    }

    // Quiz flow
    private void startQuiz() {
        if (questionBank == null) {
            Toast.makeText(this, "No se pudo cargar el banco de preguntas.", Toast.LENGTH_LONG).show();
            return;
        }

        // Se eligen 2, 2, 3 y 3 preguntas al azar, una por modulo (RF04 y RF09).
        try {
            questionList = questionBank.buildTest(currentLevel);
        } catch (IllegalStateException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            return;
        }

        currentQuestionIndex = 0;
        correctAnswersCount = 0;
        selectedOptionIndex = -1;
        renderQuestion();
        showScreen(Screen.QUESTION);
    }

    /** Ajusta la informacion que se muestra antes de comenzar el test. */
    private void updateTestIntro() {
        if (questionBank == null) {
            return;
        }
        tvTestIntroTitle.setText(questionBank.getLevelName(currentLevel));
        tvIntroCount.setText(String.valueOf(questionBank.getTestSize()));
        tvIntroDuration.setText("~" + (questionBank.getTestSize() / 2) + " min");
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
        if (questionList == null || questionList.isEmpty()) {
            showScreen(Screen.HOME);
            return;
        }

        // Cada pregunta vale 10% (RF05): 10 preguntas suman 100%.
        int percent = (int) (((float) correctAnswersCount / questionList.size()) * 100);
        int threshold = questionBank != null ? questionBank.getPassThreshold(currentLevel) : 80;
        boolean passed = percent >= threshold;

        String levelName = questionBank != null
                ? questionBank.getLevelName(currentLevel)
                : "Test Básico";

        // Guarda el resultado en la nube (HU14) y en la cache local.
        firebaseManager.saveScore(percent, currentLevel, levelName);

        // Actualiza el progreso del Home con el resultado recien obtenido.
        Integer mejorPrevio = bestScoreByLevel.get(currentLevel);
        if (mejorPrevio == null || percent > mejorPrevio) {
            bestScoreByLevel.put(currentLevel, percent);
            updateHomeStats();
        }

        // El historial se llena solo con tests realmente rendidos.
        String scoreStr = percent + "%";
        String statusStr = passed ? "Aprobado" : "No aprobado";
        resultsData.add(0, new TestResult(scoreStr, levelName,
                "Completado · Hoy · " + statusStr, !passed));
        resultsAdapter.notifyItemInserted(0);

        showTestResult(percent, passed, threshold, levelName);
        showScreen(Screen.TEST_RESULT);
    }

    /**
     * Muestra el resultado del test (HU07): puntaje obtenido, si aprobo o no
     * el nivel y el minimo requerido. No se detalla que preguntas fueron
     * correctas o incorrectas, para que el usuario aprenda por su cuenta.
     */
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

    // Dialogs / Modals
    private void showLockDialog(String toLevel, String fromLevel, int requiredPct) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_lock_info);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        }

        TextView tvTitle = dialog.findViewById(R.id.tvLockTitle);
        TextView tvMsg = dialog.findViewById(R.id.tvLockMessage);
        Button btnDismiss = dialog.findViewById(R.id.btnLockDismiss);

        tvTitle.setText("Test " + toLevel + " bloqueado");
        tvMsg.setText("Necesitas obtener al menos " + requiredPct + "% en el Test " + fromLevel + " para desbloquear el Test " + toLevel + ".");

        btnDismiss.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void showForgotPasswordDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_forgot_password);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        }

        EditText etEmail = dialog.findViewById(R.id.etForgotEmail);
        Button btnCancel = dialog.findViewById(R.id.btnForgotCancel);
        Button btnSend = dialog.findViewById(R.id.btnForgotSend);

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnSend.setOnClickListener(v -> {
            dialog.dismiss();
            showWelcomeToast("Enlace de recuperación enviado con éxito.");
        });
        dialog.show();
    }

    private void showLogoutDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_logout_confirm);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        }

        Button btnCancel = dialog.findViewById(R.id.btnLogoutCancel);
        Button btnConfirm = dialog.findViewById(R.id.btnLogoutConfirm);

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnConfirm.setOnClickListener(v -> {
            dialog.dismiss();
            firebaseManager.logout();
            userName = "";
            // Al cerrar sesion se limpia el progreso en memoria: el siguiente
            // usuario que entre vera su propio historial desde Firestore.
            resultsData.clear();
            bestScoreByLevel.clear();
            resultsAdapter.notifyDataSetChanged();
            updateHomeStats();
            etEmail.setText("");
            etPassword.setText("");
            etConfirmPassword.setText("");
            etFullName.setText("");
            setAuthMode(true);
            showScreen(Screen.AUTH);
            showWelcomeToast("Sesión cerrada correctamente.");
        });
        dialog.show();
    }

    private void showExitTestDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_exit_test_confirm);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        }

        Button btnStay = dialog.findViewById(R.id.btnStayInTest);
        Button btnExit = dialog.findViewById(R.id.btnExitTestConfirm);

        btnStay.setOnClickListener(v -> dialog.dismiss());
        btnExit.setOnClickListener(v -> {
            dialog.dismiss();
            showScreen(Screen.HOME);
        });
        dialog.show();
    }

    private void showDownloadDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_download_pdf);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        }

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
                File pdfFile = PdfReportGenerator.generateReport(MainActivity.this, name, resultsData);
                Uri pdfUri = FileProvider.getUriForFile(MainActivity.this, getPackageName() + ".fileprovider", pdfFile);

                Intent viewIntent = new Intent(Intent.ACTION_VIEW);
                viewIntent.setDataAndType(pdfUri, "application/pdf");
                viewIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                try {
                    startActivity(Intent.createChooser(viewIntent, "Abrir reporte PDF"));
                } catch (ActivityNotFoundException e) {
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("application/pdf");
                    shareIntent.putExtra(Intent.EXTRA_STREAM, pdfUri);
                    shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivity(Intent.createChooser(shareIntent, "Compartir reporte PDF"));
                }
                showWelcomeToast("PDF guardado: " + pdfFile.getName());
            } catch (Exception ex) {
                showWelcomeToast("Error al crear PDF: " + ex.getMessage());
            }
        });
        dialog.show();
    }
}
