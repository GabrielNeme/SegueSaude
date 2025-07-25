package com.example.seguesaude;

import android.content.Context;
import android.content.SharedPreferences;

public class UsuarioUtils {

    private static final String PREFS_NAME = "loginPrefs";

    public static void salvarUsuario(Context context, String nome, String sobrenome, String email) {
        SharedPreferences.Editor editor = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
        editor.putString("nomeLogado", nome);
        editor.putString("sobrenomeLogado", sobrenome);
        editor.putString("emailLogado", email);
        editor.putBoolean("isLoggedIn", true);
        editor.apply();
    }

    public static String getNome(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getString("nomeLogado", "");
    }

    public static String getSobrenome(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getString("sobrenomeLogado", "");
    }

    public static String getEmail(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getString("emailLogado", "");
    }

    public static boolean isLogado(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getBoolean("isLoggedIn", false);
    }

    public static void limparUsuario(Context context) {
        SharedPreferences.Editor editor = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
        editor.clear();
        editor.apply();
    }
}