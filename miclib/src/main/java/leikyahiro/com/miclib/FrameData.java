package leikyahiro.com.miclib;

/**
 * Created by Yahor on 19.03.2016.
 * (C) All rights reserved.
 */
public class FrameData {
    private short[] data;

    public FrameData(short[] data) {
        setData(data);
    }

    public void setData(short[] data) {
        this.data = data;
    }

    public short[] getData() {
        return data;
    }
}
