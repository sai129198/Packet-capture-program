package com.util;

/**
 * 此类为工具类，内涵各种转换，挺有用。
 * @author 陈祖煌
 * @since 2014/4/20
 */
public class NetUtil {

	/**将byte数组内的mac地址转换为字符串
	 * @param s byte数组，内有二进制表示的MAC地址
	 * @return MAC地址的字符串表示
	 */
	public static String macToString(byte[] s){
		String mac = "";
		for (int i = 0; i < s.length; i++){
			String temp = Integer.toHexString(s[i] & 0xff);
			if(temp.length() == 1) temp = "0" + temp;	//补0
			mac += temp;
		}
		return mac;
	}
	
	/**将byte数组的ip地址转换为字符串
	 * @param s 二进制表示的ip地址
	 * @return IP地址的字符串表示
	 */
	public static String ipToString(byte[] s){
		String ip = "";
		for (int i = 0; i < s.length; i++){
			String temp = Integer.toString(s[i] & 0xff);
			if(temp.length() == 1) temp = "0" + temp;	//补0
			ip += temp;
		}
		return ip;
	}
	
	/**判断IP数据报协议类型
	 * @param a IP包的协议类型数值
	 * @return 字符串形式的协议名称
	 */
	public static String getProtocol(int a){
		switch(a){
		case 0: return "HOPOPT";	//逐跳选项 
		case 1: return "ICMP";		//Internet控制消息
		case 2:	return "IGMP";		//Internet组管理
		case 4:	return "IP";		//
		case 6: return "TCP";
		case 17: return "UDP";
		case 41: return "IPv6";
		case 43: return "IPv6_Route";	//IPv6的路由标头
		case 44: return "IPv6_Frag";	//IPv6的片段标头
		case 58: return "IPv6_ICMP";	//IPv6_ICMP
		case 59: return "IPv6_NoNxt";	//用于 IPv6 的无下一个标头
		case 60: return "IPv6_Opts";	//IPv6 的目标选项
		default:	return "Other";
		}
	}
	
	/**获取ARP数据报的硬件类型
	 * @param a 硬件类型hardtype
	 * @return 字符串表示
	 */
	public static String getHardType(int a){
		switch(a){
		case 1: return "以太网";
		case 6: return "IEEE802";
		case 15: return "Frame-Relay(帧中继)";
		default: return "Other";
		}
	}
	
	/**ARP数据报操作类型
	 * @param a 操作类型数值
	 * @return 字符串表示
	 */
	public static String getOperationType(int a){
		switch(a){
		case 1: return "ARP请求";
		case 2: return "ARP应答";
		case 3: return "RARP请求";
		case 4: return "RARP应答";
		default: return "Other";
		}
	}
}
