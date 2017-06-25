package com.example.juancruz.lumenapp;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Vibrator;
import android.support.annotation.IntegerRes;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.SparseArray;
import android.util.StringBuilderPrinter;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

public class QRRecognizer extends AppCompatActivity {

    SurfaceView cameraPreview;
    TextView txtResult;
    BarcodeDetector barcodeDetector;
    CameraSource cameraSource;
    final int RequestCameraPermissionID = 1001;
    Intent intent;
    int flagQRDetected = 0;

    public void guardarDatosLogin(String qrleido){

        File file = new File(this.getApplicationContext().getFilesDir(), "login_file");
        FileOutputStream outputstream;
        try {
            outputstream = openFileOutput("login_file" , Context.MODE_PRIVATE);
            outputstream.write(qrleido.getBytes());
            outputstream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Leemos para ver si se guardo correctamente
        /*
        String read = "";
        try {
            StringBuffer buffer = new StringBuffer();
            FileInputStream inputstream = this.getApplicationContext().openFileInput("login_file");
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputstream));
            if (inputstream!=null) {
                while ((read = reader.readLine()) != null) {
                    buffer.append(read + "\n" );
                }
            }
            read= buffer.toString();


            inputstream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String[] qrArray = read.split("\\n");
        Toast.makeText(getApplicationContext(), "Leido de archivo IP: " + qrArray[0] + "\nPuerto: " + qrArray[1] , Toast.LENGTH_LONG).show();
        //Toast.makeText(getApplicationContext(), read , Toast.LENGTH_LONG).show();
        */
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case RequestCameraPermissionID: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    try {
                        cameraSource.start(cameraPreview.getHolder());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrrecognizer);

        cameraPreview = (SurfaceView) findViewById(R.id.cameraPreview);
        txtResult = (TextView) findViewById(R.id.txtResult);
        intent = new Intent(this, MainActivity.class);

        barcodeDetector = new BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.QR_CODE)
                .build();
        cameraSource = new CameraSource
                .Builder(this, barcodeDetector)
                .setRequestedPreviewSize(640, 480)
                .build();
        //Add Event
        cameraPreview.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    //Request Permission
                    ActivityCompat.requestPermissions(QRRecognizer.this, new String[]{android.Manifest.permission.CAMERA}, RequestCameraPermissionID);
                    return;
                }
                try {
                    cameraSource.start(cameraPreview.getHolder());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                cameraSource.stop();
            }
        });

        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {

            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> qrcodes = detections.getDetectedItems();
                if(qrcodes.size() != 0 && flagQRDetected==0){
                    flagQRDetected++;
                    txtResult.post(new Runnable() {
                        @Override
                        public void run() {
                            //Create vibrate
                            Vibrator vibrator = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
                            vibrator.vibrate(100);
                            txtResult.setText(qrcodes.valueAt(0).displayValue);
                            Toast.makeText(getApplicationContext(), qrcodes.valueAt(0).displayValue, Toast.LENGTH_LONG).show();
                            String qrLeido = txtResult.getText().toString();//qrcodes.valueAt(0).displayValue;
                            String[] qrArray = qrLeido.split("\\n");
                            //para validar si los datos son correctos, intentamos establecer la conexion, atrapando la ecepcion.
                            // Si la conexion es exitosa reciviriamos un JSON con el estado actual de los actuadores.
                            //Si no es exitosa se tiene que volver al activity anterior para escanear/ingresar otra vez.
                            VerificarConexion verificar = new VerificarConexion(qrArray[0], Integer.parseInt(qrArray[1]));
                            verificar.execute();
                            //si toda va bien, crea intent con estado de actuadores
                            intent.putExtra("IP", qrArray[0]);
                            intent.putExtra("Puerto", Integer.parseInt(qrArray[1]));
                            try {
                                TimeUnit.MILLISECONDS.sleep(500);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            if(!verificar.getRespuestaJson().isEmpty()) {
                                intent.putExtra("JSON", verificar.getRespuestaJson());
                                startActivity(intent);
                                guardarDatosLogin(qrLeido); //guardar()*
                                Toast.makeText(getApplicationContext(), verificar.getRespuestaJson(), Toast.LENGTH_LONG).show();
                            }else{
                                Toast.makeText(getApplicationContext(), "La re cagaste", Toast.LENGTH_LONG).show();
                            }
                            finish();//cerramos la activity

                        }
                    });
                }

            }
        });
    }

    public class VerificarConexion extends AsyncTask<Void, Void, Void> {
        String dstAddress;
        int dstPort;
        JSONArray respuestaJson = null;
        JSONArray enviadoJson = null;
        int flag = 0;

        VerificarConexion(String addr, int port){
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

        public String getRespuestaJson() {
            if(respuestaJson != null)
                return respuestaJson.toString();
            return "";
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
                flag++;
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

            if(flag != 0)
                Toast.makeText(getApplicationContext(), "Conexion establecida", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(getApplicationContext(), "Conexion fallida", Toast.LENGTH_SHORT).show();

            super.onPostExecute(o);
        }

    }
}
