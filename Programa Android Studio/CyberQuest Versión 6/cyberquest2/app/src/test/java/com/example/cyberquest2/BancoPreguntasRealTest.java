package com.example.cyberquest2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

/**
 * Pruebas unitarias contra el banco de preguntas real que la app lleva en
 * {@code assets/banco_preguntas.json}.
 */
public class BancoPreguntasRealTest {

    private QuestionBank banco;

    @Before
    public void cargarBancoReal() throws Exception {
        Path archivo = ubicarBanco();
        String json = new String(Files.readAllBytes(archivo), StandardCharsets.UTF_8);
        banco = QuestionBank.fromJson(json);
    }

    @Test
    public void elBancoRealTieneCuarentaPreguntasPorNivelYCuatroNiveles() {
        assertEquals("El banco debe tener 160 preguntas cargadas", 160, banco.getAllQuestions().size());
        assertEquals("El banco debe declarar 4 niveles", 4, banco.getLevels().size());
        assertEquals(1, banco.getVersion());
    }

    @Test
    public void elBancoRealDefineLosUmbralesDeAprobacion() {
        assertEquals(80, banco.getPassThreshold("basico"));
        assertEquals(85, banco.getPassThreshold("intermedio"));
        assertEquals(90, banco.getPassThreshold("avanzado"));
        assertEquals(90, banco.getPassThreshold("experto"));
    }

    @Test
    public void elBancoRealDefineLosCuatroModulosConCuotas2y2y3y3() {
        assertEquals(4, banco.getModules().size());
        assertEquals(2, banco.getModules().get(0).getQuota());
        assertEquals(2, banco.getModules().get(1).getQuota());
        assertEquals(3, banco.getModules().get(2).getQuota());
        assertEquals(3, banco.getModules().get(3).getQuota());
        assertEquals(10, banco.getTestSize());
    }

    @Test
    public void elBancoRealTienePreguntasSuficientesParaLaCuotaDeCadaModulo() {
        for (QuestionBank.Level nivel : banco.getLevels()) {
            for (QuestionBank.Module modulo : banco.getModules()) {
                int disponibles = banco.countByModule(nivel.getId(), modulo.getId());
                assertTrue("El nivel " + nivel.getId() + " y el modulo " + modulo.getShortName()
                                + " tienen " + disponibles + " preguntas y necesita " + modulo.getQuota(),
                        disponibles >= modulo.getQuota());
            }
        }
    }

    @Test
    public void elBancoRealPermiteArmarElTestBasicoDe10Preguntas() {
        Set<String> ids = new HashSet<>();
        for (Question q : banco.buildTest("basico")) {
            ids.add(q.getId());
            assertEquals("basico", q.getLevel());
            assertTrue(q.getCorrectOptionIndex() >= 0 && q.getCorrectOptionIndex() < q.getOptions().size());
        }
        assertEquals(10, ids.size());
    }

    @Test
    public void losCuatroNivelesArmanUnTestDe10Preguntas() {
        for (QuestionBank.Level nivel : banco.getLevels()) {
            Set<String> ids = new HashSet<>();
            for (Question q : banco.buildTest(nivel.getId())) {
                ids.add(q.getId());
                assertEquals(nivel.getId(), q.getLevel());
                assertTrue(q.getCorrectOptionIndex() >= 0
                        && q.getCorrectOptionIndex() < q.getOptions().size());
            }
            assertEquals("El test de " + nivel.getId() + " debe traer 10 preguntas distintas",
                    10, ids.size());
        }
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
