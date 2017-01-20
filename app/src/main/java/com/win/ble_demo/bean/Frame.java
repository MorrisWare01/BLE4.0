package com.win.ble_demo.bean;

import android.text.TextUtils;

import com.win.ble_demo.util.Utils;

/**
 * 作者：Wilson on 2017-01-18 13:17
 * 邮箱：MorrisWare01@gmail.com
 */
public class Frame {

    public static String TYPE_SHAKE_HAND = "00";
    public static String TYPE_REQUEST_SPECIFIED_DEVICE_STATUS = "01";
    public static String TYPE_REQUEST_SPECIFIED_DEVICE_DATA = "02";
    public static String TYPE_REQUEST_MULTIPLE_DEVICE_STATUS = "03";
    public static String TYPE_REQUEST_MULTIPLE_DEVICE_DATA = "04";
    public static String TYPE_MODIFY_BY_NAME = "05";

    public static String OPTION_FAILURE = "00";
    public static String OPTION_SUCCESS = "01";

    private String guideHead;
    private String size;
    private String channel;
    private String direction;
    private String source;
    private String dest;
    private String cmdType;
    private String opStatus;
    private String optionData;
    private String checkSum;

    public Frame() {

    }

    public Frame(String direction, String cmdType, String opStatus, String optionData) {
        guideHead = "7E";
        if (!TextUtils.isEmpty(optionData)) {
            byte[] len = {(byte) ((17 + optionData.length() / 2) >> 8),
                    (byte) (17 + optionData.length() / 2)};
            size = Utils.bytes2HexString(len);
        } else {
            size = "0011";
        }
        channel = "04";
        this.direction = direction;
        source = "FFFFFFFF";
        dest = "FFFFFFFF";
        this.cmdType = cmdType;
        this.opStatus = opStatus;
        this.optionData = optionData;

        int sum = Utils.sum(toString().substring(0, toString().length() - 4));
        checkSum = Integer.toHexString(sum);
        for (int i = 0; i < 4 - checkSum.length(); i++) {
            checkSum = "0" + checkSum;
        }
    }

    public String getGuideHead() {
        return guideHead;
    }

    public void setGuideHead(String guideHead) {
        this.guideHead = guideHead;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDest() {
        return dest;
    }

    public void setDest(String dest) {
        this.dest = dest;
    }

    public String getCmdType() {
        return cmdType;
    }

    public void setCmdType(String cmdType) {
        this.cmdType = cmdType;
    }

    public String getOpStatus() {
        return opStatus;
    }

    public void setOpStatus(String opStatus) {
        this.opStatus = opStatus;
    }

    public String getOptionData() {
        return optionData;
    }

    public void setOptionData(String optionData) {
        this.optionData = optionData;
    }

    public String getCheckSum() {
        return checkSum;
    }

    public void setCheckSum(String checkSum) {
        this.checkSum = checkSum;
    }

    public static Frame parseFrame(byte[] bytes) {
        Frame frame = new Frame();
        String data = Utils.bytes2HexString(bytes);
        data = data.replace(" ", "");
        frame.setGuideHead(data.substring(0, 2));
        frame.setSize(data.substring(2, 6));
        frame.setChannel(data.substring(6, 8));
        frame.setDirection(data.substring(8, 10));
        frame.setSource(data.substring(10, 18));
        frame.setDest(data.substring(18, 26));
        frame.setCmdType(data.substring(26, 28));
        frame.setOpStatus(data.substring(28, 30));
        frame.setOptionData(data.substring(30, data.length() - 4));
        frame.setCheckSum(data.substring(data.length() - 4, data.length()));
        return frame;
    }

    @Override
    public String toString() {
        return guideHead + size + channel
                + direction + source + dest +
                cmdType + opStatus + optionData + checkSum;
    }

    public String toDebug() {
        StringBuilder sb = new StringBuilder();
        String string = toString();
        for (int i = 0; i < string.length(); i += 2) {
            sb.append(string.substring(i, i + 2));
            sb.append(" ");
        }
        return sb.toString();
    }
}
