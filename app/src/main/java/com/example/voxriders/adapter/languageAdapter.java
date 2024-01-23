package com.example.voxriders.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.voxriders.R;

public class languageAdapter extends RecyclerView.Adapter<languageAdapter.ViewHolder> {

    String[] local;
    String[] lan;
    OnLanguageItemClickListerner listerner;

    public languageAdapter(String[] language, String[] loc, OnLanguageItemClickListerner lis){
        local=loc;
        lan=language;
        listerner=lis;
    }
    @NonNull
    @Override
    public languageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context c=parent.getContext();
        LayoutInflater layoutInflater=LayoutInflater.from(c);

        return new ViewHolder(layoutInflater.inflate(R.layout.langauge_layout,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull languageAdapter.ViewHolder holder, int position) {
        holder.lanBtn.setText(lan[position]);
        holder.lanBtn.setOnClickListener((v) ->{
            if(listerner != null){
                listerner.onLanguageItemClick(local[position]);
            }
        });
    }

    @Override
    public int getItemCount() {
        return lan.length;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        Button lanBtn;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            lanBtn=itemView.findViewById(R.id.lan_btn);
        }
    }
}
