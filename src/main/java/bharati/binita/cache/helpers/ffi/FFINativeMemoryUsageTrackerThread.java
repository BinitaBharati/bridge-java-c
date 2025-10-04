package bharati.binita.cache.helpers.ffi;

public class FFINativeMemoryUsageTrackerThread implements Runnable {

    private FFINativeMemoryMonitor nativeMemoryMonitorFFI;
    private boolean isJeMallocEnabled;

    public FFINativeMemoryUsageTrackerThread(FFINativeMemoryMonitor nativeHashMapFFI, boolean isJeMallocEnabled) {
        this.nativeMemoryMonitorFFI = nativeHashMapFFI;
        this.isJeMallocEnabled = isJeMallocEnabled;
    }

    @Override
    public void run() {
        while (true) {
            try {
                    System.out.println("PRINTING NATIVE MEM USAGE - STARTS");
                    if (isJeMallocEnabled) {
                        FFINativeMemoryMonitor.printNativeJeMallocMemoryUsage();
                    }
                    else {
                        FFINativeMemoryMonitor.printNativeGlibCMallocMemoryUsage();
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
