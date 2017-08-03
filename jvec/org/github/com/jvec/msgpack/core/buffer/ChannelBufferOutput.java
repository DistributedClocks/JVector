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

//
// MessagePack for Java
//
//    Licensed under the Apache License, Version 2.0 (the "License");
//    you may not use this file except in compliance with the License.
//    You may obtain a copy of the License at
//
//        http://www.apache.org/licenses/LICENSE-2.0
//
//    Unless required by applicable law or agreed to in writing, software
//    distributed under the License is distributed on an "AS IS" BASIS,
//    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//    See the License for the specific language governing permissions and
//    limitations under the License.
//
package org.github.com.jvec.msgpack.core.buffer;

import org.github.com.jvec.msgpack.core.Preconditions;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;

import static org.github.com.jvec.msgpack.core.Preconditions.checkNotNull;

/**
 * {@link MessageBufferOutput} adapter for {@link java.nio.channels.WritableByteChannel}
 */
public class ChannelBufferOutput
        implements MessageBufferOutput
{
    private WritableByteChannel channel;
    private MessageBuffer buffer;

    public ChannelBufferOutput(WritableByteChannel channel)
    {
        this(channel, 8192);
    }

    public ChannelBufferOutput(WritableByteChannel channel, int bufferSize)
    {
        this.channel = Preconditions.checkNotNull(channel, "output channel is null");
        this.buffer = MessageBuffer.allocate(bufferSize);
    }

    /**
     * Reset channel. This method doesn't close the old channel.
     *
     * @param channel new channel
     * @return the old channel
     */
    public WritableByteChannel reset(WritableByteChannel channel)
            throws IOException
    {
        WritableByteChannel old = this.channel;
        this.channel = channel;
        return old;
    }

    @Override
    public MessageBuffer next(int minimumSize)
            throws IOException
    {
        if (buffer.size() < minimumSize) {
            buffer = MessageBuffer.allocate(minimumSize);
        }
        return buffer;
    }

    @Override
    public void writeBuffer(int length)
            throws IOException
    {
        ByteBuffer bb = buffer.sliceAsByteBuffer(0, length);
        while (bb.hasRemaining()) {
            channel.write(bb);
        }
    }

    @Override
    public void write(byte[] buffer, int offset, int length)
            throws IOException
    {
        ByteBuffer bb = ByteBuffer.wrap(buffer, offset, length);
        while (bb.hasRemaining()) {
            channel.write(bb);
        }
    }

    @Override
    public void add(byte[] buffer, int offset, int length)
            throws IOException
    {
        write(buffer, offset, length);
    }

    @Override
    public void close()
            throws IOException
    {
        channel.close();
    }

    @Override
    public void flush()
            throws IOException
    { }
}
