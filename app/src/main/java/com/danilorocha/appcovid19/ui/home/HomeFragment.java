package com.danilorocha.appcovid19.ui.home;

import android.app.AlertDialog;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.danilorocha.appcovid19.R;
import com.danilorocha.appcovid19.dao.BrasilDao;
import com.danilorocha.appcovid19.dao.FabricaConexao;
import com.danilorocha.appcovid19.entity.Brasil;
import com.danilorocha.appcovid19.entity.DadosBrasil;
import com.danilorocha.appcovid19.service.DadosService;
import com.danilorocha.appcovid19.util.Util;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class HomeFragment extends Fragment {
    private TextView txtConfirmados, txtMortes, txtData;
    private ImageButton btnAviso;
    private BrasilDao brasilDao;

    @RequiresApi(api = Build.VERSION_CODES.O)
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        txtConfirmados = root.findViewById(R.id.txtConfirmadosBrasil);
        txtMortes = root.findViewById(R.id.txtMortesBrasil);
        txtData = root.findViewById(R.id.txtDataBrasil);
        btnAviso = root.findViewById(R.id.btnAviso);
        btnAviso.setFocusable(View.NOT_FOCUSABLE);
        btnAviso.setOnClickListener(mostrarAvisto());
        brasilDao = new BrasilDao(new FabricaConexao(getContext()));
        pegarDadosBrasil();
        return root;
    }//onCreateView

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void pegarDadosBrasil() {
        List<Brasil> dadosBrasil = brasilDao.obterTodosDadosBrasil();
        int diaAtual = LocalDate.now().getDayOfMonth();
        if (Objects.nonNull(dadosBrasil) && !dadosBrasil.isEmpty()) {
            int diaBanco = dadosBrasil.get(0).getData().getDayOfMonth();
            if (Math.subtractExact(diaAtual, diaBanco) > 1)
                pegarDadosBrasilNaApi();
            else mostrarDados(dadosBrasil.get(0));
        } else pegarDadosBrasilNaApi();
    }//metodo

    private void mostrarDados(Brasil brasil) {
        txtConfirmados.setText(Util.formatarValorInteiro(brasil.getConfirmados()));
        txtMortes.setText(Util.formatarValorInteiro(brasil.getMortes()));
        txtData.setText(Util.formatarDataParaString(brasil.getData().toString()));
    }//metodo

    private void pegarDadosBrasilNaApi() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(DadosService.BASE_URL_OUTROS)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        DadosService service = retrofit.create(DadosService.class);
        Call<DadosBrasil> call = service.pegarDadosBrasil("brazil");

        call.enqueue(new Callback<DadosBrasil>() {
            @Override
            public void onResponse(Call<DadosBrasil> call, Response<DadosBrasil> response) {
                if (response.isSuccessful())
                    if (Objects.nonNull(response.body()) && response.body().data.confirmed != 0) {
                        try {
                            Brasil brasil = Brasil.converterDados(response.body());
                            if (Objects.nonNull(brasil)) {
                                mostrarDados(brasil);
                                brasilDao.salvarDadosBrasil(brasil);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        Log.i("Sucess onResponse", call.request().toString());
                    } else toast("Dados indisponíveis");
                Log.i("Error onResponse", call.request().toString());
            }//onResponse

            @Override
            public void onFailure(Call<DadosBrasil> call, Throwable t) {
                String msg = "Não foi possível obter os dados de " +
                        "https://covid19-brazil-api.now.sh/api/report/v1/brazil";
                new AlertDialog.Builder(getContext())
                        .setTitle("Falhou")
                        .setMessage(msg +"\nTente novamente mais tarde.")
                        .setPositiveButton("Fechar", (d,w)->{})
                        .show();
                Log.i("Log onFailure", t.getMessage());
            }//onFailure
        });
    }//metodo

    private View.OnClickListener mostrarAvisto() {
        return v -> {
            String msg = "Desde o dia 18/02/2020 o Governo Federal desativou uma página com estatísticas " +
                    "oficiais. Em sua nova publicação deixou de informar os dados de casos suspeitos e " +
                    "descartados, tornando assim estes marcadores inconsistentes.";
            new AlertDialog.Builder(getContext())
                    .setTitle("Atenção")
                    .setMessage(msg)
                    .setPositiveButton("Fechar", (d,w)->{})
                    .show();
        };
    }//metodo

    private void toast(String msg) {
        Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();
    }//metodo

}//classe