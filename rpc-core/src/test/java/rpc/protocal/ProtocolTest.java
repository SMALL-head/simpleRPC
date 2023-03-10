package rpc.protocal;

import com.zyc.constants.Constants;
import com.zyc.entity.registry.RpcRegisterRequestData;
import com.zyc.entity.registry.RpcRegistryRequest;
import com.zyc.entity.rpc.GenericReturn;
import com.zyc.enums.ProtocolTypeEnum;
import com.zyc.netty.registry.ByteToRpcRegistryRequestDecoder;
import com.zyc.netty.registry.RpcRegistryRequestToByteEncoder;
import com.zyc.protocol.Protocol;
import com.zyc.utils.ByteUtils;
import com.zyc.utils.Hessian2Utils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.Test;
import rpc.serviceDemo.ServiceDemoImpl;

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
            .addLast(new RpcRegistryRequestToByteEncoder())
            .addLast(new ByteToRpcRegistryRequestDecoder())
            .addLast(new ChannelInboundHandlerAdapter() {
                @Override
                public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                    RpcRegistryRequest request = (RpcRegistryRequest) msg;
                    System.out.println(request);
                }
            });
        RpcRegisterRequestData rpcRegisterRequestData = new RpcRegisterRequestData(Constants.LOCALHOST, 100023, ServiceDemoImpl.class.getCanonicalName()  );
        RpcRegistryRequest request = new RpcRegistryRequest(rpcRegisterRequestData, Constants.PROTOCOL_VERSION, ProtocolTypeEnum.REGISTRY_SERVICE);
        embeddedChannel.writeInbound(request);

    }

    /**
     * 乱七八糟的协议测试
     */
    @Test
    public void test_protocol() throws Exception {
        ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer();
        int magic = Constants.MAGIC_NUMBER;
        byte value = ProtocolTypeEnum.REGISTRY_SERVICE.getByteValue();
        byte version = Constants.PROTOCOL_VERSION;

        Method declaredMethod = GenericReturn.class.getDeclaredMethods()[1];
        System.out.println(declaredMethod.getName());
        System.out.println(Arrays.toString(declaredMethod.getExceptionTypes()));
        System.out.println(declaredMethod.getReturnType());
        System.out.println(declaredMethod.getDeclaringClass());

        ByteBuf byteBuf = ByteBufAllocator.DEFAULT.buffer();

        System.out.println("byteBuf.capacity() = " + byteBuf.readableBytes());
        RpcRegisterRequestData rpcRegisterRequestData = new RpcRegisterRequestData(Constants.LOCALHOST, 100023, ServiceDemoImpl.class.getCanonicalName());

        // 测试hessian2序列化
        byte[] serialize = Hessian2Utils.serialize(rpcRegisterRequestData);
        System.out.println("serialize = " + serialize.length);
        short size = (short) (serialize.length);
        byteBuf.writeInt(magic).writeByte(value).writeByte(version).writeShort(size);
        byteBuf.writeBytes(serialize);


    }
    @Test
    public void test() {
        System.out.println("ByteUtils.int2byteArray() = " + Arrays.toString(ByteUtils.int2byteArray(0x1234)));
    }
}
