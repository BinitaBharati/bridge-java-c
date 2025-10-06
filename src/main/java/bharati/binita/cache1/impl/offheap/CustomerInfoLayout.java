package bharati.binita.cache1.impl.offheap;

import bharati.binita.cache1.util.Util;

import java.lang.foreign.*;
import java.lang.invoke.VarHandle;
import java.nio.charset.StandardCharsets;

public class CustomerInfoLayout {

    public static final GroupLayout LAYOUT = MemoryLayout.structLayout(
            ValueLayout.JAVA_INT.withName("id"),
            MemoryLayout.sequenceLayout(Util.MAX_NAME_CHARS, ValueLayout.JAVA_BYTE).withName("name"),
            MemoryLayout.sequenceLayout(Util.MAX_NAME_CHARS, ValueLayout.JAVA_BYTE).withName("lastName"),
            MemoryLayout.sequenceLayout(Util.MAX_PHONE_CHARS, ValueLayout.JAVA_BYTE).withName("homePhone"),
            MemoryLayout.sequenceLayout(Util.MAX_EMAIL_CHARS, ValueLayout.JAVA_BYTE).withName("homeEmail"),
            ValueLayout.JAVA_DOUBLE.withName("balance")
    );
}
