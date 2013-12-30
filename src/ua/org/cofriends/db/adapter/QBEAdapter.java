package ua.org.cofriends.db.adapter;

import java.util.ArrayList;
import java.util.List;

import ua.org.cofriends.db.R;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;

public class QBEAdapter extends ArrayAdapter<String> implements OnFocusChangeListener {
	
	private static final String TAG = "QBE";
	
	private final List<String> mUserInput;

	public QBEAdapter(Context context, List<String> columns) {
		super(context, R.layout.item_column, columns);
		
		mUserInput = new ArrayList<String>();
		for (int i = 0; i < columns.size(); i++) {
			mUserInput.add("");
		}
	}

	@Override
	public View getView(int position, View view, ViewGroup parent) {
		Log.d(TAG, "getView() with " + position);
		ViewHolder holder;
		if (view == null) {
			view = View.inflate(getContext(), R.layout.item_column, null);
			holder = new ViewHolder();
			holder.text = (TextView) view.findViewById(R.id.text_name);
			holder.edit = (EditText) view.findViewById(R.id.edit_value);
			holder.edit.setOnFocusChangeListener(this);
			view.setTag(holder);
		} else {
			holder = (ViewHolder) view.getTag();
		}
		holder.text.setText(getItem(position));
		holder.edit.setTag(Integer.valueOf(position));
		holder.edit.setText(mUserInput.get(position));
		
		return view;
	}
	
	/**
	 * @param position
	 * @return listener that remember edit text in focus
	 */
	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		if (!hasFocus) {
			int position = (Integer) v.getTag();
			mUserInput.set(position, ((EditText) v).getText().toString());
		}
	}

	private static class ViewHolder {
		public TextView text;
		public EditText edit;
	}

}
