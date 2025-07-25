package com.example.seguesaude;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "SegueSaude.db";
    private static final int DATABASE_VERSION = 3;
    // Tabela usuários
    private static final String TABLE_USERS = "users";
    private static final String COL_USER_ID = "id";
    private static final String COL_USER_NOME = "nome";
    private static final String COL_USER_SOBRENOME = "sobrenome";
    private static final String COL_USER_EMAIL = "email";
    private static final String COL_USER_SENHA = "senha";

    private static final String TABLE_AGENDAMENTOS = "agendamentos";
    private static final String COL_AG_ID = "id";
    private static final String COL_AG_TIPO = "tipo";
    private static final String COL_AG_DATA = "data";
    private static final String COL_AG_HORARIO = "horario";
    private static final String COL_AG_LOCAL = "local";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + "("
                + COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COL_USER_NOME + " TEXT,"
                + COL_USER_SOBRENOME + " TEXT,"
                + COL_USER_EMAIL + " TEXT UNIQUE,"
                + COL_USER_SENHA + " TEXT"
                + ")";
        db.execSQL(CREATE_USERS_TABLE);

        String CREATE_AGENDAMENTOS_TABLE = "CREATE TABLE " + TABLE_AGENDAMENTOS + "("
                + COL_AG_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COL_AG_TIPO + " TEXT,"
                + "descricao TEXT,"
                + COL_AG_DATA + " TEXT,"
                + COL_AG_HORARIO + " TEXT,"
                + COL_AG_LOCAL + " TEXT,"
                + COL_USER_EMAIL + " TEXT"
                + ")";
        db.execSQL(CREATE_AGENDAMENTOS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_AGENDAMENTOS);
        onCreate(db);
    }

    public boolean inserirUsuario(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_USER_NOME, user.getNome());
        values.put(COL_USER_SOBRENOME, user.getSobrenome());
        values.put(COL_USER_EMAIL, user.getEmail());
        values.put(COL_USER_SENHA, user.getSenha());

        long result = db.insert(TABLE_USERS, null, values);
        db.close();
        return result != -1;
    }

    public boolean validarLogin(String email, String senha) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS,
                new String[]{COL_USER_ID},
                COL_USER_EMAIL + "=? AND " + COL_USER_SENHA + "=?",
                new String[]{email, senha},
                null, null, null);
        boolean exists = (cursor.getCount() > 0);
        cursor.close();
        db.close();
        return exists;
    }

    public boolean atualizarSenha(String email, String novaSenha) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_USER_SENHA, novaSenha);

        int rowsAffected = db.update(TABLE_USERS, values, COL_USER_EMAIL + "=?", new String[]{
                email});
        db.close();
        return rowsAffected > 0;
    }

    public Cursor buscarUsuarioPorEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_USERS,
                new String[]{COL_USER_NOME, COL_USER_SOBRENOME},
                COL_USER_EMAIL + "=?",
                new String[]{email},
                null, null, null);
    }

    public boolean inserirAgendamento(Agendamento agendamento) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_AG_TIPO, agendamento.getTipo());
        values.put("descricao", agendamento.getDescricao());
        values.put(COL_AG_DATA, agendamento.getData());
        values.put(COL_AG_HORARIO, agendamento.getHorario());
        values.put(COL_AG_LOCAL, agendamento.getLocal());
        values.put(COL_USER_EMAIL, agendamento.getEmail());

        long result = db.insert(TABLE_AGENDAMENTOS, null, values);
        db.close();
        return result != -1;
    }

    public Cursor listarAgendamentosPorEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM agendamentos WHERE email = ? ORDER BY id DESC", new String[]{email});    }

    public boolean atualizarAgendamento(Agendamento ag) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("tipo", ag.getTipo());
        values.put("descricao", ag.getDescricao());
        values.put("data", ag.getData());
        values.put("horario", ag.getHorario());
        values.put("local", ag.getLocal());
        values.put("email", ag.getEmail());

        int linhasAfetadas = db.update("agendamentos", values, "id = ?", new String[]{
                String.valueOf(ag.getId())});
        db.close();
        return linhasAfetadas > 0;
    }

    public boolean deletarAgendamento(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete("agendamentos", "id = ?", new String[]{String.valueOf(id)});
        return result > 0;
    }
}