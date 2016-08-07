import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.*;

/**
 *
 * @author Mirko Gueregat
 */


public class UDPReceive2 {
    private boolean estado;
    private DatagramSocket dsocket;
    int port;
    private String serverIP,DNS1,DNS2,ipInicial,ipFinal,mascaraSubRed,routerIP;
    private InetAddress hostAddress;
    private DatagramPacket packet;
    private byte[] buffer;
    Hashtable<String,cliente> tabla;
    private byte[] lastIP;
    public UDPReceive2(){
        try{
            tabla=new Hashtable<String,cliente>();
            hostAddress= InetAddress.getByName("255.255.255.255");
            buffer=new byte[1024];
            packet = new DatagramPacket(buffer, buffer.length);
            dsocket = new DatagramSocket(67);
            InetAddress localHost = InetAddress.getLocalHost();
            DNS1="10.10.30.1";
            DNS2="0.0.0.0";
            mascaraSubRed="255.255.255.0";
            routerIP="0.0.0.0";
            serverIP=localHost.getHostAddress();
            ipInicial="10.10.20.100";
            ipFinal="10.10.20.150";            
            lastIP=ipToByte(ipInicial);
            System.out.println("dasdas");
            
        }
        catch (Exception e){
        }
    }
    
    public void run(){
        System.out.println("Servicio iniciado");
        byte[] ID;
        byte[] MACCliente;
        byte [] salida;
        int DHCPtype;
        DatagramPacket out;
        while(estado==true){            
            try{
                dsocket.receive(packet);
                DHCPtype=getDHCPType(buffer);
                if(DHCPtype==1||DHCPtype==3){
                    ID=getTransactionID(buffer);
                    MACCliente=getMACCliente(buffer); 
                    switch(DHCPtype){
                        case 1:                       
                            salida=offer(ID,MACCliente,addOffer(ID,MACCliente),ipToByte(serverIP),ipToByte(routerIP),ipToByte(mascaraSubRed),ipToByte(DNS1),ipToByte(DNS2));
                            dsocket.send(new DatagramPacket(salida, salida.length, hostAddress, 68));
                            System.out.println("Offer enviada");
                            break;
                        case 3:
                            String ip=getRequestedIP(buffer);
                            System.out.println(ip+" cont: "+tabla.containsKey(ip));
                            if(tabla.containsKey(ip)==true){
                                System.out.println("entro if cont");
                                boolean exist=verificarExist(ID,MACCliente,ip);
                                if(exist){//MOD
                                    System.out.println("OFFER EXIST");
                                    confirmACK(ip);//MOD
                                    System.out.println("llego");
                                    salida=ACK(ID,MACCliente,ipToByte(ip),ipToByte(serverIP),ipToByte(routerIP),ipToByte(mascaraSubRed),ipToByte(DNS1),ipToByte(DNS2));

                                    out = new DatagramPacket(salida, salida.length, hostAddress, 68);
                                    dsocket.send(out);
                                }
                            }
                            break;                        
                    }
                }
            }
            catch (Exception e) {
                System.err.println(e);
            }
            
        }
    }
    public void setEstado(boolean estate){
        estado=estate;
    }
    public boolean getEstado(){
        return estado;
    }
    public void setServerIP(String IP){
        serverIP=IP;
    }
    public String getServerIP(){
        return serverIP;
    }
    public void setDNS1(String DNS){
        DNS1=DNS;
    }
    public String getDNS1(){
        return DNS1;
    }
    public void setDNS2(String DNS){
        DNS2=DNS;
    }
    public String getDNS2(){
        return DNS2;
    }
    public void setIPInicial(String IP){
        ipInicial=IP;
    }
    public String getIPInicial(){
        return ipInicial;
    }
    public void setIPFinal(String IP){
        ipFinal=IP;
    }
    public String getIPFinal(){
        return ipFinal;
    }
    public void setMascara(String mascara){
        mascaraSubRed=mascara;
    }
    public String getMascara(){
        return mascaraSubRed;
    }
    public void setRouterIP(String IP){
        routerIP=IP;
    }
    public String getRouterIP(){
        return routerIP;
    }
    
    public byte[] addOffer(byte[] id, byte[] mac ){
        cliente temp=new cliente(mac,id);
        byte[] ip=getNewIP();
        String ipS=ipToString(ip);
        tabla.put(ipS,temp);
        temp=(cliente)tabla.get(ipS);
        System.out.println(charToString(temp.getID()));
        
        System.out.println(ipS+" eviada: "+tabla.containsKey(ipS));        
        return ip;
        
    }
    public String ipToString(byte[] ip){
        String a="";
        for(int i=0;i<4;i++){
            if(i<3)a=a+(ip[i]& 0xff)+".";
            else a=a+(ip[i]& 0xff);
        }
        return a;
    }
    public void confirmACK(String ip){
        cliente temp=(cliente)tabla.get(ip);
        temp.setACK(true);
    }
    
    public byte[] getNewIP(){
        byte[] ipB=lastIP;
        byte[] ipA=lastIP;
        int ip4=(int)ipA[3];
        int ip3=(int)ipA[2];
        int ip4F=(int)ipToByte(ipFinal)[3];
        int ip3F=(int)ipToByte(ipFinal)[2];
        if(ip4+1<=255&&ip4+1<=ip4F){
            ipB[3]=(byte)(ip4+1);
        }
        else if(ip3+1<=ip4F){
            ipB[3]=(byte)0;
            ipB[2]=(byte)(ip3+1);
        }
        lastIP=ipB; 
        if(Arrays.equals(ipA,ipToByte(routerIP))==true)return getNewIP();
        else return ipA;
    }
      
    public boolean compararArray(byte[] a,byte[] b){
        boolean band=false;
        if(Arrays.equals(a,b)==true)band=true;
        return band;
    }
    
    public boolean verificarExist(byte[] id,byte[] mac,String ip){
        boolean resultado=false;
        try{
            cliente temp=(cliente)tabla.get(ip);
            System.out.println("resultado: "+compararArray(temp.getMAC(),mac));
            if(compararArray(temp.getMAC(),mac)==true){
                resultado=true;
            }
        }
        catch(Exception e){
            System.err.println(e);
            System.out.println("excepcion");
            return false;
        }
        return resultado;
    }
    
    public byte[] getTransactionID(byte [] buff){
        byte[] ID=new byte[4];
        for(int i=4;i<8;i++){
            ID[i-4]=buff[i];
        }
        return ID;
    }
    public byte[] getMACCliente(byte[] buff){//CAMBIO
        byte[] MAC=new byte[6];
        for(int i=0;i<6;i++){
            MAC[i]=buff[i+28];
        }
        return MAC;
    }
    public int getDHCPType(byte[] buff){
        return (int)buff[242];
    }
    
    public String getRequestedIP(byte[] buff){    
        String a="";
        for(int i=240;i<450;i++){
            if((int)buff[i]==50&&(int)buff[i+1]==4){
                for(int j=i+2;j<i+6;j++){
                    a=a+(buff[j]& 0xff);
                    if(j<i+5)a=a+".";
                }                
                break;
            }
        }
        return a;
    }
    
    public static byte[] ACK(byte[] id, byte[] MAC, byte[] IP, byte[] IP_SERVER, byte[] IP_ROUTER, byte[] mascara, byte[] DNS_1, byte[] DNS_2){
	byte [] b=new byte[279];
	b[0]= (byte)2;
	b[1]= (byte)1;
	b[2]= (byte)6;
	b[3]= (byte)0;
	for(int i=4;i<8;i++){
            b[i]=id[i-4];
	}
	for(int i=8;i<12;i++){//
            b[i]= (byte)0;
	}
	for(int i=12;i<16;i++){//CIADDR client ip address
            b[i]= (byte)0;
	}
	for(int i=16;i<20;i++){//YIADDR your ip address
            b[i]= IP[i-16];
	}
	for(int i=20;i<24;i++){//SIADDR
            b[i]= (byte)0;
	}
	for(int i=24;i<28;i++){//GIADDR
            b[i]= (byte)0;
	}
	//CHADDR
	for(int i=28;i<34;i++){//MAC
            b[i]=MAC[i-28];
	}
	for(int i=34;i<238;i++){//OTROS
		b[i]=(byte)0;
	}
	//MAGIC COOKIE
	b[236]=(byte)99;
	b[237]=(byte)130;
	b[238]=(byte)83;
	b[239]=(byte)99;
		
	//OPTIONS
	//OPTION 53 DHCP MESSAGE TYPE
	b[240]=(byte)53;
	b[241]=(byte)1;
	b[242]=(byte)5;
	//OPTION 1 SUBNET MASK
	b[243]=(byte)1;
	b[244]=(byte)4;
	for(int i=245;i<249;i++){
            b[i]=mascara[i-245];
	}
	//OPTION 3	ROUTER
	b[249]=(byte)3;
	b[250]=(byte)4;
        for(int i=251;i<255;i++){
            b[i]=IP_ROUTER[i-251];
        }
		/*b[251]=(byte)0;
		b[252]=(byte)0;
		b[253]=(byte)0;
		b[254]=(byte)0;*/
	//OPTION 51 IP ADDRESS LEASE TIME
	b[255]=(byte)51;
	b[256]=(byte)4;
	for(int i=257;i<261;i++){
            b[i]=(byte)4;
	}		
	//OPTION 54 DHCP SERVER ID
	b[261]=(byte)54;
	b[262]=(byte)4;
	for(int i=263;i<267;i++){
            b[i]=IP_SERVER[i-263];
	}		
        //OPTION 6 DOMAIN NAME SERVER
	b[267]=(byte)6;
        b[268]=(byte)8;
	for(int i=269;i<273;i++){
            b[i]=DNS_1[i-269];
	}	
	for(int i=273;i<277;i++){
            b[i]=DNS_2[i-273];
	}	
		// END
	b[278]=(byte)255;		
	return b;
    }
    public static byte[] offer(byte[] id, byte[] MAC, byte[] IP, byte[] IP_SERVER, byte[] IP_ROUTER, byte[] mascara, byte[] DNS_1, byte[] DNS_2){
        byte [] b=new byte[279];
	b[0]= (byte)2;
	b[1]= (byte)1;
	b[2]= (byte)6;
	b[3]= (byte)0;
	for(int i=4;i<8;i++){
            b[i]=id[i-4];
	}
	for(int i=8;i<12;i++){//
            b[i]= (byte)0;
	}
	for(int i=12;i<16;i++){//CIADDR client ip address
            b[i]= (byte)0;
	}
	for(int i=16;i<20;i++){//YIADDR your ip address
            b[i]= IP[i-16];
	}
	for(int i=20;i<24;i++){//SIADDR
            b[i]= (byte)0;
	}
	for(int i=24;i<28;i++){//GIADDR
            b[i]= (byte)0;
	}
	//CHADDR
	for(int i=28;i<34;i++){//MAC
            b[i]=MAC[i-28];
	}
	for(int i=34;i<238;i++){//OTROS
		b[i]=(byte)0;
	}
	//MAGIC COOKIE
	b[236]=(byte)99;
	b[237]=(byte)130;
	b[238]=(byte)83;
	b[239]=(byte)99;
		
	//OPTIONS
	//OPTION 53 DHCP MESSAGE TYPE
	b[240]=(byte)53;
	b[241]=(byte)1;
	b[242]=(byte)2;
	//OPTION 1 SUBNET MASK
	b[243]=(byte)1;
	b[244]=(byte)4;
	for(int i=245;i<249;i++){
            b[i]=mascara[i-245];
	}
	//OPTION 3	ROUTER
	b[249]=(byte)3;
	b[250]=(byte)4;
        for(int i=251;i<255;i++){
            b[i]=IP_ROUTER[i-251];
        }
		/*b[251]=(byte)0;
		b[252]=(byte)0;
		b[253]=(byte)0;
		b[254]=(byte)0;*/
	//OPTION 51 IP ADDRESS LEASE TIME
	b[255]=(byte)51;
	b[256]=(byte)4;
	for(int i=257;i<261;i++){
            b[i]=(byte)4;
	}		
	//OPTION 54 DHCP SERVER ID
	b[261]=(byte)54;
	b[262]=(byte)4;
	for(int i=263;i<267;i++){
            b[i]=IP_SERVER[i-263];
	}		
		//OPTION 6 DOMAIN NAME SERVER
	b[267]=(byte)6;
        b[268]=(byte)8;
	for(int i=269;i<273;i++){
            b[i]=DNS_1[i-269];
	}	
	for(int i=273;i<277;i++){
            b[i]=DNS_2[i-273];
	}	
		// END
	b[278]=(byte)255;		
	return b;
    }
    public static byte[] ipToByte(String ip){
		String [] arreglo=ip.split("\\.");
		byte [] ipB=new byte[4];
		for(int i=0;i<4;i++){
			ipB[i]=(byte)Integer.parseInt(arreglo[i]);
		}
		return ipB;
	}
    public static String charToString(byte [] bytes){
		StringBuilder sb = new StringBuilder();
		for (byte b : bytes) {
			sb.append(String.format("%02X ", b));
		}
		return sb.toString();
		
	}
    public static byte[] stringToBytesASCII(String str) {
		 char[] buffer = str.toCharArray();
		 byte[] b = new byte[buffer.length];
		 for (int i = 0; i < b.length; i++) {
			 b[i] = (byte) buffer[i];
		}
		return b;
	}
}
