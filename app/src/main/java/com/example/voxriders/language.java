package com.example.voxriders;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.voxriders.adapter.*;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import java.util.ArrayList;

public class language extends AppCompatActivity {

    String[] local;
    String[] lan;

    RecyclerView recyclerView;
    languageAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language);

        local=getResources().getStringArray(R.array.languages_locale);
        lan=getResources().getStringArray(R.array.languages_array);

        recyclerView=findViewById(R.id.recycleView);
        adapter=new languageAdapter(lan, local, new OnLanguageItemClickListerner() {
            @Override
            public void onLanguageItemClick(String language) {
                SharedPreferences preferences=getSharedPreferences("data", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor=preferences.edit();
                editor.putString("lang",language);
                editor.commit();
                Toast.makeText(language.this, language, Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new GridLayoutManager(language.this,2));
    }
}