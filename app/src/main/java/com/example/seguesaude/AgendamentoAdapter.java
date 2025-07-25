package com.example.seguesaude;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AgendamentoAdapter extends RecyclerView.Adapter<AgendamentoAdapter.ViewHolder> {

    public interface OnAgendamentoDeleteListener {
        void onDelete(Agendamento agendamento);
    }

    private final List<Agendamento> listaAgendamentos;
    private final Context context;
    private final OnAgendamentoDeleteListener deleteListener;

    public AgendamentoAdapter(Context context, List<Agendamento> lista, OnAgendamentoDeleteListener listener) {
        this.context = context;
        this.listaAgendamentos = lista;
        this.deleteListener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtTipo, txtTipoDescricao, txtData, txtHorario, txtLocal;
        Button btnEditar;
        ImageView btnExcluir;

        public ViewHolder(View itemView) {
            super(itemView);
            txtTipo = itemView.findViewById(R.id.txtTipo);
            txtTipoDescricao = itemView.findViewById(R.id.txtTipoDescricao);
            txtData = itemView.findViewById(R.id.txtData);
            txtHorario = itemView.findViewById(R.id.txtHorario);
            txtLocal = itemView.findViewById(R.id.txtLocal);
            btnEditar = itemView.findViewById(R.id.btnEditar);
            btnExcluir = itemView.findViewById(R.id.btnExcluir);
        }
    }

    @Override
    public AgendamentoAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_agendamento, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(AgendamentoAdapter.ViewHolder holder, int position) {
        Agendamento ag = listaAgendamentos.get(position);
        SpannableStringBuilder tipoComDescricao = new SpannableStringBuilder();
        String emoji = ag.getTipo().equalsIgnoreCase("Exame") ? "🧪" : "🩺";
        tipoComDescricao.append(emoji + " " + ag.getTipo());

        if (ag.getDescricao() != null && !ag.getDescricao().trim().isEmpty()) {
            tipoComDescricao.append(" ");

            int start = tipoComDescricao.length();
            tipoComDescricao.append(ag.getDescricao());

            tipoComDescricao.setSpan(
                    new android.text.style.ForegroundColorSpan(android.graphics.Color.parseColor("#777777")),
                    start,
                    tipoComDescricao.length(),
                    android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );
        }

        holder.txtTipo.setText(tipoComDescricao);
        holder.txtTipoDescricao.setVisibility(View.GONE);
        holder.txtData.setText("📅 " + ag.getData());
        holder.txtHorario.setText("⏰ " + ag.getHorario());
        holder.txtLocal.setText("📍 " + ag.getLocal());

        holder.btnEditar.setOnClickListener(v -> {
            Intent intent = new Intent(context, EditarAgendamentoActivity.class);
            intent.putExtra("id", ag.getId());
            intent.putExtra("tipo", ag.getTipo());
            intent.putExtra("descricao", ag.getDescricao());
            intent.putExtra("data", ag.getData());
            intent.putExtra("horario", ag.getHorario());
            intent.putExtra("local", ag.getLocal());
            ((Activity) context).startActivityForResult(intent, 101);
        });

        holder.btnExcluir.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDelete(ag);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaAgendamentos.size();
    }
}