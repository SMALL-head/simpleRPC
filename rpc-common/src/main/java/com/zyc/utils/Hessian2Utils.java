package com.zyc.utils;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * hessian序列化工具类
 */
public class Hessian2Utils {
    public static <T> byte[] serialize(T javaBean) throws Exception {
        Hessian2Output ho = null;
        ByteArrayOutputStream baos = null;

        try {
            baos = new ByteArrayOutputStream();
            ho = new Hessian2Output(baos);
            ho.writeObject(javaBean);
            ho.flush();
            return baos.toByteArray();
        } catch (Exception ex) {
            System.out.println("[模拟日志记录]HessianUtils.serialize.异常." + ex.getMessage());
            throw new Exception("HessianUtils.serialize.异常.", ex);
        } finally {
            if (null != ho) {
                ho.close();
            }
        }
    }

    public static Object deserialize(byte[] serializeData) throws Exception {
        Object javaBean = null;
        Hessian2Input hi = null;
        ByteArrayInputStream bais = null;

        try {
            bais = new ByteArrayInputStream(serializeData);
            hi = new Hessian2Input(bais);

            javaBean = hi.readObject();
            return javaBean;
        } catch (Exception ex) {
            System.out.println("[模拟日志记录]HessianUtils.deserialize.异常." + ex.getMessage());
            throw new Exception("HessianUtils.deserialize.异常.", ex);
        } finally {
            if (null != hi) {
                hi.close();
            }
        }
    }

}
