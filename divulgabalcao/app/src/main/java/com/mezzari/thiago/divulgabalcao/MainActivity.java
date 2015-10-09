package com.mezzari.thiago.divulgabalcao;

import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends ActionBarActivity {

    TextView c, p, tc, tp;
    Timer timer;
    MyTimerTask myTimerTask;
    static int cbip = 0;
    static int pbip = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        c = (TextView)findViewById(R.id.cesp);
        tc = (TextView)findViewById(R.id.textView);
        p = (TextView)findViewById(R.id.pesp);
        tp = (TextView)findViewById(R.id.textView2);
        if(timer != null){
            timer.cancel();
        }
        timer = new Timer();
        myTimerTask = new MyTimerTask();
        timer.schedule(myTimerTask, 5000, 5000);
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

    public void Chama(String senha){
        try {
            Task T = new Task();
            T.execute(senha);
        }catch (Exception e) {
            System.out.println(e.toString());
            e.printStackTrace();
        }
    }

    public void Apita(int caux, int paux){
        if(caux!=cbip){
            ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
            toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_NETWORK_LITE, 1000);
            cbip = caux;
        }else{
            cbip = caux;
        }
        if(paux!=pbip){
            ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
            toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_NETWORK_LITE, 1000);
            pbip = paux;
        }else{
            pbip = paux;
        }
        cbip = caux;
        pbip = paux;
    }

    public void Fila(){
        try {
            String aux = new FilaTask().execute().get();
            String caux = aux.split("=",2)[1];
            String paux = aux.split("=",3)[2];
            caux = caux.split(";", 2)[0];
            paux = paux.split(";", 2)[0];
            c.setText(caux);
            p.setText(paux);
            Apita(Integer.valueOf(c.getText().toString()),Integer.valueOf(p.getText().toString()));
        }catch (Exception e) {
            System.out.println(e.toString());
            e.printStackTrace();
        }
    }

    public void Total(){
        try {
            String aux = new TotalTask().execute().get();
            String caux = aux.split("=",2)[1];
            String paux = aux.split("=",3)[2];
            caux = caux.split(";", 2)[0];
            paux = paux.split(";", 2)[0];
            tc.setText(caux);
            tp.setText(paux);
        }catch (Exception e) {
            System.out.println(e.toString());
            e.printStackTrace();
        }
    }

    public void onAutoClicked(View v) {
        if (v.getId() == R.id.auto) {
            Chama("AutomaticoCert");
        }
    }

    public void onCPClicked(View v) {
        if (v.getId() == R.id.cp) {
            Chama("CertidaoProximo");
        }
    }

    public void onCRClicked(View v) {
        if (v.getId() == R.id.cr) {
            Chama("RepeteCertidoes");
        }
    }

    public void onPPClicked(View v) {
        if (v.getId() == R.id.pp) {
            Chama("PreferencialProximo");
        }
    }

    public void onPRClicked(View v) {
        if (v.getId() == R.id.pr) {
            Chama("RepetePreferencial");
        }
    }

    class MyTimerTask extends TimerTask {
        @Override
        public void run() {
            //alguma coisa que seta strDate
            runOnUiThread(new Runnable(){
                @Override
                public void run() {
                    Fila();
                    Total();
                }});
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
                String URL = "http://192.168.1.112:9876/webservice.sistemadivulga?wsdl";
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


    class FilaTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... params) {
            try {
                SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                String SOAP_ACTION = "http://webservice.sistemadivulga/server/FilaCertidoes";
                String METHOD_NAME = "FilaCertidoes";
                String NAMESPACE = "http://webservice.sistemadivulga/";
                SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
                String URL = "http://192.168.1.112:9876/webservice.sistemadivulga?wsdl";
                HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);
                envelope.setOutputSoapObject(request);
                androidHttpTransport.call(SOAP_ACTION, envelope);
                SoapObject response = (SoapObject) envelope.bodyIn;
                System.out.println(response.getPropertyCount());
                return response.getProperty(0).toString();
            }catch (Exception e) {
                System.out.println(e.toString());
                e.printStackTrace();
            }
            return null;
        }
    }

    class TotalTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... params) {
            try {
                SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                String SOAP_ACTION = "http://webservice.sistemadivulga/server/TotalCertidoes";
                String METHOD_NAME = "TotalCertidoes";
                String NAMESPACE = "http://webservice.sistemadivulga/";
                SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
                String URL = "http://192.168.1.112:9876/webservice.sistemadivulga?wsdl";
                HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);
                envelope.setOutputSoapObject(request);
                androidHttpTransport.call(SOAP_ACTION, envelope);
                SoapObject response = (SoapObject) envelope.bodyIn;
                System.out.println(response.getPropertyCount());
                return response.getProperty(0).toString();
            }catch (Exception e) {
                System.out.println(e.toString());
                e.printStackTrace();
            }
            return null;
        }
    }
}
