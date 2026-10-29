package com.example.cyberquest2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Pruebas de la cadena de desbloqueo de niveles (HU08, HU09 y HU10):
 * Basico -> Intermedio (80 %), Intermedio -> Avanzado (85 %) y
 * Avanzado -> Experto (90 %).
 */
public class ProgresoTest {

    private QuestionBank banco;

    @Before
    public void cargarBancoReal() throws Exception {
        Path archivo = ubicarBanco();
        String json = new String(Files.readAllBytes(archivo), StandardCharsets.UTF_8);
        banco = QuestionBank.fromJson(json);
    }

    @Test
    public void elNivelBasicoSiempreEstaDisponible() {
        assertTrue(Progreso.estaDesbloqueado(banco, new HashMap<>(), "basico"));
        assertNull("El Basico no tiene nivel anterior", Progreso.nivelAnterior(banco, "basico"));
        assertEquals(-1, Progreso.umbralParaDesbloquear(banco, "basico"));
    }

    @Test
    public void elIntermedioSeAbreCon80EnBasico() {
        Map<String, Integer> puntajes = new HashMap<>();
        puntajes.put("basico", 79);
        assertFalse("Con 79 % todavia no se abre", Progreso.estaDesbloqueado(banco, puntajes, "intermedio"));
        puntajes.put("basico", 80);
        assertTrue("Con 80 % se abre", Progreso.estaDesbloqueado(banco, puntajes, "intermedio"));
        assertEquals("intermedio", Progreso.nivel(banco, "intermedio").getId());
        assertEquals(80, Progreso.umbralParaDesbloquear(banco, "intermedio"));
    }

    @Test
    public void elAvanzadoSeAbreCon85EnIntermedio() {
        Map<String, Integer> puntajes = new HashMap<>();
        puntajes.put("basico", 100);
        assertFalse("Aprobar el Basico no abre el Avanzado", Progreso.estaDesbloqueado(banco, puntajes, "avanzado"));
        puntajes.put("intermedio", 84);
        assertFalse(Progreso.estaDesbloqueado(banco, puntajes, "avanzado"));
        puntajes.put("intermedio", 85);
        assertTrue(Progreso.estaDesbloqueado(banco, puntajes, "avanzado"));
        assertEquals(85, Progreso.umbralParaDesbloquear(banco, "avanzado"));
    }

    @Test
    public void elExpertoSeAbreCon90EnAvanzado() {
        Map<String, Integer> puntajes = new HashMap<>();
        puntajes.put("basico", 100);
        puntajes.put("intermedio", 100);
        puntajes.put("avanzado", 89);
        assertFalse(Progreso.estaDesbloqueado(banco, puntajes, "experto"));
        puntajes.put("avanzado", 90);
        assertTrue(Progreso.estaDesbloqueado(banco, puntajes, "experto"));
        assertEquals(90, Progreso.umbralParaDesbloquear(banco, "experto"));
    }

    @Test
    public void unNivelDesconocidoNoEstaDisponible() {
        assertFalse(Progreso.estaDesbloqueado(banco, new HashMap<>(), "maestria"));
        assertFalse(Progreso.estaDesbloqueado(null, new HashMap<>(), "intermedio"));
        assertNull(Progreso.nivel(banco, "maestria"));
    }

    @Test
    public void laCadenaCompletaSeAbreAlAprobarCadaNivelJustoEnElUmbral() {
        Map<String, Integer> puntajes = new HashMap<>();
        puntajes.put("basico", 80);
        assertTrue(Progreso.estaDesbloqueado(banco, puntajes, "intermedio"));
        assertFalse(Progreso.estaDesbloqueado(banco, puntajes, "avanzado"));

        puntajes.put("intermedio", 85);
        assertTrue(Progreso.estaDesbloqueado(banco, puntajes, "avanzado"));
        assertFalse(Progreso.estaDesbloqueado(banco, puntajes, "experto"));

        puntajes.put("avanzado", 90);
        assertTrue(Progreso.estaDesbloqueado(banco, puntajes, "experto"));
    }

    private static Path ubicarBanco() {
        Path actual = Paths.get("").toAbsolutePath();
        for (Path p = actual; p != null; p = p.getParent()) {
            Path candidato = p.resolve("src/main/assets/banco_preguntas.json");
            if (Files.exists(candidato)) {
                return candidato;
            }
            candidato = p.resolve("app/src/main/assets/banco_preguntas.json");
            if (Files.exists(candidato)) {
                return candidato;
            }
        }
        throw new IllegalStateException("No se encontro banco_preguntas.json desde " + actual);
    }
}
