package com.win.server;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {

        String SHAKE_HANDE = "7E 00 39 04 01 FF FF FF FF FF FF FF FF 00 01 00 02 01 01 00 00 00 01 53 4D 44 43 5F 30 30 30 32 FF FF FF FF FF FF FF FC C7 7F FC CB 6F FF FF FF FF FF FF FF FF FF FF 20 69 ";
        System.out.println(SHAKE_HANDE.trim());


        assertEquals(4, 2 + 2);
    }
}