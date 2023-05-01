package de.androidcrypto.desfiretoolsforandroidsdk33;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.github.skjolber.desfire.ev1.model.DesfireApplication;

/**
 * class added by MichaelsPlayground / AndroidCrypto
 */

// todo use Bundle instead of a constructor for parameter
@SuppressLint("ValidFragment")
public class FileNewFragment extends Fragment {

	private EditText fileId;
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

		fileId = view.findViewById(R.id.etFileId);
		logData = view.findViewById(R.id.tvLog);
		createFile = view.findViewById(R.id.btnCreateFile);

		logData.setText("Create file for applicationID " + application.getIdString());

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

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

    public void onAttach(Activity activity) {
        super.onAttach(activity);

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
}
