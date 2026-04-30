package com.example.debtmanager;

public class Debt {
    private String name;
    private double amount;
    private boolean isPaid;

    // 无参构造（JSON序列化必须）
    public Debt() {}

    public Debt(String name, double amount) {
        this.name = name;
        this.amount = amount;
        this.isPaid = false;
    }

    // Getter 和 Setter
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public boolean isPaid() {
        return isPaid;
    }

    public void setPaid(boolean paid) {
        isPaid = paid;
    }
}