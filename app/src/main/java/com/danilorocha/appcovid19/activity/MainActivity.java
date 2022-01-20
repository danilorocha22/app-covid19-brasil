package com.danilorocha.appcovid19.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.danilorocha.appcovid19.R;
import com.danilorocha.appcovid19.dao.Dao;
import com.danilorocha.appcovid19.databinding.ActivityMainBinding;
import com.danilorocha.appcovid19.ui.estadosdiario.DiarioFragment;
import com.danilorocha.appcovid19.util.Util;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;
    private DiarioFragment diarioFragment;
    private DrawerLayout parent;
    private ConstraintLayout compartilharDados;
    private RadioGroup radioGroup;
    private Dao dao;
    private FloatingActionButton fabDiario, fabEstados;
    private RadioButton btnDownloadDiario, btnDownloadEstados;
    private MaterialCardView cardCompartilharBoletimDiario, cardCompartilharBoletimEstados;
    private static final int COD_SOLICITACAO = 1;
    private static final String PERMISSAO_ARMAZENAMENTO = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    private static String[] PERMISSOES = new String[2];
    private static final String DIARIO = "Diário", CONSOLIDADO = "Consolidado";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        dao = new Dao(this);
        dao.abrirBanco();

        setSupportActionBar(binding.appBarMain.toolbar);
        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;

        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_estado, R.id.nav_estados, R.id.nav_diario)
                .setOpenableLayout(drawer)
                .build();

        NavController navController = Navigation.findNavController(this,
                R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
    }//onCreate

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_mapa_posto_saude) {
            Intent i = new Intent(MainActivity.this, MapsActivity.class);
            startActivity(i);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this,
                R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    public void click(View view) {
        switch (view.getId()) {
            case R.id.fabDiario:
                getViewsDiario();
                cardCompartilharBoletimDiario.setVisibility(View.VISIBLE);
                fabDiario.setOnClickListener(gerenciarVisualizacaoCardDiario());
                btnDownloadDiario.setOnClickListener(v -> salvarNoDispositivo(DIARIO));
                break;

            case R.id.fabEstados:
                getViewsEstados();
                cardCompartilharBoletimEstados.setVisibility(View.VISIBLE);
                fabEstados.setOnClickListener(gerenciarVisualizacaoCardEstados());
                btnDownloadEstados.setOnClickListener(v -> salvarNoDispositivo(CONSOLIDADO));
                break;
        }//switch
    }//metodo

    private void getViewsDiario() {
        fabDiario = findViewById(R.id.fabDiario);
        cardCompartilharBoletimDiario = findViewById(R.id.cardWhatsGmailDiario);
        btnDownloadDiario = findViewById(R.id.dispositoDiario);
    }//metodo

    private void getViewsEstados() {
        fabEstados = findViewById(R.id.fabEstados);
        cardCompartilharBoletimEstados = findViewById(R.id.cardWhatsGmailEstados);
        btnDownloadEstados = findViewById(R.id.dispositoEstados);
    }//metodo

    private View.OnClickListener gerenciarVisualizacaoCardDiario() {
        return v -> {
            switch (cardCompartilharBoletimDiario.getVisibility()) {
                case View.VISIBLE:
                    cardCompartilharBoletimDiario.setVisibility(View.INVISIBLE);
                    break;
                case View.INVISIBLE:
                    cardCompartilharBoletimDiario.setVisibility(View.VISIBLE);
                    break;
                case View.GONE:
                    cardCompartilharBoletimDiario.setVisibility(View.VISIBLE);
            }//switch
        };//return
    }//metodo

    private View.OnClickListener gerenciarVisualizacaoCardEstados() {
        return v -> {
            switch (cardCompartilharBoletimEstados.getVisibility()) {
                case View.VISIBLE:
                    cardCompartilharBoletimEstados.setVisibility(View.INVISIBLE);
                    break;
                case View.INVISIBLE:
                    cardCompartilharBoletimEstados.setVisibility(View.VISIBLE);
                    break;
                case View.GONE:
                    cardCompartilharBoletimEstados.setVisibility(View.VISIBLE);
            }//switch
        };//return
    }//metodo

    private void salvarNoDispositivo(String boletim) {
        int onPermissao = ContextCompat.checkSelfPermission(this, PERMISSAO_ARMAZENAMENTO);
        if (onPermissao != PackageManager.PERMISSION_GRANTED) {
            PERMISSOES[0] = PERMISSAO_ARMAZENAMENTO;
            PERMISSOES[1] = boletim;
            ActivityCompat.requestPermissions(this, PERMISSOES, COD_SOLICITACAO);
        }else
            armazenarBoletim(boletim);
    }//metodo

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissoes,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissoes, grantResults);
        if (grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                armazenarBoletim(PERMISSOES[1]);
            } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                try {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(
                            this, permissoes[0])) {

                        new AlertDialog.Builder(this)
                                .setTitle("Atenção!")
                                .setMessage("A permissão é necessária para habilitar Armazenamento Externo")
                                .setCancelable(false)
                                .setNegativeButton("Não", (d, w) -> {
                                    Util.toast(this, "Não foi possível armazenar o Boletim");
                                })
                                .setPositiveButton("Sim", (d, w) -> {
                                    ActivityCompat.requestPermissions(this, new String[]{
                                            permissoes[0]}, COD_SOLICITACAO);
                                })
                                .show();
                    }//if
                }catch (Exception e) {
                    e.printStackTrace();
                }
            }//else if
        }//if
    }//metodo

    private void armazenarBoletim(String boletim) {
        try {
            File root = new File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                            .toString());
            File arq = new File(root, "Boletim Covid 2019.txt");
            FileWriter writer = new FileWriter(arq);
            writer.append(Util.getBoletimDiario(boletim));
            writer.flush();
            writer.close();
            Util.toast(this, "Arquivo salvo em Downloads");

            if (cardCompartilharBoletimDiario != null)
                cardCompartilharBoletimDiario.setVisibility(View.GONE);
            if (cardCompartilharBoletimEstados != null)
                cardCompartilharBoletimEstados.setVisibility(View.GONE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }//metodo

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dao.fecharBanco();
    }//onDestroy

}//classe