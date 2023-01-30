package rpc.protocal;

import com.zyc.constants.Constants;
import com.zyc.entity.RpcRegisterRequest;
import com.zyc.entity.RpcResponse;
import com.zyc.enums.ProtocolTypeEnum;
import com.zyc.netty.ByteToRpcRegisterRequestDecoder;
import com.zyc.netty.RpcRegisterRequestToByteEncoder;
import com.zyc.rpc.registry.protocol.Protocol;
import com.zyc.utils.ByteUtils;
import com.zyc.utils.Hessian2Utils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.Arrays;

public class ProtocolTest {

    /**
     * 协议编解码器+chanel测试
     */
    @Test
    public void test_netty_embeddedChannel() {
        EmbeddedChannel embeddedChannel = new EmbeddedChannel();
        embeddedChannel
            .pipeline()
            .addLast(new RpcRegisterRequestToByteEncoder())
            .addLast(new ByteToRpcRegisterRequestDecoder())
            .addLast(new ChannelInboundHandlerAdapter() {
                @Override
                public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                    RpcRegisterRequest request = (RpcRegisterRequest) msg;
                    System.out.println(request);
                }
            });
        Method declaredMethod = RpcResponse.class.getDeclaredMethods()[1];
        RpcRegisterRequest rpcRegisterRequest = new RpcRegisterRequest(Constants.LOCALHOST, 100023, declaredMethod.getName(), declaredMethod.getParameterTypes(), declaredMethod.getReturnType());
        embeddedChannel.writeInbound(rpcRegisterRequest);

    }

    /**
     * 乱七八糟的协议测试
     * @throws Exception
     */
    @Test
    public void test_protocol() throws Exception {
        ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer();
        int magic = 0x123;
        byte value = ProtocolTypeEnum.REGISTRY_SERVICE.getByteValue();
        byte version = (byte) 1;

        Method declaredMethod = RpcResponse.class.getDeclaredMethods()[1];
        System.out.println(declaredMethod.getName());
        System.out.println(Arrays.toString(declaredMethod.getExceptionTypes()));
        System.out.println(declaredMethod.getReturnType());
        System.out.println(declaredMethod.getDeclaringClass());

        ByteBuf byteBuf = ByteBufAllocator.DEFAULT.buffer();

        System.out.println("byteBuf.capacity() = " + byteBuf.readableBytes());
        RpcRegisterRequest rpcRegisterRequest = new RpcRegisterRequest(Constants.LOCALHOST, 100023, declaredMethod.getName(), declaredMethod.getParameterTypes(), declaredMethod.getReturnType());

        // 测试hessian2序列化
        byte[] serialize = Hessian2Utils.serialize(rpcRegisterRequest);
        System.out.println("serialize = " + serialize.length);
        short size = (short) (8 + serialize.length);
        byteBuf.writeInt(magic).writeByte(value).writeByte(version).writeShort(size);
        byteBuf.writeBytes(serialize);

        // 验证协议生成和解析
        byte[] bytesFromUtils = Protocol.generateProtocol(rpcRegisterRequest);
        System.out.println("bytesFromUtils.length = " + bytesFromUtils.length);
        System.out.println("byteBuf.readableBytes() = " + byteBuf.readableBytes());

//        byte[] byteBuf2 = new byte[byteBuf.readableBytes()];
//        byteBuf.readBytes(byteBuf2);
//        for (int i = 0; i < byteBuf2.length; ++i) {
//            if (byteBuf2[i] != bytesFromUtils[i]) {
//                System.out.println(i);
//            }
//        }

        RpcRegisterRequest rpcRegisterRequest1 = Protocol.parseProtocol(byteBuf);
        System.out.println(rpcRegisterRequest1);

    }
    @Test
    public void test() {
        System.out.println("ByteUtils.int2byteArray() = " + Arrays.toString(ByteUtils.int2byteArray(0x1234)));
    }
}
