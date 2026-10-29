package com.example.cyberquest2;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Patterns;

import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Gestiona la autenticación con Firebase (Authentication) y el guardado de
 * resultados en Cloud Firestore.
 */
public class FirebaseManager {

    private static final String PREF_NAME = "cyberquest_prefs";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_LAST_SCORE = "last_score";

    /** Longitud mínima que exige Firebase para la contraseña. */
    public static final int MIN_PASSWORD_LENGTH = 6;

    private final SharedPreferences prefs;
    private final FirebaseAuth auth;
    private final FirebaseFirestore firestore;
    private final boolean isFirebaseReady;

    public interface AuthCallback {
        void onSuccess(String userName, String email);
        void onError(String errorMessage);
    }

    /** Callback para la lectura del historial de resultados (HU12 / HU07). */
    public interface ResultsCallback {
        void onLoaded(List<ScoreRecord> results);
        void onError(String errorMessage);
    }

    /** Callback para el envio del enlace de restablecimiento de contraseña. */
    public interface ResetCallback {
        void onSuccess(String email);
        void onError(String errorMessage);
    }

    /** Un resultado guardado en Firestore. */
    public static class ScoreRecord {
        private final String levelId;
        private final String levelName;
        private final int score;
        private final long timestamp;

        public ScoreRecord(String levelId, String levelName, int score, long timestamp) {
            this.levelId = levelId;
            this.levelName = levelName;
            this.score = score;
            this.timestamp = timestamp;
        }

        /** Identificador del nivel ("basico", "intermedio", ...). Puede ser null. */
        public String getLevelId() {
            return levelId;
        }

        public String getLevelName() {
            return levelName;
        }

        public int getScore() {
            return score;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }

    public FirebaseManager(Context context) {
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        FirebaseAuth authInstance = null;
        FirebaseFirestore firestoreInstance = null;
        try {
            authInstance = FirebaseAuth.getInstance();
            firestoreInstance = FirebaseFirestore.getInstance();
        } catch (Exception e) {
            // Ocurre si google-services.json no está configurado correctamente.
            authInstance = null;
            firestoreInstance = null;
        }
        this.auth = authInstance;
        this.firestore = firestoreInstance;
        this.isFirebaseReady = (authInstance != null && firestoreInstance != null);
    }

    public boolean isFirebaseReady() {
        return isFirebaseReady;
    }

    public boolean isLoggedIn() {
        return isFirebaseReady && auth.getCurrentUser() != null;
    }

    public String getSavedUserName() {
        return prefs.getString(KEY_USER_NAME, "");
    }

    public String getSavedUserEmail() {
        return prefs.getString(KEY_USER_EMAIL, "");
    }

    public void saveUserLocally(String name, String email) {
        prefs.edit()
                .putString(KEY_USER_NAME, name)
                .putString(KEY_USER_EMAIL, email)
                .apply();
    }

    /** Limpia los datos de sesión guardados localmente (al cerrar sesión). */
    public void clearLocalSession() {
        prefs.edit()
                .remove(KEY_USER_NAME)
                .remove(KEY_USER_EMAIL)
                .apply();
    }

    /** Valida el formato del correo electrónico. */
    public static boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches();
    }

    /** Nombre por defecto cuando Firebase no tiene displayName. */
    public static String nameFromEmail(String email) {
        if (TextUtils.isEmpty(email)) {
            return "";
        }
        String value = email.trim();
        int at = value.indexOf('@');
        return at > 0 ? value.substring(0, at) : value;
    }

    public void saveScore(int score, String levelName) {
        saveScore(score, null, levelName);
    }

    public void saveScore(int score, String levelId, String levelName) {
        prefs.edit().putInt(KEY_LAST_SCORE, score).apply();

        if (!isFirebaseReady || auth.getCurrentUser() == null || firestore == null) {
            return;
        }
        String uid = auth.getCurrentUser().getUid();
        Map<String, Object> data = new HashMap<>();
        data.put("level", levelId);
        data.put("testName", levelName);
        data.put("score", score);
        data.put("timestamp", System.currentTimeMillis());
        firestore.collection("users")
                .document(uid)
                .collection("results")
                .add(data);
    }

    /**
     * Lee los resultados del usuario desde Cloud Firestore, del mas reciente
     * al mas antiguo. Se usa para reconstruir el progreso y el historial
     * cuando el usuario vuelve a iniciar sesion (o entra desde otro equipo).
     */
    public void loadResults(final ResultsCallback callback) {
        if (!isFirebaseReady || auth.getCurrentUser() == null || firestore == null) {
            callback.onError("No hay sesion de Firebase activa.");
            return;
        }
        String uid = auth.getCurrentUser().getUid();
        firestore.collection("users")
                .document(uid)
                .collection("results")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(50)
                .get()
                .addOnSuccessListener(new com.google.android.gms.tasks.OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot snapshot) {
                        List<ScoreRecord> results = new ArrayList<>();
                        for (DocumentSnapshot doc : snapshot.getDocuments()) {
                            Integer score = doc.getLong("score") == null
                                    ? null : doc.getLong("score").intValue();
                            if (score == null) {
                                continue;
                            }
                            Long stamp = doc.getLong("timestamp");
                            results.add(new ScoreRecord(
                                    doc.getString("level"),
                                    doc.getString("testName"),
                                    score,
                                    stamp == null ? 0L : stamp));
                        }
                        Collections.sort(results, new Comparator<ScoreRecord>() {
                            @Override
                            public int compare(ScoreRecord a, ScoreRecord b) {
                                return Long.compare(b.getTimestamp(), a.getTimestamp());
                            }
                        });
                        callback.onLoaded(results);
                    }
                })
                .addOnFailureListener(new com.google.android.gms.tasks.OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        callback.onError("No se pudo leer el historial: " + e.getMessage());
                    }
                });
    }

    public void signIn(String email, String password, AuthCallback callback) {
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            callback.onError("Por favor completa todos los campos.");
            return;
        }
        if (!isValidEmail(email)) {
            callback.onError("Ingresa un correo electrónico válido.");
            return;
        }
        if (!isFirebaseReady) {
            callback.onError("No se pudo conectar con Firebase. Revisa la configuración de la app.");
            return;
        }

        final String cleanEmail = email.trim();
        auth.signInWithEmailAndPassword(cleanEmail, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = authResult.getUser();
                    String name = (user != null && !TextUtils.isEmpty(user.getDisplayName()))
                            ? user.getDisplayName()
                            : nameFromEmail(cleanEmail);
                    saveUserLocally(name, cleanEmail);
                    callback.onSuccess(name, cleanEmail);
                })
                .addOnFailureListener(e -> callback.onError(mapAuthError(e, false)));
    }

    public void register(String name, String email, String password, AuthCallback callback) {
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            callback.onError("Por favor completa todos los campos.");
            return;
        }
        if (!isValidEmail(email)) {
            callback.onError("Ingresa un correo electrónico válido.");
            return;
        }
        if (password.length() < MIN_PASSWORD_LENGTH) {
            callback.onError("La contraseña debe tener al menos " + MIN_PASSWORD_LENGTH + " caracteres.");
            return;
        }
        if (!isFirebaseReady) {
            callback.onError("No se pudo conectar con Firebase. Revisa la configuración de la app.");
            return;
        }

        final String cleanName = name.trim();
        final String cleanEmail = email.trim();

        auth.createUserWithEmailAndPassword(cleanEmail, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = authResult.getUser();
                    if (user != null) {
                        // Guarda el nombre en el perfil de Firebase Auth.
                        UserProfileChangeRequest profile = new UserProfileChangeRequest.Builder()
                                .setDisplayName(cleanName)
                                .build();
                        user.updateProfile(profile);

                        // Guarda el perfil en Firestore (users/{uid}).
                        if (firestore != null) {
                            Map<String, Object> data = new HashMap<>();
                            data.put("displayName", cleanName);
                            data.put("email", cleanEmail);
                            data.put("createdAt", System.currentTimeMillis());
                            firestore.collection("users").document(user.getUid())
                                    .set(data)
                                    .addOnFailureListener(e -> { /* el perfil es opcional */ });
                        }
                    }
                    saveUserLocally(cleanName, cleanEmail);
                    callback.onSuccess(cleanName, cleanEmail);
                })
                .addOnFailureListener(e -> callback.onError(mapAuthError(e, true)));
    }

    /**
     * Envia a Firebase el correo con el enlace para restablecer la contraseña.
     * El mensaje lo manda Firebase Auth; la app solo recibe el resultado.
     */
    public void sendPasswordReset(String email, ResetCallback callback) {
        if (TextUtils.isEmpty(email)) {
            callback.onError("Ingresa tu correo electrónico.");
            return;
        }
        if (!isValidEmail(email)) {
            callback.onError("Ingresa un correo electrónico válido.");
            return;
        }
        if (!isFirebaseReady || auth == null) {
            callback.onError("No se pudo conectar con Firebase. Revisa la configuración de la app.");
            return;
        }

        final String cleanEmail = email.trim();
        auth.sendPasswordResetEmail(cleanEmail)
                .addOnSuccessListener(unused -> callback.onSuccess(cleanEmail))
                .addOnFailureListener(e -> callback.onError(mapResetError(e)));
    }

    public void logout() {
        if (isFirebaseReady && auth != null) {
            auth.signOut();
        }
        clearLocalSession();
    }

    /** Traduce los errores de Firebase Auth a mensajes entendibles. */
    private String mapAuthError(Exception e, boolean registering) {
        if (e instanceof FirebaseAuthWeakPasswordException) {
            return "La contraseña es muy débil. Usa al menos " + MIN_PASSWORD_LENGTH + " caracteres.";
        }
        if (e instanceof FirebaseAuthUserCollisionException) {
            return "Ese correo ya está registrado. Inicia sesión.";
        }
        if (e instanceof FirebaseAuthInvalidCredentialsException) {
            return "Correo o contraseña incorrectos.";
        }
        if (e instanceof FirebaseAuthInvalidUserException) {
            return "No existe una cuenta con ese correo. Regístrate primero.";
        }
        if (e instanceof FirebaseNetworkException) {
            return "Sin conexión a Internet. Verifica tu red e inténtalo de nuevo.";
        }
        return registering
                ? "No se pudo crear la cuenta. Inténtalo de nuevo."
                : "No se pudo iniciar sesión. Inténtalo de nuevo.";
    }

    /** Traduce los errores del envio del enlace de restablecimiento. */
    private String mapResetError(Exception e) {
        if (e instanceof FirebaseAuthInvalidUserException) {
            return "No existe una cuenta con ese correo. Regístrate primero.";
        }
        if (e instanceof FirebaseNetworkException) {
            return "Sin conexión a Internet. Verifica tu red e inténtalo de nuevo.";
        }
        if (e instanceof FirebaseAuthException) {
            String code = ((FirebaseAuthException) e).getErrorCode();
            if ("ERROR_TOO_MANY_REQUESTS".equals(code)) {
                return "Hiciste demasiados intentos. Espera unos minutos y vuelve a probar.";
            }
        }
        return "No se pudo enviar el enlace. Inténtalo de nuevo.";
    }
}
