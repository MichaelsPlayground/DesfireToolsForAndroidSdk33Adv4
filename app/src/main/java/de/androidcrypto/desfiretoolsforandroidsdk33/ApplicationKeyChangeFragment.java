package de.androidcrypto.desfiretoolsforandroidsdk33;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.github.skjolber.desfire.ev1.model.DesfireApplication;
import com.github.skjolber.desfire.ev1.model.DesfireApplicationKeySettings;
import com.shawnlin.numberpicker.NumberPicker;

/**
 * class added by MichaelsPlayground / AndroidCrypto
 */

// todo use Bundle instead of a constructor for parameter
@SuppressLint("ValidFragment")
public class ApplicationKeyChangeFragment extends Fragment {

	// todo block file creation if application id = 00 00 00 = master file application

	private NumberPicker npFileId, npKeyRw, npKeyCar, npKeyR, npKeyW, npNrOfRecords;
	private EditText changeKeySettingsAid, changeKeySettingsExisting, changeKeySettingsChanged, maximumNumberOfKeys;
	private EditText keyUsedForCar;
	private CheckBox cbBit0MasterKeyIsChangeable, cbBit1MasterKeyAuthenticationNeededDirListing, cbBit2MasterKeyAuthenticationNeededCreateDelete, cbBit3MasterKeySettingsChangeAllowed;
	private boolean bit0New, bit1New, bit2New, bit3New;
	private byte keySettingsChanged;
	private int keyNumberForAccessRightChangeExisting;
	Button doChangeKeySettings;
	private TextView logData;

	private DesfireApplication application;

	private View.OnClickListener listener;



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

		changeKeySettingsAid = view.findViewById(R.id.etKeySettingsAid);
		changeKeySettingsExisting = view.findViewById(R.id.etKeySettingsExisting);
		changeKeySettingsChanged = view.findViewById(R.id.etKeySettingsChanged);
		cbBit0MasterKeyIsChangeable = view.findViewById(R.id.cbBit0MasterKeyIsChangeable);
		cbBit1MasterKeyAuthenticationNeededDirListing = view.findViewById(R.id.cbBit1MasterKeyAuthenticationNeededDirListing);
		cbBit2MasterKeyAuthenticationNeededCreateDelete = view.findViewById(R.id.cbBit2MasterKeyAuthenticationNeededCreateDelete);
		cbBit3MasterKeySettingsChangeAllowed = view.findViewById(R.id.cbBit3MasterKeySettingsChangeAllowed);
		maximumNumberOfKeys = view.findViewById(R.id.etMaximumNumberOfKeys);
		keyUsedForCar = view.findViewById(R.id.etKeySettingsCarKey);
		doChangeKeySettings = view.findViewById(R.id.btnDoChangeApplicationKeySettings);
		logData = view.findViewById(R.id.tvLog);

		changeKeySettingsAid.setText(application.getIdString());

		// get the existing key settings
		DesfireApplicationKeySettings existingKeySettings = application.getKeySettings();
		changeKeySettingsExisting.setText(bytesToHexNpe(existingKeySettings.getSettings()));
		maximumNumberOfKeys.setText("key type: " + existingKeySettings.getType().toString()  + " keys: " + existingKeySettings.getMaxKeys());
		changeKeySettingsChanged.setText(bytesToHexNpe(existingKeySettings.getSettings()));
		byte keySettings = existingKeySettings.getSettings()[0];
		byte keyNumbers = existingKeySettings.getSettings()[1]; // do not change this as it is not send to the PICC
		keySettingsChanged = keySettings;
		System.out.println("keySettings: " + keySettings + " keyNumbers: " + keyNumbers);

		//
		/*
			bit 0 is most right bis (counted from right to left)
			bit 0 = application master key is changeable (1) or frozen (0)
			bit 1 = application master key authentication is needed for file directory access (1)
			bit 2 = application master key authentication is needed before CreateFile / DeleteFile (1)
			bit 3 = change of the application master key settings is allowed (1)
			bit 4-7 = hold the Access Rights for changing application keys (ChangeKey command)
			• 0x0: Application master key authentication is necessary to change any key (default).
			• 0x1 .. 0xD: Authentication with the specified key is necessary to change any key.
			• 0xE: Authentication with the key to be changed (same KeyNo) is necessary to change a key.
			• 0xF: All Keys (except application master key, see Bit0) within this application are frozen.
		 */
		boolean bit0MasterKeyIsChangeable = existingKeySettings.isCanChangeMasterKey();
		boolean bit1MasterKeyAuthenticationNeededDirListing = existingKeySettings.isRequiresMasterKeyForDirectoryList();
		boolean bit2MasterKeyAuthenticationNeededCreateDelete = existingKeySettings.isRequiresMasterKeyForCreateAndDelete();
		boolean bit3MasterKeySettingsChangeAllowed = existingKeySettings.isConfigurationChangable();

		cbBit0MasterKeyIsChangeable.setChecked(bit0MasterKeyIsChangeable);
		cbBit1MasterKeyAuthenticationNeededDirListing.setChecked(bit1MasterKeyAuthenticationNeededDirListing);
		cbBit2MasterKeyAuthenticationNeededCreateDelete.setChecked(bit2MasterKeyAuthenticationNeededCreateDelete);
		cbBit3MasterKeySettingsChangeAllowed.setChecked(bit3MasterKeySettingsChangeAllowed);

		logData.setText("Existing settings\n" + existingKeySettings.toString());

		// take the existing settings
		bit0New = bit0MasterKeyIsChangeable;
		bit1New = bit1MasterKeyAuthenticationNeededDirListing;
		bit2New = bit2MasterKeyAuthenticationNeededCreateDelete;
		bit3New = bit3MasterKeySettingsChangeAllowed;

		keyNumberForAccessRightChangeExisting = existingKeySettings.getChangeKeyAccessRights(); // use this for a numberPicker to change
		int maximumNumberOfKeys = existingKeySettings.getMaxKeys(); // numberPicker maximum value
		keyUsedForCar.setText("key: " + keyNumberForAccessRightChangeExisting);

		doChangeKeySettings.setOnClickListener(listener);

		// keySettingsChanged is holding the data
		// note: do not set this to unchecked on the Master Application key settings, the PICC is frozen afterwards
		cbBit0MasterKeyIsChangeable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
				bit0New = b;
				if (b) {
					keySettingsChanged = setBitInByte(keySettings, 0);
				} else {
					keySettingsChanged = unsetBitInByte(keySettings, 0);
				}
			}
		});
		cbBit1MasterKeyAuthenticationNeededDirListing.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
				bit1New = b;
				if (b) {
					keySettingsChanged = unsetBitInByte(keySettingsChanged, 1); // note this inversed logic !
				} else {
					keySettingsChanged = setBitInByte(keySettingsChanged, 1); // note this inversed logic !
				}
			}
		});
		cbBit2MasterKeyAuthenticationNeededCreateDelete.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
				bit2New = b;
				if (b) {
					keySettingsChanged = unsetBitInByte(keySettingsChanged, 2); // note this inversed logic !
				} else {
					keySettingsChanged = setBitInByte(keySettingsChanged, 2); // note this inversed logic !
				}
			}
		});

		// note: do not set this to unchecked on the Master Application key settings, the PICC is frozen afterwards
		cbBit3MasterKeySettingsChangeAllowed.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
				bit3New = b;
				if (b) {
					keySettingsChanged = setBitInByte(keySettingsChanged, 3);
				} else {
					keySettingsChanged = unsetBitInByte(keySettingsChanged, 3);
				}
			}
		});

		/*
		doChangeKeySettings.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				byte[] keySettingsComplete = new byte[2];
				keySettingsComplete[0] = keySettingsChanged;
				keySettingsComplete[1] = keyNumbers;
				changeKeySettingsChanged.setText(bytesToHexNpe(keySettingsComplete));
			}
		});
*/

        return view;
    }

	public void setOnClickListener(View.OnClickListener listener) {
		this.listener = listener;
	}

	public void setButtonEnabled(boolean enabled) {
		this.doChangeKeySettings.setEnabled(enabled);
	}

	public void setLogData(String message) {
		logData.setText(message);
	}

	public DesfireApplication getApplication() {
		return application;
	}

	public byte getKeySettingsChanged() {
		return keySettingsChanged;
	}

	public int getKeyNumberForAccessRightChangeExisting() {
		return keyNumberForAccessRightChangeExisting;
	}

	private static String bytesToHexNpe(byte[] bytes) {
		if (bytes == null) return "";
		StringBuffer result = new StringBuffer();
		for (byte b : bytes)
			result.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
		return result.toString();
	}

	/**
	 * section for bit arithmetics
	 */

	// position is 0 based starting from right to left
	private static byte setBitInByte(byte input, int pos) {
		return (byte) (input | (1 << pos));
	}

	// position is 0 based starting from right to left
	private static byte unsetBitInByte(byte input, int pos) {
		return (byte) (input & ~(1 << pos));
	}

	// https://stackoverflow.com/a/29396837/8166854
	private static boolean testBit(byte b, int n) {
		int mask = 1 << n; // equivalent of 2 to the nth power
		return (b & mask) != 0;
	}

	// https://stackoverflow.com/a/29396837/8166854
	private static boolean testBit(byte[] array, int n) {
		int index = n >>> 3; // divide by 8
		int mask = 1 << (n & 7); // n modulo 8
		return (array[index] & mask) != 0;
	}
}
