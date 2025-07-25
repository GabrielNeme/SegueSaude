package com.example.seguesaude;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class AlarmReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "segue_saude_lembrete";

    @Override
    public void onReceive(Context context, Intent intent) {
        String acao = intent.getStringExtra("acao");
        if (!"lembrete".equals(acao)) return;

        String tipo = intent.getStringExtra("tipo");
        String local = intent.getStringExtra("local");
        String horario = intent.getStringExtra("horario");

        if (tipo == null) tipo = "Agendamento";
        if (local == null) local = "Local não informado";
        if (horario == null) horario = "Horário indefinido";

        String titulo = "Lembrete: " + tipo + " amanhã!";
        String conteudo = "⏰ Horário: " + horario + "\n📍 Local: " + local;

        Intent abrirIntent = new Intent(context, MeusAgendamentosActivity.class);
        abrirIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, abrirIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
                manager.getNotificationChannel(CHANNEL_ID) == null) {

            NotificationChannel canal = new NotificationChannel(
                    CHANNEL_ID, "Lembretes Segue Saúde", NotificationManager.IMPORTANCE_HIGH
            );
            canal.setDescription("Notificações de agendamentos médicos");
            canal.enableLights(true);
            canal.setLightColor(Color.rgb(54, 163, 192));
            canal.enableVibration(true);
            manager.createNotificationChannel(canal);
        }

        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notificacao)
                .setContentTitle(titulo)
                .setContentText("Toque para ver detalhes")
                .setStyle(new NotificationCompat.BigTextStyle().bigText(conteudo))
                .setAutoCancel(true)
                .setSound(soundUri)
                .setColor(Color.rgb(54, 163, 192))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent);

        manager.notify(titulo.hashCode(), builder.build());
    }
}