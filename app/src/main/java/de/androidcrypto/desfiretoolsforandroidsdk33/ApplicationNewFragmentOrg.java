package de.androidcrypto.desfiretoolsforandroidsdk33;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * class added by MichaelsPlayground / AndroidCrypto
 */

public class ApplicationNewFragmentOrg extends Fragment {

	private EditText appId;
	private TextView logData;
	private Button createApplication;

	private View.OnClickListener listener;

	/*
	public void setApplications(List<DesfireApplication> applications) {
		this.applications = applications;
	}
	 */

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_application_new, container, false);

		appId = view.findViewById(R.id.etApplicationId);
		logData = view.findViewById(R.id.tvLog);
		createApplication = view.findViewById(R.id.btnCreateApplication);

		createApplication.setOnClickListener(listener);
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
	public String getAid() {return appId.getText().toString();}

}
