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

import com.sun.org.apache.xpath.internal.SourceTree;
import org.github.com.jvec.msgpack.core.MessageBufferPacker;
import org.github.com.jvec.msgpack.core.MessagePack;
import org.github.com.jvec.msgpack.core.MessageUnpacker;
import org.github.com.jvec.vclock.VClock;

import javax.sound.midi.Soundbank;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;


public class Jvec {

    public String pid;
    public VClock vc;
    private BufferedWriter vectorLog;
    private boolean logging;

    public Jvec(String pid, String logName) {
        this.pid = pid;
        this.logging = true;
        initJVector(logName);
    }

    private void initJVector(String logName) {

        this.vc = new VClock();
        this.vc.tick(this.pid);

        FileWriter fw = null;
        try {

            fw = new FileWriter(logName + "-shiviz.txt");
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

    void enableLogging() {
        this.logging = true;
    }

    void disableLogging() {
        this.logging = false;
    }

    public void writeLogMsg(String logMsg) throws IOException {
        StringBuilder vcString = new StringBuilder();
        vcString.append(this.pid + " " + this.vc.returnVCString() + "\n" + logMsg + "\n");
        this.vectorLog.write(vcString.toString());
        this.vectorLog.flush();
    }

    public synchronized void logLocalEvent(String logMsg) {
        updateClock(logMsg);
    }

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

    public synchronized byte[] prepare_str(String logMsg, String packetContent) throws IOException {
        if (!updateClock(logMsg)) return null;
        MessageBufferPacker packer = MessagePack.newDefaultBufferPacker();
        packer.packString(this.pid);
        packer.packString(packetContent);
        packer.packMapHeader(this.vc.getClockSize()); // the number of (key, value) pairs
        for (Map.Entry<String, Long> clock : this.vc.getClockMap().entrySet()) {
            packer.packString(clock.getKey());
            packer.packLong(clock.getValue());
        }
        return packer.toByteArray();
    }

    public synchronized byte[] prepare_i64(String logMsg, Long packetContent) throws IOException {
        if (!updateClock(logMsg)) return null;
        MessageBufferPacker packer = MessagePack.newDefaultBufferPacker();
        packer.packString(this.pid);
        packer.packLong(packetContent);
        packer.packMapHeader(this.vc.getClockSize()); // the number of (key, value) pairs
        for (Map.Entry<String, Long> clock : this.vc.getClockMap().entrySet()) {
            packer.packString(clock.getKey());
            packer.packLong(clock.getValue());
        }
        return packer.toByteArray();
    }

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

    public byte[] unpackReceive(String logMsg, byte[] encodedMsg) throws IOException {
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

    public String unpack_str(String logMsg, byte[] encodedMsg) throws IOException {
        long time = this.vc.findTicks(this.pid);
        if (time == -1) {
            System.err.println("Could not find process id in its vector clock.");
            return null;
        }

        // Deserialize with MessageUnpacker
        MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(encodedMsg);
        String src_pid = unpacker.unpackString();
        String decodedMsg = unpacker.unpackString();
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

    public Long unpack_i64(String logMsg, byte[] encodedMsg) throws IOException {
        long time = this.vc.findTicks(this.pid);
        if (time == -1) {
            System.err.println("Could not find process id in its vector clock.");
            return null;
        }

        // Deserialize with MessageUnpacker
        MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(encodedMsg);
        String src_pid = unpacker.unpackString();
        Long decodedMsg = unpacker.unpackLong();
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
}
