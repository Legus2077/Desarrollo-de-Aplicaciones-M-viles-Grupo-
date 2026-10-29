package com.example.cyberquest2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Pruebas unitarias de {@link QuestionBank}: carga del banco, cuotas por
 * modulo, umbrales de aprobacion y armado aleatorio del test de 10 preguntas.
 */
public class QuestionBankTest {

    private static final String NIVEL = "basico";

    private QuestionBank banco;

    @Before
    public void cargarBancoDePrueba() throws Exception {
        banco = QuestionBank.fromJson(bancoDePrueba(4));
    }

    // ------------------------------------------------------------------
    // Carga del banco
    // ------------------------------------------------------------------

    @Test
    public void fromJsonLeeLaVersionLosModulosYLasCuotas() {
        assertEquals("La version del banco debe leerse del JSON", 7, banco.getVersion());
        assertEquals("El banco debe definir 4 modulos", 4, banco.getModules().size());
        assertEquals(2, banco.getModules().get(0).getQuota());
        assertEquals(2, banco.getModules().get(1).getQuota());
        assertEquals(3, banco.getModules().get(2).getQuota());
        assertEquals(3, banco.getModules().get(3).getQuota());
    }

    @Test
    public void getTestSizeSumaLasCuotasDeTodosLosModulos() {
        assertEquals("2 + 2 + 3 + 3 = 10 preguntas por test", 10, banco.getTestSize());
    }

    @Test
    public void fromJsonLeeLosCuatroNivelesOrdenadosConSuUmbral() {
        List<QuestionBank.Level> niveles = banco.getLevels();
        assertEquals(4, niveles.size());
        assertEquals("basico", niveles.get(0).getId());
        assertEquals("intermedio", niveles.get(1).getId());
        assertEquals("avanzado", niveles.get(2).getId());
        assertEquals("experto", niveles.get(3).getId());
        assertEquals(80, niveles.get(0).getThreshold());
        assertEquals(85, niveles.get(1).getThreshold());
        assertEquals(90, niveles.get(2).getThreshold());
        assertEquals(90, niveles.get(3).getThreshold());
    }

    @Test
    public void getLevelNameDevuelveElNombreVisibleDelNivel() {
        assertEquals("Test B\u00e1sico", banco.getLevelName("basico"));
    }

    @Test
    public void getPassThresholdRespetaElUmbralDeCadaNivel() {
        assertEquals(80, banco.getPassThreshold("basico"));
        assertEquals(85, banco.getPassThreshold("intermedio"));
        assertEquals(90, banco.getPassThreshold("avanzado"));
        assertEquals(90, banco.getPassThreshold("experto"));
    }

    @Test
    public void getPassThresholdDevuelve80CuandoElNivelNoExiste() {
        assertEquals("Un nivel desconocido usa el umbral por defecto", 80,
                banco.getPassThreshold("nivel-inexistente"));
    }

    @Test
    public void fromJsonToleraUnBancoMinimoSinModulosNiNiveles() throws Exception {
        QuestionBank minimo = QuestionBank.fromJson("{\"version\":1,\"preguntas\":[]}");
        assertEquals(0, minimo.getAllQuestions().size());
        assertEquals(0, minimo.getModules().size());
        assertEquals(80, minimo.getPassThreshold("basico"));
        assertEquals("sin niveles definidos se muestra el id", "basico", minimo.getLevelName("basico"));
    }

    // ------------------------------------------------------------------
    // Armado del test (RF09)
    // ------------------------------------------------------------------

    @Test
    public void buildTestDevuelve10Preguntas() {
        assertEquals(10, banco.buildTest(NIVEL).size());
    }

    @Test
    public void buildTestSoloUsaPreguntasDelNivelPedido() {
        for (Question q : banco.buildTest(NIVEL)) {
            assertEquals(NIVEL, q.getLevel());
        }
    }

    @Test
    public void buildTestRespetaLaCuotaDeCadaModulo() {
        Map<Integer, Integer> porModulo = new HashMap<>();
        for (Question q : banco.buildTest(NIVEL)) {
            Integer previo = porModulo.get(q.getModuleId());
            porModulo.put(q.getModuleId(), previo == null ? 1 : previo + 1);
        }
        assertEquals(4, porModulo.size());
        assertEquals(2, (int) porModulo.get(1));
        assertEquals(2, (int) porModulo.get(2));
        assertEquals(3, (int) porModulo.get(3));
        assertEquals(3, (int) porModulo.get(4));
    }

    @Test
    public void buildTestNoRepitePreguntasEnElMismoIntento() {
        List<Question> test = banco.buildTest(NIVEL);
        Set<String> ids = new HashSet<>();
        for (Question q : test) {
            assertTrue("La pregunta " + q.getId() + " aparece dos veces en el mismo test",
                    ids.add(q.getId()));
        }
        assertEquals(10, ids.size());
    }

    @Test
    public void buildTestMezclaOpcionesSinPerderLaRespuestaCorrecta() {
        for (Question q : banco.buildTest(NIVEL)) {
            List<String> opciones = q.getOptions();
            assertTrue("La opcion correcta apunta fuera de la lista", q.getCorrectOptionIndex() >= 0);
            assertTrue(q.getCorrectOptionIndex() < opciones.size());
            assertEquals("La opcion marcada como correcta no es la del banco",
                    "C-" + q.getId(), opciones.get(q.getCorrectOptionIndex()));
        }
    }

    @Test
    public void buildTestMezclaElOrdenDeLasOpciones() {
        boolean huboReordenamiento = false;
        for (int intento = 0; intento < 50 && !huboReordenamiento; intento++) {
            for (Question q : banco.buildTest(NIVEL)) {
                if (q.getCorrectOptionIndex() != 2) {
                    huboReordenamiento = true;
                    break;
                }
            }
        }
        assertTrue("Las opciones nunca se reordenaron en 50 intentos", huboReordenamiento);
    }

    @Test
    public void buildTestGeneraSecuenciasDistintasEnCadaIntento() {
        Set<String> secuencias = new HashSet<>();
        for (int intento = 0; intento < 20; intento++) {
            StringBuilder firma = new StringBuilder();
            for (Question q : banco.buildTest(NIVEL)) {
                firma.append(q.getId()).append('|');
            }
            secuencias.add(firma.toString());
        }
        assertTrue("20 tests deberian generar mas de una secuencia distinta", secuencias.size() > 1);
    }

    @Test
    public void buildTestLanzaErrorCuandoElNivelNoTienePreguntasSuficientes() {
        try {
            banco.buildTest("intermedio");
            fail("Se esperaba IllegalStateException: el nivel no tiene preguntas cargadas");
        } catch (IllegalStateException esperado) {
            assertTrue(esperado.getMessage().contains("necesita"));
        }
    }

    @Test
    public void buildTestDevuelveListaVaciaSiElBancoNoDefineModulos() throws Exception {
        QuestionBank sinModulos = QuestionBank.fromJson("{\"version\":1,\"preguntas\":[]}");
        assertEquals(0, sinModulos.buildTest(NIVEL).size());
    }

    // ------------------------------------------------------------------
    // Consultas
    // ------------------------------------------------------------------

    @Test
    public void countByModuleCuentaLasPreguntasDisponibles() {
        assertEquals(4, banco.countByModule(NIVEL, 1));
        assertEquals(4, banco.countByModule(NIVEL, 4));
        assertEquals(0, banco.countByModule("intermedio", 1));
    }

    @Test
    public void lasPreguntasConservanEscenarioYExplicacion() {
        List<Question> todas = banco.getAllQuestions();
        assertFalse(todas.isEmpty());
        boolean hayEscenario = false;
        for (Question q : todas) {
            assertNotNull("Toda pregunta debe tener explicacion", q.getExplanation());
            if (q.hasScenario()) {
                hayEscenario = true;
            }
        }
        assertTrue("El banco de prueba incluye preguntas con escenario", hayEscenario);
    }

    @Test
    public void elNombreDelModuloSeResuelveDesdeElBanco() {
        for (Question q : banco.getAllQuestions()) {
            assertEquals("M" + q.getModuleId(), q.getModuleName());
        }
    }

    // ------------------------------------------------------------------
    // Fixture
    // ------------------------------------------------------------------

    /** Banco de prueba: 4 modulos (cuotas 2, 2, 3 y 3) con preguntas "basico". */
    private static String bancoDePrueba(int preguntasPorModulo) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"version\":7,\"modulos\":[");
        for (int m = 1; m <= 4; m++) {
            if (m > 1) {
                sb.append(',');
            }
            sb.append("{\"id\":").append(m)
              .append(",\"nombre\":\"Modulo ").append(m)
              .append("\",\"nombreCorto\":\"M").append(m)
              .append("\",\"cuota\":").append(m <= 2 ? 2 : 3).append('}');
        }
        sb.append("],\"niveles\":[");
        String[] ids = {"basico", "intermedio", "avanzado", "experto"};
        String[] nombres = {"Test B\u00e1sico", "Test Intermedio", "Test Avanzado", "Test Experto"};
        int[] umbrales = {80, 85, 90, 90};
        for (int i = 0; i < ids.length; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append("{\"id\":\"").append(ids[i])
              .append("\",\"nombre\":\"").append(nombres[i])
              .append("\",\"orden\":").append(i + 1)
              .append(",\"umbral\":").append(umbrales[i]).append('}');
        }
        sb.append("],\"preguntas\":[");
        boolean primero = true;
        for (int modulo = 1; modulo <= 4; modulo++) {
            for (int n = 1; n <= preguntasPorModulo; n++) {
                String id = "B" + modulo + "-" + n;
                if (!primero) {
                    sb.append(',');
                }
                primero = false;
                sb.append("{\"id\":\"").append(id)
                  .append("\",\"nivel\":\"").append(NIVEL)
                  .append("\",\"modulo\":").append(modulo)
                  .append(",\"escenario\":").append(n % 2 == 0 ? "\"Escenario " + id + "\"" : "null")
                  .append(",\"pregunta\":\"Pregunta ").append(id).append("\"")
                  .append(",\"opciones\":[\"A-").append(id).append("\",\"B-").append(id)
                  .append("\",\"C-").append(id).append("\",\"D-").append(id).append("\"]")
                  .append(",\"correcta\":2")
                  .append(",\"explicacion\":\"Explicacion ").append(id).append("\"}");
            }
        }
        sb.append("]}");
        return sb.toString();
    }
}
