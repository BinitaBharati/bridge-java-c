package bharati.binita.bridge.javaToC.jni;

public class NativeMemoryMonitorJNI {

    private static boolean isLoaded = false;

    public NativeMemoryMonitorJNI(String jniLibRef) {
        if (!isLoaded) {
            System.loadLibrary(jniLibRef);
            System.out.println("NativeMemoryMonitorJNI loaded");
            isLoaded = true;
        }
    }

    native public void printNativeJeMallocMemoryUsage();

    native public void printNativeGlibCMallocMemoryUsage();
}
