package de.androidcrypto.desfiretoolsforandroidsdk33;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ReplacementSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.skjolber.desfire.ev1.model.DesfireApplication;
import com.shawnlin.numberpicker.NumberPicker;


import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * class added by MichaelsPlayground / AndroidCrypto
 */

public class ApplicationNewFragment extends Fragment {

	// TODO: Rename parameter arguments, choose names that match
	// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
	private static final String ARG_PARAM1 = "param1";
	private static final String ARG_PARAM2 = "param2";
	private static final String TAG = "ApplicationNewFragment";

	// TODO: Rename and change types of parameters
	private String mParam1;
	private String mParam2;

	public ApplicationNewFragment() {
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
	public static ApplicationNewFragment newInstance(String param1, String param2) {
		ApplicationNewFragment fragment = new ApplicationNewFragment();
		Bundle args = new Bundle();
		args.putString(ARG_PARAM1, param1);
		args.putString(ARG_PARAM2, param2);
		fragment.setArguments(args);
		return fragment;
	}

	private TextView logData;
	//private EditText appId;
	private EditText appId;
	com.google.android.material.textfield.TextInputEditText appId2;
	private com.shawnlin.numberpicker.NumberPicker aid1, aid2, aid3, aid4, aid5, aid6;
	private Button createApplication;
	private View.OnClickListener listener;


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
		return inflater.inflate(R.layout.fragment_application_new, container, false);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		appId = view.findViewById(R.id.etApplicationId);
		appId2= view.findViewById(R.id.etAppNewAppId);
		aid1 = view.findViewById(R.id.npAid1);
		aid2 = view.findViewById(R.id.npAid2);
		logData = view.findViewById(R.id.tvLog);
		createApplication = view.findViewById(R.id.btnCreateApplication);

		createApplication.setOnClickListener(listener);
		appId2.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);

		appId2.setText("000001");


		String[] data = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"};
		String[] aids = new String[6];
		aid1.setMinValue(1);
		aid1.setMaxValue(data.length);
		aid1.setDisplayedValues(data);
		aid1.setValue(0);
		aid2.setMinValue(1);
		aid2.setMaxValue(data.length);
		aid2.setDisplayedValues(data);
		aid2.setValue(8);

		// OnValueChangeListener
		aid1.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
			@Override
			public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
				Log.d(TAG, String.format(Locale.US, "oldVal: %d, newVal: %d", oldVal, newVal));
				aids[0] = data[newVal-1];
				showAid(aids);
			}
		});
		aid2.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
			@Override
			public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
				Log.d(TAG, String.format(Locale.US, "oldVal: %d, newVal: %d", oldVal, newVal));
				aids[1] = data[newVal-1];
				showAid(aids);
			}
		});

	}

	private void showAid(String[] aids) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 6; i++) {
			sb.append(aids[i]);
		}
		appId2.setText(sb.toString());
	}

	public void setOnClickListener(View.OnClickListener listener) {
		this.listener = listener;
	}

	public void setLogData(String message) {
		logData.setText(message);
	}

	public String getAid() {return appId.getText().toString();}
}


