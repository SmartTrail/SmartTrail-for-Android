/**
 * GeoZen LLC
 * Copyright 2011. All rights reserved.
 */

package com.geozen.smarttrail.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.widget.Toast;

import com.geozen.smarttrail.R;

/**
 * A helper for showing EULAs and storing a {@link SharedPreferences} bit
 * indicating whether the user has accepted.
 */
public class EulaHelper {
	public static boolean hasAcceptedEula(final Context context) {
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(context);
		return sp.getBoolean("accepted_eula", false);
	}

	private static void setAcceptedEula(final Context context) {
		new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... voids) {
				SharedPreferences sp = PreferenceManager
						.getDefaultSharedPreferences(context);
				sp.edit().putBoolean("accepted_eula", true).commit();
				return null;
			}
		}.execute();
	}

	/**
	 * Show End User License Agreement.
	 * 
	 * @param accepted
	 *            True IFF user has accepted license already, which means it can
	 *            be dismissed. If the user hasn't accepted, then the EULA must
	 *            be accepted or the program exits.
	 * @param activity
	 *            Activity started from.
	 */
	public static void showEula(final boolean accepted, final Activity activity) {

		LayoutInflater inflater = LayoutInflater.from(activity);

		View alertDialogView = inflater.inflate(R.layout.eula, null);

		WebView webview = (WebView) alertDialogView.findViewById(R.id.webview);

		AlertDialog.Builder eula = new AlertDialog.Builder(activity)
				.setTitle(R.string.eula_title)
				.setIcon(android.R.drawable.ic_dialog_info)
				// .setMessage(Html.fromHtml(activity.getString(R.string.eula_text)))
				.setCancelable(accepted);

		eula.setView(alertDialogView);

		webview.loadUrl("file:///android_asset/eula.html");
		
		if (accepted) {
			// If they've accepted the EULA allow, show an OK to dismiss.
			eula.setPositiveButton(android.R.string.ok,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							Toast toast = Toast
									.makeText(
											activity,
											"This is a beta release for testing purposes only.",
											Toast.LENGTH_SHORT);
							toast.setGravity(Gravity.CENTER, 0, 0);
							toast.show();
						}
					});
		} else {
			// If they haven't accepted the EULA allow, show accept/decline
			// buttons and exit on
			// decline.
			eula.setPositiveButton(R.string.accept,
					new android.content.DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							setAcceptedEula(activity);
							dialog.dismiss();
						}
					}).setNegativeButton(R.string.decline,
					new android.content.DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
							activity.finish();
						}
					});
		}
		eula.show();
	}
}
