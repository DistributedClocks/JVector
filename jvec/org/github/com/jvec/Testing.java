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

import org.github.com.jvec.vclock.VClock;

import java.io.IOException;

public class Testing {
    static void testBasicInit() {
        VClock n = new VClock();
        n.tick("a");
        n.tick("b");
        long result = n.findTicks("a");
        if (result == -1)
            System.out.println("Failed on finding ticks: " + n.returnVCString());
        if (result != 1)
            System.out.println("Tick value did not increment: " + n.returnVCString());

        n.tick("a");

        long result1 = n.findTicks("a");
        long result2 = n.findTicks("b");


        if (result1 == -1 || result2 == -1)
            System.out.println("Failed on finding ticks: " + n.returnVCString());

        if (result1 != 2 || result2 != 1) {
            System.out.println("Tick value did not increment: " + n.returnVCString());
        }
    }

    static void testCopy() {
        VClock n = new VClock();
        n.set("a", 4);
        n.set("b", 1);
        n.set("c", 3);
        n.set("d", 2);
        VClock nc = n.copy();

        long resultan = n.findTicks("a");
        long resultbn = n.findTicks("b");
        long resultcn = n.findTicks("c");
        long resultdn = n.findTicks("d");

        long resultanc = nc.findTicks("a");
        long resultbnc = nc.findTicks("b");
        long resultcnc = nc.findTicks("c");
        long resultdnc = nc.findTicks("d");
        if (resultan != resultanc || resultbn != resultbnc || resultcn != resultcnc || resultdn != resultdnc)
            System.out.println("Copy not the same as the original new = " + n.returnVCString() + " , old = " + nc.returnVCString());
    }

    static void testMerge() {
        VClock n1 = new VClock();
        VClock n2 = new VClock();

        n1.set("b", 1);
        n1.set("a", 2);
        n2.set("b", 3);
        n2.set("c", 1);
        VClock n3 = n1.copy();
        n3.merge(n2);
        long result1n = n3.findTicks("a");
        long result2n = n3.findTicks("b");
        long result3n = n3.findTicks("c");

        if (result1n != 2 || result2n != 3 || result3n != 1)
            System.out.println("Merge not as expected = " + n1.returnVCString() + " , old = " + n2.returnVCString() + ", " + n3.returnVCString());
        else
            System.out.println("new = " + n1.returnVCString() + " , old = " + n2.returnVCString() + ", " + n3.returnVCString());
    }

    static void testJVec() throws IOException {
        Jvec vcInfo1 = new Jvec("client", "mylogfile");
        Jvec vcInfo2 = new Jvec("testingClock", "mylogbile");
        String data = "MYMSG";
        byte[] result = vcInfo2.prepareSend("This is going to be written to file.", data.getBytes());
        vcInfo1.vc.tick(vcInfo1.pid);
        vcInfo1.vc.tick("Testing..");
        System.out.println("Clock 1...");
        vcInfo1.vc.printVC();
        byte[] bmsg = vcInfo1.unpackReceive("This has been unpacked from file.", result);
        System.out.println("After decoding...");
        String msg = new String(bmsg, "UTF-8");
        System.out.println("Message: " + msg);
        vcInfo1.vc.printVC();
    }

    public static void main(String args[]) {
        testBasicInit();
        testCopy();
        testMerge();
        try {
            testJVec();
        } catch (IOException e) {
            e.printStackTrace();
        }
/*        final Logger logger = Logger.getLogger(Testing.class);
        logger.info("Hello World");*/
    }
}
