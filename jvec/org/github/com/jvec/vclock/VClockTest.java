package org.github.com.jvec.vclock;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.TreeMap;

import static org.junit.Assert.*;

public class VClockTest {
    VClock testClock;

    @Before
    public void setUp() throws Exception {
        testClock = new VClock();
    }

    @Test
    public void getClockMap() throws Exception {
        TreeMap clockMap = testClock.getClockMap();
        assertTrue("The clock map is not empty!",clockMap.entrySet().isEmpty());
        clockMap.put("Proc1", 1);
        TreeMap clockMap2 = testClock.getClockMap();
        assertEquals("The clock map does not contain the expected value!", 1, clockMap2.get("Proc1"));
        assertEquals("The clock map does not contain the expected key!", "Proc1", clockMap2.firstKey());
    }

    @Test
    public void tick() throws Exception {
        testClock.tick("Proc1");
        TreeMap clockMap = testClock.getClockMap();
        assertEquals("Key value does not match the expected value!", clockMap.get("Proc1"), 1L);
        assertEquals("Process ID does not conform to the expected key!", clockMap.firstEntry().getKey(), "Proc1");
        testClock.tick("Proc1");
        assertEquals("Key value after second tick does not match the expected value!", clockMap.get("Proc1"), 2L);
        assertEquals("Process ID after second tick does not conform to the expected key!", clockMap.firstEntry().getKey(), "Proc1");
        testClock.tick("Proc2");
        assertEquals("Key value for Proc1 does not match the expected value!", 2L, clockMap.get("Proc1"));
        assertEquals("Key value for Proc2 does not match the expected value!", 1L, clockMap.get("Proc2"));
        assertEquals("Process ID Proc1 does not conform to the expected key!", "Proc1", clockMap.firstKey());
        assertEquals("Process ID Proc2 does not conform to the expected key!", "Proc2", clockMap.lastKey());
        testClock.tick("");
        assertEquals("Key value for Proc1 does not match the expected value!", 1L, clockMap.get(""));
    }

    @Test(expected = NullPointerException.class)
    public void tickNullPointerException() {
        testClock.tick(null);
    }

    @Test
    public void set() throws Exception {
        testClock.set("Proc1", 1);
        TreeMap clockMap = testClock.getClockMap();
        assertEquals("Key value does not match the expected value!", 1L, clockMap.get("Proc1"));
        assertEquals("Process ID does not conform to the expected key!", "Proc1", clockMap.firstEntry().getKey());
        testClock.set("Proc1", 10);
        assertEquals("Key value after second tick does not match the expected value!", 10L, clockMap.get("Proc1"));
        assertEquals("Process ID after second tick does not conform to the expected key!", "Proc1", clockMap.firstEntry().getKey());
        testClock.set("Proc2", 2);
        assertEquals("Key value for Proc1 does not match the expected value!", 10L, clockMap.get("Proc1"));
        assertEquals("Key value for Proc2 does not match the expected value!", 2L, clockMap.get("Proc2"));
        assertEquals("Process ID does not conform to the expected key!", "Proc1", clockMap.firstKey());
        assertEquals("Process ID does not conform to the expected key!", "Proc2", clockMap.lastKey());
        testClock.set("Proc1", Long.MAX_VALUE);
        assertEquals("Key value for Proc1 does not match the expected value!", Long.MAX_VALUE, clockMap.get("Proc1"));
        testClock.set("Proc1", 0);
        assertEquals("Key value for Proc1 does not match the expected value!", 1L, clockMap.get("Proc1"));
        testClock.set("Proc1", Long.MIN_VALUE);
        assertEquals("Key value for Proc1 does not match the expected value!", 1L, clockMap.get("Proc1"));
    }

    @Test(expected = NullPointerException.class)
    public void setNullPointerException() {
        testClock.set(null, 0);
    }

    @Test
    public void findTicks() throws Exception {
        testClock.tick("Proc1");
        testClock.set("Proc2", 50);
        long tickResult = testClock.findTicks("Proc1");
        long setResult = testClock.findTicks("Proc2");
        assertEquals("Key value for tick Proc1 does not match the expected value!", 1L, tickResult);
        assertEquals("Key value for set Proc1 does not match the expected value!", 50L, setResult);
        long emptyResult = testClock.findTicks("Proc3");
        assertEquals("Key value for set Proc3 should not be found!", -1, emptyResult);
    }

    @Test(expected = NullPointerException.class)
    public void findTicksNullPointerException() {
        testClock.findTicks(null);
    }

    @Test
    public void copy() throws Exception {
        testClock.set("Proc1", 50);
        VClock nc = testClock.copy();
        long clockTicks = testClock.findTicks("Proc1");
        long clockTicksCopy = nc.findTicks("Proc1");
        assertEquals("Copy failed! The clock values do not match.", clockTicks, clockTicksCopy);
    }

    @Test
    public void lastUpdate() throws Exception {
        testClock.set("Proc1", 2);
        testClock.set("Proc3", 3);
        testClock.set("Proc2", 7);
        long clockTicks = testClock.lastUpdate();
        assertEquals("Copy failed! The clock values do not match.", 7L, clockTicks);

    }

    @Test
    public void returnVCString() throws Exception {
        testClock.set("Proc1", 1);
        testClock.set("Proc2", 2);
        testClock.set("Proc5", 6);
        testClock.set("", 0);
        assertEquals("The function does not return the correct String!",  "{\"\":1, \"Proc1\":1, \"Proc2\":2," +
                " \"Proc5\":6}", testClock.returnVCString());
    }

    @Test
    public void merge() throws Exception {
        VClock testClock2 = new VClock();
        testClock.set("Proc1", 1);
        testClock.set("Proc2", 2);

        testClock2.set("Proc2", 3);
        testClock2.set("Proc3", 1);
        VClock mergeClock = testClock.copy();
        mergeClock.merge(testClock2);
        assertEquals("The clock did not merge correctly!",  "{\"Proc1\":1, \"Proc2\":3," +
                " \"Proc3\":1}", mergeClock.returnVCString());
    }

    @Test
    public void printVC() throws Exception {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        testClock.set("Proc1", 2);
        testClock.set("Proc3", 3);
        testClock.set("Proc2", 7);
        testClock.printVC();
        assertEquals("{\"Proc1\":2, \"Proc2\":7," +
                " \"Proc3\":3}\n", outContent.toString());
        System.setOut(null);
    }
}