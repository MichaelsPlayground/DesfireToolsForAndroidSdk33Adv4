package de.androidcrypto.desfiretoolsforandroidsdk33;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.github.skjolber.desfire.ev1.model.DesfireApplication;
import com.shawnlin.numberpicker.NumberPicker;

/**
 * class added by MichaelsPlayground / AndroidCrypto
 */

// todo use Bundle instead of a constructor for parameter
@SuppressLint("ValidFragment")
public class FileNewFragment extends Fragment {

	// todo block file creation if application id = 00 00 00 = master file application

	private com.shawnlin.numberpicker.NumberPicker npFileId, npKeyRw, npKeyCar, npKeyR, npKeyW, npNrOfRecords;
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
	public FileNewFragment(DesfireApplication application) {
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
        View view = inflater.inflate(R.layout.fragment_file_new, container, false);

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

		logData.setText("Create file for applicationID " + application.getIdString());

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
}
