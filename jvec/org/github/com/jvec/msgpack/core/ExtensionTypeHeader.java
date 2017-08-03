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

import static org.github.com.jvec.msgpack.core.Preconditions.checkArgument;

/**
 * Header of the Extension types
 */
public class ExtensionTypeHeader
{
    private final byte type;
    private final int length;

    /**
     * Create an extension type header
     * Example:
     * <pre>
     * {@code
     * import ExtensionTypeHeader;
     * import static ExtensionTypeHeader.checkedCastToByte;
     * ...
     * ExtensionTypeHeader header = new ExtensionTypeHeader(checkedCastToByte(0x01), 32);
     * ...
     * }
     * </pre>
     *
     * @param type extension type (byte). You can check the valid byte range with {@link #checkedCastToByte(int)} method.
     * @param length extension type data length
     */
    public ExtensionTypeHeader(byte type, int length)
    {
        Preconditions.checkArgument(length >= 0, "length must be >= 0");
        this.type = type;
        this.length = length;
    }

    public static byte checkedCastToByte(int code)
    {
        Preconditions.checkArgument(Byte.MIN_VALUE <= code && code <= Byte.MAX_VALUE, "Extension type code must be within the range of byte");
        return (byte) code;
    }

    public byte getType()
    {
        return type;
    }

    public int getLength()
    {
        return length;
    }

    @Override
    public int hashCode()
    {
        return (type + 31) * 31 + length;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof ExtensionTypeHeader) {
            ExtensionTypeHeader other = (ExtensionTypeHeader) obj;
            return this.type == other.type && this.length == other.length;
        }
        return false;
    }

    @Override
    public String toString()
    {
        return String.format("ExtensionTypeHeader(type:%d, length:%,d)", type, length);
    }
}
