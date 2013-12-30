package ua.org.cofriends.db.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class InformationDialog extends DialogFragment {

	private static final String MESSAGE = "message";
	private static final String TITLE = "title";

	public static DialogFragment newInstance(int title, int message) {
		Bundle args = new Bundle();
		args.putInt(TITLE, title);
		args.putInt(MESSAGE, message);
		DialogFragment fragment = new InformationDialog();
		fragment.setArguments(args);

		return fragment;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setMessage(getArguments().getInt(MESSAGE)).setTitle(getArguments().getInt(TITLE)).setCancelable(false)
				.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});

		return builder.create();
	}

}
