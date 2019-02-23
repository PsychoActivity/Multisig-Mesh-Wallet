package com.cryptoapp.nfcapp;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.cryptoapp.nfcapp.utils.AddressBalance;
import com.cryptoapp.nfcapp.utils.ToHexBinary;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.material.tabs.TabLayout;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.ListenableFuture;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.widget.TextView;
import android.widget.Toast;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Block;
import org.bitcoinj.core.BlockChain;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.DumpedPrivateKey;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.FilteredBlock;
import org.bitcoinj.core.InsufficientMoneyException;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Peer;
import org.bitcoinj.core.PeerGroup;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionConfidence;
import org.bitcoinj.core.TransactionInput;
import org.bitcoinj.core.TransactionOutPoint;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.core.listeners.DownloadProgressTracker;
import org.bitcoinj.crypto.TransactionSignature;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.net.discovery.DnsDiscovery;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcoinj.store.BlockStoreException;
import org.bitcoinj.store.SPVBlockStore;
import org.bitcoinj.wallet.SendRequest;
import org.bitcoinj.wallet.Wallet;
import org.bitcoinj.wallet.listeners.WalletCoinsReceivedEventListener;
import org.bitcoinj.wallet.listeners.WalletCoinsSentEventListener;
import org.spongycastle.util.encoders.Hex;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity implements WalletFragment.OnFragmentInteractionListener, PocketFragment.OnFragmentInteractionListener {

    /**
     * The {@link androidx.viewpager.widget.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * androidx.fragment.app.FragmentStatePagerAdapter.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ECKey key1, key2, key3;
    private ViewPager mViewPager;
    PeerGroup peerGroup;
    private final static char[] hexArray = "0123456789abcdef".toCharArray();
    String tagContent, scriptInHex, receiveAddress;
    NfcAdapter nfcAdapter;
    private TextView multiSigBalance;
    private NetworkParameters params = MainNetParams.get();
    private WalletAppKit kit;
    private Toolbar toolbar;
    private static final String multiSigAddress = "3BesSajTyegQaBeyJXes6kyav3srZC3p7D";
    private static final String TAG = "MainActivity";
    private List<ECKey> list = new ArrayList<>();
    private List<TransactionSignature> keyListForTx = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        File chainFile = new File(this.getFilesDir(), "test.spvchain");
//        System.out.println("does chainfile exist?"  + chainFile.exists());
//        if(chainFile.exists()) {
//            try {
//                SPVBlockStore chainStore = new SPVBlockStore(params, chainFile);
//                BlockChain chain = new BlockChain(params, chainStore);
//                peerGroup = new PeerGroup(params, chain);
//                peerGroup.addPeerDiscovery(new DnsDiscovery(params));
//                peerGroup.startAsync();
//            } catch (BlockStoreException e) {
//                e.printStackTrace();
//            }
//        }
//        scriptInHex = bytesToHex(getIntent().getExtras().getByteArray("outputscript"));
        setContentView(R.layout.activity_main);
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        //TODO: Get same object from other activity to use as redeemscript

        DumpedPrivateKey dumpedPrivateKey = DumpedPrivateKey.fromBase58(params, "KyudQLnimxG2iJYCfBvFdq1RgAUpzNLZj22iJTME4rEjbA9odnxW");
        DumpedPrivateKey dumpedPrivateKey2 = DumpedPrivateKey.fromBase58(params, "L2btsqK9UVmUhxejuvJFxrHyTwaVjAy3BFCJZx52c5vJ91FKUWcB");
        DumpedPrivateKey dumpedPrivateKey3 = DumpedPrivateKey.fromBase58(params, "L13NBk796kYbvdWoXbku3cQTQ9x9HsR4fhuzaRJZFmKFySFQZ6vt");
        key1 = dumpedPrivateKey.getKey();
        key2 = dumpedPrivateKey2.getKey();
        key3 = dumpedPrivateKey3.getKey();

        multiSigBalance = findViewById(R.id.btc_amount);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        kit = new WalletAppKit(params, this.getFilesDir(), "test")
        {
            @Override
            protected void onSetupCompleted() {
                System.out.println(kit.wallet().currentReceiveAddress());
                kit.wallet().addWatchedAddress(Address.fromBase58(params, "3BesSajTyegQaBeyJXes6kyav3srZC3p7D"));
                receiveAddress = kit.wallet().currentReceiveAddress().toString();
                setWalletListeners(kit.wallet());
                System.out.println("currentBal=" + kit.wallet().getBalance(new AddressBalance(Address.fromBase58(params, multiSigAddress))).toFriendlyString());

            }
        };

        kit.setDownloadListener(bListener);
        kit.setBlockingStartup(false);
        kit.setAutoSave(true);
        kit.startAsync();
        kit.awaitRunning();
        System.out.println("watched op= " + kit.wallet().getWatchedOutputs(true));
        multiSigBalance.setText(kit.wallet().getBalance(new AddressBalance(Address.fromBase58(params, multiSigAddress))).toFriendlyString());




//         Create the adapter that will return a fragment for each of the three
//         primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setCurrentItem(1, true);

        final TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout){
            @Override
            public void onPageSelected(int position) {
                if(position == 0) {
                    multiSigBalance.setText(kit.wallet().getBalance(Wallet.BalanceType.AVAILABLE_SPENDABLE).toFriendlyString());
                }
                if(position == 1) {
                    multiSigBalance.setText(kit.wallet().getBalance(new AddressBalance(Address.fromBase58(params, multiSigAddress))).toFriendlyString());
                }


            }
        });
//        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager){
//            @Override
//            public void onTabReselected(TabLayout.Tab tab) {
//
//
//                Snackbar.make(tabLayout.getRootView(), "You should back up your wallet", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//
//            @Override
//            public void onTabSelected(TabLayout.Tab tab) {
//
//
//                Snackbar.make(tabLayout.getRootView(), "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//
//
//        });

        FloatingActionMenu fabMenu = findViewById(R.id.fab_menu);
        com.github.clans.fab.FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //present dialog from material dialog framework
                new MaterialDialog.Builder(MainActivity.this)
                        .title("Title")
                        .content("Transfer")
                        .positiveText("Begin")
                        .icon(getResources().getDrawable(R.drawable.ic_check_black_24dp))
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                Intent browse = new Intent(Intent.ACTION_VIEW, Uri.parse("https://docs.google.com/uc?export=download&id=1C9u-uQNb49V3zJK0HSVeVmG0pTqYXf9d"));
                                startActivity(browse);
                            }
                        })
                        .show();
            }
        });


        //if you want to update the items at a later time it is recommended to keep it in a variable
        PrimaryDrawerItem item1 = new PrimaryDrawerItem().withName("News").withIdentifier(1);
        SecondaryDrawerItem item2 = new SecondaryDrawerItem().withIdentifier(2).withName("Web3 Browser");
        SecondaryDrawerItem item3 = new SecondaryDrawerItem().withIdentifier(3).withName("ProofOfKeys");
        SecondaryDrawerItem item4 = new SecondaryDrawerItem().withIdentifier(3).withName("Finance");
        SecondaryDrawerItem item5 = new SecondaryDrawerItem().withIdentifier(3).withName("Education");
        SecondaryDrawerItem item6 = new SecondaryDrawerItem().withIdentifier(3).withName("Privacy");
        SecondaryDrawerItem item7 = new SecondaryDrawerItem().withIdentifier(3).withName("Collections");
        SecondaryDrawerItem item8 = new SecondaryDrawerItem().withIdentifier(3).withName("Tools");
//create the drawer and remember the `Drawer` result object
        AccountHeader headerResult = new AccountHeaderBuilder()
                .withActivity(this)
                .build();

        Drawer result = new DrawerBuilder()
                .withActivity(this)
                .withAccountHeader(headerResult)
                .withToolbar(toolbar)
                .withSelectedItem(-1)
                .addDrawerItems(
                        item1, item2, item3, item4, item5, item6, item7, item8
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        selectItem(position);
                        return false;
                    }
                })
                .build();

        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        result.getActionBarDrawerToggle().setDrawerIndicatorEnabled(true);


    }



    //Logic for selecting item position and executing it's action
    private void selectItem(int position) {
        switch (position) {
            //case 0 is officially the header??

            case 2: {
                Intent web3Browser = new Intent(MainActivity.this, Web3Activity.class);
                startActivity(web3Browser);
                break;
            }
            case 3: {

                //present dialog from material dialog framework
                new MaterialDialog.Builder(this)
                        .title("Title")
                        .content("Content")
                        .positiveText("Begin")
                        .icon(getResources().getDrawable(R.drawable.ic_check_black_24dp))
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                Intent browse = new Intent(Intent.ACTION_VIEW, Uri.parse("https://docs.google.com/uc?export=download&id=1C9u-uQNb49V3zJK0HSVeVmG0pTqYXf9d"));
                                startActivity(browse);
                            }
                        })
                        .show();
                break;
            }

        }
    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }



    /**
     * A placeholder fragment containing a simple view.
     */
//    public static class PlaceholderFragment extends Fragment {
//        /**
//         * The fragment argument representing the section number for this
//         * fragment.
//         */
//        private static final String ARG_SECTION_NUMBER = "section_number";
//
//        public PlaceholderFragment() {
//        }
//
//        /**
//         * Returns a new instance of this fragment for the given section
//         * number.
//         */
//        public static PlaceholderFragment newInstance(int sectionNumber) {
//            PlaceholderFragment fragment = new PlaceholderFragment();
//            Bundle args = new Bundle();
//            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
//            fragment.setArguments(args);
//            return fragment;
//        }
//
//        @Override
//        public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                                 Bundle savedInstanceState) {
//            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
//            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
//            textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
//            return rootView;
//        }
//    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter implements WalletFragment.OnFragmentInteractionListener, PocketFragment.OnFragmentInteractionListener{

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            switch (position) {
                case 0:
                    return PocketFragment.newInstance(kit.wallet().getBalance(Wallet.BalanceType.AVAILABLE_SPENDABLE).toFriendlyString(), null);
                case 1:
                    return WalletFragment.newInstance(multiSigAddress, null);

                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            // Show 2 total pages.
            return 2;
        }

        @Override
        public void onFragmentInteraction(Uri uri) {

        }

        @Override
        public void openNFCReader() {

        }

    }
    @Override
    public void openNFCReader() {
        enableForegroundDispatchService();
        new MaterialDialog.Builder(MainActivity.this)
                .title("Read From Tag")
                .content("Tap tag")
                .positiveText("Send")
                .icon(getResources().getDrawable(R.drawable.nfc_icon))
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                        DumpedPrivateKey dumpedPrivateKey = DumpedPrivateKey.fromBase58(params, tagContent);
                        list.add(dumpedPrivateKey.getKey());
                        list.add(DumpedPrivateKey.fromBase58(params,"L13NBk796kYbvdWoXbku3cQTQ9x9HsR4fhuzaRJZFmKFySFQZ6vt").getKey());
                        kit.wallet().importKey(list.get(0));
                        kit.wallet().importKey(list.get(1));
                        List<ECKey> keys = ImmutableList.of(key1, key2, key3);


//                        Script script = ScriptBuilder.createRedeemScript(2, keys);
//                        Script scriptPubKey = ScriptBuilder.createP2SHOutputScript(script);


                        //get UTXO and get proper output

//                        Transaction contract = new Transaction(params);
//                        TransactionOutput multiSigOutput = contract.addOutput(output.get(0));
                        //build transaction that spends money

                        //TODO: INIT

                        // initialize a transaction
                        Transaction spendTx = new Transaction(params);

                        //create redeem script used to verify the spending conditions
                        Script script = ScriptBuilder.createRedeemScript(2, keys);

                        //create address that receives the funds
                        Address address = Address.fromBase58(params, "1Kw17uSmzBdeDJbjQADsMAZkcJYLGSDLF4");


                        //create scriptpubkey
                        Script outputScript = ScriptBuilder.createOutputScript(address);
                        Script p2shOutputScript = ScriptBuilder.createP2SHOutputScript(script);

                        //TODO: INPUTS

                        //get list of UTXO's and their outpoints
                        List<TransactionOutput> output = kit.wallet().getWatchedOutputs(true);
                        TransactionOutPoint outPoint = new TransactionOutPoint(params, output.get(2));

                        //Use UTXO's as input into the transaction
                        TransactionInput input = new TransactionInput(params, spendTx, outputScript.getProgram(), outPoint);
                        spendTx.addInput(input);

                        //TODO: SIGNATURES FOR INPUTS
                        //sign transaction with both keys needed to satisfy the multisig requirements
                        TransactionSignature txSig = spendTx.calculateSignature(0, keys.get(0), script, Transaction.SigHash.ALL, false);
                        TransactionSignature txSig1 = spendTx.calculateSignature(0, keys.get(1), script, Transaction.SigHash.ALL, false);
                        Script inputScript = ScriptBuilder.createP2SHMultiSigInputScript((ImmutableList.of(txSig, txSig1)), script);
//
                        //
                        input.setScriptSig(inputScript);

                        //TODO: OUTPUTS
                        //add the output to the transaction with the satoshi amount
                        spendTx.addOutput(Coin.valueOf(100000), outputScript);
                        spendTx.addOutput(Coin.valueOf(148000), p2shOutputScript);


                        //TODO: FEES
                        spendTx.getFee();

                        //TODO: GET RAW TX HEX
                        new ToHexBinary(spendTx);
                        System.out.println(inputScript);

//                        kit.wallet().importKeys(keys);
//                        System.out.println(kit.wallet().getImportedKeys());

                        //TODO FINALLY BROADCAST TX TO NETWORK
//                      peerGroup.broadcastTransaction(spendTx);

                        //TODO: make sure to release tag data after done list.clear()
//                        list.clear();
//                        tagContent = null;
//                        System.out.println(list);
                    }
                })
                .show();
    }

    // TODO: Set up wallet listeners after setup = complete
    private void setWalletListeners(Wallet wallet) {
        wallet.addCoinsReceivedEventListener(new WalletCoinsReceivedEventListener() {
            @Override
            public void onCoinsReceived(Wallet wallet, Transaction tx, Coin prevBalance, Coin newBalance) {
                System.out.println("onCoinsReceived(): Coins received. Your new balance is " + newBalance + " satoshis");

            }
        });
        wallet.addCoinsSentEventListener(new WalletCoinsSentEventListener() {
            @Override
            public void onCoinsSent(Wallet wallet, Transaction tx, Coin prevBalance, Coin newBalance) {
                System.out.println("onCoinsSent(): Coins sent. Your new balance is " + newBalance + " satoshis");
            }
        });
    }

    // TODO: Blockchain Download Progress Tracker
    DownloadProgressTracker bListener = new DownloadProgressTracker() {
        @Override
        public void doneDownload() {
//            System.out.println("doneDownload(): wallet info " + kit.wallet().toString());

//            System.out.println("doneDownload(): Wallet map.............. == " + pWalletMap.toString());

        }

        @Override
        protected void progress(double pct, int blocksSoFar, Date date) {
            super.progress(pct, blocksSoFar, date);
            System.out.println("progress" + blocksSoFar);
            System.out.println("Percent done" + pct);

        }

        @Override
        public void onChainDownloadStarted(Peer peer, int blocksLeft) {
            super.onChainDownloadStarted(peer, blocksLeft);
            System.out.println("onChainDownloadStarted(): " + blocksLeft);
        }

        @Override
        public void onBlocksDownloaded(Peer peer, Block block, @Nullable FilteredBlock filteredBlock, int blocksLeft) {
            super.onBlocksDownloaded(peer, block, filteredBlock, blocksLeft);
            System.out.println("onBlocksDownloaded " + blocksLeft);

            if(blocksLeft == 0) {
                multiSigBalance.setText(kit.wallet().getBalance(new AddressBalance(Address.fromBase58(params, multiSigAddress))).toFriendlyString());
//                System.out.println("onBlocksDownloaded(): Wallet map.............. == " + pWalletMap.toString());
            }
        }


        @Override
        protected void startDownload(int blocks) {
            super.startDownload(blocks);
            System.out.println("startDownload");
        }
    };

    private void enableForegroundDispatchService() {
        Intent intent = new Intent(this, MainActivity.class).addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);

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

//            TODO: Tag reading
            Toast.makeText(this, "Tag read", Toast.LENGTH_SHORT).show();

            Parcelable[] parcelables = intent.getParcelableArrayExtra(nfcAdapter.EXTRA_NDEF_MESSAGES);

            if (parcelables != null && parcelables.length > 0) {
                readTextFromMessage((NdefMessage) parcelables[0]);
            } else {
                Toast.makeText(this, "No NDefmessage found", Toast.LENGTH_SHORT).show();
            }
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
