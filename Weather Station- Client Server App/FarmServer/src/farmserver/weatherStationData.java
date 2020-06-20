/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package farmserver;

/**
 *
 * @author jamiehaywood
 */
public class weatherStationData {
    
    private Integer temperature;
    private String humidity;
    private String barPressure;
    private String windForce;
    private String time;
    
    public weatherStationData(){    
        humidity = "";
        barPressure = "";
        windForce = "";
        temperature = null;
        time = "";
                
    }
    
    public weatherStationData(Integer _temp, String _hum, String _barpPressure, String _windForce, String _time){
        temperature = _temp;
        humidity = _hum;
        barPressure = _barpPressure;
        windForce = _windForce;
        time = _time;
    }
    
    public String getTemp(){
        return temperature.toString();
    }
    
    public void setTemp(Integer _temp){
        temperature = _temp;
    }
    public String getHumidity(){
        return humidity;
    }
    public void setHumidity(String _hum){
        humidity = _hum;
    }
    public String getBarPressure(){
        return barPressure;
    }
    public void setBarPressure(String _BP){
        barPressure = _BP;
    }
    public String getWindForce(){
        return windForce;
    }
    public void setWindForce(String _WF){
        windForce = _WF;
    }
     public String getTime(){
        return time;
    }
    public void setTime(String _Time){
        time = _Time;
    }
}
