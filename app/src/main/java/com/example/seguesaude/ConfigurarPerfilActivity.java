package com.example.seguesaude;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

public class ConfigurarPerfilActivity extends AppCompatActivity {

    EditText editNome, editSobrenome, editEmail;
    TextInputEditText editSenha;
    Button btnSalvar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configurar_perfil);

        editNome = findViewById(R.id.editNome);
        editSobrenome = findViewById(R.id.editSobrenome);
        editEmail = findViewById(R.id.editEmail);
        editSenha = findViewById(R.id.campoSenha);
        btnSalvar = findViewById(R.id.btnSalvar);

        SharedPreferences preferences = getSharedPreferences("loginPrefs", MODE_PRIVATE);
        String nome = preferences.getString("nomeLogado", "");
        String sobrenome = preferences.getString("sobrenomeLogado", "");
        String email = preferences.getString("emailLogado", "");

        editNome.setText(nome);
        editSobrenome.setText(sobrenome);
        editEmail.setText(email);

        btnSalvar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String novoNome = editNome.getText().toString().trim();
                String novoSobrenome = editSobrenome.getText().toString().trim();
                String novaSenha = editSenha.getText().toString().trim();

                if (novoNome.isEmpty() || novoSobrenome.isEmpty()) {
                    Toast.makeText(ConfigurarPerfilActivity.this, "Preencha nome e sobrenome", Toast.LENGTH_SHORT).show();
                    return;
                }

                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("nomeLogado", novoNome);
                editor.putString("sobrenomeLogado", novoSobrenome);
                editor.apply();

                if (!novaSenha.isEmpty()) {
                    DatabaseHelper db = new DatabaseHelper(ConfigurarPerfilActivity.this);
                    boolean alterada = db.atualizarSenha(email, novaSenha);
                    if (!alterada) {
                        Toast.makeText(ConfigurarPerfilActivity.this, "Erro ao atualizar senha", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                Toast.makeText(ConfigurarPerfilActivity.this, "Dados atualizados com sucesso", Toast.LENGTH_SHORT).show();
                finish(); // volta para Perfil
            }
        });

        ImageView btnVoltar = findViewById(R.id.btnVoltar);
        btnVoltar.setOnClickListener(v -> finish());
    }

    public void abrirPerfil(View view) {
        startActivity(new Intent(this, PerfilActivity.class));
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    public void abrirAgendamento(View view) {
        startActivity(new Intent(this, AgendamentoActivity.class));
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}