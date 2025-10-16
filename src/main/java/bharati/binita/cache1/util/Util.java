package bharati.binita.cache1.util;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class Util {

    //The below entries should correspond with C customer_info struct field limits.
    public static int MAX_NAME_CHARS = 10;
    public static int MAX_PHONE_CHARS = 20;
    public static int MAX_EMAIL_CHARS = 20;
    public static int[] READ_UPDATE_CUST_IDS = {10000, 20000, 30000, 40000, 50000, 60000, 70000, 80000, 90000, 100000};

    public static ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    public static int CREDIT_TRXN_TYPE = 0;
    public static int DEBIT_TRXN_TYPE = 1;
    public static final int MAX_CACHE_ENTRIES = 100000000;
    public static final int MAX_TRXNS_PER_CUSTOMER = 5;
    public static final int CUSTOMER_IDS_BATCH_COUNT = 10;
    public static Random NOT_THREAD_SAFE_RANDOM = new Random();
    public static Random THREAD_SAFE_RANDOM = ThreadLocalRandom.current();
    public static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                    .withZone(ZoneId.systemDefault());
    public static int CUSTOMER_INFO_JSON_STR_SIZE=200;
    public static int CUSTOMER_TRXN_INFO_JSON_STR_SIZE=600;


    private static final String EMAIL_CHARSET = "abcdefghijklmnopqrstuvwxyz0123456789";
    private static final String[] DOMAINS = {
            "gmail.com", "yahoo.com", "outlook.com", "example.com", "hotmail.com",
            };

    private static final String ALPHABET_CHARSET = "abcdefghijklmnopqrstuvwxyz";

    public static String generateRandomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        sb.append(Character.toUpperCase(ALPHABET_CHARSET.charAt(NOT_THREAD_SAFE_RANDOM.nextInt(ALPHABET_CHARSET.length()))));
        for (int i = 1; i < length; i++) {
            sb.append(ALPHABET_CHARSET.charAt(NOT_THREAD_SAFE_RANDOM.nextInt(ALPHABET_CHARSET.length())));
        }
        return sb.toString();
    }

    public static String generateRandomEmail(int maxLength) {
        Random random = new Random();

        // Pick a random domain
        String domain = DOMAINS[random.nextInt(DOMAINS.length)];
        int domainLength = domain.length() + 1; // +1 for '@'

        // Remaining space for username part
        int userMax = Math.max(1, maxLength - domainLength);
        int userLength = random.nextInt(userMax - 3) + 3; // username at least 3 chars

        StringBuilder username = new StringBuilder();
        for (int i = 0; i < userLength; i++) {
            username.append(EMAIL_CHARSET.charAt(random.nextInt(EMAIL_CHARSET.length())));
        }

        String email = username + "@" + domain;

        // Ensure final length ≤ maxLength
        if (email.length() > maxLength) {
            // truncate username if needed
            int trimLength = email.length() - maxLength;
            email = username.substring(0, username.length() - trimLength) + "@" + domain;
        }

        return email;
    }

    public static String generateUSPhoneNumber(int maxLength) {
        Random random = new Random();
        int area = 200 + random.nextInt(800);     // area code: 200–999
        int exchange = 200 + random.nextInt(800); // exchange: 200–999
        int subscriber = random.nextInt(10000);   // subscriber: 0000–9999

        String phone = String.format("(%03d) %03d-%04d", area, exchange, subscriber);

        // Ensure within max length
        if (phone.length() > maxLength) {
            phone = phone.substring(0, maxLength);
        }

        return phone;
    }

    public static long randomTimestampWithinOneDay(LocalDateTime inputDate) {
        // Parse input date string
        //LocalDateTime dateTime = LocalDateTime.parse(inputDate, formatter);
        ZoneId zone = ZoneId.systemDefault();

        long baseMillis = inputDate.atZone(zone).toInstant().toEpochMilli();
        long oneDayMillis = 24L * 60 * 60 * 1000;

        // Random offset between 0 and +1 day
        long randomOffset = ThreadLocalRandom.current().nextLong(0, oneDayMillis + 1);

        return baseMillis + randomOffset;
    }

    /*
    Returns a map of batch start customer id to batch end customer id.
     */
    public static Map<Integer, Integer> divideCustomersIntoBatches(int totalCustomers, int batchCount) {
        Map<Integer, Integer> batchStartToBatchEndMap = new HashMap<>();
        int batches = 5;

        int batchSize = totalCustomers / batchCount;
        int remainder = totalCustomers % batchCount;

        int start = 1;
        for (int i = 1; i <= batchCount; i++) {
            int end = start + batchSize - 1;

            // Distribute remainder (if total isn’t perfectly divisible)
            if (i == batches) {
                end += remainder;
            }
            batchStartToBatchEndMap.put(start, end);
            start = end + 1;
        }
        return batchStartToBatchEndMap;
    }
}