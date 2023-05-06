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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.skjolber.desfire.ev1.model.DesfireApplication;
import com.github.skjolber.desfire.ev1.model.file.DesfireFile;
import com.shawnlin.numberpicker.NumberPicker;

/**
 * class added by MichaelsPlayground / AndroidCrypto
 */

// todo use Bundle instead of a constructor for parameter
@SuppressLint("ValidFragment")
public class FileWriteFragment extends Fragment {

	// todo block file creation if application id = 00 00 00 = master file application

	private EditText fileId, fileType, dataToWrite;
	private TextView logData;
	private Button writeToFile;
	DesfireFile file;

	private View.OnClickListener listener;

	// todo use Bundle instead of a constructor for parameter
	@SuppressLint("ValidFragment")
	public FileWriteFragment(DesfireFile file) {
		this.file = file;
	}

	/*
	public void setApplications(List<DesfireApplication> applications) {
		this.applications = applications;
	}
	 */

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_file_write, container, false);

		fileId = view.findViewById(R.id.etFileWriteFileId);
		fileType = view.findViewById(R.id.etFileWriteFileType);
		dataToWrite = view.findViewById(R.id.etFileWriteData);
		writeToFile = view.findViewById(R.id.btnWriteToFile);
		writeToFile.setOnClickListener(listener);
		logData = view.findViewById(R.id.tvLog);
        return view;
    }

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		logData.setText("Write to file for fileID " + file.getIdString());
		initFileData();
	}

	private void initFileData() {
		fileId.setText(file.getIdString());
		fileType.setText(file.getFileType().toString());
	}

	public void setFileId(String fileIdString) {
		this.fileId.setText(fileIdString);
	}

	public void setFileType(String fileTypeString) {
		this.fileType.setText(fileTypeString);
	}

	public String getDataToWrite() {
		return dataToWrite.getText().toString();
	}

	public void setOnClickListener(View.OnClickListener listener) {
		this.listener = listener;
	}

	public void setLogData(String message) {
		logData.setText(message);
	}

}
