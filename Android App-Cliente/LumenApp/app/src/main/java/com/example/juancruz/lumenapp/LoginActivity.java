package com.example.juancruz.lumenapp;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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
import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {
    EditText ipText;
    EditText puertoText;
    Button conectarBtn;
    String ipServer;
    int puerto;
    Intent intent;
    Intent intent1;
    Intent intentQR;
    ImageButton qrBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(comprobarDatosGuardados() == false){
            setContentView(R.layout.activity_login);
            ipText = (EditText) findViewById(R.id.editText);
            puertoText = (EditText) findViewById(R.id.editText2);
            conectarBtn = (Button) findViewById(R.id.button);
            intent = new Intent(this, MainActivity.class);
            intentQR = new Intent(this, QRRecognizer.class);
            qrBtn = (ImageButton) findViewById(R.id.imageButton2);


            conectarBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ipServer = ipText.getText().toString();
                    puerto = Integer.parseInt(puertoText.getText().toString());

                    VerificarConexion verificar = new VerificarConexion(ipServer, puerto);
                    verificar.execute();
                    try {
                        TimeUnit.MILLISECONDS.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    intent.putExtra("IP", ipServer);
                    intent.putExtra("Puerto", puerto);  //esto se va a tener que modificar, para que despues el MainActivity lea del archivo estos datos
                    if(!verificar.getRespuestaJson().isEmpty()) {
                        intent.putExtra("JSON", verificar.getRespuestaJson());
                        startActivity(intent);
                        guardarDatosLogin(ipServer, puerto); //guardar()*
                        Toast.makeText(getApplicationContext(), verificar.getRespuestaJson(), Toast.LENGTH_LONG).show();
                        finish();//cerramos la activity
                    }else{
                        Toast.makeText(getApplicationContext(), "Falla al establecer conexión. Ingresar nuevamente los datos", Toast.LENGTH_LONG).show();
                        ipText.setText("");
                        puertoText.setText("");
                    }
                }
            });

            qrBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(intentQR);
                    finish();
                }
            });
        }
        else{
            intent = new Intent(this, MainActivity.class);
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
            String[] loginArray = read.split("\\n");
            ipServer = loginArray[0];
            puerto = Integer.parseInt(loginArray[1]);
            VerificarConexion verificar = new VerificarConexion(ipServer, puerto);
            verificar.execute();
            try {
                TimeUnit.MILLISECONDS.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            intent.putExtra("IP", ipServer);
            intent.putExtra("Puerto", puerto);
            if(!verificar.getRespuestaJson().isEmpty()) {
                intent.putExtra("JSON", verificar.getRespuestaJson());
                startActivity(intent);
                finish();//cerramos la activity
            }else{
                setContentView(R.layout.activity_login);
                Toast.makeText(getApplicationContext(), "Falla al establecer conexión. Verifique servidor e ingrese nuevamente los datos.", Toast.LENGTH_LONG).show();
                ipText = (EditText) findViewById(R.id.editText);
                puertoText = (EditText) findViewById(R.id.editText2);
                conectarBtn = (Button) findViewById(R.id.button);
                intent1 = new Intent(this, MainActivity.class);
                intentQR = new Intent(this, QRRecognizer.class);
                qrBtn = (ImageButton) findViewById(R.id.imageButton2);


                conectarBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ipServer = ipText.getText().toString();
                        puerto = Integer.parseInt(puertoText.getText().toString());

                        VerificarConexion verificar = new VerificarConexion(ipServer, puerto);
                        verificar.execute();
                        try {
                            TimeUnit.MILLISECONDS.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        intent1.putExtra("IP", ipServer);
                        intent1.putExtra("Puerto", puerto);
                        if(!verificar.getRespuestaJson().isEmpty()) {
                            intent1.putExtra("JSON", verificar.getRespuestaJson());
                            startActivity(intent1);
                            guardarDatosLogin(ipServer, puerto); //guardar()*
                            Toast.makeText(getApplicationContext(), verificar.getRespuestaJson(), Toast.LENGTH_LONG).show();
                            finish();//cerramos la activity
                        }else{
                            Toast.makeText(getApplicationContext(), "Falla al establecer conexión. Ingresar nuevamente los datos", Toast.LENGTH_LONG).show();
                            ipText.setText("");
                            puertoText.setText("");
                        }
                    }
                });

                qrBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(intentQR);
                        finish();
                    }
                });
            }
        }




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

    public void guardarDatosLogin(String ip, int puerto){

        ip = ip + "\n"; //para despues poder leer bien del archivo.
        File file = new File(this.getApplicationContext().getFilesDir(), "login_file");
        FileOutputStream outputstream;
        try {
            outputstream = openFileOutput("login_file" , Context.MODE_PRIVATE);
            outputstream.write(ip.getBytes());
            outputstream.write(Integer.toString(puerto).getBytes());
            outputstream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean comprobarDatosGuardados(){
        try {
            StringBuffer buffer = new StringBuffer();
            FileInputStream inputstream = this.getApplicationContext().openFileInput("login_file");
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputstream));
            if (inputstream!=null) {
                if (reader.readLine() != null) {
                    return true;
                } else
                    return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
