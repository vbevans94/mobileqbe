package ua.org.cofriends.db.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class ConfirmationDialog extends DialogFragment {

	protected static final String MESSAGE = "message";
	protected static final String TITLE = "title";

	public static DialogFragment newInstance(int title, int message) {
		Bundle args = new Bundle();
		args.putInt(TITLE, title);
		args.putInt(MESSAGE, message);
		DialogFragment fragment = new ConfirmationDialog();
		fragment.setArguments(args);
		
		return fragment;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		if (!(getActivity() instanceof DialogInterface.OnClickListener)) {
			throw new IllegalArgumentException("Parent activity must implement DialogInterface!");
		}
		OnClickListener yesListener = (OnClickListener) getActivity();
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setMessage(getArguments().getInt(MESSAGE))
				.setTitle(getArguments().getInt(TITLE))
				.setCancelable(false)
				.setPositiveButton(android.R.string.yes, yesListener).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});

		return builder.create();
	}

}
