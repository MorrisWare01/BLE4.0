package com.win.ble_demo.bean;

import com.win.ble_demo.util.Utils;

/**
 * 作者：Wilson on 2017-01-19 12:45
 * 邮箱：MorrisWare01@gmail.com
 */
public class SpecifiedDeviceDataFrame {

    private String deviceName;
    private String deviceNo;
    private String deviceData;


    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceData() {
        return deviceData;
    }

    public void setDeviceData(String deviceData) {
        this.deviceData = deviceData;
    }

    public String getDeviceNo() {
        return deviceNo;
    }

    public void setDeviceNo(String deviceNo) {
        this.deviceNo = deviceNo;
    }

    public static SpecifiedDeviceDataFrame parse(String data) {
        SpecifiedDeviceDataFrame frame = new SpecifiedDeviceDataFrame();
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
        }
        frame.setDeviceNo(String.valueOf(Integer.parseInt(data.substring(2, 8), 16)));
        String deviceUnit;
        String unit = data.substring(8, 10);
        switch (unit) {
            case "6D":
                deviceUnit = "mm";
                break;
            case "69":
                deviceUnit = "inch";
                break;
            case "6B":
                deviceUnit = "KGF";
                break;
            case "6C":
                deviceUnit = "lbf";
                break;
            case "4E":
                deviceUnit = "N*m";
                break;
            default:
                deviceUnit = "";
                break;
        }
        String deviceData = Utils.hexStr2AsciiString(data.substring(10).replace("FF", ""));
        frame.setDeviceData(deviceData + deviceUnit);
        return frame;
    }
}
