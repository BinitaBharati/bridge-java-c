package bharati.binita.cache.helpers.jni;

public class NativeMemoryUsageTrackerThread implements Runnable{

    private boolean isJeMallocEnabled;
    private NativeMemoryMonitorJNI nativeMemoryMonitorJNI;

    public NativeMemoryUsageTrackerThread(NativeMemoryMonitorJNI nativeMemoryMonitorJNI, boolean isJeMallocEnabled) {
        this.nativeMemoryMonitorJNI = nativeMemoryMonitorJNI;
        this.isJeMallocEnabled = isJeMallocEnabled;
    }

    @Override
    public void run() {
        while (true) {
            try {
                System.out.println("PRINTING NATIVE MEM USAGE - STARTS");
                if (isJeMallocEnabled) {
                    nativeMemoryMonitorJNI.printNativeJeMallocMemoryUsage();
                }
                else {
                    nativeMemoryMonitorJNI.printNativeGlibCMallocMemoryUsage();
                }
                System.out.println("PRINTING NATIVE MEM USAGE - ENDS");
                Thread.sleep(1 * 60 * 1000);

            }
            catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }
}
