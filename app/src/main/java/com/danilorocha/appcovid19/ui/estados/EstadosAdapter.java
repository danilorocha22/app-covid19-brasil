package com.danilorocha.appcovid19.ui.estados;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.danilorocha.appcovid19.R;
import com.danilorocha.appcovid19.entity.Estado;
import com.danilorocha.appcovid19.util.Util;

import java.util.ArrayList;
import java.util.List;

public class EstadosAdapter extends RecyclerView.Adapter<EstadosAdapter.MyViewHolder> {
    private static List<Estado> dados;

    public EstadosAdapter(List<Estado> dados) {
        this.dados = new ArrayList<>();
        this.dados = dados;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView txtEstado, txtConfirmados, txtSuspeitos, txtMortes, txtDescartados;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            txtEstado = itemView.findViewById(R.id.txtEstados);
            txtConfirmados = itemView.findViewById(R.id.txtConfirmadosEstados);
            txtSuspeitos = itemView.findViewById(R.id.txtSuspeitosEstados);
            txtMortes = itemView.findViewById(R.id.txtMortesEstados);
            txtDescartados = itemView.findViewById(R.id.txtDescartadosEstados);
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.item_recycler_view_estados, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.txtEstado.setText(dados.get(position).getNome()+" - "+dados.get(position).getUf());
        holder.txtConfirmados.setText(Util.formatarValorInteiro(dados.get(position).getConfirmados()));
        holder.txtSuspeitos.setText(Util.formatarValorInteiro(dados.get(position).getSuspeitos()));
        holder.txtMortes.setText(Util.formatarValorInteiro(dados.get(position).getMortes()));
        holder.txtDescartados.setText(Util.formatarValorInteiro(dados.get(position).getDescartados()));
    }

    @Override
    public int getItemCount() {
        return dados.size();
    }

}
