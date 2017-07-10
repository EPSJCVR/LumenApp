package com.example.juancruz.lumenapp;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.icu.util.Calendar;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Vibrator;
import android.speech.RecognizerIntent;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    //variables de luces_layout:
    Switch switchLuz;
    Switch switchLed;
    TextView estadoLuz;
    TextView estadoLed;
    SeekBar redBar;
    SeekBar greenBar;
    SeekBar blueBar;
    SeekBar dimBar;
    TextView redText;
    TextView greenText;
    TextView blueText;
    TextView dimText;
    Button btnCambiar;
    ImageButton btnActualizar;
    Switch switchAuto;
    String ipServer;
    int puerto;
    Intent intent;
    ImageButton voiceBtn;
    private SensorManager mSensorManager;
    private float mAccel; // acceleration apart from gravity
    private float mAccelCurrent; // current acceleration including gravity
    private float mAccelLast; // last acceleration including gravity

    //Variables alarma:
    TimePicker timePicker;
    TextView alarm_status;
    Button alarmOn;
    ImageButton alarmOff;
    ImageButton dateBtn;
    int yearAlarm, monthAlarm, dayAlarm, hourAlarm, minuteAlarm;
    static final int DIALOG_ID = 0;
    private DatePickerDialog.OnDateSetListener dPickerListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nav_drawer);
        //Navigation Drawer:
        ConstraintLayout luces_layout = (ConstraintLayout) findViewById(R.id.luces_layout);
        luces_layout.setVisibility(View.VISIBLE);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_luces);

        //Controles de luces:
        intent = getIntent();
        Bundle bundle = intent.getExtras();
        ipServer = bundle.getString("IP");
        puerto = bundle.getInt("Puerto");
        String json = (String) bundle.get("JSON");
        //Toast.makeText(getApplicationContext(), json, Toast.LENGTH_SHORT).show();

        //Asocio views con sus elementos
        switchLuz = (Switch) findViewById(R.id.switch1);
        switchLed = (Switch) findViewById(R.id.switch2);
        estadoLuz = (TextView) findViewById(R.id.textView12);
        estadoLed = (TextView) findViewById(R.id.textView13);
        redBar = (SeekBar) findViewById(R.id.seekBar);
        greenBar = (SeekBar) findViewById(R.id.seekBar3);
        blueBar = (SeekBar) findViewById(R.id.seekBar4);
        dimBar = (SeekBar) findViewById(R.id.seekBar5);
        redText = (TextView) findViewById(R.id.textView);
        greenText = (TextView) findViewById(R.id.textView9);
        blueText = (TextView) findViewById(R.id.textView10);
        dimText = (TextView) findViewById(R.id.textView11);
        btnCambiar = (Button) findViewById(R.id.button2);
        btnActualizar = (ImageButton) findViewById(R.id.imageButton);
        switchAuto = (Switch) findViewById(R.id.switch3);
        voiceBtn = (ImageButton) findViewById(R.id.imageButton3);

        //establezco configuracion de los switch
        //configuro los listener
        switchLuz.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    estadoLuz.setText("Luz prendida");
                } else {
                    estadoLuz.setText("Luz apagada");
                }
            }
        });

        switchLed.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    estadoLed.setText("Led prendido");
                } else {
                    estadoLed.setText("Led apagado");
                }
            }
        });


        //configuro como se mostraran la primera vez al inciar app
        if(switchLuz.isChecked()){
            estadoLuz.setText("Luz prendida");
        }else{
            estadoLuz.setText("Luz apagada");
        }

        if(switchLed.isChecked()){
            estadoLed.setText("Led prendido");
        }else{
            estadoLed.setText("Led apagado");
        }


        //establezco configuracion de los SeekBar
        //sekBar Red:
        redText.setText(redBar.getProgress() + "/" + redBar.getMax());
        redBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressValue = 0;
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressValue = progress;
                redText.setText(progress + "/" + seekBar.getMax());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //Toast.makeText(getApplicationContext(), "Started tracking seekbar", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                redText.setText(progressValue + "/" + seekBar.getMax());
                //Toast.makeText(getApplicationContext(), "Stopped tracking seekbar", Toast.LENGTH_SHORT).show();

            }
        });

        //seekbar Green:
        greenText.setText(greenBar.getProgress() + "/" + greenBar.getMax());
        greenBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressValue = 0;
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressValue = progress;
                greenText.setText(progress + "/" + seekBar.getMax());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //Toast.makeText(getApplicationContext(), "Started tracking seekbar", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                greenText.setText(progressValue + "/" + seekBar.getMax());
                //Toast.makeText(getApplicationContext(), "Stopped tracking seekbar", Toast.LENGTH_SHORT).show();

            }
        });

        //seekbar Blue:
        blueText.setText(blueBar.getProgress() + "/" + blueBar.getMax());
        blueBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressValue = 0;
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressValue = progress;
                blueText.setText(progress + "/" + seekBar.getMax());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //Toast.makeText(getApplicationContext(), "Started tracking seekbar", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                blueText.setText(progressValue + "/" + seekBar.getMax());
                //Toast.makeText(getApplicationContext(), "Stopped tracking seekbar", Toast.LENGTH_SHORT).show();

            }
        });

        //seekbar Dim:
        dimText.setText(dimBar.getProgress() + "/" + dimBar.getMax());
        dimBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressValue = 0;
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressValue = progress;
                dimText.setText(progress + "/" + seekBar.getMax());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //Toast.makeText(getApplicationContext(), "Started tracking seekbar", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                dimText.setText(progressValue + "/" + seekBar.getMax());
                //Toast.makeText(getApplicationContext(), "Stopped tracking seekbar", Toast.LENGTH_SHORT).show();

            }
        });

        //Configuro los botones:
        //Boton Actualizar:
        btnActualizar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Actualizar actualizar = new Actualizar(ipServer, puerto);
                actualizar.execute();
            }
        });

        //Boton cambiar:
        btnCambiar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Lo que se quiera hacer cuando se oprime este boton
                //Toast.makeText(getApplicationContext(), "Funciona!", Toast.LENGTH_SHORT).show();
                Cambiar cambiar = new Cambiar(ipServer, puerto);
                cambiar.execute();
            }
        });

        //Boton voice recognizer:
        voiceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSpeechInput();
            }
        });

        actualizarPanatalla(json);

        //shake:
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        mAccel = 0.00f;
        mAccelCurrent = SensorManager.GRAVITY_EARTH;
        mAccelLast = SensorManager.GRAVITY_EARTH;

        //Alarma:
        timePicker = (TimePicker)findViewById(R.id.timePicker);
        alarm_status = (TextView)findViewById(R.id.textAlarmStatus);
        alarmOn = (Button)findViewById(R.id.buttonGuardar);
        alarmOff = (ImageButton)findViewById(R.id.buttonCancelar);
        dateBtn = (ImageButton) findViewById(R.id.imageButtonDateAlarm);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            final Calendar cal = Calendar.getInstance();
            yearAlarm = cal.get(Calendar.YEAR);
            monthAlarm = cal.get(Calendar.MONTH)+1; //los meses empiezan en cero
            dayAlarm = cal.get(Calendar.DAY_OF_MONTH);
        }
        dPickerListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                yearAlarm  =year;
                monthAlarm = month + 1;
                dayAlarm = dayOfMonth;
                //Toast.makeText(getApplicationContext(), "Fecha: "+dayAlarm+"/"+monthAlarm+"/"+yearAlarm, Toast.LENGTH_LONG).show();
            }
        };

        definirInterfazAlarma();

        dateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(DIALOG_ID);
            }
        });

        alarmOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    hourAlarm = timePicker.getHour();
                } else
                    hourAlarm = timePicker.getCurrentHour();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    minuteAlarm = timePicker.getMinute();
                } else
                    minuteAlarm = timePicker.getCurrentMinute();
                //Toast.makeText(getApplicationContext(), "Alarma : "+hourAlarm+":"+minuteAlarm+" del dia "+ dayAlarm+"/"+monthAlarm+"/"+yearAlarm, Toast.LENGTH_LONG).show();
                Toast.makeText(getApplicationContext(), "Alarma activada", Toast.LENGTH_LONG).show();
                try {
                    JSONObject jsonDay = new JSONObject();
                    jsonDay.put("Día", dayAlarm);
                    JSONObject jsonMonth = new JSONObject();
                    jsonMonth.put("Mes", monthAlarm);
                    JSONObject jsonYear = new JSONObject();
                    jsonYear.put("Año", yearAlarm);
                    JSONObject jsonHour = new JSONObject();
                    jsonHour.put("Hora", hourAlarm);
                    JSONObject jsonMinute = new JSONObject();
                    jsonMinute.put("Minuto", minuteAlarm);
                    JSONArray jsonAlarm = new JSONArray();
                    jsonAlarm.put(jsonDay);
                    jsonAlarm.put(jsonMonth);
                    jsonAlarm.put(jsonYear);
                    jsonAlarm.put(jsonHour);
                    jsonAlarm.put(jsonMinute);
                    //Toast.makeText(getApplicationContext(), "JSON alarma: "+ jsonAlarm.toString(), Toast.LENGTH_LONG).show();

                    File file = new File(getApplicationContext().getFilesDir(), "alarm_file");
                    FileOutputStream outputstream;
                    try {
                        outputstream = openFileOutput("alarm_file" , Context.MODE_PRIVATE);
                        outputstream.write(jsonAlarm.toString().getBytes());
                        outputstream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    //enviar json x socket
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                definirInterfazAlarma();
            }
        });

        alarmOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Alarma desactivada", Toast.LENGTH_LONG).show();
                //crear json y mandarlo x socket
                //eliminar de archivo
                File file = new File(getApplicationContext().getFilesDir(), "alarm_file");
                file.delete();
                definirInterfazAlarma();
            }
        });
        //TODO: mandar el json x el socket...
                /*  1) leer ip y puerto de archivo guardado.
                    2) Crear clase que extienda AsyncTask y llamarla
                        2.1) en el doInBackground: que conecte x socket al server.
                        2.2) que le envie el json de alarma.    (implementar esta funcionalidad en el server)
                        2.3) esperar respuesta de confirmacion del server.
                            2.3.1) si ok -> imprimir en el TextView la hora de la alarma.
                            2.3.2) si no hay confirm -> Toast informando que no se puedo realizar.
                */

    }

    public class Cambiar extends AsyncTask<Void, Void, Void> {
        String dstAddress;
        int dstPort;
        JSONArray respuestaJson;
        JSONArray enviadoJson;

        Cambiar(String addr, int port){
            dstAddress = addr;
            dstPort = port;
        }

        private JSONArray crearJson(){
            JSONArray jsonMsg = new JSONArray();
            //crearlo segun los views
            try {
                if(switchLuz.isChecked()){
                    JSONObject switchLuzJson = new JSONObject();
                    switchLuzJson.put("Luz", 1);
                    jsonMsg.put(switchLuzJson);
                }
                else{
                    JSONObject switchLuzJson = new JSONObject();
                    switchLuzJson.put("Luz", 0);
                    jsonMsg.put(switchLuzJson);
                }

                if(switchLed.isChecked()){
                    JSONObject switchLedJson = new JSONObject();
                    switchLedJson.put("Led", 1);
                    jsonMsg.put(switchLedJson);
                }else{
                    JSONObject switchLedJson = new JSONObject();
                    switchLedJson.put("Led", 0);
                    jsonMsg.put(switchLedJson);
                }

                JSONObject redBarJson = new JSONObject();
                redBarJson.put("Red", redBar.getProgress());
                jsonMsg.put(redBarJson);

                JSONObject greenBarJson = new JSONObject();
                greenBarJson.put("Green", greenBar.getProgress());
                jsonMsg.put(greenBarJson);

                JSONObject blueBarJson = new JSONObject();
                blueBarJson.put("Blue", blueBar.getProgress());
                jsonMsg.put(blueBarJson);

                JSONObject dimBarJson = new JSONObject();
                dimBarJson.put("Dim", dimBar.getProgress());
                jsonMsg.put(dimBarJson);

                if(switchAuto.isChecked()){
                    JSONObject switchAutoJson = new JSONObject();
                    switchAutoJson.put("Auto", 1);
                    jsonMsg.put(switchAutoJson);
                }else{
                    JSONObject switchAutoJson = new JSONObject();
                    switchAutoJson.put("Auto", 0);
                    jsonMsg.put(switchAutoJson);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            return jsonMsg;
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            enviadoJson = crearJson();
            Socket socketClient = null;

            try {
                socketClient = new Socket(dstAddress, dstPort);
                BufferedReader in = new BufferedReader(new InputStreamReader(socketClient.getInputStream()));
                PrintWriter out = new PrintWriter(socketClient.getOutputStream(),true);
                out.println(enviadoJson.toString());
                respuestaJson = new JSONArray(in.readLine());
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                if(socketClient != null)
                    try {
                        socketClient.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void o) {
            //Lo que se desea hacer en este metodo es verificar la respuesta del servidor y modificar los views de acuerdo a ello.
            //SwitchLuz:

            try {
                int error=0;

                //switchLuz:
                JSONObject obj = (JSONObject) respuestaJson.get(0);
                if(switchLuz.isChecked() && obj.getInt("Luz") == 0){
                    switchLuz.toggle();
                    error++;
                }else{
                    if(!switchLuz.isChecked() && obj.getInt("Luz") == 1){
                        switchLuz.toggle();
                        error++;
                    }
                }

                //SwitchLed:
                obj = (JSONObject) respuestaJson.get(1);
                if(switchLed.isChecked() && obj.getInt("Led") == 0){
                    switchLed.toggle();
                    error++;
                }else{
                    if(!switchLed.isChecked() && obj.getInt("Led") == 1){
                        error++;
                        switchLed.toggle();
                    }
                }

                //RedBar:
                obj = (JSONObject) respuestaJson.get(2);
                JSONObject obj2 = (JSONObject) enviadoJson.get(2);

                if (obj.getInt("Red") != obj2.getInt("Red") ){
                    redBar.setProgress(obj.getInt("Red"));
                    error++;
                }
                //redBar.setProgressTintList(ColorStateList.valueOf(Color.BLUE));

                //GreenBar:
                obj = (JSONObject) respuestaJson.get(3);
                obj2 = (JSONObject) enviadoJson.get(3);

                if (obj.getInt("Green") != obj2.getInt("Green") ){
                    redBar.setProgress(obj.getInt("Green"));
                    error++;
                }

                //BlueBar:
                obj = (JSONObject) respuestaJson.get(4);
                obj2 = (JSONObject) enviadoJson.get(4);

                if (obj.getInt("Blue") != obj2.getInt("Blue") ){
                    redBar.setProgress(obj.getInt("Blue"));
                    error++;
                }

                //DimBar:
                obj = (JSONObject) respuestaJson.get(5);
                obj2 = (JSONObject) enviadoJson.get(5);

                if (obj.getInt("Dim") != obj2.getInt("Dim") ){
                    redBar.setProgress(obj.getInt("Dim"));
                    error++;
                }

                //switchAuto:
                obj = (JSONObject) respuestaJson.get(6);
                if(switchAuto.isChecked() && obj.getInt("Auto") == 0){
                    switchAuto.toggle();
                    error++;
                }else{
                    if(!switchAuto.isChecked() && obj.getInt("Auto") == 1){
                        switchAuto.toggle();
                        error++;
                    }
                }

                if ( error != 0) {
                    Toast.makeText(getApplicationContext(), "Hubo " + error + " errores! ", Toast.LENGTH_SHORT).show();
                }else
                    Toast.makeText(getApplicationContext(), "Cambios realizados satisfactoriamente", Toast.LENGTH_SHORT).show();

            } catch (JSONException e) {
                e.printStackTrace();
            }

            super.onPostExecute(o);
        }
    }

    public class Actualizar extends AsyncTask<Void, Void, Void> {
        String dstAddress;
        int dstPort;
        JSONArray respuestaJson;
        JSONArray enviadoJson;

        Actualizar(String addr, int port){
            dstAddress = addr;
            dstPort = port;
            enviadoJson = new JSONArray();
            JSONObject obj = new JSONObject();
            try {
                obj.put("Actualizar", 1);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            enviadoJson.put(obj);

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            Socket socketClient = null;

            try {
                socketClient = new Socket(dstAddress, dstPort);
                BufferedReader in = new BufferedReader(new InputStreamReader(socketClient.getInputStream()));
                PrintWriter out = new PrintWriter(socketClient.getOutputStream(),true);
                out.println(enviadoJson.toString());
                respuestaJson = new JSONArray(in.readLine());
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                if(socketClient != null)
                    try {
                        socketClient.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void o) {
            //Lo que se desea hacer en este metodo es verificar la respuesta del servidor y modificar los views de acuerdo a ello.

            actualizarPanatalla(respuestaJson.toString());
            super.onPostExecute(o);
        }

    }

    public void actualizarPanatalla(String jsonString){
        JSONArray respuestaJson = null;
        try {
            respuestaJson = new JSONArray(jsonString);
            //switchLuz:
            JSONObject obj = (JSONObject) respuestaJson.get(0);
            if(switchLuz.isChecked() && obj.getInt("Luz") == 0){
                switchLuz.toggle();
            }else{
                if(!switchLuz.isChecked() && obj.getInt("Luz") == 1){
                    switchLuz.toggle();
                }
            }

            //SwitchLed:
            obj = (JSONObject) respuestaJson.get(1);
            if(switchLed.isChecked() && obj.getInt("Led") == 0){
                switchLed.toggle();
            }else{
                if(!switchLed.isChecked() && obj.getInt("Led") == 1){
                    switchLed.toggle();
                }
            }

            //RedBar:
            obj = (JSONObject) respuestaJson.get(2);
            redBar.setProgress(obj.getInt("Red"));

            //GreenBar:
            obj = (JSONObject) respuestaJson.get(3);
            greenBar.setProgress(obj.getInt("Green"));

            //BlueBar:
            obj = (JSONObject) respuestaJson.get(4);
            blueBar.setProgress(obj.getInt("Blue"));

            //DimBar:
            obj = (JSONObject) respuestaJson.get(5);
            dimBar.setProgress(obj.getInt("Dim"));

            //switchAuto:
            obj = (JSONObject) respuestaJson.get(6);
            if(switchAuto.isChecked() && obj.getInt("Auto") == 0){
                switchAuto.toggle();
            }else{
                if(!switchAuto.isChecked() && obj.getInt("Auto") == 1){
                    switchAuto.toggle();
                }
            }

            Toast.makeText(getApplicationContext(), "Panatalla actualizada", Toast.LENGTH_SHORT).show();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private final SensorEventListener mSensorListener = new SensorEventListener() {
        private static final int SHAKE_SLOP_TIME_MS = 500;
        private static final int SHAKE_COUNT_RESET_TIME_MS = 6000;
        private static final int SHAKE_TRESHOLD_COUNT = 2;
        private long mShakeTimestamp;
        private int mShakeCount;

        public void onSensorChanged(SensorEvent se) {
            float x = se.values[0];
            float y = se.values[1];
            float z = se.values[2];
            mAccelLast = mAccelCurrent;
            mAccelCurrent = (float) Math.sqrt((double) (x*x + y*y + z*z));
            float delta = mAccelCurrent - mAccelLast;
            mAccel = mAccel * 0.9f + delta; // perform low-cut filter

            if (mAccel > 35) {
                final long now = System.currentTimeMillis();
                // ignore shake events too close to each other (500ms)
                if (mShakeTimestamp + SHAKE_SLOP_TIME_MS > now) {
                    return;
                }
                // reset the shake count after 3 seconds of no shakes
                if (mShakeTimestamp + SHAKE_COUNT_RESET_TIME_MS < now) {
                    mShakeCount = 0;
                }
                mShakeTimestamp = now;
                mShakeCount++;
                if (mShakeCount >= SHAKE_TRESHOLD_COUNT){
                    //Toast toast = Toast.makeText(getApplicationContext(), "Device has shaken.", Toast.LENGTH_SHORT);
                    //toast.show();
                    Random randomGenerator = new Random();
                    redBar.setProgress(randomGenerator.nextInt(100));
                    greenBar.setProgress(randomGenerator.nextInt(100));
                    blueBar.setProgress(randomGenerator.nextInt(100));
                    Vibrator vibrator = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
                    vibrator.vibrate(100);
                    Cambiar cambiar = new Cambiar(ipServer, puerto);
                    cambiar.execute();
                }
            }
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    public void getSpeechInput() {

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es");

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, 10);
        } else {
            Toast.makeText(this, "Your Device Don't Support Speech Input", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case 10:
                if (resultCode == RESULT_OK && data != null) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    //txvResult.setText(result.get(0));
                    String voiceResult = result.get(0);
                    Toast.makeText(getApplicationContext(), voiceResult, Toast.LENGTH_LONG).show();
                    int flagActualizar = 0;
                    //FALTAN AGREGAR TOASTS!!!!!!!!!
                    switch (voiceResult.toLowerCase()){
                        case "prender luz":
                            if(switchLuz.isChecked())
                                Toast.makeText(getApplicationContext(), "La luz ya se encuentra encendida", Toast.LENGTH_LONG).show();
                            else{
                                switchLuz.setChecked(true);
                                Toast.makeText(getApplicationContext(), "Luz encendida", Toast.LENGTH_LONG).show();
                            }
                            break;
                        case "apagar luz":
                            if(switchLuz.isChecked())
                                switchLuz.setChecked(false);
                            else
                                Toast.makeText(getApplicationContext(), "La luz ya se encuentra apagada", Toast.LENGTH_LONG).show();
                            break;
                        case "prender led":
                            if(switchLed.isChecked())
                                Toast.makeText(getApplicationContext(), "El LED ya se encuentra encendido", Toast.LENGTH_LONG).show();
                            else
                                switchLed.setChecked(true);
                            break;
                        case "apagar led":
                            if(switchLed.isChecked())
                                switchLed.setChecked(false);
                            else
                                Toast.makeText(getApplicationContext(), "El LED ya se encuentra apagado", Toast.LENGTH_LONG).show();
                            break;
                        case "led rojo":
                            redBar.setProgress(100);
                            greenBar.setProgress(0);
                            blueBar.setProgress(0);
                            break;
                        case "led azul":
                            redBar.setProgress(0);
                            greenBar.setProgress(0);
                            blueBar.setProgress(100);
                            break;
                        case "led verde":
                            redBar.setProgress(0);
                            greenBar.setProgress(100);
                            blueBar.setProgress(0);
                            break;
                        case "led blanco":
                            redBar.setProgress(100);
                            greenBar.setProgress(100);
                            blueBar.setProgress(100);
                            break;
                        case "led amarillo":
                            redBar.setProgress(100);
                            greenBar.setProgress(100);
                            blueBar.setProgress(0);
                            break;
                        case "led rosa":
                            redBar.setProgress(100);
                            greenBar.setProgress(0);
                            blueBar.setProgress(60);
                            break;
                        case "led naranja":
                            redBar.setProgress(100);
                            greenBar.setProgress(60);
                            blueBar.setProgress(0);
                            break;
                        case "led violeta":
                            redBar.setProgress(60);
                            greenBar.setProgress(0);
                            blueBar.setProgress(100);
                            break;
                        case "led aleatorio":
                            Random randomGenerator = new Random();
                            redBar.setProgress(randomGenerator.nextInt(100));
                            greenBar.setProgress(randomGenerator.nextInt(100));
                            blueBar.setProgress(randomGenerator.nextInt(100));
                            break;
                        case "intensidad alta":
                            dimBar.setProgress(100);
                            break;
                        case "intensidad media":
                            dimBar.setProgress(65);
                            break;
                        case "intensidad baja":
                            dimBar.setProgress(30);
                            break;
                        case "modo automatico activado":
                            if(switchAuto.isChecked())
                                Toast.makeText(getApplicationContext(), "El modo automático ya esta activado", Toast.LENGTH_LONG).show();
                            else
                                switchAuto.setChecked(true);
                            break;
                        case "modo automatico desactivado":
                            if(switchAuto.isChecked())
                                switchAuto.setChecked(false);
                            else
                                Toast.makeText(getApplicationContext(), "El modo automático ya esta desactivado", Toast.LENGTH_LONG).show();
                            break;
                        case "actuaizar":
                            flagActualizar = 1;
                            break;
                        default:
                            Toast.makeText(getApplicationContext(), "Comando no reconocido. Intente nuevamente...", Toast.LENGTH_LONG).show();
                    }

                    if(flagActualizar == 0){
                        Cambiar cambiar = new Cambiar(ipServer, puerto);
                        cambiar.execute();
                    }else{
                        Actualizar actualizar = new Actualizar(ipServer, puerto);
                        actualizar.execute();
                    }

                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.nav_drawer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        ConstraintLayout luces_layout = (ConstraintLayout) findViewById(R.id.luces_layout);
        ConstraintLayout alarma_layout = (ConstraintLayout) findViewById(R.id.alarma_layout);
        ConstraintLayout ajustes_layout = (ConstraintLayout) findViewById(R.id.ajustes_layout);
        ConstraintLayout info_layout = (ConstraintLayout) findViewById(R.id.info_layout);

        if (id == R.id.nav_luces) {
            alarma_layout.setVisibility(View.GONE);
            ajustes_layout.setVisibility(View.GONE);
            info_layout.setVisibility(View.GONE);
            luces_layout.setVisibility(View.VISIBLE);

        } else if (id == R.id.nav_alarma) {
            luces_layout.setVisibility(View.GONE);
            ajustes_layout.setVisibility(View.GONE);
            info_layout.setVisibility(View.GONE);
            alarma_layout.setVisibility(View.VISIBLE);

        } else if (id == R.id.nav_ajustes) {
            luces_layout.setVisibility(View.GONE);
            alarma_layout.setVisibility(View.GONE);
            info_layout.setVisibility(View.GONE);
            ajustes_layout.setVisibility(View.VISIBLE);

        } else if (id == R.id.nav_info) {
            luces_layout.setVisibility(View.GONE);
            alarma_layout.setVisibility(View.GONE);
            ajustes_layout.setVisibility(View.GONE);
            info_layout.setVisibility(View.VISIBLE);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        if(id == DIALOG_ID){
            return new DatePickerDialog(this, dPickerListener, yearAlarm, monthAlarm-1, dayAlarm);
        }
        return null;
    }

    public void definirInterfazAlarma(){
        try {
            StringBuffer buffer = new StringBuffer();
            FileInputStream inputstream = this.getApplicationContext().openFileInput("alarm_file");
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputstream));
            String read = "";
            if (inputstream!=null) {
                if ((read = reader.readLine()) != null) {
                    alarmOn.setVisibility(View.GONE);
                    timePicker.setVisibility(View.GONE);
                    dateBtn.setVisibility(View.GONE);
                    alarmOff.setVisibility(View.VISIBLE);
                    try {
                        JSONArray jsonAlarm = new JSONArray(read);
                        JSONObject jObj = (JSONObject) jsonAlarm.get(3);
                        hourAlarm = jObj.getInt("Hora");
                        jObj = (JSONObject) jsonAlarm.get(4);
                        minuteAlarm = jObj.getInt("Minuto");
                        jObj = (JSONObject) jsonAlarm.get(0);
                        dayAlarm = jObj.getInt("Día");
                        jObj = (JSONObject) jsonAlarm.get(1);
                        monthAlarm = jObj.getInt("Mes");
                        jObj = (JSONObject) jsonAlarm.get(2);
                        yearAlarm = jObj.getInt("Año");
                        alarm_status.setText("Alarma activada para "+String.format("%02d:%02d", hourAlarm, minuteAlarm)+" hs "+dayAlarm+"/"+monthAlarm+"/"+yearAlarm);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            alarm_status.setText(""); //si no puede accerder al archivo (xq no existe o fue borrado)...
            alarmOff.setVisibility(View.GONE);
            alarmOn.setVisibility(View.VISIBLE);
            timePicker.setVisibility(View.VISIBLE);
            dateBtn.setVisibility(View.VISIBLE);
        }
    }
}
