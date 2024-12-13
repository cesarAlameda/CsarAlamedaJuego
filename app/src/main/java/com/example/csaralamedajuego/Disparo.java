package com.example.csaralamedajuego;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.media.MediaPlayer;
import android.util.Log;

public class Disparo {
    public float coordenada_y, coordenada_x; //coordenadas donde se dibuja el control
    private Juego juego;
    private float velocidad;
    private MediaPlayer mediaPlayer; //para reproducir el sonido de disparo
    private final float MAX_SEGUNDOS_EN_CRUZAR_PANTALLA=3;

    /*Constructor con coordenadas iniciales y número de disparo*/
    public Disparo(Juego j,float robotX, float robotY){
        juego=j;


        coordenada_x = robotX;
        coordenada_y = robotY + j.robot.getHeight() / 4;



        velocidad=j.maxX /MAX_SEGUNDOS_EN_CRUZAR_PANTALLA/BucleJuego.MAX_FPS; //adaptar velocidad al tamaño de pantalla
        Log.i(Juego.class.getSimpleName(),"Velocidad de disparo: " + velocidad);
        mediaPlayer=MediaPlayer.create(j.getContext(), R.raw.disparo);
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.release();
            }
        });
        mediaPlayer.start();
    }



    //se actualiza la coordenada y nada más
    public void actualizaCoordenadas(){
        coordenada_x += velocidad;
    }

    public void Dibujar(Canvas c, Paint p) {
        c.drawBitmap(juego.disparo, coordenada_x, coordenada_y, p);
    }

    public int ancho(){
        return juego.disparo.getWidth();
    }

    public int alto(){
        return juego.disparo.getHeight();
    }

    public boolean fueraDePantalla() {
        return coordenada_x < 0;
    }

}
