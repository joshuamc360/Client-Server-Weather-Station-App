package farmserver;

/**
 * Package imports
 */
import java.io.DataInputStream;
import java.net.Socket;
import java.io.DataOutputStream;
import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * The weather station handler class.
 */
public class WeatherStation {
    protected Socket socket;
    public static int id;
    public Integer temperature;
    
    
    static final int PORT = 9000;
    
    public static void main(String[] args) {
        final int minTemp = -2;
        final int maxTemp = 36;
        final String validTokenResponse = "Y";
        Socket socket = null;
        Boolean transmit = false;
        
        
        try {
            socket = new Socket("localhost" , PORT);
            // Makes a connection with the central server.\
            System.out.println(socket.getLocalSocketAddress().toString());
            int index = socket.getLocalSocketAddress().toString().indexOf(":");
            id = Integer.parseInt(socket.getLocalSocketAddress().toString().substring(index+1));
        } catch(IOException e) {
            System.out.println(e.getMessage());
        }
        
        DataOutputStream out;
        DataInputStream in;
        
        try {
            out = new DataOutputStream(socket.getOutputStream());
            in = new DataInputStream(socket.getInputStream());  
        } catch(IOException | NullPointerException e) {
            System.out.println(e.getMessage());
            return; // If an output stream cannot be created, terminate the station.
        }
         //Handshake
        try {
            out.writeUTF("WeatherStation");
            System.out.println("Attempting to connect...");
            Thread.sleep(10);
            String validToken = in.readUTF();
            System.out.println(validToken);
            if (validToken.contains(validTokenResponse)){
                transmit = true;
                System.out.println("Connected!");
            } else {
                System.err.println("Connection token not recieved");
            }
        } catch (IOException | InterruptedException e){
            System.err.println("Invalid connection handshake");
            System.err.println(e.getMessage());
        }
        
        while(transmit) {
            Integer temperature = generateTemp(minTemp, maxTemp);
            String rHumidity = String.valueOf(generateRelativeHumdity());
            String barometricP = String.valueOf(generateBarometricPressure());
            String windForce = String.valueOf(generateWindForce());
            
            LocalTime localTime = LocalTime.now();
            DateTimeFormatter formattedTime = DateTimeFormatter.ofPattern("HH:mm:ss");
            String time = formattedTime.format(localTime);
            
             String dataSentence = id + "-"+temperature+"-"+rHumidity+ "-"+barometricP+"-"+windForce+"-"+time;
            
            try {
                out.writeUTF(dataSentence);
                System.out.println(dataSentence);
                System.out.println("Sent");
                Thread.sleep(5000);
            } catch(IOException | InterruptedException e) {
                System.out.println(e.getMessage());
                return;
            }
        }
    }
    
    /**
     * generateTemp
     * Generates a new temperature in a range specified by min/maxTemp.
     * @param minTemp - the minimum temperature.
     * @param maxTemp - the maximum temperature.
     * @return a random temperature value.
     */
    public static int generateTemp(int minTemp, int maxTemp) {
        return (int) (minTemp + Math.random() * (maxTemp - minTemp));
    }
    
    public static int generateRelativeHumdity(){
        final int minVal = 0;
        final int maxVal= 100;
        return (int)(minVal + Math.random() * (maxVal - minVal));
    }
    
    public static int generateBarometricPressure(){
        final int minVal = 0;
        final int maxVal= 16000;
        return (int)(minVal + Math.random() * (maxVal - minVal));
    }
    
    public static int generateWindForce(){
        final int minVal = 0;
        final int maxVal= 118;
        return (int)(minVal + Math.random() * (maxVal - minVal));
    }
}
