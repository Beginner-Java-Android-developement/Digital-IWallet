package com.golan.amit.iwallet;

import java.util.Date;

public class WalletAction {

    private int id;
    private int deposit;
    private int draw;
    private String curr_datetime;

    public WalletAction(int id, int deposit, int draw, String curr_datetime) {
        this.id = id;
        this.deposit = deposit;
        this.draw = draw;
        this.curr_datetime = curr_datetime;
    }

    public WalletAction() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getDeposit() {
        return deposit;
    }

    public void setDeposit(int deposit) {
        this.deposit = deposit;
    }

    public int getDraw() {
        return draw;
    }

    public void setDraw(int draw) {
        this.draw = draw;
    }

    public String getCurr_datetime() {
        return curr_datetime;
    }

    public void setCurr_datetime(String curr_datetime) {
        this.curr_datetime = curr_datetime;
    }

    @Override
    public String toString() {
        return "WalletAction{" +
                "id=" + id +
                ", deposit=" + deposit +
                ", draw=" + draw +
                ", curr_datetime=" + curr_datetime +
                '}';
    }
}
