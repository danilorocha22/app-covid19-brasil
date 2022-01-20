package com.danilorocha.appcovid19.ui.estadosdiario;

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

public class DiarioFragment extends Fragment {
    private TextView txtViewData;
    private EstadoDao estadoDao;
    private ImageButton btnPesquisar;
    private DatePickerDialog.OnDateSetListener data;
    private Calendar calendario = Calendar.getInstance();
    private RecyclerView recyclerView;
    private DiarioAdapter adapter;
    private ProgressBar progressBar;
    private LottieAnimationView lottieLoading;
    private ImageView imgDadosNaoLocalizados;
    private FloatingActionButton fabDiario;
    private MaterialCardView cardCompartilharBoletimDiario;
    private RadioButton btnWhats, btnGmail, btnDispositivo;
    private String dataDoDia = null, dataDiaAnterior = null;
    private final int DATA_INICIAL = 0, DATA_USUARIO = 1;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_diario, container, false);
        txtViewData = root.findViewById(R.id.txtViewDataDiario);
        btnPesquisar = root.findViewById(R.id.btnPesquisarDiario);
        recyclerView = root.findViewById(R.id.recyclerViewDiario);
        progressBar = root.findViewById(R.id.progressBarDiario);
        lottieLoading = root.findViewById(R.id.lottieLoadingDiario);
        imgDadosNaoLocalizados = root.findViewById(R.id.dadosNaoLocalizadosDiario);
        fabDiario = root.findViewById(R.id.fabDiario);
        cardCompartilharBoletimDiario = root.findViewById(R.id.cardWhatsGmailDiario);
        btnWhats = root.findViewById(R.id.whatsappDiario);
        btnGmail = root.findViewById(R.id.gmailDiario);
        btnDispositivo = root.findViewById(R.id.dispositoDiario);

        estadoDao = new EstadoDao(new FabricaConexao(getContext()));
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        recyclerView.setFocusable(View.NOT_FOCUSABLE);
        btnPesquisar.setFocusable(View.NOT_FOCUSABLE);
        fabDiario.setFocusable(View.NOT_FOCUSABLE);
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

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void carregamentoInicial() {
        List<Estado> casosDiario = estadoDao.obterCasosDiarioPorEstado();
        List<Estado> casosConsolidados = estadoDao.obterCasosConsolidadosDiaPorEstado();

        if (Objects.nonNull(casosConsolidados) && !casosConsolidados.isEmpty()) {
            int diaAtual = LocalDate.now().getDayOfMonth();
            int diaBanco = casosConsolidados.get(0).getData().getDayOfMonth();

            if (Util.dataMaiorQueUmDia(diaAtual, diaBanco) && !Util.isFoiNaApiDiario())
                pegarDadosNaApi();
            else {
                pegarDadosDoDiaAtualEAnteriorNoBanco();
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
            @Override
            public void onResponse(Call<DadosEstados> call, Response<DadosEstados> response) {
                if (response.isSuccessful()) {
                    try {
                        if (Objects.nonNull(response.body().data) && !response.body().data.isEmpty()) {
                            List<Estado> estados = Estado.converterDados(response.body());
                            if (!existeNoBanco(estados))
                                estadoDao.salvarCasosConsolidadosDiaPorEstado(estados);
                            String dataDiaAnterior = estados.get(0).getData().minusDays(1).toString();
                            pegarDadosNaApiPelaData(dataDiaAnterior, DATA_INICIAL);
                            //Util.toast(getContext(), "Última atualização disponível");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }//catch
                    Log.i("Sucess onResponse", call.request().toString());
                } else
                    Log.i("Erro onResponse", call.request().toString());
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

    private void pegarDadosNaApiPelaData(String data, int OPCAO) {
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
                            if (Objects.nonNull(estados) && !estados.isEmpty()) {
                                if (!existeNoBanco(estados))
                                    estadoDao.salvarCasosConsolidadosDiaPorEstado(estados);
                                if (OPCAO == DATA_INICIAL) {
                                    pegarDadosDoDiaAtualEAnteriorNoBanco();
                                    Util.toast(getContext(), "Última atualização disponível");
                                }
                                if (OPCAO == DATA_USUARIO)
                                    pegarDadosDoDiaEscolhidoPeloUsuarioNoBanco();
                                Util.setFoiNaApiDiario(true);
                            }
                        } else {
                            clear();
                            imgDadosNaoLocalizados.setVisibility(View.VISIBLE);
                            fabDiario.setVisibility(View.INVISIBLE);
                            Util.toast(getContext(), "Não há dados para esta data");
                        }
                        Log.i("Sucess onResponse", call.request().toString());
                    } else
                        Log.i("Erro onResponse", call.request().toString());
                } catch (Exception e) {
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
        fabDiario.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        lottieLoading.setVisibility(View.VISIBLE);
    }//metodo

    @RequiresApi(api = Build.VERSION_CODES.O)
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

    private boolean existeNoBanco(List<Estado> estadosApi) {
        List<Estado> estadosBanco = estadoDao.obterCasosDiarioPelaData(
                estadosApi.get(0).getData().toString());
        return (Objects.nonNull(estadosBanco) && !estadosBanco.isEmpty());
    }//metodo

    private void pegarDadosDoDiaAtualEAnteriorNoBanco() {
        try {
            String dataAtualBanco = estadoDao.obterCasosConsolidadosDiaPorEstado().get(0).getData().toString();
            List<Estado> dadosDoDia = estadoDao.obterCasosConsolidadosDiaPelaData(dataAtualBanco);
            List<Estado> dadosDiaAnterior = new ArrayList<>();

            if (Objects.nonNull(dadosDoDia) && !dadosDoDia.isEmpty()) {
                String dataDiaAnterior = dadosDoDia.get(0).getData().minusDays(1).toString();
                dadosDiaAnterior = estadoDao.obterCasosConsolidadosDiaPelaData(dataDiaAnterior);

                if (Objects.nonNull(dadosDiaAnterior) && !dadosDiaAnterior.isEmpty())
                    gerarBoletimDiario(dadosDoDia, dadosDiaAnterior);
                else
                    pegarDadosNaApiPelaData(dataDiaAnterior, DATA_INICIAL);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }//metodo

    private void pegarDadosDoDiaEscolhidoPeloUsuarioNoBanco() {
        if (Objects.nonNull(dataDoDia) && !dataDoDia.isEmpty() &&
                Objects.nonNull(dataDiaAnterior) && !dataDiaAnterior.isEmpty()) {
            List<Estado> dadosDoDia = estadoDao.obterCasosConsolidadosDiaPelaData(dataDoDia);
            List<Estado> dadosDiaAnterior = estadoDao.obterCasosConsolidadosDiaPelaData(dataDiaAnterior);
            gerarBoletimDiario(dadosDoDia, dadosDiaAnterior);
        }
    }//metodo

    private void gerarBoletimDiario(List<Estado> dadosDoDia, List<Estado> dadosDoDiaAnterior) {
        List<Estado> boletimDiario = new ArrayList<>();
        int ct = 0;

        if (Objects.nonNull(dadosDoDia) && !dadosDoDia.isEmpty() &&
                Objects.nonNull(dadosDoDiaAnterior) && !dadosDoDiaAnterior.isEmpty()) {

            for (Estado dadoDia : dadosDoDia) {
                for (Estado dadoDiaAnterior : dadosDoDiaAnterior) {

                    if (dadoDia.getNome().equals(dadoDiaAnterior.getNome())) {
                        Estado estado = new Estado();
                        estado.setId(dadosDoDia.get(ct).getId());
                        estado.setNome(dadosDoDia.get(ct).getNome());
                        estado.setUf(dadosDoDia.get(ct).getUf());
                        estado.setData(dadosDoDia.get(ct).getData());

                        if (dadoDia.getConfirmados() > dadoDiaAnterior.getConfirmados()) {
                            estado.setConfirmados(Math.subtractExact(
                                    dadoDia.getConfirmados(),
                                    dadoDiaAnterior.getConfirmados()));
                        }
                        if (dadoDia.getMortes() > dadoDiaAnterior.getMortes()) {
                            estado.setMortes(Math.subtractExact(dadoDia.getMortes(),
                                    dadoDiaAnterior.getMortes()));
                        }
                        if (dadoDia.getSuspeitos() > dadoDiaAnterior.getSuspeitos()) {
                            estado.setSuspeitos(Math.subtractExact(dadoDia.getSuspeitos(),
                                    dadoDiaAnterior.getSuspeitos()));
                        }
                        if (dadoDia.getDescartados() > dadoDiaAnterior.getDescartados()) {
                            estado.setDescartados(Math.subtractExact(dadoDia.getDescartados(),
                                    dadoDiaAnterior.getDescartados()));
                        }
                        if (!boletimDiario.contains(estado)) {
                            boletimDiario.add(estado);
                        }
                    }//if
                }//for
                ct++;
            }//for
        }//if
        if (!existeNoBanco(boletimDiario))
            estadoDao.salvarCasosDiarioPoEstado(boletimDiario);
        mostrarDados(boletimDiario);
    }//metodo

    private void pesquisarPelaData() {
        clear();
        imgDadosNaoLocalizados.setVisibility(View.GONE);
        dataDoDia = Util.formatarParaLocalDate(calendario.getTime()).toString();
        dataDiaAnterior = LocalDate.parse(dataDoDia).minusDays(1).toString();
        List<Estado> dadosDiario = estadoDao.obterCasosDiarioPelaData(dataDoDia);

        if (Objects.nonNull(dadosDiario) && !dadosDiario.isEmpty()) {
            mostrarDados(dadosDiario);
        } else {
            List<Estado> dadosConsolidadosDoDia = estadoDao.obterCasosConsolidadosDiaPelaData(dataDoDia);
            List<Estado> dadosConsolidadosDiaAnterior = null;

            if (Objects.nonNull(dadosConsolidadosDoDia) && !dadosConsolidadosDoDia.isEmpty()) {
                dadosConsolidadosDiaAnterior = estadoDao.obterCasosConsolidadosDiaPelaData(dataDiaAnterior);
                if (Objects.nonNull(dadosConsolidadosDiaAnterior) && !dadosConsolidadosDiaAnterior.isEmpty()) {
                    gerarBoletimDiario(dadosConsolidadosDoDia, dadosConsolidadosDiaAnterior);
                } else {
                    pegarDadosNaApiPelaData(dataDiaAnterior, DATA_USUARIO);
                }
            } else {
                pegarDadosNaApiPelaData(dataDoDia, DATA_USUARIO);
                pegarDadosNaApiPelaData(dataDiaAnterior, DATA_USUARIO);
            }
        }
    }//metodo

    private void mostrarDados(List<Estado> estados) {
        finishLoading();
        adapter = new DiarioAdapter(estados);
        recyclerView.setAdapter(adapter);
        atualizarDataUI(estados);
        Util.setBoletimDiario(estados);
    }//metodo

    private void finishLoading() {
        progressBar.setVisibility(View.GONE);
        lottieLoading.setVisibility(View.GONE);
        fabDiario.setVisibility(View.VISIBLE);
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
        } else
            txtViewData.setText(Util.formatarDataParaString(
                    Util.formatarParaLocalDate(calendario.getTime()).toString()));
    }//metodo

    private View.OnClickListener compartilharBoletim() {
        return (view) -> {
            Intent it = instanciarIntent();
            switch (view.getId()) {
                case R.id.whatsappDiario:
                    whatsApp(it);
                    break;

                case R.id.gmailDiario:
                    gmail(it);
                    break;
            }//switch
            cardCompartilharBoletimDiario.setVisibility(View.GONE);
        };
    }//metodo

    private Intent instanciarIntent() {
        Intent it = new Intent();
        it.setAction(Intent.ACTION_SEND);//intenção da intent: enviar dados para alguém
        it.setType("text/plain");//tipo de texto que será enviado
        return it;
    }//metodo

    private void whatsApp(Intent it) {
        try {
            String tipo = "Diário";
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

    private void gmail(Intent it) {
        try {
            String tipo = "Diário";
            if (Objects.nonNull(Util.getBoletimDiario(tipo)) && !Util.getBoletimDiario(tipo).isEmpty()) {
                PackageInfo info = getContext().getPackageManager().getPackageInfo(
                        "com.google.android.gm", PackageManager.GET_META_DATA);
                it.putExtra(Intent.EXTRA_EMAIL, "");//definindo email
                it.putExtra(Intent.EXTRA_SUBJECT, "Boletim Diário COVID 2019");//definindo titulo
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