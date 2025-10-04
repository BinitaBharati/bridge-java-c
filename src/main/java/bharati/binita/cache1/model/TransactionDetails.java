package bharati.binita.cache1.model;

public class TransactionDetails {

    private String trxnDate;
    private double credit;
    private double debit;
    private double balance;

    public String getTrxnDate() {
        return trxnDate;
    }

    public void setTrxnDate(String trxnDate) {
        this.trxnDate = trxnDate;
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
}
