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
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

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

    final static String off="A;";//off
    final static String on="B;";//on
    static String singleColor_String;
    static String fade_String;
    static String qWest_String;



    private ProgressDialog progressDialog;
    ToggleButton btnOn_Off;

    //singleColor
    ToggleButton singleColor_on_off;
    Button singleColor_send;
    LinearLayout singleColor_View, singleColor_full;
    ImageView singleColor_Image;
    Bitmap singleColor_bitmap;
    View singleColor_ImageColor;
    int singleColorR, singleColorG, singleColorB;
    String singleColorR_String, singleColorG_String, singleColorB_String;

    //qWest
    LinearLayout qWest_full, qWest_Title, qWest_Label, qWest_Custom;
    ToggleButton qWest_on_off;
    Button qWest_send;
    Bitmap qWest_bitmap;
    ImageView qWest_Image;
    View qWest_ImageColor;
    int qWestR, qWestG, qWestB, qWestSpeed;
    SeekBar qWest_speedbar;
    boolean colorChosen, speedChosen;
    TextView qWest_speedBar_Progress;
    String qWestR_String, qWestG_String, qWestB_String, qWestSpeed_String;

    //fade
    LinearLayout fade_full, fade_layout, fade_label;
    ToggleButton fade_on_off;
    Button fade_send;
    SeekBar fade_speedbar;
    TextView fade_speedBar_Progress;
    String fadeSpeed_String;
    int fadeSpeed;


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
        singleColor_send =(Button) findViewById(R.id.send_singlecolor);
        singleColor_ImageColor = (View) findViewById(R.id.singleColor_colorwheel_color);
        singleColor_full = (LinearLayout) findViewById(R.id.singleColor_full);

        //qWest
        qWest_full = (LinearLayout) findViewById(R.id.qWest_full);
        qWest_on_off = (ToggleButton) findViewById(R.id.q_west_on_off);
        qWest_Title = (LinearLayout) findViewById(R.id.qWest_Title);
        qWest_Label = (LinearLayout) findViewById(R.id.qWest_Label);
        qWest_Image = (ImageView) findViewById(R.id.qWest_colorwheel);
        qWest_ImageColor = (View) findViewById(R.id.qWest_colorwheel_color);
        qWest_send = (Button) findViewById(R.id.send_qWest);
        qWest_speedbar = (SeekBar) findViewById(R.id.qWest_speedbar);
        colorChosen = false;
        speedChosen = false;
        qWest_speedBar_Progress = (TextView) findViewById(R.id.qWest_speedBar_Progress);
        qWest_Custom = (LinearLayout) findViewById(R.id.qWest_Custom);

        //fade
        fade_full = (LinearLayout) findViewById(R.id.fade_full);
        fade_on_off = (ToggleButton) findViewById(R.id.fade_on_off);
        fade_layout = (LinearLayout) findViewById(R.id.fade_layout);
        fade_label = (LinearLayout) findViewById(R.id.fade_label);
        fade_send = (Button) findViewById(R.id.send_fade);
        fade_speedbar = (SeekBar) findViewById(R.id.fade_speedbar);
        fade_speedBar_Progress = (TextView) findViewById(R.id.fade_speedBar_Progress);


        singleColor_full.setVisibility(View.GONE);
        qWest_full.setVisibility(View.GONE);
        qWest_send.setVisibility(View.GONE);
        qWest_Label.setVisibility(View.GONE);
        qWest_Custom.setVisibility(View.GONE);


        Intent intent = getIntent();
        Bundle b = intent.getExtras();
        mDevice = b.getParcelable(MainActivity.DEVICE_EXTRA);
        mDeviceUUID = UUID.fromString(b.getString(MainActivity.DEVICE_UUID));
        mMaxChars = b.getInt(MainActivity.BUFFER_SIZE);

        Log.d(TAG, "Ready");

        //on / off
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


        // single color
        singleColor_on_off.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    singleColor_View.setVisibility(View.VISIBLE);
                    fade_on_off.setChecked(false);
                    qWest_on_off.setChecked(false);
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

                    singleColor_send.setVisibility(View.VISIBLE);
                }
                return true;
            }
        });
        singleColor_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                singleColor_String = "C" + setToLenght3(String.valueOf(singleColorR)) + setToLenght3(String.valueOf(singleColorG)) + setToLenght3(String.valueOf(singleColorB))+";";
                sendMsg(singleColor_String);
            }
        });

        // Fade
        fade_on_off.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    fade_label.setVisibility(View.VISIBLE);
                    singleColor_on_off.setChecked(false);
                    qWest_on_off.setChecked(false);
                }
                else {
                    fade_label.setVisibility(View.GONE);
                }
            }
        });
        fade_speedbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progress -= 100;
                fade_speedBar_Progress.setText("" + -progress + "%");
                fadeSpeed = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                speedChosen = true;
                    fade_send.setVisibility(View.VISIBLE);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        fade_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fade_String = "F" + setToLenght3(String.valueOf(fadeSpeed))+";";
                sendMsg(fade_String);
            }
        });

        // qWest
        qWest_on_off.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    qWest_Label.setVisibility(View.VISIBLE);
                    qWest_Custom.setVisibility(View.VISIBLE);
                    fade_on_off.setChecked(false);
                    singleColor_on_off.setChecked(false);
                }
                else {
                    qWest_Label.setVisibility(View.GONE);
                    colorChosen = false;
                    speedChosen = false;
                    qWest_Label.setVisibility(View.GONE);
                    qWest_send.setVisibility(View.GONE);
                    qWest_Custom.setVisibility(View.GONE);

                }
            }
        });
        qWest_Image.setDrawingCacheEnabled(true);
        qWest_Image.buildDrawingCache(true);
        qWest_Image.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE){
                    qWest_bitmap = qWest_Image.getDrawingCache();

                    int pixel = qWest_bitmap.getPixel((int)event.getX(), (int)event.getY());

                    qWestR = Color.red(pixel);
                    qWestG = Color.green(pixel);
                    qWestB = Color.blue(pixel);

                    qWest_ImageColor.setBackgroundColor(Color.rgb(qWestR, qWestG, qWestB));

                    colorChosen = true;

                    if (colorChosen && speedChosen){
                        qWest_send.setVisibility(View.VISIBLE);
                    }

                }
                return true;
            }
        });
        qWest_speedbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progress -= 100;
                qWest_speedBar_Progress.setText("" + -progress + "%");
                qWestSpeed = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                speedChosen = true;
                if (colorChosen && speedChosen){
                    qWest_send.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                qWestSpeed_String = String.valueOf(qWestSpeed);
            }
        });
        qWest_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                qWest_String = "Q" + setToLenght3(qWestSpeed_String) + setToLenght3(String.valueOf(qWestR)) + setToLenght3(String.valueOf(qWestG)) + setToLenght3(String.valueOf(qWestB)) +";";
                sendMsg(qWest_String);
            }
        });

    }

    private void setVisibilityVisible(){
        singleColor_full.setVisibility(View.VISIBLE);
        qWest_full.setVisibility(View.VISIBLE);
        qWest_Title.setVisibility(View.VISIBLE);
        fade_full.setVisibility(View.VISIBLE);
        fade_layout.setVisibility(View.VISIBLE);

    }
    private void setVisibilityGone(){
        singleColor_full.setVisibility(View.GONE);
        qWest_full.setVisibility(View.GONE);
        qWest_Title.setVisibility(View.GONE);
        fade_full.setVisibility(View.GONE);
        fade_layout.setVisibility(View.GONE);
    }

    private String setToLenght3(String value){
        int tempLength = value.toCharArray().length;
        switch (tempLength){
            case 1:
                return "0" + "0" + value;
            case 2:
                return "0" + value;
            case 3:
                return value;
            default:
                return "000";
        }
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
