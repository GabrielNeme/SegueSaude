package com.example.seguesaude;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.common.api.ApiException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity {

    EditText editEmail;
    TextInputEditText editSenha;
    Button btnLogin;
    TextView btnCadastro;
    LinearLayout loginGoogle;
    DatabaseHelper db;

    FirebaseAuth mAuth;
    GoogleSignInClient mGoogleSignInClient;
    final int RC_SIGN_IN = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences preferences = getSharedPreferences("loginPrefs", MODE_PRIVATE);
        boolean logado = preferences.getBoolean("isLoggedIn", false);

        if (logado) {
            startActivity(new Intent(LoginActivity.this, AgendamentoActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_login);

        editEmail = findViewById(R.id.editEmail);
        editSenha = findViewById(R.id.campoSenha);
        btnLogin = findViewById(R.id.btnLogin);
        btnCadastro = findViewById(R.id.btnCadastro);
        loginGoogle = findViewById(R.id.loginGoogle);
        db = new DatabaseHelper(this);

        mAuth = FirebaseAuth.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .requestProfile()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        btnLogin.setOnClickListener(v -> {
            String email = editEmail.getText().toString().trim();
            String senha = editSenha.getText().toString().trim();

            if (email.isEmpty() || senha.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
            } else {
                boolean valido = db.validarLogin(email, senha);
                if (valido) {
                    salvarLogin(email); // versão com nome/sobrenome opcionais
                    startActivity(new Intent(this, AgendamentoActivity.class));
                    finish();
                } else {
                    Toast.makeText(this, "Email ou senha incorretos", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnCadastro.setOnClickListener(v -> {
            startActivity(new Intent(this, CadastroActivity.class));
        });

        loginGoogle.setOnClickListener(v -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                Toast.makeText(this, "Erro no login Google: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        String email = user != null ? user.getEmail() : null;

                        String nome = acct.getGivenName();
                        String sobrenome = acct.getFamilyName();

                        SharedPreferences prefs = getSharedPreferences("loginPrefs", MODE_PRIVATE);
                        String nomeSalvo = prefs.getString("nomeLogado", null);
                        String sobrenomeSalvo = prefs.getString("sobrenomeLogado", null);

                        boolean perfilCompleto = prefs.getBoolean("isLoggedIn", false)
                                && nomeSalvo != null && !nomeSalvo.isEmpty()
                                && sobrenomeSalvo != null && !sobrenomeSalvo.isEmpty();

                        if (!perfilCompleto) {
                            if (nome != null && sobrenome != null && !nome.isEmpty() && !sobrenome.isEmpty()) {
                                salvarLogin(email, nome, sobrenome);
                                Toast.makeText(this, "Login Google realizado com sucesso", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(this, AgendamentoActivity.class));
                                finish();
                                return;
                            }

                            Intent intent = new Intent(this, CadastroGoogleActivity.class);
                            intent.putExtra("emailGoogle", email);
                            startActivity(intent);
                            finish();
                            return;
                        }

                        Toast.makeText(this, "Login Google realizado com sucesso", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(this, AgendamentoActivity.class));
                        finish();

                    } else {
                        Log.e("GoogleLogin", "Erro: ", task.getException());
                        Toast.makeText(this, "Falha no login com Google", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void salvarLogin(String email, String nome, String sobrenome) {
        SharedPreferences.Editor editor = getSharedPreferences("loginPrefs", MODE_PRIVATE).edit();
        editor.putBoolean("isLoggedIn", true);
        editor.putString("emailLogado", email);
        editor.putString("nomeLogado", nome);
        editor.putString("sobrenomeLogado", sobrenome);
        editor.apply();
    }

    private void salvarLogin(String email) {
        SharedPreferences prefs = getSharedPreferences("loginPrefs", MODE_PRIVATE);
        String nome = prefs.getString("nomeLogado", "");
        String sobrenome = prefs.getString("sobrenomeLogado", "");
        salvarLogin(email, nome, sobrenome);
    }
}