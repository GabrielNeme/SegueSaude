package com.example.seguesaude;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import com.google.android.material.textfield.TextInputEditText;
import android.widget.Button;

public class CadastroActivity extends AppCompatActivity {

    EditText editNome, editSobrenome, editEmail;
    TextInputEditText editSenha;
    Button btnCadastrar;
    ImageView btnVoltar;

    DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        editNome = findViewById(R.id.editNome);
        editSobrenome = findViewById(R.id.editSobrenome);
        editEmail = findViewById(R.id.editEmail);
        editSenha = findViewById(R.id.campoSenha);
        btnCadastrar = findViewById(R.id.btnCadastrar);
        btnVoltar = findViewById(R.id.btnVoltar);

        db = new DatabaseHelper(this);

        btnCadastrar.setOnClickListener(v -> {
            String nome = editNome.getText().toString().trim();
            String sobrenome = editSobrenome.getText().toString().trim();
            String email = editEmail.getText().toString().trim();
            String senha = editSenha.getText().toString();

            if (nome.isEmpty() || sobrenome.isEmpty() || email.isEmpty() || senha.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
                return;
            }

            User user = new User();
            user.setNome(nome);
            user.setSobrenome(sobrenome);
            user.setEmail(email);
            user.setSenha(senha);

            boolean sucesso = db.inserirUsuario(user);

            if (sucesso) {
                UsuarioUtils.salvarUsuario(this, nome, sobrenome, email);
                Toast.makeText(this, "Cadastro realizado com sucesso!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            } else {
                Toast.makeText(this, "Erro ao cadastrar", Toast.LENGTH_SHORT).show();
            }
        });

        btnVoltar.setOnClickListener(v -> finish());
    }
}