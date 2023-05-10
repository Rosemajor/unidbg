package com.jingdong.common.utils;

import com.github.unidbg.AndroidEmulator;
import com.github.unidbg.Module;
import com.github.unidbg.linux.android.AndroidEmulatorBuilder;
import com.github.unidbg.linux.android.AndroidResolver;
import com.github.unidbg.linux.android.dvm.*;
import com.github.unidbg.linux.android.dvm.array.ArrayObject;
import com.github.unidbg.linux.android.dvm.array.ByteArray;
import com.github.unidbg.memory.Memory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import sun.security.pkcs.PKCS7;
import sun.security.pkcs.ParsingException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.security.cert.X509Certificate;

// 可用的

public class BitmapkitUtils extends AbstractJni {
    private static final Log log = LogFactory.getLog(Utils.class);
    private final AndroidEmulator emulator;
    private final VM vm;
    private final Memory memory;
    private final Module module;
    private final DvmClass obj;
    private PKCS7 pkcs7;
    private final DvmObject<?> context;
    private DvmClass dvmClass;

    public BitmapkitUtils() {
        emulator = AndroidEmulatorBuilder
                .for32Bit()
                //.setRootDir(new File("target/rootfs/default"))
                //.addBackendFactory(new DynarmicFactory(true))
                .build();// 创建模拟器实例，要模拟32位或者64位，在这里区分

        memory = emulator.getMemory(); // 模拟器的内存操作接口
        memory.setLibraryResolver(new AndroidResolver(23));// 设置系统类库解析
        vm = emulator.createDalvikVM(new File("unidbg-android/src/test/resources/jdd_so/jdd_A.apk"));
        vm.setVerbose(true);
        vm.setJni(this);

        DalvikModule dalvikModule = vm.loadLibrary(new File("unidbg-android/src/test/resources/jdd_so/libjdbitmapkit.so"), false);
        module = dalvikModule.getModule();

        vm.callJNI_OnLoad(emulator, module);
        context = vm.resolveClass("android/content/Context").newObject(null);
        // DvmClass dvmClass = vm.resolveClass("android/app/Application");
        // vm.resolveClass("android/app/Activity", dvmClass);


        dvmClass = vm.resolveClass("android/content/Context");
        obj = vm.resolveClass("com/jingdong/common/utils/BitmapkitUtils");//获取类在调用
    }


    public static void main(String[] args) {
        BitmapkitUtils mainActivity = new BitmapkitUtils();
        mainActivity.getSignFromJni();
    }

    private void getSignFromJni() {
        StringObject str = new StringObject(vm, "search");
        StringObject str2 = new StringObject(vm, "11111111111111111111111111111111");
        StringObject str3 = new StringObject(vm, "2222222222222222222");
        StringObject str4 = new StringObject(vm, "android");
        StringObject str5 = new StringObject(vm, "10.2.0");
        DvmObject<?> dvmObject = obj.callStaticJniMethodObject(emulator, "getSignFromJni(Landroid/content/Context;Ljava/lang/String;Ljava/lang/String;" +
                        "Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;",
                dvmClass, str, str2,
                str3, str4, str5);

        String toString = dvmObject.getValue().toString();

        System.out.println(toString);


    }


    @Override
    public DvmObject<?> getStaticObjectField(BaseVM vm, DvmClass dvmClass, String signature) {
        if ("com/jingdong/common/utils/BitmapkitUtils->a:Landroid/app/Application;".equals(signature)) {
            DvmClass dvmClass1 = vm.resolveClass("android/app/Activity");
            return vm.resolveClass("android/app/Application", dvmClass1).newObject(null);

        }

        return super.getStaticObjectField(vm, dvmClass, signature);
    }

    @Override
    public DvmObject<?> callObjectMethod(BaseVM vm, DvmObject<?> dvmObject, String signature, VarArg varArg) {
        if ("android/app/Application->getApplicationInfo()Landroid/content/pm/ApplicationInfo;".equals(signature)) {
            return vm.resolveClass("content/pm/ApplicationInfo").newObject(null);
        }
        if ("sun/security/pkcs/PKCS7->getCertificates()[Ljava/security/cert/X509Certificate;".equals(signature)) {
            try {

                PKCS7 pkcs7 = (PKCS7) dvmObject.getValue();
                X509Certificate[] certificates = pkcs7.getCertificates();
                return new ArrayObject(vm.resolveClass("java/security/cert/X509Certificate").newObject(certificates[0]));
            } catch (Exception e) {
                log.error("pkcs7");
                return null;
            }
        }
        return super.callObjectMethod(vm, dvmObject, signature, varArg);
    }

    @Override
    public DvmObject<?> getObjectField(BaseVM vm, DvmObject<?> dvmObject, String signature) {
        if ("content/pm/ApplicationInfo->sourceDir:Ljava/lang/String;".equals(signature)) {
            return new StringObject(vm, "/data/app/com.base.apk");
        }
        return super.getObjectField(vm, dvmObject, signature);
    }

    @Override
    public DvmObject<?> callStaticObjectMethod(BaseVM vm, DvmClass dvmClass, String signature, VarArg varArg) {
        if ("com/jingdong/common/utils/BitmapkitZip->unZip(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)[B".equals(signature)) {
            byte[] data = vm.unzip("META-INF/JINGDONG.RSA");
            return new ByteArray(vm, data);
        }

        if ("com/jingdong/common/utils/BitmapkitZip->objectToBytes(Ljava/lang/Object;)[B".equals(signature)) {
            System.out.println("1111111111111111111111");
            DvmObject<?> objectArg = varArg.getObjectArg(0);
            byte[] bytes = objectToBytes(objectArg.getValue());
            return new ByteArray(vm, bytes);
        }
        return super.callStaticObjectMethod(vm, dvmClass, signature, varArg);
    }

    private static byte[] objectToBytes(Object obj) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(obj);
            oos.flush();
            byte[] array = baos.toByteArray();
            oos.close();
            baos.close();
            return array;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }


    @Override
    public DvmObject<?> newObject(BaseVM vm, DvmClass dvmClass, String signature, VarArg varArg) {
        if ("sun/security/pkcs/PKCS7-><init>([B)V".equals(signature)) {
            try {
                pkcs7 = new PKCS7((byte[]) varArg.getObjectArg(0).getValue());
            } catch (ParsingException e) {
                e.printStackTrace();
            }
            return vm.resolveClass("sun/security/pkcs/PKCS7").newObject(pkcs7);
        }
        return super.newObject(vm, dvmClass, signature, varArg);
    }


    @Override
    public DvmObject<?> newObjectV(BaseVM vm, DvmClass dvmClass, String signature, VaList vaList) {

        if ("java/lang/StringBuffer-><init>()V".equals(signature)) {
            return vm.resolveClass("java/lang/StringBuffer").newObject(new StringBuffer());

        }
        if ("java/lang/Integer-><init>(I)V".equals(signature)) {
            Integer integer = vaList.getIntArg(0);
            return vm.resolveClass("java/lang/Integer").newObject(integer);
        }
        return super.newObjectV(vm, dvmClass, signature, vaList);
    }

    @Override
    public DvmObject<?> callObjectMethodV(BaseVM vm, DvmObject<?> dvmObject, String signature, VaList vaList) {

        if ("java/lang/StringBuffer->append(Ljava/lang/String;)Ljava/lang/StringBuffer;".equals(signature)) {
            StringBuffer stringBuffer = (StringBuffer) dvmObject.getValue();
            String str = (String) vaList.getObjectArg(0).getValue();
            return vm.resolveClass("java/lang/StringBuffer").newObject(stringBuffer.append(str));
        }
        if ("java/lang/Integer->toString()Ljava/lang/String;".equals(signature)) {
            System.out.println("1111111111111111111111");

            Integer integer = (Integer) dvmObject.getValue();
            System.out.println("222222222222:" + integer);
            String toString = integer.toString();

            return new StringObject(vm, toString);
        }
        if ("java/lang/StringBuffer->toString()Ljava/lang/String;".equals(signature)) {
            StringBuffer stringBuffer = (StringBuffer) dvmObject.getValue();

            return new StringObject(vm, stringBuffer.toString());

        }
        return super.callObjectMethodV(vm, dvmObject, signature, vaList);
    }
}


