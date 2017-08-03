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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;

/**
 * Wraps the difference of access methods to DirectBuffers between Android and others.
 */
class DirectBufferAccess
{
    private DirectBufferAccess()
    {}

    enum DirectBufferConstructorType
    {
        ARGS_LONG_INT_REF,
        ARGS_LONG_INT,
        ARGS_INT_INT,
        ARGS_MB_INT_INT
    }

    static Method mGetAddress;
    static Method mCleaner;
    static Method mClean;

    // TODO We should use MethodHandle for efficiency, but it is not available in JDK6
    static Constructor byteBufferConstructor;
    static Class<?> directByteBufferClass;
    static DirectBufferConstructorType directBufferConstructorType;
    static Method memoryBlockWrapFromJni;

    static {
        try {
            // Find the hidden constructor for DirectByteBuffer
            directByteBufferClass = ClassLoader.getSystemClassLoader().loadClass("java.nio.DirectByteBuffer");
            Constructor directByteBufferConstructor = null;
            DirectBufferConstructorType constructorType = null;
            Method mbWrap = null;
            try {
                // TODO We should use MethodHandle for Java7, which can avoid the cost of boxing with JIT optimization
                directByteBufferConstructor = directByteBufferClass.getDeclaredConstructor(long.class, int.class, Object.class);
                constructorType = DirectBufferConstructorType.ARGS_LONG_INT_REF;
            }
            catch (NoSuchMethodException e0) {
                try {
                    // https://android.googlesource.com/platform/libcore/+/master/luni/src/main/java/java/nio/DirectByteBuffer.java
                    // DirectByteBuffer(long address, int capacity)
                    directByteBufferConstructor = directByteBufferClass.getDeclaredConstructor(long.class, int.class);
                    constructorType = DirectBufferConstructorType.ARGS_LONG_INT;
                }
                catch (NoSuchMethodException e1) {
                    try {
                        directByteBufferConstructor = directByteBufferClass.getDeclaredConstructor(int.class, int.class);
                        constructorType = DirectBufferConstructorType.ARGS_INT_INT;
                    }
                    catch (NoSuchMethodException e2) {
                        Class<?> aClass = Class.forName("java.nio.MemoryBlock");
                        mbWrap = aClass.getDeclaredMethod("wrapFromJni", int.class, long.class);
                        mbWrap.setAccessible(true);
                        directByteBufferConstructor = directByteBufferClass.getDeclaredConstructor(aClass, int.class, int.class);
                        constructorType = DirectBufferConstructorType.ARGS_MB_INT_INT;
                    }
                }
            }

            byteBufferConstructor = directByteBufferConstructor;
            directBufferConstructorType = constructorType;
            memoryBlockWrapFromJni = mbWrap;

            if (byteBufferConstructor == null) {
                throw new RuntimeException("Constructor of DirectByteBuffer is not found");
            }
            byteBufferConstructor.setAccessible(true);

            mGetAddress = directByteBufferClass.getDeclaredMethod("address");
            mGetAddress.setAccessible(true);

            mCleaner = directByteBufferClass.getDeclaredMethod("cleaner");
            mCleaner.setAccessible(true);

            mClean = mCleaner.getReturnType().getDeclaredMethod("clean");
            mClean.setAccessible(true);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static long getAddress(Object base)
    {
        try {
            return (Long) mGetAddress.invoke(base);
        }
        catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    static void clean(Object base)
    {
        try {
            Object cleaner = mCleaner.invoke(base);
            mClean.invoke(cleaner);
        }
        catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    static boolean isDirectByteBufferInstance(Object s)
    {
        return directByteBufferClass.isInstance(s);
    }

    static ByteBuffer newByteBuffer(long address, int index, int length, ByteBuffer reference)
    {
        try {
            switch (directBufferConstructorType) {
                case ARGS_LONG_INT_REF:
                    return (ByteBuffer) byteBufferConstructor.newInstance(address + index, length, reference);
                case ARGS_LONG_INT:
                    return (ByteBuffer) byteBufferConstructor.newInstance(address + index, length);
                case ARGS_INT_INT:
                    return (ByteBuffer) byteBufferConstructor.newInstance((int) address + index, length);
                case ARGS_MB_INT_INT:
                    return (ByteBuffer) byteBufferConstructor.newInstance(
                            memoryBlockWrapFromJni.invoke(null, address + index, length),
                            length, 0);
                default:
                    throw new IllegalStateException("Unexpected value");
            }
        }
        catch (Throwable e) {
            // Convert checked exception to unchecked exception
            throw new RuntimeException(e);
        }
    }
}
