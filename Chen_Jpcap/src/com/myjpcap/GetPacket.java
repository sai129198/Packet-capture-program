package com.myjpcap;

import com.ui.Main;

import jpcap.PacketReceiver;
import jpcap.packet.EthernetPacket;
import jpcap.packet.Packet;
import jpcap.packet.TCPPacket;
import jpcap.packet.UDPPacket;

/**
 * ����ʵ��PacketReceiver�ӿ�
 * @author �����
 * @since 2014/4/19
 */
public class GetPacket implements PacketReceiver {
	Main main;	//�����ھ��
	
	public GetPacket(Main main){
		this.main = main;
	}
	
	@Override
	public void receivePacket(Packet packet) {
		EthernetPacket ethernet = (EthernetPacket)packet.datalink;
		this.main.addPacket(packet);
	}

}
