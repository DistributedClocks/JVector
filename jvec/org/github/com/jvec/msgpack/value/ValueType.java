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
package org.github.com.jvec.msgpack.value;

import org.github.com.jvec.msgpack.core.MessageFormat;

/**
 * Representation of MessagePack types.
 * <p>
 * MessagePack uses hierarchical type system. Integer and Float are subypte of Number, Thus {@link #isNumberType()}
 * returns true if type is Integer or Float. String and Binary are subtype of Raw. Thus {@link #isRawType()} returns
 * true if type is String or Binary.
 *
 * @see MessageFormat
 */
public enum ValueType
{
    NIL(false, false),
    BOOLEAN(false, false),
    INTEGER(true, false),
    FLOAT(true, false),
    STRING(false, true),
    BINARY(false, true),
    ARRAY(false, false),
    MAP(false, false),
    EXTENSION(false, false);

    private final boolean numberType;
    private final boolean rawType;

    private ValueType(boolean numberType, boolean rawType)
    {
        this.numberType = numberType;
        this.rawType = rawType;
    }

    public boolean isNilType()
    {
        return this == NIL;
    }

    public boolean isBooleanType()
    {
        return this == BOOLEAN;
    }

    public boolean isNumberType()
    {
        return numberType;
    }

    public boolean isIntegerType()
    {
        return this == INTEGER;
    }

    public boolean isFloatType()
    {
        return this == FLOAT;
    }

    public boolean isRawType()
    {
        return rawType;
    }

    public boolean isStringType()
    {
        return this == STRING;
    }

    public boolean isBinaryType()
    {
        return this == BINARY;
    }

    public boolean isArrayType()
    {
        return this == ARRAY;
    }

    public boolean isMapType()
    {
        return this == MAP;
    }

    public boolean isExtensionType()
    {
        return this == EXTENSION;
    }
}
