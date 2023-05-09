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

	private LinearLayout nonValueLayout, valueLayout;
	private EditText fileId, fileType, dataToWrite, creditValueWrite, debitValueWrite;
	private TextView logData;
	private Button writeToFile, writeCredit, writeDebit;
	DesfireFile file;

	private View.OnClickListener listener, creditListener, debitListener;

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

		nonValueLayout = view.findViewById(R.id.llFileWriteNonValueLayout);
		valueLayout = view.findViewById(R.id.llFileWriteValueLayout);

		fileId = view.findViewById(R.id.etFileWriteFileId);
		fileType = view.findViewById(R.id.etFileWriteFileType);

		dataToWrite = view.findViewById(R.id.etFileWriteData);
		writeToFile = view.findViewById(R.id.btnWriteToFile);
		writeToFile.setOnClickListener(listener);

		creditValueWrite = view.findViewById(R.id.etFileWriteCreditValue);
		writeCredit = view.findViewById(R.id.btnFileWriteCredit);
		writeCredit.setOnClickListener(creditListener);
		debitValueWrite = view.findViewById(R.id.etFileWriteDebitValue);
		writeDebit = view.findViewById(R.id.btnFileWriteDebit);
		writeDebit.setOnClickListener(debitListener);

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
		if (file.getFileType().toString().equals("VALUE_FILE")) {
			nonValueLayout.setVisibility(View.GONE);
			valueLayout.setVisibility(View.VISIBLE);
		} else {
			nonValueLayout.setVisibility(View.VISIBLE);
			valueLayout.setVisibility(View.GONE);
		}
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
		this.creditListener = listener;
		this.debitListener = listener;
	}

	public String getCreditDataToWrite() {
		return creditValueWrite.getText().toString();
	}

	public void setCreditOnClickListener(View.OnClickListener creditListener) {
		this.creditListener = creditListener;
	}

	public String getDebitDataToWrite() {
		return debitValueWrite.getText().toString();
	}

	public void setDebitOnClickListener(View.OnClickListener debitListener) {
		this.debitListener = debitListener;
	}

	public void setLogData(String message) {
		logData.setText(message);
	}

}
