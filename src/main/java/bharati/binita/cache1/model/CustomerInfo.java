package bharati.binita.cache1.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomerInfo {

    private int id;
    private String name;
    private String lastName;
    private String homePhone;
    private String homeEmail;
    private double balance;

    @JsonProperty("trxns")
    private List<TransactionDetails> transactionDetails;

    public CustomerInfo() {
        this.transactionDetails = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getHomePhone() {
        return homePhone;
    }

    public void setHomePhone(String homePhone) {
        this.homePhone = homePhone;
    }

    public String getHomeEmail() {
        return homeEmail;
    }

    public void setHomeEmail(String homeEmail) {
        this.homeEmail = homeEmail;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public List<TransactionDetails> getTransactionDetails() {
        return transactionDetails;
    }

    public void addTransactionDetails(TransactionDetails transactionDetails) {
        this.transactionDetails.add(transactionDetails);
    }
}
