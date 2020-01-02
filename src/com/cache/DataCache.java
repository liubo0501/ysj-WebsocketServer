package com.cache;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import io.netty.channel.Channel;

public class DataCache {
	public static Queue<String> messageQueue = new ConcurrentLinkedQueue<>();
	
	public static Map<String, Channel> channelMap = new ConcurrentHashMap<>();
	public static Map<String, String> userMap = new ConcurrentHashMap<>();
	public static Map<String, String> padMap = new ConcurrentHashMap<>();

}
