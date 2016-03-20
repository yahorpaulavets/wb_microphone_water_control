package leikyahiro.com.miclib;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by Yahor on 19.03.2016.
 * (C) All rights reserved.
 */
public class MicLib {
    private Context context;
    private static final long RECORD_DURATION = 5 * 1000;
    private static final int[] mAvailableRates = new int[] { 11025, 8000, 22050, 44100 };

    private static final short[] mAvailableEncodings = new short[] { AudioFormat.ENCODING_DEFAULT,
            AudioFormat.ENCODING_PCM_8BIT, AudioFormat.ENCODING_PCM_16BIT };

    private static final short[] mAvailableChannels = new short[] { AudioFormat.CHANNEL_IN_DEFAULT,
            AudioFormat.CHANNEL_IN_STEREO, AudioFormat.CHANNEL_IN_MONO };
    private AudioTrack audioTrack = null;
    private int mMinBufferSize;
    private short mAudioFormat;
    private short mChannelConfig;
    private int mRate;
    protected volatile boolean mIsRecording = false;
    private File mFile = new File(Environment.getExternalStorageDirectory(), "test.pcm");
    Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if(msg.what == 2) {
                String message = msg.obj.toString();
            } else {

                int approx = (Integer) msg.obj;

                if (approx > 20000) {
                    approx = 100;
                } else {
                    approx *= 0.01;
                }
                Log.e(MicLib.class.getSimpleName(), "HANDLE: " + approx);

            }
            return false;
        }
    });

    public MicLib(Context context) {
        setContext(context);
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public Context getContext() {
        return context;
    }

    public void startRecordingToFile(final AudioRecordCallback callback) {

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                startRecord(callback);
            }
        });

        t.start();
    }

    public void startRecord(AudioRecordCallback callback) {
        AudioRecord audioRecord = null;
        DataOutputStream dataOutputStream = null;
        mIsRecording = true;
        try {
            mHandler.postDelayed(new Runnable() { @Override public void run() { mIsRecording = false; }}, RECORD_DURATION);
            mFile.createNewFile();
            OutputStream outputStream = new FileOutputStream(mFile);
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);
            dataOutputStream = new DataOutputStream(bufferedOutputStream);
            audioRecord = findAvailableAudioRecord();
            if (audioRecord == null) {
                dataOutputStream.close();
                return;
            }
            short[] audioData = new short[mMinBufferSize];
            audioRecord.startRecording();
            while (mIsRecording) {
                int numberOfShort = audioRecord.read(audioData, 0, mMinBufferSize);

                for (int i = 0; i < numberOfShort; i++) {
                    dataOutputStream.writeShort(audioData[i]);
                }
                callback.onFrameRecorded(new FrameData(audioData));
                postAudioLevel(audioData, numberOfShort);
            }


        } catch (IOException e) {
            e.printStackTrace();
            String message = e.getMessage();
            if(message == null) {
                message = "Unable to write audio clip to sdcard (perhaps no space left, or sdcard is not accessible";
            }
            Message msg = new Message();
            msg.what = 2;
            msg.obj = message;

            if(mHandler != null) {
                mHandler.sendMessage(msg);
            }
        } finally {
            if (audioRecord != null) {
                try {
                    audioRecord.stop();
                    audioRecord.release();
                } catch (Exception e) {
                }
            }
            if (dataOutputStream != null) {
                try {
                    dataOutputStream.close();
                } catch (Exception e) {
                }
            }

            callback.onRecordCompleted(mFile);
        }
    }

    private void postAudioLevel(short[] audioData, int numberOfShort) {
        int step = audioData.length/20;

        int approx = 0;
        for(int i = step; i < audioData.length; i+=step) {
            approx += audioData[i];
        }

        approx = Math.abs(approx/5);
        Message msg = new Message();
        msg.obj = approx;

        Log.e(MicLib.class.getSimpleName(), "APPROX: " + approx + " - " + numberOfShort + " " + printAudioData(audioData));

        if(mHandler != null) { mHandler.sendMessage(msg); }
    }

    private String printAudioData(short[] audioData) {
        StringBuilder sb = new StringBuilder(10000);
        for(short s: audioData) {
            sb.append(s).append(",");
        }
        return sb.toString();
    }

    private PlayAsyncTask mPlayAsync;

    public void playFile(File mFileRecorded) {
        playRecordAsync();
    }

    class PlayAsyncTask extends AsyncTask {
        @Override
        protected Object doInBackground(Object... params) {
            playRecordSynchronized();
            return null;
        }

        protected void onPostExecute(Object result) {
//            mButtonPlayRecording.setVisibility(View.GONE);
//            mLinearLayoutHearSoundYesNo.setVisibility(View.VISIBLE);
//            mTextViewPleaseHearSound.setVisibility(View.VISIBLE);
        };
    };

    private void playRecordAsync() {
        mPlayAsync = new PlayAsyncTask();
        mPlayAsync.execute();
    }

    private void playRecordSynchronized() {
        int shortSizeInBytes = Short.SIZE / Byte.SIZE;
        int bufferSizeInBytes = (int) (mFile.length() / shortSizeInBytes);
        short[] audioData = new short[bufferSizeInBytes];
        try {
            InputStream inputStream = new FileInputStream(mFile);
            BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
            DataInputStream dataInputStream = new DataInputStream(bufferedInputStream);
            int i = 0;
            while (dataInputStream.available() > 0) {
                try {
                    audioData[i] = dataInputStream.readShort();
                    i++;
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            dataInputStream.close();

            audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, mRate, mChannelConfig, mAudioFormat,
                    mMinBufferSize, AudioTrack.MODE_STREAM);

            audioTrack.play();
            audioTrack.write(audioData, 0, bufferSizeInBytes);
            audioTrack.stop();
            audioTrack.release();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private AudioRecord findAvailableAudioRecord() {
        for (int rate : mAvailableRates) {
            for (short audioFormat : mAvailableEncodings) {
                for (short channelConfig : mAvailableChannels) {
                    try {
                        mAudioFormat = audioFormat;
                        mChannelConfig = channelConfig;
                        mRate = rate;
                        Log.d("TAG", "Attempting rate " + mRate + "Hz, bits: " + audioFormat + ", channel: "
                                + mChannelConfig);
                        mMinBufferSize = AudioRecord.getMinBufferSize(mRate, mChannelConfig, audioFormat);
                        if (mMinBufferSize != AudioRecord.ERROR_BAD_VALUE) {
                            // check if we can instantiate and have a success
                            AudioRecord recorder = new AudioRecord(MediaRecorder.AudioSource.DEFAULT, mRate, mChannelConfig,
                                    mAudioFormat, mMinBufferSize);
                            if (recorder.getState() == AudioRecord.STATE_INITIALIZED)
                                return recorder;
                        }
                    } catch (Exception e) {
                        Log.e("TAG", mRate + "Exception, keep trying.", e);
                    }
                }
            }
        }
        return null;
    }
}
