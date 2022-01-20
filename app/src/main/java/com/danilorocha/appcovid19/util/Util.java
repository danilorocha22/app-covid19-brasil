package com.danilorocha.appcovid19.util;

import android.content.Context;
import android.widget.Toast;

import com.danilorocha.appcovid19.entity.Estado;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class Util {
    private static boolean foiNaApiDiario = false;
    private static boolean foiNaApiEstado = false;
    private static boolean foiNaApiEstados = false;
    private static List<Estado> boletimDiario = new ArrayList<>();

    public static boolean isFoiNaApiDiario() {
        return foiNaApiDiario;
    }

    public static void setFoiNaApiDiario(boolean foiNaApiDiario) {
        Util.foiNaApiDiario = foiNaApiDiario;
    }

    public static boolean isFoiNaApiEstado() {
        return foiNaApiEstado;
    }

    public static void setFoiNaApiEstado(boolean foiNaApiEstado) {
        Util.foiNaApiEstado = foiNaApiEstado;
    }

    public static boolean isFoiNaApiEstados() {
        return foiNaApiEstados;
    }

    public static void setFoiNaApiEstados(boolean foiNaApiEstados) {
        Util.foiNaApiEstados = foiNaApiEstados;
    }

    public static String getBoletimDiario(String tipo) {
        String boletim = "Boletim " + tipo + " COVID 19 ";
        boletim += "\nData: " + Util.formatarDataParaString(boletimDiario.get(0).getData().toString());
        for (Estado estado : boletimDiario) {
            boletim += "\n\nEstado: " + estado.getNome() + "-" + estado.getUf() +
                    "\nConfirmados: " + Util.formatarValorInteiro(estado.getConfirmados()) +
                    "\nSuspeitos: " + Util.formatarValorInteiro(estado.getSuspeitos()) +
                    "\nDescartados: " + Util.formatarValorInteiro(estado.getDescartados()) +
                    "\nMortes: " + Util.formatarValorInteiro(estado.getMortes());
        }
        return boletim;
    }

    public static void setBoletimDiario(List<Estado> boletim) {
        boletimDiario.clear();
        if (Objects.nonNull(boletim) && !boletim.isEmpty())
            Util.boletimDiario = boletim;
    }

    public static String formatarDataParaString(String dataString) {
        if (Objects.nonNull(dataString)) {
            String splitData[] = dataString.toString().split("-");
            String novaData = "";
            for (int i = splitData.length - 1; i >= 0; i--)
                novaData += splitData[i] + "/";
            return novaData.replaceFirst(".$", "");
        }
        return null;
    }//metodo

    public static LocalDate formatarParaLocalDate(Date date) {
        return LocalDate.parse(new SimpleDateFormat("yyyy-MM-dd").format(date));
    }//metodo

    public static Date formatarStringParaDate(String data) {
        SimpleDateFormat formatador = new SimpleDateFormat("dd/MM/yyyy");
        Date date = null;
        try {
            date = formatador.parse(data);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public static String formatarDataParaApi(String formatLocalDate) {
        String[] dataSplit = formatLocalDate.split("-");
        return dataSplit[0].concat(dataSplit[1]).concat(dataSplit[2]);
    }//metodo

    public static void toast(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }//metodo

    public static boolean dataMaiorQueUmDia(int diaAtual, int diaAnterior) {
        return (Math.subtractExact(diaAtual, diaAnterior) > 1);
    }//metodo

    public static String formatarValorInteiro(int numero) {
        DecimalFormat formatador = new DecimalFormat("###,###.##",
                new DecimalFormatSymbols(new Locale("pt", "BR")));
        BigDecimal valor = new BigDecimal(numero);
        return  formatador.format(valor.floatValue());
    }//metodo

}//classe
