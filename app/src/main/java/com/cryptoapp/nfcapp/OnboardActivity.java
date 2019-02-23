package com.cryptoapp.nfcapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.github.paolorotolo.appintro.AppIntro;
import com.google.common.collect.ImmutableList;
import com.subgraph.orchid.encoders.Hex;

import org.bitcoin.Secp256k1Context;
import org.bitcoinj.core.Base58;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.DumpedPrivateKey;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.bitcoinj.core.ECKey.fromPrivate;

public class OnboardActivity extends AppIntro implements FirstFragment.OnFragmentInteractionListener,
SecondFragment.OnFragmentInteractionListener, ThirdFragment.OnFragmentInteractionListener, FourthFragment.OnFragmentInteractionListener,
FifthFragment.OnFragmentInteractionListener, SixthFragment.OnFragmentInteractionListener{

    ECKey key1, key2, key3;
    Script script2;
    NfcAdapter nfcAdapter;
    List<ECKey> list = new ArrayList<>();
    String tagContent;
    NetworkParameters params = MainNetParams.get();
    String currentKey, address, scriptInHex;
    private final static char[] hexArray = "0123456789abcdef".toCharArray();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        for(int i = 0; i < 3; i++) {
            ECKey key = new ECKey();
            list.add(key);
        }

        //hardcoded keys for testing purposes

        DumpedPrivateKey dumpedPrivateKey = DumpedPrivateKey.fromBase58(params, "KyudQLnimxG2iJYCfBvFdq1RgAUpzNLZj22iJTME4rEjbA9odnxW");
        DumpedPrivateKey dumpedPrivateKey2 = DumpedPrivateKey.fromBase58(params, "L2btsqK9UVmUhxejuvJFxrHyTwaVjAy3BFCJZx52c5vJ91FKUWcB");
        DumpedPrivateKey dumpedPrivateKey3 = DumpedPrivateKey.fromBase58(params, "L13NBk796kYbvdWoXbku3cQTQ9x9HsR4fhuzaRJZFmKFySFQZ6vt");
        key1 = dumpedPrivateKey.getKey();
        key2 = dumpedPrivateKey2.getKey();
        key3 = dumpedPrivateKey3.getKey();
        System.out.println(key1.getPrivateKeyAsWiF(params));
        System.out.println(key2.getPrivateKeyAsWiF(params));
        System.out.println(key3.getPrivateKeyAsWiF(params));




        //create multisig output script
        Transaction contract = new Transaction(params);
//        List<ECKey> keys = ImmutableList.of(list.get(0), list.get(1), list.get(2));
        List<ECKey> keys = ImmutableList.of(key1, key2, key3);
        Script script = ScriptBuilder.createMultiSigOutputScript(2, keys);
        script2 = ScriptBuilder.createRedeemScript(2, keys);
        Script lockingScript = ScriptBuilder.createP2SHOutputScript(script2);
        address = String.valueOf(lockingScript.getToAddress(params));
        scriptInHex = bytesToHex(script2.getProgram());
        System.out.println(address);
        System.out.println(bytesToHex(script2.getProgram()));
        Coin amount = Coin.valueOf(0, 0);




        addSlide(new FirstFragment().newInstance(R.layout.fragment_first));
        addSlide(new SecondFragment().newInstance(R.layout.fragment_second));
        addSlide(new ThirdFragment().newInstance(R.layout.fragment_third));
        addSlide(new FourthFragment().newInstance(R.layout.fragment_fourth));
        addSlide(new FifthFragment().newInstance(R.layout.fragment_fifth));
        addSlide(new SixthFragment().newInstance(R.layout.fragment_sixth, address));


    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        Intent i = new Intent(OnboardActivity.this, MainActivity.class);
        i.putExtra("outputscript", script2.getProgram());
        startActivity(i);
        finish();


    }


    @Override
    public void onSlideChanged(@Nullable Fragment oldFragment, @Nullable Fragment newFragment) {
        super.onSlideChanged(oldFragment, newFragment);



        if(newFragment instanceof ThirdFragment) {
            enableForegroundDispatchService();
            currentKey = key1.getPrivateKeyAsWiF(params);
            System.out.println(currentKey);

        }

        if(newFragment instanceof FourthFragment){
            disableForegroundDispatchService();
            System.out.println(currentKey);

        }

        if(newFragment instanceof  FifthFragment){
            enableForegroundDispatchService();
            currentKey = key2.getPrivateKeyAsWiF(params);
        }
        if(newFragment instanceof  SixthFragment){
            disableForegroundDispatchService();
        }




    }

    @Override
    public void generateKey2(String key) {

//        key2 = key;
//        System.out.println(key2);
    }

    @Override
    public void generateKey1(String key) {

//        key1 = key;
//        currentKey = key1;
//        System.out.println(currentKey);
//        assert currentKey == key1;
    }



    private void enableForegroundDispatchService() {
        Intent intent = new Intent(this, OnboardActivity.class).addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        IntentFilter[] intentfilter = new IntentFilter[]{};


        nfcAdapter.enableForegroundDispatch(this, pendingIntent, intentfilter, null);


    }


    private void disableForegroundDispatchService() {
        nfcAdapter.disableForegroundDispatch(this);
    }

    public String getTextFromNDEFRecord(NdefRecord ndefRecord) {
        String tagContent = null;

        try {
            byte[] payload = ndefRecord.getPayload();
            String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16";
            int languageSize = payload[0] & 0063;
            tagContent = new String(payload, languageSize + 1,
                    payload.length - languageSize - 1, textEncoding);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return tagContent;
    }

    private void readTextFromMessage(NdefMessage ndefMessage) {
        NdefRecord[] ndefRecords = ndefMessage.getRecords();

        if (ndefRecords != null && ndefRecords.length > 0) {
            NdefRecord ndefRecord = ndefRecords[0];
            tagContent = getTextFromNDEFRecord(ndefRecord);
            Log.d("key", tagContent);
            Toast.makeText(this, tagContent, Toast.LENGTH_SHORT).show();


        } else {
            Toast.makeText(this, "No NDEF Records found", Toast.LENGTH_SHORT).show();
        }


    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (intent.hasExtra(nfcAdapter.EXTRA_TAG)) {


                Toast.makeText(this, "NFCIntent", Toast.LENGTH_SHORT).show();

                Tag tag = intent.getParcelableExtra(nfcAdapter.EXTRA_TAG);

                NdefMessage ndefMessage = createNdefMessage(currentKey);
                writeNdefMessage(tag, ndefMessage);
            }
    }

    //TODO: writing to tag
    private void formatTag(Tag tag, NdefMessage ndefMessage) {
        try {
            NdefFormatable ndefFormatable = NdefFormatable.get(tag);

            if (ndefFormatable == null) {
                Toast.makeText(this, "Tag is not ndef formattable", Toast.LENGTH_SHORT).show();

            }

            ndefFormatable.connect();
            ndefFormatable.format(ndefMessage);
            ndefFormatable.close();

            Toast.makeText(this, "tag written", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (FormatException e) {
            e.printStackTrace();
        }
    }

    private void writeNdefMessage(Tag tag, NdefMessage ndefMessage) {
        try {
            if (tag == null) {
                Toast.makeText(this, "Tag object cannot be null", Toast.LENGTH_SHORT).show();
                return;
            }

            Ndef ndef = Ndef.get(tag);

            if (ndef == null) {
                formatTag(tag, ndefMessage);
            } else {
                ndef.connect();

                if (!ndef.isWritable()) {
                    Toast.makeText(this, "Ndef is not writable", Toast.LENGTH_SHORT).show();
                    ndef.close();
                    return;


                }

                ndef.writeNdefMessage(ndefMessage);
                ndef.close();

                Toast.makeText(this, "tag written", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (FormatException e) {
            e.printStackTrace();
        }
    }


    private NdefRecord createTextRecord(String content) {
        try {
            byte[] language;
            language = Locale.getDefault().getLanguage().getBytes("UTF-8");

            final byte[] text = content.getBytes();
            final int languageSize = language.length;
            final int textLength = text.length;
            final ByteArrayOutputStream payload = new ByteArrayOutputStream(1 + languageSize + textLength);

            payload.write((byte) languageSize & 0x1F);
            payload.write(language, 0, languageSize);
            payload.write(text, 0, textLength);

            return new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, new byte[0], payload.toByteArray());

        } catch (UnsupportedEncodingException e) {
            Log.e("createTextRecord", e.getMessage());
        }
        return null;
    }

    private NdefMessage createNdefMessage(String content) {
        NdefRecord ndefRecord = createTextRecord(content);
        NdefMessage ndefMessage = new NdefMessage(new NdefRecord[] {
            ndefRecord
        });

        return ndefMessage;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }



    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
}
