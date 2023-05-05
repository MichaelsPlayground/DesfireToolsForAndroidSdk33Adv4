package de.androidcrypto.desfiretoolsforandroidsdk33;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
public class ApplicationKeySettingsChangeFragment extends Fragment {

	// todo block file creation if application id = 00 00 00 = master file application

	private NumberPicker npFileId, npKeyRw, npKeyCar, npKeyR, npKeyW, npNrOfRecords;
	private EditText changeKeySettingsAid, changeKeySettingsExisting, changeKeySettingsChanged, maximumNumberOfKeys;
	private CheckBox cbBit0MasterKeyIsChangeable, cbBit1MasterKeyAuthenticationNeededDirListing, cbBit2MasterKeyAuthenticationNeededCreateDelete, cbBit3MasterKeySettingsChangeAllowed;
	private boolean bit0New, bit1New, bit2New, bit3New;
	private byte keySettingsChanged;
	Button doChangeKeySettings;

	private EditText standardFileSize, lowerLimit, upperLimit, value, recordFileSize;
	private AutoCompleteTextView choiceCommunicationSettings;
	private TextView logData;
	RadioGroup rgFileType;
	RadioButton rbStandard, rbValue, rbRecord, rbCyclic;
	private LinearLayout standardLayout, valueLayout, recordLayout;
	private Button createFile;
	private DesfireApplication application;
	private int nrOfApplicationKeys;

	private View.OnClickListener listener;



	// todo use Bundle instead of a constructor for parameter
	@SuppressLint("ValidFragment")
	public ApplicationKeySettingsChangeFragment(DesfireApplication application) {
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
        View view = inflater.inflate(R.layout.fragment_application_key_settings_change, container, false);

		changeKeySettingsAid = view.findViewById(R.id.etKeySettingsAid);
		changeKeySettingsExisting = view.findViewById(R.id.etKeySettingsExisting);
		changeKeySettingsChanged = view.findViewById(R.id.etKeySettingsChanged);
		cbBit0MasterKeyIsChangeable = view.findViewById(R.id.cbBit0MasterKeyIsChangeable);
		cbBit1MasterKeyAuthenticationNeededDirListing = view.findViewById(R.id.cbBit1MasterKeyAuthenticationNeededDirListing);
		cbBit2MasterKeyAuthenticationNeededCreateDelete = view.findViewById(R.id.cbBit2MasterKeyAuthenticationNeededCreateDelete);
		cbBit3MasterKeySettingsChangeAllowed = view.findViewById(R.id.cbBit3MasterKeySettingsChangeAllowed);
		maximumNumberOfKeys = view.findViewById(R.id.etMaximumNumberOfKeys);
		doChangeKeySettings = view.findViewById(R.id.btnDoChangeApplicationKeySettings);


		npFileId = view.findViewById(R.id.npFileId);
		npKeyRw = view.findViewById(R.id.npKeyRw);
		npKeyCar = view.findViewById(R.id.npKeyCar);
		npKeyR = view.findViewById(R.id.npKeyR);
		npKeyW = view.findViewById(R.id.npKeyW);
		standardFileSize = view.findViewById(R.id.etFileNewStandardFileSize);
		recordFileSize = view.findViewById(R.id.etFileNewRecordFileSize);
		npNrOfRecords = view.findViewById(R.id.npNrOfRecords);
		lowerLimit = view.findViewById(R.id.etFileNewValueLowerLimit);
		upperLimit = view.findViewById(R.id.etFileNewValueUpperLimit);
		value = view.findViewById(R.id.etFileNewValueValue);
		logData = view.findViewById(R.id.tvLog);
		standardLayout = view.findViewById(R.id.llFileNewStandardLinearLayout);
		valueLayout = view.findViewById(R.id.llFileNewValueLinearLayout);
		recordLayout = view.findViewById(R.id.llFileNewRecordLinearLayout);
		rgFileType =  view.findViewById(R.id.rgFileType);
		rbStandard =  view.findViewById(R.id.rbStandardFile);
		rbValue =  view.findViewById(R.id.rbValueFile);
		rbRecord =  view.findViewById(R.id.rbRecordFile);
		rbCyclic =  view.findViewById(R.id.rbCyclicFile);

		createFile = view.findViewById(R.id.btnCreateFile);

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

		int keyNumberForAccessRightChangeExisting = existingKeySettings.getChangeKeyAccessRights(); // use this for a numberPicker to change
		int maximumNumberOfKeys = existingKeySettings.getMaxKeys(); // numberPicker maximum value

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

		doChangeKeySettings.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				byte[] keySettingsComplete = new byte[2];
				keySettingsComplete[0] = keySettingsChanged;
				keySettingsComplete[1] = keyNumbers;
				changeKeySettingsChanged.setText(bytesToHexNpe(keySettingsComplete));
			}
		});





		String[] type = new String[]{"Plain",
				"MACed",
				"Encrypted",
		};
		ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
				getContext(),
				R.layout.drop_down_item,
				type);
		choiceCommunicationSettings = view.findViewById(R.id.spCommunicationSettings);
		choiceCommunicationSettings.setAdapter(arrayAdapter);

		// todo remove after implementing create value file
		//rbValue.setEnabled(false);

		// default = standard file
		standardLayout.setVisibility(View.VISIBLE);
		valueLayout.setVisibility(View.GONE);
		recordLayout.setVisibility(View.GONE);
		// as a linear record file and cyclic record file are using the same parameters I'm using the same layout for both

		createFile.setOnClickListener(listener);

		nrOfApplicationKeys = application.getKeys().size();
		// todo implement maximum key number for RW/CAR/R/W number key checker

		rgFileType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

				@Override
				public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {

					View radioButton = radioGroup.findViewById(checkedId);
					int index = radioGroup.indexOfChild(radioButton);

					switch (index) {
						case 0: // first button standard file
							standardLayout.setVisibility(View.VISIBLE);
							valueLayout.setVisibility(View.GONE);
							recordLayout.setVisibility(View.GONE);
							break;
						case 1: // second button value file
							standardLayout.setVisibility(View.GONE);
							valueLayout.setVisibility(View.VISIBLE);
							recordLayout.setVisibility(View.GONE);
							break;
						case 2: // third button record file
							standardLayout.setVisibility(View.GONE);
							valueLayout.setVisibility(View.GONE);
							recordLayout.setVisibility(View.VISIBLE);
							break;
						case 3: // fourth button cyclic file
							standardLayout.setVisibility(View.GONE);
							valueLayout.setVisibility(View.GONE);
							recordLayout.setVisibility(View.VISIBLE);
							break;
					}
				}
		});


		/*
		createApplication.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				System.out.println("*** createApplicationPressed ***");
			}
		});

		 */


        return view;
    }

	public void setOnClickListener(View.OnClickListener listener) {
		this.listener = listener;
	}

	public void setLogData(String message) {
		logData.setText(message);
	}

	public DesfireApplication getApplication() {
		return application;
	}

	public NumberPicker getNpFileId() {
		return npFileId;
	}

	public AutoCompleteTextView getChoiceCommunicationSettings() {
		return choiceCommunicationSettings;
	}

	public NumberPicker getNpKeyRw() {
		return npKeyRw;
	}

	public NumberPicker getNpKeyCar() {
		return npKeyCar;
	}

	public NumberPicker getNpKeyR() {
		return npKeyR;
	}

	public NumberPicker getNpKeyW() {
		return npKeyW;
	}

	public EditText getStandardFileSize() {
		return standardFileSize;
	}

	public EditText getRecordFileSize() {
		return recordFileSize;
	}

	public NumberPicker getNpNrOfRecords() {
		return npNrOfRecords;
	}

	public int getLowerLimit() {
		return Integer.parseInt(lowerLimit.getText().toString());
	}

	public int getUpperLimit() {
		return Integer.parseInt(upperLimit.getText().toString());
	}

	public int getValue() {
		return Integer.parseInt(value.getText().toString());
	}

	public int getNrOfApplicationKeys() {
		return nrOfApplicationKeys;
	}

	public boolean isRbStandardChecked() {
		return rbStandard.isChecked();
	}

	public boolean isRbValueChecked() {
		return rbValue.isChecked();
	}

	public boolean isRbRecordChecked() {
		return rbRecord.isChecked();
	}

	public boolean isRbCyclicChecked() {
		return rbCyclic.isChecked();
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
