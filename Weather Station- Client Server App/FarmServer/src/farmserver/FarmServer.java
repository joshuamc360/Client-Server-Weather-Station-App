package farmserver;

/**
 * Package imports
 */
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Paths;
import java.util.StringTokenizer;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * The central server class.
 */
public class FarmServer {
    /**
     * Constants
     */
    static final int PORT = 9000; 
    /**
     * Variables
     */
    static ServerSocket server = null;
    static serverGUI serverGUI = null;
    static ArrayList<Integer> WSList = new ArrayList<Integer>(); 
    static ArrayList<Integer> clientList = new ArrayList<Integer>(); 
    public static Dictionary<Integer, weatherStationData> weatherStationValues = new Hashtable();
    
    
    /**
     * main
     * The main program method.
     * @param args - arguments.
     */
    
    
    public static void main(String args[]) {
        try {
            server = new ServerSocket(PORT);
            System.out.println("Running...");
            System.out.println(server.getLocalSocketAddress());
            String ip = server.getLocalSocketAddress().toString();
            serverGUI = new serverGUI();
            serverGUI.setVisible(true);
            serverGUI.setAddress(ip);          
        } catch(IOException e) {
            System.out.println(e.getMessage());
        }
        
        while(true) { //Main loop wwaiting for connections. server.accept() blocks. 
            try {
                Socket socket = server.accept();
                String remoteAdd = socket.getRemoteSocketAddress().toString();
                StringTokenizer st1 = new StringTokenizer(remoteAdd,":");
                ///127.0.0.1:54134
                String t = st1.nextToken();
                int ID = Integer.parseInt(st1.nextToken()); //ID is set as the port the TCP connection channels through
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                DataInputStream in = new DataInputStream(socket.getInputStream());
                
                String incomingConnection = in.readUTF().trim(); //Reads the connection type
                System.out.println(incomingConnection);
                
                if (incomingConnection.contains("Weather")){ //If its a weather station, allocate the data handler
                    //Assign data handler
                    System.out.println("Weather station connected");
                    WSList.add(ID);
                    serverGUI.addWeatherStation(String.valueOf(ID) + "\n");          
                    new Thread (new DataHandler(socket, ID)).start();
                    out.writeUTF("Y");
                } else if (incomingConnection.contains("Client")){ //If its a client connecting, allocate the client handler
                    //Assign client handler
                    clientList.add(ID);
                    serverGUI.addClient(String.valueOf(ID) + "\n");
                    new Thread (new ClientHandler(socket, ID)).start();
                    out.writeUTF("Y");
                }                              
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }
    
    /**
     * The data handler class.
     * Each weather station is allocated a handler to read its data.
     */
    public static class DataHandler implements Runnable {
        Socket socket = null;
        Integer ID = null;
        public DataHandler(Socket _theSocket, Integer _ID) {
            socket = _theSocket;
            ID = _ID;
        }
        
        /**
         * run
         * DataHandler's override of Thread.run().
         */
        @Override
        public void run() {
            while(true) {
                DataInputStream in;
                try {
                    in = new DataInputStream(socket.getInputStream());
                    System.out.println(in.readUTF());
                    //Breadkdown the input string to the measurables
                    String line = null;
                    
                    while((line = in.readUTF()) != null){
                        StringTokenizer st = new StringTokenizer(line,"-");
                       
                        String id_INP = st.nextToken().trim();
                        Integer temperature = Integer.parseInt(st.nextToken().trim());
                        String relativeHumidity = st.nextToken().trim();                                    
                        String barometicP = st.nextToken().trim();
                        String windForce = st.nextToken().trim();
                        String time = st.nextToken().trim();  
                        
                         //Add to dictionary for that weather station 
                        
                        if (weatherStationValues.get(ID) == null){    //If the weather station is new, create a new weatherStationData object
                            weatherStationData x = new weatherStationData(temperature, relativeHumidity, barometicP, windForce, time);
                            weatherStationValues.put(ID, x);
                        }  else { //If the weather station already exists, update the values passed 
                            weatherStationValues.get(ID).setTemp(temperature);
                            weatherStationValues.get(ID).setHumidity(relativeHumidity);
                            weatherStationValues.get(ID).setBarPressure(barometicP);
                            weatherStationValues.get(ID).setWindForce(windForce); 
                            weatherStationValues.get(ID).setTime(time);
                        }
                        System.out.println(ID + " - " + time);
                        
                    }
                } catch (IOException | NullPointerException e) {
                    System.out.println(e.getMessage());
                    return; // If an input stream cannot be created, terminate the station.
                }
            }
        }
    }
    
    public static class ClientHandler implements Runnable{
        
        Socket socket;
        Integer ID;
        Boolean loggedIn = false;
        public ClientHandler() {
        }
         
        public ClientHandler(Socket _theSocket, Integer _ID){
            socket = _theSocket;
            ID = _ID;
        }
        
        @Override
        public void run(){
            try {
                DataInputStream in = new DataInputStream(socket.getInputStream());
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                System.out.println("Connected to client " + ID);
                //Listen for various requests to dispaly weather station data
                
                //Login first!
                String loginString = in.readUTF();
                StringTokenizer st1 = new StringTokenizer(loginString,":");
                String inputType = st1.nextToken();
                
                if (inputType.contains("Login")){
                    System.out.println("Login request recieved  - " + loginString);
                    String un = st1.nextToken();
                    String pw = st1.nextToken();
                    if (validateLogin(un, pw)){ //If the login details are valid, respond with a confirm token
                        loggedIn = true;
                        System.out.println("Logged in client " + ID);
                        out.writeUTF("Accepted");
                    }  else {
                        out.writeUTF("Invalid");
                        System.out.println("Invalid login attempt");
                    }
                } else {
                    socket.close();
                    System.out.println("Disconnected from " + ID);           
                }
      
                while (loggedIn) {                    
                    //Take inputs from client for various data reqeusts .
                    String request = in.readUTF();                 
                    System.out.println("Request - " + request);
                    if (request.contains("RAWS")){  //Get all weather stations (REQUEST ALL WEATHER STATIONS)
                        String allWS = "";
                        for (int i = 0; i < WSList.size(); i++) {
                            allWS += WSList.get(i);
                            if (i < WSList.size() - 1){
                                allWS += ":";
                            }
                        }
                        
                        out.writeUTF(allWS);
                        System.out.println("All weather stations sent");
                        
                    } else if (request.contains("RWS")){ //Return data for a weather station (RWS:XXXX)
                        try {
                            Integer reqStation = Integer.parseInt(request.substring(request.indexOf(":") + 1));
                            System.out.println(reqStation);
                            String outString = ""; 
                            
                            outString += weatherStationValues.get(reqStation).getTemp() + "-" +
                                         weatherStationValues.get(reqStation).getHumidity() + "-" +
                                         weatherStationValues.get(reqStation).getBarPressure()+ "-" +
                                         weatherStationValues.get(reqStation).getWindForce() + "-" + 
                                         weatherStationValues.get(reqStation).getTime();
                            
                            out.writeUTF(outString);
                            System.out.println("Sent WS data to " + ID);
                            
                        } catch (Exception e) {
                            System.err.println("Invalid Request - " + e.getMessage());
                        }                     
                    }
                }
                            
            } catch (Exception e) {
            }
            //
        }
       
    }
    //Returns if the username and parameters and valid credentials
    private static Boolean validateLogin(String _UN, String _PW) {
        String loginString;
        try{
                String filePath = new File("").getAbsolutePath();
                FileReader fin = new FileReader(filePath + "\\src\\resources\\logins.txt");
                BufferedReader din = new BufferedReader(fin);

                while((loginString = din.readLine()) != null) {
                    StringTokenizer st = new StringTokenizer(loginString, ":");
                    String UN = st.nextToken().trim();
                    String PW = st.nextToken().trim();

                    if (UN.contentEquals(_UN) && PW.contentEquals(_PW)) {
                        din.close();
                        return true;
                    }
                }
        } catch (IOException e) {
                System.err.println("Error! - " + e.getMessage());
        }

        return false;

    }
}