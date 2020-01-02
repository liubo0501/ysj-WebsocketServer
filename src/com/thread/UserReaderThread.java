package com.thread;

import com.anbot.server.kafkatool.TopicReaderLatest;
import com.cache.DataCache;
import com.server.WebSocketServer;

public class UserReaderThread implements Runnable {
	public static TopicReaderLatest<String> userDownTopicReader;
	
	public UserReaderThread() {
		userDownTopicReader=WebSocketServer.client.getTopicReaderLatest("USER_DOWN");
	}
	
	@Override
	public void run() {
		while(true) {
			try {
				String message = userDownTopicReader.read();
				if(message!=null && !message.equals("")) {
					DataCache.messageQueue.add(message);
				}else {
					Thread.sleep(10);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
