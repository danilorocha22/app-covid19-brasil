package com.danilorocha.appcovid19.ui.estados;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.danilorocha.appcovid19.R;
import com.danilorocha.appcovid19.dao.EstadoDao;
import com.danilorocha.appcovid19.dao.FabricaConexao;
import com.danilorocha.appcovid19.entity.DadosEstados;
import com.danilorocha.appcovid19.entity.Estado;
import com.danilorocha.appcovid19.service.DadosService;
import com.danilorocha.appcovid19.util.Util;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class EstadosFragment extends Fragment {
    private TextView txtViewData;
    private EstadoDao estadoDao;
    private ImageButton btnPesquisar;
    private DatePickerDialog.OnDateSetListener data;
    private Calendar calendario = Calendar.getInstance();
    private RecyclerView recyclerView;
    private EstadosAdapter adapter;
    private ProgressBar progressBar;
    private LottieAnimationView lottieLoading;
    private ImageView imgDadosNaoLocalizados;
    private FloatingActionButton fabEstados;
    private MaterialCardView cardCompartilharBoletim;
    private RadioButton btnWhats, btnGmail, btnDispositivo;

    @RequiresApi(api = Build.VERSION_CODES.O)
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_estados, container, false);
        txtViewData = root.findViewById(R.id.txtViewData);
        btnPesquisar = root.findViewById(R.id.btnPesquisarEstados);
        progressBar = root.findViewById(R.id.progressBarEstados);
        recyclerView = root.findViewById(R.id.recyclerViewEstados);
        lottieLoading = root.findViewById(R.id.lottieLoadingEstados);
        imgDadosNaoLocalizados = root.findViewById(R.id.dadosNaoLocalizadosEstados);
        fabEstados = root.findViewById(R.id.fabEstados);
        cardCompartilharBoletim = root.findViewById(R.id.cardWhatsGmailEstados);
        btnWhats = root.findViewById(R.id.whatsappEstados);
        btnGmail = root.findViewById(R.id.gmailEstados);
        btnDispositivo = root.findViewById(R.id.dispositoEstados);

        estadoDao = new EstadoDao(new FabricaConexao(getContext()));
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        recyclerView.setFocusable(View.NOT_FOCUSABLE);
        btnPesquisar.setFocusable(View.NOT_FOCUSABLE);
        fabEstados.setFocusable(View.NOT_FOCUSABLE);
        btnWhats.setFocusable(View.NOT_FOCUSABLE);
        btnGmail.setFocusable(View.NOT_FOCUSABLE);
        btnDispositivo.setFocusable(View.NOT_FOCUSABLE);

        btnPesquisar.setOnClickListener(mostrarCalendario());
        btnWhats.setOnClickListener(compartilharBoletim());
        btnGmail.setOnClickListener(compartilharBoletim());

        data = pegarDataCalendario();
        carregamentoInicial();

        return root;
    }//onCreateView

    public void carregamentoInicial() {
        List<Estado> casosConsolidados = pegarDadosNoBancoPelaUltimaDataCadastrada();
        int diaAtual = LocalDate.now().getDayOfMonth();

        if (Objects.nonNull(casosConsolidados) && !casosConsolidados.isEmpty()) {
            int diaBanco = casosConsolidados.get(0).getData().getDayOfMonth();

            if (Util.dataMaiorQueUmDia(diaAtual, diaBanco) && !Util.isFoiNaApiEstados())
                pegarDadosNaApi();
            else {
                mostrarDados(casosConsolidados);
                Util.toast(getContext(), "Última atualização disponível");
            }
        } else
            pegarDadosNaApi();
    }//metodo

    private void pegarDadosNaApi() {
        DadosService servico = servico(DadosService.BASE_URL_PRINCIPAL);
        Call<DadosEstados> call = servico.pegarDadosEstados();
        loading();

        call.enqueue(new Callback<DadosEstados>() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onResponse(Call<DadosEstados> call, Response<DadosEstados> response) {
                if (response.isSuccessful()) {
                    try {
                        if (Objects.nonNull(response.body().data) && !response.body().data.isEmpty()) {
                            List<Estado> estados = Estado.converterDados(response.body());
                            if (Objects.nonNull(estados) && !estados.isEmpty()) {
                                mostrarDados(estados);
                                Util.setFoiNaApiEstados(true);
                                Util.toast(getContext(), "Última atualização disponível");
                                if (!existeNoBanco(estados))
                                    estadoDao.salvarCasosConsolidadosDiaPorEstado(estados);
                            }
                        } else
                            Util.toast(getContext(), "Não foi possível obter os dados.");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }//catch
                    Log.i("Sucess onResponse", call.request().toString());
                } else Log.i("Erro onResponse", call.request().toString());
            }//onResponse

            @Override
            public void onFailure(Call<DadosEstados> call, Throwable t) {
                String msg = "Não foi possível obter os dados a partir de: " +
                        "https://covid19-brazil-api.now.sh/api/report/v1";
                new AlertDialog.Builder(getContext())
                        .setTitle("Falhou")
                        .setMessage(msg + "\nTente novamente mais tarde.")
                        .setPositiveButton("Fechar", (d, w) -> {
                        })
                        .show();
                Log.i("Log onFailure", t.getMessage());
            }//onFailure
        });
    }//metodo

    private void pegarDadosNaApiPelaData(String data) {
        data = Util.formatarDataParaApi(data);
        DadosService servico = servico(DadosService.BASE_URL_OUTROS);
        Call<DadosEstados> call = servico.pegarDadosEstadosPelaData(data);
        txtViewData.setVisibility(View.INVISIBLE);
        String finalData = data;
        loading();

        call.enqueue(new Callback<DadosEstados>() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onResponse(Call<DadosEstados> call, Response<DadosEstados> response) {
                try {
                    if (response.isSuccessful()) {
                        if (Objects.nonNull(response.body().data) && !response.body().data.isEmpty()) {
                            List<Estado> estados = Estado.converterDados(response.body());
                            mostrarDados(estados);
                            if (!existeNoBanco(estados))
                                estadoDao.salvarCasosConsolidadosDiaPorEstado(estados);
                        } else {
                            clear();
                            fabEstados.setVisibility(View.INVISIBLE);
                            imgDadosNaoLocalizados.setVisibility(View.VISIBLE);
                            Util.toast(getContext(), "Não há dados para esta data");
                        }
                        Log.i("Sucess onResponse", call.request().toString());
                    } else
                        Log.i("Erro onResponse", call.request().toString());
                } catch (
                        Exception e) {
                    e.printStackTrace();
                }//catch
            }//onResponse

            @Override
            public void onFailure(Call<DadosEstados> call, Throwable t) {
                String msg = "Não foi possível obter os dados a partir de: " +
                        "https://covid19-brazil-api.now.sh/api/report/v1/brazil/" + finalData;
                new AlertDialog.Builder(getContext())
                        .setTitle("Falhou")
                        .setMessage(msg + ".\nTente novamente mais tarde.")
                        .setPositiveButton("Fechar", (d, w) -> {
                        })
                        .show();
                Log.i("Log onFailure", t.getMessage());
            }//onFailure
        });
    }//metodo

    private DadosService servico(String URL) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        DadosService servico = retrofit.create(DadosService.class);
        return servico;
    }//metodo

    private void loading() {
        fabEstados.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        lottieLoading.setVisibility(View.VISIBLE);
    }//metodo

    private List<Estado> pegarDadosNoBancoPelaUltimaDataCadastrada() {
        List<Estado> estados = estadoDao.obterCasosConsolidadosDiaPorEstado();
        if (Objects.nonNull(estados) && !estados.isEmpty()) {
            String dataBanco = estados.get(0).getData().toString();
            return estadoDao.obterCasosConsolidadosDiaPelaData(dataBanco);
        }
        return null;
    }//metodo

    private void mostrarDados(List<Estado> estados) {
        finishLoading();
        adapter = new EstadosAdapter(estados);
        recyclerView.setAdapter(adapter);
        atualizarDataUI(estados);
        Util.setBoletimDiario(estados);
    }//metodo

    private void finishLoading() {
        progressBar.setVisibility(View.GONE);
        lottieLoading.setVisibility(View.GONE);
        fabEstados.setVisibility(View.VISIBLE);
    }//metodo

    private void atualizarDataUI(List<Estado> estados) {
        txtViewData.setVisibility(View.VISIBLE);
        if (Objects.nonNull(estados) && !estados.isEmpty()) {
            LocalDate localDate = estados.get(0).getData();
            String data = Util.formatarDataParaString(localDate.toString());
            txtViewData.setText(data);
            calendario.set(Calendar.YEAR, Integer.parseInt(data.split("/")[2]));
            calendario.set(Calendar.MONTH, Integer.parseInt(data.split("/")[1]) - 1);
            calendario.set(Calendar.DAY_OF_MONTH, Integer.parseInt(data.split("/")[0]));
        } else {
            txtViewData.setText(Util.formatarDataParaString(
                    Util.formatarParaLocalDate(calendario.getTime()).toString()));
        }
    }//metodo

    private boolean existeNoBanco(List<Estado> estadosApi) {
        List<Estado> estadosBanco = estadoDao.obterCasosConsolidadosDiaPelaData(
                estadosApi.get(0).getData().toString());
        return (Objects.nonNull(estadosBanco) && !estadosBanco.isEmpty());
    }//metodo

    private View.OnClickListener mostrarCalendario() {
        return v -> new DatePickerDialog(getContext(), data,
                calendario.get(Calendar.YEAR),
                calendario.get(Calendar.MONTH),
                calendario.get(Calendar.DAY_OF_MONTH))
                .show();
    }//metodo

    private DatePickerDialog.OnDateSetListener pegarDataCalendario() {
        return (view, year, month, dayOfMonth) -> {
            calendario.set(Calendar.YEAR, year);
            calendario.set(Calendar.MONTH, month);
            calendario.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            pesquisarPelaData();
        };
    }//metodo

    private void pesquisarPelaData() {
        clear();
        imgDadosNaoLocalizados.setVisibility(View.GONE);
        String dataUsuario = Util.formatarParaLocalDate(calendario.getTime()).toString();
        List<Estado> estados = estadoDao.obterCasosConsolidadosDiaPelaData(dataUsuario);
        if (Objects.nonNull(estados) && !estados.isEmpty()) {
            mostrarDados(estados);
        } else {
            pegarDadosNaApiPelaData(dataUsuario);
        }
    }//metodo

    private View.OnClickListener compartilharBoletim() {
        return (view) -> {
            Intent it = instanciarIntent();
            switch (view.getId()) {
                case R.id.whatsappEstados:
                    porWhatsApp(it);
                    break;

                case R.id.gmailEstados:
                    porGmail(it);
                    break;
            }//switch
            cardCompartilharBoletim.setVisibility(View.GONE);
        };
    }//metodo

    private Intent instanciarIntent() {
        Intent it = new Intent();
        it.setAction(Intent.ACTION_SEND);//intenção da intent: enviar dados para alguém
        it.setType("text/plain");//tipo de texto que será enviado
        return it;
    }//metodo

    private void porWhatsApp(Intent it) {
        try {
            String tipo = "Consolidado";
            if (Objects.nonNull(Util.getBoletimDiario(tipo)) && !Util.getBoletimDiario(tipo).isEmpty()) {
                PackageInfo info = getContext().getPackageManager().getPackageInfo(
                        "com.whatsapp", PackageManager.GET_META_DATA);
                it.putExtra(Intent.EXTRA_TEXT, Util.getBoletimDiario(tipo));
                it.setPackage("com.whatsapp");
                startActivity(it);
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            Util.toast(getContext(), "Por favor instale o app do WhatsApp no seu celular");
        }
    }//metodo

    private void porGmail(Intent it) {
        try {
            String tipo = "Consolidado";
            if (Objects.nonNull(Util.getBoletimDiario(tipo)) && !Util.getBoletimDiario(tipo).isEmpty()) {
                PackageInfo info = getContext().getPackageManager().getPackageInfo(
                        "com.google.android.gm", PackageManager.GET_META_DATA);
                it.putExtra(Intent.EXTRA_EMAIL, "");//definindo email
                it.putExtra(Intent.EXTRA_SUBJECT, "Boletim Consolidado COVID 2019");//definindo titulo
                it.putExtra(Intent.EXTRA_TEXT, Util.getBoletimDiario(tipo));//definindo a mensagem
                it.setPackage("com.google.android.gm");//por qual app será enviado
                startActivity(it);
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            Util.toast(getContext(), "Por favor instale o app do Gmail no seu celular");
        }
    }//metodo

    private void clear() {
        mostrarDados(new ArrayList<>());
    }//metodo

}//classe