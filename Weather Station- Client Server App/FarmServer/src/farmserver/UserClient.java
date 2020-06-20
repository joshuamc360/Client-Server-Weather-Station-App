package farmserver;

/**
 * Package Imports
 */
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.StringTokenizer;
import java.util.ArrayList;


/**
 * The user client class.
 */
public class UserClient {
    
     /**
     * Variables
     */
    static loginGUI loginForm;
    static userClientGUI client;
    static final int PORT = 9000;
    static Integer ID;
    static DataOutputStream out;
    static DataInputStream in;
    public UserClient() {
    }
   
    public static void main(String[] args) {
        client = new userClientGUI();
        loginForm = new loginGUI();
        loginForm.setVisible(true);
        Boolean run = false;
        Socket socket = null;
        Boolean loggedIn = false;
        
        //Connect to the server
        try {
            socket = new Socket("localhost" , PORT);
            // Makes a connection with the central server.\
            System.out.println(socket.getLocalSocketAddress().toString());
            int index = socket.getLocalSocketAddress().toString().indexOf(":");
            ID = Integer.parseInt(socket.getLocalSocketAddress().toString().substring(index+1));
        } catch(IOException e) {
            System.out.println(e.getMessage());
        }
        
        //link data streams        
        try {
            out = new DataOutputStream(socket.getOutputStream());
            in = new DataInputStream(socket.getInputStream());  
        } catch(IOException | NullPointerException e) {
            System.out.println(e.getMessage());
            return; // If an output stream cannot be created, terminate the station.
        }
        //Perfrom handshake protocol 
        if (handshake()){
            //If the handshake protocols returns true, wait for the login credentials to be ready
            System.out.println("Attempting to login...");
            run = true;
            while (run){
               System.out.println(loginForm.readyToSend); //Has to be here, breaks if not ????        
               if (loginForm.readyToSend){ //When the user has hit submit
                   try {
                       Thread.sleep(500);
                       //Validate login
                       System.out.println("Client - " + loginForm.loginString);
                       if (loginForm.loginString != null) {
                           String loginSting = loginForm.loginString;
                           out.writeUTF(loginSting);
                           System.out.println("Login credentials sent - " + loginSting);
                           Thread.sleep(100);
                           String loginAcceptedString = in.readUTF();
                           System.out.println(loginAcceptedString);
                           if (loginAcceptedString.contains("Accepted")){ //If login credentials return true
                               run = false;
                               loggedIn = true;
                               client.setVisible(true); //Load client GUI
                               loginForm.setVisible(false);
                               LoadWeatherStations(); //Load all weather stations connected
                               Thread.sleep(1000);
                               while (loggedIn) {                                   
                                   LoadData();
                                   //LoadWeatherStations();
                               } 
                               
                           } else {
                               loginForm.readyToSend = false;
                               
                           }
                           
                       } else {
                           return;
                       }
                   } catch (Exception e) {
                   }
               }
            }
        }                                
    }
    
    public static void LoadData(){
        //Send a RWS for the client's selected station
         try {
             System.out.println(client.selectedStation);
                if (client.selectedStation != null){
                    Integer selectedStation = client.selectedStation;
                    //System.out.println(selectedStation);
                    out.writeUTF("RWS:" + selectedStation.toString().trim());
                    String data = in.readUTF();
                    StringTokenizer st = new StringTokenizer(data,"-");
                    String temp = st.nextToken();//Temp         Humidity         BP            WF               Time
                    client.setData(Integer.parseInt(temp), st.nextToken(), st.nextToken(), st.nextToken(), st.nextToken());  
                }
            } catch (Exception e) {
                System.err.println("Could not send request - " + e.getMessage());
            }
           
        
    }
    
    public static void LoadWeatherStations(){
        //Send a RAWS request to the server
        
        //Take input of weather stations
        ArrayList<Integer> connectedWS = new ArrayList<Integer>();
        
        try {
             out.writeUTF("RAWS");
             String allWSList = in.readUTF();
             System.out.println("All weather stations - " + allWSList);
             StringTokenizer st = new StringTokenizer(allWSList,":");
             while (st.hasMoreTokens()) {               
                connectedWS.add(Integer.parseInt(st.nextToken()));
            }
            
            client.addWeatherStations(connectedWS);
             
             
        } catch (Exception e) {
        }    
    }
    
    public static Boolean handshake(){
        try {
            out.writeUTF("Client"); //Send a message identifying as a client to the server
            System.out.println("Attempting to connect...");
            Thread.sleep(10);
            String validToken = in.readUTF();
            System.out.println(validToken);
           if (validToken.contains("Y")){ //If the server responds with "Y" then the cleint is connected succesfully
               System.out.println("Connected!");
               return true;
           } else {
               return  false;
           }
        } catch (Exception e) {
            System.err.println("Connection token not recieved");
            return false;
        }
        
        
    }
   
}
