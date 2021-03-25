package com.afss.impresario.Services;

import java.util.ArrayList;

public class DataService {

    public String getBalance(ArrayList<String> txnAmountList, ArrayList<String> txnTypeList)
    {
        Integer length = txnAmountList.size();
        double balance = 0.0;

        for (int i=0; i<length; i++)
        {
            if (txnTypeList.get(i).contains("exp"))
            {
                balance-=Double.parseDouble(txnAmountList.get(i));
            }else {
                balance+=Double.parseDouble(txnAmountList.get(i));
            }

        }
        String strBalance = String.valueOf(balance);

        return strBalance;
    }

}
