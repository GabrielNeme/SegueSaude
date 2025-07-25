package com.example.seguesaude;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class CalendarioActivity extends AppCompatActivity {

    MaterialCalendarView calendarView;
    TextView textDataSelecionada, textSemAgendamentos;
    LinearLayout layoutCards;
    Map<String, List<Agendamento>> agendamentos = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendario);

        calendarView = findViewById(R.id.calendarView);
        textDataSelecionada = findViewById(R.id.textDataSelecionada);
        textSemAgendamentos = findViewById(R.id.textSemAgendamentos);
        layoutCards = findViewById(R.id.layoutCards);

        calendarView.setOnDateChangedListener((widget, date, selected) -> {
            String dataSelecionada = formatarData(date.getYear(), date.getMonth(), date.getDay());
            atualizarCabecalho(dataSelecionada);
            mostrarAgendamentosDoDia(dataSelecionada);
        });

        ImageView btnVoltar = findViewById(R.id.btnVoltar);
        btnVoltar.setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences prefs = getSharedPreferences("loginPrefs", MODE_PRIVATE);
        String emailLogado = prefs.getString("emailLogado", "");
        carregarAgendamentosDoBanco(emailLogado);

        Calendar hoje = Calendar.getInstance();
        String dataHoje = formatarData(
                hoje.get(Calendar.YEAR),
                hoje.get(Calendar.MONTH),
                hoje.get(Calendar.DAY_OF_MONTH)
        );
        atualizarCabecalho(dataHoje);
        mostrarAgendamentosDoDia(dataHoje);
    }

    void carregarAgendamentosDoBanco(String emailLogado) {
        agendamentos.clear();

        try (Cursor cursor = new DatabaseHelper(this).listarAgendamentosPorEmail(emailLogado)) {
            while (cursor.moveToNext()) {
                Agendamento ag = new Agendamento();
                ag.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                ag.setTipo(cursor.getString(cursor.getColumnIndexOrThrow("tipo")));
                ag.setDescricao(cursor.getString(cursor.getColumnIndexOrThrow("descricao")));
                ag.setData(cursor.getString(cursor.getColumnIndexOrThrow("data")));
                ag.setHorario(cursor.getString(cursor.getColumnIndexOrThrow("horario")));
                ag.setLocal(cursor.getString(cursor.getColumnIndexOrThrow("local")));
                ag.setEmail(cursor.getString(cursor.getColumnIndexOrThrow("email")));

                agendamentos.computeIfAbsent(ag.getData(), k -> new ArrayList<>()).add(ag);
            }
        }

        calendarView.removeDecorators();

        Set<CalendarDay> diasConsulta = new HashSet<>();
        Set<CalendarDay> diasExame = new HashSet<>();
        Set<CalendarDay> diasAmbos = new HashSet<>();

        for (Map.Entry<String, List<Agendamento>> entry : agendamentos.entrySet()) {
            String data = entry.getKey();
            String[] partes = data.split("-");
            int ano = Integer.parseInt(partes[0]);
            int mes = Integer.parseInt(partes[1]) - 1;
            int dia = Integer.parseInt(partes[2]);
            CalendarDay diaCalendario = CalendarDay.from(ano, mes, dia);

            boolean temConsulta = false;
            boolean temExame = false;

            for (Agendamento ag : entry.getValue()) {
                if (ag.getTipo().equalsIgnoreCase("Consulta")) temConsulta = true;
                if (ag.getTipo().equalsIgnoreCase("Exame")) temExame = true;
            }

            if (temConsulta && temExame) {
                diasAmbos.add(diaCalendario);
            } else if (temConsulta) {
                diasConsulta.add(diaCalendario);
            } else if (temExame) {
                diasExame.add(diaCalendario);
            }
        }

        calendarView.addDecorator(new EventDecorator(0xFF00CDBA, diasExame));
        calendarView.addDecorator(new EventDecorator(0xFFD32F2F, diasConsulta));

        List<Integer> coresAmbos = List.of(0xFFD32F2F, 0xFF00CDBA);
        calendarView.addDecorator(new MultiDotDecorator(diasAmbos, coresAmbos));
    }

    String formatarData(int ano, int mes, int dia) {
        return String.format(Locale.getDefault(), "%04d-%02d-%02d", ano, mes + 1, dia);
    }

    void atualizarCabecalho(String data) {
        String[] partes = data.split("-");
        int mes = Integer.parseInt(partes[1]);
        String[] meses = {
                "jan.", "fev.", "mar.", "abr.", "mai.", "jun.",
                "jul.", "ago.", "set.", "out.", "nov.", "dez."
        };

        String dia = partes[2];
        String nomeMes = meses[mes - 1];
        textDataSelecionada.setText(dia + " " + nomeMes);
    }

    void mostrarAgendamentosDoDia(String data) {
        List<Agendamento> eventos = agendamentos.get(data);
        layoutCards.removeAllViews();

        if (eventos != null && !eventos.isEmpty()) {
            Collections.sort(eventos, Comparator.comparing(Agendamento::getHorario));

            for (Agendamento evento : eventos) {
                View card = criarCardEvento(evento);
                layoutCards.addView(card);
            }
            layoutCards.setVisibility(View.VISIBLE);
            textSemAgendamentos.setVisibility(View.GONE);
        } else {
            layoutCards.setVisibility(View.GONE);
            textSemAgendamentos.setVisibility(View.VISIBLE);
        }
    }

    View criarCardEvento(Agendamento evento) {
        View cardView = getLayoutInflater().inflate(R.layout.item_calendario, layoutCards, false);

        TextView textTitulo = cardView.findViewById(R.id.textTitulo);
        TextView textHorario = cardView.findViewById(R.id.textHorario);
        TextView textLocal = cardView.findViewById(R.id.textLocal);
        View indicador = cardView.findViewById(R.id.viewIndicador);

        String tipo = evento.getTipo();
        String descricao = evento.getDescricao();

        SpannableStringBuilder builder = new SpannableStringBuilder();
        SpannableString spanTipo = new SpannableString(tipo.equalsIgnoreCase("Exame") ? "Exame " : "Consulta ");
        spanTipo.setSpan(new ForegroundColorSpan(0xFF000000), 0, spanTipo.length(), 0);

        SpannableString spanDescricao = new SpannableString(descricao);
        spanDescricao.setSpan(new ForegroundColorSpan(0xFF444444), 0, spanDescricao.length(), 0);

        builder.append(spanTipo).append(spanDescricao);
        textTitulo.setText(builder);

        textHorario.setText("⏰ " + evento.getHorario());
        textLocal.setText("📍 " + evento.getLocal());

        indicador.setBackgroundColor(tipo.equalsIgnoreCase("Exame") ? 0xFF00CDBA : 0xFFD32F2F);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 8, 0, 8);
        cardView.setLayoutParams(params);

        return cardView;
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