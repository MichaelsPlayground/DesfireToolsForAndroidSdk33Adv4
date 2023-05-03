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
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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
	com.google.android.material.textfield.TextInputEditText appId;
	private com.shawnlin.numberpicker.NumberPicker aid1, aid2, aid3, aid4, aid5, aid6, numberOfKeys;
	private AutoCompleteTextView choiceTypeOfKeys;
	String[] data = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"};
	private String[] aids = new String[6];
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

		appId= view.findViewById(R.id.etAppNewAppId);
		aid1 = view.findViewById(R.id.npAid1);
		aid2 = view.findViewById(R.id.npAid2);
		aid3 = view.findViewById(R.id.npAid3);
		aid4 = view.findViewById(R.id.npAid4);
		aid5 = view.findViewById(R.id.npAid5);
		aid6 = view.findViewById(R.id.npAid6);
		choiceTypeOfKeys = view.findViewById(R.id.spTypeOfKeys);
		numberOfKeys = view.findViewById(R.id.npNumberOfKeys);
		logData = view.findViewById(R.id.tvLog);
		createApplication = view.findViewById(R.id.btnCreateApplication);

		createApplication.setOnClickListener(listener);
		appId.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);

		appId.setText("000001");

		initNumberPicker();

		String[] typeKeys = new String[]{"DES",
				"TDES",
				"AES",
		};
		ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
				getContext(),
				R.layout.drop_down_item,
				typeKeys);
		choiceTypeOfKeys.setAdapter(arrayAdapter);

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
		aid3.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
			@Override
			public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
				Log.d(TAG, String.format(Locale.US, "oldVal: %d, newVal: %d", oldVal, newVal));
				aids[2] = data[newVal-1];
				showAid(aids);
			}
		});
		aid4.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
			@Override
			public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
				Log.d(TAG, String.format(Locale.US, "oldVal: %d, newVal: %d", oldVal, newVal));
				aids[3] = data[newVal-1];
				showAid(aids);
			}
		});
		aid5.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
			@Override
			public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
				Log.d(TAG, String.format(Locale.US, "oldVal: %d, newVal: %d", oldVal, newVal));
				aids[4] = data[newVal-1];
				showAid(aids);
			}
		});
		aid6.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
			@Override
			public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
				Log.d(TAG, String.format(Locale.US, "oldVal: %d, newVal: %d", oldVal, newVal));
				aids[5] = data[newVal-1];
				showAid(aids);
			}
		});

	}

	private void initNumberPicker() {
		aid1.setMinValue(1);
		aid1.setMaxValue(data.length);
		aid1.setDisplayedValues(data);
		aid1.setValue(1);
		aid2.setMinValue(1);
		aid2.setMaxValue(data.length);
		aid2.setDisplayedValues(data);
		aid2.setValue(1);
		aid3.setMinValue(1);
		aid3.setMaxValue(data.length);
		aid3.setDisplayedValues(data);
		aid3.setValue(1);
		aid4.setMinValue(1);
		aid4.setMaxValue(data.length);
		aid4.setDisplayedValues(data);
		aid4.setValue(1);
		aid5.setMinValue(1);
		aid5.setMaxValue(data.length);
		aid5.setDisplayedValues(data);
		aid5.setValue(1);
		aid6.setMinValue(1);
		aid6.setMaxValue(data.length);
		aid6.setDisplayedValues(data);
		aid6.setValue(2);
		for (int i = 0; i < 5; i++) {
			aids[i] = "0";
		}
		aids[5] = "1";
	}

	private void showAid(String[] aids) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 6; i++) {
			sb.append(aids[i]);
		}
		appId.setText(sb.toString());
	}

	public void setOnClickListener(View.OnClickListener listener) {
		this.listener = listener;
	}

	public void setLogData(String message) {
		logData.setText(message);
	}

	public String getAid() {return appId.getText().toString();}

	public String getChoiceTypeOfKeys() {
		return choiceTypeOfKeys.getText().toString();
	}

	public int getNumberOfKeys() {
		return numberOfKeys.getValue();
	}
}


