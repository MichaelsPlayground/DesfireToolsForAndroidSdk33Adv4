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


	private com.shawnlin.numberpicker.NumberPicker npFileId;
	private EditText fileId;
	private AutoCompleteTextView choiceCommunicationSettings;
	private TextView logData;
	private Button createFile;
	private DesfireApplication application;

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
		fileId = view.findViewById(R.id.etFileId);
		logData = view.findViewById(R.id.tvLog);
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

		createFile.setOnClickListener(listener);
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
	public String getFid() {return fileId.getText().toString();}

	public DesfireApplication getApplication() {
		return application;
	}

	public NumberPicker getNpFileId() {
		return npFileId;
	}

	public AutoCompleteTextView getChoiceCommunicationSettings() {
		return choiceCommunicationSettings;
	}
}
