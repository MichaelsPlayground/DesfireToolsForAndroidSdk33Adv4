package de.androidcrypto.desfiretoolsforandroidsdk33;

import java.util.List;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.skjolber.desfire.ev1.model.DesfireApplication;

public class ApplicationListFragment extends Fragment {

	// TODO: Rename parameter arguments, choose names that match
	// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
	private static final String ARG_PARAM1 = "param1";
	private static final String ARG_PARAM2 = "param2";
	private static final String TAG = "ReadFragment";

	// TODO: Rename and change types of parameters
	private String mParam1;
	private String mParam2;

	public ApplicationListFragment() {
		// Required empty public constructor
	}

	/**
	 * Use this factory method to create a new instance of
	 * this fragment using the provided parameters.
	 *
	 * @param param1 Parameter 1.
	 * @param param2 Parameter 2.
	 * @return A new instance of fragment ReceiveFragment.
	 */
	// TODO: Rename and change types and number of parameters
	public static ApplicationListFragment newInstance(String param1, String param2) {
		ApplicationListFragment fragment = new ApplicationListFragment();
		Bundle args = new Bundle();
		args.putString(ARG_PARAM1, param1);
		args.putString(ARG_PARAM2, param2);
		fragment.setArguments(args);
		return fragment;
	}

	private List<DesfireApplication> applications;
	
	private ListView listView;
	
	private OnItemClickListener listener;
	
	public void setApplications(List<DesfireApplication> applications) {
		this.applications = applications;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			mParam1 = getArguments().getString(ARG_PARAM1);
			mParam2 = getArguments().getString(ARG_PARAM2);
		}
		//contextSave = getActivity().getApplicationContext();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_application_list, container, false);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		listView = (ListView)view.findViewById(R.id.listView);
		listView.setOnItemClickListener(listener);

		if(getActivity() != null) {
			listView.setAdapter(new ApplicationListItemAdapter(getActivity(), applications));
		}
	}
	
	/*
	@Override
    public View onCreateView2(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_application_list, container, false);
        
        listView = (ListView)view.findViewById(R.id.listView);
        listView.setOnItemClickListener(listener);

        if(getActivity() != null) {
        	listView.setAdapter(new ApplicationListItemAdapter(getActivity(), applications));
        }
        
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
	*/

	public void setOnItemClickListener(OnItemClickListener listener) {
		this.listener = listener;
	}
}
