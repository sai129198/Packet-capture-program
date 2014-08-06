package com.myjpcap;

import java.io.IOException;

import com.ui.Main;

import jpcap.packet.*;
import jpcap.*;

/**
 * 该类继承Thread，用多线程避免界面阻塞，大家应该不陌生吧
 * @author 陈祖煌
 * @since 2014/4/18
 */
public class MyJpcap extends Thread {
	private int device = 0;	//默认选中的设备
	JpcapCaptor jpcap;
	Main main;	//主窗口句柄
	
	public MyJpcap(int device, Main main){
		this.device = device;
		this.main = main;
	}
	
	@Override
	public void run() {
		try {
			jpcap = JpcapCaptor.openDevice(JpcapCaptor.getDeviceList()[device], 65535, false, 20);	//设备名称，每次捕获IP包最大长度，false为混杂模式，超时时间
			jpcap.loopPacket(-1, new GetPacket(this.main));
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("wrong!");
		
		}	
	}
	
	//停止抓包
	public void breakLoop(){
		jpcap.breakLoop();
	}
}
