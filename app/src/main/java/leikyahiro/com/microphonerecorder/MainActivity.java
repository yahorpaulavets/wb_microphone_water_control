package leikyahiro.com.microphonerecorder;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.List;

import leikyahiro.com.miclib.Recorder;

public class MainActivity extends AppCompatActivity {

    boolean isRecording = false;
    private Recorder mRecorder;
    private Thread recorderThread;
    private boolean isListening;
    private WaterView mWaterView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        isRecording = false;
        isListening = true;
        mRecorder = Recorder.getInstance(getServerIpAddress());
        mWaterView = (WaterView) findViewById(R.id.water_view);
        if(getServerIpAddress() == null) {
            openSettings();
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mRecorder.isRecording()) {
                    startRecording();
                    Snackbar.make(view, getMessage(), Snackbar.LENGTH_LONG).setAction("Action", null).show();
                } else {
                    stopRecording();
                    Snackbar.make(view, getMessage(), Snackbar.LENGTH_LONG).setAction("Action", null).show();
                }
            }
        });
    }

    private void openSettings() {
        Intent i = new Intent();
        i.setClass(this, Settings.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(new Intent(i));
    }

    private String getServerIpAddress() {
        return PreferencesHelper.getIp(this);
    }

    private String getMessage() {
        return isRecording ? getString(R.string.monitoring_started) :  getString(R.string.monitoring_stopped);
    }

    private void startSocketListener() {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    DatagramSocket socket = new DatagramSocket(8888, getBroadcastAddress());
                    socket.setBroadcast(true);

                    while (isListening) {
                        Log.i(MainActivity.class.getSimpleName(),"Ready to receive broadcast packets: " + getBroadcastAddress());

                        //Receive a packet
                        byte[] recvBuf = new byte[15000];
                        DatagramPacket packet = new DatagramPacket(recvBuf, recvBuf.length);
                        socket.receive(packet);

                        //Packet received
                        Log.i(MainActivity.class.getSimpleName(), "Packet received from: " + packet.getAddress().getHostAddress());
                        final String data = new String(packet.getData()).trim();
                        Log.i(MainActivity.class.getSimpleName(), "Packet received; data: " + data);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "SERVER MESSAGE: " + data, Toast.LENGTH_SHORT).show();
                                if(data.contains("true")) {
                                    mWaterView.incrementWaterLevel(20);
                                }
                            }
                        });
                    }
                } catch (IOException ex) {
                    Log.i(MainActivity.class.getSimpleName(), "Oops" + ex.getMessage());
                }
            }
        });

        t.start();
    }

    private void stopRecording() {
        isRecording = false;
        mRecorder.stopRecording();
    }

    private void startRecording() {
        isRecording = true;
        mRecorder = Recorder.getInstance(getServerIpAddress());
        recorderThread = new Thread(new Runnable() {
            @Override
            public void run() {
                mRecorder.startRecording();
            }
        });

        recorderThread.start();
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
            openSettings();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        isListening = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        isListening = true;
        startSocketListener();
    }

    private static InetAddress getBroadcastAddress() throws IOException {
        Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
        while (en.hasMoreElements()) {
            NetworkInterface ni = en.nextElement();
            System.out.println(" Display Name = " + ni.getDisplayName());

            List<InterfaceAddress> list = ni.getInterfaceAddresses();
            if(ni.isLoopback()) { continue; }
            if(!ni.supportsMulticast()) {continue;}
            for (InterfaceAddress ia : list) {
                System.out.println(" Broadcast = " + ia.getBroadcast());

                if (ia.getBroadcast() != null) {
                    return ia.getBroadcast();
                }
            }
        }

        return null;
    }
}
