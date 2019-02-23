package com.cryptoapp.nfcapp.utils;

import org.bitcoinj.core.Transaction;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Formatter;

public class ToHexBinary {

    public ToHexBinary(Transaction input) {
        final StringBuilder sb = new StringBuilder();
        Formatter formatter = new Formatter(sb);
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            input.bitcoinSerialize(os);
            byte[] bytes = os.toByteArray();
            for (byte b : bytes) {
                formatter.format("%02x", b);
            }

            System.out.println(sb.toString());
        }catch (IOException e) {
        } finally {
            formatter.close();
        }
    }
}
