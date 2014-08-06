package com.test;

import java.io.IOException;

//import java.net.NetworkInterface;
import jpcap.*;
import jpcap.packet.*;

public class Test {
	
	public static void main(String[] args) throws IOException {
		/**
		 * 获取本地的网络设备列表，有无线网卡、有线网卡，win7还有虚拟网卡
		 * NetworkInterface是jpcap里面的一个类
		 */
		NetworkInterface[] devices = JpcapCaptor.getDeviceList();
		
		/**
		 * 创建一个与指定设备的连接并返回该连接。
		 * 第一个参数：指定一个设备
		 * 第二个参数：限制每一次收到一个数据包，只提取该数据包中前多少字节；
		 * 第三个参数：设置模式。false为混杂模式。混杂模式可抓取所有经过本地网络设备的数据包
		 * 第四个参数：超时时间。超过一定时间捕获不到就返回。
		 */
		JpcapCaptor jpcap = JpcapCaptor.openDevice(devices[0], 65535, false, 20);	//65535bit
		
		/**
		 * 捕获指定数目的包。第一个参数是抓包的数目，-1表示不停地抓包，第二个参数为实现了PacketReceiver接口的类，返回的是捕获包的数目
		 * loopPacket自动将抓来的包交给这个类来处理，注意是每抓到一个包就处理一次
		 * 此方法不受超时限制影响
		 */
		jpcap.loopPacket(-1, new MyJpcap());
		
		/**
		 * 功能和上面的方法一样，但是不受超时影响
		 */
		jpcap.processPacket(10, new MyJpcap());
		
		/**
		 * 返回一个捕获到的Packet
		 */
		Packet packet = jpcap.getPacket();	
		
		/**
		 * 终止正在抓包的任务
		 */
		jpcap.breakLoop();
		
		/**
		 * 断开与设备的连接
		 */
		jpcap.close();
	}
	
	
	
	
	public static String macToString(byte[] s){
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
}
