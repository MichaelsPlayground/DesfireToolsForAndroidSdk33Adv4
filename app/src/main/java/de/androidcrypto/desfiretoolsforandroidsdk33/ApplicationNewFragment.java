package de.androidcrypto.desfiretoolsforandroidsdk33;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ReplacementSpan;
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

import java.util.List;

/**
 * class added by MichaelsPlayground / AndroidCrypto
 */

public class ApplicationNewFragment extends Fragment {

	// TODO: Rename parameter arguments, choose names that match
	// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
	private static final String ARG_PARAM1 = "param1";
	private static final String ARG_PARAM2 = "param2";
	private static final String TAG = "ReadFragment";

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
	private EditText appId;
	com.google.android.material.textfield.TextInputEditText appId2;
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
		logData = view.findViewById(R.id.tvLog);
		createApplication = view.findViewById(R.id.btnCreateApplication);

		createApplication.setOnClickListener(listener);



		appId2.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {}

			@Override
			public void afterTextChanged(Editable editable) {
				Object[] paddingSpans = editable.getSpans(0, editable.length(), SpaceSpan.class);
				for (Object span : paddingSpans) {
					editable.removeSpan(span);
				}

				addSpans(editable);
			}

			private static final int GROUP_SIZE = 1;

			private void addSpans(Editable editable) {

				final int length = editable.length();
				for (int i = 1; i * (GROUP_SIZE) < length; i++) {
					int index = i * GROUP_SIZE;
					editable.setSpan(new SpaceSpan(), index - 1, index,
							Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				}
			}
		});

		appId2.setText("000001");
	}

	public void setOnClickListener(View.OnClickListener listener) {
		this.listener = listener;
	}

	public void setLogData(String message) {
		logData.setText(message);
	}

	public String getAid() {return appId.getText().toString();}
}

/**
 * A {@link ReplacementSpan} used for spacing in {@link android.widget.EditText}
 * to space things out. Adds ' 's
 */
class SpaceSpan extends ReplacementSpan {

	@Override
	public int getSize(@NonNull Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {
		float padding = paint.measureText(" ", 0, 1);
		float textSize = paint.measureText(text, start, end);
		return (int) (padding + textSize);
	}

	@Override
	public void draw(@NonNull Canvas canvas, CharSequence text, int start, int end, float x, int top, int y,
					 int bottom, @NonNull Paint paint) {
		canvas.drawText(text.subSequence(start, end) + " ", x, y, paint);
	}
}

