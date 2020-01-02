package com.thread;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cache.DataCache;

import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.GlobalEventExecutor;

public class MessageThread implements Runnable {
	ThreadPoolExecutor executor = new ThreadPoolExecutor(5, 10, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<>(1000),
			new ThreadPoolExecutor.AbortPolicy());

	@Override
	public void run() {
		while (true) {
			try {
				String message = DataCache.messageQueue.poll();
				if (message != null && !message.equals("")) {
					executor.execute(() -> {
						sendMessage(message);
					});
				} else {
					Thread.sleep(10);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void sendMessage(String message) {
		JSONObject allParam = JSON.parseObject(message);
		String event = allParam.getString("event");
		if ("record".equals(event) || "recordStatus".equals(event)) {
			JSONObject param = allParam.getJSONObject("param");
			String padId = param.getString("padId");
			if (DataCache.padMap.containsKey(padId)) {
				String channelId = DataCache.padMap.get(padId);
				if (DataCache.channelMap.containsKey(channelId)) {
					DataCache.channelMap.get(channelId).writeAndFlush(new TextWebSocketFrame(message));
					return;
				}
			}
		} else if ("streamStatus".equals(event)) {
			ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
			DataCache.userMap.entrySet().forEach(entry -> {
				if (DataCache.channelMap.containsKey(entry.getValue())) {
					channelGroup.add(DataCache.channelMap.get(entry.getValue()));
				}
			});
			if (!channelGroup.isEmpty()) {
				channelGroup.writeAndFlush(new TextWebSocketFrame(message));
			}
		}

	}

}
