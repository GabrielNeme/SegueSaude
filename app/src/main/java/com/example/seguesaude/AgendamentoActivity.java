package com.example.seguesaude;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.ContextThemeWrapper;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AgendamentoActivity extends AppCompatActivity {

    AutoCompleteTextView autocompleteTipo;
    EditText editDescricao, editData, editHorario, editLocal;
    Button btnAgendar;
    DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agendamento);

        autocompleteTipo = findViewById(R.id.autocompleteTipo);
        editDescricao = findViewById(R.id.editDescricao);
        editData = findViewById(R.id.editData);
        editHorario = findViewById(R.id.editHorario);
        editLocal = findViewById(R.id.editLocal);
        btnAgendar = findViewById(R.id.btnAgendar);
        db = new DatabaseHelper(this);

        String[] tipos = {"Consulta", "Exame"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.item_dropdown, tipos);
        autocompleteTipo.setAdapter(adapter);
        autocompleteTipo.setThreshold(0);
        autocompleteTipo.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                autocompleteTipo.showDropDown();
            }
            return false;
        });
        autocompleteTipo.setDropDownBackgroundDrawable(new ColorDrawable(Color.WHITE));
        autocompleteTipo.setTextColor(Color.BLACK);

        editData.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            String dataAtual = editData.getText().toString().trim();
            if (!dataAtual.isEmpty()) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    String apenasData = dataAtual.split(" ")[0];
                    calendar.setTime(sdf.parse(apenasData));
                } catch (Exception e) {}
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
        });

        editHorario.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            String horarioAtual = editHorario.getText().toString().trim();
            if (!horarioAtual.isEmpty()) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                    calendar.setTime(sdf.parse(horarioAtual));
                } catch (Exception e) {}
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

        btnAgendar.setOnClickListener(v -> {
            String tipo = autocompleteTipo.getText().toString().trim();
            String descricao = editDescricao.getText().toString().trim();
            String data = editData.getText().toString().trim();
            String horario = editHorario.getText().toString().trim();
            String local = editLocal.getText().toString().trim();

            if (tipo.isEmpty() || descricao.isEmpty() || data.isEmpty() || horario.isEmpty() || local.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!tipo.equalsIgnoreCase("Consulta") && !tipo.equalsIgnoreCase("Exame")) {
                Toast.makeText(this, "Escolha entre 'Consulta' ou 'Exame'", Toast.LENGTH_SHORT).show();
                return;
            }

            SharedPreferences prefs = getSharedPreferences("loginPrefs", MODE_PRIVATE);
            String emailLogado = prefs.getString("emailLogado", "");

            Agendamento ag = new Agendamento();
            ag.setTipo(tipo);
            ag.setDescricao(descricao);

            String apenasData = data.split(" ")[0].trim();

            SimpleDateFormat formatoVisual = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            SimpleDateFormat formatoBanco = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

            String dataConvertidaParaBanco = "";
            try {
                dataConvertidaParaBanco = formatoBanco.format(formatoVisual.parse(apenasData));
            } catch (Exception e) {
                Toast.makeText(this, "Erro ao formatar a data", Toast.LENGTH_SHORT).show();
                return;
            }

            ag.setData(dataConvertidaParaBanco);
            ag.setHorario(horario);
            ag.setLocal(local);
            ag.setEmail(emailLogado);

            boolean inserido = db.inserirAgendamento(ag);
            if (inserido) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                    if (!alarmManager.canScheduleExactAlarms()) {
                        Intent intentPermissao = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                        startActivity(intentPermissao);
                        return;
                    }
                }

                Toast.makeText(this, tipo + " agendado com sucesso", Toast.LENGTH_SHORT).show();
                agendarNotificacoes(ag);

                autocompleteTipo.setText(null);
                autocompleteTipo.dismissDropDown();
                autocompleteTipo.clearFocus();
                editDescricao.setText("");
                editData.setText("");
                editHorario.setText("");
                editLocal.setText("");
            } else {
                Toast.makeText(this, "Erro ao agendar", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void agendarNotificacoes(Agendamento ag) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        SimpleDateFormat sdfData = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        Calendar horarioAgendado = Calendar.getInstance();

        try {
            String dataHora = ag.getData() + " " + ag.getHorario();
            horarioAgendado.setTime(sdfData.parse(dataHora));
        } catch (Exception e) {
            Toast.makeText(this, "Erro ao calcular horário", Toast.LENGTH_SHORT).show();
            return;
        }

        long tempoNotificacao = horarioAgendado.getTimeInMillis() - 24 * 60 * 60 * 1000;
        if (tempoNotificacao > System.currentTimeMillis()) {
            Intent intentLembrete = new Intent(this, AlarmReceiver.class);
            intentLembrete.putExtra("acao", "lembrete");
            intentLembrete.putExtra("tipo", ag.getTipo());
            intentLembrete.putExtra("local", ag.getLocal());
            intentLembrete.putExtra("horario", ag.getHorario());

            PendingIntent piLembrete = PendingIntent.getBroadcast(
                    this, ag.getId() + 10000, // ID diferente
                    intentLembrete, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            alarmManager.setExact(AlarmManager.RTC_WAKEUP, tempoNotificacao, piLembrete);
        }
    }

    public void abrirPerfil(View view) {
        Intent intent = new Intent(this, PerfilActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }
}