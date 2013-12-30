package ua.org.cofriends.db.activity;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ua.org.cofriends.db.R;
import ua.org.cofriends.db.fragment.ConfirmationDialog;
import ua.org.cofriends.db.utils.DatabaseHelper;
import ua.org.cofriends.db.view.SwipeDismissListViewTouchListener;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ResultActivity extends ActionBarActivity implements DialogInterface.OnClickListener {

	private static final String DIVIVER = " - ";
	protected static final String DIALOG = "dialog";
	private ListView mListResult;
	protected String mRemovedItem;

	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);

		setContentView(R.layout.activity_result);

		mListResult = (ListView) findViewById(R.id.list_result);

		SwipeDismissListViewTouchListener touchListener = new SwipeDismissListViewTouchListener(mListResult,
				new SwipeDismissListViewTouchListener.OnDismissCallback() {
					@Override
					public void onDismiss(ListView listView, int[] reverseSortedPositions) {
						for (int position : reverseSortedPositions) {
							mRemovedItem = listView.getItemAtPosition(position).toString();
							ConfirmationDialog.newInstance(R.string.confirmation_title, R.string.remove_confirmation_message).show(
									getSupportFragmentManager(), DIALOG);
						}
					}
				});
		mListResult.setOnTouchListener(touchListener);
		mListResult.setOnScrollListener(touchListener.makeScrollListener());

		updateAdapter();

		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			finish();
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * Renews result set.
	 */
	private void updateAdapter() {
		if (mListResult != null) {
			Bundle bundle = getIntent().getBundleExtra(MainActivity.EXAMPLE_BUNDLE);
			Map<String, String> example = new HashMap<String, String>();
			StringBuilder header = new StringBuilder(getString(R.string.id) + DIVIVER);
			for (String name : bundle.keySet()) {
				example.put(name, bundle.getString(name));
				header.append(name + DIVIVER);
			}
			if (mListResult.getHeaderViewsCount() == 0) {
				TextView textHeader = (TextView) View.inflate(getApplicationContext(), R.layout.item_header, null);
				textHeader.setText(header.subSequence(0, header.length() - DIVIVER.length()).toString());
				mListResult.addHeaderView(textHeader);
			}
			new UpdateTask(getApplicationContext(), mListResult, example).execute();
		}
	}

	private static class UpdateTask extends AsyncTask<Void, Void, List<String>> {

		private final WeakReference<ListView> mListResult;
		private final Context mContext;
		private final Map<String, String> mExample;

		/**
		 * @param context
		 *            better off to pass an application context
		 * @param listView
		 *            to be populated
		 */
		public UpdateTask(Context context, ListView listView, Map<String, String> example) {
			mListResult = new WeakReference<ListView>(listView);
			mExample = example;
			mContext = context;
		}

		@Override
		protected List<String> doInBackground(Void... params) {
			DatabaseHelper helper = DatabaseHelper.newInstance(mContext);
			List<String> result = helper.query(mExample);

			helper.close();

			return result;
		}

		@Override
		protected void onPostExecute(List<String> result) {
			if (mListResult.get() != null) {
				ArrayAdapter<String> adapter = new ArrayAdapter<String>(mContext, R.layout.item_result, result);

				mListResult.get().setAdapter(adapter);
				adapter.notifyDataSetChanged();
			}
		}
	}
	
	private static class RemoveTask extends AsyncTask<Void, Void, Void> {

		private final WeakReference<ResultActivity> mActivity;
		private final String mId;

		/**
		 * @param context
		 *            better off to pass an application context
		 * @param listView
		 *            to be populated
		 */
		public RemoveTask(ResultActivity activity, String id) {
			mId = id;
			mActivity = new WeakReference<ResultActivity>(activity);
		}

		@Override
		protected Void doInBackground(Void... params) {
			DatabaseHelper helper = DatabaseHelper.newInstance(mActivity.get().getApplicationContext());
			helper.remove(mId);

			helper.close();

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			if (mActivity.get() != null) {
				Toast.makeText(mActivity.get(), R.string.removed, Toast.LENGTH_LONG).show();
				mActivity.get().updateAdapter();
			}
		}
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		new RemoveTask(ResultActivity.this, mRemovedItem.split(DIVIVER)[0]).execute();
	}

}
