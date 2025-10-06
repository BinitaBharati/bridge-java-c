package bharati.binita.cache1.util;

import bharati.binita.cache1.model.CustomerInfo;
import bharati.binita.cache1.model.TransactionDetails;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Random;

public class Util {

    //The below entries should correspond with C customer_info struct field limits.
    public static int MAX_NAME_CHARS = 10;
    public static int MAX_PHONE_CHARS = 20;
    public static int MAX_EMAIL_CHARS = 20;
    public static int[] READ_UPDATE_CUST_IDS = {10000, 20000, 30000, 40000, 50000, 60000, 70000, 80000, 90000, 100000};

    public static ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    public static int CREDIT_TRXN_TYPE = 1;
    public static int DEBIT_TRXN_TYPE = 2;
    public static final int MAX_CACHE_ENTRIES = 100000000;
    public static Random NOT_THREAD_SAFE_RANDOM = new Random();




    private static final String EMAIL_CHARSET = "abcdefghijklmnopqrstuvwxyz0123456789";
    private static final String[] DOMAINS = {
            "gmail.com", "yahoo.com", "outlook.com", "example.com", "hotmail.com",
    };

    private static final String ALPHABET_CHARSET = "abcdefghijklmnopqrstuvwxyz";
    private static final int START_YEAR = 2020;
    private static final int END_YEAR = 2025;

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

    public static TransactionDetails addTransactionEntry(CustomerInfo customerInfo) {
        TransactionDetails trxn = new TransactionDetails();
        trxn.setTrxnDate(generateRandomDate());
        if (NOT_THREAD_SAFE_RANDOM.nextBoolean()) {
            trxn.setCredit(NOT_THREAD_SAFE_RANDOM.nextDouble(100, 100000000));
            trxn.setBalance(trxn.getCredit() + customerInfo.getBalance());
            customerInfo.setBalance(trxn.getBalance());
        }
        else {
            trxn.setDebit(NOT_THREAD_SAFE_RANDOM.nextDouble(0, customerInfo.getBalance()));
            trxn.setBalance(customerInfo.getBalance() - trxn.getDebit());
            customerInfo.setBalance(trxn.getBalance());
        }
        return trxn;
    }

    public static String generateRandomDate() {
        // Start and end dates
        LocalDate start = LocalDate.of(START_YEAR, 1, 1);
        LocalDate end = LocalDate.of(END_YEAR, 12, 31);

        // Random epoch day between start and end
        long randomDay = NOT_THREAD_SAFE_RANDOM.nextLong(start.toEpochDay(), end.toEpochDay() + 1);

        LocalDate randomDate = LocalDate.ofEpochDay(randomDay);
        return randomDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

}
