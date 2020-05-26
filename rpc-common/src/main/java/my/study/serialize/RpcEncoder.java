package my.study.serialize;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.codec.MessageToMessageDecoder;
import my.study.serialize.Serializer;

import java.util.List;

public class RpcEncoder extends MessageToByteEncoder {

    private Class<?> clazz;

    private Serializer serializer;


    public RpcEncoder(Class<?> clazz, Serializer serializer) {

        this.clazz = clazz;

        this.serializer = serializer;

    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        if (clazz != null && clazz.isInstance(msg)) {
            byte[] serialize = serializer.serialize(msg);
            out.writeInt(serialize.length);
            out.writeBytes(serialize);
        }

    }
}