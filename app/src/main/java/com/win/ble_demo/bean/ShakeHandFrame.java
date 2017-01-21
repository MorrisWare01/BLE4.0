package com.win.ble_demo.bean;

import com.win.ble_demo.util.Utils;

/**
 * 作者：Wilson on 2017-01-18 17:06
 * 邮箱：MorrisWare01@gmail.com
 */
public class ShakeHandFrame {

    private String deviceType;
    private String protocolVersion;
    private String factoryCode;
    private String softVersion;
    private String smdcId;
    private String smdcName;
    private String bleMac;

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getProtocolVersion() {
        return protocolVersion;
    }

    public void setProtocolVersion(String protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    public String getFactoryCode() {
        return factoryCode;
    }

    public void setFactoryCode(String factoryCode) {
        this.factoryCode = factoryCode;
    }

    public String getSoftVersion() {
        return softVersion;
    }

    public void setSoftVersion(String softVersion) {
        this.softVersion = softVersion;
    }

    public String getSmdcId() {
        return smdcId;
    }

    public void setSmdcId(String smdcId) {
        this.smdcId = smdcId;
    }

    public String getSmdcName() {
        return smdcName;
    }

    public void setSmdcName(String smdcName) {
        this.smdcName = smdcName;
    }

    public String getBleMac() {
        return bleMac;
    }

    public void setBleMac(String bleMac) {
        this.bleMac = bleMac;
    }

    public static ShakeHandFrame parse(String data) {
        ShakeHandFrame frame = new ShakeHandFrame();
        frame.setDeviceType(data.substring(0, 2).equals("0x00") ? "SMDC" : "其他");
        frame.setProtocolVersion(data.substring(2, 4));
        frame.setFactoryCode(data.substring(4, 6));
        frame.setSoftVersion(data.substring(6, 8));
        frame.setSmdcId("" + Integer.parseInt(data.substring(8, 16), 16));
        frame.setSmdcName(Utils.hexStr2AsciiString(data.substring(16, 48).replace("FF", "")));
        frame.setBleMac(data.substring(48, 50) + ":" + data.substring(50, 52) + ":" +
                data.substring(52, 54) + ":" + data.substring(54, 56) + ":" +
                data.substring(56, 58) + ":" + data.substring(58, 60));
        return frame;
    }

    @Override
    public String toString() {
        return "ShakeHandFrame{" +
                "deviceType='" + deviceType + '\'' +
                ", protocolVersion='" + protocolVersion + '\'' +
                ", factoryCode='" + factoryCode + '\'' +
                ", softVersion='" + softVersion + '\'' +
                ", smdcId='" + smdcId + '\'' +
                ", smdcName='" + smdcName + '\'' +
                ", bleMac='" + bleMac + '\'' +
                '}';
    }
}
