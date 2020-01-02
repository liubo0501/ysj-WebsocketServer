package com.thread;

import com.anbot.server.kafkatool.TopicReaderLatest;
import com.cache.DataCache;
import com.server.WebSocketServer;

public class PadReaderThread implements Runnable {
	public static TopicReaderLatest<String> padDownTopicReader;

	public PadReaderThread() {
		padDownTopicReader = WebSocketServer.client.getTopicReaderLatest("PAD_DOWN");
	}

	@Override
	public void run() {
		while (true) {
			try {
				String message = padDownTopicReader.read();
				if (message != null && !message.equals("")) {
					DataCache.messageQueue.add(message);
				} else {
					Thread.sleep(10);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

}
