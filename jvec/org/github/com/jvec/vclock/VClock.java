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


package org.github.com.jvec.vclock;


import java.util.Map;
import java.util.TreeMap;


public class VClock {

    private static final long serialVersionUID = 1;
    private TreeMap<String, Long> vc;

    /**
     * Construct an empty VectorClock
     */
    public VClock() {
        this.vc = clockInit();
    }

    private TreeMap<String, Long> clockInit() {
        return new TreeMap<String, Long>();
    }

    public void tick(String pid) {
        if (this.vc.containsKey(pid)) this.vc.put(pid, this.vc.get(pid) + 1);
        else this.vc.put(pid, (long) 1);

    }

    public void set(String pid, long ticks) {
        if (this.vc.containsKey(pid)) this.vc.put(pid, ticks);
        else this.vc.put(pid, ticks);
    }

    public VClock copy() {
        VClock clock = new VClock();
        clock.vc.putAll(this.vc);
        return clock;
    }

    public long findTicks(String pid) {
        return this.vc.getOrDefault(pid, (long) -1);
    }

    public long lastUpdate(String pid) {
        long last = 0;
        for (Map.Entry<String, Long> clock : this.vc.entrySet()) {
            if (clock.getValue() > last) {
                last = clock.getValue();
            }
        }
        return last;
    }

    public void merge(VClock other) {
        for (Map.Entry<String, Long> clock : other.vc.entrySet()) {
            Long time = this.vc.get(clock.getKey());
            if (time == null) {
                this.vc.put(clock.getKey(), clock.getValue());
            } else {
                if (time < clock.getValue())
                    this.vc.put(clock.getKey(), clock.getValue());
            }
        }
    }

    public String returnVCString() {
        int mapSize = this.vc.size();
        int i = 0;
        StringBuilder vcString = new StringBuilder();
        vcString.append("{");
        for (Map.Entry<String, Long> clock : this.vc.entrySet()) {
            vcString.append("\"");
            vcString.append(clock.getKey());
            vcString.append("\":");
            vcString.append(clock.getValue());
            if (i < mapSize) vcString.append(", ");
        }
        vcString.append("}");
        return vcString.toString();
    }

    public void printVC() {
        System.out.println(returnVCString());
    }

    public int getClockSize() {
        return this.vc.size();
    }

    public TreeMap<String, Long> getClockMap() {
        return this.vc;
    }
}
