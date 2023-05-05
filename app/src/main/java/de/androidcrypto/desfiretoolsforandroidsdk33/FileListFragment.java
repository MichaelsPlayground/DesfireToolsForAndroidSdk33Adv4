package de.androidcrypto.desfiretoolsforandroidsdk33;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.github.skjolber.desfire.ev1.model.DesfireApplication;
import com.github.skjolber.desfire.ev1.model.DesfireApplicationKey;
import com.github.skjolber.desfire.ev1.model.DesfireApplicationKeySettings;
import com.github.skjolber.desfire.ev1.model.file.DesfireFile;
import com.github.skjolber.desfire.ev1.model.key.DesfireKey;

import de.androidcrypto.desfiretoolsforandroidsdk33.filelist.ApplicationDetail;
import de.androidcrypto.desfiretoolsforandroidsdk33.filelist.ApplicationDetailApplicationKey;
import de.androidcrypto.desfiretoolsforandroidsdk33.filelist.ApplicationDetailFile;
import de.androidcrypto.desfiretoolsforandroidsdk33.filelist.ApplicationDetailHeader;
import de.androidcrypto.desfiretoolsforandroidsdk33.filelist.ApplicationDetailSetting;

public class FileListFragment extends Fragment {

	// TODO: Rename parameter arguments, choose names that match
	// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
	private static final String ARG_PARAM1 = "param1";
	private static final String ARG_PARAM2 = "param2";
	private static final String TAG = "ReadFragment";

	// TODO: Rename and change types of parameters
	private String mParam1;
	private String mParam2;

	public FileListFragment() {
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
	public static FileListFragment newInstance(String param1, String param2) {
		FileListFragment fragment = new FileListFragment();
		Bundle args = new Bundle();
		args.putString(ARG_PARAM1, param1);
		args.putString(ARG_PARAM2, param2);
		fragment.setArguments(args);
		return fragment;
	}

	private DesfireApplication application;

	private ListView listView;
	
	private OnItemClickListener listener;

	private Button changeApplicationSettings; // added
	private View.OnClickListener buttonListener; // added
	
	private FileListItemAdapter adapter;

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
		return inflater.inflate(R.layout.fragment_file_list, container, false);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		listView = (ListView)view.findViewById(R.id.listView);
		listView.setOnItemClickListener(listener);
		changeApplicationSettings = view.findViewById(R.id.btnChangeApplicationSettings);
		changeApplicationSettings.setOnClickListener(buttonListener);
		init(view, getActivity());
	}

	private void init(View view, FragmentActivity activity) {
		if(view != null && activity != null) {
			List<ApplicationDetail> details = new ArrayList<ApplicationDetail>();
			
			details.add(new ApplicationDetailHeader(activity.getString(R.string.applicationId, "0x" + application.getIdString())));

        	DesfireApplicationKeySettings settings = application.getKeySettings();
			
            if(settings.isConfigurationChangable()) {
    			details.add(new ApplicationDetailSetting(getString(R.string.applicationConfigurationChangable), activity.getString(R.string.applicationKeySettingYes)));
            } else {
    			details.add(new ApplicationDetailSetting(getString(R.string.applicationConfigurationChangable), activity.getString(R.string.applicationKeySettingNo)));
            }
                       	
			details.add(new ApplicationDetailHeader(activity.getString(R.string.fileList)));
			
            if(settings.isFreeCreateAndDelete()) {
    			details.add(new ApplicationDetailSetting(getString(R.string.applicationFreeCreateAndDelete), activity.getString(R.string.applicationKeySettingYes)));
            } else {
    			details.add(new ApplicationDetailSetting(getString(R.string.applicationFreeCreateAndDelete), activity.getString(R.string.applicationKeySettingNo)));
            }

            if(settings.isFreeDirectoryAccess()) {
    			details.add(new ApplicationDetailSetting(getString(R.string.applicationFreeDirectoryList), activity.getString(R.string.applicationKeySettingYes)));
            } else {
    			details.add(new ApplicationDetailSetting(getString(R.string.applicationFreeDirectoryList), activity.getString(R.string.applicationKeySettingNo)));
            }

			List<DesfireFile> files = application.getFiles();
			for(DesfireFile file : files) {
				
		       	String title = activity.getString(R.string.fileId, "0x" + Integer.toHexString(file.getId()).toUpperCase(Locale.ROOT));
		        
		        int communicationSettingString;
		        switch(file.getCommunicationSettings()) {
		        	case ENCIPHERED : {
		        		communicationSettingString = R.string.fileCommuncationTypeEnciphered;
		        		break;
		        	}
		        	case PLAIN: {
		        		communicationSettingString = R.string.fileCommuncationTypePlain;
		        		break;
		        	}
		        	case PLAIN_MAC: {
		        		communicationSettingString = R.string.fileCommuncationTypePlainMac;
		        		break;
		        	}
		        	default : {
		        		throw new IllegalArgumentException();
		        	}
		        }
		        
		        int fileTypeString;
		        switch(file.getFileType()) {
		        	case BACKUP_DATA_FILE : {
		        		fileTypeString = R.string.fileTypeBackup;
		        		
		        		break;
		        	}
		        	case CYCLIC_RECORD_FILE: {
		        		fileTypeString = R.string.fileTypeCyclicRecord;
		        		
		        		break;
		        	}
		        	case LINEAR_RECORD_FILE : {
		        		fileTypeString = R.string.fileTypeLinearRecord;
		        		
		        		break;
		        	}
		        	case STANDARD_DATA_FILE: {
		        		fileTypeString = R.string.fileTypeStandard;
		        		
		        		break;
		        	}
		        	case VALUE_FILE: {
		        		fileTypeString = R.string.fileTypeValue;
		        		
		        		break;
		        	}
		        	default : {
		        		throw new IllegalArgumentException();
		        	}
		        }
		        
		        String description = activity.getString(R.string.fileDescription, activity.getString(fileTypeString), activity.getString(communicationSettingString));

		        String access;
		        if(file.isFreeReadWriteAccess()) {
		        	access = activity.getString(R.string.fileAccessSummaryFree);
		        } else {
		        	access = activity.getString(R.string.fileAccessSummary, translateAccessKey(file.getReadAccessKey(), activity),  translateAccessKey(file.getWriteAccessKey(), activity),  translateAccessKey(file.getReadWriteAccessKey(), activity),  translateAccessKey(file.getChangeAccessKey(), activity));
		        }
		        details.add(new ApplicationDetailFile(title, description, file, access));
			}

			details.add(new ApplicationDetailHeader(activity.getString(R.string.keyList)));

            if(settings.isCanChangeMasterKey()) {
    			details.add(new ApplicationDetailSetting(getString(R.string.applicationCanChangeMasterKey), activity.getString(R.string.applicationKeySettingYes)));
            } else {
    			details.add(new ApplicationDetailSetting(getString(R.string.applicationCanChangeMasterKey), activity.getString(R.string.applicationKeySettingNo)));
            }

			details.add(new ApplicationDetailSetting(getString(R.string.applicationChangeKeyAccessRights), Integer.toString(settings.getChangeKeyAccessRights())));

            details.add(new ApplicationDetailSetting(getString(R.string.applicationKeys), Integer.toString(settings.getMaxKeys())));

			List<DesfireApplicationKey> keys = application.getKeys();
			for(DesfireApplicationKey desfireApplicationKey : keys) {
				DesfireKey key = desfireApplicationKey.getDesfireKey();
				// todo found something wired and red dotted, should be ok now
				//details.add(new ApplicationDetailApplicationKey(activity.getString(R.string.key, desfireApplicationKey.getIndex()), activity.getString(R.string.keyVersion, Integer.toHexString(key.getVersion())), desfireApplicationKey));
				details.add(new ApplicationDetailApplicationKey(activity.getString(R.string.key, String.valueOf(desfireApplicationKey.getIndex())), activity.getString(R.string.keyVersion, Integer.toHexString(key.getVersion())), desfireApplicationKey));
			}

			adapter = new FileListItemAdapter(getActivity(), details);
        	listView.setAdapter(adapter);
        	
     
		}
	}

	private String translateAccessKey(int key, Context context) {
		if(key == 14) {
			return context.getString(R.string.fileAccessFree);
		}
		return Integer.toString(key);
	}

	public ApplicationDetail getApplicationDetail(int position) {
		return (ApplicationDetail) adapter.getItem(position);
	}

	public void setApplication(DesfireApplication application) {
		this.application = application;
	}
	
	public void setOnItemClickListener(OnItemClickListener listener) {
		this.listener = listener;
	}

	public void setOnButtonClickListener(View.OnClickListener buttonListener) {this.buttonListener = buttonListener; } // added
	public void setButtonEnabled(boolean buttonEnabled) {this.changeApplicationSettings.setEnabled(buttonEnabled);}

}
