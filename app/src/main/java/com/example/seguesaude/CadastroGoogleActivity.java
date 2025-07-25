package com.example.seguesaude;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class CadastroGoogleActivity extends AppCompatActivity {

    private EditText campoNome, campoSobrenome, campoEmail;
    private Button btnConfirmar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro_google);

        campoNome = findViewById(R.id.campoNome);
        campoSobrenome = findViewById(R.id.campoSobrenome);
        campoEmail = findViewById(R.id.campoEmail);
        btnConfirmar = findViewById(R.id.btnConfirmarCadastro);

        String email = getIntent().getStringExtra("emailGoogle");
        if (email != null) {
            campoEmail.setText(email);
        } else {
            campoEmail.setText("Email não disponível");
        }
        campoEmail.setEnabled(false);

        btnConfirmar.setOnClickListener(v -> {
            String nome = campoNome.getText().toString().trim();
            String sobrenome = campoSobrenome.getText().toString().trim();

            if (nome.isEmpty() || sobrenome.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
                return;
            }

            SharedPreferences.Editor editor = getSharedPreferences("loginPrefs", MODE_PRIVATE).edit();
            editor.putString("nomeLogado", nome);
            editor.putString("sobrenomeLogado", sobrenome);
            editor.putString("emailLogado", email);
            editor.putBoolean("isLoggedIn", true);
            editor.apply();

            Log.d("CadastroGoogle", "Dados salvos: " + nome + " " + sobrenome);

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                String nomeCompleto = nome + " " + sobrenome;

                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                        .setDisplayName(nomeCompleto)
                        .build();

                user.updateProfile(profileUpdates)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Log.d("CadastroGoogle", "Perfil atualizado com: " + nomeCompleto);
                            } else {
                                Log.e("CadastroGoogle", "Erro ao atualizar perfil", task.getException());
                            }
                        });
            }

            Toast.makeText(this, "Cadastro completo!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, AgendamentoActivity.class));
            finish();
        });
    }
}