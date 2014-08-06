package com.test;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Arrays;

import jpcap.JpcapCaptor;
import jpcap.JpcapSender;
import jpcap.packet.ARPPacket;
import jpcap.packet.Packet;

public class AcceptThread implements Runnable {
	InetAddress senderIP; // = InetAddress.getByName("10.96.69.74");
	JpcapCaptor jc;
	public AcceptThread(InetAddress senderIP, JpcapCaptor jc){
		this.senderIP = senderIP;
		this.jc = jc;
	}
	
	@Override
	public void run(){
		long i = 1;
		//JpcapSender sender = jc.getJpcapSenderInstance();
		while(true){                                                //获取ARP回复包，从中提取出目的主机的MAC地址
				Packet packet = jc.getPacket();
				   if(packet instanceof ARPPacket){
					   ARPPacket p=(ARPPacket)packet;
					   if(Arrays.equals(p.target_protoaddr,senderIP.getAddress())){
						   System.out.println(Attack.macToString(p.sender_hardaddr));
						   System.out.println(Attack.ipToString(p.sender_protoaddr));
						   //System.out.println(p.operation);
						   System.out.println(i++);
						   //return p.sender_hardaddr;
					   }
				}
		}
	}

}
