package com.win.ble_demo.bean;

/**
 * 作者：Wilson on 2017-01-19 12:45
 * 邮箱：MorrisWare01@gmail.com
 */
public class SpecifiedDeviceStatusFrame {

    private String deviceName;
    private String deviceNo;
    private String deviceStatus;

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceStatus() {
        return deviceStatus;
    }

    public void setDeviceStatus(String deviceStatus) {
        this.deviceStatus = deviceStatus;
    }

    public String getDeviceNo() {
        return deviceNo;
    }

    public void setDeviceNo(String deviceNo) {
        this.deviceNo = deviceNo;
    }

    public static SpecifiedDeviceStatusFrame parse(String data) {
        SpecifiedDeviceStatusFrame frame = new SpecifiedDeviceStatusFrame();
        String type = data.substring(0, 2);
        switch (type) {
            case "01":
                frame.setDeviceName("数显卡尺");
                break;
            case "02":
                frame.setDeviceName("数显千分尺");
                break;
            case "03":
                frame.setDeviceName("数显百分表");
                break;
            case "04":
                frame.setDeviceName("数显扭矩扳手");
                break;
            case "05":
                frame.setDeviceName("数显深度尺");
                break;
            case "06":
                frame.setDeviceName("里氏硬度计");
        }
        frame.setDeviceNo(String.valueOf(Integer.parseInt(data.substring(2, 8), 16)));
        frame.setDeviceStatus(data.substring(data.length() - 2, data.length())
                .equals("01") ? "在线" : "不在线");
        return frame;
    }
}
