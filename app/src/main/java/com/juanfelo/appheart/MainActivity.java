package com.juanfelo.appheart;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;



import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.Set;
import java.util.Date;

import zephyr.android.HxMBT.BTClient;
import zephyr.android.HxMBT.ZephyrProtocol;

public class MainActivity extends ActionBarActivity {

/********** DECLARACION DE VARIABLES *****************/

    String user = "juanlara@taxy.co";
    String pass = "6P9#DTH-$+hK";
    String ip = "ftp.taxy.co";
    String TAG = "AVISO";
    public FTPClient mFTPClient = null;
    BluetoothAdapter adapter = null;
    BTClient _bt;
    ZephyrProtocol _protocol;
    NewConnectedListener _NConnListener;
    private final int HEART_RATE  = 0x100;
    private final int INSTANT_SPEED = 0x101;
    private final int SKIN_TEMP  = 0x102;
    private final int POSTURE  = 0x111;
    private final int PEACK_ACC  = 0x1000;
    int cont = 0;
    boolean flag1 = false, flag2 = false;
    public TextView tvx, tv1, tv2, tv3, tv4, tv5;
    public float	x1;
    EditText ActFisica, Contador;
    OutputStreamWriter osw;
    String calendar, condfisica;
    String nomarchivo = "File.txt";
    String[] contenido1 = new String[1000];
    String[] contenido2 = new String[1000];
    File file;
    final Time today = new Time(Time.getCurrentTimezone());
    Typeface mTf1, mTf2;
    TextView tv_1, tv_2, welcome;
    Button Borrar;


    /****************ACCESO AL SERVIDO FTP - ENVIO DE ARCHIVO AL SERVIDO*********/
    private class MiTarea extends AsyncTask<String, Float, Integer> {

        protected void onPreExecute() {
        //    Toast.makeText(getApplicationContext(), "Subiendo archivo...", Toast.LENGTH_SHORT).show();
        }
        /************CODIGO PARA ACCEDER POR USUARIO Y CONTRASENA AL SERVIDO*********/
        protected Integer doInBackground(String... urls) {

            try {
                FTPClient ftpClient = new FTPClient();
                ftpClient.connect(InetAddress.getByName(ip)); // direccion o nombre del host
                ftpClient.login(user, pass); // usuario y login del ftp

                ftpClient.changeWorkingDirectory("/Servidor"); // carpeta del servidor donde vamos a guardar el archivo

                ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
                BufferedInputStream buffIn=null;

                /**********************SUBIDA AL ARCHIVO******************/

                buffIn=new BufferedInputStream(new FileInputStream("/storage/sdcard0/Download/BitTorrent Sync/BitTorrent/File.txt")); // ruta del archivo a subir en el dispositivo android

                ftpClient.enterLocalPassiveMode();
                ftpClient.storeFile("File.txt", buffIn); // nombre del archivo aL subir
                buffIn.close();
                ftpClient.logout();
                ftpClient.disconnect();

                /************ERROR SI NO FUNCIONA EL ACCESO*****************/

            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), "Error...", Toast.LENGTH_SHORT).show();
            }
            return null;
        }
        protected void onPostExecute(Integer bytes) {
        //    Toast.makeText(getApplicationContext(), "Archivo subido...", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTf1 = Typeface.createFromAsset(getAssets(), "DS-DIGI.TTF");
        mTf2 = Typeface.createFromAsset(getAssets(), "Future-Earth.ttf");
        tv_1 = (EditText)findViewById(R.id.labelHeartRate);
        tv_2 = (EditText)findViewById(R.id.labelInstantSpeed);
        welcome = (TextView) findViewById(R.id.Welcometext);
        tv_1.setTypeface(mTf1);
        tv_2.setTypeface(mTf1);
        welcome.setTypeface(mTf2);

        ActFisica = (EditText)findViewById(R.id.etActividadFisica);
        Contador = (EditText)findViewById(R.id.etCont);

        file = new File("/storage/sdcard0/Download/BitTorrent Sync/BitTorrent/" + nomarchivo);

        /****ENVIO DE MESAJE ANDROID PARA INICIAR LA SOLICITUD DE VINCULACION BT*******/
        IntentFilter filter = new IntentFilter("android.bluetooth.device.action.PAIRING_REQUEST");

        /****REGISTRO DE NUEVO RECEPTOR BT BROADCAST***/
        this.getApplicationContext().registerReceiver(new BTBroadcastReceiver(), filter);

        /****REGISTRO DEL BT EN LA SOLICITUD EL ESTADO DEL RECEPTOR HA CAMBIADO A EMPAREJADOS***/
        IntentFilter filter2 = new IntentFilter("android.bluetooth.device.action.BOND_STATE_CHANGED");
        this.getApplicationContext().registerReceiver(new BTBondReceiver(), filter2);

        /***VISUALIZACION QUE LA CONEXION BLUETOOTH CON EL DISPOSITIVO AUN NO SE HA REALIZADO****/
        tvx = (TextView) findViewById(R.id.labelStatusMsg);
        String ErrorText  = "HxM Sin Conectar ! !";
        tvx.setText(ErrorText);

        /***OBTENCION DE LA CONEXION CUANDO SE ACTIVE EL BOTON***/
        Button btnConnect = (Button) findViewById(R.id.ButtonConnect);
        if (btnConnect != null)
        {
            btnConnect.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    //*********MAC DEL HXM***********//
                    String BhMacID = "00:07:80:9D:8A:E8";      

                    adapter = BluetoothAdapter.getDefaultAdapter();

                    Set<BluetoothDevice> pairedDevices = adapter.getBondedDevices();

                    if (pairedDevices.size() > 0)
                    {
                        for (BluetoothDevice device : pairedDevices)
                        {
                            if (device.getName().startsWith("HXM"))
                            {
                                BluetoothDevice btDevice = device;
                                BhMacID = btDevice.getAddress();
                                break;

                            }
                        }


                    }

                    
                    BluetoothDevice Device = adapter.getRemoteDevice(BhMacID);
                    /*********VISUALIZA NOMBRE DEL DISPOSITIVO CONECTADO********/
                    String DeviceName = Device.getName();
                    _bt = new BTClient(adapter, BhMacID);
                    /*********CONECTANDO PARA RECIBIR LAS TRAMAS DEL DISPOSITIVO***/
                    _NConnListener = new NewConnectedListener(Newhandler,Newhandler);
                    _bt.addConnectedEventListener((zephyr.android.HxMBT.ConnectedListener<BTClient>) _NConnListener);

                    /**********GUARDA LOS DATOS EN VARIABLES STRING PARA SE VISUALIZADAS*****/


                    tv1 = (EditText)findViewById(R.id.labelHeartRate);
                    tv1.setText("000");

                    tv2 = (EditText)findViewById(R.id.labelInstantSpeed);
                    tv2.setText("0.0");



                    /***********VERIFICA CONEXION COMPLETA DEL BT*****/
                    if(_bt.IsConnected())
                    {
                        _bt.start();
                        TextView tv = (TextView) findViewById(R.id.labelStatusMsg);
                        String ErrorText  = "Conectado HxM"+DeviceName;
                        tv.setText(ErrorText);



                    }
                    else
                    {
                        TextView tv = (TextView) findViewById(R.id.labelStatusMsg);
                        String ErrorText  = "Conexion Invalida!";
                        tv.setText(ErrorText);
                    }

                    //new MiTarea().execute();

                }
            });
        }
        /***********BOTON PARA DESCONEXION BT CON EL DISPOSITIVO*******/
        Button btnDisconnect = (Button) findViewById(R.id.ButtonDisconnect);
        if (btnDisconnect != null)
        {
            btnDisconnect.setOnClickListener(new View.OnClickListener() {
                @Override

                public void onClick(View v) {
                    // TODO Auto-generated method stub

                    TextView tv = (TextView) findViewById(R.id.labelStatusMsg);
                    String ErrorText  = "Desconectado HXM!";
                    tv.setText(ErrorText);


                    _bt.removeConnectedEventListener((zephyr.android.HxMBT.ConnectedListener<BTClient>) _NConnListener);

                    _bt.Close();

                    new MiTarea().isCancelled();

                }
            });
        }

        /*********BOTON PARA GUARDAR EN EL SERVIDOR EL ARCHIVO DE LOS DATOS RECIBIDO DEL HXM*********/
        Borrar = (Button) findViewById(R.id.BorraArchivo);
        if (Borrar != null){
            Borrar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean deleted = file.delete();
                }
            });
        }


    }

    private class BTBondReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle b = intent.getExtras();
            BluetoothDevice device = adapter.getRemoteDevice(b.get("android.bluetooth.device.extra.DEVICE").toString());
            Log.d("Bond state", "BOND_STATED = " + device.getBondState());
        }
    }

    /******GENERAR UNA CONEXION BROADCAST PARA ENCONTRAR EL DISPOSITIVO QUE SE ENCUENTRA ACTIVADO EL BT*********/
    private class BTBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("BTIntent", intent.getAction());
            Bundle b = intent.getExtras();
            Log.d("BTIntent", b.get("android.bluetooth.device.extra.DEVICE").toString());
            Log.d("BTIntent", b.get("android.bluetooth.device.extra.PAIRING_VARIANT").toString());
            try {
                /*****GENERAR UNA VINCULACION YA ENCONTRADO EL DISPOSITIVO Y DIGITAR CLAVE DE APAREO*************/
                BluetoothDevice device = adapter.getRemoteDevice(b.get("android.bluetooth.device.extra.DEVICE").toString());
                Method m = BluetoothDevice.class.getMethod("convertPinToBytes", new Class[] {String.class} );
                byte[] pin = (byte[])m.invoke(device, "1234");
                m = device.getClass().getMethod("setPin", new Class [] {pin.getClass()});
                Object result = m.invoke(device, pin);
                Log.d("BTTest", result.toString());
            /*******GENERACION DE CODIGO SI SE PRESENTA ALGUN PROBLEMA CON LA EJECUCION DEL PROGRAMA***********/
            } catch (SecurityException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            } catch (NoSuchMethodException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            } catch (IllegalArgumentException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    /************ENCARGADO DE RECIBIR DATOS DE LA CLASE NEWCONNECTLISTENER*********/
    final Handler Newhandler = new Handler(){
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                /************SEGUN SEA EL CASO TOMA LOS VALORES PARA SER VISUALIZADOS EN PANTALLA*******/
                case HEART_RATE:
                    String HeartRatetext = msg.getData().getString("HeartRate");
                    System.out.println("Heart Rate Info is "+ HeartRatetext);
                    if (tv_1 != null)tv_1.setText(HeartRatetext);

                    if(cont < contenido1.length){
                        guardar_archivo();
                        cont++;
                    }
                    new MiTarea().execute();
                    break;

                case INSTANT_SPEED:
                    String InstantSpeedtext = msg.getData().getString("InstantSpeed");
                    if (tv_2 != null)tv_2.setText(InstantSpeedtext);
                    x1 = Float.parseFloat(tv2.getText().toString());
                    if(x1 == 0){
                        condfisica = "SENTADO";
                        ActFisica.setText(condfisica);
                    }else {
                        condfisica = "CAMINANDO";
                        ActFisica.setText(condfisica);
                    }
                    break;


            }
        }

    };

    /**********METODO PARA REALIZAR EL GUARDADO DEL ARCHIVO EN LA RUTA INDICADA**************/
    public void guardar_archivo(){

            today.setToNow();
            calendar = today.monthDay + "/" + (today.month + 1) + "/"  + today.year + " "  + today.format("%k:%M:%S");
            try{
            File tarjeta = Environment.getExternalStorageDirectory();
                boolean sobreescribir = false;
                BufferedWriter osw = new BufferedWriter(new FileWriter(file, !sobreescribir));
                contenido1 [cont] = tv1.getText().toString();
                osw.append(calendar + " " + contenido1[cont] + "   "+condfisica);
                osw.newLine();
                osw.flush();
                osw.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            String count = String.valueOf(cont);
            Contador.setText(count);


            //Toast.makeText(getApplicationContext(), "Los datos fueron grabados correctamente",Toast.LENGTH_SHORT).show();
        }
}
