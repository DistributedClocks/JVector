/*
 * MIT License
 *
 * Copyright (c) 2017 Distributed clocks
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package org.github.com.jvec;

import org.github.com.jvec.msgpack.core.MessageBufferPacker;
import org.github.com.jvec.msgpack.core.MessagePack;
import org.github.com.jvec.msgpack.core.MessageUnpacker;
import org.github.com.jvec.vclock.VClock;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

/**
 * This is the basic JVec class used in any JVector application.
 * It contains the thread-local vector clock and process as well as
 * information about the logging procedure and file name.
 * This class is the basis of any further operation in JVector.
 * Any log files with the same name as "logName" will be overwritten. "pid"
 * should be unique in the current distributed system.
 */
public class JVec {

    private final String pid;
    private VClock vc;
    private BufferedWriter vectorLog;
    private boolean logging;

    public JVec(String pid, String logName) {
        this.pid = pid;
        this.logging = true;
        initJVector(logName);
    }

    /**
     * Returns the process id of the class.
     */
    public String getPid() {
        return pid;
    }

    /**
     * Returns the vector clock map contained in the class.
     */
    public VClock getVc() {
        return vc;
    }

    /**
     * Initialise the vector clock class and open a log file.
     */
    private void initJVector(String logName) {

        this.vc = new VClock();
        this.vc.tick(this.pid);
        this.logging = true;

        try {
            FileWriter fw = new FileWriter(logName + "-shiviz.txt");
            vectorLog = new BufferedWriter(fw);
        } catch (IOException e) {
            System.err.println("Could not open log file.");
            e.printStackTrace();
        }

        try {
            writeLogMsg("Initialization Complete");
        } catch (IOException e) {
            System.err.println("Could not write to log file.");
            e.printStackTrace();
        }
    }

    public void flushJVectorLog() {
        try {
            vectorLog.flush();
        } catch (IOException e) {
            System.err.println("Flushing failed:");
            e.printStackTrace();
        }
    }
    public void closeJVectorLog() {
        try {
            vectorLog.flush();
            vectorLog.close();
        } catch (IOException e) {
            System.err.println("Deallocation failed:");
            e.printStackTrace();
        }
    }
    private boolean updateClock(String logMsg) {
        long time = this.vc.findTicks(this.pid);
        if (time == -1) {
            System.err.println("Could not find process id in its vector clock.");
            return false;
        }
        this.vc.tick(this.pid);

        try {
            writeLogMsg(logMsg);
        } catch (IOException e) {
            System.err.println("Could not write to log file.");
            e.printStackTrace();
        }
        return true;
    }

    /**
     * Appends a message in the log file defined in this class.
     *
     * @param logMsg Custom message that will be written to the log.
     */
    public void writeLogMsg(String logMsg) throws IOException {
        if (!this.logging) {
            return;
        }
        String vcString = this.pid + " " + this.vc.returnVCString() + "\n" + logMsg + "\n";
        this.vectorLog.write(vcString);
    }

    /**
     * Records a local event and increments the vector clock of this class.
     * Also appends a message in the log file defined in the vcInfo structure.
     *
     * @param logMsg Custom message will be written to the "vectorLog" log.
     */
    public synchronized void logLocalEvent(String logMsg) {
        updateClock(logMsg);
    }

    /**
     * Encodes a buffer into a custom MessagePack byte array.
     * This is the default JVector method.
     * The function increments the vector clock contained of the JVec class, appends it to
     * the binary "packetContent" and converts the full message into MessagePack format.
     * This method is as generic as possible, any format passed to prepareSend will have to be
     * decoded by unpackReceive. The decoded content will have to be cast back to the original format.
     * In addition, prepareSend writes a custom defined message "logMsg" to the
     * main JVector log.
     *
     * @param logMsg        Custom message will be written to the vectorLog log.
     * @param packetContent The actual content of the packet we want to send out.
     */
    public synchronized byte[] prepareSend(String logMsg, byte[] packetContent) throws IOException {
        if (!updateClock(logMsg)) return null;
        MessageBufferPacker packer = MessagePack.newDefaultBufferPacker();
        packer.packString(this.pid);
        packer.packBinaryHeader(packetContent.length);
        packer.writePayload(packetContent);
        packer.packMapHeader(this.vc.getClockSize()); // the number of (key, value) pairs
        for (Map.Entry<String, Long> clock : this.vc.getClockMap().entrySet()) {
            packer.packString(clock.getKey());
            packer.packLong(clock.getValue());
        }

        return packer.toByteArray();
    }

    /**
     * Encodes a buffer into a custom MessagePack byte array.
     * The function increments the vector clock contained of the JVec class, appends it to
     * the String "packetContent" and converts the full message into MessagePack format.
     * This method is overloaded to accept single byte inputs as format.
     * In addition, prepareSend writes a custom defined message "logMsg" to the
     * main JVector log.
     *
     * @param logMsg        Custom message will be written to the "vectorLog" log.
     * @param packetContent The actual content of the packet we want to send out.
     */
    public synchronized byte[] prepareSend(String logMsg, byte packetContent) throws IOException {
        byte[] packetProxy = new byte[1];
        packetProxy[0] = packetContent;
        return prepareSend(logMsg, packetProxy);
    }

    private void mergeRemoteClock(VClock remoteClock) {
        long time = this.vc.findTicks(this.pid);
        if (time == -1) {
            System.err.println("Could not find process id in its vector clock.");
            return;
        }
        this.vc.merge(remoteClock);
    }

    /**
     * Decodes a JVector buffer, updates the local vector clock, and returns the
     * decoded data.
     * This function takes a MessagePack buffer and extracts the vector clock as
     * well as data. It increments the local vector clock, merges the unpacked
     * clock with its own and returns a character representation of the data.
     * This is the default method, which accepts any binary encoded data.
     * In addition, prepareSend writes a custom defined message to the main
     * JVector log.
     *
     * @param logMsg     Custom message will be written to the "vectorLog" log.
     * @param encodedMsg The buffer to be decoded.
     */
    public synchronized byte[] unpackReceive(String logMsg, byte[] encodedMsg) throws IOException {
        long time = this.vc.findTicks(this.pid);
        if (time == -1) {
            System.err.println("Could not find process id in its vector clock.");
            return null;
        }

        // Deserialize with MessageUnpacker
        MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(encodedMsg);
        String src_pid = unpacker.unpackString();
        int msglen = unpacker.unpackBinaryHeader();
        byte[] decodedMsg = unpacker.readPayload(msglen);
        int numClocks = unpacker.unpackMapHeader();
        VClock remoteClock = new VClock();
        for (int i = 0; i < numClocks; ++i) {
            String clock_pid = unpacker.unpackString();
            Long clock_time = unpacker.unpackLong();
            remoteClock.set(clock_pid, clock_time);
        }
        vc.tick(this.pid);
        mergeRemoteClock(remoteClock);
        try {
            writeLogMsg(logMsg);
        } catch (IOException e) {
            System.err.println("Could not write to log file.");
            e.printStackTrace();
        }
        unpacker.close();
        return decodedMsg;
    }

    /**
     * Enables the logging mechanism of JVector. Logging is turned on by default.
     * This is a cosmetic function. Setting vc.logging to true fulfils the same purpose.
     */
    public void enableLogging() {
        this.logging = true;
    }

    /**
     * Disables the logging mechanism of JVector. Logging is turned on by default.
     * This is a cosmetic function. Setting vc.logging to false fulfils the same purpose.
     */
    public void disableLogging() {
        this.logging = false;
    }
}
