package de.androidcrypto.desfiretoolsforandroidsdk33;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.github.skjolber.desfire.ev1.model.DesfireApplication;

import java.util.List;

/**
 * class added by MichaelsPlayground / AndroidCrypto
 */

public class ApplicationNewFragmentOld extends Fragment {

	private List<DesfireApplication> applications;

	private ListView listView;

	private EditText appId;
	private Button createApplication;

	private OnItemClickListener listener;

	public void setApplications(List<DesfireApplication> applications) {
		this.applications = applications;
	}

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_application_new, container, false);

		appId = view.findViewById(R.id.etApplicationId);
		createApplication = view.findViewById(R.id.btnCreateApplication);

        //listView = (ListView)view.findViewById(R.id.listView);
        //listView.setOnItemClickListener(listener);
/*
        if(getActivity() != null) {
        	listView.setAdapter(new ApplicationListItemAdapter(getActivity(), applications));
        }
*/
        return view;
    }

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if(getView() != null) {
        	listView.setAdapter(new ApplicationListItemAdapter(activity, applications));
        }
    }

}
