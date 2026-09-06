package com.example.s03_condori;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import EMPRESA.ANDROID.DAO.ProductoDAO;
import EMPRESAANDROIDBEAN.ProductoBean;

public class MainActivity extends AppCompatActivity {

    private Spinner spinnerMarca;
    private Spinner spinnerTalla;
    private EditText editPares;
    private Button btnCalcular;
    private TextView txtResultado;

    private ProductoBean objProductoBean;
    private ProductoDAO objProductoDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main),
                (v, insets) -> {

                    Insets systemBars =
                            insets.getInsets(
                                    WindowInsetsCompat.Type.systemBars()
                            );

                    v.setPadding(
                            systemBars.left,
                            systemBars.top,
                            systemBars.right,
                            systemBars.bottom
                    );

                    return insets;
                }
        );

        spinnerMarca = findViewById(R.id.spinnerMarca);
        spinnerTalla = findViewById(R.id.spinnerTalla);
        editPares = findViewById(R.id.editPares);
        btnCalcular = findViewById(R.id.btnCalcular);
        txtResultado = findViewById(R.id.txtResultado);

        btnCalcular.setOnClickListener(v -> Calcular());
    }

    private void Calcular() {

        String numparesTexto = editPares.getText().toString().trim();

        if (numparesTexto.isEmpty()) {

            txtResultado.setText(
                    "Ingresa el número de pares vendidos"
            );

            return;
        }

        int numpares = Integer.parseInt(numparesTexto);

        if (numpares <= 0) {

            txtResultado.setText(
                    "El número de pares debe ser mayor que 0"
            );

            return;
        }

        int marca = spinnerMarca.getSelectedItemPosition();
        int talla = spinnerTalla.getSelectedItemPosition();

        objProductoBean = new ProductoBean();

        objProductoBean.setMarca(marca);
        objProductoBean.setTalla(talla);
        objProductoBean.setNumpares(numpares);

        objProductoDAO = new ProductoDAO();

        String resultado =
                objProductoDAO.CalcularOperacion(objProductoBean);

        String marcaTexto =
                spinnerMarca.getSelectedItem().toString();

        String tallaTexto =
                spinnerTalla.getSelectedItem().toString();

        String mensaje =
                "Marca: " + marcaTexto + "\n" +
                        "Talla: " + tallaTexto + "\n" +
                        "Pares vendidos: " + numpares + "\n\n" +
                        resultado;

        txtResultado.setText(mensaje);
    }
}

