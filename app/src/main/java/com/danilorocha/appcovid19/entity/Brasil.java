package com.danilorocha.appcovid19.entity;

import com.danilorocha.appcovid19.util.Util;

import java.time.LocalDate;

public class Brasil {
    private int id;
    private int confirmados;
    private int mortes;
    private LocalDate data;

    public static Brasil converterDados(DadosBrasil dadosBrasil) {
        Brasil brasil = new Brasil();
        brasil.setConfirmados(dadosBrasil.data.confirmed);
        brasil.setMortes(dadosBrasil.data.deaths);
        brasil.setData(Util.formatarParaLocalDate(dadosBrasil.data.updated_at));
        return brasil;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "Brasil{" +
                "id=" + id +
                ", confirmados=" + confirmados +
                ", mortes=" + mortes +
                ", data=" + data +
                '}';
    }

}
