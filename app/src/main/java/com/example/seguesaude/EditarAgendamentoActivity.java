package com.example.seguesaude;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class EditarAgendamentoActivity extends AppCompatActivity {

    AutoCompleteTextView autocompleteTipo;
    EditText editDescricao, editData, editHorario, editLocal;
    Button btnSalvar;
    DatabaseHelper db;
    int agendamentoId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_agendamento);

        autocompleteTipo = findViewById(R.id.autocompleteTipo);
        editDescricao = findViewById(R.id.editDescricao);
        editData = findViewById(R.id.editData);
        editHorario = findViewById(R.id.editHorario);
        editLocal = findViewById(R.id.editLocal);
        btnSalvar = findViewById(R.id.btnSalvar);
        db = new DatabaseHelper(this);

        String[] tipos = {"Consulta", "Exame"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, tipos
        );
        autocompleteTipo.setAdapter(adapter);
        autocompleteTipo.setThreshold(0);

        agendamentoId = getIntent().getIntExtra("id", -1);
        autocompleteTipo.setText(getIntent().getStringExtra("tipo"), false);
        editDescricao.setText(getIntent().getStringExtra("descricao"));
        editData.setText(getIntent().getStringExtra("data"));
        editHorario.setText(getIntent().getStringExtra("horario"));
        editLocal.setText(getIntent().getStringExtra("local"));

        editData.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            String textoOriginal = editData.getText().toString().trim();

            if (!textoOriginal.isEmpty()) {
                String apenasData = textoOriginal.contains("(")
                        ? textoOriginal.substring(0, textoOriginal.indexOf(" (")).trim()
                        : textoOriginal;

                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    Date dataCampo = sdf.parse(apenasData);
                    if (dataCampo != null) {
                        calendar.setTime(dataCampo);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            ContextThemeWrapper themedContext = new ContextThemeWrapper(this, android.R.style.Theme_Holo_Light_Dialog_MinWidth);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    themedContext,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        Calendar selectedDate = Calendar.getInstance();
                        selectedDate.set(selectedYear, selectedMonth, selectedDay);

                        SimpleDateFormat sdfData = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                        SimpleDateFormat sdfSemana = new SimpleDateFormat("EEEE", new Locale("pt", "BR"));

                        String dataFormatada = sdfData.format(selectedDate.getTime());
                        String diaSemana = sdfSemana.format(selectedDate.getTime());

                        editData.setText(dataFormatada + " (" + diaSemana + ")");
                    },
                    year, month, day
            );

            datePickerDialog.show();
            Log.d("DEBUG_CALENDARIO", "Texto do campo: " + editData.getText());
        });

        editHorario.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            String horarioTexto = editHorario.getText().toString().trim();

            if (!horarioTexto.isEmpty() && horarioTexto.contains(":")) {
                try {
                    String[] partes = horarioTexto.split(":");
                    int horaCampo = Integer.parseInt(partes[0]);
                    int minutoCampo = Integer.parseInt(partes[1]);
                    calendar.set(Calendar.HOUR_OF_DAY, horaCampo);
                    calendar.set(Calendar.MINUTE, minutoCampo);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            int hora = calendar.get(Calendar.HOUR_OF_DAY);
            int minuto = calendar.get(Calendar.MINUTE);

            ContextThemeWrapper themedContext = new ContextThemeWrapper(this, android.R.style.Theme_Holo_Light_Dialog_MinWidth);

            TimePickerDialog timePickerDialog = new TimePickerDialog(
                    themedContext,
                    (view, selectedHour, selectedMinute) -> {
                        String horarioFormatado = String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute);
                        editHorario.setText(horarioFormatado);
                    },
                    hora, minuto, true
            );

            timePickerDialog.show();
        });

        btnSalvar.setOnClickListener(v -> {
            String tipo = autocompleteTipo.getText().toString().trim();
            String descricao = editDescricao.getText().toString().trim();
            String dataBruta = editData.getText().toString().trim();
            String horario = editHorario.getText().toString().trim();
            String local = editLocal.getText().toString().trim();

            if (tipo.isEmpty() || dataBruta.isEmpty() || horario.isEmpty() || local.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
                return;
            }

            String apenasData = dataBruta.contains("(")
                    ? dataBruta.substring(0, dataBruta.indexOf(" (")).trim()
                    : dataBruta;

            SimpleDateFormat formatoVisual = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            SimpleDateFormat formatoBanco = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String dataFormatada = apenasData;

            try {
                Date dataConvertida = formatoVisual.parse(apenasData);
                dataFormatada = formatoBanco.format(dataConvertida);
            } catch (ParseException e) {
                Log.e("DATA_CONVERSAO", "Erro ao converter data: " + apenasData, e);
            }

            SharedPreferences prefs = getSharedPreferences("loginPrefs", MODE_PRIVATE);
            String emailLogado = prefs.getString("emailLogado", "");

            Agendamento ag = new Agendamento();
            ag.setId(agendamentoId);
            ag.setTipo(tipo);
            ag.setDescricao(descricao);
            ag.setData(dataFormatada);
            ag.setHorario(horario);
            ag.setLocal(local);
            ag.setEmail(emailLogado);

            boolean sucesso = db.atualizarAgendamento(ag);
            if (sucesso) {
                Toast.makeText(this, "Agendamento atualizado", Toast.LENGTH_SHORT).show();
                Intent resultIntent = new Intent();
                resultIntent.putExtra("id", ag.getId());
                resultIntent.putExtra("tipo", ag.getTipo());
                resultIntent.putExtra("descricao", ag.getDescricao());
                resultIntent.putExtra("data", formatarDataVisual(ag.getData()));
                resultIntent.putExtra("horario", ag.getHorario());
                resultIntent.putExtra("local", ag.getLocal());

                setResult(RESULT_OK, resultIntent);
                finish();
            } else {
                Toast.makeText(this, "Erro ao atualizar", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String formatarDataVisual(String dataEntrada) {
        try {
            SimpleDateFormat formatoBanco = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date dataConvertida = formatoBanco.parse(dataEntrada);

            SimpleDateFormat formatoVisual = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            SimpleDateFormat formatoSemana = new SimpleDateFormat("EEEE", new Locale("pt", "BR"));
            String dataBonita = formatoVisual.format(dataConvertida);
            String diaSemana = formatoSemana.format(dataConvertida);
            return dataBonita + " (" + diaSemana + ")";
        } catch (Exception e1) {
            try {
                String apenasData = dataEntrada.contains("(")
                        ? dataEntrada.substring(0, dataEntrada.indexOf(" (")).trim()
                        : dataEntrada.trim();

                SimpleDateFormat formatoVisual = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                Date dataConvertida = formatoVisual.parse(apenasData);

                SimpleDateFormat formatoSemana = new SimpleDateFormat("EEEE", new Locale("pt", "BR"));
                String dataBonita = formatoVisual.format(dataConvertida);
                String diaSemana = formatoSemana.format(dataConvertida);
                return dataBonita + " (" + diaSemana + ")";
            } catch (Exception e2) {
                return dataEntrada;
            }
        }
    }
}