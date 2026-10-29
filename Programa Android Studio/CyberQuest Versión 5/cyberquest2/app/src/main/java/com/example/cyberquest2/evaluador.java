package com.example.cyberquest2;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

/** Evaluador de fortaleza de contraseñas. */
public class evaluador extends AppCompatActivity {

    EditText etPwdTest;
    ProgressBar pbPwdMeter;
    TextView tvStrengthLabel, tvStrengthPct;
    TextView iconChkLen, textChkLen, iconChkUpper, textChkUpper;
    TextView iconChkNum, textChkNum, iconChkSym, textChkSym;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_evaluador);

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

        findViewById(R.id.btnBackFromPwd).setOnClickListener(v -> finish());

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

        evaluatePassword("");
    }

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
}
