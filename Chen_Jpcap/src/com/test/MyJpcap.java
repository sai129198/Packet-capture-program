package com.test;

import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;

import jpcap.PacketReceiver;
import jpcap.packet.EthernetPacket;
import jpcap.packet.IPPacket;
import jpcap.packet.Packet;
import jpcap.packet.TCPPacket;
import jpcap.packet.UDPPacket;


public class MyJpcap implements PacketReceiver {
	static int j=0;
    static long p=0,q=0;
    IPPacket ip;
    String s,s1;
    Long s2;
   static long time11,time12;
	String time;     
   	static long num=0; //加一个静态产量，表示抓的包的序号，不然连自己抓的顺序都不知道
   	//public static ConnectException conn;
	
   	public void print(byte[] b){
   		for(int i=0; i < b.length; i++){
   			String a = Integer.toHexString((b[i]&0xff));//toBinaryString((b[i]&0xff));
   			if(a.length() == 1){
   				a = "0" + a;
   			}
   			System.out.print(a);
   			System.out.print(" ");
   			System.out.print((char)Integer.parseInt(a, 16));
   		}
   		System.out.println();
   	}
   	
	@Override
	public void receivePacket(Packet packet) {
		
		EthernetPacket ethernet = (EthernetPacket)packet.datalink;
		if(packet instanceof TCPPacket){
			TCPPacket tcp = (TCPPacket)packet;
			System.out.println("源IP:"+tcp.src_ip+" 端口:"+tcp.src_port+" 目的IP:"+tcp.dst_ip+" 目的端口:"+tcp.dst_port
				+" 目的MAC:"+ ethernet.dst_mac + " 协议:TCP" + " 数据:");
			for(int i = 0; i < tcp.data.length; i++)
				System.out.print((char)tcp.data[i]);
		
			System.out.println("");
		
			print(tcp.data);
		}
	
	else if(packet instanceof UDPPacket) {
		UDPPacket udp = (UDPPacket)packet;
		
			System.out.println("源IP:"+udp.src_ip+" 端口:"+udp.src_port+" 目的IP:"+udp.dst_ip+" 目的端口:"+udp.dst_port
					+" 目的MAC:"+ ethernet.dst_mac+ " 协议:UDP" + " 数据:");
		
		for(int i = 0; i < udp.data.length; i++)
			System.out.print(udp.data[i]);
		System.out.println("");
		print(udp.data);
		
	}
	
	/*
	//判断是不是是ip包
	if(packet instanceof IPPacket){ 
		//System.out.println(j+"ok");
		ip=(IPPacket)packet ;
		if(j<10000){ 
  	 
  	         
			try {
				RandomAccessFile rf = new RandomAccessFile("packet.txt", "rw");//动态文件
				rf.seek(rf.length());//文件尾
				rf.writeBytes(num+"\t"+ip.src_ip+"\t"+ip.dst_ip+"\t"+ip.protocol+"\t"+
				ip.length+"\t"+ip.version+"\t"+ip.ident+"\t"+
				ip.rsv_frag+"\t"+ip.offset+"\t"+ip.hop_limit+"\t"+
				ip.rsv_tos+"\r\n");//写入
				rf.close();//关闭文件
				num++;
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			
			
			//上层协议TCP(6)和UDP(17)
			System.out.println(
			num+"\t源:"+ip.src_ip+"\t目:"+ip.dst_ip+"\t上层协议:"+ip.protocol+"\t长度:"+
			ip.length+"\t版本:"+ip.version+"\t标识:"+ip.ident+"\t"+
			ip.rsv_frag+"\t"+ip.offset+"\t"+ip.hop_limit+"\t"+
			ip.rsv_tos+"\tTTL:"+ip.hop_limit+"\r\n");
			j++;
			
		}
		*/
    /*   
	else {
    	   time12=System.currentTimeMillis();//当前系统时间
    	   System.out.println(""+time12+"-"+time11+"="+(time12-time11)+"\ncapture count:"+j);
    	   System.exit(0);
    }
    */
	}
}


