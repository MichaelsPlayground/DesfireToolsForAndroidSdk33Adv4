package de.androidcrypto.desfiretoolsforandroidsdk33;

import static com.github.skjolber.desfire.libfreefare.MifareDesfire.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
//import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.nfc.NfcAdapter;
import android.nfc.NfcAdapter.ReaderCallback;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.github.skjolber.desfire.ev1.model.DesfireApplication;
import com.github.skjolber.desfire.ev1.model.DesfireApplicationId;
import com.github.skjolber.desfire.ev1.model.DesfireApplicationKey;
import com.github.skjolber.desfire.ev1.model.DesfireApplicationKeySettings;
import com.github.skjolber.desfire.ev1.model.DesfireTag;
import com.github.skjolber.desfire.ev1.model.VersionInfo;
import com.github.skjolber.desfire.ev1.model.command.DefaultIsoDepAdapter;
import com.github.skjolber.desfire.ev1.model.command.DefaultIsoDepWrapper;
import com.github.skjolber.desfire.ev1.model.command.Utils;
import com.github.skjolber.desfire.ev1.model.file.DesfireFile;
import com.github.skjolber.desfire.ev1.model.file.RecordDesfireFile;
import com.github.skjolber.desfire.ev1.model.file.StandardDesfireFile;
import com.github.skjolber.desfire.ev1.model.file.ValueDesfireFile;
import com.github.skjolber.desfire.ev1.model.key.Desfire3DESKey;
import com.github.skjolber.desfire.ev1.model.key.Desfire3K3DESKey;
import com.github.skjolber.desfire.ev1.model.key.DesfireAESKey;
import com.github.skjolber.desfire.ev1.model.key.DesfireDESKey;
import com.github.skjolber.desfire.ev1.model.key.DesfireKey;
import com.github.skjolber.desfire.ev1.model.key.DesfireKeyType;
import com.github.skjolber.desfire.libfreefare.MifareDESFireKey;
import com.github.skjolber.desfire.libfreefare.MifareDesfireKey1;
import com.github.skjolber.desfire.libfreefare.MifareTag;

import de.androidcrypto.desfiretoolsforandroidsdk33.filelist.ApplicationDetail;
import de.androidcrypto.desfiretoolsforandroidsdk33.filelist.ApplicationDetailApplicationKey;
import de.androidcrypto.desfiretoolsforandroidsdk33.filelist.ApplicationDetailFile;
import de.androidcrypto.desfiretoolsforandroidsdk33.filelist.ApplicationDetailKey;
import de.androidcrypto.desfiretoolsforandroidsdk33.filelist.ApplicationDetailRecord;
import de.androidcrypto.desfiretoolsforandroidsdk33.keys.DataSource;

/**
 * Note on DesfireToolsForAndroidSdk33Adv2
 * This is the same code as on Adv1 but all fragments are substituted from old/deprecated Fragment to AndroidX Fragment
 * Some new functions were implemented and error corrections has been done
 */

@SuppressLint("ResourceAsColor")
public class MainActivity extends AppCompatActivity implements ReaderCallback, FragmentManager.OnBackStackChangedListener, FileSaveFragment.Callbacks {

    private static final String ACTION_NFC_SETTINGS = "android.settings.NFC_SETTINGS";

    /**
     * this action seems never to be emitted, but is here for future use
     */
    private static final String ACTION_TAG_LEFT_FIELD = "android.nfc.action.TAG_LOST";

    public static final byte APPLICATION_CRYPTO_DES = 0x00;
    public static final byte APPLICATION_CRYPTO_3K3DES = 0x40;
    public static final byte APPLICATION_CRYPTO_AES = (byte) 0x80;

    private final byte[] MASTER_APPLICATION_ID = new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00};

    public static byte[] key_data_aes = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};

    public static final byte key_data_aes_version = 0x42;

    private static final String TAG = MainActivity.class.getName();

    private interface OnKeyListener {
        void onKey(DesfireKey key);
    }

    private interface OnKeyNumberListener {
        void onKeyNumber(int index, String access);
    }

    private NfcAdapter nfcAdapter;
    private List<DesfireApplication> applications;

    private DesfireApplication application;

    private DesfireApplicationKey authenticatedKey;

    private MifareTag tag;
    private DesfireTag desfireTag;
    private DefaultIsoDepAdapter defaultIsoDepAdapter;

    protected AlertDialog alertDialog;

    protected FileSaveFragment.Callbacks callbacks;

    protected BroadcastReceiver nfcStateChangeBroadcastReceiver;

    private TagPresenceScanner tagPresenceScanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //getFragmentManager().addOnBackStackChangedListener(this);
        getSupportFragmentManager().addOnBackStackChangedListener(this);

        // Check for available NFC Adapter
        PackageManager pm = getPackageManager();
        if (pm.hasSystemFeature(PackageManager.FEATURE_NFC) && NfcAdapter.getDefaultAdapter(this) != null) {
            Log.d(TAG, "NFC feature found");

            nfcAdapter = NfcAdapter.getDefaultAdapter(this);
            if (!nfcAdapter.isEnabled()) {
                startNfcSettingsActivity();

                showToast(R.string.nfcNotEnabledMessage);
            }

            showMainFragment();

            tagPresenceScanner = new TagPresenceScanner(this);
        } else {
            Log.d(TAG, "NFC feature not found");

            showToast(R.string.nfcNotAvailableMessage);

            finish();
        }

    }

    @Override
    public void onResume() {
        super.onResume();

        nfcAdapter.enableReaderMode(this, this, NfcAdapter.FLAG_READER_NFC_A | NfcAdapter.FLAG_READER_NFC_B | NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK, null);
    }

    @Override
    public void onPause() {
        super.onPause();

        nfcAdapter.disableReaderMode(this);

        tagPresenceScanner.pause();
    }

    @Override
    public void onTagDiscovered(Tag nfc) {
        IsoDep isoDep = IsoDep.get(nfc);

        //FragmentManager fragmentManager = getFragmentManager();
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.popBackStack("main", 0);

        DefaultIsoDepWrapper isoDepWrapper = new DefaultIsoDepWrapper(isoDep);

        defaultIsoDepAdapter = new DefaultIsoDepAdapter(isoDepWrapper, true); // todo change back to false

        try {
            isoDep.connect();

            setBackgroundColor(R.color.greenWhite);

            tag = mifare_desfire_tag_new();
            tag.setActive(1);
            tag.setIo(defaultIsoDepAdapter);

            desfireTag = new DesfireTag();

            VersionInfo versionInfo = mifare_desfire_get_version(tag);

            List<DesfireApplicationId> aids = mifare_desfire_get_application_ids(tag);
            if (aids != null) {
                Log.d(TAG, "Found applications " + aids.size());

                aids.add(0, new DesfireApplicationId()); // add default

                // 			DesfireApplicationKeySettings desfireApplicationKeySettings = mifare_desfire_get_key_settings(tag);
                applications = new ArrayList<DesfireApplication>();

                for (DesfireApplicationId aid : aids) {
                    DesfireApplication desfireApplication = new DesfireApplication();
                    desfireApplication.setId(aid.getId());

                    applications.add(desfireApplication);

                    Log.d(TAG, "Found application " + aid);

                    if (mifare_desfire_select_application(tag, aid) == 0) {
                        Log.d(TAG, "Selected application " + Utils.getHexString(aid.getId()));

                        // this will fail when the directory listing of the application needs to get an authentication with key 0
                        //desfireApplication.setKeySettings(mifare_desfire_get_key_settings(tag));
                        DesfireApplicationKeySettings keySettings1 = null;
                        try {
                            keySettings1 = mifare_desfire_get_key_settings(tag);
                        } catch (Exception e) {
                            Log.e(TAG, "Exception on mifare_desfire_get_key_settings: " + e.getMessage());
                            // Exception on mifare_desfire_get_key_settings: PICC error code: ae
                            // this means we do need to authenticate FIRST before we are trying to read the PICC/this AID
                        }

                        desfireApplication.setKeySettings(keySettings1);

                        DesfireApplicationKeySettings keySettings = desfireApplication.getKeySettings();

                        Log.d(TAG, keySettings.toString());
                    }
                }

                desfireTag.setApplications(applications);

                showApplicationFragment(applications);

                tagPresenceScanner.resumeDelayed();
            } else {
                Log.d(TAG, "Did not find any applications");
            }
        } catch (Exception e) {
            Log.d(TAG, "Problem running commands", e);
        } finally {

        }
    }

    @Override
    public void onBackPressed() {
        //FragmentManager fragmentManager = getFragmentManager();
        androidx.fragment.app.FragmentManager fragmentManager = getSupportFragmentManager();

        int count = fragmentManager.getBackStackEntryCount();
        Log.d(TAG, "onBackPressed " + count);
        if (count == 1) {
            this.finish();
        } else {
            if (count == 2) {
                setBackgroundColor(R.color.white);
            }
            fragmentManager.popBackStack();
        }


    }

    private void setBackgroundColor(final int color) {
        MainActivity.this.runOnUiThread(new Runnable() {
            public void run() {
                View view = findViewById(R.id.rootLayout);
                view.setBackgroundColor(getResources().getColor(color));
            }
        });
    }

	/* new with AppCompatActivity
		final ApplicationNewFragment newFragment = new ApplicationNewFragment();
		Fragment fragment = new ApplicationNewFragment();
		getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
		*/

    private void showApplicationFragment(final List<DesfireApplication> applications) {
        Log.d(TAG, "showApplicationFragment");

        final ApplicationListFragment newFragment = new ApplicationListFragment();
        newFragment.setApplications(applications);


        //FragmentManager fragmentManager = getFragmentManager();
        // Create new fragment and transaction
        //ApplicationListFragment newFragment = new ApplicationListFragment();
        //newFragment.setApplications(applications);
        newFragment.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemClick " + position + " for " + id);

                int parentId = parent.getId();

                if (parentId == R.id.listView) {
                    application = applications.get(position);

                    Log.d(TAG, "Click on application " + application.getIdString());

                    MainActivity.this.authenticatedKey = null;

                    try {

                        if (tag.getSelectedApplication() != application.getIdInt()) {

                            if (!isConnected()) {
                                Log.d(TAG, "Tag lost wanting to change application");

                                onTagLost();

                                return;
                            }

                            try {
                                if (mifare_desfire_select_application(tag, new DesfireApplicationId(application.getId())) != 0) {
                                    Log.d(TAG, "Unable to select application");
                                }
                            } catch (Exception e) {
                                Log.d(TAG, "Problem selecting app " + application.getIdString(), e);

                                return;
                            }
                        }

                        if (!application.hasKeys()) {
                            if (!isConnected()) {
                                Log.d(TAG, "Tag lost wanting to get keys");

                                onTagLost();

                                return;
                            }

                            Log.d(TAG, "Get application keys");
                            DesfireKeyType type = application.getKeySettings().getType();
                            for (int i = 0; i < application.getKeySettings().getMaxKeys(); i++) {

                                try {
                                    byte version = mifare_desfire_get_key_version(tag, (byte) i);

                                    application.add(new DesfireApplicationKey(i, DesfireKey.newInstance(type, version)));
                                } catch (IllegalArgumentException e) {
                                    // assume no key set
                                }
                            }
                        } else {
                            Log.d(TAG, "Already read key versions");
                        }

                        if (application.getIdInt() != 0) {

                            if (!application.hasFiles()) {
                                if (!isConnected()) {
                                    Log.d(TAG, "Tag lost wanting to read application files");

                                    onTagLost();

                                    return;
                                }

                                readApplicationFiles();
                            } else {
                                Log.d(TAG, "Already read file settings");
                            }

                        }

                        showApplicationFragment();

                    } catch (Exception e) {
                        Log.d(TAG, "Problem selecting app " + application.getIdString(), e);
                    }
                }

            }

            private boolean readApplicationFiles() throws Exception {
                Log.d(TAG, "Get application files");

                DesfireApplicationKeySettings keySettings = application.getKeySettings();

                Log.d(TAG, keySettings.toString());

                if (keySettings.isRequiresMasterKeyForDirectoryList()) {
                    final List<DesfireApplicationKey> keys = application.getKeys();

                    final DesfireApplicationKey root = keys.get(0);

                    showKeySelector(keySettings.getType(), new OnKeyListener() {

                        @Override
                        public void onKey(DesfireKey key) {
                            if (!isConnected()) {
                                Log.d(TAG, "Tag lost wanting to select application");

                                onTagLost();

                                return;
                            }

                            try {
                                DesfireApplicationKey clone = new DesfireApplicationKey(root.getIndex(), key);

                                if (authenticate(clone)) {
                                    MainActivity.this.authenticatedKey = clone;

                                    readApplicationFiles();

                                    showApplicationFragment();

                                    showToast(R.string.applicationAuthenticatedSuccess);
                                } else {
                                    showToast(R.string.applicationAuthenticatedFail);
                                }

                            } catch (Exception e) {
                                Log.d(TAG, "Unable to authenticate", e);

                                showToast(R.string.applicationAuthenticatedFail);
                            }

                        }
                    });

                } else {
                    Log.d(TAG, "Can list files");
                }

                Log.d(TAG, "Get files ids");
                byte[] ids = mifare_desfire_get_file_ids(tag);

                if (ids != null) {
                    Log.d(TAG, "Got " + ids.length + " files");

                    for (int i = 0; i < ids.length; i++) {
                        DesfireFile settings = mifare_desfire_get_file_settings(tag, ids[i]);

                        Log.d(TAG, "File setting " + i + ": " + settings);

                        application.add(settings);
                    }
                } else {
                    Log.d(TAG, "Unable to get files ids");
                }

                return true;
            }

        });

        androidx.fragment.app.FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack
        //Fragment fragment = new ApplicationNewFragment();
        transaction.replace(R.id.content, newFragment, "applications");
        transaction.addToBackStack("applications");
        // Commit the transaction
        transaction.commit();
    }

    protected void onTagLost() {
        showShortToast(R.string.tagStatusLost);

        setBackgroundColor(R.color.redWhite);
    }

    private void showMainFragment() {
        Log.d(TAG, "showMainFragment");

        // Create new fragment and transaction
        final MainFragment newFragment = new MainFragment();
        //getSupportFragmentManager().beginTransaction().replace(R.id.content, newFragment).commit();

        androidx.fragment.app.FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack
        //Fragment fragment = new ApplicationNewFragment();
        transaction.replace(R.id.content, newFragment, "main");
        transaction.addToBackStack("main");
        // Commit the transaction
        transaction.commit();
    }

    private void showApplicationFragment() {
        Log.d(TAG, "showApplicationFragment");

        // Create new fragment and transaction
        final FileListFragment newFragment = new FileListFragment();
        newFragment.setApplication(application);
        newFragment.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemClick " + position + " for " + id);

                ApplicationDetail applicationDetail = newFragment.getApplicationDetail(position);

                if (applicationDetail instanceof ApplicationDetailFile) {
                    ApplicationDetailFile file = (ApplicationDetailFile) applicationDetail;

                    final DesfireFile desfireFile = file.getFile();

                    Log.d(TAG, "Select file " + desfireFile);

                    if (desfireFile.isContent()) {
                        Log.d(TAG, "Already read file content");

                        showFileFragment(desfireFile);

                        return;
                    }

                    if (!isConnected()) {
                        onTagLost();

                        return;
                    }

                    if (!desfireFile.isFreeReadWriteAccess()) {
                        if (authenticatedKey != null) {
                            Log.d(TAG, "Already authenticated using key " + authenticatedKey.getIndex());

                            if (desfireFile.freeReadAccess() || desfireFile.isReadAccess(authenticatedKey.getIndex())) {
                                Log.d(TAG, "Already authenticated with read file access");

                                if (!desfireFile.freeReadAccess()) {
                                    try {
                                        if (authenticate(authenticatedKey)) {
                                            readFile(desfireFile);
                                        }
                                    } catch (Exception e) {
                                        Log.d(TAG, "Unable to authenticate", e);

                                        showToast(R.string.applicationAuthenticatedFail);
                                    }
                                } else {
                                    readFile(desfireFile);
                                }

                                showFileFragment(desfireFile);

                                return;
                            }
                        }

                        showKeyNumber(desfireFile, new OnKeyNumberListener() {

                            @Override
                            public void onKeyNumber(int index, final String access) {

                                if (!isConnected()) {
                                    onTagLost();

                                    return;
                                }

                                final DesfireApplicationKey desfire = application.getKeys().get(index);

                                showKeySelector(application.getKeySettings().getType(), new OnKeyListener() {

                                    @Override
                                    public void onKey(DesfireKey key) {
                                        try {
                                            DesfireApplicationKey clone = new DesfireApplicationKey(desfire.getIndex(), key);

                                            if (authenticate(clone)) {
                                                MainActivity.this.authenticatedKey = clone;

                                                if (desfireFile.freeReadAccess() || access.contains("R")) {
                                                    readFile(desfireFile);
                                                }

                                                showFileFragment(desfireFile);

                                                showToast(R.string.applicationAuthenticatedSuccess);
                                            } else {
                                                showToast(R.string.applicationAuthenticatedFail);
                                            }

                                        } catch (Exception e) {
                                            Log.d(TAG, "Unable to authenticate", e);

                                            showToast(R.string.applicationAuthenticatedFail);
                                        }

                                    }
                                });

                            }
                        });


                    } else {
                        try {
                            readFile(desfireFile);

                            showFileFragment(desfireFile);
                        } catch (Exception e) {
                            Log.d(TAG, "Problem reading file", e);
                        }

                    }

                } else if (applicationDetail instanceof ApplicationDetailApplicationKey) {
                    ApplicationDetailApplicationKey key = (ApplicationDetailApplicationKey) applicationDetail;

                    final DesfireApplicationKey desfire = key.getKey();

                    Log.d(TAG, "Select key " + desfire);

                    DesfireKey desfireKey = desfire.getDesfireKey();

                    DesfireKeyType type = desfireKey.getType();

                    showKeySelector(type, new OnKeyListener() {

                        @Override
                        public void onKey(DesfireKey key) {
                            if (!isConnected()) {
                                Log.d(TAG, "Tag lost wanting to change application");

                                onTagLost();

                                return;
                            }

                            try {
                                DesfireApplicationKey clone = new DesfireApplicationKey(desfire.getIndex(), key);

                                if (authenticate(clone)) {
                                    MainActivity.this.authenticatedKey = clone;

                                    showToast(R.string.applicationAuthenticatedSuccess);
                                } else {
                                    showToast(R.string.applicationAuthenticatedFail);
                                }
                            } catch (Exception e) {
                                Log.d(TAG, "Unable to authenticate", e);

                                showToast(R.string.applicationAuthenticatedFail);
                            }

                        }
                    });
                }

            }

        });

        // added
        // avoid to change the settings for master application
        /*
        if (application.getIdString().equals("000000")) {
            newFragment.setButtonEnabled(false);
        } else {
            newFragment.setButtonEnabled(true);
        }

         */

        newFragment.setOnButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // this is for changing the application key settings
                showApplicationKeySettingsChangeFragment();
            }
        });

        newFragment.setOnChangeKeyButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // this is for changing the application key settings
                showApplicationKeyChangeFragment();
            }
        });

        androidx.fragment.app.FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack
        transaction.replace(R.id.content, newFragment, "application");
        transaction.addToBackStack("application");

        // Commit the transaction
        transaction.commit();
    }

    // added
    private void showApplicationNewFragment() {
        Log.d(TAG, "showApplicationNewFragment");

        // Create new fragment and transaction
        final ApplicationNewFragment newFragment = new ApplicationNewFragment();
        //final FileListFragment newFragment = new FileListFragment();

        //newFragment.setApplication(application);

		/* new with AppCompatActivity
		final ApplicationNewFragment newFragment = new ApplicationNewFragment();
		Fragment fragment = new ApplicationNewFragment();
		getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
		*/

        newFragment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("*** createApplication pressed");

                // sanity checks on AID
                String inputAid = newFragment.getAid();
                if (TextUtils.isEmpty(inputAid)) {
                    newFragment.setLogData("please enter a 6 hex character long string");
                    return;
                }
                if (inputAid.length() != 6) {
                    newFragment.setLogData("please enter a 6 hex character long string");
                    return;
                }
                byte[] aidByte;
                aidByte = Utils.hexStringToByteArray(inputAid);
                if (aidByte == null) {
                    newFragment.setLogData("please enter a 6 hex character long string");
                    return;
                }
                System.out.println("aidByte: " + Utils.getHexString(aidByte));
                String typeOfKeys = newFragment.getChoiceTypeOfKeys();
                int numberOfKeysWithoutKeyType = newFragment.getNumberOfKeys();
                int numberOfKeysWithKeyType;
                if (typeOfKeys.equals("DES")) {
                    numberOfKeysWithKeyType = numberOfKeysWithoutKeyType | APPLICATION_CRYPTO_DES;
                } else if (typeOfKeys.equals("TDES")) {
                    numberOfKeysWithKeyType = numberOfKeysWithoutKeyType | APPLICATION_CRYPTO_3K3DES;
                } else if (typeOfKeys.equals("AES")) {
                    numberOfKeysWithKeyType = numberOfKeysWithoutKeyType | APPLICATION_CRYPTO_AES;
                } else {
                    newFragment.setLogData("The key type is undefined");
                    return;
                }

                // com.github.skjolber.desfire.libfreefare.MifareDesfire.java

                // select master application
                //public static int mifare_desfire_select_application (MifareTag tag, DesfireApplicationId aid) throws Exception
                int selectMasterApplicationResult = -99;
                try {
                    selectMasterApplicationResult = mifare_desfire_select_application(tag, null);
                } catch (Exception e) {
                    // throw new RuntimeException(e);
                    newFragment.setLogData("selectMasterApplication error: " + e.getMessage());
                    return;
                }
                if (selectMasterApplicationResult == 0) {
                    newFragment.setLogData("selectMasterApplication success");
                } else {
                    newFragment.setLogData("selectMasterApplication failure: " + selectMasterApplicationResult);
                    return;
                }

                // how to authenticate for create an application ?
                if (application == null) {
                    System.out.println("application is null");

                    //applications = new ArrayList<DesfireApplication>();

                    //DesfireApplication desfireApplication = new DesfireApplication();
                    //desfireApplication.setId(new byte[3]); // master application
                    //applications.add(desfireApplication);
                }
                application = applications.get(0); // master application
                System.out.println("*** application: " + application.toString());
                System.out.println("*** application idString: " + application.getIdString());
                System.out.println("*** application hasKeys: " + application.hasKeys());


                DesfireApplicationKeySettings keySettings = application.getKeySettings();
                Log.d(TAG, keySettings.toString());
                if (keySettings.isRequiresMasterKeyForDirectoryList()) {
                    final List<DesfireApplicationKey> keys = application.getKeys();
                    final DesfireApplicationKey root = keys.get(0);
                    showKeySelector(keySettings.getType(), new OnKeyListener() {
                        @Override
                        public void onKey(DesfireKey key) {
                            if (!isConnected()) {
                                Log.d(TAG, "Tag lost wanting to select application");
                                onTagLost();
                                return;
                            }
                            try {
                                DesfireApplicationKey clone = new DesfireApplicationKey(root.getIndex(), key);
                                if (authenticate(clone)) {
                                    MainActivity.this.authenticatedKey = clone;
                                    // todo run the code after auth here ?
                                    //readApplicationFiles();
                                    //showApplicationFragment();
                                    showToast(R.string.applicationAuthenticatedSuccess);
                                } else {
                                    showToast(R.string.applicationAuthenticatedFail);
                                }

                            } catch (Exception e) {
                                Log.d(TAG, "Unable to authenticate", e);
                                showToast(R.string.applicationAuthenticatedFail);
                            }
                        }
                    });
                } else {
                    Log.d(TAG, "Can't authenticate an application");
                }

                // line 628
                // public static int
                //	create_application (MifareTag tag, DesfireApplicationId aid, byte settings1, byte settings2, int want_iso_application,
                //	int want_iso_file_identifiers, /* uint16 */ int iso_file_id, byte[] iso_file_name, int iso_file_name_len) throws Exception
                // mifare_desfire_create_application_aes (MifareTag tag, DesfireApplicationId aid, byte settings, byte key_no) throws Exception
                //byte[] aid = new byte[]{(byte) 0x08, (byte) 0x08, (byte) 0x01};
                DesfireApplicationId desfireApplicationId = new DesfireApplicationId(aidByte);
                byte aidKeySettings = (byte) 0x0f; // fixed at the moment
                //byte numberOfKeys = (byte) 0x03;
                byte numberOfKeysByte = (byte) (numberOfKeysWithKeyType & 0xff);

                int result = -99;
                try {
                    if (typeOfKeys.equals("DES")) {
                        byte nbrOfKeysByte = (byte) (numberOfKeysWithoutKeyType & 0xff);
                        result = mifare_desfire_create_application(tag, desfireApplicationId, aidKeySettings, nbrOfKeysByte);
                    } else if (typeOfKeys.equals("TDES")) {
                        byte nbrOfKeysByte = (byte) (numberOfKeysWithoutKeyType & 0xff);
                        result = mifare_desfire_create_application_3k3des(tag, desfireApplicationId, aidKeySettings, nbrOfKeysByte);
                    } else if (typeOfKeys.equals("AES")) {
                        byte nbrOfKeysByte = (byte) (numberOfKeysWithoutKeyType & 0xff);
                        result = mifare_desfire_create_application_aes(tag, desfireApplicationId, aidKeySettings, nbrOfKeysByte);
                    }
                } catch (Exception e) {
                    //throw new RuntimeException(e);
                    newFragment.setLogData("createApplication error: " + e.getMessage());
                    return;
                }
                if (result == 0) {
                    newFragment.setLogData("createApplication success");
                } else {
                    newFragment.setLogData("createApplication failure: " + result);
                    return;
                }
            }
        });

        //getSupportFragmentManager().beginTransaction().replace(R.id.content, newFragment).commit();
        //FragmentTransaction transaction = getFragmentManager().beginTransaction();
        androidx.fragment.app.FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack
        //Fragment fragment = new ApplicationNewFragment();
        transaction.replace(R.id.content, newFragment, "addApplication");
        transaction.addToBackStack("addApplication");
        // Commit the transaction
        transaction.commit();

    }

    // added
    private void showFileNewFragment() {
        Log.d(TAG, "showFileNewFragment");

        // Create new fragment and transaction
        final FileNewFragment newFragment = new FileNewFragment(application);

        newFragment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("*** createFile pressed");

                // get the data from FileNewFragment
                // file number
                com.shawnlin.numberpicker.NumberPicker pickerFid = newFragment.getNpFileId();
                int fid = pickerFid.getValue();
                byte fileNumber = (byte) (fid & 0xff);

                // communication settings
                AutoCompleteTextView communicationSettingsSpinner = newFragment.getChoiceCommunicationSettings();
                String comChoice = communicationSettingsSpinner.getText().toString();
                byte commSett = (byte) 0x03;
                if (comChoice.equals("Plain")) {
                    commSett = (byte) 0x00;
                } else if (comChoice.equals("MACed")) {
                    commSett = (byte) 0x01;
                } else if (comChoice.equals("Encrypted")) {
                    commSett = (byte) 0x03;
                } else {
                    newFragment.setLogData("unsupported communication setting");
                    return;
                }

                // access rights
                int nrOfKeysInApplication = newFragment.getNrOfApplicationKeys();
                int keyForRw = newFragment.getNpKeyRw().getValue();
                int keyForCar = newFragment.getNpKeyCar().getValue();
                int keyForR = newFragment.getNpKeyR().getValue();
                int keyForW = newFragment.getNpKeyW().getValue();
                // sanity checks on key number
                // todo special handling for key numbers 14 = free access rights without key and 15 = no rights necessary
                if (keyForRw >= nrOfKeysInApplication) {
                    newFragment.setLogData("key for RW is too large, maximum is " + (nrOfKeysInApplication - 1)); // as they are numbered from 00 to 15
                    return;
                }
                if (keyForCar >= nrOfKeysInApplication) {
                    newFragment.setLogData("key for CAR is too large, maximum is " + (nrOfKeysInApplication - 1)); // as they are numbered from 00 to 15
                    return;
                }
                if (keyForR >= nrOfKeysInApplication) {
                    newFragment.setLogData("key for Read is too large, maximum is " + (nrOfKeysInApplication - 1)); // as they are numbered from 00 to 15
                    return;
                }
                if (keyForW >= nrOfKeysInApplication) {
                    newFragment.setLogData("key for Write is too large, maximum is " + (nrOfKeysInApplication - 1)); // as they are numbered from 00 to 15
                    return;
                }
                // get the new value
                //int accessRights = (keyForRw * 4096) + (keyForCar * 256) + (keyForR * 16) + (keyForW * 1);
                int accessRights = (keyForR * 4096) + (keyForW * 256) + (keyForRw * 16) + (keyForCar * 1);

                // standard fileSize
                int standardFileSize = Integer.parseInt(newFragment.getStandardFileSize().getText().toString());
                if ((standardFileSize < 1) || (standardFileSize > 256)) {
                    newFragment.setLogData("fileSize has to be in range of 1 to 256");
                    return;
                }

                // record fileSize
                int recordFileSize = Integer.parseInt(newFragment.getRecordFileSize().getText().toString());
                if ((recordFileSize < 1) || (recordFileSize > 256)) {
                    newFragment.setLogData("fileSize has to be in range of 1 to 256");
                    return;
                }

                // number of records
                int numberOfRecords = newFragment.getNpNrOfRecords().getValue();
                if ((numberOfRecords < 1) || (numberOfRecords > 10)) {
                    newFragment.setLogData("number of records has to be in range of 1 to 10");
                    return;
                }

                // value file parameters
                // lower limit
                int lowerLimit = newFragment.getLowerLimit();
                // upper limit
                int upperLimit = newFragment.getUpperLimit();
                // lower limit
                int value = newFragment.getValue();
                if (lowerLimit >= upperLimit) {
                    newFragment.setLogData("the upper limit has to be higher than the lower limit");
                    return;
                }
                if ((value >= lowerLimit) && (value <= upperLimit)) {
                    // everything is ok, value is in range lower limit <= value <= upper limit
                } else {
                    newFragment.setLogData("the value has to be in range of lower limit to upper limit");
                    return;
                }

                // select application
                //public static int mifare_desfire_select_application (MifareTag tag, DesfireApplicationId aid) throws Exception
                int selectApplicationResult = -99;
                byte[] selectedAid = newFragment.getApplication().getId();
                DesfireApplicationId desfireApplicationId = new DesfireApplicationId(selectedAid);
                try {
                    selectApplicationResult = mifare_desfire_select_application(tag, desfireApplicationId);
                } catch (Exception e) {
                    // throw new RuntimeException(e);
                    newFragment.setLogData("selectApplication error: " + e.getMessage());
                    return;
                }
                if (selectApplicationResult == 0) {
                    newFragment.setLogData("selectApplication success");
                } else {
                    newFragment.setLogData("selectApplication failure: " + selectApplicationResult);
                    return;
                }

                // how to authenticate for create a file ?
                if (application == null) {
                    System.out.println("application is null");
                }
                DesfireApplicationKeySettings keySettings = application.getKeySettings();
                Log.d(TAG, keySettings.toString());
                //if(keySettings.isRequiresMasterKeyForDirectoryList()) {
                final List<DesfireApplicationKey> keys = application.getKeys();
                final DesfireApplicationKey root = keys.get(0);
                showKeySelector(keySettings.getType(), new OnKeyListener() {
                    @Override
                    public void onKey(DesfireKey key) {
                        if (!isConnected()) {
                            Log.d(TAG, "Tag lost wanting to select application");
                            onTagLost();
                            return;
                        }
                        try {
                            DesfireApplicationKey clone = new DesfireApplicationKey(root.getIndex(), key);
                            if (authenticate(clone)) {
                                MainActivity.this.authenticatedKey = clone;
                                // todo run the code after auth here ?
                                //readApplicationFiles();
                                //showApplicationFragment();

                                showToast(R.string.applicationAuthenticatedSuccess);
                                // this throws an exception on creating a new value file:
                                // public static byte[] mifare_cryto_postprocess_data (MifareTag tag, byte[] data, int nbytes, int communication
                                // in MifareDesfireCrypto.java
                                // line 577


                            } else {
                                showToast(R.string.applicationAuthenticatedFail);
                            }

                        } catch (Exception e) {
                            Log.d(TAG, "Unable to authenticate", e);
                            showToast(R.string.applicationAuthenticatedFail);
                        }
                    }
                });

// line 628
                // public static int

                // mifare_desfire_create_std_data_file (MifareTag tag, byte file_no, byte communication_settings, int access_rights, int file_size) throws Exception


                //byte communicationSettings = (byte) 0x03;
				/*
				if (communicationSetting == CommunicationSetting.Plain) communicationSettings = (byte) 0x00;
        		if (communicationSetting == CommunicationSetting.MACed) communicationSettings = (byte) 0x01;
        		if (communicationSetting == CommunicationSetting.Encrypted) communicationSettings = (byte) 0x03;
				 */
                //int accessRights = 18; // 0x00 0x12 = Read&Write Access & ChangeAccessRights | Read Access & Write Access


                // for value file
                //mifare_desfire_create_value_file (MifareTag tag, byte file_no, byte communication_settings, short access_rights,
                // int lower_limit, int upper_limit, int value, byte limited_credit_enable) throws Exception

                // for cyclic file:
                // mifare_desfire_create_cyclic_record_file (MifareTag tag, byte file_no, byte communication_settings, short access_rights,
                // int record_size, int max_number_of_records) throws Exception


                int result = -99;
                try {
                    // the chosen method depends on the type file to be created
                    if (newFragment.isRbStandardChecked()) {
                        Log.d(TAG, "create a standard file");
                        result = mifare_desfire_create_std_data_file(tag, fileNumber, commSett, accessRights, standardFileSize);
                    } else if (newFragment.isRbValueChecked()) {
                        Log.d(TAG, "create a value file");
                        System.out.println("*** create a value file with initial value " + value);
                        // limited credit is disabled
                        result = mifare_desfire_create_value_file(tag, fileNumber, commSett, (short) accessRights, lowerLimit, upperLimit, value, (byte) (0x00));
                        System.out.println("create value file result: " + result);
                    } else if (newFragment.isRbRecordChecked()) {
                        Log.d(TAG, "create a linear record file");
                        System.out.println("*** create a linear record file recordSize " + recordFileSize + " numberOfRecords " + numberOfRecords);
                        result = mifare_desfire_create_linear_record_file(tag, fileNumber, commSett, accessRights, recordFileSize, numberOfRecords);
                    } else if (newFragment.isRbCyclicChecked()) {
                        Log.d(TAG, "create a cyclic record file");
                        System.out.println("*** create a cyclic file recordSize " + recordFileSize + " numberOfRecords " + numberOfRecords);
                        result = mifare_desfire_create_cyclic_record_file(tag, fileNumber, commSett, accessRights, recordFileSize, numberOfRecords);

                    }
                } catch (Exception e) {
                    //throw new RuntimeException(e);
                    newFragment.setLogData("createFile error: " + e.getMessage());
                    return;
                }
                if (result == 0) {
                    newFragment.setLogData("createFile success");
                } else {
                    newFragment.setLogData("createFile failure: " + result);
                    return;
                }
            }
        });

        //FragmentTransaction transaction = getFragmentManager().beginTransaction();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack
        transaction.replace(R.id.content, newFragment, "addFile");
        transaction.addToBackStack("addFile");
        // Commit the transaction
        transaction.commit();
    }

    // added
    private void showApplicationKeySettingsChangeFragment() {
        Log.d(TAG, "showApplicationKeySettingsChangeFragment");

        // Create new fragment and transaction
        final ApplicationKeySettingsChangeFragment newFragment = new ApplicationKeySettingsChangeFragment(application);
        System.out.println("*** applicationID: " + Utils.getHexString(application.getId()));
        System.out.println("*** Master    AID: " + Utils.getHexString(MASTER_APPLICATION_ID));
        if (!Arrays.equals(application.getId(), MASTER_APPLICATION_ID)) {
            newFragment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    System.out.println("*** change application key settings pressed");

                    // get the data from ApplicationKeySettingsChangeFragment
                    byte newKeySettings = newFragment.getKeySettingsChanged();

                    // to authenticate for change key settings we need to use the key number placed in key settings
                    int carKeyNumber = newFragment.getKeyNumberForAccessRightChangeExisting();

                    Log.d(TAG, "request to use car key " + carKeyNumber + " for change Application Key Settings to " + newKeySettings);

                    DesfireApplicationKeySettings keySettings = application.getKeySettings();
                    Log.d(TAG, keySettings.toString());
                    //if(keySettings.isRequiresMasterKeyForDirectoryList()) {
                    final List<DesfireApplicationKey> keys = application.getKeys();
                    final DesfireApplicationKey root = keys.get(carKeyNumber);
                    showKeySelector(keySettings.getType(), new OnKeyListener() {
                        @Override
                        public void onKey(DesfireKey key) {
                            if (!isConnected()) {
                                Log.d(TAG, "Tag lost wanting to select application");
                                onTagLost();
                                return;
                            }
                            try {
                                DesfireApplicationKey clone = new DesfireApplicationKey(root.getIndex(), key);
                                if (authenticate(clone)) {
                                    MainActivity.this.authenticatedKey = clone;

                                    showToast(R.string.applicationAuthenticatedSuccess);

                                    int result = mifare_desfire_change_key_settings(tag, newKeySettings);
                                    showToastShortToast("change application key settings result: " + result);
                                    newFragment.setLogData("change application key settings result: " + result);

                                } else {
                                    showToast(R.string.applicationAuthenticatedFail);
                                }

                            } catch (Exception e) {
                                Log.d(TAG, "Unable to authenticate", e);
                                showToast(R.string.applicationAuthenticatedFail);
                            }
                        }
                    });

                }
            });
        }

        //FragmentTransaction transaction = getFragmentManager().beginTransaction();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack
        transaction.replace(R.id.content, newFragment, "changeApplicationKeySettings");
        transaction.addToBackStack("changeApplicationKeySettings");
        // Commit the transaction
        transaction.commit();
    }

    // added
    private void showApplicationKeyChangeFragment() {
        Log.d(TAG, "showApplicationKeyChangeFragment");

        // Create new fragment and transaction
        final ApplicationKeyChangeFragment newFragment = new ApplicationKeyChangeFragment(application);
        System.out.println("*** applicationID: " + Utils.getHexString(application.getId()));
        System.out.println("*** Master    AID: " + Utils.getHexString(MASTER_APPLICATION_ID));
        if (!Arrays.equals(application.getId(), MASTER_APPLICATION_ID)) {
            newFragment.setButtonListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    // 1 we do need to know the key number to get changed
                    // 2 get the old key value from store
                    // 3 get the new key from store
                    // 4 authenticate with the old key
                    // 5 send the change key command to PICC
                    System.out.println("*** newKey: " + newFragment.getChangeKeyNewKey());
                    DesfireKey desfireOldKey = newFragment.getOldDesfireKeyForChanging();
                    DesfireKey desfireNewKey = newFragment.getNewDesfireKeyForChanging();
                    if (desfireOldKey == null) {
                        newFragment.setLogData("Please select the old key");
                        return;
                    }
                    if (desfireNewKey == null)  {
                        newFragment.setLogData("Please select the new key");
                        return;
                    }

                    byte[] oldKey = hexStringToByteArray(newFragment.getChangeKeyOldKey());
                    byte[] newKey = hexStringToByteArray(newFragment.getChangeKeyNewKey());
                    if (oldKey == null) {
                        newFragment.setLogData("Please select the old key");
                        return;
                    }
                    if (newKey == null)  {
                        newFragment.setLogData("Please select the new key");
                        return;
                    }
                    //byte[] oldKey = newFragment.getOldKeyForChanging();
                    //byte[] newKey = newFragment.getNewKeyForChanging();
                    byte keyNumberForChanging = newFragment.getKeyNumberForChanging();
                    Log.d(TAG, "keyNumberForChanging: " + keyNumberForChanging);
                    // todo selectKey return MifareDesfireKey and not byte[]

                    DesfireApplicationKeySettings keySettings = application.getKeySettings();
                    Log.d(TAG, keySettings.toString());
                    //if(keySettings.isRequiresMasterKeyForDirectoryList()) {
                    final List<DesfireApplicationKey> keys = application.getKeys();
                    final DesfireApplicationKey root = keys.get(0);
                    showKeySelector(keySettings.getType(), new OnKeyListener() {
                        @Override
                        public void onKey(DesfireKey key) {
                            if (!isConnected()) {
                                Log.d(TAG, "Tag lost wanting to select application");
                                onTagLost();
                                return;
                            }
                            try {
                                DesfireApplicationKey clone = new DesfireApplicationKey(root.getIndex(), key);
                                if (authenticate(clone)) {
                                    MainActivity.this.authenticatedKey = clone;
                                    // todo run the code after auth here ?
                                    //readApplicationFiles();
                                    //showApplicationFragment();

                                    showToast(R.string.applicationAuthenticatedSuccess);

                                    try {
                                        int result = mifare_desfire_change_key(tag, keyNumberForChanging, desfireNewKey, desfireOldKey);
                                        newFragment.setLogData("change key result: " + result);
                                    } catch (Exception e) {
                                        newFragment.setLogData("Error on change key: " + e.getMessage());
                                        e.printStackTrace();
                                    }

                                } else {
                                    showToast(R.string.applicationAuthenticatedFail);
                                }

                            } catch (Exception e) {
                                Log.d(TAG, "Unable to authenticate", e);
                                showToast(R.string.applicationAuthenticatedFail);
                            }
                        }
                    });






                }
            });
        }

        //FragmentTransaction transaction = getFragmentManager().beginTransaction();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack
        transaction.replace(R.id.content, newFragment, "changeApplicationKey");
        transaction.addToBackStack("changeApplicationKey");
        // Commit the transaction
        transaction.commit();
    }


    private String getName(DesfireKeyType type) {
        switch (type) {
            case TDES:
                return getString(R.string.applicationAuthenticateKey3DES);
            case TKTDES:
                return getString(R.string.applicationAuthenticateKey3K3DES);
            case AES:
                return getString(R.string.applicationAuthenticateKeyAES);
            case DES:
                return getString(R.string.applicationAuthenticateKeyDES);
            default:
                throw new IllegalArgumentException();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        //inflater.inflate(R.menu.main, menu);
        inflater.inflate(R.menu.main_add_application, menu); // changed

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        for (int i = 0; i < menu.size(); i++) {
            menu.getItem(i).setVisible(false);
        }

        MenuItem keys = menu.findItem(R.id.action_settings);
        MenuItem addKey = menu.findItem(R.id.action_add);
        MenuItem addSampleKeys = menu.findItem(R.id.action_sample_keys);
        MenuItem save = menu.findItem(R.id.action_save);
        MenuItem addApplication = menu.findItem(R.id.action_add_app); // added
        MenuItem addFile = menu.findItem(R.id.action_add_file); // added
        MenuItem freeMemory = menu.findItem(R.id.action_free_memory); // added
        MenuItem formatPicc = menu.findItem(R.id.action_format_picc); // added

        // set freeMemory always visible
        freeMemory.setVisible(true);

        //FragmentManager fragmentManager = getFragmentManager();
        //String name = fragmentManager.getBackStackEntryAt(fragmentManager.getBackStackEntryCount() - 1).getName();

        androidx.fragment.app.FragmentManager fragmentManager = getSupportFragmentManager();
        String name = fragmentManager.getBackStackEntryAt(fragmentManager.getBackStackEntryCount() - 1).getName();

        if (name != null && name.equals("keys")) {
            keys.setVisible(false);
            addKey.setVisible(true);
            addSampleKeys.setVisible(true);
            addApplication.setVisible(false); // added
            addFile.setVisible(false); // added

        } else {
            keys.setVisible(true);
            addKey.setVisible(false);
            addApplication.setVisible(false); // added
            addFile.setVisible(false); // added
        }

        // added
        if (name != null && name.equals("applications")) {
            //keys.setVisible(false);
            //addKey.setVisible(true);
            addApplication.setVisible(true); // added
            addFile.setVisible(false); // added
            freeMemory.setVisible(true); // added
            formatPicc.setVisible(true); // added
        } else {
            //keys.setVisible(true);
            //addKey.setVisible(false);
            addApplication.setVisible(false); // added
            addFile.setVisible(false); // added
        }
        System.out.println("*** name: " + fragmentManager.getBackStackEntryAt(fragmentManager.getBackStackEntryCount() - 1).getName());
        if (name != null && name.equals("application")) {
            addFile.setVisible(true); // added
        } else {
            addFile.setVisible(false); // added
        }

        Log.d(TAG, "Prepare options menu for " + name);
        if (name != null && name.equals("file")) {
            //getFragmentManager().executePendingTransactions();
            getSupportFragmentManager().executePendingTransactions();

            FileFragment fragment = (FileFragment) fragmentManager.findFragmentByTag("file");

            DesfireFile file = fragment.getFile();

            if (file instanceof ValueDesfireFile) {
                save.setVisible(false);
            } else {
                save.setVisible(file.isContent());
            }
        } else {
            save.setVisible(false);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            showKeysFragment();
            return true;
        } else if (item.getItemId() == R.id.action_add) {
            addKey();
            return true;
        } else if (item.getItemId() == R.id.action_sample_keys) {
            setupSampleKeys();
            return true;
        } else if (item.getItemId() == R.id.action_save) {
            saveFileData();
            return true;
        } else if (item.getItemId() == R.id.action_add_app) { // added
            showApplicationNewFragment();
            return true;
        } else if (item.getItemId() == R.id.action_add_file) { // added
            showFileNewFragment();
            return true;
        } else if (item.getItemId() == R.id.action_free_memory) { // added
            showFreeMemory();
            return true;
        } else if (item.getItemId() == R.id.action_format_picc) { // added
            formatPicc();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }

		/*
		switch (item.getItemId()) {
    	case R.id.action_settings: {
    		showKeysFragment();
    		return true;
    	}
    	case R.id.action_add: {
    		addKey();
    		return true;
    	}
    	case R.id.action_save : {
    		saveFileData();
    		return true;
    	}
    	default:
    		return super.onOptionsItemSelected(item);
    	}
		*/
    }

    // added
    private void showFreeMemory() {
        int memory = 0;
        try {
            memory = mifare_desfire_get_free_memory(tag);
        } catch (Exception e) {
            showToastShortToast("Error when get the free memory on card");
        }
        showToastShortToast("The free memory on card is " + String.valueOf(memory) + " bytes");
    }

    // added

    /**
     * This method will setup keys for all key types (DES, 3DES, 3K3DES and AES)
     * It will add 3 keys (nr 0, 1 and 2) for each type
     * The naming will get this structure (e.g. for DES):
     * DES
     * 0 RW
     * 1 R
     * 2 W
     * The keys will get these lengths:
     * DES = 8 bytes long, 3DES = 16 bytes long, 3K3DES = 24 bytes long and AES = 16 bytes long
     * The first byte will get these hex bytes:
     * DES = 0xD1, 3DES = 0xD3, 3K3DES = 0xD4, AES = 0xA1
     * The byte is set due to key number
     * key 0 (RW) = 0xF0, 1 (R) = 0xF1, 2 (W) = 0xF2
     * The following bytes will be filled with 0x00's
     */
    private void setupSampleKeys() {
        int numberOfKeys = 4;
        int lengthDes = 8;
        int length3Des = 16;
        int length3K3Des = 24;
        int lengthAes = 16;
/*
		// des
		for (int i = 0; i < numberOfKeys; i++) {
			byte[] key = new byte[lengthDes];
			key[0] = (byte) (0xD1);
			key[1] = (byte) ((byte)(0xF0) | (byte) (i & 0xff));
			StringBuilder sb = new StringBuilder();
			sb.append("3DES ").append(i);
			if (i == 0) {
				sb.append(" RW");
			} else if (i == 1) {
				sb.append(" R");
			} else if (i == 2) {
				sb.append(" W");
			} else {
				sb.append(" unknown");
			}
			DesfireKey desfireKey = DesfireKey.newInstance(DesfireKeyType.DES, Integer.parseInt("EE", 16));
			desfireKey.setName(sb.toString());
			desfireKey.setValue(key);
			try {
				MainApplication.getInstance().getDataSource().createKey(desfireKey);
			} catch (IOException e) {
				Log.d(TAG, "Problem adding key", e);
			}
		} // DES

		// 3des
		for (int i = 0; i < numberOfKeys; i++) {
			byte[] key = new byte[length3Des];
			key[0] = (byte) (0xD3);
			key[1] = (byte) ((byte)(0xF0) | (byte) (i & 0xff));
			StringBuilder sb = new StringBuilder();
			sb.append("DES ").append(i);
			if (i == 0) {
				sb.append(" RW");
			} else if (i == 1) {
				sb.append(" R");
			} else if (i == 2) {
				sb.append(" W");
			} else {
			} else {
				sb.append(" unknown");
			}
			DesfireKey desfireKey = DesfireKey.newInstance(DesfireKeyType.TDES, Integer.parseInt("EE", 16));
			desfireKey.setName(sb.toString());
			desfireKey.setValue(key);
			try {
				MainApplication.getInstance().getDataSource().createKey(desfireKey);
			} catch (IOException e) {
				Log.d(TAG, "Problem adding key", e);
			}
		} // 3DES

		// 3K3des
		for (int i = 0; i < numberOfKeys; i++) {
			byte[] key = new byte[length3K3Des];
			key[0] = (byte) (0xD4);
			key[1] = (byte) ((byte)(0xF0) | (byte) (i & 0xff));
			StringBuilder sb = new StringBuilder();
			sb.append("3K3DES ").append(i);
			if (i == 0) {
				sb.append(" RW");
			} else if (i == 1) {
				sb.append(" R");
			} else if (i == 2) {
				sb.append(" W");
			} else {
				sb.append(" unknown");
			}
			DesfireKey desfireKey = DesfireKey.newInstance(DesfireKeyType.TKTDES, Integer.parseInt("EE", 16));
			desfireKey.setName(sb.toString());
			desfireKey.setValue(key);
			try {
				MainApplication.getInstance().getDataSource().createKey(desfireKey);
			} catch (IOException e) {
				Log.d(TAG, "Problem adding key", e);
			}
		} // 3K3DES
*/
        // aes
        for (int i = 0; i < numberOfKeys; i++) {
            byte[] key = new byte[lengthAes];
            key[0] = (byte) (0xA1);
            key[1] = (byte) ((byte) (0xF0) | (byte) (i & 0xff));
            StringBuilder sb = new StringBuilder();
            sb.append("AES ").append(i);
            if (i == 0) {
                sb.append(" RW");
            } else if (i == 1) {
                sb.append(" CAR");
            } else if (i == 2) {
                sb.append(" R");
            } else if (i == 3) {
                sb.append(" W");
            } else {
                sb.append(" unknown");
            }
            DesfireKey desfireKey = DesfireKey.newInstance(DesfireKeyType.AES, Integer.parseInt("00", 16));
            desfireKey.setName(sb.toString());
            desfireKey.setValue(key);
            try {
                MainApplication.getInstance().getDataSource().createKey(desfireKey);
            } catch (IOException e) {
                Log.d(TAG, "Problem adding key", e);
            }
        } // AES

    }

    private void addKey() {
        //KeyListFragment fragment = (KeyListFragment) getFragmentManager().findFragmentByTag("keys");
        KeyListFragment fragment = (KeyListFragment) getSupportFragmentManager().findFragmentByTag("keys");
        //this.dataSource = MainApplication.getInstance().getDataSource();
        fragment.setDataSource(MainApplication.getInstance().getDataSource());
        fragment.showAddKey(null);

    }

    private void saveFileData() {
        String fragTag = "saveFileData";

        // Get an instance supplying a default extension, captions and
        // icon appropriate to the calling application/activity.
        FileSaveFragment fsf = FileSaveFragment.newInstance("bin",
                R.string.fileSaveDialogOk,
                R.string.fileSaveDialogCancel,
                R.string.fileSaveDialogSaveAs,
                R.string.fileSaveDialogHintFilenameUnadorned,
                android.R.drawable.ic_menu_save);
        //fsf.show(getFragmentManager(), fragTag);
        fsf.show(getSupportFragmentManager(), fragTag);

        this.callbacks = new FileCallbacks();
    }

    private void showKeysFragment() {
        Log.d(TAG, "showKeysFragment");

        // Create new fragment and transaction
        final KeyListFragment newFragment = new KeyListFragment();
        newFragment.setContext(this);
        newFragment.setDataSource(MainApplication.getInstance().getDataSource());

        newFragment.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemClick " + position + " for " + id);

                ApplicationDetail applicationDetail = newFragment.getApplicationDetail(position);

                if (applicationDetail instanceof ApplicationDetailKey) {
                    ApplicationDetailKey key = (ApplicationDetailKey) applicationDetail;

                    DesfireKey desfire = key.getKey();

                    Log.d(TAG, "Show details for key " + desfire);

                    //KeyListFragment fragment = (KeyListFragment) getFragmentManager().findFragmentByTag("keys");
                    KeyListFragment fragment = (KeyListFragment) getSupportFragmentManager().findFragmentByTag("keys");
                    fragment.setDataSource(MainApplication.getInstance().getDataSource());
                    fragment.showAddKey(desfire);

                }

            }

        });
        //FragmentTransaction transaction = getFragmentManager().beginTransaction();
        androidx.fragment.app.FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack
        transaction.replace(R.id.content, newFragment, "keys");
        transaction.addToBackStack("keys");

        // Commit the transaction
        transaction.commit();
    }


    private boolean authenticate(DesfireApplicationKey desfireApplicationKey) throws Exception {

        DesfireKey key = desfireApplicationKey.getDesfireKey();

        Log.d(TAG, "Authenticate key " + (byte) desfireApplicationKey.getIndex());

        /* Authenticate with this key */
        switch (key.getType()) {
            case AES: {

                DesfireAESKey aesKey = (DesfireAESKey) key;

                MifareDESFireKey mifareDESFireKey = MifareDesfireKey1.mifare_desfire_aes_key_new_with_version(aesKey.getValue(), (byte) key.getVersion());

                int result = mifare_desfire_authenticate_aes(tag, (byte) desfireApplicationKey.getIndex(), mifareDESFireKey);

                if (result == 0) {
                    Log.d(TAG, "Authenticated AES using key " + key.getName() + " index " + (byte) desfireApplicationKey.getIndex());

                    return true;
                } else {
                    Log.d(TAG, "Unable to authenticate AES using key " + key.getName());
                }

                break;
            }
            case TKTDES: {

                Desfire3K3DESKey desfire3k3desKey = (Desfire3K3DESKey) key;

                MifareDESFireKey mifareDESFireKey = MifareDesfireKey1.mifare_desfire_3k3des_key_new(desfire3k3desKey.getValue());

                int result = mifare_desfire_authenticate_iso(tag, (byte) desfireApplicationKey.getIndex(), mifareDESFireKey);

                if (result == 0) {
                    Log.d(TAG, "Authenticated 3K3DES using key " + key.getName());

                    return true;
                } else {
                    Log.d(TAG, "Unable to authenticate 3K3DES using key " + key.getName());
                }

                break;
            }
            case TDES: {

                Desfire3DESKey desfire3desKey = (Desfire3DESKey) key;

                MifareDESFireKey mifareDESFireKey = MifareDesfireKey1.mifare_desfire_3des_key_new(desfire3desKey.getValue());

                MifareDesfireKey1.mifare_desfire_key_set_version(mifareDESFireKey, (byte) desfire3desKey.getVersion());

                int result = mifare_desfire_authenticate(tag, (byte) desfireApplicationKey.getIndex(), mifareDESFireKey);

                if (result == 0) {
                    Log.d(TAG, "Authenticated 3DES using key " + key.getName());

                    return true;
                } else {
                    Log.d(TAG, "Unable to authenticate 3DES using key " + key.getName());
                }

                break;
            }
            case DES: {

                DesfireDESKey desfireDesKey = (DesfireDESKey) key;

                MifareDESFireKey mifareDESFireKey = MifareDesfireKey1.mifare_desfire_des_key_new(desfireDesKey.getValue());

                MifareDesfireKey1.mifare_desfire_key_set_version(mifareDESFireKey, (byte) desfireDesKey.getVersion());

                int result = mifare_desfire_authenticate(tag, (byte) desfireApplicationKey.getIndex(), mifareDESFireKey);

                if (result == 0) {
                    Log.d(TAG, "Authenticated DES using key " + key.getName());

                    return true;
                } else {
                    Log.d(TAG, "Unable to authenticate DES using key " + key.getName());
                }

                break;
            }
        }
        return false;
    }

    private void showKeyNumber(DesfireFile desfireFile, final OnKeyNumberListener listener) {

        final Map<Integer, String> compactPermissionMap = desfireFile.getCompactPermissionMap();

        final List<Integer> keyNumbers = new ArrayList<>(compactPermissionMap.keySet());
        Collections.sort(keyNumbers);

        List<String> keys = new ArrayList<>();

        for (int i = 0; i < keyNumbers.size(); i++) {
            Integer keyNumber = keyNumbers.get(i);
            if (keyNumber == 14) {
                continue;
            }
            String access = compactPermissionMap.get(keyNumber);
            StringBuffer buffer = new StringBuffer();
            if (access.contains("R")) {
                buffer.append(getString(R.string.fileAccessKeyRead));
            }
            if (access.contains("W")) {
                if (buffer.length() > 0) {
                    buffer.append(", ");
                }
                buffer.append(getString(R.string.fileAccessKeyWrite));
            }
            if (access.contains("C")) {
                if (buffer.length() > 0) {
                    buffer.append(", ");
                }
                buffer.append(getString(R.string.fileAccessKeyChange));
            }

            keys.add(getString(R.string.fileAccessKey, keyNumber, buffer.toString()));
        }

        String names[] = keys.toArray(new String[keys.size()]);

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        View convertView = (View) inflater.inflate(R.layout.dialog_list, null);
        alertDialog.setView(convertView);

        alertDialog.setTitle(getString(R.string.fileAccessSelectKey));
        ListView lv = (ListView) convertView.findViewById(R.id.listView);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, names);
        lv.setAdapter(adapter);
        final AlertDialog show = alertDialog.show();

        lv.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                show.dismiss();

                Integer keyNumber = keyNumbers.get(position);

                listener.onKeyNumber(keyNumber, compactPermissionMap.get(keyNumber));
            }
        });
    }

    private void showKeySelector(DesfireKeyType type, final OnKeyListener listener) {
        MainApplication application = MainApplication.getInstance();

        DataSource dataSource = application.getDataSource();

        final List<DesfireKey> keys;
        if (type == DesfireKeyType.TDES || type == DesfireKeyType.DES) {
            keys = new ArrayList<>();

            keys.addAll(dataSource.getKeys(DesfireKeyType.DES));
            keys.addAll(dataSource.getKeys(DesfireKeyType.TKTDES));
        } else {
            keys = dataSource.getKeys(type);
        }

        if (!keys.isEmpty()) {
            String names[] = new String[keys.size()];
            for (int i = 0; i < names.length; i++) {
                names[i] = getString(R.string.applicationAuthenticateKeyNameVersion, keys.get(i).getName(), keys.get(i).getVersionAsHexString());
            }
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
            LayoutInflater inflater = getLayoutInflater();
            View convertView = (View) inflater.inflate(R.layout.dialog_list, null);
            alertDialog.setView(convertView);

            alertDialog.setTitle(getString(R.string.applicationAuthenticateKey, getName(type)));
            ListView lv = (ListView) convertView.findViewById(R.id.listView);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, names);
            lv.setAdapter(adapter);
            final AlertDialog show = alertDialog.show();

            lv.setOnItemClickListener(new OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    show.dismiss();

                    DesfireKey key = keys.get(position);

                    listener.onKey(key);
                }

            });
        } else {
            Log.d(TAG, "No " + type + " keys found");
        }
    }

    public void showToast(int resource, Object... args) {
        Toast.makeText(getApplicationContext(), getString(resource, args), Toast.LENGTH_LONG).show();
    }

    public void showToast(int resource) {
        Toast.makeText(getApplicationContext(), getString(resource), Toast.LENGTH_LONG).show();
    }

    public void showShortToast(int resource) {
        Toast.makeText(getApplicationContext(), getString(resource), Toast.LENGTH_SHORT).show();
    }

    public void showToastShortToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void showFileFragment(DesfireFile file) {
        Log.d(TAG, "showFileFragment");

        // Create new fragment and transaction
        final FileFragment newFragment = new FileFragment();
        newFragment.setFile(file);

        newFragment.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemClick " + position + " for " + id);

                ApplicationDetail applicationDetail = newFragment.getApplicationDetail(position);

                if (applicationDetail instanceof ApplicationDetailRecord) {
                    ApplicationDetailRecord key = (ApplicationDetailRecord) applicationDetail;

                    byte[] content = key.getContent();

                    Log.d(TAG, "Save " + Utils.getHexString(content));
                }

            }

        });

        // write to this file
        newFragment.setButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onButtonClick writeToFile");
                showFileWriteFragment(file);
            }
        });
        androidx.fragment.app.FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack
        transaction.replace(R.id.content, newFragment, "file");
        transaction.addToBackStack("file");
        // Commit the transaction
        transaction.commit();
    }

    private void showFileWriteFragment(DesfireFile file) {
        Log.d(TAG, "showFileWriteFragment");

        final FileWriteFragment newFragment = new FileWriteFragment(file);
        // write to this file
        newFragment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onButtonClick writeToFile");

                // can we distinguish which button was pressed ?
                boolean isButtonCreditPressed = false;
                boolean isButtonDebitPressed = false;
                boolean isButtonWritePressed = false;
                if (view.getId() == R.id.btnFileWriteCredit) {
                    isButtonCreditPressed = true;
                } else if (view.getId() == R.id.btnFileWriteDebit) {
                    isButtonDebitPressed = true;
                } else {
                    isButtonWritePressed = true;
                }
                String dataToWrite = newFragment.getDataToWrite();
                if (TextUtils.isEmpty(dataToWrite)) {
                    newFragment.setLogData("please enter any text to write");
                    return;
                }
                String creditDataToWrite = newFragment.getCreditDataToWrite();
                if (TextUtils.isEmpty(creditDataToWrite)) {
                    newFragment.setLogData("please enter a value to write");
                    return;
                }
                String debitDataToWrite = newFragment.getDebitDataToWrite();
                if (TextUtils.isEmpty(debitDataToWrite)) {
                    newFragment.setLogData("please enter a value to write");
                    return;
                }
                int creditValue = Integer.parseInt(creditDataToWrite);
                int debitValue = Integer.parseInt(debitDataToWrite);
                if (creditValue == 0) {
                    newFragment.setLogData("please enter a value to write");
                    return;
                }
                if (debitValue == 0) {
                    newFragment.setLogData("please enter a value to write");
                    return;
                }
                int fileLength = 0;
                byte fileNo = (byte) (file.getId() & 0xff);
                StandardDesfireFile standardDesfireFile;
                RecordDesfireFile recordDesfireFile;
                ValueDesfireFile valueDesfireFile;
                boolean isStandardFile = false;
                boolean isRecordFile = false;
                boolean isValueFile = false;
                if(file instanceof StandardDesfireFile) {
                    standardDesfireFile = (StandardDesfireFile) file;
                    fileLength = standardDesfireFile.getFileSize();
                    isStandardFile = true;
                } else if (file instanceof RecordDesfireFile) {
                    recordDesfireFile = (RecordDesfireFile) file;
                    fileLength = recordDesfireFile.getRecordSize();
                    isRecordFile = true;
                } else if (file instanceof ValueDesfireFile) {
                    valueDesfireFile = (ValueDesfireFile) file;
                    // todo at this point we could check that the new value (after debit or credit) is within range of lower or upper limit)
                    // if we debit or credit too much the commit transaction will fail
                    isValueFile = true;
                    //newFragment.setLogData("writing to a value file is not supported at this time");
                    //return;
                } else {
                    newFragment.setLogData("unsupported file type, aborted");
                    return;
                }

                // todo authenticate with a write key !


                if (isButtonWritePressed) {
                    // fill up the string with blanks up to fileLength (or trim the string)
                    byte[] dataToWriteByte = returnStringOfDefinedLength(dataToWrite, fileLength).getBytes(StandardCharsets.UTF_8);
                    if (isStandardFile) {
                        try {
                            int result = mifare_desfire_write_data(tag, fileNo, 0, dataToWriteByte.length, dataToWriteByte);
                            newFragment.setLogData("write StandardFile result (returns the data length if ok): " + result);
                        } catch (Exception e) {
                            //throw new RuntimeException(e);
                            newFragment.setLogData("Exception on writing data to StandardFile: " + e.getMessage());
                        }
                    }
                    // todo: if it is a LinearRecord file we receive an BE = boundary error, means the file is 'full'
                    if (isRecordFile) {
                        try {
                            int result = mifare_desfire_write_record(tag, fileNo, 0, dataToWriteByte.length, dataToWriteByte);
                            newFragment.setLogData("write RecordFile result (returns the data length if ok): " + result);
                            // don't forget to commit
                            int result2 = mifare_desfire_commit_transaction(tag);
                            newFragment.setLogData("write RecordFile result (returns the data length if ok): " + result + " commit result: " + result2);
                        } catch (Exception e) {
                            //throw new RuntimeException(e);
                            newFragment.setLogData("Exception on writing data to RecordFile: " + e.getMessage());
                        }
                    }
                } // if (isButtonWritePressed) {
                if (isButtonCreditPressed) {
                    try {
                        int result = mifare_desfire_credit(tag, fileNo, creditValue);
                        newFragment.setLogData("credit ValueFile result (returns 0 if ok): " + result);
                        // don't forget to commit
                        int result2 = mifare_desfire_commit_transaction(tag);
                        newFragment.setLogData("credit ValueFile result (returns 0 if ok): " + result + " commit result: " + result2);
                    } catch (Exception e) {
                        //throw new RuntimeException(e);
                        newFragment.setLogData("Exception on crediting data to ValueFile: " + e.getMessage());
                    }
                }
                if (isButtonDebitPressed) {
                    try {
                        int result = mifare_desfire_debit(tag, fileNo, debitValue);
                        newFragment.setLogData("debit ValueFile result (returns 0 if ok): " + result);
                        // don't forget to commit
                        int result2 = mifare_desfire_commit_transaction(tag);
                        newFragment.setLogData("debit ValueFile result (returns 0 if ok): " + result + " commit result: " + result2);
                    } catch (Exception e) {
                        //throw new RuntimeException(e);
                        newFragment.setLogData("Exception on crediting data to ValueFile: " + e.getMessage());
                    }
                }

            }
        });

        androidx.fragment.app.FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack
        transaction.replace(R.id.content, newFragment, "fileWrite");
        transaction.addToBackStack("fileWrite");
        // Commit the transaction
        transaction.commit();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onBackStackChanged() {
        invalidateOptionsMenu();
    }

    private class FileCallbacks implements FileSaveFragment.Callbacks {

        @Override
        public boolean onCanSave(String absolutePath, String fileName) {
            return absolutePath != null && absolutePath.length() > 0 && fileName != null && fileName.length() > 0;
        }

        @Override
        public void onConfirmSave(String absolutePath, String fileName) {
            if (absolutePath == null || absolutePath.length() == 0 || fileName == null || fileName.length() == 0) {
                //getFragmentManager().popBackStack();
                getSupportFragmentManager().popBackStack();

                return;
            }

            //FileFragment fragment = (FileFragment) getFragmentManager().findFragmentByTag("file");
            FileFragment fragment = (FileFragment) getSupportFragmentManager().findFragmentByTag("file");

            DesfireFile file = fragment.getFile();

            if (file instanceof ValueDesfireFile) {
                throw new IllegalArgumentException();
            }

            byte[] data;
            if (file instanceof StandardDesfireFile) {
                StandardDesfireFile standardDesfireFile = (StandardDesfireFile) file;
                data = standardDesfireFile.getData();
            } else if (file instanceof RecordDesfireFile) {
                RecordDesfireFile recordDesfireFile = (RecordDesfireFile) file;
                data = recordDesfireFile.getRecords();
            } else {
                throw new IllegalArgumentException();
            }
            FileOutputStream out = null;
            try {
                File outputFile = new File(absolutePath, fileName);

                if (outputFile.exists()) {
                    if (!outputFile.delete()) {
                        Log.d(TAG, "Unable to delete file " + outputFile);

                        return;
                    }
                }

                out = new FileOutputStream(outputFile);
                out.write(data);

                out.flush();

                Log.d(TAG, "Saved file " + file);

                Toast.makeText(getApplicationContext(), getString(R.string.fileSavedSuccess, outputFile.toString()), Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                Log.d(TAG, "Problem saving file " + absolutePath + " " + fileName);

                Toast.makeText(getApplicationContext(), getString(R.string.fileSavedFailure), Toast.LENGTH_LONG).show();

            } finally {
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e) {
                        // ignore
                    }
                }
            }
            //getFragmentManager().popBackStack();
            getSupportFragmentManager().popBackStack();

        }
    }

    private void formatPicc() {
        new AlertDialog.Builder(this).setTitle("Confirm Formatting the PICC?")
                .setMessage("Are you sure - this is irreversible?\n\nYou need to authenticate with the Master key first depending on PICC's settings!")
                .setPositiveButton("YES",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                try {

                                    // select master application first
                                    //public static int mifare_desfire_select_application (MifareTag tag, DesfireApplicationId aid) throws Exception
                                    System.out.println("*** start selectMasterApplication");
                                    int selectMasterApplicationResult = -99;
                                    try {
                                        selectMasterApplicationResult = mifare_desfire_select_application(tag, null);
                                    } catch (Exception e) {
                                        // throw new RuntimeException(e);
                                        showToastShortToast("selectMasterApplication error: " + e.getMessage());
                                        return;
                                    }
                                    System.out.println("*** selectMasterApplicationResult: " + selectMasterApplicationResult);
                                    if (selectMasterApplicationResult == 0) {
                                        showToastShortToast("selectMasterApplication success");
                                    } else {
                                        showToastShortToast("selectMasterApplication failure: " + selectMasterApplicationResult);
                                        return;
                                    }

                                    // get all keys from application 0
                                    application = applications.get(0); // master application
                                    DesfireKeyType type = application.getKeySettings().getType();
                                    for (int i = 0; i < application.getKeySettings().getMaxKeys(); i++) {

                                        try {
                                            byte version = mifare_desfire_get_key_version(tag, (byte) i);

                                            application.add(new DesfireApplicationKey(i, DesfireKey.newInstance(type, version)));
                                        } catch (IllegalArgumentException e) {
                                            // assume no key set
                                        }
                                    }

                                    DesfireApplicationKeySettings keySettings = application.getKeySettings();
                                    Log.d(TAG, keySettings.toString());
                                    //if(keySettings.isRequiresMasterKeyForDirectoryList()) {
                                    final List<DesfireApplicationKey> keys = application.getKeys();
                                    final DesfireApplicationKey root = keys.get(0);
                                    showKeySelector(keySettings.getType(), new OnKeyListener() {
                                        @Override
                                        public void onKey(DesfireKey key) {
                                            if (!isConnected()) {
                                                Log.d(TAG, "Tag lost wanting to select application");
                                                onTagLost();
                                                return;
                                            }
                                            try {
                                                DesfireApplicationKey clone = new DesfireApplicationKey(root.getIndex(), key);
                                                if (authenticate(clone)) {
                                                    MainActivity.this.authenticatedKey = clone;
                                                    int result = mifare_desfire_format_picc(tag);
                                                    showToastShortToast("formatting result (0 is OK): " + String.valueOf(result));
                                                    showToast(R.string.applicationAuthenticatedSuccess);
                                                    return;
                                                } else {
                                                    showToast(R.string.applicationAuthenticatedFail);
                                                }

                                            } catch (Exception e) {
                                                Log.d(TAG, "Unable to authenticate", e);
                                                showToast(R.string.applicationAuthenticatedFail);
                                            }
                                        }
                                    });
                                    // this code is run without authentication so it may fail
                                    int result = mifare_desfire_format_picc(tag);
                                    showToastShortToast("formatting result (0 is OK): " + String.valueOf(result));
								/*
								} else {
										//Log.d(TAG, "Can't authenticate an application");
										// we do mot need to authenticate
										int result = mifare_desfire_format_picc(tag);
										showToastShortToast("formatting result (0 is OK): " + String.valueOf(result));
									}

								 */

                                } catch (Exception e) {
                                    showToastShortToast("error on formatting the card");
                                }
                                dialog.dismiss();
                            }
                        })
                .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Do nothing
                        dialog.dismiss();
                    }
                })
                .create()
                .show();
    }


    public void show(AlertDialog altertDialog) {
        synchronized (this) {
            if (alertDialog != null) {
                alertDialog.cancel();
            }
            // create alert dialog
            this.alertDialog = altertDialog;

            runOnUiThread(new Runnable() {
                public void run() {
                    // show it
                    alertDialog.show();
                }
            });

        }
    }

    public void hideDialog() {
        synchronized (this) {
            if (alertDialog != null) {
                alertDialog.cancel();
                alertDialog = null;
            }
        }
    }

    private void readFile(final DesfireFile desfireFile) {

        Log.d(TAG, "Read file access");
        if (desfireFile instanceof StandardDesfireFile) {
            try {
                StandardDesfireFile standardDesfireFile = (StandardDesfireFile) desfireFile;

                if (!standardDesfireFile.isData()) {
                    Log.d(TAG, "Read data from file " + Integer.toHexString(desfireFile.getId()));

                    byte[] data = mifare_desfire_read_data(tag, (byte) desfireFile.getId(), 0, 0);

                    Log.d(TAG, "Read data length " + data.length);

                    standardDesfireFile.setData(data);
                }
            } catch (Exception e) {
                Log.d(TAG, "Problem reading file", e);
            }
        } else if (desfireFile instanceof ValueDesfireFile) {
            try {
                ValueDesfireFile valueDesfireFile = (ValueDesfireFile) desfireFile;

                if (!valueDesfireFile.isValue()) {
                    Log.d(TAG, "Read value from file " + Integer.toHexString(desfireFile.getId()));

                    Integer value = mifare_desfire_get_value(tag, (byte) desfireFile.getId());

                    Log.d(TAG, "Read value " + value);

                    valueDesfireFile.setValue(value);
                }
            } catch (Exception e) {
                Log.d(TAG, "Problem reading file", e);
            }
        } else if (desfireFile instanceof RecordDesfireFile) {
            try {
                RecordDesfireFile recordDesfireFile = (RecordDesfireFile) desfireFile;

                if (!recordDesfireFile.isRecords()) {
                    Log.d(TAG, "Read records from file " + Integer.toHexString(desfireFile.getId()));

                    byte[] records = mifare_desfire_read_records(tag, (byte) desfireFile.getId(), 0, recordDesfireFile.getCurrentRecords());

                    Log.d(TAG, "Read " + recordDesfireFile.getCurrentRecords() + " records " + Utils.getHexString(records));

                    recordDesfireFile.setRecords(records);
                }
            } catch (Exception e) {
                Log.d(TAG, "Problem reading record file", e);
            }
        }
    }

    /**
     * some string manipulations
     */

    private String returnStringOfDefinedLength(String data, int length) {
        int dataLength = data.length();
        if (dataLength == length) {
            return data;
        } else if (dataLength > length) {
            return data.substring(0, length);
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append(data);
            for (int i = 0; i < (length - dataLength); i++) {
                sb.append(" ");
            }
            return sb.toString();
        }
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    @Override
    public boolean onCanSave(String absolutePath, String fileName) {
        return callbacks.onCanSave(absolutePath, fileName);
    }

    @Override
    public void onConfirmSave(String absolutePath, String fileName) {
        callbacks.onConfirmSave(absolutePath, fileName);

        this.callbacks = null;
    }

    private boolean isConnected() {
        MifareTag tag = this.tag;

        if (tag != null) {
            DefaultIsoDepWrapper wrapper = (DefaultIsoDepWrapper) tag.getIo().getIsoDepWrapper();

            return wrapper.getIsoDep().isConnected();
        }
        return false;
    }

    /**
     * Launch an activity for NFC (or wireless) settings, so that the user might enable or disable nfc
     */


    protected void startNfcSettingsActivity() {
        if (android.os.Build.VERSION.SDK_INT >= 16) {
            startActivity(new Intent(ACTION_NFC_SETTINGS)); // android.provider.Settings.ACTION_NFC_SETTINGS
        } else {
            startActivity(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));
        }
    }

    protected static class TagPresenceScanner extends Handler {

        private static final long TAG_RESCAN_INTERVAL_MS = 1000;

        private WeakReference<MainActivity> activityReference;

        public TagPresenceScanner(MainActivity activity) {
            this.activityReference = new WeakReference<MainActivity>(activity);
        }

        void resume() {
            synchronized (this) {
                if (!hasMessages(0)) {
                    sendEmptyMessage(0);
                }
            }
        }

        public void resumeDelayed() {
            synchronized (this) {
                if (!hasMessages(0)) {
                    sendEmptyMessageDelayed(0, TAG_RESCAN_INTERVAL_MS);
                }
            }
        }

        public void pause() {
            synchronized (this) {
                removeMessages(0);
            }
        }

        @Override
        public void handleMessage(android.os.Message msg) {
            //Log.v(TAG, "Handle message");

            MainActivity activity = activityReference.get();
            if (activity != null) {
                if (activity.isConnected()) {
                    resumeDelayed();
                } else {
                    activity.onTagLost();

                    pause();
                }
            }
        }
    }
}
