package com.example.seguesaude;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;

public class MeusAgendamentosActivity extends AppCompatActivity {

    RecyclerView recyclerAgendamentos;
    AgendamentoAdapter adapter;
    ArrayList<Agendamento> listaAgendamentos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meus_agendamentos);

        recyclerAgendamentos = findViewById(R.id.recyclerAgendamentos);
        recyclerAgendamentos.setLayoutManager(new LinearLayoutManager(this));

        listaAgendamentos = new ArrayList<>();
        adapter = new AgendamentoAdapter(this, listaAgendamentos, agendamento -> {
            // Exibe confirmação antes de excluir
            new AlertDialog.Builder(this)
                    .setTitle("Excluir agendamento")
                    .setMessage("Deseja realmente apagar este agendamento?")
                    .setPositiveButton("Sim", (dialog, which) -> {
                        DatabaseHelper db = new DatabaseHelper(this);
                        boolean apagado = db.deletarAgendamento(agendamento.getId());
                        if (apagado) {
                            Iterator<Agendamento> iterator = listaAgendamentos.iterator();
                            while (iterator.hasNext()) {
                                if (iterator.next().getId() == agendamento.getId()) {
                                    iterator.remove();
                                    break;
                                }
                            }
                            adapter.notifyDataSetChanged();
                            Toast.makeText(this, "Agendamento excluído", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Erro ao excluir agendamento", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
        });

        recyclerAgendamentos.setAdapter(adapter);
        carregarAgendamentos();

        ImageView btnVoltar = findViewById(R.id.btnVoltar);
        btnVoltar.setOnClickListener(v -> finish());
    }

    public void abrirPerfil(View view) {
        Intent intent = new Intent(this, PerfilActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    public void abrirAgendamento(View view) {
        Intent intent = new Intent(this, AgendamentoActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    private void carregarAgendamentos() {
        SharedPreferences prefs = getSharedPreferences("loginPrefs", MODE_PRIVATE);
        String emailLogado = prefs.getString("emailLogado", "");

        DatabaseHelper db = new DatabaseHelper(this);
        Cursor cursor = db.listarAgendamentosPorEmail(emailLogado);

        listaAgendamentos.clear();
        while (cursor.moveToNext()) {
            Agendamento ag = new Agendamento();
            ag.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
            ag.setTipo(cursor.getString(cursor.getColumnIndexOrThrow("tipo")));
            ag.setDescricao(cursor.getString(cursor.getColumnIndexOrThrow("descricao")));

            String dataBruta = cursor.getString(cursor.getColumnIndexOrThrow("data"));
            SimpleDateFormat formatoBanco = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat formatoVisual = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            SimpleDateFormat formatoSemana = new SimpleDateFormat("EEEE", new Locale("pt", "BR"));

            String dataFormatada = dataBruta;
            try {
                Date dataConvertida = formatoBanco.parse(dataBruta);
                String diaBonito = formatoVisual.format(dataConvertida);
                String diaSemana = formatoSemana.format(dataConvertida);
                dataFormatada = diaBonito + " (" + diaSemana + ")";
            } catch (Exception e) {
            }
            ag.setData(dataFormatada);

            ag.setHorario(cursor.getString(cursor.getColumnIndexOrThrow("horario")));
            ag.setLocal(cursor.getString(cursor.getColumnIndexOrThrow("local")));
            ag.setEmail(cursor.getString(cursor.getColumnIndexOrThrow("email")));
            listaAgendamentos.add(ag);
        }
        cursor.close();

        Collections.sort(listaAgendamentos, (a1, a2) -> {
            try {
                SimpleDateFormat formatoCompleto = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                Date d1 = formatoCompleto.parse(a1.getData().split(" ")[0] + " " + a1.getHorario());
                Date d2 = formatoCompleto.parse(a2.getData().split(" ")[0] + " " + a2.getHorario());
                return d1.compareTo(d2);
            } catch (ParseException e) {
                return 0;
            }
        });

        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 101 && resultCode == RESULT_OK && data != null) {
            int id = data.getIntExtra("id", -1);
            String tipo = data.getStringExtra("tipo");
            String descricao = data.getStringExtra("descricao");
            String dataStr = data.getStringExtra("data");
            String horario = data.getStringExtra("horario");
            String local = data.getStringExtra("local");

            for (Agendamento ag : listaAgendamentos) {
                if (ag.getId() == id) {
                    ag.setTipo(tipo);
                    ag.setDescricao(descricao);
                    ag.setData(dataStr);
                    ag.setHorario(horario);
                    ag.setLocal(local);
                    break;
                }
            }

            adapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        carregarAgendamentos();
    }
}