import com.zyc.entity.RpcResponse;
import org.junit.Test;

public class EnumTest {
    @Test
    public void testRpcResponse() {
        RpcResponse rpcResponse = new RpcResponse();
        rpcResponse.setResponse("abc");
        rpcResponse.setResponseType(String.class);


    }
}
