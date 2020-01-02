package com.test;

import com.anbot.server.kafkatool.KafkaClient;
import com.anbot.server.kafkatool.TopicReader;
import com.anbot.server.kafkatool.exception.InitialException;

public class TestReader {

	public static void main(String[] args) throws InitialException {
		//根据程序编号生成kafka客户端对象
		KafkaClient client = new KafkaClient("111");
		//获得从topic读数据的对象
//				TopicReader<String> topicReader= client.getTopicReader("PAD_UP");
				TopicReader<String> topicReader= client.getTopicReader("PAD_UP");
				for (int j = 0; j < 10; j++){
					//从Topic读取数据,若无数据，一直阻塞等待数据
					String json = topicReader.read();
					System.out.println(System.currentTimeMillis()+":"+json);
				}
				//关闭读数据对象
				topicReader.close();
	}
}
