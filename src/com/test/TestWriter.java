package com.test;

import com.anbot.server.kafkatool.KafkaClient;
import com.anbot.server.kafkatool.TopicWriter;
import com.anbot.server.kafkatool.exception.InitialException;

public class TestWriter {

	public static void main(String[] args) throws InitialException {
		//根据程序编号生成kafka客户端对象
				KafkaClient client = new KafkaClient("111");
				//获得写数据到topic的对象
//				TopicWriter<String> topicWriter = client.getTopicWriter("PAD_DOWN");
				TopicWriter<String> topicWriter = client.getTopicWriter("PAD_UP");
				for (int j = 0; j < 10; j++){
//					String record = "{\r\n" + 
//							"	\"event\": \"record\",\r\n" + 
//							"	\"token\": \"xxxx\",\r\n" + 
//							"\"mode\":2,\r\n" + 
//							"	\"param\": {\r\n" + 
//							"\"type\":1,\r\n" + 
//							"\"padId\": 123,\r\n" + 
//							"\"result\":1,\r\n" + 
//							"\"info\":\"xxxxxx\"/\r\n" + 
//							"	}\r\n" + 
//							"}";
//					String record = "{\r\n" + 
//							"	\"event\": \"recordStatus\",\r\n" + 
//							"	\"token\": \"xxxx\",\r\n" + 
//							"\"mode\":1,\r\n" + 
//							"	\"param\": {\r\n" + 
//							"\"padId\": 123,\r\n" + 
//							"\"status\":1\r\n" + 
//							"	}\r\n" + 
//							"}";
					String record = "{\r\n" + 
							"	\"event\": \"streamStatus\",\r\n" + 
							"	\"token\": \"xxxx\",\r\n" + 
							"\"mode\":1,\r\n" + 
							"	\"param\": {\r\n" + 
							"\"padId\": 123,\r\n" + 
							"\"status\":1\r\n" + 
							"	}\r\n" + 
							"}";
					System.out.println(System.currentTimeMillis()+":"+record);
					try {
						//写入数据到Topic
						topicWriter.Write(record);
					} catch (Exception e1) {
						e1.printStackTrace();
						topicWriter.close();
						topicWriter = client.getTopicWriter("PAD_UP");
					}
				}
				//关闭写数据对象
				topicWriter.close();
	}

}
