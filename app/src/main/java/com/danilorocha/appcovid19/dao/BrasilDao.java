package com.danilorocha.appcovid19.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.danilorocha.appcovid19.entity.Brasil;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BrasilDao {
    private static final String TB_CASOS_BRASIL = "TB_CASOS_BRASIL";
    private static final String ID = "id";
    private static final String CONFIRMADOS = "confirmados";
    private static final String MORTES = "mortes";
    private static final String DATA = "data";
    private final FabricaConexao conexao;

    public BrasilDao(FabricaConexao conexao) {
        this.conexao = conexao;
    }

    public List<Brasil> obterTodosDadosBrasil() {
        try (SQLiteDatabase db = conexao.conectar()) {
            String[] colunas = {ID, CONFIRMADOS, MORTES, DATA};
            String orderBy = DATA+" DESC";
            Cursor cursor = db.query(TB_CASOS_BRASIL, colunas, null, null,
                    null,null, orderBy,null);
            List<Brasil> dados = new ArrayList<>();
            Brasil brasil = null;
            if (cursor.moveToFirst()) {
                do {
                    brasil = new Brasil();
                    brasil.setId(cursor.getInt(cursor.getColumnIndex(ID)));
                    brasil.setConfirmados(cursor.getInt(cursor.getColumnIndex(CONFIRMADOS)));
                    brasil.setMortes(cursor.getInt(cursor.getColumnIndex(MORTES)));
                    brasil.setData(LocalDate.parse(cursor.getString(cursor.getColumnIndex(DATA))));
                    dados.add(brasil);
                } while (cursor.moveToNext());
            }
            cursor.close();
            return dados;
        }
    }//metodo

    public Brasil obterDadosBrasilPelaData(String data) {
        try (SQLiteDatabase db = conexao.conectar()) {
            String[] colunas = {ID, CONFIRMADOS, MORTES, DATA};
            String selection = DATA +" LIKE ?";
            String[] selectionArgs = { String.valueOf(data) };
            Cursor cursor = db.query(TB_CASOS_BRASIL, colunas, selection, selectionArgs, null,
                    null, null,null);
            Brasil brasil = null;
            if (cursor.moveToFirst()) {
                do {
                    brasil = new Brasil();
                    brasil.setId(cursor.getInt(cursor.getColumnIndex(ID)));
                    brasil.setConfirmados(cursor.getInt(cursor.getColumnIndex(CONFIRMADOS)));
                    brasil.setMortes(cursor.getInt(cursor.getColumnIndex(MORTES)));
                    brasil.setData(LocalDate.parse(cursor.getString(cursor.getColumnIndex(DATA))));
                } while (cursor.moveToNext());
            }
            cursor.close();
            return brasil;
        }//try
    }//metodo

    public void salvarDadosBrasil(Brasil brasil) {
        try (SQLiteDatabase db = conexao.conectar()) {
            if (Objects.nonNull(brasil)) {
                ContentValues cv = new ContentValues();
                cv.put(CONFIRMADOS, brasil.getConfirmados());
                cv.put(MORTES, brasil.getMortes());
                cv.put(DATA, brasil.getData().toString());
                db.insert(TB_CASOS_BRASIL, null, cv);
                System.out.println("Dados inseridos com sucesso TB_CASOS_BRASIL");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }//metodo

}//classe
