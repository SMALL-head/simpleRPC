import com.zyc.annotations.ServiceReference;
import com.zyc.utils.ClassUtil;
import org.junit.Test;

import java.util.Set;

public class PackageScanTest {
    @Test
    public void test1() {
        Set<Class<?>> classes = ClassUtil.getClassesWithAnnotation("com.zyc.service", ServiceReference.class);
        System.out.println(classes);
    }
}
