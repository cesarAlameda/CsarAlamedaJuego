package com.example.csaralamedajuego;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    Button btnPlay;
    Juego j;
    boolean perdido;
    TextView tvPerder;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        tvPerder=findViewById(R.id.tvPerder);
        btnPlay = findViewById(R.id.btnPlay);
        Intent intent = getIntent();
        perdido= intent.getBooleanExtra("perdido", false);

        if(perdido){

            btnPlay.setText("INTÉNTALO DE NUEVO");
            tvPerder.setText("PERDISTE!!");
        }

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getWindow().getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_IMMERSIVE |
                                View.SYSTEM_UI_FLAG_FULLSCREEN |
                                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                );

                j = new Juego(MainActivity.this);
                setContentView(j);
            }
        });
    }
}