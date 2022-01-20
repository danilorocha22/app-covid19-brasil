package com.danilorocha.appcovid19.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

public final class FabricaConexao {
    private final DataHelper dataHelper;

    public FabricaConexao(Context context) {
        this.dataHelper = new DataHelper(
                context,
                Constantes.BANCO,
                null,
                Constantes.VERSAO);
    }

    public SQLiteDatabase conectar() throws SQLiteException {
        return dataHelper.getWritableDatabase();
    }
}
