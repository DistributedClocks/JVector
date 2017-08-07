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

import java.io.IOException;

/**
 * This is the basic JVec class used in any CVector application.
 * It contains the thread-local vector clock and process as well as
 * information about the logging procedure and file name.
 * Creating a JVec object initialises and returns a new vcLog structure. This vcLog structure
 * contains the configuration of the current vector thread as well as the
 * vector clock map and process id.
 * This structure is the basis of any further operation in CVector.
 * Any log files with the same name as "logName" will be overwritten. "pid"
 * should be unique in the current distributed system.
 */
public interface Jvec {
    /**
     * Appends a message in the log file defined in the vcLog vcInfo structure.
     *
     * @param logMsg Custom message will be written to the "vcInfo" log.
     */
    void writeLogMsg(String logMsg) throws IOException;

    /**
     * Records a local event and increments the vector clock contained in "vcInfo".
     * Also appends a message in the log file defined in the vcInfo structure.
     *
     * @param logMsg Custom message will be written to the "vcInfo" log.
     */
    void logLocalEvent(String logMsg);

    /**
     * Encodes a buffer into a custom MessagePack byte array.
     * This is the default JVector method.
     * The function increments the vector clock contained of the JVec class, appends it to
     * the binary "packetContent" and converts the full message into MessagePack format.
     * This method is as generic as possible, any format passed to prepareSend will have to be
     * decoded by unpackReceive. The decoded content will have to be cast back to the original format.
     * In addition, prepareSend writes a custom defined message "logMsg" to the
     * main CVector log.
     *
     * @param logMsg        Custom message will be written to the "vcInfo" log.
     * @param packetContent The actual content of the packet we want to send out.
     */
    byte[] prepareSend(String logMsg, byte[] packetContent) throws IOException;

    /**
     * Encodes a buffer into a custom MessagePack byte array.
     * The function increments the vector clock contained of the JVec class, appends it to
     * the String "packetContent" and converts the full message into MessagePack format.
     * This method is overloaded to accept single byte inputs as format.
     * In addition, prepareSend writes a custom defined message "logMsg" to the
     * main CVector log.
     *
     * @param logMsg        Custom message will be written to the "vcInfo" log.
     * @param packetContent The actual content of the packet we want to send out.
     */
    byte[] prepareSend(String logMsg, byte packetContent) throws IOException;

    /**
     * Decodes a GoVector buffer, updates the local vector clock, and returns the
     * decoded data.
     * This function takes a MessagePack buffer and extracts the vector clock as
     * well as data. It increments the local vector clock, merges the unpacked
     * clock with its own and returns a character representation of the data.
     * This is the default method, which accepts any binary encoded data.
     * If the data has been encoded as long or String, unpack_str or unpack_i64
     * have to be used.
     * In addition, prepareSend writes a custom defined message to the main
     * CVector log.
     *
     * @param logMsg     Custom message will be written to the "vcInfo" log.
     * @param encodedMsg The buffer to be decoded.
     */
    byte[] unpackReceive(String logMsg, byte[] encodedMsg) throws IOException;

    /**
     * Enables the logging mechanism of CVector. Logging is turned on by default.
     * This is a cosmetic function. Setting vc.logging to true fulfils the same purpose.
     */
    void enableLogging();

    /**
     * Disables the logging mechanism of CVector. Logging is turned on by default.
     * This is a cosmetic function. Setting vc.logging to false fulfils the same purpose.
     */
    void disableLogging();
}
