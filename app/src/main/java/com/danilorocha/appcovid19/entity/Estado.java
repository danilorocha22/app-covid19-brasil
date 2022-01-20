package com.danilorocha.appcovid19.entity;

import com.danilorocha.appcovid19.util.Util;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Estado {
    private int id;
    private String nome;
    private String uf;
    private int confirmados;
    private int mortes;
    private int suspeitos;
    private int descartados;
    private LocalDate data;

    public static List<Estado> converterDados(DadosEstados data) {
        List<Estado> estados = new ArrayList<>();
        data.data.stream().forEach(pojoEstado -> {
            Estado estado = new Estado();
            estado.setId(pojoEstado.uid);
            estado.setUf(pojoEstado.uf);
            estado.setNome(pojoEstado.state);
            estado.setConfirmados(pojoEstado.cases);
            estado.setMortes(pojoEstado.deaths);
            estado.setSuspeitos(pojoEstado.suspects);
            estado.setDescartados(pojoEstado.refuses);
            estado.setData(Util.formatarParaLocalDate(pojoEstado.datetime));
            estados.add(estado);
        });
        return estados;
    }

    public static Estado converterDados(PojoEstado pojoEstado) {
        Estado estado = new Estado();
        estado.setId(pojoEstado.uid);
        estado.setUf(pojoEstado.uf);
        estado.setNome(pojoEstado.state);
        estado.setConfirmados(pojoEstado.cases);
        estado.setMortes(pojoEstado.deaths);
        estado.setSuspeitos(pojoEstado.suspects);
        estado.setDescartados(pojoEstado.refuses);
        estado.setData(Util.formatarParaLocalDate(pojoEstado.datetime));
        return estado;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUf() {
        return uf;
    }

    public void setUf(String uf) {
        this.uf = uf;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public int getConfirmados() {
        return confirmados;
    }

    public void setConfirmados(int confirmados) {
        this.confirmados = confirmados;
    }

    public int getMortes() {
        return mortes;
    }

    public void setMortes(int mortes) {
        this.mortes = mortes;
    }

    public int getSuspeitos() {
        return suspeitos;
    }

    public void setSuspeitos(int suspeitos) {
        this.suspeitos = suspeitos;
    }

    public int getDescartados() {
        return descartados;
    }

    public void setDescartados(int descartados) {
        this.descartados = descartados;
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "BOLETIM: " + Util.formatarDataParaString(data.toString()) +
                "\nEstado: " + nome + "-" + uf +
                "\nConfirmados: " + confirmados +
                "\nSuspeitos: " + suspeitos +
                "\nDescartados: " + descartados +
                "\nMortes: " + mortes;
    }

}
