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
package org.github.com.jvec.msgpack.core;

import org.github.com.jvec.msgpack.core.buffer.ArrayBufferOutput;
import org.github.com.jvec.msgpack.core.buffer.MessageBuffer;
import org.github.com.jvec.msgpack.core.buffer.MessageBufferOutput;

import java.io.IOException;
import java.util.List;

/**
 * MessagePacker that is useful to produce byte array output.
 * <p>
 * This class allocates a new buffer instead of resizing the buffer when data doesn't fit in the initial capacity.
 * This is faster than ByteArrayOutputStream especially when size of written bytes is large because resizing a buffer
 * usually needs to copy contents of the buffer.
 */
public class MessageBufferPacker
        extends MessagePacker
{
    protected MessageBufferPacker(MessagePack.PackerConfig config)
    {
        this(new ArrayBufferOutput(), config);
    }

    protected MessageBufferPacker(ArrayBufferOutput out, MessagePack.PackerConfig config)
    {
        super(out, config);
    }

    public MessageBufferOutput reset(MessageBufferOutput out)
            throws IOException
    {
        if (!(out instanceof ArrayBufferOutput)) {
            throw new IllegalArgumentException("MessageBufferPacker accepts only ArrayBufferOutput");
        }
        return super.reset(out);
    }

    private ArrayBufferOutput getArrayBufferOut()
    {
        return (ArrayBufferOutput) out;
    }

    /**
     * Clears the written data.
     */
    public void clear()
    {
        getArrayBufferOut().clear();
    }

    /**
     * Gets copy of the written data as a byte array.
     * <p>
     * If your application needs better performance and smaller memory consumption, you may prefer
     * {@link #toMessageBuffer()} or {@link #toBufferList()} to avoid copying.
     *
     * @return the byte array
     */
    public byte[] toByteArray()
    {
        try {
            flush();
        }
        catch (IOException ex) {
            // IOException must not happen because underlying ArrayBufferOutput never throws IOException
            throw new RuntimeException(ex);
        }
        return getArrayBufferOut().toByteArray();
    }

    /**
     * Gets the written data as a MessageBuffer.
     * <p>
     * Unlike {@link #toByteArray()}, this method omits copy of the contents if size of the written data is smaller
     * than a single buffer capacity.
     *
     * @return the MessageBuffer instance
     */
    public MessageBuffer toMessageBuffer()
    {
        try {
            flush();
        }
        catch (IOException ex) {
            // IOException must not happen because underlying ArrayBufferOutput never throws IOException
            throw new RuntimeException(ex);
        }
        return getArrayBufferOut().toMessageBuffer();
    }

    /**
     * Returns the written data as a list of MessageBuffer.
     * <p>
     * Unlike {@link #toByteArray()} or {@link #toMessageBuffer()}, this is the fastest method that doesn't
     * copy contents in any cases.
     *
     * @return the list of MessageBuffer instances
     */
    public List<MessageBuffer> toBufferList()
    {
        try {
            flush();
        }
        catch (IOException ex) {
            // IOException must not happen because underlying ArrayBufferOutput never throws IOException
            throw new RuntimeException(ex);
        }
        return getArrayBufferOut().toBufferList();
    }
}
