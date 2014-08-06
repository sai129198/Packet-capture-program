package com.ui;

import java.awt.EventQueue;

import javax.swing.JFrame;

import jpcap.JpcapCaptor;
import jpcap.NetworkInterface;

import javax.swing.JList;
import java.awt.BorderLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;

import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.JScrollPane;

import com.myjpcap.MyJpcap;
import com.util.NetUtil;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Date;

import jpcap.packet.*;

import javax.swing.JTree;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JTextArea;

/**
 * 抓包程序的主类。程序界面和PPT上的程序截图有点不一样，因为我去掉了“保存”按钮和两个复选框，原因是那还没来得及做。
 * 做PPT的时候疏忽了，忘记去掉它们。不过无关紧要了，实验目的不是这个。
 * 本程序使用的jpcap是0.7版本，注意看jar包内有两个包：jpcap和jpcap.packet，版本不对则无法运行.
 * 程序运行的时候，请先选择设备。点击开始后无数据显示，则说明选择的网卡不对。我这电脑上0号是有线网卡，1号是无线，2号是虚拟。
 * 注：jpcap.packet包内的数据结构的类关系图见PPT，这点对理解jpcap编程很重要
 * @author 陈祖煌
 * @since 2014/4/18
 */
public class Main {

	private JFrame frame;
	private NetworkInterface[] devices = JpcapCaptor.getDeviceList();
	private int selectedDevice = 0; 	//用户选择的设备顺序，默认为0
	private long index = 0; 	//	数据包序号
	
	
	public JButton btnStart;
	public Thread getPacketThread;	//抓包线程
	public JButton btnStop;
	private JTable table;
	JTextArea textAreaData;
	JTextArea textAreaString;
	private JTree packetTree;
	private DefaultTreeModel treeModel;
	
	public Main main = this;
	
	DefaultTableModel packetModel;	//tableModel
	ArrayList<Packet> packetList = new ArrayList<Packet>();	//Packet数组，用来存放packet的

	/**
	 * 程序的入口。。
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Main window = new Main();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * 注意这个不是程序入口main函数，创建类的时候手抖，取了个Main做类名。。。
	 */
	public Main() {
		initialize();
	}

	/**
	 * 这里主要是界面Swing编程，不多写注释了。程序核心部分还是抓包那块~~
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setTitle("\u8BA1\u7B97\u673A\u7F51\u7EDC\u5B9E\u9A8C\u4E09");	//不明白百度下unicode~
		frame.setBounds(100, 100, 1100, 714);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		btnStart = new JButton("\u5F00\u59CB");
		btnStart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				getPacketThread = new MyJpcap(selectedDevice, main);
				getPacketThread.start();
				btnStart.setEnabled(false);
				btnStop.setEnabled(true);
			}
		});
		btnStart.setBounds(171, 0, 93, 23);
		frame.getContentPane().add(btnStart);
		
		JComboBox cbBoxDevice = new JComboBox();
		
		String model[] = new String[devices.length];
		for(int i = 0; i < model.length; i++){
			model[i] = i+"";
		}
		cbBoxDevice.setModel(new DefaultComboBoxModel(model));
		cbBoxDevice.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange() == e.SELECTED){	//如果是选中事件
					selectedDevice = Integer.parseInt((String) e.getItem());	//更改选中的设备
				}
				
			}
		});
		cbBoxDevice.setBounds(75, 1, 58, 21);
		frame.getContentPane().add(cbBoxDevice);
		
		btnStop = new JButton("\u505C\u6B62");
		btnStop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				((MyJpcap) getPacketThread).breakLoop();
				getPacketThread.stop();
				btnStart.setEnabled(true);
				btnStop.setEnabled(false);
			}
		});
		btnStop.setEnabled(false);
		btnStop.setBounds(289, 0, 93, 23);
		frame.getContentPane().add(btnStop);
		
		JLabel lblDevice = new JLabel("\u9009\u62E9\u8BBE\u5907");
		lblDevice.setBounds(10, 4, 54, 15);
		frame.getContentPane().add(lblDevice);
		packetModel = new  DefaultTableModel(
				new Object[][] {},
				new String[] {
					"序号", "时间", "源地址", "目的地址", "协议", "长度", "信息"
				});
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(10, 74, 1064, 268);
		frame.getContentPane().add(scrollPane);
		
		table = new JTable();
		scrollPane.setViewportView(table);
		table.setModel(packetModel);
		
		//点击table事件
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				int index = table.getSelectedRow();
				Packet packet = packetList.get(index);
				setTree(packet);
				setDataArea(packet);
			}
		});
			
		JButton btnClear = new JButton("\u6E05\u7A7A");
		btnClear.addActionListener(new ActionListener() {	//清空信息
			public void actionPerformed(ActionEvent e) {
				packetList.clear();
				long length = packetModel.getRowCount();
				for(int i = (int) (length - 1); i >= 0; i--){
					packetModel.removeRow(i);
				}
			}
		});
		btnClear.setBounds(415, 0, 93, 23);
		frame.getContentPane().add(btnClear);
		
		DefaultMutableTreeNode t1 = new DefaultMutableTreeNode("Packet",true);
		t1.add(new DefaultMutableTreeNode("null"));
		treeModel = new DefaultTreeModel(t1);
		
		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setBounds(10, 352, 297, 310);
		frame.getContentPane().add(scrollPane_1);
		packetTree = new JTree(treeModel);
		scrollPane_1.setViewportView(packetTree);
		
		JScrollPane scrollPaneData = new JScrollPane();
		scrollPaneData.setBounds(315, 352, 373, 310);
		frame.getContentPane().add(scrollPaneData);
		
		//十六进制的
		textAreaData = new JTextArea();
		scrollPaneData.setViewportView(textAreaData);
		textAreaData.setEditable(false);
		
		JScrollPane scrollPaneString = new JScrollPane();
		scrollPaneString.setBounds(698, 352, 376, 310);
		frame.getContentPane().add(scrollPaneString);
		
		//数据字符文本域
		textAreaString = new JTextArea();
		scrollPaneString.setViewportView(textAreaString);
		textAreaString.setEditable(false);
		
		table.getColumnModel().getColumn(0).setMinWidth(30);
		table.getColumnModel().getColumn(0).setMaxWidth(45);
		
		table.getColumnModel().getColumn(1).setMinWidth(120);
		table.getColumnModel().getColumn(1).setMaxWidth(180);
		
		table.getColumnModel().getColumn(2).setMinWidth(100);
		table.getColumnModel().getColumn(2).setMaxWidth(120);
		
		table.getColumnModel().getColumn(3).setMinWidth(100);
		table.getColumnModel().getColumn(3).setMaxWidth(120);
		
		table.getColumnModel().getColumn(4).setMinWidth(50);
		table.getColumnModel().getColumn(4).setMaxWidth(70);
		
		table.getColumnModel().getColumn(5).setMinWidth(40);
		table.getColumnModel().getColumn(5).setMaxWidth(50);
	}
	
	/**
	 * 这个方法往packetList内添加了新捕获到的packet，并且往主界面的JTable中添加一行行的信息.这个方法是供GetPacket调用的.
	 * @param packet 捕获到的包
	 */
	public void addPacket(Packet packet){
		packetList.add(packet);
		index++;
		
		//如果是IP包
		if(packet instanceof IPPacket){
			packet = (IPPacket)packet;
			packetModel.addRow(new Object[]{
					index, 
					new Date(), 
					((IPPacket) packet).src_ip.getHostAddress(),
					((IPPacket) packet).dst_ip.getHostAddress(),
					NetUtil.getProtocol(((IPPacket) packet).protocol),
					packet.len,
					packet.toString()});
		}
		//如果是ARP包
		else if(packet instanceof ARPPacket){
			ARPPacket arp = (ARPPacket)packet;
			packetModel.addRow(new Object[]{
					index,
					new Date(),
					arp.getSenderProtocolAddress().toString(),
					arp.getTargetProtocolAddress().toString(),
					"ARP",
					arp.len,
					arp.toString()
			});
		}
	}
	
	/**
	 * 如果点击table里面的一行packet，则创建一个树来描述其基本信息
	 * @param packet 根据用户点击的序号，从packet数组中获取相应的包来显示
	 */
	public void setTree(Packet packet){
		//创建树
		DefaultMutableTreeNode root = new DefaultMutableTreeNode("包信息");
		root.add(new DefaultMutableTreeNode("捕获长度："+packet.len));	//长度信息
		
		//以太帧信息节点
		DefaultMutableTreeNode ethernetNode = new DefaultMutableTreeNode("以太帧", true);
		EthernetPacket ethernet = (EthernetPacket) packet.datalink;
		ethernetNode.add(new DefaultMutableTreeNode("帧类型"+ethernet.frametype));
		ethernetNode.add(new DefaultMutableTreeNode("源MAC地址："+NetUtil.macToString(ethernet.src_mac)));
		ethernetNode.add(new DefaultMutableTreeNode("目的MAC地址："+NetUtil.macToString(ethernet.dst_mac)));
		root.add(ethernetNode);
		
		//如果是IP数据包
		if(packet instanceof IPPacket){
			IPPacket ip = (IPPacket)packet;
			DefaultMutableTreeNode ipNode = new DefaultMutableTreeNode("IP数据报", true);
			ipNode.add(new DefaultMutableTreeNode("版本："+ip.version));
			ipNode.add(new DefaultMutableTreeNode("优先权："+ip.priority));
			ipNode.add(new DefaultMutableTreeNode("包长度："+ip.length));
			ipNode.add(new DefaultMutableTreeNode("协议："+NetUtil.getProtocol(ip.protocol)));
			ipNode.add(new DefaultMutableTreeNode("TTL："+ip.hop_limit));
			ipNode.add(new DefaultMutableTreeNode("标识："+ip.ident));
			ipNode.add(new DefaultMutableTreeNode("区分服务：最大吞吐量"+ip.t_flag));
			ipNode.add(new DefaultMutableTreeNode("区分服务：最高可靠性"+ip.r_flag));
			ipNode.add(new DefaultMutableTreeNode("DF(不能分片)："+ip.dont_frag));	//只有当DF=0时才允许分片。
			ipNode.add(new DefaultMutableTreeNode("MF(还有分片)："+ip.more_frag));	//MF=0表示这已是若干数据报片中的最后一个。
			ipNode.add(new DefaultMutableTreeNode("片位移："+ip.offset));
			ipNode.add(new DefaultMutableTreeNode("源IP："+ip.src_ip.getHostAddress()));
			//ipNode.add(new DefaultMutableTreeNode("源主机："+ip.src_ip.getHostName()));
			ipNode.add(new DefaultMutableTreeNode("目的IP："+ip.dst_ip.getHostAddress()));
			//ipNode.add(new DefaultMutableTreeNode("目的主机："+ip.dst_ip.getHostName()));
			root.add(ipNode);
			
			//如果是UDP数据报
			if(ip instanceof UDPPacket){
				UDPPacket udp = (UDPPacket)ip;
				DefaultMutableTreeNode udpNode = new DefaultMutableTreeNode("UDP报文",true);
				udpNode.add(new DefaultMutableTreeNode("源端口：" + udp.src_port));
				udpNode.add(new DefaultMutableTreeNode("目的端口：" + udp.dst_port));
				udpNode.add(new DefaultMutableTreeNode("报文长度：" + udp.length));
				root.add(udpNode);
			}
			/**
			 * TCP三次握手的抓包具体分析请看PPT
			 */
			else if(ip instanceof TCPPacket){
				TCPPacket tcp = (TCPPacket)ip;
				DefaultMutableTreeNode tcpNode = new DefaultMutableTreeNode("TCP报文",true);
				tcpNode.add(new DefaultMutableTreeNode("源端口：" + tcp.src_port));
				tcpNode.add(new DefaultMutableTreeNode("目的端口：" + tcp.dst_port));
				tcpNode.add(new DefaultMutableTreeNode("报文长度：" + tcp.length));
				tcpNode.add(new DefaultMutableTreeNode("SYN：" + tcp.syn));	//一个SYN包就是仅SYN标记设为1的TCP包
				tcpNode.add(new DefaultMutableTreeNode("ACK：" + tcp.ack));	//SYN/ACK包的ACK和SYN位都是true,也就是当这两个位都是true的时候，这个为三次握手中的第二步
				tcpNode.add(new DefaultMutableTreeNode("确认号：" + tcp.ack_num));	//ACK包就是仅ACK标记设为1的TCP包.需要注意的是当三此握手完成.连接建立以后，TCP连接的每个包都会设置ACK位
				tcpNode.add(new DefaultMutableTreeNode("序号：" + tcp.sequence));
				tcpNode.add(new DefaultMutableTreeNode("FIN：" + tcp.fin));	//断开连接
				tcpNode.add(new DefaultMutableTreeNode("URG：" + tcp.urg));	//当URG＝1时，表明紧急指针字段有效。它告诉系统此报文段中有紧急数据，应尽快传送(相当于高优先级的数据)。
				tcpNode.add(new DefaultMutableTreeNode("URG指针：" + tcp.urgent_pointer));
				tcpNode.add(new DefaultMutableTreeNode("数据(PSH)：" + tcp.psh));	//此TCP是否有数据传输
				tcpNode.add(new DefaultMutableTreeNode("数据偏移：" + tcp.offset));//占4bit，它指出TCP报文段的数据起始处距离TCP报文段的起始处有多远。“数据偏移”的单位不是字节而是32bit字（4字节为计算单位）。
				tcpNode.add(new DefaultMutableTreeNode("连接重置(RST)：" + tcp.rst));	//连接重置，一般在FIN之后才会出现true.当RST＝1时，表明TCP连接中出现严重差错（如由于主机崩溃或其他原因），必须释放连接，然后再重新建立运输连接。
				tcpNode.add(new DefaultMutableTreeNode("窗口：" + tcp.window));	//占2字节。窗口字段用来控制对方发送的数据量，单位为字节。TCP连接的一端根据设置的缓存空间大小确定自己的接收窗口大小，然后通知对方以确定对方的发送窗口的上限。
				//tcpNode.add(new DefaultMutableTreeNode("选项：" + tcp.option));	//选项
				root.add(tcpNode);
			}
			else if(ip instanceof ICMPPacket){
				ICMPPacket icmp = (ICMPPacket)ip;
				DefaultMutableTreeNode icmpNode = new DefaultMutableTreeNode("ICMP",true);
				icmpNode.add(new DefaultMutableTreeNode("类型：" + icmp.type));
				icmpNode.add(new DefaultMutableTreeNode("代码：" + icmp.code));
				icmpNode.add(new DefaultMutableTreeNode("校验和：" + icmp.checksum));
				root.add(icmpNode);
			}
		}
		else if(packet instanceof ARPPacket){
			ARPPacket arp = (ARPPacket)packet;
			DefaultMutableTreeNode arpNode = new DefaultMutableTreeNode("ARP数据报", true);
			arpNode.add(new DefaultMutableTreeNode("硬件类型：" + NetUtil.getHardType(arp.hardtype)));
			arpNode.add(new DefaultMutableTreeNode("协议类型：" + ((arp.prototype==2048)?"IP":"Other")));
			arpNode.add(new DefaultMutableTreeNode("协议长度：" + arp.plen + "字节"));
			arpNode.add(new DefaultMutableTreeNode("硬件长度：" + arp.hlen + "字节"));
			arpNode.add(new DefaultMutableTreeNode("操作类型：" + NetUtil.getOperationType(arp.operation)));
			root.add(arpNode);
		}
		
		treeModel.setRoot(root);
		
	}
	
	/**
	 * 如果点击table里面的一行packet，则显示这个包的16进制数据、字符数据(分列显示)
	 * @param packet 根据用户点击的序号，从packet数组中获取相应的包来显示
	 */
	public void setDataArea(Packet packet){
		this.textAreaData.setText("");
		this.textAreaData.append("十六进制数据：\n");
		this.textAreaString.setText("");
		this.textAreaString.append("字符数据(通过ASCII转换)：\n");
		if(packet instanceof TCPPacket){
			TCPPacket tcp = (TCPPacket)packet;
			this.textAreaData.append("数据长度" + tcp.data.length+"\n");
			this.textAreaString.append("数据长度" + tcp.data.length+"\n");
			int num = 20; 	//每行显示多少个
			for(int i=0; i < tcp.data.length; i++){
	   			String hex = Integer.toHexString((tcp.data[i]&0xff));
	   			if(hex.length() == 1){
	   				hex = "0" + hex;
	   			}
	   			
	   			this.textAreaData.append(hex+" ");
	   			this.textAreaString.append((char)Integer.parseInt(hex, 16)+"");
	   			if(num == 1){
	   				num = 20;
	   				this.textAreaData.append("\n");
	   			}
	   			num--;
	   		}
		}
		else if(packet instanceof UDPPacket){
			UDPPacket udp = (UDPPacket)packet;
			this.textAreaData.append("数据长度" + udp.data.length+"\n");
			this.textAreaString.append("数据长度" + udp.data.length+"\n");
			int num = 20; 	//每行显示多少个字符
			for(int i=0; i < udp.data.length; i++){
	   			String hex = Integer.toHexString((udp.data[i]&0xff));
	   			if(hex.length() == 1){
	   				hex = "0" + hex;
	   			}
	   			this.textAreaData.append(hex+" ");
	   			this.textAreaString.append((char)Integer.parseInt(hex, 16)+"");
	   			if(num == 1){
	   				num = 20;
	   				this.textAreaData.append("\n");
	   			}
	   			num--;
	   		}
		}
		else if(packet instanceof ARPPacket){
			ARPPacket arp = (ARPPacket)packet;
			this.textAreaData.append("数据长度" + arp.data.length+"\n");
			this.textAreaString.append("数据长度" + arp.data.length+"\n");
			int num = 20; 	//每行显示多少个字符
			for(int i=0; i < arp.data.length; i++){
	   			String hex = Integer.toHexString((arp.data[i]&0xff));
	   			if(hex.length() == 1){
	   				hex = "0" + hex;
	   			}
	   			this.textAreaData.append(hex+" ");
	   			this.textAreaString.append((char)Integer.parseInt(hex, 16)+"");
	   			if(num == 1){
	   				num = 20;
	   				this.textAreaData.append("\n");
	   			}
	   			num--;
	   		}
		}
	}
}
