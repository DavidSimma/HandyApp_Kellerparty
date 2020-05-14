package com.simmaszimma.kellerparty;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.ToggleButton;
import com.simmaszimma.kellerparty.R;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public class Controlling extends Activity {
    private static final String TAG = "BlueTest5-Controlling";
    private int mMaxChars = 50000;//Default//change this to string..........
    private UUID mDeviceUUID;
    private BluetoothSocket mBTSocket;
    private ReadInput mReadThread = null;

    private boolean mIsUserInitiatedDisconnect = false;
    private boolean mIsBluetoothConnected = false;


    private Button mBtnDisconnect;
    private BluetoothDevice mDevice;

    final static String off="A0000000000";//off
    final static String on="B0000000000";//on
    static String singleColorString;
    static String fade_String;
    static String q_west_String;



    private ProgressDialog progressDialog;
    ToggleButton btnOn_Off;

    //singleColor
    ToggleButton singleColor_on_off;
    Button singleColor_set;
    LinearLayout singleColor_View, singleColor_full;
    ImageView singleColor_Image;
    Bitmap singleColor_bitmap;
    View singleColor_ImageColor;
    int singleColorR, singleColorG, singleColorB;

    //qWest
    LinearLayout qWest_full, qWest_Title, qWest_Label;
    ToggleButton qWest_on_off;
    Button qWest_send;
    Bitmap qWest_bitmap;
    ImageView qWest_Image;
    View qWest_ImageColor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controlling);



        ActivityHelper.initialize(this);
        btnOn_Off=(ToggleButton) findViewById(R.id.LED_ein_aus);

        //singleColor
        singleColor_on_off =(ToggleButton) findViewById(R.id.singlecolor);
        singleColor_View = (LinearLayout) findViewById(R.id.singleColorLayout);
        singleColor_Image = (ImageView) findViewById(R.id.singleColor_colorwheel);
        singleColor_set=(Button) findViewById(R.id.send_singlecolor);
        singleColor_ImageColor = (View) findViewById(R.id.singleColor_colorwheel_color);
        singleColor_full = (LinearLayout) findViewById(R.id.singleColor_full);

        //qWest
        qWest_full = (LinearLayout) findViewById()


        singleColor_full.setVisibility(View.GONE);

        Intent intent = getIntent();
        Bundle b = intent.getExtras();
        mDevice = b.getParcelable(MainActivity.DEVICE_EXTRA);
        mDeviceUUID = UUID.fromString(b.getString(MainActivity.DEVICE_UUID));
        mMaxChars = b.getInt(MainActivity.BUFFER_SIZE);

        Log.d(TAG, "Ready");

        btnOn_Off.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    sendMsg(on);
                    btnOn_Off.setBackgroundColor(Color.GREEN);
                    setVisibilityVisible();
                }
                else {
                    sendMsg(off);
                    btnOn_Off.setBackgroundColor(Color.RED);
                    setVisibilityGone();

                }
            }
        });


        singleColor_on_off.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    singleColor_View.setVisibility(View.VISIBLE);
                }
                else {
                    singleColor_View.setVisibility(View.GONE);
                }
            }
        });

        singleColor_Image.setDrawingCacheEnabled(true);
        singleColor_Image.buildDrawingCache(true);
        singleColor_Image.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE){
                    singleColor_bitmap = singleColor_Image.getDrawingCache();

                    int pixel = singleColor_bitmap.getPixel((int)event.getX(), (int)event.getY());

                    singleColorR = Color.red(pixel);
                    singleColorG = Color.green(pixel);
                    singleColorB = Color.blue(pixel);

                    singleColor_ImageColor.setBackgroundColor(Color.rgb(singleColorR, singleColorG, singleColorB));

                    singleColor_set.setVisibility(View.VISIBLE);
                    singleColorString = "C" + "0" + singleColorR + singleColorG + singleColorB;
                }
                return true;
            }
        });

        singleColor_set.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMsg(singleColorString);
            }
        });

    }

    private void setVisibilityVisible(){
        singleColor_full.setVisibility(View.VISIBLE);
    }
    private void setVisibilityGone(){
        singleColor_full.setVisibility(View.GONE);
    }

    private void sendMsg(String txt){
        try {
            mBTSocket.getOutputStream().write(txt.getBytes());

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private class ReadInput implements Runnable {

        private boolean bStop = false;
        private Thread t;

        public ReadInput() {
            t = new Thread(this, "Input Thread");
            t.start();
        }

        public boolean isRunning() {
            return t.isAlive();
        }

        @Override
        public void run() {
            InputStream inputStream;

            try {
                inputStream = mBTSocket.getInputStream();
                while (!bStop) {
                    byte[] buffer = new byte[256];
                    if (inputStream.available() > 0) {
                        inputStream.read(buffer);
                        int i = 0;
                        /*
                         * This is needed because new String(buffer) is taking the entire buffer i.e. 256 chars on Android 2.3.4 http://stackoverflow.com/a/8843462/1287554
                         */
                        for (i = 0; i < buffer.length && buffer[i] != 0; i++) {
                        }
                        final String strInput = new String(buffer, 0, i);

                        /*
                         * If checked then receive text, better design would probably be to stop thread if unchecked and free resources, but this is a quick fix
                         */



                    }
                    Thread.sleep(500);
                }
            } catch (IOException e) {
// TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InterruptedException e) {
// TODO Auto-generated catch block
                e.printStackTrace();
            }

        }

        public void stop() {
            bStop = true;
        }

    }

    private class DisConnectBT extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Void doInBackground(Void... params) {//cant inderstand these dotss

            if (mReadThread != null) {
                mReadThread.stop();
                while (mReadThread.isRunning())
                    ; // Wait until it stops
                mReadThread = null;

            }

            try {
                mBTSocket.close();
            } catch (IOException e) {
// TODO Auto-generated catch block
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            mIsBluetoothConnected = false;
            if (mIsUserInitiatedDisconnect) {
                finish();
            }
        }

    }

    private void msg(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onPause() {
        if (mBTSocket != null && mIsBluetoothConnected) {
            new DisConnectBT().execute();
        }
        Log.d(TAG, "Paused");
        super.onPause();
    }

    @Override
    protected void onResume() {
        if (mBTSocket == null || !mIsBluetoothConnected) {
            new ConnectBT().execute();
        }
        Log.d(TAG, "Resumed");
        super.onResume();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "Stopped");
        super.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
// TODO Auto-generated method stub
        super.onSaveInstanceState(outState);
    }

    private class ConnectBT extends AsyncTask<Void, Void, Void> {
        private boolean mConnectSuccessful = true;

        @Override
        protected void onPreExecute() {

            progressDialog = ProgressDialog.show(Controlling.this, "Hold on", "Connecting");// http://stackoverflow.com/a/11130220/1287554

        }

        @Override
        protected Void doInBackground(Void... devices) {

            try {
                if (mBTSocket == null || !mIsBluetoothConnected) {
                    mBTSocket = mDevice.createInsecureRfcommSocketToServiceRecord(mDeviceUUID);
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    mBTSocket.connect();
                }
            } catch (IOException e) {
// Unable to connect to device`
                // e.printStackTrace();
                mConnectSuccessful = false;



            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            if (!mConnectSuccessful) {
                Toast.makeText(getApplicationContext(), "Could not connect to device.Please turn on your Hardware", Toast.LENGTH_LONG).show();
                finish();
            } else {
                msg("Connected to device");
                mIsBluetoothConnected = true;
                mReadThread = new ReadInput(); // Kick off input reader
            }

            progressDialog.dismiss();
        }

    }
    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
    }
}
