package com.win.ble_demo;

import org.junit.Test;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {

        String data = "12345678";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < data.length(); i += 2) {
            sb.append(data.substring(i, i + 2));
            sb.append(" ");
        }
        System.out.println(sb.toString());
    }


    /**
     * 从字符串到16进制byte数组转换
     * String to HEX
     */
    public static byte[] getHexBytes(String message) {
        message = message.trim();
        int len = message.length() / 2;
        char[] chars = message.toCharArray();
        String[] hexStr = new String[len];
        byte[] bytes = new byte[len];
        for (int i = 0, j = 0; j < len; i += 2, j++) {
            hexStr[j] = "" + chars[i] + chars[i + 1];
            bytes[j] = (byte) Integer.parseInt(hexStr[j], 16);
        }
        return bytes;
    }

}