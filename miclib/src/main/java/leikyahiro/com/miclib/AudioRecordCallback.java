package leikyahiro.com.miclib;

import java.io.File;

/**
 * Created by Yahor on 19.03.2016.
 * (C) All rights reserved.
 */
public interface AudioRecordCallback {
    void onFrameRecorded(FrameData data);
    void onRecordCompleted(File file);
}
