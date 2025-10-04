package bharati.binita.cache.helpers.ffi;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.SymbolLookup;
import java.lang.invoke.MethodHandle;

public class FFINativeMemoryMonitor {

    private static final Linker linker = Linker.nativeLinker();
    private static final SymbolLookup lookup = SymbolLookup.loaderLookup();
    private static boolean isLoaded = false;

    private static MethodHandle printJeMallocNativeMemoryUsage;
    private static MethodHandle printGlibCMallocNativeMemoryUsage;

    public FFINativeMemoryMonitor(String jniLibRef) {
        if (!isLoaded) {
            System.loadLibrary(jniLibRef);

            try {
                printJeMallocNativeMemoryUsage = linker.downcallHandle(
                        lookup.find("print_jemalloc_stats").orElseThrow(),
                        FunctionDescriptor.ofVoid()

                );

                printGlibCMallocNativeMemoryUsage = linker.downcallHandle(
                        lookup.find("print_glibc_malloc_stats2").orElseThrow(),
                        FunctionDescriptor.ofVoid()

                );
            }
            catch (Throwable e) {
                throw new RuntimeException(e);
            }

        }
    }

    public static void printNativeJeMallocMemoryUsage() throws Throwable{
        printJeMallocNativeMemoryUsage.invoke();
    }

    public static void printNativeGlibCMallocMemoryUsage() throws Throwable{
        printGlibCMallocNativeMemoryUsage.invoke();
    }
}
