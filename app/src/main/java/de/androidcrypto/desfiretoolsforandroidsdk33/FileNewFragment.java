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

	private com.shawnlin.numberpicker.NumberPicker npFileId, npKeyRw, npKeyCar, npKeyR, npKeyW;
	private EditText fileSize;
	private AutoCompleteTextView choiceCommunicationSettings;
	private TextView logData;
	RadioGroup rgFileType;
	RadioButton rbStandard, rbValue, rbCyclic;
	private LinearLayout standardLayout, valueLayout, cyclicLayout;
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
		fileSize = view.findViewById(R.id.etFileNewStandardFileSize);
		logData = view.findViewById(R.id.tvLog);
		standardLayout = view.findViewById(R.id.llFileNewStandardLinearLayout);
		valueLayout = view.findViewById(R.id.llFileNewValueLinearLayout);
		cyclicLayout = view.findViewById(R.id.llFileNewRecordLinearLayout);
		rgFileType =  view.findViewById(R.id.rgFileType);
		rbStandard =  view.findViewById(R.id.rbStandardFile);
		rbValue =  view.findViewById(R.id.rbValueFile);
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

		// default = standard file
		standardLayout.setVisibility(View.VISIBLE);
		valueLayout.setVisibility(View.GONE);
		cyclicLayout.setVisibility(View.GONE);

		createFile.setOnClickListener(listener);

		nrOfApplicationKeys = application.getKeys().size();


		rgFileType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

				@Override
				public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {

					View radioButton = radioGroup.findViewById(checkedId);
					int index = radioGroup.indexOfChild(radioButton);

					// Add logic here

					switch (index) {
						case 0: // first button plain
							standardLayout.setVisibility(View.VISIBLE);
							valueLayout.setVisibility(View.GONE);
							cyclicLayout.setVisibility(View.GONE);
							break;
						case 1: // second button maced
							standardLayout.setVisibility(View.GONE);
							valueLayout.setVisibility(View.VISIBLE);
							cyclicLayout.setVisibility(View.GONE);
							break;
						case 2: // third button encrypted
							standardLayout.setVisibility(View.GONE);
							valueLayout.setVisibility(View.GONE);
							cyclicLayout.setVisibility(View.VISIBLE);
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

	public EditText getFileSize() {
		return fileSize;
	}

	public int getNrOfApplicationKeys() {
		return nrOfApplicationKeys;
	}
}
