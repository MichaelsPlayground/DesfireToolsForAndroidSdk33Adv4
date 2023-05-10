package de.androidcrypto.desfiretoolsforandroidsdk33;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.github.skjolber.desfire.ev1.model.DesfireApplication;
import com.github.skjolber.desfire.ev1.model.DesfireApplicationKey;
import com.github.skjolber.desfire.ev1.model.DesfireApplicationKeySettings;
import com.github.skjolber.desfire.ev1.model.command.Utils;
import com.github.skjolber.desfire.ev1.model.key.DesfireKey;
import com.github.skjolber.desfire.ev1.model.key.DesfireKeyType;
import com.shawnlin.numberpicker.NumberPicker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.androidcrypto.desfiretoolsforandroidsdk33.keys.DataSource;

/**
 * class added by MichaelsPlayground / AndroidCrypto
 */

// todo use Bundle instead of a constructor for parameter
@SuppressLint("ValidFragment")
public class ApplicationKeyChangeFragment extends Fragment {
    private static final String TAG = "ApplicationKeyChange";

    // todo block file creation if application id = 00 00 00 = master file application

    private NumberPicker npKeyChangeSelectedKey;
    private EditText changeKeyAid, changeKeyOldKey, changeKeyNewKey;

    private EditText maximumNumberOfKeys;
    private Button selectOldKey, selectNewKey;
    private DesfireKey oldDesfireKeyForChanging, newDesfireKeyForChanging;
    private byte keyNumberForChanging;
    private int keyNumberForChangingInt;

    Button doChangeKey;
    private TextView logData;

    private DesfireApplication application;

    private View.OnClickListener listener;

    private interface OnKeyListener {
        void onKey(DesfireKey key);
    }

    // todo use Bundle instead of a constructor for parameter
    @SuppressLint("ValidFragment")
    public ApplicationKeyChangeFragment(DesfireApplication application) {
        this.application = application;
    }

	/*
	public void setApplications(List<DesfireApplication> applications) {
		this.applications = applications;
	}
	 */

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_application_key_change, container, false);

        changeKeyAid = view.findViewById(R.id.etKeyChangingAid);
        changeKeyOldKey = view.findViewById(R.id.etKeyChangeOldKey);
        changeKeyNewKey = view.findViewById(R.id.etKeyChangeNewKey);

        selectOldKey = view.findViewById(R.id.btnSelectOldKey);
        selectNewKey = view.findViewById(R.id.btnSelectNewKey);

        npKeyChangeSelectedKey = view.findViewById(R.id.npKeyChangingSelectedKey);
        maximumNumberOfKeys = view.findViewById(R.id.etMaximumNumberOfKeys);

        doChangeKey = view.findViewById(R.id.btnDoChangeKey);
        logData = view.findViewById(R.id.tvLog);

        changeKeyAid.setText(application.getIdString());

        keyNumberForChanging = (byte) 0x00; // default
        keyNumberForChangingInt = 0; // default


        // init the key selector (numberPicker), the maximumKeys info and enable the doChange button
        // get the existing key settings
        DesfireApplicationKeySettings existingKeySettings = application.getKeySettings();
        int maxKeys = existingKeySettings.getMaxKeys();
        if (maxKeys > 0) {
            doChangeKey.setEnabled(true);
        } else {
            doChangeKey.setEnabled(false);
        }
        maximumNumberOfKeys.setText("key type: " + existingKeySettings.getType().toString() + " keys: " + maxKeys);
        npKeyChangeSelectedKey.setMaxValue(maxKeys - 1);

        npKeyChangeSelectedKey.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                keyNumberForChanging = (byte) (newVal & 0xff);
                keyNumberForChangingInt = newVal;
            }
        });

        selectOldKey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Log.d(TAG, "change app key workflow");
                boolean appHasKeys = application.hasKeys();
                Log.d(TAG, "application has keys: " + appHasKeys);
                if (appHasKeys = false) {
                    logData.setText("the application has no keys, aborted");
                    return;
                }

                List<DesfireApplicationKey> appKeys = application.getKeys();
                int appNumberOfKeys = appKeys.size();
                Log.d(TAG, "appNumberOfKeys: " + appNumberOfKeys);

                Log.d(TAG, "change old key " + keyNumberForChangingInt);
                DesfireApplicationKey appKey1 = appKeys.get(keyNumberForChangingInt);
                int appKey1Index = appKey1.getIndex();
                Log.d(TAG, "appKey1Index: " + appKey1Index);
                DesfireKey appKey1DesfireKey = appKey1.getDesfireKey();
                String appKey1DesfireKeyName = appKey1DesfireKey.getName();
                Log.d(TAG, "appKey1DesfireKeyName: " + appKey1DesfireKeyName);
                String appKey1DesfireKeyType = appKey1DesfireKey.getType().name();
                Log.d(TAG, "appKey1DesfireKeyType: " + appKey1DesfireKeyType);

                // select the old key
                showKeySelector(appKey1DesfireKey.getType(), new ApplicationKeyChangeFragment.OnKeyListener() {
                    @Override
                    public void onKey(DesfireKey key) {
                        Log.d(TAG, "keySelector OLD key " + key.toString() + " data: " + bytesToHexNpe(key.getValue()));
                        changeKeyOldKey.setText(bytesToHexNpe(key.getValue()));
                        oldDesfireKeyForChanging = key;
                    }
                });
            }
        });

        selectNewKey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Log.d(TAG, "change app key workflow");
                boolean appHasKeys = application.hasKeys();
                Log.d(TAG, "application has keys: " + appHasKeys);
                if (appHasKeys = false) {
                    logData.setText("the application has no keys, aborted");
                    return;
                }

                List<DesfireApplicationKey> appKeys = application.getKeys();
                int appNumberOfKeys = appKeys.size();
                Log.d(TAG, "appNumberOfKeys: " + appNumberOfKeys);

                Log.d(TAG, "change new key " + keyNumberForChangingInt);
                DesfireApplicationKey appKey1 = appKeys.get(keyNumberForChangingInt);
                int appKey1Index = appKey1.getIndex();
                Log.d(TAG, "appKey1Index: " + appKey1Index);
                DesfireKey appKey1DesfireKey = appKey1.getDesfireKey();
                String appKey1DesfireKeyName = appKey1DesfireKey.getName();
                Log.d(TAG, "appKey1DesfireKeyName: " + appKey1DesfireKeyName);
                String appKey1DesfireKeyType = appKey1DesfireKey.getType().name();
                Log.d(TAG, "appKey1DesfireKeyType: " + appKey1DesfireKeyType);

                // select the old key
                showKeySelector(appKey1DesfireKey.getType(), new ApplicationKeyChangeFragment.OnKeyListener() {
                    @Override
                    public void onKey(DesfireKey key) {
                        Log.d(TAG, "keySelector NEW key " + key.toString() + " data: " + bytesToHexNpe(key.getValue()));
                        changeKeyNewKey.setText(bytesToHexNpe(key.getValue()));
                        newDesfireKeyForChanging = key;
                    }
                });
            }
        });

        logData.setText("Existing settings\n" + existingKeySettings.toString());
        doChangeKey.setOnClickListener(listener);
        return view;
    }

    private void showKeySelector(DesfireKeyType type,
                                 final ApplicationKeyChangeFragment.OnKeyListener listener) {
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
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
            LayoutInflater inflater = getLayoutInflater();
            View convertView = (View) inflater.inflate(R.layout.dialog_list, null);
            alertDialog.setView(convertView);

            //alertDialog.setTitle(getString(R.string.applicationAuthenticateKey, getName(type)));
            alertDialog.setTitle("select the key");
            ListView lv = (ListView) convertView.findViewById(R.id.listView);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, names);
            lv.setAdapter(adapter);
            final AlertDialog show = alertDialog.show();

            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

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

    public static String bytesToHex(byte[] bytes) {
        if (bytes == null) return "";
        StringBuffer result = new StringBuffer();
        for (byte b : bytes)
            result.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
        return result.toString();
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

    public void setButtonListener(View.OnClickListener listener) {
        this.listener = listener;
    }

    public void setLogData(String message) {
        logData.setText(message);
    }

    public DesfireApplication getApplication() {
        return application;
    }

    public String getChangeKeyOldKey() {
        return changeKeyOldKey.getText().toString();
    }

    public String getChangeKeyNewKey() {
        return changeKeyNewKey.getText().toString();
    }

    public DesfireKey getOldDesfireKeyForChanging() {
        return oldDesfireKeyForChanging;
    }

    public DesfireKey getNewDesfireKeyForChanging() {
        return newDesfireKeyForChanging;
    }

    public byte getKeyNumberForChanging() {
        return keyNumberForChanging;
    }

    private static String bytesToHexNpe(byte[] bytes) {
        if (bytes == null) return "";
        StringBuffer result = new StringBuffer();
        for (byte b : bytes)
            result.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
        return result.toString();
    }


}
