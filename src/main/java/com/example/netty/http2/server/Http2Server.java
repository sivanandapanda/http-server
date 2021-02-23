package com.example.netty.http2.server;

import com.example.netty.http2.Http2Util;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;

/**
 * curl -k -v --http2 https://127.0.0.1:8443
 */
public final class Http2Server {

    private static final int PORT = 8443;

    public static void main(String[] args) throws Exception {
        SslContext sslCtx = Http2Util.createSSLContext(true);

        EventLoopGroup group = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.option(ChannelOption.SO_BACKLOG, 1024);
            b.group(group)
                .channel(NioServerSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        if (sslCtx != null) {
                            ch.pipeline()
                                .addLast(sslCtx.newHandler(ch.alloc()), Http2Util.getServerAPNHandler());
                        }
                    }

                });

            Channel ch = b.bind(PORT)
                .sync()
                .channel();

            System.out.println("HTTP/2 Server is listening on https://127.0.0.1:" + PORT + '/');

            ch.closeFuture()
                .sync();
        } finally {
            group.shutdownGracefully();
        }
    }

}