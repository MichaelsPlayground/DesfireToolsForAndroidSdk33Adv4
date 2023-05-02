package de.androidcrypto.desfiretoolsforandroidsdk33;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.github.skjolber.desfire.ev1.model.command.Utils;
import com.github.skjolber.desfire.ev1.model.file.DesfireFile;
import com.github.skjolber.desfire.ev1.model.file.RecordDesfireFile;
import com.github.skjolber.desfire.ev1.model.file.StandardDesfireFile;
import com.github.skjolber.desfire.ev1.model.file.ValueDesfireFile;

import de.androidcrypto.desfiretoolsforandroidsdk33.filelist.ApplicationDetail;
import de.androidcrypto.desfiretoolsforandroidsdk33.filelist.ApplicationDetailAccessKey;
import de.androidcrypto.desfiretoolsforandroidsdk33.filelist.ApplicationDetailHeader;
import de.androidcrypto.desfiretoolsforandroidsdk33.filelist.ApplicationDetailRecord;
import de.androidcrypto.desfiretoolsforandroidsdk33.filelist.ApplicationDetailSetting;


public class FileFragment extends Fragment {

	// TODO: Rename parameter arguments, choose names that match
	// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
	private static final String ARG_PARAM1 = "param1";
	private static final String ARG_PARAM2 = "param2";
	private static final String TAG = "ReadFragment";

	// TODO: Rename and change types of parameters
	private String mParam1;
	private String mParam2;

	public FileFragment() {
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
	public static FileFragment newInstance(String param1, String param2) {
		FileFragment fragment = new FileFragment();
		Bundle args = new Bundle();
		args.putString(ARG_PARAM1, param1);
		args.putString(ARG_PARAM2, param2);
		fragment.setArguments(args);
		return fragment;
	}

	private DesfireFile file;
	
	private ListView listView;
	
	private OnItemClickListener listener;
	
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
		return inflater.inflate(R.layout.fragment_file, container, false);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		listView = (ListView)view.findViewById(R.id.listView);
		listView.setOnItemClickListener(listener);
		init(view, getActivity());
	}
	
	private void init(View view, FragmentActivity activity) {
		if(view != null && activity != null) {
			List<ApplicationDetail> details = new ArrayList<ApplicationDetail>();
			
			details.add(new ApplicationDetailHeader(activity.getString(R.string.fileId, file.getIdString())));

	        int communicationSettingString;
	        switch(file.getCommunicationSettings()) {
	        	case ENCIPHERED : {
	        		communicationSettingString = R.string.fileCommuncationsTypeEnciphered;
	        		break;
	        	}
	        	case PLAIN: {
	        		communicationSettingString = R.string.fileCommuncationsTypePlain;
	        		break;
	        	}
	        	case PLAIN_MAC: {
	        		communicationSettingString = R.string.fileCommuncationsTypePlainMac;
	        		break;
	        	}
	        	default : {
	        		throw new IllegalArgumentException();
	        	}
	        }
	        
			details.add(new ApplicationDetailSetting(getString(R.string.fileCommuncations), activity.getString(communicationSettingString)));
	        
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

			details.add(new ApplicationDetailSetting(getString(R.string.fileType), activity.getString(fileTypeString)));

			if(file instanceof StandardDesfireFile) {
				StandardDesfireFile standardDesfireFile = (StandardDesfireFile)file;
				
				details.add(new ApplicationDetailSetting(getString(R.string.fileSize), getString(R.string.fileSizeBytes, standardDesfireFile.getFileSize())));

				// todo AndroidCrypto added the data read from file
				byte[] data = standardDesfireFile.getData();
				if (data != null) {
					// sometimes the getData has an appended byte
					if (data.length > standardDesfireFile.getFileSize()) {
						data = Arrays.copyOf(data, standardDesfireFile.getFileSize());
					}
					details.add(new ApplicationDetailHeader(activity.getString(R.string.fileStandard)));
					details.add(new ApplicationDetailRecord(activity.getString(R.string.fileStandardHex), Utils.getHexString(data), data));
					details.add(new ApplicationDetailRecord(activity.getString(R.string.fileStandardString), new String(data, StandardCharsets.UTF_8), data));
				}

			} else if(file instanceof ValueDesfireFile) {
				ValueDesfireFile valueDesfireFile = (ValueDesfireFile)file;

				if(valueDesfireFile.isValue()) {
					details.add(new ApplicationDetailSetting(getString(R.string.fileValue), Integer.toString(valueDesfireFile.getValue())));
				} else {
					details.add(new ApplicationDetailSetting(getString(R.string.fileValue), "-"));
				}

				details.add(new ApplicationDetailSetting(getString(R.string.fileValueLowerLimit), Integer.toString(valueDesfireFile.getLowerLimit())));
				details.add(new ApplicationDetailSetting(getString(R.string.fileValueUpperLimit), Integer.toString(valueDesfireFile.getUpperLimit())));
				details.add(new ApplicationDetailSetting(getString(R.string.fileValueLimitedCredit), getString(valueDesfireFile.isLimitedCredit()? R.string.fileValueLimitedCreditYes : R.string.fileValueLimitedCreditNo)));
				details.add(new ApplicationDetailSetting(getString(R.string.fileValueLimitedCreditValue), Integer.toString(valueDesfireFile.getLowerLimit())));
			} else if(file instanceof RecordDesfireFile) {
				RecordDesfireFile recordDesfireFile = (RecordDesfireFile)file;

				details.add(new ApplicationDetailSetting(getString(R.string.fileRecordRecordSize), Integer.toString(recordDesfireFile.getRecordSize())));
				details.add(new ApplicationDetailSetting(getString(R.string.fileRecordMaxRecords), Integer.toString(recordDesfireFile.getMaxRecords())));
				details.add(new ApplicationDetailSetting(getString(R.string.fileRecordCurrentRecords), Integer.toString(recordDesfireFile.getCurrentRecords())));
				

				if(recordDesfireFile.isRecords()) {
					details.add(new ApplicationDetailHeader(activity.getString(R.string.fileRecords)));
					
					byte[] value = recordDesfireFile.getRecords();
					
					int recordSize = recordDesfireFile.getRecordSize();
					for(int i = 0; i < recordDesfireFile.getCurrentRecords(); i ++) {
						byte[] record = new byte[recordSize];
						System.arraycopy(value, i * recordSize, record, 0, recordSize);
						details.add(new ApplicationDetailRecord(activity.getString(R.string.fileRecordRecord, i), Utils.getHexString(record), record));
					}
				}
				
			}

			details.add(new ApplicationDetailHeader(activity.getString(R.string.keyList)));
			
			details.add(new ApplicationDetailAccessKey(getString(R.string.fileReadAccessKey), translateAccessKey(file.getReadAccessKey(), activity), file.getReadAccessKey()));
			details.add(new ApplicationDetailAccessKey(getString(R.string.fileWriteAccessKey), translateAccessKey(file.getWriteAccessKey(), activity), file.getWriteAccessKey()));
			details.add(new ApplicationDetailAccessKey(getString(R.string.fileReadWriteAccessKey), translateAccessKey(file.getReadWriteAccessKey(), activity), file.getReadWriteAccessKey()));
			details.add(new ApplicationDetailAccessKey(getString(R.string.fileChangeAccessKey), translateAccessKey(file.getChangeAccessKey(), activity), file.getChangeAccessKey()));
			
			adapter = new FileListItemAdapter(getActivity(), details);
        	listView.setAdapter(adapter);
		}
	}

	public ApplicationDetail getApplicationDetail(int position) {
		return (ApplicationDetail) adapter.getItem(position);
	}
	
	public void setFile(DesfireFile file) {
		this.file = file;
	}
	
	public void setOnItemClickListener(OnItemClickListener listener) {
		this.listener = listener;
	}

	private String translateAccessKey(int key, Context context) {
		if(key == 14) {
			return context.getString(R.string.fileAccessSummaryFree);
		}
		return Integer.toString(key);
	}
	
	public DesfireFile getFile() {
		return file;
	}
}
