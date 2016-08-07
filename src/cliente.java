/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Mirko Gueregat
 */
public class cliente {
    private byte[] MAC;
    private byte[] ID;
    private boolean offer;
    private boolean ACK;
    
    public cliente(byte[]m,byte[]i){
        MAC=m;
        ID=i;
    }
    
    public void setMAC(byte[] m){
        MAC=m;
    }
    public byte[] getMAC(){
        return MAC;
    }
    public void setID(byte[] i){
        ID=i;
    }
    public byte[] getID(){
        return ID;
    }
    public void setOffer(boolean b){
        offer=b;
    }
    public boolean getOffer(){
        return offer;
    }
    public void setACK(boolean b){
        ACK=b;
    }
    public boolean getACK(){
        return ACK;
    }    
}
