package bharati.binita.test;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

public class Main {

    private static void validateAddress(Set<String> address) {
        if (null != address && !address.isEmpty()) {
            boolean success = false;
            Set<String> invalidAddressList = new HashSet<String>();
            for (String _address : address) {
                success = _validateAddress(_address);
                if (!success) {
                    //result.put(ErrorCode.INVALID_RECIPIENT_ADDRESS_FORMAT, _address);
                    invalidAddressList.add(_address);
                }
            }
            //invalidAddressList.add("abc@def.in");
            //remove invalid addresses from original list
            //if (invalidAddressList.size() > 0)
                address.removeAll(invalidAddressList);
            System.out.println(address);

        }
    }

    private static boolean _validateAddress(String address) {
        boolean failed = false;
        try {
            InternetAddress.parse(address);
        } catch (AddressException e) {
            //address parsing failed
            failed = true;
        }
        return !failed;
    }

    public static void main(String[] args) {
        Set<String> address = Set.of("annie.myers@vdx.tv", "abc@def.in");
        validateAddress(address);
        System.out.println(address);
    }
}
