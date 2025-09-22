package bharati.binita.bridge.javaToC.ffi;

public class NativeMemoryUsageTrackerThread implements Runnable {

    private NativeMemoryMonitorFFI nativeMemoryMonitorFFI;
    private boolean isJeMallocEnabled;

    public NativeMemoryUsageTrackerThread(NativeMemoryMonitorFFI nativeHashMapFFI, boolean isJeMallocEnabled) {
        this.nativeMemoryMonitorFFI = nativeHashMapFFI;
        this.isJeMallocEnabled = isJeMallocEnabled;
    }

    @Override
    public void run() {
        while (true) {
            try {
                    System.out.println("PRINTING NATIVE MEM USAGE - STARTS");
                    if (isJeMallocEnabled) {
                        NativeMemoryMonitorFFI.printNativeJeMallocMemoryUsage();
                    }
                    else {
                        NativeMemoryMonitorFFI.printNativeGlibCMallocMemoryUsage();
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
