package com.example.cyberquest2;

import java.util.List;
import java.util.Map;

/**
 * Reglas de desbloqueo progresivo de los niveles (HU08, HU09 y HU10).
 *
 * <p>Un nivel se abre cuando el mejor puntaje del nivel anterior alcanza su
 * umbral: Basico -> Intermedio con 80 %, Intermedio -> Avanzado con 85 % y
 * Avanzado -> Experto con 90 %. El nivel Basico siempre esta disponible.
 */
public final class Progreso {

    private Progreso() {
    }

    /** Devuelve el nivel con ese id, o {@code null} si no existe. */
    public static QuestionBank.Level nivel(QuestionBank banco, String id) {
        if (banco == null || id == null) {
            return null;
        }
        for (QuestionBank.Level level : banco.getLevels()) {
            if (level.getId().equals(id)) {
                return level;
            }
        }
        return null;
    }

    /** Nivel inmediatamente anterior en el orden del banco, o {@code null}. */
    public static QuestionBank.Level nivelAnterior(QuestionBank banco, String id) {
        QuestionBank.Level actual = nivel(banco, id);
        if (actual == null || banco == null) {
            return null;
        }
        List<QuestionBank.Level> levels = banco.getLevels();
        int indice = levels.indexOf(actual);
        if (indice <= 0) {
            return null;
        }
        return levels.get(indice - 1);
    }

    /**
     * Porcentaje necesario en el nivel anterior para abrir este nivel.
     * Devuelve -1 cuando el nivel no existe o es el primero.
     */
    public static int umbralParaDesbloquear(QuestionBank banco, String id) {
        QuestionBank.Level anterior = nivelAnterior(banco, id);
        return anterior != null ? anterior.getThreshold() : -1;
    }

    /**
     * Indica si el nivel esta abierto para el usuario.
     *
     * @param mejorPuntaje mejor porcentaje obtenido por nivel (id -> %).
     */
    public static boolean estaDesbloqueado(QuestionBank banco,
                                           Map<String, Integer> mejorPuntaje,
                                           String id) {
        if (nivel(banco, id) == null) {
            return false;
        }
        QuestionBank.Level anterior = nivelAnterior(banco, id);
        if (anterior == null) {
            return true; // el primer nivel siempre esta abierto
        }
        Integer mejor = mejorPuntaje != null ? mejorPuntaje.get(anterior.getId()) : null;
        return mejor != null && mejor >= anterior.getThreshold();
    }
}
