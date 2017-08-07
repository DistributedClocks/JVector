JVector
========

This library can be added to a Java project to generate a [ShiViz](http://bestchai.bitbucket.org/shiviz/)-compatible vector-clock timestamped log of events in a concurrent or distributed system.
JVector is compatible with Java 1.7+.

* org.github.com.jvec    : Contains the Library and all its dependencies
* example/  : Contains examples which can be instrumented with JVector


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

To use JVector simply include the jar file contained in lib or the raw source files.

You can compile your project by importing the following package:
```
src/vclock/jvec.h src/vclock/vclock.h src/mpack/mpack.h
```

Or by generating the libjvec library via this command:

```make
make lib
```
and then compiling your program by including src/libjvec.a

```make
gcc -o myApp src/libjvec.a
```

### Index
```
struct vcLog *initJVector(char *pid, char *logName);
```
```
int writeLogMsg(struct vcLog *vcInfo, char *logMsg);
```
```
int loglocalEvent(struct vcLog *vcInfo, char *logMsg);
```
```
char *prepareSend(struct vcLog *vcInfo, char *logMsg, char *packetContent, int *encodeLen);
```
```
char *unpackReceive(struct vcLog *vcInfo,  char *logMsg, char *encodedBuffer, int bufLen);
```
```
void DisableLogging (struct vcLog *vcInfo)
```
#####   struct vcLog

```c
struct vcLog {
    char pid[VC_ID_LENGTH]; // Fixed to VC_ID_LENGTH defined in vclock.h.
    struct vectorClock *vc; // The local vector clock map.
    char logName[FILE_MAX]; // Name of the JVector log file to write to.
    int printonscreen;      // Print all logging operations to screen.
    int logging;            // Enable logging.
}
```
This is the basic vcLog structure used in any JVector application.
It contains the thread-local vector clock and process as well as 
information about the logging procedure and file name.

 
#####   initJVector
```c
struct vcLog *initJVector(char *pid, char *logName)
```
Initialises and returns a new vcLog structure. This vcLog structure contains the configuration of the current vector thread as well as the vector clock map and process id.
This structure is the basis of any further operation in JVector.
Any log files with the same name as "logName" will be overwritten. "pid" should be unique in the current distributed system.

#####   prepareSend
```c
char *prepareSend(struct vcLog *vcInfo, char *logMsg, char *packetContent, int *encodeLen)
```
Decodes a GoVector buffer, updates the local vector clock, and returns the decoded data.
This function takes a MessagePack buffer and extracts the vector clock as well as data.
It increments the local vector clock, merges the unpacked clock with its own and returns a character representation of the data. 
In addition, prepareSend writes a custom defined message to the main JVector log.

#####   unpackReceive
```c
char *unpackReceive(struct vcLog *vcInfo,  char *logMsg, char *encodedBuffer, int bufLen)
```
Decodes a GoVector buffer, updates the local vector clock, and returns the 
decoded data.
This function takes a MessagePack buffer and extracts the vector clock as well as data. It increments the local vector clock, merges the unpacked clock with its own and returns a character representation of the data.
In addition, prepareSend writes a custom defined message to the main JVector log.

#####   logLocalEvent
```c
int logLocalEvent(struct vcLog *vcInfo, char *logMsg)
```
Records a local event and increments the vector clock contained in "vcInfo".
Also appends a message in the log file defined in the vcInfo structure.

#####   writeLogMsg

```c
int writeLogMsg(struct vcLog *vcInfo, char *logMsg)
```
Appends a message in the log file defined in the vcLog vcInfo structure.

#####   enableLogging
```c
void enableLogging(struct vcLog *vcInfo);
```
Enables the logging mechanism of JVector. Logging is turned on by default.
This is a cosmetic function. Setting `vcInfo->logging` to 1 fulfils the same purpose.

#####   disableLogging
```c
void disableLogging(struct vcLog *vcInfo);
```
Disables the logging mechanism of JVector. Logging is turned on by default.
This is a cosmetic function. Setting `vcInfo->logging` to 0 fulfils the same purpose.

###   Examples

The following is a basic example of how this library can be used:

```c
#include "../src/jvec.h"

int main (){

    struct vcLog *vcInfo = initJVector("MyProcess","basiclog");
    //Prepare  and send a Message
    int size;
    char sendingMessage[50];
    strcpy(sendingMessage, "ExampleMessage");
    printf("We are packing this message: %s\n", sendingMessage);
    char *resultBuffer = prepareSend(vcInfo, "Sending Message", sendingMessage, &size);
    //Unpack the message again
    char *receivedMessage = unpackReceive(vcInfo, "Receiving Message", resultBuffer, size);
    printf("We received this message: %s\n", receivedMessage);

    //Can be called at any point 
    logLocalEvent(vcInfo, "Example Complete");
    //No further events will be written to log file
    disableLogging(vcInfo);
    logLocalEvent(vcInfo, "This will not be logged.");
}
```

This produces the log "basiclog.shiviz" :

    MyProcess {"MyProcess":1}
    Initialization Complete
    MyProcess {"MyProcess":2}
    Sending Message
    MyProcess {"MyProcess":3}
    Receiving Message
    MyProcess {"MyProcess":4}
    Example Complete


An executable example of a similar program can be found in
[example/client_server.c](https://github.com/DistributedClocks/JVector/blob/master/example/client_server.c)
