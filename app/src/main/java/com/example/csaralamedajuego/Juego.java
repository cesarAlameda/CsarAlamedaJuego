package com.example.csaralamedajuego;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

public class Juego extends SurfaceView implements SurfaceHolder.Callback, SurfaceView.OnTouchListener {

    private SurfaceHolder holder;
    private BucleJuego bucle;

    private static final float ESCALA_ROBOT = 1.5f;
    private boolean mostrandoIntroduccion = true;
    private long tiempoInicioIntroduccion;
    private static final long DURACION_INTRODUCCION = 6000; // Duración en milisegundos

    private Bitmap introduccion;
    private MediaPlayer introduccionMusic1, introduccionMusic2;

    // POSICIONAR EL MAPA EN LA PANTALLA
    private Bitmap mapa;
    public float maxX, maxY; // Ancho y alto del canvas: nos lo podemos trare del bucleJuego
    private final int X=0, Y=1;
    private float posMapa[]= new float[2]; // coordenadas del mapa para ser posicionado en la pantalla
    private float recorteMapa[]= new float[2]; // coordenadas del recorte del mapa a mostrar
    private float mapa_h, mapa_w; // alto y ancho del bitmap del mapa

    // PINTAR EL ROBOT
    public Bitmap robot;

    public Bitmap disparo;
    private float robot_h, robot_w; // alto y ancho del bitmap del robot
    private float posRobot[]= new float[2]; // coordenadas del robot
    private float posYInicialRobot;
    private float puntero_robot_sprite=0;
    private int estado_robot=1;

    private float velocidadRobot[]= new float[2];
    private float gravedad[]= new float[2];
    private float deltaT;
    private int contadorFrames=0;

    private float tiempoCrucePantalla=5.0f;

    // CREAR LOS CONTROLES
    Control controles[]=new Control[4];
    private final int IZQUIERDA=0, DERECHA=1, ARRIBA=2, DISPARO=3;

    private static final String TAG = Juego.class.getSimpleName();
    private ArrayList<Toque> toques=new ArrayList<>();
    private boolean hayToque=false;
    private boolean salto=false;
    Bitmap ganador;
    Bitmap perdedor;
    private boolean cambioizquierda=false;
    private boolean finPantalla=false;
    private boolean cambiotiro=false;
    private ArrayList<Disparo> disparos = new ArrayList<>();

    private ArrayList<Dinosaurio> dinosaurios;

    private Random random;
    private int frames_para_nuevo_enemigo=0;
    private int nEnemigos=30;

    private boolean derrota=false;
    Bitmap dinosaurio;

    private static final int MAX_BALAS_POR_PARTIDA=10;
    private int contadorBalas=0;


    // SONIDO
    private MediaPlayer gameMusic,endMusic;
    int contMusica=0;

    public Juego(Activity context) {
        super(context);
        holder = getHolder();
        holder.addCallback(this);

        dinosaurios = new ArrayList<>();
        random = new Random();

        cargaControles();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }
    public boolean colision(Dinosaurio e, Disparo d){
        Bitmap enemigo=dinosaurio;
        Bitmap disparo=this.disparo;
        return Colision.hayColision(enemigo,(int) e.getX(),(int)e.getY(),
                disparo,(int)d.coordenada_x,(int)d.coordenada_y);
    }
    private void disparar() {
        // Obtener las coordenadas actuales del robot
        float robotX = posRobot[X];
        float robotY = posRobot[Y];
        if (contadorBalas < MAX_BALAS_POR_PARTIDA) {
            // Crear un nuevo disparo desde la posición derecha del robot
            Disparo nuevoDisparo = new Disparo(this, robotX, robotY);
            disparos.add(nuevoDisparo);
            contadorBalas++;
        } else {

            MediaPlayer mediaPlayer = MediaPlayer.create(getContext(), R.raw.pistolavacia);
            mediaPlayer.start();
        }

    }

    private void generarNuevoDinosaurio() {


            // Generar un nuevo dinosaurio
            float xMaxima = maxX; // maxX es la anchura máxima de la pantalla
            float yJugador = posYInicialRobot; // posRobot[Y] es la posición Y del jugador
            Dinosaurio nuevoDinosaurio = new Dinosaurio(xMaxima, yJugador);
            dinosaurios.add(nuevoDinosaurio);



    }
    public boolean colisionRobot(Dinosaurio e) {
        Bitmap enemigo = dinosaurio; // Suponiendo que "dinosaurio" es el sprite del dinosaurio
        Bitmap robotSprite = robot; // Suponiendo que "robot" es el sprite del robot

        // Obtener las coordenadas y dimensiones de la parte relevante del robot y el dinosaurio
        int xRobot = (int) posRobot[X]; // Coordenada X del robot
        int yRobot = (int) posRobot[Y]; // Coordenada Y del robot
        int anchoRobot = robotSprite.getWidth() / 21; // Ancho de la parte relevante del robot (suponiendo que la hoja de sprites del robot se divide en 21 partes)
        int altoRobot = robotSprite.getHeight() * 2 / 3; // Alto de la parte relevante del robot
        int xDinosaurio = (int) e.getX(); // Coordenada X del dinosaurio
        int yDinosaurio = (int) e.getY(); // Coordenada Y del dinosaurio
        int anchoDinosaurio = enemigo.getWidth()/5; // Ancho del dinosaurio
        int altoDinosaurio = enemigo.getHeight(); // Alto del dinosaurio

        // Verificar la colisión entre la parte relevante del robot y el dinosaurio
        return Colision.hayColision2(robotSprite, xRobot, yRobot, anchoRobot, altoRobot,
                enemigo, xDinosaurio, yDinosaurio, anchoDinosaurio, altoDinosaurio);
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // se crea la superficie, creamos el game loop


        //creo tiro
        disparo=BitmapFactory.decodeResource(getResources(), R.drawable.disparo);
        // Carga la imagen de introducción
        introduccion = BitmapFactory.decodeResource(getResources(), R.drawable.introfoto);

        dinosaurio=BitmapFactory.decodeResource(getResources(), R.drawable.dinosa);
        // Inicia el temporizador para controlar la duración de la introducción
        tiempoInicioIntroduccion = System.currentTimeMillis();

        // Para interceptar los eventos de la SurfaceView
        getHolder().addCallback(this);

        // creamos el game loop
        bucle = new BucleJuego(getHolder(), this);
        maxX=bucle.maxX;
        maxY= bucle.maxY;



        ganador=BitmapFactory.decodeResource(getResources(), R.drawable.ganador);


        perdedor=BitmapFactory.decodeResource(getResources(), R.drawable.perdedor);



        mapa=BitmapFactory.decodeResource(getResources(), R.drawable.mapaprincipal);
        mapa_h=mapa.getHeight();
        mapa_w=mapa.getWidth();
        posMapa[X]=0;
        posMapa[Y]=(maxY-mapa_h)/2;

        robot=BitmapFactory.decodeResource(getResources(), R.drawable.robotprota);
        robot_h=robot.getHeight();
        robot_w=robot.getWidth();

        posYInicialRobot=(posMapa[Y]+mapa_h*9/10 - robot_h*2/3);
        posRobot[X]=100;
        posRobot[Y]=posYInicialRobot;
        deltaT=1f/bucle.MAX_FPS;
        velocidadRobot[X]=maxX/tiempoCrucePantalla;
        velocidadRobot[Y]=-maxX/tiempoCrucePantalla*2;

        gravedad[Y]=-velocidadRobot[Y]*2;

        // Hacer la Vista focusable para que pueda capturar eventos
        setFocusable(true);

        //PREPARAR MUSICA
        gameMusic =MediaPlayer.create(getContext(), R.raw.mprincipal);
        endMusic=MediaPlayer.create(getContext(), R.raw.victoria);
        introduccionMusic1 = MediaPlayer.create(getContext(), R.raw.p_35244109_166);
        introduccionMusic2 = MediaPlayer.create(getContext(), R.raw.zas);
        introduccionMusic1.start();
        introduccionMusic1.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                // Reproduce la segunda parte de la música de introducción al terminar la primera
                introduccionMusic2.start();
            }
        });


        //comenzar el bucle
        bucle.start();



        cargaControles();

        setOnTouchListener(this);

    }

    /**
     * Este método actualiza el estado del juego. Contiene la lógica del videojuego
     * generando los nuevos estados y dejando listo el sistema para un repintado.
     */
    public void actualizar() {
        if (mostrandoIntroduccion) {
            long tiempoActual = System.currentTimeMillis();
            long tiempoTranscurrido = tiempoActual - tiempoInicioIntroduccion;
            if (tiempoTranscurrido >= DURACION_INTRODUCCION) {
                mostrandoIntroduccion = false;
            }
        } else {
            // Generar nuevos dinosaurios
            if(frames_para_nuevo_enemigo==0){
                generarNuevoDinosaurio();
                frames_para_nuevo_enemigo = bucle.MAX_FPS*60/nEnemigos;
            }
            frames_para_nuevo_enemigo--;

            // Actualizar la posición de los dinosaurios
            for (Dinosaurio d : dinosaurios) {
                // Calcular el movimiento total del dinosaurio
                float movimientoDinosaurio = d.getVelocidad() * deltaT;

                // Si el mapa se está desplazando más allá de la mitad de la pantalla
                if (posRobot[X] > maxX / 2 && recorteMapa[X] <= 9250) {
                    // Restar el movimiento del mapa al movimiento del dinosaurio
                    movimientoDinosaurio -= deltaT * velocidadRobot[X];
                }

                // Actualizar la posición del dinosaurio
                d.setX(d.getX() + movimientoDinosaurio);

                // Mantener la animación del dinosaurio
                d.actualizar(deltaT);
            }
        }

        // El resto del método permanece igual...
        for (int i = 0; i < disparos.size(); i++) {
            Disparo disparo = disparos.get(i);
            disparo.actualizaCoordenadas();
            if (disparo.fueraDePantalla()) {
                disparos.remove(i);
                i--;
            }
        }

        // Matar dinosaurios
        for(Iterator<Dinosaurio> it_enemigos = dinosaurios.iterator(); it_enemigos.hasNext();) {
            Dinosaurio e = it_enemigos.next();
            for (Iterator<Disparo> it_disparos = disparos.iterator(); it_disparos.hasNext(); ) {
                Disparo d = it_disparos.next();
                if (colision(e, d)) {
                    e.setEstado(1);
                    try {
                        it_enemigos.remove();
                        it_disparos.remove();
                    } catch (Exception ex) {
                    }
                }
            }
        }

        // Colisión con el robot
        for(Iterator<Dinosaurio> it_enemigos = dinosaurios.iterator(); it_enemigos.hasNext();) {
            Dinosaurio e = it_enemigos.next();
            if (colisionRobot(e)) {
                try {
                    it_enemigos.remove();
                    derrota = true;
                } catch (Exception ex) {
                }
            }
        }

        if (recorteMapa[X] + posRobot[X] > mapa_w * 0.959) {
            estado_robot = 0;
            finPantalla = true;
        }

        if (!finPantalla) {
            if (!controles[IZQUIERDA].pulsado && !controles[DERECHA].pulsado && !controles[ARRIBA].pulsado && !controles[DISPARO].pulsado) {
                estado_robot = 0;
                puntero_robot_sprite = robot_w / 21 * estado_robot;
                cambiotiro=true;
            } else {

                //voy hacia la derecha
                if (!controles[IZQUIERDA].pulsado && controles[DERECHA].pulsado) {

                    cambioizquierda=true;
                    cambiotiro=true;
                    if (posRobot[X] <= maxX / 2) {
                        posRobot[X] += deltaT * velocidadRobot[X];
                    } else if (recorteMapa[X] <= 9250) {
                        recorteMapa[X] += deltaT * velocidadRobot[X];
                    } else {
                        posRobot[X] += deltaT * velocidadRobot[X];
                    }

                    //cambio los sprites de mi muñeco
                    puntero_robot_sprite = robot_w / 21 * estado_robot;
                    contadorFrames++;
                    if (contadorFrames % 3 == 0) {
                        estado_robot++;
                        if (estado_robot >= 4) estado_robot = 1;
                    }

                }

                //voy hacia la izquierda
                if (controles[IZQUIERDA].pulsado && !controles[DERECHA].pulsado && posRobot[X] > 0) {

                    if (posRobot[X] <= maxX / 2) {
                        posRobot[X] -= deltaT * velocidadRobot[X];
                    } else if (recorteMapa[X] >= 0) {
                        recorteMapa[X] -= deltaT * velocidadRobot[X];
                    } else if (recorteMapa[X] <= robot_w) {
                        posRobot[X] -= deltaT * velocidadRobot[X];
                    }

                    //cambio los sprites de mi muñeco
                    if(cambioizquierda){
                        estado_robot=4;
                        cambioizquierda=false;
                        cambiotiro=true;

                    }else{
                        puntero_robot_sprite =  robot_w / 21 * estado_robot;
                        contadorFrames++;
                        if (contadorFrames % 3 == 0) {
                            estado_robot++;
                            if (estado_robot >= 7) estado_robot = 4;
                        }
                    }


                }
                if(controles[DISPARO].pulsado && !controles[IZQUIERDA].pulsado && !controles[DERECHA].pulsado && !controles[ARRIBA].pulsado ){
                    //cambio los sprites de mi muñeco
                    if(cambiotiro){
                        estado_robot=12;

                        cambiotiro=false;
                    }else{
                        puntero_robot_sprite = robot_w / 21 * estado_robot;
                        contadorFrames++;
                        if (contadorFrames % 3 == 0) {
                            estado_robot++;
                            if (estado_robot >=14){
                                disparar();

                                estado_robot = 12;



                            }
                        }
                    }

                }

            }



            if (controles[ARRIBA].pulsado && !salto) {
                salto = true;
                velocidadRobot[Y] = -maxX / tiempoCrucePantalla * 2;
            }

            if (salto) {
                // Actualizar la posición vertical del personaje según la velocidad de salto
                posRobot[Y] += deltaT * velocidadRobot[Y];

                // Aplicar la gravedad para simular la caída del personaje
                velocidadRobot[Y] += deltaT * gravedad[Y];

                // Cambiar estados del 7 al 11 mientras el robot está saltando
                estado_robot = 7 + (contadorFrames / 3) % 5;

                // Verificar si el personaje ha alcanzado la posición inicial de salto
                if (posRobot[Y] >= posYInicialRobot) {
                    // Restaurar la posición vertical y la velocidad de salto del personaje
                    posRobot[Y] = posYInicialRobot;
                    velocidadRobot[Y] = -maxX / tiempoCrucePantalla * 2;
                    estado_robot = 0;
                    salto = false; // Indicar que el personaje ha terminado de saltar
                }
            }

            if (posRobot[Y] > posYInicialRobot) {
                salto = false;
                posRobot[Y] = posYInicialRobot;
                velocidadRobot[Y] = -maxX / tiempoCrucePantalla * 2;
            }
        }



        if (finPantalla) {
            if (gameMusic.isPlaying()) gameMusic.stop();
            if (!endMusic.isPlaying() && contMusica == 0) {
                endMusic.start();
                contMusica++;
            }
        } else {
            if (!gameMusic.isPlaying() && contMusica == 0) gameMusic.start();
        }
    }



    /**
     * Este método dibuja el siguiente paso de la animación correspondiente
     */
    @SuppressLint("SuspiciousIndentation")
    public void renderizar(Canvas canvas) throws InterruptedException {
        canvas.drawColor(Color.BLACK);

        if (mostrandoIntroduccion) {
            // Dibuja la imagen de introducción en todo el canvas
            canvas.drawBitmap(introduccion, null, new Rect(0, 0, (int) maxX, (int) maxY), null);
        } else {
            // Dibuja el juego principal
            Paint p = new Paint();
            p.setStyle(Paint.Style.FILL_AND_STROKE);
            p.setColor(Color.RED);
            p.setTextSize(50);
            //canvas.drawText("Frame "+bucle.iteraciones+";"+"Tiempo "+bucle.tiempoTotal + " ["+bucle.maxX+","+bucle.maxY+"]",50,150,p);
            //canvas.drawBitmap(mapa,posMapa[X],posMapa[Y],p);

            //canvas.drawBitmap(mapa,posMapa[X],posMapa[Y],p);
            canvas.drawBitmap(mapa,new Rect((int) recorteMapa[X],(int)recorteMapa[Y],(int)maxX+(int)recorteMapa[X],(int)mapa_h),
                    new Rect((int)posMapa[X], (int) posMapa[Y], (int) maxX,(int) posMapa[Y]+(int)mapa_h),null);


            p.setColor(Color.WHITE);
            canvas.drawText("Balas: " + (MAX_BALAS_POR_PARTIDA-contadorBalas) + "/" + MAX_BALAS_POR_PARTIDA, 50, 50, p);

            if (!finPantalla || !derrota) {
    // En el método renderizar(), actualiza el dibujo del personaje Robot
                canvas.drawBitmap(robot,
                        new Rect((int) puntero_robot_sprite, 0, (int) (robot_w / 21 + puntero_robot_sprite), (int) robot_h),
                        new Rect((int) posRobot[X], (int) posRobot[Y],
                                (int) (posRobot[X] + robot_w / 21 * ESCALA_ROBOT),
                                (int) (posRobot[Y] + robot_h * 2 / 3 * ESCALA_ROBOT)),
                        null);

                for (Dinosaurio d : dinosaurios) {
                    d.dibujar(canvas, p, dinosaurio);
                }
            }


            // Pintar controles
            for (Control c : controles) {
                c.dibujar(canvas, p);
                if (c.pulsado) {
                    canvas.drawCircle(c.coordenada_x + (c.ancho() / 2), c.coordenada_y + (c.alto() / 2), 20, p);
                }
            }
            if(derrota) {
                if (gameMusic.isPlaying()) gameMusic.stop();
                derrota=false;

                bucle.sleep(50);
                Intent intent = new Intent(getContext(), MainActivity.class);
                intent.putExtra("perdido", true);
                getContext().startActivity(intent);

                bucle.JuegoEnEjecucion=false;//DETENGO EL BUCLE

            }


            if (finPantalla) {
                if (gameMusic.isPlaying()) gameMusic.stop();
                if (!endMusic.isPlaying() && contMusica == 0) {
                    nEnemigos=0;
                    endMusic.start();
                    contMusica++;
                }

                canvas.drawBitmap(ganador, null, new Rect(0, 0, (int) maxX, (int) maxY), null);

            } else {
                if (!gameMusic.isPlaying() && contMusica == 0)
                    gameMusic.start();
            }

            // Dentro del método renderizar()
            for (Disparo disparo : disparos) {
                disparo.Dibujar(canvas, p);
            }
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d(TAG, "Juego destruido!");
        // cerrar el thread y esperar que acabe
        boolean retry = true;
        while (retry) {
            try {
                bucle.join();
                retry = false;
            } catch (InterruptedException e) {

            }
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int index;
        int x, y;

        // Obtener el pointer asociado con la acción
        index = event.getActionIndex();

        x = (int) event.getX(index);
        y = (int) event.getY(index);

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                hayToque = true;

                synchronized (this) {
                    toques.add(index, new Toque(index, x, y));
                }

                // Se comprueba si se ha pulsado
                for (int i = 0; i < controles.length; i++)
                    controles[i].compruebaPulsado(x, y);
                break;

            case MotionEvent.ACTION_POINTER_UP:
                synchronized (this) {
                    toques.remove(index);
                }

                // Se comprueba si se ha soltado el botón
                for (int i = 0; i < controles.length; i++)
                    controles[i].compruebaSoltado(toques);
                break;

            case MotionEvent.ACTION_UP:
                synchronized (this) {
                    toques.clear();
                }
                hayToque = false;
                // Se comprueba si se ha soltado el botón
                for (int i = 0; i < controles.length; i++)
                    controles[i].compruebaSoltado(toques);
                break;
        }

        return true;
    }
    public void cargaControles(){
        float aux;

        //flecha_izda
        controles[IZQUIERDA]=new Control(getContext(),0, maxY /5*4);
        controles[IZQUIERDA].cargar( R.drawable.flecha_izda);
        controles[IZQUIERDA].nombre="IZQUIERDA";
        //flecha_derecha
        controles[DERECHA]=new Control(getContext(),
                controles[0].ancho()+controles[0].coordenada_x+5,controles[0].coordenada_y);
        controles[DERECHA].cargar(R.drawable.flecha_dcha);
        controles[DERECHA].nombre="DERECHA";

        //disparo
        aux=5.0f/7.0f* maxX; //en los 5/7 del ancho
        controles[ARRIBA]=new Control(getContext(),aux,controles[0].coordenada_y);
        controles[ARRIBA].cargar(R.drawable.flecha_arrb);
        controles[ARRIBA].nombre="ARRIBA";

        // Agregar el botón de disparo
        controles[DISPARO] = new Control(getContext(), controles[ARRIBA].ancho() + controles[ARRIBA].coordenada_x + 5, controles[ARRIBA].coordenada_y);
        controles[DISPARO].cargar(R.drawable.disparocontrol); // Reemplaza "flecha_disparo" con el nombre de tu recurso de imagen
        controles[DISPARO].nombre = "DISPARO";
    }


}