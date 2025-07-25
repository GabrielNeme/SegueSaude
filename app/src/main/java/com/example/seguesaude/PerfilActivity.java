package com.example.seguesaude;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class PerfilActivity extends AppCompatActivity {

    private TextView txtNomeCompleto, txtEmail;
    private TextView btnEditarPerfil, btnAgendamentos, btnCalendario, btnSair;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        txtNomeCompleto = findViewById(R.id.txtNomeCompleto);
        txtEmail = findViewById(R.id.txtEmail);
        btnEditarPerfil = findViewById(R.id.btnEditarPerfil);
        btnAgendamentos = findViewById(R.id.btnAgendamentos);
        btnCalendario = findViewById(R.id.btnCalendario);
        btnSair = findViewById(R.id.btnSair);

        carregarDadosPerfil();

        btnEditarPerfil.setOnClickListener(v -> {
            startActivity(new Intent(this, ConfigurarPerfilActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        btnAgendamentos.setOnClickListener(v -> {
            startActivity(new Intent(this, MeusAgendamentosActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        btnCalendario.setOnClickListener(v -> {
            Intent intent = new Intent(this, CalendarioActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        btnSair.setOnClickListener(v -> {
            UsuarioUtils.limparUsuario(this);

            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        carregarDadosPerfil();
    }

    private void carregarDadosPerfil() {
        String nome = UsuarioUtils.getNome(this);
        String sobrenome = UsuarioUtils.getSobrenome(this);
        String email = UsuarioUtils.getEmail(this);

        if ((nome == null || nome.isEmpty()) || (sobrenome == null || sobrenome.isEmpty())) {
            if (email != null && !email.isEmpty()) {
                DatabaseHelper db = new DatabaseHelper(this);
                Cursor cursor = db.buscarUsuarioPorEmail(email);
                if (cursor != null && cursor.moveToFirst()) {
                    nome = cursor.getString(cursor.getColumnIndexOrThrow("nome"));
                    sobrenome = cursor.getString(cursor.getColumnIndexOrThrow("sobrenome"));

                    UsuarioUtils.salvarUsuario(this, nome, sobrenome, email);
                }
                if (cursor != null) cursor.close();
            }
        }

        String nomeFinal = (nome != null ? nome : "").trim();
        String sobrenomeFinal = (sobrenome != null ? sobrenome : "").trim();
        StringBuilder nomeCompletoBuilder = new StringBuilder();

        if (!nomeFinal.isEmpty()) nomeCompletoBuilder.append(nomeFinal);
        if (!sobrenomeFinal.isEmpty()) {
            if (nomeCompletoBuilder.length() > 0) nomeCompletoBuilder.append(" ");
            nomeCompletoBuilder.append(sobrenomeFinal);
        }

        txtNomeCompleto.setText(nomeCompletoBuilder.toString());
        txtEmail.setText(email != null ? email : "email@exemplo.com");
    }

    public void abrirAgendamento(View view) {
        startActivity(new Intent(this, AgendamentoActivity.class));
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}