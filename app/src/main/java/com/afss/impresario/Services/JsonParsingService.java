package com.afss.impresario.Services;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class JsonParsingService {

    public String parse(String retrievesTransactionsJSON, String keyName) throws JSONException {
        JSONObject jsonObject = new JSONObject(retrievesTransactionsJSON);

       String extractedData = (String) jsonObject.get(keyName);
         return extractedData;
    }
}
