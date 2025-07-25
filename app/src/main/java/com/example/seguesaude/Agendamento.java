package com.example.seguesaude;

public class Agendamento {
    private int id;
    private String tipo;
    private String data;
    private String horario;
    private String local;
    private String email;
    private String descricao;


    public Agendamento(int id, String tipo, String data, String horario, String local, String email) {
        this.id = id;
        this.tipo = tipo;
        this.data = data;
        this.horario = horario;
        this.local = local;
        this.email = email;
    }

    public Agendamento() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getHorario() {
        return horario;
    }

    public void setHorario(String horario) {
        this.horario = horario;
    }

    public String getLocal() {
        return local;
    }

    public void setLocal(String local) {
        this.local = local;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
    public String getDescricao() {
        return descricao;
    }
    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
}