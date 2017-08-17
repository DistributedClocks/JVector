import org.github.com.jvec.JVec;
import org.github.com.jvec.vclock.VClock;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;


import static org.junit.Assert.*;

public class JVecTest {
    JVec testVector;
    TestObject testO;
    static String logName = "testLog-shiviz.txt";

    @Before
    public void setUp() throws Exception {
        testVector = new JVec("proc1", "testLog");
        File file = new File(logName);
        assertTrue("Creating the file failed!", file.exists());
        testO = new TestObject("Information", 1234);
    }

    @Test
    public void flushJVectorLog() throws Exception {
        testVector.flushJVectorLog();
        BufferedReader reader = new BufferedReader(new FileReader(logName));
        String line = reader.readLine();
        assertNotNull("Buffer was not flushed!", line);
        reader.close();
    }

    @Test
    public void closeJVectorLog() throws Exception {
        testVector.closeJVectorLog();
        BufferedReader reader = new BufferedReader(new FileReader(logName));
        String line = reader.readLine();
        assertNotNull("Buffer was not properly flushed!", line);
        reader.close();
    }

    @Test
    public void testInitialWrite() throws Exception {
        testVector.flushJVectorLog();
        BufferedReader reader = new BufferedReader(new FileReader(logName));
        String line = reader.readLine();
        assertEquals("Writing the vector clock failed!", "proc1 {\"proc1\":1}", line);
        line = reader.readLine();
        assertEquals("Writing to the log failed!", "Initialization Complete", line);
        reader.close();
    }

    public void testWrite(String expectedMessage) throws Exception {
        testVector.flushJVectorLog();
        BufferedReader reader = new BufferedReader(new FileReader(logName));
        String line = "";
        for (int i = 0; i < 3; i++) {
            line = reader.readLine();
        }
        assertEquals("Writing the vector clock failed!", "proc1 {\"proc1\":2}", line);
        line = reader.readLine();
        assertEquals("Writing to the log failed!", expectedMessage, line);
        reader.close();
    }

    @Test
    public void getPid() throws Exception {
        assertEquals("getPid failed! Process id is not correct", "proc1", testVector.getPid());
    }

    @Test
    public void getVc() throws Exception {
        VClock vc = testVector.getVc();
        assertEquals("The initial vector clock is incorrect!", "{\"proc1\":1}", vc.returnVCString());
    }

    @Test
    public void writeLogMsg() throws Exception {
        testVector.writeLogMsg("This is a simple log message.");
        testVector.flushJVectorLog();
        BufferedReader reader = new BufferedReader(new FileReader(logName));
        String line = "";
        for (int i = 0; i < 3; i++) {
            line = reader.readLine();
        }
        assertEquals("Writing the vector clock failed!", "proc1 {\"proc1\":1}", line);
        line = reader.readLine();
        assertEquals("Writing to the log failed!", "This is a simple log message.", line);
        reader.close();
    }

    @Test
    public void logLocalEvent() throws Exception {
        testVector.logLocalEvent("This is a local event!");
        testWrite("This is a local event!");
    }

    @Test
    public void prepareSend_Byte() throws Exception {
        byte test = 'a';
        byte[] result = testVector.prepareSend("Packing Byte.", test);
        byte[] expected = {-91, 112, 114, 111, 99, 49, -60, 1, 97, -127, -91, 112, 114, 111, 99, 49, 2};
        ByteBuffer decodedMsg = ByteBuffer.wrap(expected);
        int decodedInt = decodedMsg.getInt();
        assertArrayEquals("PrepareSend Output does not match!", expected, result);
        testWrite("Packing Byte.");
    }

    @Test
    public void prepareSend_Int() throws Exception {
        int test = 1;
        ByteBuffer b = ByteBuffer.allocate(Integer.SIZE / Byte.SIZE);
        byte[] result = testVector.prepareSend("Packing Integer.", b.putInt(test).array());
        byte[] expected = {-91, 112, 114, 111, 99, 49, -60, 4, 0, 0, 0, 1, -127, -91, 112, 114, 111, 99, 49, 2};
        assertArrayEquals("PrepareSend Output does not match!", expected, result);
        testWrite("Packing Integer.");
    }

    @Test
    public void prepareSend_IntArray() throws Exception {
        int[] test = {0, 1, 128, 32768, 2147483647};
        ByteBuffer b = ByteBuffer.allocate(test.length * Integer.SIZE / Byte.SIZE);
        for (int i = 0; i < test.length; i++) {
            b.putInt(test[i]);
        }
        byte[] result = testVector.prepareSend("Packing Integer Array.", b.array());
        byte[] expected = {-91, 112, 114, 111, 99, 49, -60, 20, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0,
                -128, 0, 0, -128, 0, 127, -1, -1, -1, -127, -91, 112, 114, 111, 99, 49, 2};
        assertArrayEquals("PrepareSend Output does not match!", expected, result);
        testWrite("Packing Integer Array.");
    }

    @Test
    public void prepareSend_String() throws Exception {
        String test = "Test";
        byte[] result = testVector.prepareSend("Packing String.", test.getBytes());
        byte[] expected = {-91, 112, 114, 111, 99, 49, -60, 4, 84, 101, 115, 116, -127, -91,
                112, 114, 111, 99, 49, 2};
        assertArrayEquals("PrepareSend Output does not match!", expected, result);
        testWrite("Packing String.");
    }

    @Test
    public void prepareSend_Object() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = new ObjectOutputStream(bos);
        out.writeObject(testO);
        out.flush();
        byte[] result = testVector.prepareSend("Packing Object.", bos.toByteArray());
        byte[] expected = {-91, 112, 114, 111, 99, 49, -60, 82, -84, -19, 0, 5, 115, 114,
                0, 10, 84, 101, 115, 116, 79, 98, 106, 101, 99, 116, 9, -44, 62, -81, 94,
                -55, -60, -90, 2, 0, 2, 73, 0, 2, 105, 100, 76, 0, 4, 110, 97, 109, 101,
                116, 0, 18, 76, 106, 97, 118, 97, 47, 108, 97, 110, 103, 47, 83, 116, 114,
                105, 110, 103, 59, 120, 112, 0, 0, 4, -46, 116, 0, 11, 73, 110, 102, 111,
                114, 109, 97, 116, 105, 111, 110, -127, -91, 112, 114, 111, 99, 49, 2};
        assertArrayEquals("PrepareSend Output does not match!", expected, result);
        testWrite("Packing Object.");
        bos.close();
    }

    @Test
    public void unpackReceive_Byte() throws Exception {
        byte expected = 'a';
        byte[] input = {-91, 112, 114, 111, 99, 49, -60, 1, 97, -127, -91, 112, 114, 111, 99, 49, 2};
        ByteBuffer result = ByteBuffer.wrap(testVector.unpackReceive("Unpacking Byte.", input));
        assertEquals("UnpackReceive Output does not match!", expected, result.get());
        testWrite("Unpacking Byte.");
    }

    @Test
    public void unpackReceive_Int() throws Exception {
        int expected = 1;
        byte[] input = {-91, 112, 114, 111, 99, 49, -60, 4, 0, 0, 0, 1, -127, -91, 112, 114, 111, 99, 49, 2};
        ByteBuffer result = ByteBuffer.wrap(testVector.unpackReceive("Unpacking Integer.", input));
        assertEquals("UnpackReceive Output does not match!", expected, result.getInt());
        testWrite("Unpacking Integer.");
    }

    @Test
    public void unpackReceive_IntArray() throws Exception {
        int[] expected = {0, 1, 128, 32768, 2147483647};
        byte[] input = {-91, 112, 114, 111, 99, 49, -60, 20, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0,
                -128, 0, 0, -128, 0, 127, -1, -1, -1, -127, -91, 112, 114, 111, 99, 49, 2};
        IntBuffer result = ByteBuffer.wrap(testVector.unpackReceive("Unpacking Integer.", input)).asIntBuffer();
        int[] intResult = new int[result.remaining()];
        result.get(intResult);
        assertArrayEquals("UnpackReceive Output does not match!", expected, intResult);
        testWrite("Unpacking Integer.");
    }

    @Test
    public void unpackReceive_String() throws Exception {
        String expected = "Test";
        byte[] input = {-91, 112, 114, 111, 99, 49, -60, 4, 84, 101, 115, 116, -127, -91,
                112, 114, 111, 99, 49, 2};
        String result  = new String (testVector.unpackReceive("Unpacking String.", input));
        assertEquals("UnpackReceive Output does not match!", expected, result);
        testWrite("Unpacking String.");
    }

    @Test
    public void unpackReceive_Object() throws Exception {
        TestObject expected = testO;
        byte[] input = {-91, 112, 114, 111, 99, 49, -60, 82, -84, -19, 0, 5, 115, 114,
                0, 10, 84, 101, 115, 116, 79, 98, 106, 101, 99, 116, 9, -44, 62, -81, 94,
                -55, -60, -90, 2, 0, 2, 73, 0, 2, 105, 100, 76, 0, 4, 110, 97, 109, 101,
                116, 0, 18, 76, 106, 97, 118, 97, 47, 108, 97, 110, 103, 47, 83, 116, 114,
                105, 110, 103, 59, 120, 112, 0, 0, 4, -46, 116, 0, 11, 73, 110, 102, 111,
                114, 109, 97, 116, 105, 111, 110, -127, -91, 112, 114, 111, 99, 49, 2};
        byte[] result  = testVector.unpackReceive("Unpacking Object.", input);
        ByteArrayInputStream bis = new ByteArrayInputStream(result);
        ObjectInput in = new ObjectInputStream(bis);
        TestObject resultObject = (TestObject) in.readObject();
        assertEquals("UnpackReceive Output does not match!", expected, resultObject);
        testWrite("Unpacking Object.");
        bis.close();
    }

    @Test
    public void disableLogging() throws Exception {
        testVector.disableLogging();
        testVector.logLocalEvent("This should not be logged.");
        testVector.flushJVectorLog();
        BufferedReader reader = new BufferedReader(new FileReader(logName));
        String line = "";
        for (int i = 0; i < 3; i++) {
            line = reader.readLine();
        }
        assertNull("Disable logging failed, we wrote to file!", line);
        VClock vc = testVector.getVc();
        assertEquals("The vector clock did not update!", "{\"proc1\":2}", vc.returnVCString());
        reader.close();
    }

    @Test
    public void enableLogging() throws Exception {
        testVector.disableLogging();
        testVector.logLocalEvent("This should not be logged.");
        testVector.enableLogging();
        testVector.writeLogMsg("This should be logged.");
        testVector.flushJVectorLog();
        BufferedReader reader = new BufferedReader(new FileReader(logName));
        String line = "";
        for (int i = 0; i < 3; i++) {
            line = reader.readLine();
        }
        VClock vc = testVector.getVc();
        assertEquals("The vector clock did not update!", "{\"proc1\":2}", vc.returnVCString());
        line = reader.readLine();
        assertEquals("Enable logging failed!", "This should be logged.", line);
        reader.close();
    }
}