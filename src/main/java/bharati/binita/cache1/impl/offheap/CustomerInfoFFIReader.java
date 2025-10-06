package bharati.binita.cache1.impl.offheap;

import bharati.binita.cache1.model.CustomerInfo;
import bharati.binita.cache1.util.Util;

import java.lang.foreign.*;
import java.nio.charset.StandardCharsets;

public class CustomerInfoFFIReader {

    public static CustomerInfo fromOffHeap(MemorySegment seg) {
        CustomerInfo info = new CustomerInfo();

        info.setId(seg.get(
                ValueLayout.JAVA_INT,
                CustomerInfoLayout.LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("id"))
        ));

        info.setBalance(seg.get(
                ValueLayout.JAVA_DOUBLE,
                CustomerInfoLayout.LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("balance"))
        ));

        info.setName(readString(seg, "name", Util.MAX_NAME_CHARS));
        info.setLastName(readString(seg, "lastName", Util.MAX_NAME_CHARS));
        info.setHomePhone(readString(seg, "homePhone", Util.MAX_PHONE_CHARS));
        info.setHomeEmail(readString(seg, "homeEmail", Util.MAX_EMAIL_CHARS));

        return info;
    }

    private static String readString(MemorySegment parent, String field, int maxLen) {
        MemorySegment strSeg = parent.asSlice(
                CustomerInfoLayout.LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement(field)),
                maxLen
        );

        // Read bytes until '\0' (null terminator)
        byte[] bytes = new byte[maxLen];
        for (int i = 0; i < maxLen; i++) {
            byte b = strSeg.getAtIndex(ValueLayout.JAVA_BYTE, i);
            if (b == 0) {
                bytes = java.util.Arrays.copyOf(bytes, i);
                break;
            }
            bytes[i] = b;
        }

        return new String(bytes, StandardCharsets.UTF_8);
    }
}
