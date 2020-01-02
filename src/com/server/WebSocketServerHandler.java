package com.server;

import static io.netty.handler.codec.http.HttpHeaderNames.HOST;
import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.FORBIDDEN;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cache.DataCache;
import com.dao.UserDao;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.util.CharsetUtil;

public class WebSocketServerHandler extends SimpleChannelInboundHandler<Object> {

	public static Logger logger = LoggerFactory.getLogger(WebSocketServerHandler.class); 
	
    private static final String WEBSOCKET_PATH = "/websocket";

    private WebSocketServerHandshaker handshaker;

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest req) {
        // Handle a bad request.
        if (!req.decoderResult().isSuccess()) {
            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST));
            return;
        }

        // Allow only GET methods.
        if (req.method() != GET) {
            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, FORBIDDEN));
            return;
        }

        if ("/favicon.ico".equals(req.uri()) || ("/".equals(req.uri()))) {
            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, NOT_FOUND));
            return;
        }

        // Handshake
        WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(getWebSocketLocation(req), null, true);
        handshaker = wsFactory.newHandshaker(req);
        if (handshaker == null) {
            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
        } else {
            ChannelFuture channelFuture = handshaker.handshake(ctx.channel(), req);

            if (channelFuture.isSuccess()) {

            }
        }
    }

    private void broadcast(ChannelHandlerContext ctx, WebSocketFrame frame) {
        String request = ((TextWebSocketFrame) frame).text();
        System.out.println(" 收到 " + ctx.channel() + request);
        JSONObject allParam = JSON.parseObject(request);
        if(allParam.containsKey("event")) {
        	String event = allParam.getString("event");
        	if("connect".equals(event)) {
        		 allParam.put("mode", 2);
        		connect(ctx, request, allParam);
        	}else if("socketHeart".equals(event)) {
        		allParam.put("mode", 2);
        		ctx.channel().writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(allParam)));
        	}else if("call".equals(event)) {
        		call(ctx, allParam);
        	}else if("record".equals(event)) {
        		try {
					WebSocketServer.padUpTopicWriter.Write(request);
				} catch (Exception e) {
					e.printStackTrace();
				}
        	}
        }else {
        	 ctx.channel().writeAndFlush(new TextWebSocketFrame(request + "这个参数有误"));
        }
    }

	private void call(ChannelHandlerContext ctx, JSONObject allParam) {
		int result =3;
		int mode = allParam.getIntValue("mode");
		if(mode==1) {
			JSONObject param = allParam.getJSONObject("param");
			String padId = param.getString("padId");
			if(DataCache.padMap.containsKey(padId)) {
				String channelId = DataCache.padMap.get(padId);
				if(DataCache.channelMap.containsKey(channelId)) {
					DataCache.channelMap.get(channelId).writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(allParam)));
					return;
				}
			}
			param.remove("audioUrl");
			param.put("result", result);
			ctx.channel().writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(allParam)));
		}else if(mode==2) {
			JSONObject param = allParam.getJSONObject("param");
			String uesrId = param.getString("uesrId");
			if(DataCache.userMap.containsKey(uesrId)) {
				String channelId = DataCache.userMap.get(uesrId);
				if(DataCache.channelMap.containsKey(channelId)) {
					DataCache.channelMap.get(channelId).writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(allParam)));
					return;
				}
			}
			param.put("result", result);
			ctx.channel().writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(allParam)));
		}
	}

	private void connect(ChannelHandlerContext ctx, String request, JSONObject allParam) {
		JSONObject param = allParam.getJSONObject("param");
		Integer type = param.getInteger("type");
		int result=0;
		String info="";
		String userId = param.getString("userId");
		if(userId!=null && type!=null) {
			String channelId = ctx.channel().id().toString();
			if(type==1) {
				UserDao dao = new UserDao();
				int id = Integer.parseInt(userId);
				if(dao.isPad(id)) {
					DataCache.padMap.put(userId, channelId);
					result=1;
				}else {
					logger.info("id不存在");
				}
			}else if(type==2){
				UserDao dao = new UserDao();
				int id = Integer.parseInt(userId);
				if(dao.isUser(id)) {
					DataCache.userMap.put(userId, channelId);
					result=1;
				}else {
					logger.info("id不存在");
				}
			}else {
				logger.info("type不存在");
			}
			if(result==1) {
				DataCache.channelMap.put(channelId, ctx.channel());
			}else {
				info ="参数有误";
			}
		}else {
			info ="参数有误";
		}
		param = new JSONObject();
		param.put("result", result);
		param.put("info", info);
		allParam.put("param", param);
		ctx.channel().writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(allParam)));
		if(result==0) {
			ctx.close();
		}
	}

    private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {

        if (frame instanceof CloseWebSocketFrame) {
            handshaker.close(ctx.channel(), (CloseWebSocketFrame) frame.retain());
            return;
        }
        if (frame instanceof PingWebSocketFrame) {
            ctx.channel().write(new PongWebSocketFrame(frame.content().retain()));
            return;
        }
        if (!(frame instanceof TextWebSocketFrame)) {
            throw new UnsupportedOperationException(String.format("%s frame types not supported", frame.getClass().getName()));
        }

        broadcast(ctx, frame);
    }

    private static void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest req, FullHttpResponse res) {
        if (res.status().code() != 200) {
            ByteBuf buf = Unpooled.copiedBuffer(res.status().toString(), CharsetUtil.UTF_8);
            res.content().writeBytes(buf);
            buf.release();
            HttpHeaders.setContentLength(res, res.content().readableBytes());
        }

        ChannelFuture f = ctx.channel().writeAndFlush(res);
        if (!HttpHeaders.isKeepAlive(req) || res.status().code() != 200) {
            f.addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        Channel incoming = ctx.channel();
        System.out.println("收到" + incoming.remoteAddress() + " 握手请求");
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
//        if (client != null && channelGroupMap.containsKey(client.getRoomId())) {
//            channelGroupMap.get(client.getRoomId()).remove(ctx.channel());
//        }
    }

    private static String getWebSocketLocation(FullHttpRequest req) {
        String location = req.headers().get(HOST) + WEBSOCKET_PATH;
        return "ws://" + location;
    }

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (msg instanceof FullHttpRequest) {
            handleHttpRequest(ctx, (FullHttpRequest) msg);
        } else if (msg instanceof WebSocketFrame) {
            handleWebSocketFrame(ctx, (WebSocketFrame) msg);
        }
		
	}
}
