package ua.org.cofriends.db.activity;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ua.org.cofriends.db.R;
import ua.org.cofriends.db.adapter.QBEAdapter;
import ua.org.cofriends.db.fragment.ConfirmationDialog;
import ua.org.cofriends.db.fragment.InformationDialog;
import ua.org.cofriends.db.utils.DatabaseHelper;
import ua.org.cofriends.db.view.SwipeDismissListViewTouchListener;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity implements OnClickListener, DialogInterface.OnClickListener {

	private enum Action {
		ADD, REMOVE
	}
	
	public static final String EXAMPLE_BUNDLE = "example_bundle";
	private static final String DIALOG = "confirmation_dialog";
	private EditText mEditNew;
	private ListView mListColumns;
	private String mRemovedItem;
	private Action mCurrentAction;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mEditNew = (EditText) findViewById(R.id.edit_new_column);
		mListColumns = (ListView) findViewById(R.id.list_columns);
		mListColumns.setEmptyView(findViewById(R.id.text_empty));
		findViewById(R.id.image_add).setOnClickListener(this);
		
		SwipeDismissListViewTouchListener touchListener = new SwipeDismissListViewTouchListener(mListColumns,
				new SwipeDismissListViewTouchListener.OnDismissCallback() {
					@Override
					public void onDismiss(ListView listView, int[] reverseSortedPositions) {
						for (int position : reverseSortedPositions) {
							mRemovedItem = listView.getItemAtPosition(position).toString();
							mCurrentAction = Action.REMOVE;
							ConfirmationDialog.newInstance(R.string.confirmation_title, R.string.drop_confirmation_message).show(
									getSupportFragmentManager(), DIALOG);
						}
					}
				});
		mListColumns.setOnTouchListener(touchListener);
		mListColumns.setOnScrollListener(touchListener.makeScrollListener());

		updateAdapter();
	}

	/**
	 * Renews list of columns available to user.
	 */
	private void updateAdapter() {
		if (mListColumns != null) {
			new UpdateTask(getApplicationContext(), mListColumns).execute();
		}
	}

	/**
	 * @return user query example
	 */
	private Map<String, String> getUserInput() {
		Map<String, String> result = new HashMap<String, String>();
		for (int i = 0; i < mListColumns.getCount(); i++) {
			ViewGroup group = (ViewGroup) mListColumns.getChildAt(i);
			TextView textName = (TextView) group.findViewById(R.id.text_name);
			EditText editValue = (EditText) group.findViewById(R.id.edit_value);
			result.put(textName.getText().toString(), editValue.getText().toString());
		}

		return result;
	}

	/**
	 * Forwards user to the result activity.
	 */
	private void queryExample() {
		Map<String, String> example = getUserInput();
		Bundle bundle = new Bundle();
		for (String name : example.keySet()) {
			bundle.putString(name, example.get(name));
		}
		Intent intent = new Intent(this, ResultActivity.class);
		intent.putExtra(EXAMPLE_BUNDLE, bundle);
		startActivity(intent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_add:
			new InsertTask(getApplicationContext(), getUserInput()).execute();
			break;

		case R.id.action_search:
			queryExample();
			break;

		case R.id.action_help:
			InformationDialog.newInstance(R.string.help_title, R.string.help_message)
				.show(getSupportFragmentManager(), DIALOG);
			break;
			
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.image_add:
			// we show confirmation, operation causes data loss
			mCurrentAction = Action.ADD;
			ConfirmationDialog.newInstance(R.string.confirmation_title, R.string.drop_confirmation_message).show(
					getSupportFragmentManager(), DIALOG);

			break;
		}
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		// yes, handler
		switch (mCurrentAction) {
		case ADD:
			DatabaseHelper.addNewColumn(getApplicationContext(), mEditNew.getText().toString());
			break;

		case REMOVE:
			DatabaseHelper.removeColumn(getApplicationContext(), mRemovedItem);
			break;
			
		default:
			break;
		}
		mCurrentAction = null;
		Toast.makeText(this, R.string.db_changed, Toast.LENGTH_LONG).show();

		dialog.dismiss();

		updateAdapter();
	}

	private static class InsertTask extends AsyncTask<Void, Void, Void> {

		private final Map<String, String> mExample;
		private final Context mContext;

		/**
		 * @param context
		 *            better off to pass an application context
		 * @param example
		 */
		public InsertTask(Context context, Map<String, String> example) {
			mExample = example;
			mContext = context;
		}

		@Override
		protected Void doInBackground(Void... params) {
			DatabaseHelper helper = DatabaseHelper.newInstance(mContext);
			helper.insert(mExample);

			helper.close();
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			Toast.makeText(mContext, R.string.action_add, Toast.LENGTH_LONG).show();
		}
	}

	private static class UpdateTask extends AsyncTask<Void, Void, List<String>> {

		private final WeakReference<ListView> mListColumns;
		private final Context mContext;

		/**
		 * @param context
		 *            better off to pass an application context
		 * @param listView
		 *            to be populated
		 */
		public UpdateTask(Context context, ListView listView) {
			mListColumns = new WeakReference<ListView>(listView);
			mContext = context;
		}

		@Override
		protected List<String> doInBackground(Void... params) {
			DatabaseHelper helper = DatabaseHelper.newInstance(mContext);
			List<String> result = helper.getColumns();

			helper.close();

			return result;
		}

		@Override
		protected void onPostExecute(List<String> result) {
			if (mListColumns.get() != null) {
				ArrayAdapter<String> adapter = new QBEAdapter(mContext, result);

				mListColumns.get().setAdapter(adapter);
				adapter.notifyDataSetChanged();
			}
		}
	}

}
