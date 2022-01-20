package com.danilorocha.appcovid19.ui.estado;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.danilorocha.appcovid19.R;
import com.danilorocha.appcovid19.dao.EstadoDao;
import com.danilorocha.appcovid19.dao.FabricaConexao;
import com.danilorocha.appcovid19.entity.Estado;
import com.danilorocha.appcovid19.entity.PojoEstado;
import com.danilorocha.appcovid19.service.DadosService;
import com.danilorocha.appcovid19.util.Util;
import com.google.android.material.card.MaterialCardView;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class EstadoFragment extends Fragment {
    private ImageButton btnPesquisarEstado;
    private EstadoDao estadoDao;
    private AutoCompleteTextView autoComplete;
    private MaterialCardView cardView;
    private ProgressBar progressBar;
    private TextView txtEstado, txtConfirmados, txtSuspeitos, txtMortes, txtDescartados, txtData;

    @RequiresApi(api = Build.VERSION_CODES.O)
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_estado, container, false);
        estadoDao = new EstadoDao(new FabricaConexao(getContext()));
        btnPesquisarEstado = root.findViewById(R.id.btnPesquisarEstado);
        autoComplete = root.findViewById(R.id.autoCompleteNomeEstado);
        txtEstado = root.findViewById(R.id.txtEstado);
        txtConfirmados = root.findViewById(R.id.txtConfirmados);
        txtSuspeitos = root.findViewById(R.id.txtSuspeitos);
        txtMortes = root.findViewById(R.id.txtMortes);
        txtDescartados = root.findViewById(R.id.txtDescartados);
        txtData = root.findViewById(R.id.txtDataPorEstado);
        cardView = root.findViewById(R.id.cardViewEstado);
        progressBar = root.findViewById(R.id.progressBarEstado);
        btnPesquisarEstado.setFocusable(View.NOT_FOCUSABLE);

        ArrayAdapter adapter = new ArrayAdapter(getContext(), android.R.layout.simple_dropdown_item_1line,
                pegarNomesEstados());

        autoComplete.setAdapter(adapter);
        btnPesquisarEstado.setOnClickListener(pesquisarEstado());
        autoComplete.requestFocus();
        return root;
    }//onCreateView

    public void esconderTeclado(Context context, View view) {
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }//if
    }//metodo

    @RequiresApi(api = Build.VERSION_CODES.O)
    private View.OnClickListener pesquisarEstado() {
        return v -> {
            if (verificarInput()) {
                String nomeEstado = autoComplete.getText().toString();
                Estado estado = estadoDao.obterCasosTotalPorEstado(nomeEstado);
                int diaAtual = LocalDate.now().getDayOfMonth();

                if (Objects.nonNull(estado)) {
                    int diaBanco = estado.getData().getDayOfMonth();

                    if (Util.dataMaiorQueUmDia(diaAtual, diaBanco) && !Util.isFoiNaApiEstado())
                        pegarDadosNaApiPelaUF(pegarUF(nomeEstado));
                    else
                        mostrarDados(estado);
                } else {
                    pegarDadosNaApiPelaUF(pegarUF(nomeEstado));
                }
            }//if
            esconderTeclado(getContext(), getView());
            autoComplete.clearFocus();
        };//return
    }//metodo

    private void mostrarDados(Estado estado) {
        progressBar.setVisibility(View.INVISIBLE);
        if (Objects.nonNull(estado)) {
            txtEstado.setText(estado.getNome() + " - " + estado.getUf());
            txtConfirmados.setText(Util.formatarValorInteiro(estado.getConfirmados()));
            txtSuspeitos.setText(Util.formatarValorInteiro(estado.getSuspeitos()));
            txtMortes.setText(Util.formatarValorInteiro(estado.getMortes()));
            txtDescartados.setText(Util.formatarValorInteiro(estado.getDescartados()));
            txtData.setText(Util.formatarDataParaString(estado.getData().toString()));
            autoComplete.setText("");
            autoComplete.requestFocus();
            cardView.setVisibility(View.VISIBLE);
        } else Util.toast(getContext(),"Não foi possível localizar o Estado");
    }//metodo

    private void pegarDadosNaApiPelaUF(String uf) {
        loading();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(DadosService.BASE_URL_OUTROS)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        DadosService service = retrofit.create(DadosService.class);
        Call<PojoEstado> call = service.pegarDadosEstadosPeloUF(uf);

        call.enqueue(new Callback<PojoEstado>() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onResponse(Call<PojoEstado> call, Response<PojoEstado> response) {
                if (response.isSuccessful()) {
                    if (response.body().uid != 0)
                        try {
                            Estado estado = Estado.converterDados(response.body());
                            if (Objects.nonNull(estado)) {
                                mostrarDados(estado);
                                Util.setFoiNaApiEstado(true);
                                if (!existeNoBanco(estado))
                                    estadoDao.salvarCasosTotalPorEstado(estado);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            System.out.println("Erro: Estado é " + response.body().uid);
                        }//catch
                    else Util.toast(getContext(),"Estado não localizado");
                    Log.i("Sucess onResponse", call.request().toString());
                } else Log.i("Erro onResponse", call.request().toString());
            }//onResponse

            @Override
            public void onFailure(Call<PojoEstado> call, Throwable t) {
                String msg = "Não foi possível obter os dados de " +
                        "https://covid19-brazil-api.now.sh/api/report/v1/brazil/uf/" + uf;
                new AlertDialog.Builder(getContext())
                        .setTitle("Falhou")
                        .setMessage(msg + "\nTente novamente mais tarde.")
                        .setPositiveButton("Fechar", (d, w) -> {})
                        .show();
                Log.i("Log onFailure", t.getMessage());
            }//onFailure
        });
    }//metodo

    private boolean existeNoBanco(Estado estado) {
        Estado estadoBanco = estadoDao.obterCasosTotalPorEstado(estado.getNome());
        return (Objects.nonNull(estadoBanco) && estadoBanco.getNome().equals(estado.getNome()));
    }//metodo

    private void loading() {
        txtEstado.setText("");
        cardView.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);
    }//metodo

    private List<String> pegarNomesEstados() {
        List<String> estados = new ArrayList<>();
        JsonObject jsonObject = new JsonParser().parse(pegarJson()).getAsJsonObject();
        JsonArray jsonArray = jsonObject.getAsJsonArray("estados");
        jsonArray.forEach(json -> estados.add(json.getAsJsonObject().get("nome").getAsString()));
        return estados;
    }//metodo

    private String pegarUF(String estado) {
        estado = estado.toLowerCase();
        HashMap<String, String> map = new HashMap<>();
        JsonObject jsonObject = new JsonParser().parse(pegarJson()).getAsJsonObject();
        JsonArray jsonArray = jsonObject.getAsJsonArray("estados");

        for (int i = 0; i < jsonArray.size(); i++) {
            String nome = jsonArray.get(i).getAsJsonObject().get("nome").getAsString().toLowerCase();
            String sigla = jsonArray.get(i).getAsJsonObject().get("sigla").getAsString().toLowerCase();
            map.put(nome, sigla);
            if (Objects.nonNull(map) && !map.isEmpty())
                if (map.containsKey(estado))
                    return map.get(estado).toLowerCase();
        }
        return "Estado não localizado";
    }//metodo


    private String pegarJson() {
        InputStream is = getResources().openRawResource(R.raw.estados);
        String json = null;
        try {
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            json = new String(buffer, "UTF-8");
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return json;
    }//metodo

    private boolean verificarInput() {
        return !autoComplete.getText().toString().isEmpty();
    }//metodo

}//classe