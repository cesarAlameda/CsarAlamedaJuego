package com.example.csaralamedajuego;


import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

public class Dinosaurio {

    private long tiempoEstado1; // Tiempo en milisegundos en el que el dinosaurio está en estado 1
    private float x, y; // Coordenadas del dinosaurio
    private float velocidad; // Velocidad a la que se mueve el dinosaurio hacia la izquierda
    private static final float VELOCIDAD_INICIAL =200.0f; // Velocidad inicial del dinosaurio
    private int estadoActual; // Estado actual del dinosaurio
    private final int NUM_ESTADOS = 5; // Número total de estados del dinosaurio en la hoja de sprites
    public Dinosaurio(float x, float y) {
        this.x = x;
        this.y = y;
        this.velocidad = -VELOCIDAD_INICIAL; // Velocidad negativa para moverse hacia la izquierda
        this.estadoActual = 2;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getVelocidad() {
        return velocidad;
    }

    public int getEstadoActual() {
        return estadoActual;
    }

    public int getNUM_ESTADOS() {
        return NUM_ESTADOS;
    }

    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

    public void setVelocidad(float velocidad) {
        this.velocidad = velocidad;
    }

    public void setEstadoActual(int estadoActual) {
        this.estadoActual = estadoActual;
    }

    public void actualizar(float deltaT) {
        // Actualizar la posición del dinosaurio en el eje X
        x += velocidad * deltaT;

        // Actualizar el estado del dinosaurio (recorrer solo los últimos 3 estados de la hoja de sprites)
        // Actualizar el estado del dinosaurio (recorrer solo los últimos 3 estados de la hoja de sprites)
        estadoActual++;
        if (estadoActual >= NUM_ESTADOS) {
            estadoActual = 3;
        }



    }


    public void setEstado(int estado) {
        this.estadoActual = estado;
        if (estado == 1) {
            tiempoEstado1 = System.currentTimeMillis();
        }
    }

    public int getEstado() {
        return estadoActual;
    }

    public long getTiempoEstado1() {
        return tiempoEstado1;
    }
    public void dibujar(Canvas canvas, Paint paint, Bitmap dinosaurio) {
        // Dibujar el dinosaurio en el lienzo en las coordenadas X y Y con el estado actual
        int anchoFrame = dinosaurio.getWidth() / NUM_ESTADOS; // Ancho de cada frame en la hoja de sprites
        int origenX = anchoFrame * estadoActual; // Coordenada X de inicio del frame en la hoja de sprites

        canvas.drawBitmap(dinosaurio, new Rect(origenX, 0, origenX + anchoFrame, dinosaurio.getHeight()), new Rect((int) x, (int) y, (int) x + anchoFrame, (int) y + dinosaurio.getHeight()), paint);
    }
}