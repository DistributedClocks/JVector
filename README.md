JVector
========

This library can be added to a Java project to generate a [ShiViz](http://bestchai.bitbucket.org/shiviz/)-compatible vector-clock timestamped log of events in a concurrent or distributed system.
JVector is compatible with Java 1.7+.

* org/github/com/jvec /   : Contains the Library and all its dependencies
* examples/  : Contains examples which can be instrumented with JVector


### Usage

JVector has the following dependencies:

VClock - A vector clock library written in Java.
```
org.github.com.jvec.vclock
```
[MPack](https://github.com/msgpack/msgpack-java) - A [MessagePack](http://msgpack.org/index.html) implementation.
```
org.github.com.jvec.msgpack
```

To use JVector simply include the jar file contained in lib/ or use the raw source files.

You can compile your project by importing the following package:
```
import org.github.com.jvec.JVec;
```


### Index
```
void writeLogMsg(String logMsg);
```
```
synchronized void logLocalEvent(String logMsg);
```
```
synchronized byte[] prepareSend(String logMsg, byte[] packetContent);
```
```
synchronized byte[] prepareSend(String logMsg, byte packetContent);
```
```
synchronized byte[] unpackReceive(String logMsg, byte[] encodedMsg);
```
```
void enableLogging();
void disableLogging();
```
#####   JVec class

This is the basic JVec class used in any JVector application.
It contains the thread-local vector clock and process as well as information about the logging procedure and file name.
This class is the basis of any further operation in JVector.
Any log files with the same name as "logName" will be overwritten. "pid" should be unique in the current distributed system.

#####   prepareSend
```java
synchronized byte[] prepareSend(String logMsg, byte[] packetContent);
```
Encodes a buffer into a custom MessagePack byte array.
This is the default JVector method.
The function increments the vector clock contained of the JVec class, appends it to the binary "packetContent" and converts the full message into MessagePack format.
This method is as generic as possible, any format passed to prepareSend will have to be decoded by unpackReceive. The decoded content will have to be cast back to the original format.
In addition, prepareSend writes a custom defined message "logMsg" to the main JVector log.

#####   unpackReceive
```java
synchronized byte[] unpackReceive(String logMsg, byte[] encodedMsg);
```
Decodes a JVector buffer, updates the local vector clock, and returns the decoded data.
This function takes a MessagePack buffer and extracts the vector clock as well as data. It increments the local vector clock, merges the unpacked clock with its own and returns a character representation of the data.
This is the default method, which accepts any binary encoded data.
In addition, prepareSend writes a custom defined message to the main JVector log.

#####   logLocalEvent
```java
synchronized void logLocalEvent(String logMsg);
```
Records a local event and increments the vector clock of this class.
Also appends a message in the log file defined in the vcInfo structure.

#####   writeLogMsg

```java
void writeLogMsg(String logMsg);
```
Appends a message in the log file defined in this class.

#####   enableLogging
```java
void enableLogging();
```
Enables the logging mechanism of JVector. Logging is turned on by default.
This is a cosmetic function. Setting vc.logging to true fulfils the same purpose.

#####   disableLogging
```java
void disableLogging();
```
Disables the logging mechanism of JVector. Logging is turned on by default.
This is a cosmetic function. Setting vc.logging to false fulfils the same purpose.

###   Examples

The following is a basic example of how this library can be used:

```java
import org.github.com.jvec.JVec;
import java.io.IOException;

public class BasicExample {

    public static void main(String args[]) {

        JVec vcInfo = new JVec("MyProcess", "basiclog");
        String sendingMessage = "ExampleMessage";
        System.out.println("We are packing this message: " + sendingMessage);
        try {
            byte[] resultBuffer = vcInfo.prepareSend("Sending Message", sendingMessage.getBytes());
            //Unpack the message again
            byte[] receivedBuffer = vcInfo.unpackReceive("Receiving Message", resultBuffer);
            String receivedMessage = new String(receivedBuffer);
            System.out.println("We received this message: " + receivedMessage);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Can be called at any point
        vcInfo.logLocalEvent("Example Complete");
        //No further events will be written to log file
        vcInfo.disableLogging();
        vcInfo.logLocalEvent("This will not be logged.");
    }
}
```

This produces the log "basiclog-shiviz.txt" :

    MyProcess {"MyProcess":1}
    Initialization Complete
    MyProcess {"MyProcess":2}
    Sending Message
    MyProcess {"MyProcess":3}
    Receiving Message
    MyProcess {"MyProcess":4}
    Example Complete


An executable example of a similar program can be found in
[examples/ClientServer](https://github.com/DistributedClocks/JVector/blob/master/examples/ClientServer.java)
