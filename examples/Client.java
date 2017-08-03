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

import org.github.com.jvec.Jvec;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class Client {
    private static final int SERVERPORT = 8080;
    private static final int CLIENTPORT = 8081;
    private static final String SERVERIP = "localhost";
    private static final int MESSAGES = 10;
    private static final int MAXBUFLEN = 100;

    public static void main(String[] args) throws Exception {
        Jvec vcInfo = new Jvec("client", "clientlogfile");
        DatagramSocket clientSocket = new DatagramSocket(CLIENTPORT);
        byte[] sendData = new byte[MAXBUFLEN];
        byte[] receiveData = new byte[MAXBUFLEN];
        InetAddress IPAddress = InetAddress.getByName("localhost");
        int n = 0, nMinOne = 0, nMinTwo = 0;
        for (int i = 0; i < MESSAGES; i++) {
            byte[] inBuf = vcInfo.prepareSend("Sending message to server.", (byte) i);
            System.out.println("Sending message to server.");
            DatagramPacket sendPacket = new DatagramPacket(inBuf, inBuf.length, IPAddress, SERVERPORT);
            clientSocket.send(sendPacket);
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            clientSocket.receive(receivePacket);
            byte[] decodedMsg = vcInfo.unpackReceive("Received message from server.", receivePacket.getData());
            int msg = decodedMsg[0];
            System.out.println("Received value " + msg + " from server.");
        }
        clientSocket.close();
    }
}
