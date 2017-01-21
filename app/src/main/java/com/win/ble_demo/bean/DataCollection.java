package com.win.ble_demo.bean;

/**
 * 作者：Wilson on 2017-01-21 09:10
 * 邮箱：MorrisWare01@gmail.com
 */
public class DataCollection {
    private String type;
    private String data;
    private long time;


    public DataCollection() {
    }

    public DataCollection(String type, String data, long time) {
        this.type = type;
        this.data = data;
        this.time = time;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return "DataCollection{" +
                "type='" + type + '\'' +
                ", data='" + data + '\'' +
                ", time=" + time +
                '}';
    }
}

