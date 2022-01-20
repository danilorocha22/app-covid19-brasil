package com.danilorocha.appcovid19.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DataHelper extends SQLiteOpenHelper {
    private static final String CREATE_TB_CASOS_DIARIO_POR_ESTADO =
            "CREATE TABLE IF NOT EXISTS [TB_CASOS_DIARIO_POR_ESTADO] (" +
                    "[id] INTEGER PRIMARY KEY, " +
                    "[estado] TEXT NOT NULL, " +
                    "[uf] TEXT NOT NULL, " +
                    "[confirmados] INTEGER NOT NULL, " +
                    "[suspeitos] INTEGER NOT NULL, " +
                    "[mortes] INTEGER NOT NULL, " +
                    "[descartados] INTEGER NOT NULL, " +
                    "[data] DATE NOT NULL, " +
                    "UNIQUE (estado, data))";

    private static final String CREATE_TB_CASOS_CONSOLIDADOS_DIA_POR_ESTADO =
            "CREATE TABLE IF NOT EXISTS [TB_CASOS_CONSOLIDADOS_DIA_POR_ESTADO] (" +
                    "[id] INTEGER PRIMARY KEY, " +
                    "[estado] TEXT NOT NULL, " +
                    "[uf] TEXT NOT NULL, " +
                    "[confirmados] INTEGER NOT NULL, " +
                    "[suspeitos] INTEGER NOT NULL, " +
                    "[mortes] INTEGER NOT NULL, " +
                    "[descartados] INTEGER NOT NULL, " +
                    "[data] DATE NOT NULL, " +
                    "UNIQUE (estado, data))";

    private static final String CREATE_TB_CASOS_TOTAL_POR_ESTADO =
            "CREATE TABLE IF NOT EXISTS [TB_CASOS_TOTAL_POR_ESTADO] (" +
                    "[id] INTEGER PRIMARY KEY, " +
                    "[estado] TEXT NOT NULL, " +
                    "[uf] TEXT NOT NULL, " +
                    "[confirmados] INTEGER NOT NULL, " +
                    "[suspeitos] INTEGER NOT NULL, " +
                    "[mortes] INTEGER NOT NULL, " +
                    "[descartados] INTEGER NOT NULL, " +
                    "[data] DATE NOT NULL, " +
                    "UNIQUE (estado, data))";

    private static final String CREATE_TB_CASOS_BRASIL =
            "CREATE TABLE IF NOT EXISTS [TB_CASOS_BRASIL] (" +
                    "[id] INTEGER PRIMARY KEY, " +
                    "[confirmados] INTEGER NOT NULL, " +
                    "[mortes] INTEGER NOT NULL, " +
                    "[data] DATE NOT NULL)";

    public DataHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TB_CASOS_DIARIO_POR_ESTADO);
        db.execSQL(CREATE_TB_CASOS_CONSOLIDADOS_DIA_POR_ESTADO);
        db.execSQL(CREATE_TB_CASOS_TOTAL_POR_ESTADO);
        db.execSQL(CREATE_TB_CASOS_BRASIL);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
    }

}//classe
