package com.example.csaralamedajuego;


import android.graphics.Canvas;
import android.util.Log;
import android.view.SurfaceHolder;

public class BucleJuego extends Thread {


    public final static int 	MAX_FPS = 30;

    private final static int	MAX_FRAMES_SALTADOS = 5;

    private final static int	TIEMPO_FRAME = 1000 / MAX_FPS;

    private Juego juego;

    public int iteraciones;
    public long tiempoTotal;

    public boolean JuegoEnEjecucion=true;
    private static final String TAG = Juego.class.getSimpleName();
    private SurfaceHolder surfaceHolder;

    public int maxX,maxY;

    BucleJuego(SurfaceHolder sh, Juego s){
        juego=s;
        surfaceHolder=sh;


        Canvas c=sh.lockCanvas();
        maxX = c.getWidth();
        maxY = c.getHeight();
        sh.unlockCanvasAndPost(c);
    }

    @Override
    public void run() {
        Canvas canvas;
        Log.d(TAG, "Comienza el game loop");


        long tiempoComienzo;
        long tiempoDiferencia;
        int tiempoDormir;
        int framesASaltar;

        tiempoDormir = 0;

        while (JuegoEnEjecucion) {
            canvas = null;

            try {
                canvas = this.surfaceHolder.lockCanvas();
                synchronized (surfaceHolder) {

                    tiempoComienzo = System.currentTimeMillis();
                    framesASaltar = 0;	// resetear los frames saltados

                    juego.actualizar();


                    juego.renderizar(canvas);
                    iteraciones++;

                    tiempoDiferencia = System.currentTimeMillis() - tiempoComienzo;


                    tiempoDormir = (int)(TIEMPO_FRAME - tiempoDiferencia);

                    tiempoTotal+=tiempoDiferencia+tiempoDormir;

                    if (tiempoDormir > 0) {
                        try {

                            Thread.sleep(tiempoDormir);
                        } catch (InterruptedException e) {}
                    }

                    while (tiempoDormir < 0 && framesASaltar < MAX_FRAMES_SALTADOS) {
                        juego.actualizar();
                        tiempoDormir += TIEMPO_FRAME;
                        framesASaltar++;
                    }


                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {

                if (canvas != null) {
                    surfaceHolder.unlockCanvasAndPost(canvas);
                }
            }
            Log.d(TAG, "Nueva iteración!");
        }
    }


}
