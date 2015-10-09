package com.mezzari.thiago.divulgaregistros;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;


public class MainActivity extends ActionBarActivity {
    public int fila = 200;
    public int atual = 200;
    TextView t, t2;
    static int bip = 0;
    String IP = "http://10.0.2.2:9876/webservice.sistemadivulga?wsdl";

    //Atributos de Servidor SOCKET---------//
    private ServerSocket serverSocket;
    Handler updateConversationHandler;
    Thread serverThread;
    private String text;
    public static final int PORT = 9898;
    //-------------------------------------//

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        t = (TextView)findViewById(R.id.espera);
        t2 = (TextView)findViewById(R.id.textView3);

        //Inicialização do Servidor Socket:
        updateConversationHandler = new Handler();
        this.serverThread = new Thread(new ServerThread());
        this.serverThread.start();
        //---------------------------------------//
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    //Configuração do Servidor Thread:
    class ServerThread implements Runnable{
        public void run(){
            Socket socket = null;
            try{
                serverSocket = new ServerSocket(PORT);
            }catch(IOException e){
                e.printStackTrace();
            }
            while(!Thread.currentThread().isInterrupted()){
                try{
                    socket = serverSocket.accept();
                    CommunicationThread commThread = new CommunicationThread(socket);
                    new Thread(commThread).start();
                }catch(IOException e){
                    e.printStackTrace();
                }
            }
        }
    }

    //Configuração da comunicação Servidor-Cliente
    class CommunicationThread implements Runnable {
        private Socket clientSocket;
        private BufferedReader input;

        public CommunicationThread(Socket clientSocket) {
            this.clientSocket = clientSocket;
            try{
                this.input = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
            }catch(IOException e){
                e.printStackTrace();
            }
        }

        public void run(){
            while(!Thread.currentThread().isInterrupted()){
                try{
                    String read = input.readLine();
                    updateConversationHandler.post(new updateUIThread(read));
                }catch(IOException e){
                    e.printStackTrace();
                }
            }
        }
    }

    //Classe responsável por exibir as informações recebdias
    class updateUIThread implements Runnable {
        private String msg;

        public updateUIThread(String str){
            this.msg = str;
        }

        @Override
        public void run(){

            //exibe janela para aguardar --------------------------------------------------------------------------------------------------------//
            final AlertDialog.Builder dialog;
            dialog = new AlertDialog.Builder(getApplication());
            dialog.setTitle("AGUARDE");
            dialog.setMessage(msg);
            final AlertDialog alert = dialog.create();
            alert.show();
            //-----------------------------------------------------------------------------------------------------------------------------------//

            t.setText(t.getText().toString()+"Client Says: "+ msg + "\n");
            t2.setText(t2.getText().toString()+"Client Says: "+ msg + "\n");
        }
    }

    public void Chama(String senha){
        try {

            //exibe janela para aguardar --------------------------------------------------------------------------------------------------------//
            final AlertDialog.Builder dialog = new AlertDialog.Builder(this).setTitle("AGUARDE").setMessage("Aguarde o Termino do Chamado");
            final AlertDialog alert = dialog.create();
            alert.show();
            //-----------------------------------------------------------------------------------------------------------------------------------//

            // Configura um novo Handler para fechar a janela após algum tempo -------------------------------------------------------------------//
            final Handler handler  = new Handler();
            final Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    if (alert.isShowing()) {
                        alert.dismiss();
                    }
                }
            };
            //--------------------------------------------------------------------------------------------------------------------------------------//

            //Executa o chamado ------------------------------------------------------------------------------------------------------------------//
            Task T = new Task();
            T.execute(senha);
            //------------------------------------------------------------------------------------------------------------------------------------//

            //Configura o tempo e o método de encerramento da janela--------------------------------------------------------------------------------//
            alert.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    handler.removeCallbacks(runnable);
                }
            });

            handler.postDelayed(runnable, 5000);
            //---------------------------------------------------------------------------------------------------------------------------------------//
        }catch (Exception e) {
            System.out.println(e.toString());
            e.printStackTrace();
        }
    }

    public void Apita(int aux){
        if(aux!=bip){
            ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
            toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_NETWORK_LITE, 2000);
        }
        bip = aux;
    }

    public void onAutoClicked(View v){
        if(v.getId() == R.id.button3){
            Chama("RegistrosProximo");
        }
    }

    public void onRepClicked(View v) {
        if (v.getId() == R.id.button2) {
            Chama("RepeteRegistros");
        }
    }

    public void onCallClicked(View v) {
        if (v.getId() == R.id.button) {
            Chama("RegistrosProximo");
        }
    }

    class Task extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            try {
                SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                String SOAP_ACTION = "http://webservice.sistemadivulga/server/" + params[0];
                String METHOD_NAME = params[0];
                String NAMESPACE = "http://webservice.sistemadivulga/";
                SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
                String URL = IP;
                HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);
                envelope.setOutputSoapObject(request);
                androidHttpTransport.call(SOAP_ACTION, envelope);
            } catch (Exception e) {
                System.out.println(e.toString());
                e.printStackTrace();
            }
            return null;
        }
    }
}
