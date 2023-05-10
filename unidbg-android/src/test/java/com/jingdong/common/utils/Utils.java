package com.jingdong.common.utils;

import com.github.unidbg.AndroidEmulator;
import com.github.unidbg.Module;
import com.github.unidbg.linux.android.AndroidEmulatorBuilder;
import com.github.unidbg.linux.android.AndroidResolver;
import com.github.unidbg.linux.android.dvm.*;
import com.github.unidbg.linux.android.dvm.api.ApplicationInfo;
import com.github.unidbg.linux.android.dvm.array.ByteArray;
import com.github.unidbg.memory.Memory;
import com.github.unidbg.virtualmodule.android.AndroidModule;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import sun.security.pkcs.PKCS7;
import sun.security.pkcs.ParsingException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class Utils extends AbstractJni {
    private static final Log log = LogFactory.getLog(Utils.class);
    private final AndroidEmulator emulator;
    private final VM vm;
    private final Module module;
    private DvmClass cNative;
    private PKCS7  pkcs7;

    public Utils(){
        emulator = AndroidEmulatorBuilder.for32Bit().build(); // 创建模拟器实例，要模拟32位或者64位，在这里区分
        final Memory memory = emulator.getMemory(); // 模拟器的内存操作接口
        memory.setLibraryResolver(new AndroidResolver(23)); // 设置系统类库解析
        vm = emulator.createDalvikVM(new File("C:\\Users\\Davide\\Downloads\\unidbg-0.9.7\\unidbg-0.9.7\\unidbg-android\\src\\test\\resources\\jdd_so\\jdd_.apk"));

        // 注册 libandroid.so
        new AndroidModule(emulator, vm).register(memory);
        DalvikModule dm = vm.loadLibrary(new File("C:\\\\Users\\\\Davide\\\\Downloads\\\\unidbg-0.9.7\\\\unidbg-0.9.7\\\\unidbg-android\\\\src\\\\test\\\\resources\\\\jdd_so\\\\libjdbitmapkit.so"), true);
        module = dm.getModule();

        vm.setJni(this);
        vm.setVerbose(true);

        cNative = vm.resolveClass("com/jingdong/common/utils/BitmapkitUtils");

        dm.callJNI_OnLoad(emulator);

    }
    // public static byte[] unZip(String str, String str2, String str3) {
    //     ZipFile zipFile = null;
    //     ZipFile zipFile2 = null;
    //     try {
    //     } catch (Throwable th) {
    //         th = th;
    //     }
    //     try {
    //         try {
    //             zipFile = new ZipFile(new File(str));
    //         } catch (IOException e) {
    //             log.error( e);
    //         }
    //         try {
    //             Enumeration<? extends ZipEntry> entries = zipFile.entries();
    //             while (entries.hasMoreElements()) {
    //                 ZipEntry nextElement = entries.nextElement();
    //                 if (!nextElement.isDirectory() && (str2 == null || str2.length() <= 0 || nextElement.getName().startsWith(str2))) {
    //                     InputStream inputStream = zipFile.getInputStream(nextElement);
    //                     int size = (int) nextElement.getSize();
    //                     byte[] bArr = new byte[size];
    //                     if (inputStream.read(bArr) != size) {
    //                         try {
    //                             zipFile.close();
    //                         } catch (IOException e2) {
    //                             log.error( e2);
    //                         }
    //                         return null;
    //                     }
    //                     try {
    //                         zipFile.close();
    //                     } catch (IOException e3) {
    //                         log.error( e3);
    //                     }
    //                     return bArr;
    //                 }
    //             }
    //             zipFile.close();
    //         } catch (Exception e4) {
    //             log.error( e4);
    //             if (zipFile != null) {
    //                 zipFile.close();
    //             }
    //             return null;
    //         }
    //     } catch (Exception e5) {
    //         log.error( e5);
    //         zipFile = null;
    //     } catch (Throwable th2) {
    //
    //             try {
    //                 zipFile2.close();
    //             } catch (IOException e6) {
    //                 log.error( e6);;
    //
    //         }
    //         throw th2;
    //     }
    //     return null;
    // }

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
    public DvmObject<?> callStaticObjectMethod(BaseVM vm, DvmClass dvmClass, String signature, VarArg varArg) {
        if ("com/jingdong/common/utils/BitmapkitZip->unZip(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)[B".equals(signature)){
            byte[] data = vm.unzip("META-INF/JINGDONG.RSA");
            return new ByteArray(vm, data);
        }

        if ("com/jingdong/common/utils/BitmapkitZip->objectToBytes(Ljava/lang/Object;)[B".equals(signature)){
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
    public DvmObject<?> getStaticObjectField(BaseVM vm, DvmClass dvmClass, String signature) {
        switch (signature) {
            case "com/jingdong/common/utils/BitmapkitUtils->a:Landroid/app/Application;":
                return vm.resolveClass("android/app/Activity",
                                vm.resolveClass("android/content/pm/PackageManager")).newObject(null);
            default:
                return super.getStaticObjectField(vm, dvmClass, signature);
        }
    }

    // @Override
    // public DvmObject<?> callStaticObjectMethod(BaseVM vm, DvmClass dvmClass, String signature, VarArg varArg) {
    //     if ("com/jingdong/common/utils/BitmapkitZip->unZip(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)[B".equals(signature)) {
    //         return unZip();
    //     }
    //     return super.callStaticObjectMethod(vm, dvmClass, signature, varArg);
    // }

    @Override
    public DvmObject<?> callObjectMethod(BaseVM vm, DvmObject<?> dvmObject, String signature, VarArg varArg) {
        switch (signature) {
            case "android/app/Application->getApplicationInfo()Landroid/content/pm/ApplicationInfo;":
                return new ApplicationInfo(vm);
            default:
                return super.callObjectMethod(vm, dvmObject, signature, varArg);
        }
    }

//    getStaticObjectField

    public static void main(String[] args) {
//        Logger.getLogger("com.github.unidbg.linux.ARM32SyscallHandler").setLevel(Level.DEBUG);
//        Logger.getLogger("com.github.unidbg.unix.UnixSyscallHandler").setLevel(Level.DEBUG);
//        Logger.getLogger("com.github.unidbg.AbstractEmulator").setLevel(Level.DEBUG);
//        Logger.getLogger("com.github.unidbg.linux.android.dvm.DalvikVM").setLevel(Level.DEBUG);
//        Logger.getLogger("com.github.unidbg.linux.android.dvm.BaseVM").setLevel(Level.DEBUG);
//        Logger.getLogger("com.github.unidbg.linux.android.dvm").setLevel(Level.DEBUG);
        Utils u = new Utils();
    }
}


