package com.example.cyberquest2;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/** Historial de resultados de los tests y descarga del reporte en PDF. */
public class resultados extends AppCompatActivity {

    RecyclerView rvResults;
    private ResultsAdapter resultsAdapter;
    private FirebaseManager firebaseManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resultados);

        firebaseManager = new FirebaseManager(this);
        Utilidades.marcarNav(this, false);

        rvResults = findViewById(R.id.rvResults);
        rvResults.setLayoutManager(new LinearLayoutManager(this));
        resultsAdapter = new ResultsAdapter(Utilidades.listaResultados);
        rvResults.setAdapter(resultsAdapter);

        findViewById(R.id.btnBackFromResults).setOnClickListener(v -> finish());
        findViewById(R.id.btnDownloadResults).setOnClickListener(v ->
                Utilidades.mostrarDialogoDescarga(this, firebaseManager));

        // Barra de navegación inferior
        findViewById(R.id.tabNavHome).setOnClickListener(v -> finish());
        findViewById(R.id.tabNavLogout).setOnClickListener(v ->
                Utilidades.mostrarDialogoCerrarSesion(this, firebaseManager));

        String mensaje = getIntent().getStringExtra("mensaje");
        if (mensaje != null) {
            Utilidades.mostrarBanner(this, mensaje);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        resultsAdapter.notifyDataSetChanged();
        Utilidades.alActualizar = () -> resultsAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Utilidades.alActualizar = null;
    }
}
