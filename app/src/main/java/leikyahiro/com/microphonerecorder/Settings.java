package leikyahiro.com.microphonerecorder;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class Settings extends AppCompatActivity {

    private EditText mEtServerIp;
    private Button mBtnSave;
    private Button mBtnCancel;
    private boolean doNotSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mEtServerIp = (EditText)findViewById(R.id.server_address);
        mBtnSave = (Button)findViewById(R.id.btn_save);
        mBtnCancel = (Button)findViewById(R.id.btn_cancel);

        mBtnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doNotSave = false;
                finish();
            }
        });

        mBtnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        doNotSave = true;
        String ip = PreferencesHelper.getIp(this);
        if(!TextUtils.isEmpty(ip)) {
            mEtServerIp.setText(ip);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if(doNotSave) {return;}

        try {
            String ip = mEtServerIp.getText().toString().trim();
            InetAddress address = InetAddress.getByName(ip);
            PreferencesHelper.setServerIpKey(this, ip);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Address is wrong", Toast.LENGTH_LONG).show();
        }
    }
}
