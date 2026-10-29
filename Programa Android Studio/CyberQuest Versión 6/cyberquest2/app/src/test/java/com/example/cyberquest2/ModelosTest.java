package com.example.cyberquest2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Pruebas unitarias de los modelos del dominio. */
public class ModelosTest {

    @Test
    public void questionExponeTodosSusDatos() {
        Question q = new Question("B1-01", "basico", 1, "Seguridad de Dispositivos",
                "Escenario de prueba", "Pregunta de prueba",
                Arrays.asList("uno", "dos", "tres"), 1, "Explicacion de prueba");

        assertEquals("B1-01", q.getId());
        assertEquals("basico", q.getLevel());
        assertEquals(1, q.getModuleId());
        assertEquals("Seguridad de Dispositivos", q.getModuleName());
        assertEquals("Escenario de prueba", q.getScenario());
        assertEquals("Pregunta de prueba", q.getText());
        assertEquals(3, q.getOptions().size());
        assertEquals(1, q.getCorrectOptionIndex());
        assertEquals("Explicacion de prueba", q.getExplanation());
    }

    @Test
    public void questionHasScenarioIndicaSiTieneEscenario() {
        Question conEscenario = new Question("B1-01", "basico", 1, "M1", "Escenario",
                "Pregunta", Arrays.asList("a", "b"), 0, "Explicacion");
        Question sinEscenario = new Question("B1-02", "basico", 1, "M1", null,
                "Pregunta", Arrays.asList("a", "b"), 0, "Explicacion");

        assertTrue(conEscenario.hasScenario());
        assertFalse(sinEscenario.hasScenario());
    }

    @Test
    public void questionWithOptionsCambiaLasOpcionesSinTocarElResto() {
        Question original = new Question("B1-01", "basico", 2, "M2", "Escenario",
                "Pregunta", Arrays.asList("uno", "dos", "tres"), 0, "Explicacion");

        List<String> nuevas = new ArrayList<>(Arrays.asList("tres", "uno", "dos"));
        Question mezclada = original.withOptions(nuevas, 1);

        assertNotSame("withOptions debe devolver una copia", original, mezclada);
        assertEquals("tres", mezclada.getOptions().get(0));
        assertEquals(1, mezclada.getCorrectOptionIndex());
        assertEquals(original.getId(), mezclada.getId());
        assertEquals(original.getText(), mezclada.getText());
        assertEquals(original.getExplanation(), mezclada.getExplanation());
        assertEquals(original.getModuleName(), mezclada.getModuleName());
    }

    @Test
    public void testResultGuardaElPuntajeYLaBanderaDePuntajeBajo() {
        TestResult aprobado = new TestResult("90%", "Aprobado", "Test Basico - minimo 80%", false);
        TestResult desaprobado = new TestResult("20%", "No aprobado", "Test Basico - minimo 80%", true);

        assertEquals("90%", aprobado.getScore());
        assertEquals("Aprobado", aprobado.getTitle());
        assertFalse(aprobado.isLowScore());
        assertTrue(desaprobado.isLowScore());
    }
}
