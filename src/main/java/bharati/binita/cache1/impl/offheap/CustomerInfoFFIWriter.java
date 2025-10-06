package bharati.binita.cache1.impl.offheap;

import bharati.binita.cache1.model.CustomerInfo;
import bharati.binita.cache1.util.Util;

import java.lang.foreign.*;
import java.nio.charset.StandardCharsets;

public class CustomerInfoFFIWriter {

    public static MemorySegment toOffHeap2(Arena arena, CustomerInfo customer) {
        MemorySegment seg = arena.allocate(CustomerInfoLayout.LAYOUT);

        // write primitive fields
        seg.set(ValueLayout.JAVA_INT, CustomerInfoLayout.LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("id")), customer.getId());
        seg.set(ValueLayout.JAVA_DOUBLE, CustomerInfoLayout.LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("balance")), customer.getBalance());

        // helper to copy Java String → fixed-size C char[]
        writeString(seg, "name", customer.getName(), Util.MAX_NAME_CHARS);
        writeString(seg, "lastName", customer.getLastName(), Util.MAX_NAME_CHARS);
        writeString(seg, "homePhone", customer.getHomePhone(), Util.MAX_PHONE_CHARS);
        writeString(seg, "homeEmail", customer.getHomeEmail(), Util.MAX_EMAIL_CHARS);

        return seg;
    }

    public static MemorySegment toOffHeap(Arena arena, int custId, String firstName, String lastName,
                                          String phone, String email, double balance ) {
        MemorySegment seg = arena.allocate(CustomerInfoLayout.LAYOUT);//like malloc

        // write primitive fields
        seg.set(ValueLayout.JAVA_INT, CustomerInfoLayout.LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("id")), custId);
        seg.set(ValueLayout.JAVA_DOUBLE, CustomerInfoLayout.LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("balance")), balance);

        // helper to copy Java String → fixed-size C char[]
        writeString(seg, "name", firstName, Util.MAX_NAME_CHARS);
        writeString(seg, "lastName", lastName, Util.MAX_NAME_CHARS);
        writeString(seg, "homePhone", phone, Util.MAX_PHONE_CHARS);
        writeString(seg, "homeEmail", email, Util.MAX_EMAIL_CHARS);

        return seg;
    }

    public static void updateContact(MemorySegment seg, String newPhone, String newEmail) {
        writeString(seg, "homePhone", newPhone, Util.MAX_PHONE_CHARS);
        writeString(seg, "homeEmail", newEmail, Util.MAX_EMAIL_CHARS);
    }

    private static void writeString(MemorySegment parent, String field, String value, int maxLen) {
        MemorySegment stringSegment = parent.asSlice(CustomerInfoLayout.LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement(field)), maxLen);
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        int len = Math.min(bytes.length, maxLen - 1); // leave space for '\0'
        //stringSegment.copyFrom(MemorySegment.ofArray(bytes, 0, len));
        stringSegment.copyFrom(MemorySegment.ofArray(bytes));
        stringSegment.setAtIndex(ValueLayout.JAVA_BYTE, len, (byte) 0); // null terminate
    }
}
