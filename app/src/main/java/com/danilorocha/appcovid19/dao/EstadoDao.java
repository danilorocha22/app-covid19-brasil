package com.danilorocha.appcovid19.dao;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.danilorocha.appcovid19.entity.Estado;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class EstadoDao {
    private static final String TB_CASOS_DIARIO_POR_ESTADO = "TB_CASOS_DIARIO_POR_ESTADO";
    private static final String TB_CASOS_CONSOLIDADOS_DIA_POR_ESTADO = "TB_CASOS_CONSOLIDADOS_DIA_POR_ESTADO";
    private static final String TB_CASOS_TOTAL_POR_ESTADO = "TB_CASOS_TOTAL_POR_ESTADO";
    private static final String ID = "id";
    private static final String ESTADO = "estado";
    private static final String UF = "uf";
    private static final String CONFIRMADOS = "confirmados";
    private static final String SUSPEITOS = "suspeitos";
    private static final String MORTES = "mortes";
    private static final String DESCARTADOS = "descartados";
    private static final String DATA = "data";
    private final FabricaConexao conexao;

    public EstadoDao(FabricaConexao conexao) {
        this.conexao = conexao;
    }


    //########################### CASOS DIÁRIO POR ESTADO #####################################

    public void salvarCasosDiarioPoEstado(List<Estado> dados) {
        try (SQLiteDatabase db = conexao.conectar()) {
            if (Objects.nonNull(dados) && !dados.isEmpty()) {
                dados.stream().forEach(estado -> {
                    ContentValues cv = new ContentValues();
                    cv.put(ESTADO, estado.getNome());
                    cv.put(UF, estado.getUf());
                    cv.put(CONFIRMADOS, estado.getConfirmados());
                    cv.put(SUSPEITOS, estado.getSuspeitos());
                    cv.put(MORTES, estado.getMortes());
                    cv.put(DESCARTADOS, estado.getDescartados());
                    cv.put(DATA, estado.getData().toString());
                    db.insert(TB_CASOS_DIARIO_POR_ESTADO, null, cv);
                });
                System.out.println("Dados inseridos com sucesso TB_CASOS_DIARIO_POR_ESTADO");
            } else
                System.out.println("Dados não foram salvos na TB_CASOS_DIARIO_POR_ESTADO");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }//metodo

    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("Range")
    public List<Estado> obterCasosDiarioPorEstado() {
        try (SQLiteDatabase db = conexao.conectar()) {
            String[] colunas = {ID, ESTADO, UF, CONFIRMADOS, SUSPEITOS, MORTES, DESCARTADOS, DATA};
            String orderBy = DATA + " DESC";
            Cursor cursor = db.query(TB_CASOS_DIARIO_POR_ESTADO, colunas, null,
                    null, null, null, orderBy, null);
            List<Estado> dados = new ArrayList<>();
            if (cursor.moveToFirst()) {
                do {
                    Estado estado = new Estado();
                    estado.setId(cursor.getInt(cursor.getColumnIndex(ID)));
                    estado.setNome(cursor.getString(cursor.getColumnIndex(ESTADO)));
                    estado.setUf(cursor.getString(cursor.getColumnIndex(UF)));
                    estado.setConfirmados(cursor.getInt(cursor.getColumnIndex(CONFIRMADOS)));
                    estado.setSuspeitos(cursor.getInt(cursor.getColumnIndex(SUSPEITOS)));
                    estado.setMortes(cursor.getInt(cursor.getColumnIndex(MORTES)));
                    estado.setDescartados(cursor.getInt(cursor.getColumnIndex(DESCARTADOS)));
                    estado.setData(LocalDate.parse(cursor.getString(cursor.getColumnIndex(DATA))));
                    dados.add(estado);
                } while (cursor.moveToNext());
            }
            cursor.close();
            return dados;
        }
    }//metodo

    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("Range")
    public List<Estado> obterCasosDiarioPelaData(String data) {
        try (SQLiteDatabase db = conexao.conectar()) {
            String[] colunas = {ID, ESTADO, UF, CONFIRMADOS, SUSPEITOS, MORTES, DESCARTADOS, DATA};
            String selection = DATA + " LIKE ?";
            String[] selectionArgs = {String.valueOf(data)};
            String orderBy = CONFIRMADOS + " DESC";
            Cursor cursor = db.query(TB_CASOS_DIARIO_POR_ESTADO, colunas, selection, selectionArgs,
                    null,
                    null, orderBy, null);
            List<Estado> dados = new ArrayList<>();
            if (cursor.moveToFirst()) {
                do {
                    Estado estado = new Estado();
                    estado.setId(cursor.getInt(cursor.getColumnIndex(ID)));
                    estado.setNome(cursor.getString(cursor.getColumnIndex(ESTADO)));
                    estado.setUf(cursor.getString(cursor.getColumnIndex(UF)));
                    estado.setConfirmados(cursor.getInt(cursor.getColumnIndex(CONFIRMADOS)));
                    estado.setSuspeitos(cursor.getInt(cursor.getColumnIndex(SUSPEITOS)));
                    estado.setMortes(cursor.getInt(cursor.getColumnIndex(MORTES)));
                    estado.setDescartados(cursor.getInt(cursor.getColumnIndex(DESCARTADOS)));
                    estado.setData(LocalDate.parse(cursor.getString(cursor.getColumnIndex(DATA))));
                    dados.add(estado);
                } while (cursor.moveToNext());
            }
            cursor.close();
            return dados;
        }
    }//metodo

    //########################### CONSOLIDADOS DIA POR ESTADO #####################################

    public void salvarCasosConsolidadosDiaPorEstado(List<Estado> dados) {
        try (SQLiteDatabase db = conexao.conectar()) {
            if (Objects.nonNull(dados) && !dados.isEmpty()) {
                dados.stream().forEach(estado -> {
                    ContentValues cv = new ContentValues();
                    cv.put(ESTADO, estado.getNome());
                    cv.put(UF, estado.getUf());
                    cv.put(CONFIRMADOS, estado.getConfirmados());
                    cv.put(SUSPEITOS, estado.getSuspeitos());
                    cv.put(MORTES, estado.getMortes());
                    cv.put(DESCARTADOS, estado.getDescartados());
                    cv.put(DATA, estado.getData().toString());
                    db.insert(TB_CASOS_CONSOLIDADOS_DIA_POR_ESTADO, null, cv);
                });
                System.out.println("Dados inseridos com sucesso TB_CASOS_CONSOLIDADOS_DIA_POR_ESTADO");
            } else
                System.out.println("Dados não foram salvos na TB_CASOS_CONSOLIDADOS_DIA_POR_ESTADO");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }//metodo

    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("Range")
    public List<Estado> obterCasosConsolidadosDiaPorEstado() {
        try (SQLiteDatabase db = conexao.conectar()) {
            String[] colunas = {ID, ESTADO, UF, CONFIRMADOS, SUSPEITOS, MORTES, DESCARTADOS, DATA};
            String orderBy = DATA + " DESC";
            Cursor cursor = db.query(TB_CASOS_CONSOLIDADOS_DIA_POR_ESTADO, colunas, null,
                    null,null, null, orderBy, null);
            List<Estado> dados = new ArrayList<>();
            if (cursor.moveToFirst()) {
                do {
                    Estado estado = new Estado();
                    estado.setId(cursor.getInt(cursor.getColumnIndex(ID)));
                    estado.setNome(cursor.getString(cursor.getColumnIndex(ESTADO)));
                    estado.setUf(cursor.getString(cursor.getColumnIndex(UF)));
                    estado.setConfirmados(cursor.getInt(cursor.getColumnIndex(CONFIRMADOS)));
                    estado.setSuspeitos(cursor.getInt(cursor.getColumnIndex(SUSPEITOS)));
                    estado.setMortes(cursor.getInt(cursor.getColumnIndex(MORTES)));
                    estado.setDescartados(cursor.getInt(cursor.getColumnIndex(DESCARTADOS)));
                    estado.setData(LocalDate.parse(cursor.getString(cursor.getColumnIndex(DATA))));
                    dados.add(estado);
                } while (cursor.moveToNext());
            }
            cursor.close();
            return dados;
        }
    }//metodo

    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("Range")
    public List<Estado> obterCasosConsolidadosDiaPelaData(String data) {
        try (SQLiteDatabase db = conexao.conectar()) {
            String[] colunas = {ID, ESTADO, UF, CONFIRMADOS, SUSPEITOS, MORTES, DESCARTADOS, DATA};
            String selection = DATA + " LIKE ?";
            String[] selectionArgs = {String.valueOf(data)};
            String orderBy = CONFIRMADOS + " DESC";
            Cursor cursor = db.query(TB_CASOS_CONSOLIDADOS_DIA_POR_ESTADO, colunas, selection,
                    selectionArgs, null,null, orderBy, null);
            List<Estado> dados = new ArrayList<>();
            if (cursor.moveToFirst()) {
                do {
                    Estado estado = new Estado();
                    estado.setId(cursor.getInt(cursor.getColumnIndex(ID)));
                    estado.setNome(cursor.getString(cursor.getColumnIndex(ESTADO)));
                    estado.setUf(cursor.getString(cursor.getColumnIndex(UF)));
                    estado.setConfirmados(cursor.getInt(cursor.getColumnIndex(CONFIRMADOS)));
                    estado.setSuspeitos(cursor.getInt(cursor.getColumnIndex(SUSPEITOS)));
                    estado.setMortes(cursor.getInt(cursor.getColumnIndex(MORTES)));
                    estado.setDescartados(cursor.getInt(cursor.getColumnIndex(DESCARTADOS)));
                    estado.setData(LocalDate.parse(cursor.getString(cursor.getColumnIndex(DATA))));
                    dados.add(estado);
                } while (cursor.moveToNext());
            }
            cursor.close();
            return dados;
        }
    }//metodo

    //########################### TOTAL POR ESTADO ##################################

    public void salvarCasosTotalPorEstado(Estado estado) {
        try (SQLiteDatabase db = conexao.conectar()) {
            if (Objects.nonNull(estado)) {
                ContentValues cv = new ContentValues();
                cv.put(ESTADO, estado.getNome());
                cv.put(UF, estado.getUf());
                cv.put(CONFIRMADOS, estado.getConfirmados());
                cv.put(SUSPEITOS, estado.getSuspeitos());
                cv.put(MORTES, estado.getMortes());
                cv.put(DESCARTADOS, estado.getDescartados());
                cv.put(DATA, estado.getData().toString());
                db.insert(TB_CASOS_TOTAL_POR_ESTADO, null, cv);
                System.out.println("Dados inseridos com sucesso TB_CASOS_TOTAL");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }//metodo

    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("Range")
    public Estado obterCasosTotalPorEstado(String nome) {
        try (SQLiteDatabase db = conexao.conectar()) {
            String[] colunas = {ID, ESTADO, UF, CONFIRMADOS, SUSPEITOS, MORTES, DESCARTADOS, DATA};
            String selection = ESTADO + " LIKE ?";
            String[] selectionArgs = {String.valueOf(nome)};
            Cursor cursor = db.query(TB_CASOS_TOTAL_POR_ESTADO, colunas, selection, selectionArgs,
                    null, null, null, null);
            Estado estado = null;
            if (cursor.moveToFirst()) {
                do {
                    estado = new Estado();
                    estado.setId(cursor.getInt(cursor.getColumnIndex(ID)));
                    estado.setNome(cursor.getString(cursor.getColumnIndex(ESTADO)));
                    estado.setUf(cursor.getString(cursor.getColumnIndex(UF)));
                    estado.setConfirmados(cursor.getInt(cursor.getColumnIndex(CONFIRMADOS)));
                    estado.setSuspeitos(cursor.getInt(cursor.getColumnIndex(SUSPEITOS)));
                    estado.setMortes(cursor.getInt(cursor.getColumnIndex(MORTES)));
                    estado.setDescartados(cursor.getInt(cursor.getColumnIndex(DESCARTADOS)));
                    estado.setData(LocalDate.parse(cursor.getString(cursor.getColumnIndex(DATA))));
                } while (cursor.moveToNext());
            }
            cursor.close();
            return estado;
        }
    }//metodo

}//classe
