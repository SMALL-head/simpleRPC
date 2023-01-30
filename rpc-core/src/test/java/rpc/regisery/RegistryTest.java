package rpc.regisery;

import com.zyc.constants.Constants;
import com.zyc.rpc.registry.ServiceRegistryCenter;
import org.junit.Test;

public class RegistryTest {
    @Test
    public void centerTest() {
        ServiceRegistryCenter serviceRegistryCenter = new ServiceRegistryCenter(Constants.LOCALHOST, 8088);
        serviceRegistryCenter.serverStart();

        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
