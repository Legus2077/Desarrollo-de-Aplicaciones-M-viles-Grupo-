package com.example.cyberquest2;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

/** Pantalla de inicio de sesion y registro (Firebase Auth). */
public class MainActivity extends AppCompatActivity {

    TextView tvAuthTitle, tvAuthSub, tabLogin, tabRegister, tvForgotPassword, tvAuthFoot;
    LinearLayout layoutFieldName, layoutFieldConfirm;
    EditText etFullName, etEmail, etPassword, etConfirmPassword;
    Button btnAuthSubmit;

    private boolean isLoginMode = true;
    private FirebaseManager firebaseManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseManager = new FirebaseManager(this);

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

        tabLogin.setOnClickListener(v -> setAuthMode(true));
        tabRegister.setOnClickListener(v -> setAuthMode(false));
        tvAuthFoot.setOnClickListener(v -> setAuthMode(!isLoginMode));
        tvForgotPassword.setOnClickListener(v -> showForgotPasswordDialog());
        btnAuthSubmit.setOnClickListener(v -> enviar());

        String mensaje = getIntent().getStringExtra("mensaje");
        if (mensaje != null) {
            Utilidades.mostrarBanner(this, mensaje);
        }
    }

    private void enviar() {
        String email = etEmail.getText().toString().trim();
        String pwd = etPassword.getText().toString();

        if (isLoginMode) {
            setAuthLoading(true);
            firebaseManager.signIn(email, pwd, new FirebaseManager.AuthCallback() {
                @Override
                public void onSuccess(String name, String email) {
                    setAuthLoading(false);
                    String userName = TextUtils.isEmpty(name) ? FirebaseManager.nameFromEmail(email) : name;
                    Utilidades.cargarResultadosUsuario(firebaseManager);
                    irAInicio("¡Bienvenido de nuevo, " + userName + "!");
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
                    Utilidades.cargarResultadosUsuario(firebaseManager);
                    irAInicio("¡Cuenta creada! Bienvenido, " + name);
                }

                @Override
                public void onError(String errorMessage) {
                    setAuthLoading(false);
                    Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void irAInicio(String mensaje) {
        Intent i = new Intent(getApplicationContext(), inicio.class);
        i.putExtra("mensaje", mensaje);
        startActivity(i);
        finish();
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

    private void showForgotPasswordDialog() {
        Dialog dialog = Utilidades.crearDialogo(this, R.layout.dialog_forgot_password);
        Button btnCancel = dialog.findViewById(R.id.btnForgotCancel);
        Button btnSend = dialog.findViewById(R.id.btnForgotSend);

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnSend.setOnClickListener(v -> {
            dialog.dismiss();
            Utilidades.mostrarBanner(this, "Enlace de recuperación enviado con éxito.");
        });
        dialog.show();
    }
}
