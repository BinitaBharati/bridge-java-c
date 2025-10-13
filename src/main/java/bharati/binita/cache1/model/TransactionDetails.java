package bharati.binita.cache1.model;

import com.fasterxml.jackson.annotation.JsonSetter;

import java.time.Instant;

import static bharati.binita.cache1.util.Util.FORMATTER;

public class TransactionDetails {

    private String trxnDate;
    private double credit;
    private double debit;
    private double balance;

    public String getTrxnDate() {
        return trxnDate;
    }

    @JsonSetter("trxnDate")
    public void setTrxnDate(long epochMillis) {
        this.trxnDate = FORMATTER.format(Instant.ofEpochMilli(epochMillis));
    }

    public double getCredit() {
        return credit;
    }

    public void setCredit(double credit) {
        this.credit = credit;
    }

    public double getDebit() {
        return debit;
    }

    public void setDebit(double debit) {
        this.debit = debit;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    @Override
    public String toString() {
        return "TransactionDetails{" +
                "trxnDate='" + trxnDate + '\'' +
                ", credit=" + credit +
                ", debit=" + debit +
                ", balance=" + balance +
                '}';
    }
}
