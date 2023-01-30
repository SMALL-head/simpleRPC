package rpc.protocal;

import com.zyc.constants.Constants;
import com.zyc.entity.RpcRegisterRequest;
import com.zyc.entity.RpcResponse;
import com.zyc.enums.ProtocolTypeEnum;
import com.zyc.rpc.registry.protocol.Protocol;
import com.zyc.utils.ByteUtils;
import com.zyc.utils.Hessian2Utils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.FixedLengthFrameDecoder;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.Arrays;

public class ProtocolTest {
    @Test
    public void test_netty_embeddedChannel() {
        new EmbeddedChannel()
            .pipeline()
            .addLast(new FixedLengthFrameDecoder(12));
    }

    @Test
    public void test_protocol() throws Exception {
        ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer();
        int magic = 0x123;
        byte value = ProtocolTypeEnum.REGISTRY_SERVICE.getByteValue();
        byte version = (byte) 1;
        short size = 111; // todo修改
        // 内容序列化方式为json
        Method declaredMethod = RpcResponse.class.getDeclaredMethods()[1];
        System.out.println(declaredMethod.getName());
        System.out.println(Arrays.toString(declaredMethod.getExceptionTypes()));
        System.out.println(declaredMethod.getReturnType());
        System.out.println(declaredMethod.getDeclaringClass());

        ByteBuf byteBuf = ByteBufAllocator.DEFAULT.buffer();

        System.out.println("byteBuf.capacity() = " + byteBuf.readableBytes());
        RpcRegisterRequest rpcRegisterRequest = new RpcRegisterRequest(Constants.LOCALHOST, 100023, declaredMethod.getName(), declaredMethod.getParameterTypes(), declaredMethod.getReturnType());

        byte[] serialize = Hessian2Utils.serialize(rpcRegisterRequest);
        System.out.println("serialize = " + serialize.length);
        size = (short) (8 + serialize.length);
        byteBuf.writeInt(magic).writeByte(value).writeByte(version).writeShort(size);
        byteBuf.writeBytes(serialize);


        byte[] bytesFromUtils = Protocol.generateProtocol(rpcRegisterRequest);
        System.out.println("bytesFromUtils.length = " + bytesFromUtils.length);
        System.out.println("byteBuf.readableBytes() = " + byteBuf.readableBytes());

        byte[] byteBuf2 = new byte[byteBuf.readableBytes()];
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
