package com.danilorocha.appcovid19.service;

import com.danilorocha.appcovid19.entity.DadosBrasil;
import com.danilorocha.appcovid19.entity.DadosEstados;
import com.danilorocha.appcovid19.entity.PojoBrasil;
import com.danilorocha.appcovid19.entity.PojoEstado;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface DadosService {

    String BASE_URL_PRINCIPAL = "https://covid19-brazil-api.now.sh/api/report/";
    String BASE_URL_OUTROS = "https://covid19-brazil-api.vercel.app/api/report/";

    @GET("v1")
    Call<DadosEstados> pegarDadosEstados();

    @GET("v1/brazil/uf/{uf}")
    Call<PojoEstado> pegarDadosEstadosPeloUF(@Path("uf") String uf);

    @GET("v1/{pais}")
    Call<DadosBrasil> pegarDadosBrasil(@Path("pais") String pais);

    @GET("v1/brazil/{date}")
    Call<DadosEstados> pegarDadosEstadosPelaData(@Path("date") String date);

}
