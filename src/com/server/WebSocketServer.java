package com.server;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.anbot.server.kafkatool.KafkaClient;
import com.anbot.server.kafkatool.TopicWriter;
import com.anbot.server.kafkatool.exception.InitialException;
import com.thread.MessageThread;
import com.thread.PadReaderThread;
import com.thread.UserReaderThread;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class WebSocketServer {
	public static KafkaClient client;
	//获得写数据到topic的对象
	public static TopicWriter<String> padUpTopicWriter;



	public static void main(String[] args) throws InitialException {
		final int PORT = 8083;
		
		client = new KafkaClient("AnbotGW01");
		padUpTopicWriter = client.getTopicWriter("PAD_UP");

		 ExecutorService es = Executors.newFixedThreadPool(3);  
		 es.submit(new MessageThread());
		 es.submit(new PadReaderThread());
		 es.submit(new UserReaderThread());
		 es.submit(new MessageThread());
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new WebSocketServerInitializer());

            Channel ch = b.bind(PORT).sync().channel();
            ch.closeFuture().sync();
        } catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }

	}

}
