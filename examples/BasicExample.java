import org.github.com.jvec.JVec;
import java.io.IOException;

public class BasicExample {

    public static void main(String args[]) {

        JVec vcInfo = new JVec("MyProcess", "basiclog");
        String sendingMessage = "ExampleMessage";
        System.out.println("We are packing this message: " + sendingMessage);
        try {
            byte[] resultBuffer = vcInfo.prepareSend("Sending Message", sendingMessage.getBytes());
            // Unpack the message again
            byte[] receivedBuffer = vcInfo.unpackReceive("Receiving Message", resultBuffer);
            String receivedMessage = new String(receivedBuffer);
            System.out.println("We received this message: " + receivedMessage);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Can be called at any point
        vcInfo.logLocalEvent("Example Complete");
        // No further events will be written to log file
        vcInfo.disableLogging();
        vcInfo.logLocalEvent("This will not be logged.");
        // We are done. Flush the buffered results to our logfile and close the reader.
        vcInfo.closeJVectorLog();
    }
}
