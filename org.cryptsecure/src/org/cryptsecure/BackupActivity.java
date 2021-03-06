/*
 * Copyright (c) 2015, Christian Motika. Dedicated to Sara.
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 * all contributors, this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, an acknowledgment to all contributors, this list of conditions
 * and the following disclaimer in the documentation and/or other materials
 * provided with the distribution.
 * 
 * 3. Neither the name Delphino CryptSecure nor the names of its contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 *
 * 4. Free or commercial forks of CryptSecure are permitted as long as
 *    both (a) and (b) are and stay fulfilled: 
 *    (a) This license is enclosed.
 *    (b) The protocol to communicate between CryptSecure servers
 *        and CryptSecure clients *MUST* must be fully conform with 
 *        the documentation and (possibly updated) reference 
 *        implementation from cryptsecure.org. This is to ensure 
 *        interconnectivity between all clients and servers. 
 * 
 * THIS SOFTWARE IS PROVIDED BY THE CONTRIBUTORS �AS IS� AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, 
 * OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT
 * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, 
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
 */
package org.cryptsecure;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * The BackupActivity class is responsible for displaying a backup dialog to let
 * the user choose which messages (mids) to backup. It also performs copying the
 * backup to the Clipboard of the device.
 * 
 * @author Christian Motika
 * @since 1.2
 * @date 08/23/2015
 * 
 */
public class BackupActivity extends Activity {

	/** The conversation item. */
	public static ConversationItem conversationItem = null;

	/** The host uid. */
	public static int hostUid;

	/** The cached first mid. This is the first possible mid to backup. */
	private int firstMid = 0;

	/** The cached last mid. This is the last possible mid to backup */
	private int lastMid = 0;

	/** The complete conversation list. */
	private List<ConversationItem> conversationList = new ArrayList<ConversationItem>();

	/** The activity. */
	Activity activity = null;

	/** The context. */
	Context context = null;

	/** The backup dialog. */
	AlertDialog backupDialog = null;

	/** The cancel flag. Per default it is true unless an OK-button was pressed. */
	boolean cancel = true;

	/**
	 * The handled flag. It tells if a button was pressed or if the dialog was
	 * closed, e.g., using the BACK key.
	 */
	boolean handled = false;

	// ------------------------------------------------------------------------

	@SuppressLint("InflateParams")
	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		cancel = true;
		handled = false;
		activity = this;
		context = this;

		AlertDialog.Builder builder = new AlertDialog.Builder(activity);

		// ATTENTION: Necessary to see the calling activity in the background!
		// android:theme="@style/Theme.Transparent"

		// The title is set later
		String activityTitle = "";

		LayoutInflater inflaterInfo = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		LinearLayout dialogLayout = (LinearLayout) inflaterInfo.inflate(
				R.layout.activity_backup, null);

		LinearLayout outerLayout = (LinearLayout) dialogLayout
				.findViewById(R.id.backupmain);
		LinearLayout buttonLayout = (LinearLayout) dialogLayout
				.findViewById(R.id.backupbuttons);

		// -------------

		// Set icon and title of the dialog
		builder.setIcon(R.drawable.btnbackup);
		activityTitle = "Backup Messages";

		final EditText fromText = (EditText) dialogLayout
				.findViewById(R.id.fromid);
		final EditText toText = (EditText) dialogLayout.findViewById(R.id.toid);
		final TextView fromTextInfo = (TextView) dialogLayout
				.findViewById(R.id.fromidinfo);
		final TextView toTextInfo = (TextView) dialogLayout
				.findViewById(R.id.toidinfo);

		
		final CheckBox first = (CheckBox) dialogLayout
				.findViewById(R.id.fromfirst);
		final CheckBox last = (CheckBox) dialogLayout.findViewById(R.id.tolast);
		final CheckBox details = (CheckBox) dialogLayout
				.findViewById(R.id.details);
		final CheckBox removeimages = (CheckBox) dialogLayout
				.findViewById(R.id.removeimages);
		removeimages.setChecked(true);

		DB.loadConversation(context, hostUid, conversationList, -1);

		if (conversationList.size() > 0) {
			lastMid = conversationList.get(conversationList.size() - 1).localid;
			firstMid = conversationList.get(0).localid;
		}
		fromText.setText(firstMid + "");
		toText.setText(lastMid + "");

		first.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				fromText.setText(firstMid + "");
				if (isChecked) {
					fromText.setVisibility(View.GONE);
					fromTextInfo.setVisibility(View.GONE);
				} else {
					fromText.setVisibility(View.VISIBLE);
					fromTextInfo.setVisibility(View.VISIBLE);
				}
			}
		});
		last.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				toText.setText(lastMid + "");
				if (isChecked) {
					toText.setVisibility(View.GONE);
					toTextInfo.setVisibility(View.GONE);
				} else {
					toTextInfo.setVisibility(View.VISIBLE);
					toText.setVisibility(View.VISIBLE);
				}
			}
		});
		first.setChecked(true);
		last.setChecked(true);

		Button buttonBackup = (Button) dialogLayout
				.findViewById(R.id.buttonbackup);
		Button buttonCancel = (Button) dialogLayout
				.findViewById(R.id.buttoncancel);

		buttonBackup.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {

				int fromLid = Utility.parseInt(fromText.getText().toString(),
						-1);
				int toLid = Utility.parseInt(toText.getText().toString(), -1);


				doBackup(context, fromLid, toLid, conversationList,
						details.isChecked(), removeimages.isChecked());

				activity.finish();
			}
		});
		buttonCancel.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				activity.finish();
			}
		});

		// -------------

		builder.setTitle(activityTitle);
		builder.setView(dialogLayout);

		builder.setOnCancelListener(new OnCancelListener() {
			public void onCancel(DialogInterface dialog) {
				// dialog.dismiss();
				activity.finish();
			}
		});

		backupDialog = builder.show();

		// Grab the window of the dialog, and change the width
		WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
		Window window = backupDialog.getWindow();
		lp.copyFrom(window.getAttributes());
		// This makes the dialog take up the full width
		lp.width = WindowManager.LayoutParams.MATCH_PARENT;
		// lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
		window.setAttributes(lp);

		Utility.setBackground(context, outerLayout, R.drawable.dolphins3light);
		Utility.setBackground(context, buttonLayout, R.drawable.dolphins4light);

	}

	// ------------------------------------------------------------------------

	/**
	 * Perform the backup.
	 * 
	 * @param context
	 *            the context
	 * @param fromLocal
	 *            the from local
	 * @param toLocal
	 *            the to local
	 * @param conversationList
	 *            the conversation list
	 * @param details
	 *            the details
	 * @param removeImages
	 *            the remove images
	 * @return the int
	 */
	public static int doBackup(Context context, int fromLocal, int toLocal,
			List<ConversationItem> conversationList, boolean details,
			boolean removeImages) {
		int numMessages = 0;
		// DO BACKUP
		StringBuilder backupText = new StringBuilder();
		boolean active = false;

		for (ConversationItem item : conversationList) {

			if (!active && item.localid >= fromLocal) {
				active = true;
			}

			String itemText = item.text;
			if (itemText.length() > 30) {
				itemText = itemText.substring(0, 29);
			}

			if (active) {
				numMessages++;
				if (backupText.length() != 0) {
					backupText.append(System.getProperty("line.separator"));
					backupText.append(System.getProperty("line.separator"));
				}

				backupText.append(Main.UID2Name(context, item.from, true));
				if (details) {
					backupText.append(System.getProperty("line.separator"));
					backupText.append("Created: "
							+ DB.getDateString(item.created, false));
					backupText.append(System.getProperty("line.separator"));
					backupText.append("Sent: "
							+ DB.getDateString(item.sent, false));
					backupText.append(System.getProperty("line.separator"));
					backupText.append("Received: "
							+ DB.getDateString(item.received, false));
					backupText.append(System.getProperty("line.separator"));
					backupText.append("Read: "
							+ DB.getDateString(item.read, false));
					backupText.append(System.getProperty("line.separator"));
					backupText.append("Revoked: "
							+ DB.getDateString(item.revoked, false));
					backupText.append(System.getProperty("line.separator"));
					if (item.transport == DB.TRANSPORT_INTERNET) {
						backupText.append("Transport: Internet");
					} else {
						backupText.append("Transport: SMS");
					}
					backupText.append(System.getProperty("line.separator"));
					if (item.encrypted) {
						backupText.append("Encrypted: Yes");
					} else {
						backupText.append("Encrypted: No");
					}
					backupText.append(System.getProperty("line.separator"));
				} else {
					backupText.append(" @ ");
					backupText.append(DB.getDateString(item.sent, false));
				}
				backupText.append(System.getProperty("line.separator"));

				if (!removeImages) {
					backupText.append(item.text);
				} else {
					backupText.append(Conversation
							.possiblyRemoveImageAttachments(context, item.text,
									true, "[ image ]", -1));
				}
			}

			if (active && item.localid >= toLocal) {
				active = false;
				break;
			}

		}

		Utility.copyToClipboard(context, backupText.toString());
		if (backupText.toString().length() > 0) {
			Utility.showToastAsync(context, +numMessages
					+ " messages backed up to Clipboard.");
		} else {
			Utility.showToastAsync(context, "Nothing to backup.");
		}

		return numMessages;
	}

	// ------------------------------------------------------------------------

}