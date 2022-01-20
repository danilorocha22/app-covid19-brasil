package com.danilorocha.appcovid19.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class Dao {
    private Context context;
    protected SQLiteDatabase db;

    public Dao(Context context) {
        this.context = context;
    }//construtor

    public void abrirBanco() {
        DataHelper dataHelper = new DataHelper(
                context,
                Constantes.BANCO,
                null,
                Constantes.VERSAO
        );
        this.db = dataHelper.getWritableDatabase();
    }//metodo

    public void fecharBanco() {
        if (db != null) {
            db.close();
        }
    }//metodo

}//classe
