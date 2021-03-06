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
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;

import org.cryptsecure.ImageContextMenu.ImageContextMenuProvider;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore.Images;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.View.OnLongClickListener;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * The Conversation activity is the second most important class. It holds the
 * currently displayed conversation. Also there should always only be one
 * instance of this activity. Hence some data is static for faster access.
 * 
 * @author Christian Motika
 * @date 08/23/2015
 * @since 1.2
 * 
 */
@SuppressLint("InflateParams")
public class Conversation extends Activity {

	/**
	 * The fixed inset for conversation items NOT from me. This may differ
	 * depending on if the user has an avatar or not. With an avatar the insert
	 * is larger (200) to have room for the avatar.
	 */
	private int fixed_inset = 160;

	/** The fixed inset for conversation items. */
	private int fixed_inset_me = 180;

	/** The fast scroll view. */
	FastScrollView fastScrollView;

	/** The conversation root view. */
	private View conversationRootView;

	/** The titleconversation. */
	private LinearLayout titleconversation;

	/** The conversation list. */
	private List<ConversationItem> conversationList = new ArrayList<ConversationItem>();

	/** The conversation list diff. */
	private List<ConversationItem> conversationListDiff = new ArrayList<ConversationItem>(); // only
																								// new
	/** The sendspinner. */
	Spinner sendspinner = null;

	// /** The failed color as background for failed SMS. */
	// private static int FAILEDCOLOR = Color.parseColor("#6DB0FD");

	/** The mapping. */
	@SuppressLint("UseSparseArrays")
	private HashMap<Integer, Mapping> mapping = new HashMap<Integer, Mapping>();

	/** The host uid. */
	private static int hostUid = -1;

	/** The has scrolled. */
	private static boolean hasScrolled = false;

	/** The scroll item. */
	private static int scrollItem = -1;

	/** The max scroll message items. */
	private static int maxScrollMessageItems = -1;

	/**
	 * The scrolled down. If scrolled down, maintain scrolled down lock even
	 * when orientation changes, keyboard comes up or new message arrives.
	 */
	public static boolean scrolledDown = false;

	/** The scrolled up. Keep being scrolled up if this was set to true earlier. */
	private static boolean scrolledUp = false;

	/** The conversation size. */
	private int conversationSize = -1;

	/** The central send button. */
	private ImagePressButton sendbutton;

	/** The smiley button. */
	private ImagePressButton smileybutton;

	/** The attachment button. */
	private ImagePressButton attachmentbutton;

	/** The addition button. */
	private ImagePressButton additionbutton;

	/** The message text. */
	public ImageSmileyEditText messageText;

	public TextView titletext;

	/** The inflater. */
	private LayoutInflater inflater;

	/** The additions visible flag tells if, e.g., smilie button is visible. */
	private static boolean additionsVisible = false;

	/**
	 * The last height is necessary to detect if the height changes. If it NOT
	 * changes we can skip a lot of processing which makes the UI feel much
	 * faster.
	 */
	int lastHeight = 0;

	/** The last messag text line count. */
	int lastMessagTextLineCount = 1;

	/** The current screen width. */
	public static int currentScreenWidth = 300;

	/** The screen width changed. */
	boolean screenWidthChanged = false;

	/**
	 * The color of the fast scroll background when scrolling and not scroll
	 * locked down.
	 */
	public static final int FASTSCROLLBACKSCROLLINGBACKGROUND = Color
			.parseColor("#44000000");

	/**
	 * The color of the fast scroll background when scrolling and not scroll
	 * locked down in NON-darkmode.
	 */
	public static final int FASTSCROLLBACKSCROLLINGBACKGROUNDW = Color
			.parseColor("#11000000");

	/**
	 * The color of the fast scroll background when scroll locked down.
	 */
	public static final int FASTSCROLLBACKLOCKEDBACKGROUND = Color
			.parseColor("#00555555");

	/** The image context menu provider for the main menu. */
	private ImageContextMenuProvider imageContextMenuProvider = null;

	/** The image context menu provider for the send menu. */
	private ImageContextMenuProvider imageSendMenuProvider = null;

	/** The image context menu provider for the image menu. */
	private ImageContextMenuProvider imageImageMenuProvider = null;

	/** The image context menu provider for the message menu. */
	private ImageContextMenuProvider imageMessageMenuProvider = null;

	/**
	 * The skips ONE resume. Necessary for the send context menu call because we
	 * do not want to typical resume there.
	 */
	public static boolean skipResume = false;

	/**
	 * The list of all text views in order to update their with when screen
	 * orientation changes.
	 */
	private List<ImageSmileyEditText> textViews = new ArrayList<ImageSmileyEditText>();

	/** Tells if sms mode is on. */
	private boolean isSMSModeOn = false;

	/** The is encryption on. */
	private boolean isEncryptionOn = false;

	/** The list of all images. */
	private LinkedHashSet<Bitmap> images = new LinkedHashSet<Bitmap>();

	/** The bitmap_speech. */
	static Bitmap bitmap_speechmaster = null;

	/** The bitmap_speech. */
	static Bitmap bitmap_speechmastersmall = null;

	/** The bitmap_speech. */
	static Bitmap bitmap_speech = null;

	/** The bitmap_speechalert. */
	static Bitmap bitmap_speechalert = null;

	/** The Constant GROUPPREFIX following the secret. */
	static final String GROUPINVITATION_PREFIX = "[ invitation ";

	// ------------------------------------------------------------------------

	/**
	 * The internal class Mapping for mapping message ids to elements in the
	 * conversation in order to update, e.g., e state icons.
	 */
	private class Mapping {

		/** The parent view speech containint everything. */
		View parent = null;

		/** The multipartid. */
		String multipartid = "";

		/** The progress. */
		ProgressBar progress;

		/** The speech. */
		ImageView speech;

		/** The oneline. */
		LinearLayout oneline;

		/** The sent. */
		ImageView sent;

		/** The received. */
		ImageView received;

		/** The read. */
		ImageView read;

		/** The text. */
		EditText text;
	}

	// ------------------------------------------------------------------------

	/**
	 * The listener interface for receiving onSend events. The class that is
	 * interested in processing a onSend event implements this interface, and
	 * the object created with that class is registered with a component using
	 * the component's <code>addonSendListener<code> method. When
	 * the onSend event occurs, that object's appropriate
	 * method is invoked.
	 * 
	 * @see onSendEvent
	 */
	interface OnSendListener {
		void onSend(boolean success, boolean encrypted, int transport);
	}

	// ------------------------------------------------------------------------

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Setup.possiblyDisableScreenshot(this);
		super.onCreate(savedInstanceState);
		Conversation.visible = true;
		instance = this;
		final Activity context = this;

		inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		// Apply custom title bar (with holo :-)
		// See comment in Main.java
		LinearLayout main = Utility.setContentViewWithCustomTitle(this,
				R.layout.activity_conversation, R.layout.title_conversation);
		main.setGravity(Gravity.BOTTOM);
		titleconversation = (LinearLayout) findViewById(R.id.titleconversation);

		ImageView groupimage = (ImageView) findViewById(R.id.conversationgroupimage);
		if (!Setup.isGroup(context, hostUid)) {
			groupimage.setVisibility(View.GONE);
		}

		// Add title bar buttons
		ImagePressButton btnback = (ImagePressButton) findViewById(R.id.btnback);
		btnback.initializePressImageResource(R.drawable.btnback);
		LinearLayout btnbackparent = (LinearLayout) findViewById(R.id.btnbackparent);
		btnback.setAdditionalPressWhiteView(btnbackparent);
		btnback.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				goBack(context);
			}
		});

		titletext = (TextView) findViewById(R.id.titletext);
		final LinearLayout titletextparent = (LinearLayout) findViewById(R.id.titletextparent);
		titletextparent.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// SHow details
				Main.promptUserClick(context, hostUid);
			}
		});
		titletextparent.setOnTouchListener(new View.OnTouchListener() {
			@SuppressLint("ClickableViewAccessibility")
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					titletextparent
							.setBackgroundColor(ImagePressButton.WHITEPRESS);
					titletextparent.postDelayed(new Runnable() {
						public void run() {
							titletextparent
									.setBackgroundColor(ImagePressButton.TRANSPARENT);
						}
					}, 300);
				}
				return false;
			}
		});

		ImagePressButton btnkey = (ImagePressButton) findViewById(R.id.btnkey);
		btnkey.initializePressImageResource(R.drawable.btnkey);
		LinearLayout btnkeyparent = (LinearLayout) findViewById(R.id.btnkeyparent);
		btnkey.setAdditionalPressWhiteView(btnkeyparent);
		btnkey.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				possiblePromptNewSession(context);
			}
		});
		ImagePressButton btnsearch = (ImagePressButton) findViewById(R.id.btnsearch);
		btnsearch.initializePressImageResource(R.drawable.btnsearch);
		LinearLayout btnsearchparent = (LinearLayout) findViewById(R.id.btnsearchparent);
		btnsearch.setAdditionalPressWhiteView(btnsearchparent);
		btnsearch.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				promptSearch(context);
			}
		});
		ImagePressButton btnmenu = (ImagePressButton) findViewById(R.id.btnmenu);
		btnmenu.initializePressImageResource(R.drawable.btnmenu);
		LinearLayout btnmenuparent = (LinearLayout) findViewById(R.id.btnmenuparent);
		btnmenu.setAdditionalPressWhiteView(btnmenuparent);
		btnmenu.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				openOptionsMenu();
			}
		});

		// conversationinnerview = ((LinearLayout)
		// findViewById(R.id.conversationinnerview));
		fastScrollView = (FastScrollView) findViewById(R.id.fastscrollview);

		fastScrollView
				.setOnNoHangListener(new FastScrollView.OnNoHangListener() {
					public boolean noHangNeeded() {
						return isTypingFast();
					}
				});

		messageText = ((ImageSmileyEditText) findViewById(R.id.messageText));
		messageText.setInputTextField(true);
		// fastscrollbar = (FastScrollBar) findViewById(R.id.fastscrollbar);

		messageText
				.setOnCutCopyPasteListener(new ImageSmileyEditText.OnCutCopyPasteListener() {
					public void onPaste() {
						// If an image or smiley is pasted then do a new layout!
						reprocessPossibleImagesInText(messageText);
					}

					public void onCut() {
					}

					public void onCopy() {
					}
				});

		TextWatcher textWatcher = new TextWatcher() {
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// DO FAST COMPUTATION HERE
				// It does appear that System.currentTimeMillis() is twice as
				// fast as System.nanoTime(). However 29ns is going to be much
				// shorter than anything else you'd be measuring anyhow.
				lastKeyStroke = System.currentTimeMillis();

				int newLineCount = messageText.getLineCount();
				if (lastMessagTextLineCount != newLineCount) {
					lastMessagTextLineCount = newLineCount;
					// Allow reLayout
					FastScrollView.allowOneLayoutOverride = 3;
				}

				if ((isSMSModeOn || hostUid < 0) && keyboardVisible) {
					// In SMS mode and with keyboard visible, show the remaining
					// text and the number of multipart SMS in the title
					int smsSize = Setup.SMS_DEFAULT_SIZE;
					int textLen = messageText.length();
					if (isEncryptionOn) {
						smsSize = Setup.SMS_DEFAULT_SIZE_ENCRYPTED;
					} else if (textLen > smsSize) {
						smsSize = Setup.SMS_DEFAULT_SIZE_MULTIPART;
					}
					int numSMS = (int) Math.ceil((double) textLen
							/ (double) smsSize);
					int remainingUntilNextSMS = (numSMS * smsSize) - textLen;
					setTitle(remainingUntilNextSMS + " / " + numSMS);
				}

			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			public void afterTextChanged(Editable s) {
			}
		};
		messageText.addTextChangedListener(textWatcher);

		sendbutton = ((ImagePressButton) findViewById(R.id.sendbutton));
		LinearLayout sendbuttonparent = (LinearLayout) findViewById(R.id.sendbuttonparent);
		sendbutton.setAdditionalPressWhiteView(sendbuttonparent);

		final LinearLayout additions = (LinearLayout) findViewById(R.id.additions);
		smileybutton = ((ImagePressButton) findViewById(R.id.smileybutton));
		LinearLayout smiliebuttonparent = (LinearLayout) findViewById(R.id.smileybuttonparent);
		smileybutton.setAdditionalPressWhiteView(smiliebuttonparent);

		attachmentbutton = ((ImagePressButton) findViewById(R.id.attachmentbutton));
		LinearLayout attachmentbuttonparent = (LinearLayout) findViewById(R.id.attachmentbuttonparent);
		attachmentbutton.setAdditionalPressWhiteView(attachmentbuttonparent);
		if (Setup.isDarkmode(context)) {
			smileybutton.initializePressImageResource(R.drawable.smileybtn, 3, 300,
					false);
			attachmentbutton.initializePressImageResource(R.drawable.attachmentbtn,
					3, 300, false);
		} else {
			smileybutton.setImageResource(R.drawable.smileybtnw);
			smileybutton.initializePressImageResource(R.drawable.smileybtnw, 3, 300,
					false);
			attachmentbutton.setImageResource(R.drawable.attachmentbtnw);
			attachmentbutton.initializePressImageResource(R.drawable.attachmentbtnw,
					3, 300, false);
		}

		
		additionbutton = ((ImagePressButton) findViewById(R.id.additionbutton));
		if (!Setup.isDarkmode(context)) {
			additionbutton.setImageResource(R.drawable.additionbtnw);
			additionbutton.setBackgroundResource(R.drawable.additionbtnbackw);
		}
		additionbutton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// toggle
				additionsVisible = !additionsVisible;
				if (additionsVisible) {
					additions.setVisibility(View.VISIBLE);
				} else {
					additions.setVisibility(View.GONE);
				}
			}
		});
		if (additionsVisible) {
			additions.setVisibility(View.VISIBLE);
		} else {
			additions.setVisibility(View.GONE);
		}
		// If smileys are turned of then do not display the additions button
		if (!(Utility.loadBooleanSetting(context, Setup.OPTION_SMILEYS,
				Setup.DEFAULT_SMILEYS))) {
			smileybutton.setVisibility(View.GONE);
			LinearLayout.LayoutParams lpattachmentbuttonparent = new LinearLayout.LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
			lpattachmentbuttonparent.setMargins(2, 0, 5, 0);
			attachmentbuttonparent.setLayoutParams(lpattachmentbuttonparent);
		}
		smileybutton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				final boolean wasKeyboardVisible = isKeyboardVisible(conversationRootView);

				SmileyPrompt smileyPrompt = new SmileyPrompt();
				smileyPrompt
						.setOnSmileySelectedListener(new SmileyPrompt.OnSmileySelectedListener() {
							public void onSelect(String textualSmiley) {
								if (textualSmiley != null) {
									Utility.smartPaste(messageText,
											textualSmiley, " ", " ", true,
											false, true);
								}
								if (wasKeyboardVisible) {
									// Log.d("communicator",
									// "@@@@ smileybutton->scrollDownNow()");
									scrollDownNow(context, true);
								}
							}
						});
				smileyPrompt.promptSmileys(context);
			}
		});
		attachmentbutton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				promptImageInsert(context, hostUid);
			}
		});

		if (hostUid == 0) {
			// system, disable
			sendbutton.setEnabled(false);
		}

		sendbutton.setLongClickable(true);
		sendbutton.setOnLongClickListener(new OnLongClickListener() {
			public boolean onLongClick(View v) {
				// sendspinner.performClick();
				ImageContextMenu.show(instance, createSendMenu(instance));
				return false;
			}
		});

		updateSendButtonImage(context);

		sendbutton.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				// If registered user and SMS mode active, or if SMS-user only
				if (hostUid < 0 || isSMSModeAvailableAndOn(context)) {
					sendMessageOrPrompt(context, DB.TRANSPORT_SMS, true);
				} else {
					sendMessageOrPrompt(context, DB.TRANSPORT_INTERNET, true);
				}
			}
		});

		messageText.setOnKeyListener(new OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				// Log.d("communicator",
				// "@@@@ setOnKeyListener() : keyCode = " + keyCode);
				boolean chatmodeOn = Utility.loadBooleanSetting(context,
						Setup.OPTION_CHATMODE, Setup.DEFAULT_CHATMODE);
				if (keyCode == 66 && chatmodeOn) {
					if (chatmodeOn) {
						sendbutton.performClick();
					}
					return true;
				}
				return false;
			}

		});

		// Possibly load draft
		String draft = Utility.loadStringSetting(this, "cachedraft" + hostUid,
				"");
		if (draft != null && draft.length() > 0) {
			messageText.setText(draft);
			messageText.setSelection(0, draft.length());
			Utility.saveStringSetting(this, "cachedraft" + hostUid, "");
		}

		// Here we initialize / rebuild the complete (visible) conversation
		// list.
		rebuildConversationlist(context);

		// Setting backgrounds
		Utility.setBackground(this, main, Setup.dolphins2(context));
		Utility.setBackground(this, titleconversation, R.drawable.dolphins3blue);
		View inputLayout = ((View) findViewById(R.id.inputlayout));
		conversationRootView = (View) findViewById(R.id.conversationRootView);
		Utility.setBackground(this, conversationRootView, Setup.dolphins2(context));
		if (!Setup.isDarkmode(context)) {
			Utility.setBackground(this, inputLayout, Setup.dolphins3(context));
		} else {
			Utility.setBackground(this, inputLayout, Setup.dolphins1(context));
		}

		// DO NOT SCROLL HERE BECAUSE onResume() WILL DO THIS.
		// onResume() is ALWAYS called if the user starts OR returns to the APP!
		// DO NOT -> scrollOnCreateOrResume(context);

		// Snap to 100% if >90%
		fastScrollView.setSnapDown(85);
		fastScrollView.setSnapUp(10);

		fastScrollView
				.setOnScrollListener(new FastScrollView.OnScrollListener() {

					public void onScrollRested(FastScrollView fastScrollView,
							int x, int y, int oldx, int oldy, int percent,
							int item) {

						if (percent >= 99) {
							scrolledDown = true;
							scrolledUp = false;
								fastScrollView
								.setScrollBackground(FASTSCROLLBACKLOCKEDBACKGROUND);
						} else if (percent <= 1) {
							showTitlebarAsync(context);
							scrolledUp = true;
							scrolledDown = false;
								fastScrollView
								.setScrollBackground(FASTSCROLLBACKLOCKEDBACKGROUND);
						} else {
							scrolledUp = false;
							scrolledDown = false;
							if (Setup.isDarkmode(context)) {
								fastScrollView
								.setScrollBackground(FASTSCROLLBACKSCROLLINGBACKGROUND);
							} else {
								fastScrollView
								.setScrollBackground(FASTSCROLLBACKSCROLLINGBACKGROUNDW);
							}
						}
						scrollItem = item;

						// Log.d("communicator",
						// "@@@@ onScrollRested() :  y = " + y
						// + ", percent=" + percent + ", item="
						// + item + ", getMaxPosition="
						// + fastScrollView.getMaxPosition());

						// Log.d("communicator",
						// "@@@@ onScrollRested() :  scrollItem = "
						// + scrollItem + ", percent=" + percent
						// + ", scrolledUp=" + scrolledUp
						// + ", scrolledDown=" + scrolledDown);

						// Switch back to normal title
						updateConversationTitleAsync(context);
					}

					public void onScrollChanged(FastScrollView fastScrollView,
							int x, int y, int oldx, int oldy, int percent,
							int item) {
						int realItem = conversationSize
								- conversationList.size() + item;
						updateConversationTitleAsync(context, realItem + " / "
								+ conversationSize);
					}

					public void onOversroll(boolean up) {
						// Switch back to normal title
						updateConversationTitleAsync(context);
					}

					public void onSnapScroll(int percent, boolean snappedDown,
							boolean snappedUp) {
						// DO NOTHING HERE
					}

				});
		if (Setup.isDarkmode(context)) {
			fastScrollView
			.setScrollBackground(FASTSCROLLBACKSCROLLINGBACKGROUND);
		} else {
			fastScrollView
			.setScrollBackground(FASTSCROLLBACKSCROLLINGBACKGROUNDW);
		}


		// The following piece of code is necessary for dealing with width
		// changes.
		fastScrollView
				.setOnSizeChangeListener(new FastScrollView.OnSizeChangeListener() {
					public void onSizeChange(int w, int h, int oldw, int oldh) {
						if (currentScreenWidth != w) {
							screenWidthChanged = true;
						}
						currentScreenWidth = w;
						// Log.d("communicator", "######## SCROLL CHANGED X");
						// if the keyboard pops up and scrolledDown == true,
						// then scroll down manually!
						if (keyboardVisible && scrolledDown) {
							scrollDownAfterTypingFast(false);
						}
					}
				});

		// The following code is necessary to FORCE further scrolling down if
		// the virtual keyboard
		// is brought up. Otherwise the top scrolled position is remaining but
		// this is uncomfortable!
		conversationRootView.getViewTreeObserver().addOnGlobalLayoutListener(
				new ViewTreeObserver.OnGlobalLayoutListener() {
					public void onGlobalLayout() {

						Rect r = new Rect();
						conversationRootView.getWindowVisibleDisplayFrame(r);
						// get screen height
						int screenHeight = conversationRootView.getRootView()
								.getHeight();
						// calculate the height difference
						int heightDifference = screenHeight - r.bottom - r.top;
						if (lastHeight == heightDifference) {
							return;
						}
						lastHeight = heightDifference;

						Log.d("communicator",
								"@@@@ onGlobalLayout() scoll down request "
										+ heightDifference + ", scrolledDown="
										+ scrolledDown);

						// THE FOLLOWING IS A FEATURE TO HIDE THE TITLE IF THE
						// MESSAGE TEXT GETS LONGER
						//
						// IT IS DEACTIVATED RIGHT NOW BECAUSE IT FEELS
						// UNCOMFORTABLE IF THE BACK BUTTON
						// AND TITLE BAR IS MISSING ESP. FOR SHORT MESSAGES. MAY
						// BE REACTIVATE THIS FEATURE
						// AT A LATER TIME BUT MAKE SURE TO BE PERFORMANT!
						//
						// if (messageText.length() == 0) {
						// messageTextEmptyHeight = messageText.getHeight();
						// }
						// if (isKeyboardVisible(conversationRootView)) {
						// if (!scrolledUp
						// && messageText.getHeight() > messageTextEmptyHeight)
						// {
						// // if scrolled up completely, we want to show it
						// // even with keyboard visible!
						// // also, if this is an empty or one-line message
						// // we want to see the titlebar! (e.g., directly
						// // after sending)
						// titleconversation.setVisibility(View.GONE);
						// }
						// } else {
						// titleconversation.setVisibility(View.VISIBLE);
						// }

						if (screenWidthChanged) {
							screenWidthChanged = false;
							updateTextViewsWidth(currentScreenWidth);
						}

						// Allow the next layout pass!
						FastScrollView.allowOneLayoutOverride = 3;

						// The following will also trigger a relayout, if
						// necessary
						fastScrollView.potentiallyRefreshState(true);

						boolean showKeyboard = false;
						if (Utility
								.loadBooleanSetting(context,
										Setup.OPTION_QUICKTYPE,
										Setup.DEFAULT_QUICKTYPE)
								&& Utility.isOrientationLandscape(context)) {
							showKeyboard = true;
						}

						// If the keyboard pops up and scrolledDown == true,
						// then scroll down manually!
						// do not do this delayed because orientation changed!
						if (scrolledDown) {
							scrollDownNow(context, showKeyboard);
						}
					}
				});
	}

	// ------------------------------------------------------------------------

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onStart()
	 */
	@Override
	protected void onStart() {
		super.onStart();
	}

	// ------------------------------------------------------------------------

	/**
	 * Initialize bitmaps if not initialized already.
	 */
	public static void initializeBitmaps(Context context) {
		if (bitmap_speechmaster == null) {
			bitmap_speechmaster = BitmapFactory.decodeResource(
					context.getResources(), R.drawable.speechmaster);
			bitmap_speechmastersmall = BitmapFactory.decodeResource(
					context.getResources(), R.drawable.speechmastersmall);
			bitmap_speech = BitmapFactory.decodeResource(
					context.getResources(), R.drawable.speech);
			bitmap_speechalert = BitmapFactory.decodeResource(
					context.getResources(), R.drawable.speechalert);
		}
	}

	// ------------------------------------------------------------------------

	/**
	 * Gets the host uid of the current conversation.
	 * 
	 * @return the host uid
	 */
	public static int getHostUid() {
		return hostUid;
	}

	// ------------------------------------------------------------------------

	/**
	 * Update all conversation text views widths.
	 * 
	 * @param width
	 *            the width
	 */
	private void updateTextViewWidth(ImageSmileyEditText textView, int width) {
		android.view.ViewGroup.LayoutParams lp = textView.getLayoutParams();
		if (textView.isMe()) {
			lp.width = width - fixed_inset_me;
		} else {
			lp.width = width - fixed_inset;
		}
		textView.updateMaxWidth(width);
	}

	// ------------------------------------------------------------------------

	/**
	 * Update all conversation text views widths.
	 * 
	 * @param width
	 *            the width
	 */
	private void updateTextViewsWidth(int width) {
		// We MUST clear the list of images here and coall GC!
		images.clear();
		System.gc();
		// The list of images will be filled with updateTextViewWidth again!
		for (ImageSmileyEditText textView : textViews) {
			updateTextViewWidth(textView, width);
		}
	}

	// ------------------------------------------------------------------------

	/**
	 * Invite other user to enable his SMS mode.
	 * 
	 * @param context
	 *            the context
	 */
	private void inviteOtherUserToSMSMode(final Context context, int serverId) {
		// Possibly read other ones telephone number
		// at this point
		Communicator.updatePhonesFromServer(context, Main.loadUIDList(context),
				true, serverId);

		final String titleMessage = "Enable SMS";
		String partner = Main.UID2Name(context, hostUid, false);
		final String textMessage = "Delphino CryptSecure also allows to send/receive secure encrypted SMS.\n\nTo be able to use this option both communication partners have to turn this feature on.\n\nCurrently '"
				+ partner
				+ "' seems not to have turned on this feature. If you know '"
				+ partner
				+ "' has turned it on, try to refresh your userlist manually.\n\nAlternative: You can long press on the name '"
				+ partner
				+ "' in the userlist and add her/his phone number manually. This will just enable you to send him SMS.";
		new MessageAlertDialog(context, titleMessage, textMessage, " Ok ",
				null, null, new MessageAlertDialog.OnSelectionListener() {
					public void selected(int button, boolean cancel) {
					}
				}).show();
	}

	// ------------------------------------------------------------------------

	/**
	 * Invite current user to sms mode.
	 * 
	 * @param context
	 *            the context
	 */
	private void inviteUserToSMSMode(final Context context) {
		try {
			final String titleMessage = "Enable SMS";
			final String textMessage = "Delphino CryptSecure also allows to send/receive secure encrypted SMS.\n\nFor this,"
					+ " your phone number and your userlist must be stored at the server. Furthermore, to be able to use this"
					+ " option both communication partners have to turn this feature on.\n\nDo you want to enable the secure"
					+ " SMS possibility?";
			new MessageAlertDialog(context, titleMessage, textMessage, " Yes ",
					" Account ", " Cancel ",
					new MessageAlertDialog.OnSelectionListener() {
						public void selected(int button, boolean cancel) {
							if (!cancel) {
								int serverId = Setup.getServerId(context,
										hostUid);
								if (button == 0) {
									// Turn on if possible
									String phone = Utility
											.getPhoneNumber(context);
									if (phone != null && phone.length() > 0) {
										Setup.updateSMSOption(context, true,
												serverId);
										Setup.backup(context, true, false,
												serverId);
										// Possibly read other ones telephone
										// number at this poiint
										Communicator.updatePhonesFromServer(
												context,
												Main.loadUIDList(context),
												true, serverId);
									} else {
										Utility.showToastAsync(
												context,
												"Cannot automatically read required phone number. Please enable SMS option"
														+ " manually in account settings after login/validate!");
										// Go to account settings
										Main.startAccount(context, serverId);
									}
								} else if (button == 1) {
									// Go to account settings
									Main.startAccount(context, serverId);
								}
							}
						}
					}).show();
		} catch (Exception e) {
			// ignore
		}
	}

	// ------------------------------------------------------------------------
	// ------------------------------------------------------------------------

	/**
	 * Send message dispatch method is only called from sendMessageOrPrompt().
	 * It should not be called directly.
	 * 
	 * @param context
	 *            the context
	 * @param text
	 *            the text
	 * @param encrypted
	 *            the encrypted
	 * @param transport
	 *            the transport
	 */
	private void sendMessage(Context context, int transport, boolean encrypted,
			String text) {
		if (transport == DB.TRANSPORT_INTERNET) {
			if (encrypted) {
				sendSecureMsg(context, text);
			} else {
				sendUnsecureMsg(context, text);
			}
		} else {
			if (encrypted) {
				sendSecureSms(context, text);
			} else {
				sendUnsecureSms(context, text);
			}
		}
	}

	// ------------------------------------------------------------------------

	/**
	 * Send unsecure sms.
	 * 
	 * @param context
	 *            the context
	 */
	private void sendUnsecureSms(Context context, String text) {
		if (text.length() > 0) {
			String phone = Setup.getPhone(context, hostUid);
			if (phone != null && phone.length() > 0) {
				if (DB.addSendMessage(context, hostUid, text, false,
						DB.TRANSPORT_SMS, false, DB.PRIORITY_MESSAGE)) {
					updateConversationlist(context);
					Communicator.sendNewNextMessageAsync(context,
							DB.TRANSPORT_SMS);
					messageText.setText("");
				}
			}
		}
	}

	// ------------------------------------------------------------------------

	/**
	 * Send secure sms.
	 * 
	 * @param context
	 *            the context
	 */
	private void sendSecureSms(Context context, String text) {
		if (text.length() > 0) {
			boolean encrypted = Setup.encryptedSentPossible(context, hostUid);
			String phone = Setup.getPhone(context, hostUid);
			if (phone != null && phone.length() > 0) {
				if (DB.addSendMessage(context, hostUid, text, encrypted,
						DB.TRANSPORT_SMS, false, DB.PRIORITY_MESSAGE)) {
					updateConversationlist(context);
					Communicator.sendNewNextMessageAsync(context,
							DB.TRANSPORT_SMS);
					messageText.setText("");
				}
			}
		}
	}

	// ------------------------------------------------------------------------

	/**
	 * Send secure msg.
	 * 
	 * @param context
	 *            the context
	 */
	private void sendSecureMsg(Context context, String text) {
		if (text.length() > 0) {
			text = possiblyRemoveImageAttachments(context, text, hostUid);
			boolean encrypted = Setup.encryptedSentPossible(context, hostUid);
			if (DB.addSendMessage(context, hostUid, text, encrypted,
					DB.TRANSPORT_INTERNET, false, DB.PRIORITY_MESSAGE)) {
				updateConversationlist(context);
				Communicator.sendNewNextMessageAsync(context,
						DB.TRANSPORT_INTERNET);
				messageText.setText("");
			}
		}
	}

	// ------------------------------------------------------------------------

	/**
	 * Send unsecure msg.
	 * 
	 * @param context
	 *            the context
	 */
	private void sendUnsecureMsg(Context context, String text) {
		if (text.length() > 0) {
			text = possiblyRemoveImageAttachments(context, text, hostUid);
			boolean encrypted = false;
			if (DB.addSendMessage(context, hostUid, text, encrypted,
					DB.TRANSPORT_INTERNET, false, DB.PRIORITY_MESSAGE)) {
				updateConversationlist(context);
				Communicator.sendNewNextMessageAsync(context,
						DB.TRANSPORT_INTERNET);
			}
			messageText.setText("");
		}
	}

	// ------------------------------------------------------------------------
	// ------------------------------------------------------------------------

	/**
	 * Sets the title of the conversation custom title bar.
	 * 
	 * @param title
	 *            the new title
	 */
	public void setTitle(String title) {
		if (titletext == null) {
			titletext = (TextView) findViewById(R.id.titletext);
		}
		titletext.setText(title);
	}

	// -------------------------------------------------------------------------
	// -------------------------------------------------------------------------

	/**
	 * Checks if is SMS mode available so we and our partner have enabled SMS
	 * mode by providing phone numbers.
	 * 
	 * @param context
	 *            the context
	 * @return true, if is SMS mode available
	 */
	private boolean isSMSModeAvailable(Context context) {
		int serverId = Setup.getServerId(context, hostUid);
		boolean smsOptionOn = Setup.isSMSOptionEnabled(context, serverId);
		boolean haveTelephoneNumber = Setup.havePhone(context, hostUid);
		return (haveTelephoneNumber && smsOptionOn);
	}

	// -------------------------------------------------------------------------

	/**
	 * Checks if is SMS mode available (because we have a telephone number) and
	 * if the mode is and on.
	 * 
	 * @param context
	 *            the context
	 * @return true, if is SMS mode available and on
	 */
	private boolean isSMSModeAvailableAndOn(Context context) {
		boolean smsmodeOn = Setup.isSMSModeOn(context, hostUid);
		return (isSMSModeAvailable(context) && smsmodeOn);
	}

	// -------------------------------------------------------------------------

	/**
	 * Update send button image according to the possible enabled SMS mode and
	 * according to the communication partner. For an unregistered user there
	 * will only be SMS sending available.
	 * 
	 * @param context
	 *            the context
	 */
	private void updateSendButtonImage(Context context) {
		isSMSModeOn = isSMSModeAvailableAndOn(context);
		isEncryptionOn = false;
		if (Setup.isGroup(context, hostUid)) {
			sendbutton.setImageResource(R.drawable.sendgroup);
			sendbutton
					.initializePressImageResource(R.drawable.sendgroup, false);
		} else if (hostUid < 0) {
			sendbutton.setImageResource(R.drawable.sendsms);
			sendbutton.initializePressImageResource(R.drawable.sendsms, false);
			// sendbutton.setImageResource(R.drawable.sendsms);
		} else if (isSMSModeOn) {
			if (Setup.isEncryptionAvailable(context, hostUid)) {
				isEncryptionOn = true;
				sendbutton.setImageResource(R.drawable.sendsmslock);
				sendbutton.initializePressImageResource(R.drawable.sendsmslock,
						false);
			} else {
				sendbutton.setImageResource(R.drawable.sendsms);
				sendbutton.initializePressImageResource(R.drawable.sendsms,
						false);
			}
		} else if (Setup.isEncryptionAvailable(context, hostUid)) {
			isEncryptionOn = true;
			sendbutton.setImageResource(R.drawable.sendlock);
			sendbutton.initializePressImageResource(R.drawable.sendlock, false);
		} else {
			sendbutton.setImageResource(R.drawable.send);
			sendbutton.initializePressImageResource(R.drawable.send, false);
		}
	}

	// -------------------------------------------------------------------------
	// -------------------------------------------------------------------------

	// "If the process is killed then all static variables will be reinitialized to their default values."

	/** The instance. */
	private static Conversation instance = null;

	/** The visible. */
	private static boolean visible = false;

	/**
	 * The last key stroke. Only written from Conversation and
	 * ConversationCompose. This allows basically to detect the type or fasttype
	 * mode where NoHang� skips any heavier computation to make the UI feel
	 * fast.
	 */
	public static long lastKeyStroke = 0;

	/**
	 * Checks if is typing. We want to skip background activity if the user is
	 * typing
	 * 
	 * @return true, if is typing
	 */
	public static boolean isTyping() {
		// If the conversation is NOT visible the user cannot type fast!
		return isVisible()
				&& !(lastKeyStroke == 0)
				&& ((System.currentTimeMillis() - lastKeyStroke) < Setup.TYPING_TIMEOUT_BEFORE_BACKGROUND_ACTIVITY);
	}

	/**
	 * Checks if is typing fast. We want to defer any heavier (also UI)
	 * computation like scrolling.
	 * 
	 * @return true, if is typing fast
	 */
	public static boolean isTypingFast() {
		// If the conversation is NOT visible the user cannot type fast!
		return isVisible()
				&& !(lastKeyStroke == 0)
				&& ((System.currentTimeMillis() - lastKeyStroke) < Setup.TYPING_TIMEOUT_BEFORE_UI_ACTIVITY);
	}

	/**
	 * Gets the single instance of Conversation.
	 * 
	 * @return single instance of Conversation
	 */
	public static Conversation getInstance() {
		return instance;
	}

	/**
	 * Returns true if the activity is visible.
	 * 
	 * @return true, if is visible
	 */
	public static boolean isVisible() {
		return (visible && instance != null);
	}

	/**
	 * Checks if this activity is alive and UI elements like status icons should
	 * be updated, e.g., when incoming messages arrive.
	 * 
	 * @return true, if is alive
	 */
	public static boolean isAlive() {
		return (instance != null && Conversation.hostUid != -1);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	public void onDestroy() {
		Conversation.visible = false;
		// Try some freeing of memory
		resetMapping();
		fastScrollView.clearChilds();
		conversationList.clear();
		conversationListDiff.clear();
		textViews.clear();
		images.clear();
		// For large conversations with images this is needed
		System.gc();
		super.onDestroy();
		/*
		 * Note: do not count on this method being called as a place for saving
		 * data! For example, if an activity is editing data in a content
		 * provider, those edits should be committed in either onPause() or
		 * onSaveInstanceState(Bundle), not here. This method is usually
		 * implemented to free resources like threads that are associated with
		 * an activity, so that a destroyed activity does not leave such things
		 * around while the rest of its application is still running. There are
		 * situations where the system will simply kill the activity's hosting
		 * process without calling this method (or any others) in it, so it
		 * should not be used to do things that are intended to remain around
		 * after the process goes away.
		 * 
		 * You can move your code to onPause() or onStop()
		 */
	}

	// --------------------------------------

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onStop()
	 */
	@Override
	public void onStop() {
		Conversation.visible = false;
		// If chat is not empty, save it as draft
		String msg = messageText.getText().toString();
		if (msg != null && msg.trim().length() > 0) {
			String draft = Utility.loadStringSetting(this, "cachedraft"
					+ hostUid, "");
			if (!draft.equals(msg)) {
				// only if message changed
				Utility.saveStringSetting(this, "cachedraft" + hostUid, msg);
				Utility.showToastShortAsync(this, "Draft saved.");
			}
		}
		// For large conversations with images this is needed
		System.gc();
		super.onStop();
	}

	// ------------------------------------------------------------------------

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onPause()
	 */
	@Override
	protected void onPause() {
		Conversation.visible = false;
		System.gc();
		super.onPause();
		// Necessary for the following situation:
		// if scrolled to somewhere and changing focused activity, the
		// scrollview would automatically try to
		// scroll up! this prevents!
		// Log.d("communicator", "@@@@ onPause() LOCK POSITION ");
		fastScrollView.lockPosition();
		if (scrolledDown) {
			// This trick will fore a rigorous scrolldown at onResume!
			hasScrolled = false;
		}
	}

	// --------------------------------------

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onResume()
	 */
	@Override
	public void onResume() {
		super.onResume();
		// For large conversations with images this is needed
		System.gc();
		if (!Conversation.isAlive()) {
			// this class instance was lost, close it
			Conversation.visible = false;
			this.finish();
		} else {
			Conversation.visible = true;

			// Only ONCE skip the resume after context menu closure
			if (skipResume) {
				skipResume = false;
				return;

			}

			// Reset error claims
			Setup.setErrorUpdateInterval(this, false);
			Scheduler.reschedule(this, false, false, true);

			conversationSize = DB.getConversationSize(this, hostUid, true);

			// Reset the new message counter
			Communicator.setNotificationCount(this, hostUid, true);
			// set all messages (internally as read)

			// Flag all messages (internally as read)
			DB.updateOwnMessageRead(this, hostUid);
			if (Conversation.isVisible()) {
				Communicator.sendReadConfirmation(this, hostUid);
			}

			// Cancel possible system notifications for this hostuid. Do this
			// only if the screen is also UNLOCKED otherwise the user might be
			// interested in further seeing the notification of an arrived
			// message!
			if (Utility.loadBooleanSetting(this, Setup.OPTION_NOTIFICATION,
					Setup.DEFAULT_NOTIFICATION)
					&& !Utility.isScreenLocked(this)) {
				Communicator.cancelNotification(this, hostUid);
			}

			// If (Utility.loadBooleanSetting(this, Setup.OPTION_NOTIFICATION,
			// Setup.DEFAULT_NOTIFICATION) && Utility.isScreenLocked(this)) {
			// onResume of the activity will be called when ACTION_SCREEN_ON
			// is fired. Create a handler and wait for ACTION_USER_PRESENT.
			// When it is fired, implement what you want for your activity.
			//
			// WE NEED TO WAIT UNTIL THE SCREEN IS UNLOCKED BEFORE WE
			// CANCEL THE NOTIFICATION
			// =>>> The UserPresentReceiver listens to USER_PRESENT, it will
			// clear the notification in this case!
			// }

			// ALWAYS (RE)SET KEYBOARD TO INVISIBLE (also when orientation
			// changes)!!!
			Utility.hideKeyboard(this);
			Utility.hideKeyboardExplicit(messageText);

			scrollOnCreateOrResume(this);
		}
	}

	// --------------------------------------

	/**
	 * Scroll on create or resume.
	 * 
	 * @param context
	 *            the context
	 */
	private void scrollOnCreateOrResume(Context context) {
		// Only scroll for the first time, it is annoying to scroll when
		// changing orientation, we want to prevent this!

		if (fastScrollView.isLocked()) {
			// Log.d("communicator", "@@@@ SCROLL RESTORE #0");
			fastScrollView.restoreLockedPosition();
		} else if (!hasScrolled) {
			hasScrolled = true;
			// Log.d("communicator",
			// "@@@@ onCreate() 1: not scrolled  down before");
			fastScrollView.postDelayed(new Runnable() {
				public void run() {
					// Log.d("communicator", "@@@@ SCROLL DOWN #1");
					isKeyboardVisible(conversationRootView);
					fastScrollView.scrollDown();
					// foceScrollDown();
					scrolledDown = true;
					fastScrollView.setScrollBackground(FASTSCROLLBACKLOCKEDBACKGROUND);
				}
			}, 200);
			fastScrollView.postDelayed(new Runnable() {
				public void run() {
					// Log.d("communicator", "@@@@ SCROLL DOWN #1");
					isKeyboardVisible(conversationRootView);
					fastScrollView.scrollDown();
					// foceScrollDown();
					scrolledDown = true;
					fastScrollView.setScrollBackground(FASTSCROLLBACKLOCKEDBACKGROUND);
				}
			}, 500);
			fastScrollView.postDelayed(new Runnable() {
				public void run() {
					// Log.d("communicator", "@@@@ SCROLL DOWN #1");
					isKeyboardVisible(conversationRootView);
					fastScrollView.scrollDown();
					// foceScrollDown();
					scrolledDown = true;
					fastScrollView.setScrollBackground(FASTSCROLLBACKLOCKEDBACKGROUND);
				}
			}, 1200);
		} else if (scrolledDown) {
			// Log.d("communicator", "@@@@ onCreate() 2: scrolled down lock");
			// scroll to restore position
			fastScrollView.postDelayed(new Runnable() {
				public void run() {
					// Log.d("communicator", "@@@@ SCROLL DOWN #2");
					fastScrollView.scrollDown();
				}
			}, 200);
		} else if (scrolledUp) {
			// Log.d("communicator", "@@@@ onCreate() 3: scrolled up lock");
			// Scroll to restore position
			fastScrollView.postDelayed(new Runnable() {
				public void run() {
					Log.d("communicator", "@@@@ SCROLL UP #3");
					fastScrollView.scrollUp();
				}
			}, 200);
		} else if (scrollItem > -1) {
			// Log.d("communicator",
			// "@@@@ onCreate() 4: scroll to specific position");
			// Scroll to restore position
			fastScrollView.postDelayed(new Runnable() {
				public void run() {
					Log.d("communicator", "@@@@ SCROLL ITEM #4:" + scrollItem);
					fastScrollView.scrollToItem(scrollItem + 1);
				}
			}, 200);
		}
	}

	// -------------------------------------------------------------------------
	// -------------------------------------------------------------------------

	/**
	 * Force scroll down.
	 * 
	 * @param keyboardVisible
	 *            the keyboard visible
	 */
	private void foceScrollDown(boolean keyboardVisible, int delay) {
		Log.d("communicator", "@@@@ foceScrollDown(alsoSetTextFocus? "
				+ keyboardVisible + ")");
		fastScrollView.postDelayed(new Runnable() {
			public void run() {
				fastScrollView.scrollDown();
			}
		}, 100 + delay);
		fastScrollView.postDelayed(new Runnable() {
			public void run() {
				fastScrollView.scrollDown();
			}
		}, 500 + delay);

		Log.d("communicator", "@@@@ foceScrollDown() -> keyboardVisible="
				+ keyboardVisible);

		// If keyboard is visible, then set cursor to text field!
		if (keyboardVisible) {
			messageText.postDelayed(new Runnable() {
				public void run() {
					lastKeyStroke = DB.getTimestamp();
					messageText.requestFocus();
					// Allow the next layout pass!
					FastScrollView.allowOneLayoutOverride = 3;
					Utility.showKeyboardExplicit(messageText);
					messageText.requestFocus();
					scrolledDown = true;
				}
			}, 200 + delay);
		}
		scrolledDown = true;
	}

	// -------------------------------------------------------------------------
	// -------------------------------------------------------------------------

	/**
	 * The active scroller. Each scroller increments this counter and will not
	 * resume if he has not the highest number any more => this means another
	 * scroller has arrived which will do the job of us.
	 */
	private int activeScoller = 0;

	/**
	 * Scroll down after typing fast.
	 * 
	 * @param showKeyboard
	 *            the show keyboard
	 */
	private void scrollDownAfterTypingFast(boolean showKeyboard) {
		if (!scrolledDown && hasScrolled) {
			Log.d("communicator",
					"@@@@ scollDownAfterTypingFast() => return (scrolledDown="
							+ scrolledDown + ")");
			return;
		}
		// -1 means we do not have a number yet!
		// note that this is not thread safe so potentially two threads may get
		// the same
		// number. this is OK for us as this only tries to reduce the number of
		// backward threads
		// when typing fast & long texts. The worst case scenario is only that
		// we try to scroll
		// more than once... but thats OK because we actually do not scroll if
		// we already are there!
		scrollDownAfterTypingFast(showKeyboard, -1);
	}

	/**
	 * Scroll down after typing fast.
	 * 
	 * @param showKeyboard
	 *            the show keyboard
	 * @param scrollNumber
	 *            the scroll number
	 */
	private void scrollDownAfterTypingFast(final boolean showKeyboard,
			final int scrollNumber) {
		// Log.d("communicator", "@@@@ scollDownAfterTypingFast([" +
		// scrollNumber
		// + "], showKeyboard=" + showKeyboard + ") ENTRY ");
		final Context context = this;
		if (isTypingFast()) {
			// Log.d("communicator", "@@@@ scollDownAfterTypingFast(["
			// + scrollNumber + "], showKeyboard=" + showKeyboard
			// + ") 1 => isTypingFast, retry ");
			fastScrollView.postDelayed(new Runnable() {
				public void run() {
					isKeyboardVisible(conversationRootView);
					if (!isTypingFast()) {
						// here we scroll if not already scrolled down!
						foceScrollDown(keyboardVisible || showKeyboard, 0);
					} else {
						int newOrOldScrollNumber = scrollNumber;
						if (scrollNumber < 0) {
							// take a new number
							activeScoller++;
							newOrOldScrollNumber = activeScoller;
							// Log.d("communicator",
							// "@@@@ scollDownAfterTypingFast(["
							// + scrollNumber
							// + "]) 1A => take new number "
							// + newOrOldScrollNumber
							// + " and wait...");
						} else {
							// test if there is someone "behind" us how will do
							// the work, so we can
							// stop our recursion...
							if (activeScoller > scrollNumber) {
								// Log.d("communicator",
								// "@@@@ scollDownAfterTypingFast(["
								// + scrollNumber
								// + "]) 1A_1 => we have an old number = "
								// + newOrOldScrollNumber + " < "
								// + activeScoller + " => QUIT");
								return; // SOMEONE ELSE WILL SCROLL FOR US...
							}
							// Log.d("communicator",
							// "@@@@ scollDownAfterTypingFast(["
							// + scrollNumber
							// + "]) 1A_2 => we have the highest number "
							// + newOrOldScrollNumber
							// + " ... further waiting");
						}
						// WAIT RECUSIVELY
						scrollDownAfterTypingFast(showKeyboard,
								newOrOldScrollNumber);
					}
				}
			}, 100 + Setup.TYPING_TIMEOUT_BEFORE_UI_ACTIVITY);
			// Ensure that we scroll after this time at least if
			// the user rests
			// for some time..
		} else if (!isTypingFast()) {
			scrollDownNow(context, showKeyboard);
		}
	}

	// -------------------------------------------------------------------------

	/**
	 * Scroll down now.
	 * 
	 * @param context
	 *            the context
	 * @param showKeyboard
	 *            the show keyboard
	 * @param delay
	 *            the delay
	 */
	public void scrollDownNow(final Context context, final boolean showKeyboard) {
		scrollDownSoon(context, showKeyboard, 0);
	}

	// -------------------------------------------------------------------------

	/**
	 * Scroll down soon or now - do not wait for fast typing to end, we should
	 * be sure that the user does not fast type!
	 * 
	 * @param context
	 *            the context
	 * @param showKeyboard
	 *            the show keyboard
	 */
	public void scrollDownSoon(final Context context,
			final boolean showKeyboard, final int delay) {
		// we should update the scroll view height here because it was
		// blocked/skipped during fast typing!
		fastScrollView.potentiallyRefreshState(false);
		// Otherwise we can scroll immediately if the user already rests!
		// and is not typing
		fastScrollView.postDelayed(new Runnable() {
			public void run() {
				updateConversationTitleAsync(context);
				isKeyboardVisible(conversationRootView);
				foceScrollDown(keyboardVisible || showKeyboard, delay);
			}
		}, 200);
		scrolledDown = true;
	}

	// -------------------------------------------------------------------------

	/**
	 * Rebuild conversationlist.
	 * 
	 * @param context
	 *            the context
	 */
	public void rebuildConversationlist(final Context context) {
		fastScrollView.clearChilds();
		resetMapping();
		textViews.clear();
		images.clear();
		// For large conversations with images this is needed
		System.gc();

		loadConversationList(context, hostUid, maxScrollMessageItems);
		try {
			// For every item in the conversation list we build a balloon
			// which is also filling the mapping (that we just cleaned)
			for (ConversationItem conversationItem : conversationList) {
				View newView = addConversationLine(context, conversationItem);
				if (newView != null) {
					fastScrollView.addChild(newView);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		if (Conversation.isVisible()) {
			Communicator.sendReadConfirmation(context, hostUid);
		}

		conversationSize = DB.getConversationSize(this, hostUid, true);
		updateConversationTitle(context);
	}

	// -------------------------------------------------------------------------

	/**
	 * Show titlebar async from non UI thread.
	 * 
	 * @param context
	 *            the context
	 */
	public void showTitlebarAsync(final Context context) {
		final Handler mUIHandler = new Handler(Looper.getMainLooper());
		mUIHandler.post(new Thread() {
			@Override
			public void run() {
				super.run();
				titleconversation.setVisibility(View.VISIBLE);
			}
		});
	}

	// -------------------------------------------------------------------------

	/**
	 * Update conversationlist async from non UI thread.
	 * 
	 * @param context
	 *            the context
	 */
	public static void updateConversationlistAsync(final Context context) {
		final Handler mUIHandler = new Handler(Looper.getMainLooper());
		mUIHandler.post(new Thread() {
			@Override
			public void run() {
				super.run();
				if (Conversation.isAlive()) {
					Conversation.getInstance().updateConversationlist(context);
				}
			}
		});
	}

	// -------------------------------------------------------------------------

	/**
	 * Update conversationlist.
	 * 
	 * @param context
	 *            the context
	 */
	public void updateConversationlist(final Context context) {
		loadConversationList(context, hostUid, maxScrollMessageItems);
		try {
			boolean isScrolledDown = Conversation.scrolledDown;
			if (!isScrolledDown) {
				fastScrollView.lockPosition();
			}
			if (conversationListDiff.size() > 0) {
				int additionalItems = 0;
				for (ConversationItem conversationItem : conversationListDiff) {
					if (!isAlreadyMapped(conversationItem.localid)) {
						View newView = addConversationLine(context,
								conversationItem);
						if (newView != null) {
							additionalItems++;
							fastScrollView.addChild(newView);
						}
					}
				}
				conversationSize += additionalItems;
			}

			if (!isScrolledDown) {
				fastScrollView.restoreLockedPosition();
			} else {
				scrollDownAfterTypingFast(false);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (Conversation.isVisible()) {
			Communicator.sendReadConfirmation(this, hostUid);
		}
		updateConversationTitle(context);
	}

	// ------------------------------------------------------------------------

	/**
	 * Adds the mapping. Note the convention: Use local id instead as a
	 * placeholder if a mid not yet exists (before the message is sent or if it
	 * is an SMS). Local ids by convertion are negative numbers.
	 * 
	 * @param mid
	 *            the mid
	 * @param localid
	 *            the localid
	 * @param sent
	 *            the sent
	 * @param received
	 *            the received
	 * @param read
	 *            the read
	 * @param text
	 *            the text
	 * @param oneline
	 *            the oneline
	 * @param speech
	 *            the speech
	 * @param progress
	 *            the progress
	 */
	private void addMapping(int localid, ImageView sent, ImageView received,
			ImageView read, EditText text, LinearLayout oneline,
			ImageView speech, ProgressBar progress, String multipartid,
			View parent) {
		Mapping mappingItem = new Mapping();
		mappingItem.read = read;
		mappingItem.received = received;
		mappingItem.sent = sent;
		mappingItem.text = text;
		mappingItem.oneline = oneline;
		mappingItem.speech = speech;
		mappingItem.progress = progress;
		mappingItem.multipartid = multipartid;
		mappingItem.parent = parent;
		// Convention: use local id instead as a placeholder
		int insertId = -1 * localid;
		// Log.d("communicator",
		// "MAPPING INSERT "
		// + insertId);
		mapping.put(insertId, mappingItem);
	}

	/**
	 * Checks if an entry is already mapped and hence displayed.
	 * 
	 * @param localid
	 *            the localid
	 * @return true, if is already mapped
	 */
	private boolean isAlreadyMapped(int localid) {
		int insertedId = -1 * localid;
		return mapping.containsKey(insertedId);
	}

	/**
	 * Reset mapping.
	 */
	private void resetMapping() {
		mapping.clear();
	}

	// ------------------------------------------------------------------------

	/**
	 * Gets the mapping by multipartid.
	 * 
	 * @param context
	 *            the context
	 * @param multipartid
	 *            the multipartid
	 * @return the mapping
	 */
	public List<Mapping> getMapping(Context context, String multipartid) {
		List<Mapping> returnList = new ArrayList<Mapping>();
		for (Entry<Integer, Mapping> entry : mapping.entrySet()) {
			Mapping mappingItem = entry.getValue();
			if (mappingItem.multipartid.equals(multipartid)) {
				returnList.add(mappingItem);
			}
		}
		return returnList;
	}

	// ------------------------------------------------------------------------

	/**
	 * Gets the mapping. If mid is negative by convention we look for a local id
	 * mapped element because the mid will still be -1 (eg for not-yet-sent
	 * messages or for SMS).
	 * 
	 * @param context
	 *            the context
	 * @param mid
	 *            the mid
	 * @return the mapping
	 */
	public Mapping getMapping(Context context, int mid) {
		// Log.d("communicator",
		// "MAPPING REQUEST "
		// + mid);
		Mapping mappingItem = mapping.get(mid);
		if (mappingItem != null && mid > 0) {
			// if we already found a valid mid, then return the mappingItem
			return mappingItem;
		} else if (mid < 0) {
			// FOR SMS MESSAGES: mid == local id!!!
			int negativeLocalId = mid;
			mappingItem = mapping.get(negativeLocalId);
			if (mappingItem != null) {
				// if we already found a valid mid, then return the mappingItem
				return mappingItem;
			}
		}
		// otherwise lookup the localid in the DB and update it!
		// in the not-sent-case there is no mid yet, so the localid is used in
		// the mapping
		// it will be available under the NEGATIVE localid to prevent conflicts
		// with other (always positive) mids
		int localId = DB.getHostLocalIdByMid(context, mid, hostUid);
		if (localId >= 0) {
			// found a localId to the (now) available mid ... so update the
			// mapping to this mid!
			int negativeLocalId = localId * -1;
			mappingItem = mapping.get(negativeLocalId);
			// ATTENTION: for SMS-messages, there will never be a mid!!! DO NOT
			// UPDATE THE MAPPING IN THIS CASE!
			if (mid > 0) {
				// update in the real mapping for later usage
				mapping.put(mid, mappingItem);
			}
		}
		return mappingItem;
	}

	// -------------------------------------------------------------------------

	/**
	 * Sets the sent status for a displayed message.
	 * 
	 * @param context
	 *            the context
	 * @param mid
	 *            the mid
	 */
	public static void setSent(Context context, int mid) {
		Conversation conversation = Conversation.getInstance();
		if (Conversation.isAlive()) {
			Mapping mappingItem = conversation.getMapping(context, mid);
			if (mappingItem != null) {
				if (mappingItem.received.getVisibility() != View.VISIBLE) {
					// if not already received...
					mappingItem.sent.setVisibility(View.VISIBLE);
					mappingItem.received.setVisibility(View.GONE);
					mappingItem.read.setVisibility(View.GONE);
				}
			}
		}
	}

	// -------------------------------------------------------------------------

	/**
	 * Hides all multiparts that may be displayed already. We are about to add
	 * another multipart and only want to see this new one.
	 * 
	 * @param context
	 *            the context
	 * @param multipartid
	 *            the multipartid
	 * @param percent
	 *            the percent
	 * @param fullText
	 *            the full text
	 */
	public static void hideMultiparts(Context context, String multipartid) {
		Conversation conversation = Conversation.getInstance();
		if (Conversation.isAlive()) {
			List<Mapping> mappingItems = conversation.getMapping(context,
					multipartid);
			for (Mapping mappingItem : mappingItems) {
				mappingItem.parent.setVisibility(View.GONE);
			}
		}
	}

	// -------------------------------------------------------------------------

	/**
	 * Sets the multipart progress bar if the progress is less than 100%.
	 * 
	 * @param context
	 *            the context
	 * @param multipartid
	 *            the multipartid
	 * @param percent
	 *            the percent
	 * @param fullText
	 *            the full text
	 */
	public static void setMultipartProgress(Context context,
			String multipartid, int percent, String fullText) {
		Conversation conversation = Conversation.getInstance();
		if (Conversation.isAlive()) {
			List<Mapping> mappingItems = conversation.getMapping(context,
					multipartid);
			for (Mapping mappingItem : mappingItems) {
				if (percent < 100) {
					if (mappingItem.progress != null) {
						mappingItem.progress.setVisibility(View.VISIBLE);
						mappingItem.progress.setMax(100);
						mappingItem.progress.setProgress(percent);
					}
					mappingItem.text.setVisibility(View.GONE);
				}
			}
		}
	}

	// -------------------------------------------------------------------------

	/**
	 * Sets the revoked status for a displayed message.
	 * 
	 * @param context
	 *            the context
	 * @param mid
	 *            the mid
	 */
	public static void setRevokedInConversation(Context context, int mid) {
		Conversation conversation = Conversation.getInstance();
		if (Conversation.isAlive()) {
			Mapping mappingItem = conversation.getMapping(context, mid);
			if (mappingItem != null) {
				mappingItem.text.setText(DB.REVOKEDTEXT);
			}
		}
	}

	// -------------------------------------------------------------------------

	/**
	 * Sets the received status for a displayed message.
	 * 
	 * @param context
	 *            the context
	 * @param mid
	 *            the mid
	 */
	public static void setReceived(Context context, int mid) {
		Conversation conversation = Conversation.getInstance();
		if (Conversation.isAlive()) {
			Mapping mappingItem = conversation.getMapping(context, mid);
			if (mappingItem != null) {
				if (mappingItem.read.getVisibility() != View.VISIBLE) {
					// if not already read...
					mappingItem.sent.setVisibility(View.GONE);
					mappingItem.received.setVisibility(View.VISIBLE);
					mappingItem.read.setVisibility(View.GONE);
				}
			}
		}
	}

	// -------------------------------------------------------------------------

	/**
	 * Sets the failed status async from non UI thread.
	 * 
	 * @param context
	 *            the context
	 * @param localid
	 *            the localid
	 */
	public static void setFailedAsync(final Context context, final int localid) {
		final Handler mUIHandler = new Handler(Looper.getMainLooper());
		mUIHandler.post(new Thread() {
			@Override
			public void run() {
				super.run();
				setFailed(context, localid);
			}
		});
	}

	/**
	 * Sets the failed status for a displayed message.
	 * 
	 * @param context
	 *            the context
	 * @param localid
	 *            the localid
	 */
	public static void setFailed(Context context, int localid) {
		Conversation conversation = Conversation.getInstance();
		if (Conversation.isAlive()) {
			Mapping mappingItem = conversation
					.getMapping(context, -1 * localid);
			if (mappingItem != null) {
				// Rounded corners failed
				mappingItem.oneline
						.setBackgroundResource(R.drawable.rounded_cornersme_failed);
				mappingItem.speech.setImageResource(R.drawable.speechmefailed);
			}
		}
	}

	// -------------------------------------------------------------------------

	/**
	 * Sets the decryption failed status for a displayed message. This is, it
	 * has been received and tried to read but with no luck on decryption. The
	 * read TS is therefore negative as an indicator.
	 * 
	 * @param context
	 *            the context
	 * @param mid
	 *            the mid
	 */
	public static void setDecryptionFailed(Context context, int mid) {
		if (!Utility.loadBooleanSetting(context, Setup.OPTION_NOREAD,
				Setup.DEFAULT_NOREAD)) {
			Conversation conversation = Conversation.getInstance();
			if (Conversation.isAlive()) {
				Mapping mappingItem = conversation.getMapping(context, mid);
				if (mappingItem != null) {
					// Rounded corners failed
					mappingItem.oneline
							.setBackgroundResource(R.drawable.rounded_cornersme_failed);
					mappingItem.speech
							.setImageResource(R.drawable.speechmefailed);

					mappingItem.sent.setVisibility(View.GONE);
					mappingItem.received.setVisibility(View.GONE);
					mappingItem.read.setVisibility(View.VISIBLE);
					mappingItem.read
							.setImageResource(R.drawable.msgdecryptionfailed);
				}
			}
		}
	}

	// ------------------------------------------------------------------------

	/**
	 * Sets the read status for a displayed message.
	 * 
	 * @param context
	 *            the context
	 * @param mid
	 *            the mid
	 */
	public static void setRead(Context context, int mid) {
		if (!Utility.loadBooleanSetting(context, Setup.OPTION_NOREAD,
				Setup.DEFAULT_NOREAD)) {
			Conversation conversation = Conversation.getInstance();
			if (Conversation.isAlive()) {
				Mapping mappingItem = conversation.getMapping(context, mid);
				if (mappingItem != null) {
					mappingItem.sent.setVisibility(View.GONE);
					mappingItem.received.setVisibility(View.GONE);
					mappingItem.read.setVisibility(View.VISIBLE);
				}
			}
		}
	}

	// ------------------------------------------------------------------------

	/**
	 * Reset values of this activity. This is usually called from outside if the
	 * activity is initialized to be used with a specific hostUid (= the
	 * conversation partner).
	 * 
	 * @param hostUid
	 *            the host uid
	 */
	public static void resetValues(int hostUid) {
		Log.d("communicator", " GROUP resetValues() hostUid:=" + hostUid);
		Conversation.hostUid = hostUid;
		Conversation.maxScrollMessageItems = Setup.MAX_SHOW_CONVERSATION_MESSAGES;
		hasScrolled = false;
		scrollItem = -1;
	}

	// ------------------------------------------------------------------------

	/**
	 * Adds the conversation line. Here is where the mapping is created and
	 * where all elements (e.g. status icons) get created for a specific
	 * conversation item.
	 * 
	 * @param context
	 *            the context
	 * @param conversationItem
	 *            the conversation item
	 * @return the view
	 */
	private View addConversationLine(final Context context,
			final ConversationItem conversationItem) {

		boolean isGroup = Setup.isGroup(context, hostUid);
		int hostUid = conversationItem.from;

		// Do not show system messages or empty messages
		if (conversationItem.text == null
				|| conversationItem.text.length() == 0) {
			return null;
		}

		View conversationlistitem = null;
		ImageSmileyEditText conversationText = null;

		// Inflate other XMLs for me or for my conversation partner
		if (!conversationItem.me()) {
			conversationlistitem = inflater.inflate(R.layout.conversationitem,
					null);
			conversationText = (ImageSmileyEditText) conversationlistitem
					.findViewById(R.id.conversationtext);
			conversationText.setConversationItem(conversationItem);

			// Rounded corners
			LinearLayout oneline = (LinearLayout) conversationlistitem
					.findViewById(R.id.oneline);
			oneline.setBackgroundResource(R.drawable.rounded_corners);

			ImageView speech = (ImageView) conversationlistitem
					.findViewById(R.id.msgspeech);

			ProgressBar progress = (ProgressBar) conversationlistitem
					.findViewById(R.id.conversationprogress);
			// Hide per default, only used for multi part receiving
			progress.setVisibility(View.GONE);

			boolean isAlert = conversationItem.text
					.startsWith(Setup.ALERT_PREFIX);
			if (isAlert) {
				// Rounded corners failed
				oneline.setBackgroundResource(R.drawable.rounded_corners_alert);
				speech.setImageResource(R.drawable.speechalert);
			}

			Bitmap avatar = retrieveAvatar(context, hostUid, isAlert, isGroup,
					true);
			if (avatar != null) {
				speech.setImageBitmap(avatar);
				LinearLayout.LayoutParams lpSpeech = new LinearLayout.LayoutParams(
						60, LayoutParams.WRAP_CONTENT);
				speech.setLayoutParams(lpSpeech);
				fixed_inset = 200;
			}

			// Create a mapping for later async update
			addMapping(conversationItem.localid, null, null, null,
					conversationText, oneline, speech, progress,
					conversationItem.multipartid, conversationlistitem);
		} else {
			conversationlistitem = inflater.inflate(
					R.layout.conversationitemme, null);

			// // Rounded corners
			LinearLayout oneline = (LinearLayout) conversationlistitem
					.findViewById(R.id.oneline);
			oneline.setBackgroundResource(R.drawable.rounded_cornersme);

			ImageView sent = (ImageView) conversationlistitem
					.findViewById(R.id.msgsent);
			ImageView received = (ImageView) conversationlistitem
					.findViewById(R.id.msgreceived);
			ImageView read = (ImageView) conversationlistitem
					.findViewById(R.id.msgread);
			conversationText = (ImageSmileyEditText) conversationlistitem
					.findViewById(R.id.conversationtext);
			conversationText.setConversationItem(conversationItem);
			ImageView speech = (ImageView) conversationlistitem
					.findViewById(R.id.msgspeech);

			// Create a mapping for later async update
			addMapping(conversationItem.localid, sent, received, read,
					conversationText, oneline, speech, null,
					DB.NO_MULTIPART_ID, conversationlistitem);

			if (conversationItem.read < -10) {
				// decryption failed
				sent.setVisibility(View.GONE);
				received.setVisibility(View.GONE);
				read.setImageResource(R.drawable.msgdecryptionfailed);

				// Rounded corners failed
				oneline.setBackgroundResource(R.drawable.rounded_cornersme_failed);
				speech.setImageResource(R.drawable.speechmefailed);
			} else if (conversationItem.read > 0
					&& !Utility.loadBooleanSetting(context,
							Setup.OPTION_NOREAD, Setup.DEFAULT_NOREAD)) {
				// only display read for the people that allow this
				sent.setVisibility(View.GONE);
				received.setVisibility(View.GONE);
			} else if (conversationItem.received > 0) {
				sent.setVisibility(View.GONE);
				read.setVisibility(View.GONE);
			} else if (conversationItem.sent > 0) {
				received.setVisibility(View.GONE);
				read.setVisibility(View.GONE);
			} else {
				sent.setVisibility(View.GONE);
				received.setVisibility(View.GONE);
				read.setVisibility(View.GONE);
			}

			if (conversationItem.smsfailed) {
				// Rounded corners failed
				oneline.setBackgroundResource(R.drawable.rounded_cornersme_failed);
				speech.setImageResource(R.drawable.speechmefailed);
			}
		}

		String text = conversationItem.text;
		if (text.startsWith(Setup.ALERT_PREFIX)) {
			text = text.substring(Setup.ALERT_PREFIX.length());
		}

		boolean isInvitation = conversationItem.text
				.startsWith(GROUPINVITATION_PREFIX);
		if (isInvitation) {
			// parse groupsecret
			int start = GROUPINVITATION_PREFIX.length();
			int end = conversationItem.text.indexOf(" ]", start);
			final String groupsecret = conversationItem.text.substring(start,
					end);
			text = text.replace(groupsecret + " ", "");
			final int serverId = Setup.getServerId(context, hostUid);

			LinearLayout conversationaddition = (LinearLayout) conversationlistitem
					.findViewById(R.id.conversationaddition);

			final Button acceptButton = new Button(context);
			acceptButton.setText(" Join Group ");
			LinearLayout.LayoutParams lpButton = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.MATCH_PARENT,
					LinearLayout.LayoutParams.MATCH_PARENT);
			lpButton.setMargins(20, 20, 20, 20);
			acceptButton.setLayoutParams(lpButton);
			acceptButton.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					acceptButton.setEnabled(false);
					Setup.groupConfirm(context, serverId, groupsecret);
				}
			});
			if (!conversationItem.me() && groupsecret != null
					&& groupsecret.length() > 1) {
				// Only add the JOIN button to the other host that was invited!
				conversationaddition.addView(acceptButton);
			}
		}

		// Add this to this list in order to later adjust the width
		textViews.add(conversationText);
		conversationText.setMe(conversationItem.me());
		// Set the current width
		updateTextViewWidth(conversationText, currentScreenWidth);

		// Currently not needed
		//
		// conversationText
		// .setOnCutCopyPasteListener(new
		// ImageSmileyEditText.OnCutCopyPasteListener() {
		// public void onPaste() {
		// }
		//
		// public void onCut() {
		// }
		//
		// public void onCopy() {
		// }
		// });

		TextView conversationTime = (TextView) conversationlistitem
				.findViewById(R.id.conversationtime);

		// // set software layering
		// conversationText.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

		ImageView locked = (ImageView) conversationlistitem
				.findViewById(R.id.msglocked);
		if (!conversationItem.encrypted) {
			locked.setVisibility(View.GONE);
		}

		ImageView sms = (ImageView) conversationlistitem
				.findViewById(R.id.msgsms);
		if (conversationItem.transport == DB.TRANSPORT_INTERNET) {
			sms.setVisibility(View.GONE);
		}

		OnLongClickListener longClickListener = new OnLongClickListener() {
			public boolean onLongClick(View v) {
				promptMessageDetails(context, conversationItem);
				return true;
			}
		};
		conversationlistitem.setLongClickable(true);
		conversationlistitem.setOnLongClickListener(longClickListener);
		conversationTime.setLongClickable(true);
		conversationTime.setOnLongClickListener(longClickListener);

		OnClickListener clickListener = new OnClickListener() {
			public void onClick(View v) {
				if (Conversation.isAlive()) {
					showMessageMenu(Conversation.getInstance(),
							conversationItem);
				}
			}
		};
		conversationlistitem.setOnClickListener(clickListener);
		conversationTime.setOnClickListener(clickListener);

		long time = conversationItem.sent;
		if (time < 0) {
			time = conversationItem.created;
		}
		conversationTime.setText(DB.getDateString(time, false));
		conversationTime.setId(conversationTime.hashCode());

		conversationText.setText(text);
		conversationText.setId(conversationItem.hashCode());

		return conversationlistitem;
	}

	// ------------------------------------------------------------------------

	private static HashMap<String, Bitmap> conversationImageCache = new HashMap<String, Bitmap>();

	// ------------------------------------------------------------------------

	/**
	 * Invalidate avatar cache if an avatar was changed.
	 */
	public static void invalidateAvatarCache() {
		Log.d("communicator", "AVATAR CLEAR CACHE ");

		conversationImageCache.clear();
	}

	// ------------------------------------------------------------------------

	/**
	 * Retrieve conversation avatar either create it freshly or take it from the
	 * cache for faster processing.
	 * 
	 * @param context
	 *            the context
	 * @param uid
	 *            the uid
	 * @param alert
	 *            the alert
	 * @return the bitmap
	 */
	@SuppressLint("DefaultLocale")
	public static Bitmap retrieveAvatar(Context context, int uid,
			boolean alert, boolean fakeAvatarIfNoOther, boolean speech) {

		String id = uid + "_" + fakeAvatarIfNoOther + "_" + alert + "_"
				+ speech;

		Log.d("communicator", "AVATAR #1 " + id);

		if (!conversationImageCache.containsKey(id)) {
			initializeBitmaps(context);

			Log.d("communicator", "AVATAR #2");

			Bitmap avatar = Setup.getAvatarAsBitmap(context, uid);
			if (avatar != null) {
				Log.d("communicator", "AVATAR #3");
				Bitmap bitmap = Bitmap.createBitmap(bitmap_speechmaster);

				bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
				Canvas canvas = new Canvas(bitmap);
				Rect src = new Rect();
				src.left = 14;
				src.top = 10;
				src.bottom = 90;
				src.right = 90;
				Rect dst = new Rect();
				dst.left = 0;
				dst.top = 10;
				dst.bottom = 90;
				dst.right = 75;
				canvas.drawBitmap(avatar, src, dst, null);

				if (speech) {
					if (!alert) {
						canvas.drawBitmap(bitmap_speech, 53, 10, null);
					} else {
						canvas.drawBitmap(bitmap_speechalert, 53, 10, null);
					}
				}

				conversationImageCache.put(id, bitmap);
			} else {
				Log.d("communicator", "AVATAR #4");
				if (!fakeAvatarIfNoOther) {
					Log.d("communicator", "AVATAR #5");
					conversationImageCache.put(id, null);
				} else {
					Log.d("communicator", "AVATAR #6");
					// TODO: create fake avatar here!!!
					Bitmap bitmap = Bitmap
							.createBitmap(bitmap_speechmastersmall);
					bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
					Canvas canvas = new Canvas(bitmap);
					String name = Main.UID2Name(context, uid, false);
					String firstChar = name.substring(0, 1).toUpperCase();

					// parseColor("#FF4377B2")

					// final int add = 120;
					final int add = 0;
					int hashCode = Math.abs(name.hashCode());
					int r = add + (hashCode % 100);
					int g = add + ((hashCode / 100) % 100);
					int b = add + ((hashCode / 1000) % 100);
					bitmap.eraseColor(Color.rgb(r, g, b));

					// Log.d("communicator", "RGB r="+r + ", g=" + g + ", b=" +
					// b);

					Paint paint = new Paint();
					paint.setColor(Color.WHITE);
					paint.setTextSize(55);

					// canvas.drawBitmap(bitmap_speechmastersmall, 0, 0, null);
					// paint.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
					canvas.drawText(firstChar, 15, 50, paint);

					if (speech) {
						if (!alert) {
							canvas.drawBitmap(bitmap_speech, 53, 10, null);
						} else {
							canvas.drawBitmap(bitmap_speechalert, 53, 10, null);
						}

					}
					conversationImageCache.put(id, bitmap);
				}
			}
		}

		Log.d("communicator", "AVATAR #7");

		Bitmap returnBitmap = conversationImageCache.get(id);

		return returnBitmap;
	}

	// ------------------------------------------------------------------------
	// ------------------------------------------------------------------------

	/**
	 * Possible prompt for sending a new session key if this is not possible
	 * because one of the involved users does not have encryption enabled.
	 * 
	 * @param context
	 *            the context
	 */
	private void possiblePromptNewSession(Context context) {
		if (Setup.isGroup(context, hostUid)) {
			promptInfo(
					this,
					"Group",
					"Encryption in a group is used whenever you have the account key of a member.\n\nIf"
							+ " you do not have the account key of a member (e.g., the member disabled encryption) then"
							+ " all messages to this member are send in plaintext and will NOT appear "
							+ "as group messages at this member!");
			return;
		} else if (hostUid < 0) {
			// Tell the user to register
			promptInfo(
					this,
					"No Registered User",
					"In order to use encryption, your communication partner needs to be registered.");
			return;
		} else if (!Setup.isEncryptionAvailable(this, hostUid)) {
			// Tell the user to activate
			promptInfo(
					this,
					"Encryption Disabled",
					"You cannot use encryption because either you or your communication partner has not"
							+ " turned on this feature.\n\nIn order to use encryption both communication"
							+ " partners need to turn this feature on in their settings.");
			return;
		}
		// if (!isSMSModeAvailable(context)) {
		// // No choice, because SMS is not available
		// sendNewSession(context, DB.TRANSPORT_INTERNET);
		// return;
		// }
		// ALWAYS OFFER A CHOICE TO MAKE SURE WE REALLY WANT TO SEND A NEW
		// SESSION
		// Choice between Internet and SMS
		promptNewSession(context);
	}

	// -------------------------------------------------------------------------

	/**
	 * Send a new session key using the given transport.
	 * 
	 * @param context
	 *            the context
	 * @param transport
	 *            the transport
	 */
	public void sendNewSession(Context context, int transport) {
		// Send new key
		Communicator.getAESKey(this, Conversation.hostUid, true, transport,
				false, null, true, false);
	}

	// ------------------------------------------------------------------------

	/**
	 * Prompt to send a new session key. Prompt the user for a transport
	 * (Internet or SMS).
	 * 
	 * @param context
	 *            the context
	 */
	private void promptNewSession(final Context context) {
		String title = "Send New Session Key";
		String text = "Send the new session key via Internet or SMS?";

		// No choice, because SMS is not available
		final boolean noSMSOption = !isSMSModeAvailable(context);
		if (noSMSOption) {
			text = "Send the new session key via Internet?";
		}

		new MessageAlertDialog(context, title, text, null, null, " Cancel ",
				new MessageAlertDialog.OnSelectionListener() {
					public void selected(int button, boolean cancel) {
						// nothing
					}
				}, new MessageAlertDialog.OnInnerViewProvider() {

					public View provide(final MessageAlertDialog dialog) {
						LinearLayout buttonLayout = new LinearLayout(context);
						buttonLayout.setOrientation(LinearLayout.HORIZONTAL);
						buttonLayout.setGravity(Gravity.CENTER_HORIZONTAL);

						LinearLayout.LayoutParams lpButtons = new LinearLayout.LayoutParams(
								180, 140);
						lpButtons.setMargins(20, 20, 20, 20);

						ImageLabelButton internetButton = new ImageLabelButton(
								context);
						internetButton.setTextAndImageResource("Internet",
								R.drawable.send);
						internetButton.setLayoutParams(lpButtons);
						internetButton
								.setOnClickListener(new View.OnClickListener() {
									public void onClick(View v) {
										sendNewSession(context,
												DB.TRANSPORT_INTERNET);
										dialog.dismiss();
									}
								});
						ImageLabelButton smsButton = new ImageLabelButton(
								context);
						smsButton.setTextAndImageResource("SMS",
								R.drawable.sendsms);
						smsButton.setLayoutParams(lpButtons);
						smsButton
								.setOnClickListener(new View.OnClickListener() {
									public void onClick(View v) {
										sendNewSession(context,
												DB.TRANSPORT_SMS);
										dialog.dismiss();
									}
								});
						buttonLayout.addView(internetButton);
						if (!noSMSOption) {
							buttonLayout.addView(smsButton);
						}
						return buttonLayout;
					}
				}).show();
	}

	// ------------------------------------------------------------------------

	/**
	 * Prompt message details. Use the Messagedetails activity for that. Set the
	 * hostUid and the conversationItem here.
	 * 
	 * @param context
	 *            the context
	 * @param conversationItem
	 *            the conversation item
	 */
	private void promptMessageDetails(final Context context,
			final ConversationItem conversationItem) {
		try {
			Intent dialogIntent = new Intent(context,
					MessageDetailsActivity.class);
			dialogIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			MessageDetailsActivity.conversationItem = conversationItem;
			MessageDetailsActivity.hostUid = hostUid;
			context.startActivity(dialogIntent);
		} catch (Exception e) {
			e.printStackTrace();
			// ignore
		}
	}

	// ------------------------------------------------------------------------

	/**
	 * Load conversation list. This is a helper method that uses the database DB
	 * class. It constructs a conversationListDiff list that can be used to only
	 * update the currently displayed conversation. This is necessary to FAST
	 * add new sent or received messages! Note that it is not possible to change
	 * the order later on. The only case where this matters is when suspending a
	 * message for sending and auto-sending a fresh session key before sending
	 * the suspended message afterwards. In this case the suspended message was
	 * added before the key message but both messages were sent in the other
	 * order. It may be harder to fix this because we want to see the message
	 * already even at a point when it is not clear if a new key needs to be
	 * generated and send before. A hot-fix might be to add the message another
	 * time and make the previous message invisible to fake a correct order. But
	 * as this seems not to be clean currently we live with a wrong order in
	 * this scenario. Still it is the correct created-order but only the wrong
	 * sent-order.
	 * 
	 * @param context
	 *            the context
	 * @param hostUid
	 *            the host uid
	 * @param maxScrollMessageItems
	 *            the max scroll message items
	 */
	private void loadConversationList(Context context, int hostUid,
			int maxScrollMessageItems) {
		List<ConversationItem> conversationListNew = new ArrayList<ConversationItem>();
		DB.loadConversation(context, hostUid, conversationListNew,
				maxScrollMessageItems);
		conversationListDiff.clear();
		for (ConversationItem itemNew : conversationListNew) {
			boolean found = false;
			for (ConversationItem item : conversationList) {
				// && item.text.equals(itemNew.text) : Be SURE that we can stop
				// here...
				// if we combine multipart messages this might NOT be the case
				// for SMS
				// if we just test the localid's because when combining, we
				// delete all other parts
				// and then we somehow "reuse" the localid for the combined
				// message (unfortunately)
				if (item.localid == itemNew.localid
						&& item.text.equals(itemNew.text)
						&& item.parts == itemNew.parts) {
					found = true;
					break;
				}
			}
			if (!found) {
				Log.d("communicator", "MULTIPART conversationListDiff: part="
						+ itemNew.part + ", parts=" + itemNew.parts + ", text="
						+ itemNew.text);
				conversationListDiff.add(itemNew);
			}
		}
		conversationList = conversationListNew;
	}

	// ------------------------------------------------------------------------
	// ------------------------------------------------------------------------

	/** The min width diff. */
	private static int minWidthDiff = 100;

	/** The m last height differece. */
	private static int lastHeightDifferece;

	/** The keyboard visible. */
	private static boolean keyboardVisible = false;

	/**
	 * Checks if is keyboard visible. This is a central message that is used in
	 * {@link OnGlobalLayoutListener} in order to detect keyboard
	 * opening/closing.
	 * 
	 * @param rootView
	 *            the root view
	 * @return true, if is keyboard visible
	 */
	private boolean isKeyboardVisible(View rootView) {
		// Screen height
		Rect rect = new Rect();
		rootView.getWindowVisibleDisplayFrame(rect);
		int screenHeight = rootView.getRootView().getHeight();
		// Height difference of root view and screen heights
		int heightDifference = screenHeight - (rect.bottom - rect.top);

		// If height difference is different then the last time and if
		// is bigger than 1/4 of the screen => assume visible keyboard
		if (heightDifference != lastHeightDifferece) {
			if (heightDifference > screenHeight / 4
					&& ((heightDifference > lastHeightDifferece + minWidthDiff) || (heightDifference < lastHeightDifferece
							- minWidthDiff))) {
				// Keyboard is now visible
				// Log.d("communicator", "@@@@@@ CHANGE TO VISIBLE=TRUE");
				lastHeightDifferece = heightDifference;
				keyboardVisible = true;
			} else if (heightDifference < screenHeight / 4) {
				// Log.d("communicator", "@@@@@@ CHANGE TO VISIBLE=FALSE");
				// Keyboard is now hidden
				lastHeightDifferece = heightDifference;
				keyboardVisible = false;
			}
		}
		return keyboardVisible;
	}

	// ------------------------------------------------------------------------
	// ------------------------------------------------------------------------

	private int menuEnableChat = -1;
	private int menuDisableChat = -1;
	private int menuEnableSMS = -1;
	private int menuDisableSMS = -1;
	private int menuSndSMSSec = -1;
	private int menuSndSMS = -1;
	private int menuSndMsgSec = -1;
	private int menuSndMsg = -1;

	private void updateSendMenu(Context context) {
		boolean chatmodeOn = Utility.loadBooleanSetting(context,
				Setup.OPTION_CHATMODE, Setup.DEFAULT_CHATMODE);
		boolean smsmodeOn = Setup.isSMSModeOn(context, hostUid);
		boolean havephonenumber = Setup.havePhone(context, hostUid);
		boolean onlySMS = hostUid < 0;

		if (onlySMS) {
			// only SMS mode available
			imageSendMenuProvider.setVisible(menuSndMsg, false);
			imageSendMenuProvider.setVisible(menuSndMsgSec, false);
			imageSendMenuProvider.setVisible(menuSndSMSSec, true);
			imageSendMenuProvider.setVisible(menuSndSMS, true); // this is the
																// default

			imageSendMenuProvider.setVisible(menuDisableSMS, false);
			imageSendMenuProvider.setVisible(menuEnableSMS, false);
		} else if (!havephonenumber) {
			// no SMS mode available
			imageSendMenuProvider.setVisible(menuSndMsg, true);
			imageSendMenuProvider.setVisible(menuSndMsgSec, true); // this is
																	// the
																	// default
			imageSendMenuProvider.setVisible(menuSndSMSSec, false);
			imageSendMenuProvider.setVisible(menuSndSMS, false);

			imageSendMenuProvider.setVisible(menuDisableSMS, false);
			imageSendMenuProvider.setVisible(menuEnableSMS, true);
		} else {
			// sms mode available
			imageSendMenuProvider.setVisible(menuSndMsg, true);
			imageSendMenuProvider.setVisible(menuSndMsgSec, true); // this is
																	// the
																	// default
			imageSendMenuProvider.setVisible(menuSndSMSSec, true);
			imageSendMenuProvider.setVisible(menuSndSMS, true);
			if (smsmodeOn) {
				// sms mode on
				imageSendMenuProvider.setVisible(menuDisableSMS, true);
				imageSendMenuProvider.setVisible(menuEnableSMS, false);
			} else {
				// normal : sms mode off
				imageSendMenuProvider.setVisible(menuDisableSMS, false);
				imageSendMenuProvider.setVisible(menuEnableSMS, true);
			}
		}

		// Update chat mode
		if (chatmodeOn) {
			imageSendMenuProvider.setVisible(menuEnableChat, false);
			imageSendMenuProvider.setVisible(menuDisableChat, true);
		} else {
			imageSendMenuProvider.setVisible(menuEnableChat, true);
			imageSendMenuProvider.setVisible(menuDisableChat, false);
		}
		updateSendButtonImage(context);
	}

	// ------------------------------------------------------------------------

	/**
	 * Creates the send menu.
	 * 
	 * @param context
	 *            the context
	 */
	private ImageContextMenuProvider createSendMenu(final Activity context) {
		if (imageSendMenuProvider == null) {
			imageSendMenuProvider = new ImageContextMenuProvider(context, null,
					null);
			menuEnableChat = imageSendMenuProvider.addEntry("Enable Chat Mode",
					R.drawable.menuchaton,
					new ImageContextMenu.ImageContextMenuSelectionListener() {
						public boolean onSelection(ImageContextMenu instance) {
							skipResume = true;
							Utility.saveBooleanSetting(context,
									Setup.OPTION_CHATMODE, true);
							updateSendMenu(context);
							Utility.showToastAsync(context,
									"Chat mode enabled.");
							return true;
						}
					});
			menuDisableChat = imageSendMenuProvider.addEntry(
					"Disable Chat Mode", R.drawable.menuchatoff,
					new ImageContextMenu.ImageContextMenuSelectionListener() {
						public boolean onSelection(ImageContextMenu instance) {
							skipResume = true;
							Utility.saveBooleanSetting(context,
									Setup.OPTION_CHATMODE, false);
							updateSendMenu(context);
							Utility.showToastAsync(context,
									"Chat mode disabled.");
							return true;
						}
					});
			menuEnableSMS = imageSendMenuProvider.addEntry("Enable SMS Mode",
					R.drawable.menusmson,
					new ImageContextMenu.ImageContextMenuSelectionListener() {
						public boolean onSelection(ImageContextMenu instance) {
							skipResume = true;
							boolean smsmodeOn = Setup.isSMSModeOn(context,
									hostUid);
							boolean haveTelephoneNumber = Setup.havePhone(
									context, hostUid);
							if (!smsmodeOn && !haveTelephoneNumber) {
								int serverId = Setup.getServerId(context,
										hostUid);
								if (Setup.isSMSOptionEnabled(context, serverId)) {
									inviteOtherUserToSMSMode(context, serverId);
								} else {
									inviteUserToSMSMode(context);
								}
							} else {
								// enable
								Utility.saveBooleanSetting(context,
										Setup.OPTION_SMSMODE + hostUid, true);
								updateSendMenu(context);
								Utility.showToastAsync(
										context,
										"SMS mode for "
												+ Main.UID2Name(context,
														hostUid, false)
												+ " enabled.");
							}
							return true;
						}
					});
			menuDisableSMS = imageSendMenuProvider.addEntry("Disable SMS Mode",
					R.drawable.menusmsoff,
					new ImageContextMenu.ImageContextMenuSelectionListener() {
						public boolean onSelection(ImageContextMenu instance) {
							skipResume = true;
							// disable
							Utility.saveBooleanSetting(context,
									Setup.OPTION_SMSMODE + hostUid, false);
							updateSendMenu(context);
							Utility.showToastAsync(context, "SMS mode for "
									+ Main.UID2Name(context, hostUid, false)
									+ " disabled.");
							return true;
						}
					});
			menuSndSMS = imageSendMenuProvider.addEntry("Send Unsecure SMS",
					R.drawable.menusmsunsec,
					new ImageContextMenu.ImageContextMenuSelectionListener() {
						public boolean onSelection(ImageContextMenu instance) {
							skipResume = true;
							sendMessageOrPrompt(context, DB.TRANSPORT_SMS,
									false);
							return true;
						}
					});
			menuSndSMSSec = imageSendMenuProvider.addEntry("Send Secure SMS",
					R.drawable.menusmssec,
					new ImageContextMenu.ImageContextMenuSelectionListener() {
						public boolean onSelection(ImageContextMenu instance) {
							skipResume = true;
							if (hostUid >= 0) {
								sendMessageOrPrompt(context, DB.TRANSPORT_SMS,
										true);
							} else {
								promptInfo(
										context,
										"No Registered User",
										"In order to send secure encrypted SMS or messages, your communication partner needs to be registered.");
							}
							return true;
						}
					});
			menuSndMsg = imageSendMenuProvider.addEntry(
					"Send Unsecure Message", R.drawable.menumsgunsec,
					new ImageContextMenu.ImageContextMenuSelectionListener() {
						public boolean onSelection(ImageContextMenu instance) {
							skipResume = true;
							sendMessageOrPrompt(context, DB.TRANSPORT_INTERNET,
									false);
							return true;
						}
					});
			menuSndMsgSec = imageSendMenuProvider.addEntry(
					"Send Secure Message", R.drawable.menumsgsec,
					new ImageContextMenu.ImageContextMenuSelectionListener() {
						public boolean onSelection(ImageContextMenu instance) {
							skipResume = true;
							if (Setup.haveKey(context, hostUid)) {
								sendMessageOrPrompt(context,
										DB.TRANSPORT_INTERNET, true);
							} else {
								promptInfo(
										context,
										"No Encryption Possible",
										"In order to send secure encrypted messages or SMS, your communication partner needs to enable encryption.");

							}
							return true;
						}
					});
		}
		// Initially update
		updateSendMenu(context);
		return imageSendMenuProvider;
	}

	// ------------------------------------------------------------------------

	private int menuContextShowAll = -1;
	private int menuContextShowMore = -1;
	private int menuContextGroupInvite = -1;

	/**
	 * Creates the context menu for the conversation activity.
	 * 
	 * @param context
	 *            the context
	 */
	private ImageContextMenuProvider createContextMenu(final Activity context) {
		if (imageContextMenuProvider == null) {
			imageContextMenuProvider = new ImageContextMenuProvider(context,
					null, null);
			menuContextShowAll = imageContextMenuProvider.addEntry(
					"Show All Messages", R.drawable.menushowall,
					new ImageContextMenu.ImageContextMenuSelectionListener() {
						public boolean onSelection(ImageContextMenu instance) {
							maxScrollMessageItems = Setup.SHOW_ALL;
							rebuildConversation(context, 200);
							return true;
						}
					});
			menuContextShowMore = imageContextMenuProvider.addEntry(
					"Show More Messages", R.drawable.menushowall,
					new ImageContextMenu.ImageContextMenuSelectionListener() {
						public boolean onSelection(ImageContextMenu instance) {
							maxScrollMessageItems = Setup.SHOW_MORE;
							rebuildConversation(context, 200);
							return true;
						}
					});
			imageContextMenuProvider.addEntry("Clear Conversation",
					R.drawable.menudelete,
					new ImageContextMenu.ImageContextMenuSelectionListener() {
						public boolean onSelection(ImageContextMenu instance) {
							clearConversation(context);
							return true;
						}
					});
			imageContextMenuProvider.addEntry("Backup", R.drawable.menubackup,
					new ImageContextMenu.ImageContextMenuSelectionListener() {
						public boolean onSelection(ImageContextMenu instance) {
							backup(context);
							return true;
						}
					});
			menuContextGroupInvite = imageContextMenuProvider.addEntry(
					"Invite to Group", R.drawable.menugroup,
					new ImageContextMenu.ImageContextMenuSelectionListener() {
						public boolean onSelection(ImageContextMenu instance) {
							groupInvite(context);
							return true;
						}
					});

			imageContextMenuProvider.addEntry("New Session Key",
					R.drawable.menukey,
					new ImageContextMenu.ImageContextMenuSelectionListener() {
						public boolean onSelection(ImageContextMenu instance) {
							possiblePromptNewSession(context);
							return true;
						}
					});
			imageContextMenuProvider.addEntry("Search", R.drawable.menusearch,
					new ImageContextMenu.ImageContextMenuSelectionListener() {
						public boolean onSelection(ImageContextMenu instance) {
							promptSearch(context);
							return true;
						}
					});
			imageContextMenuProvider.addEntry("Attach Image",
					R.drawable.menuattachment,
					new ImageContextMenu.ImageContextMenuSelectionListener() {
						public boolean onSelection(ImageContextMenu instance) {
							promptImageInsert(context, hostUid);
							return true;
						}
					});
			imageContextMenuProvider.addEntry("Clear Message Input",
					R.drawable.menuclearinput,
					new ImageContextMenu.ImageContextMenuSelectionListener() {
						public boolean onSelection(ImageContextMenu instance) {
							messageText.setText("");
							return true;
						}
					});
			imageContextMenuProvider.addEntry("Refresh",
					R.drawable.menurefresh,
					new ImageContextMenu.ImageContextMenuSelectionListener() {
						public boolean onSelection(ImageContextMenu instance) {
							doRefresh(context);
							return true;
						}
					});
		}

		
		if (Setup.isGroup(context, hostUid)) {
			imageContextMenuProvider.setVisible(menuContextGroupInvite, false);
		} else {
			imageContextMenuProvider.setVisible(menuContextGroupInvite, true);
		}
		
		// Update
		if (maxScrollMessageItems == Setup.SHOW_ALL) {
			imageContextMenuProvider.setVisible(menuContextShowAll, false);
			imageContextMenuProvider.setVisible(menuContextShowMore, false);
		} else if (maxScrollMessageItems == Setup.SHOW_MORE) {
			imageContextMenuProvider.setVisible(menuContextShowMore, false);
			imageContextMenuProvider.setVisible(menuContextShowAll, true);
		} else {
			imageContextMenuProvider.setVisible(menuContextShowMore, true);
			imageContextMenuProvider.setVisible(menuContextShowAll, false);
		}
		return imageContextMenuProvider;
	}

	// ------------------------------------------------------------------------
	// ------------------------------------------------------------------------

	/**
	 * Show the message menu for one conversation item.
	 * 
	 * @param activity
	 *            the activity
	 * @param item
	 *            the item
	 */
	public void showMessageMenu(Activity activity, ConversationItem item) {
		ImageContextMenu.show(activity,
				createMessageContextMenu(activity, item));
	}

	// ------------------------------------------------------------------------

	/**
	 * The current image to be considered by choices of the context menu bitmap.
	 */
	ConversationItem imageMessageMenuItem = null;
	int imageMessageMenuRevoke = -1;
	int imageMessageMenuResend = -1;
	TextView imageMessageMenuInfoText = null;

	public ImageContextMenuProvider createMessageContextMenu(
			final Activity activity, ConversationItem item) {
		imageMessageMenuItem = item;

		if (imageMessageMenuProvider == null) {
			ImageContextMenu.ExtendedEntryViewProvider infoViewProvider = new ImageContextMenu.ExtendedEntryViewProvider() {
				public View provideView(Context context) {
					LinearLayout infoTextBox = new LinearLayout(context);
					infoTextBox.setOrientation(LinearLayout.VERTICAL);
					LinearLayout.LayoutParams lpInfoTextBox = new LinearLayout.LayoutParams(
							LinearLayout.LayoutParams.MATCH_PARENT,
							LinearLayout.LayoutParams.WRAP_CONTENT);
					lpInfoTextBox.setMargins(0, 0, 0, 0);
					infoTextBox.setLayoutParams(lpInfoTextBox);
					infoTextBox
							.setBackgroundColor(Setup.COLOR_MAIN_BLUEDARKEST);

					LinearLayout infoTextBoxInner;

					// If this is not a group message or we received it (not
					// ME()) then display normal info!
					if (!Setup.isGroup(context, hostUid)
							|| !imageMessageMenuItem.me()) {
						infoTextBoxInner = getMsgInfoLayoutDefault(context);
					} else {
						int localGroupId = hostUid;
						int localid = imageMessageMenuItem.localid;
						infoTextBoxInner = getGroupLayout(context,
								localGroupId, localid);
					}

					// The separator
					LinearLayout infoTextLine = new LinearLayout(context);
					infoTextLine.setBackgroundColor(Setup.COLOR_BLUELINE);
					LinearLayout.LayoutParams lpInfoTextLine = new LinearLayout.LayoutParams(
							LinearLayout.LayoutParams.MATCH_PARENT,
							LinearLayout.LayoutParams.WRAP_CONTENT);
					lpInfoTextLine.setMargins(0, 0, 0, 0);
					lpInfoTextLine.height = 2;
					infoTextLine.setLayoutParams(lpInfoTextLine);

					infoTextBox.addView(infoTextBoxInner);
					infoTextBox.addView(infoTextLine);

					return infoTextBox;
				}
			};

			imageMessageMenuProvider = new ImageContextMenuProvider(activity,
					"Message N/A", activity.getResources().getDrawable(
							R.drawable.message));

			imageMessageMenuProvider.addEntry(infoViewProvider,
					new ImageContextMenu.ImageContextMenuSelectionListener() {
						public boolean onSelection(ImageContextMenu instance) {
							// Details
							promptMessageDetails(activity, imageMessageMenuItem);
							return true;
						}
					});

			imageMessageMenuProvider.addEntry("Copy", R.drawable.menucopy,
					new ImageContextMenu.ImageContextMenuSelectionListener() {
						public boolean onSelection(ImageContextMenu instance) {
							// COPY
							Utility.copyToClipboard(activity,
									imageMessageMenuItem.text);
							Utility.showToastAsync(activity,
									"Message copied to Clipboard.");
							return true;
						}
					});
			imageMessageMenuProvider.addEntry("Respond",
					R.drawable.menurespond,
					new ImageContextMenu.ImageContextMenuSelectionListener() {
						public boolean onSelection(ImageContextMenu instance) {
							// Respond
							String respondText = "''"
									+ imageMessageMenuItem.text + "'' -  ";
							Conversation.getInstance().messageText
									.setText(respondText);
							Conversation.getInstance().messageText
									.setSelection(respondText.length() - 1,
											respondText.length() - 1);
							Conversation.getInstance().messageText.postDelayed(
									new Runnable() {
										public void run() {
											Conversation.getInstance().messageText
													.requestFocus();
											Utility.showKeyboardExplicit(Conversation
													.getInstance().messageText);
										}
									}, 400); // 400ms important because after
												// 200ms the
												// resume() will hid the
												// keyboard
							return true;
						}
					});
			imageMessageMenuResend = imageMessageMenuProvider.addEntry(
					"Resend", R.drawable.menurefresh,
					new ImageContextMenu.ImageContextMenuSelectionListener() {
						public boolean onSelection(ImageContextMenu instance) {
							// Resend
							promptResend(activity, imageMessageMenuItem);
							return true;
						}
					});
			imageMessageMenuRevoke = imageMessageMenuProvider.addEntry(
					"Revoke", R.drawable.menurevoke,
					new ImageContextMenu.ImageContextMenuSelectionListener() {
						public boolean onSelection(ImageContextMenu instance) {
							// Revoke
							promptRevoke(activity, imageMessageMenuItem.mid,
									imageMessageMenuItem.localid,
									imageMessageMenuItem.to, activity);
							return true;
						}
					});
			imageMessageMenuProvider.addEntry("Details", R.drawable.menuinfo,
					new ImageContextMenu.ImageContextMenuSelectionListener() {
						public boolean onSelection(ImageContextMenu instance) {
							// Details
							promptMessageDetails(activity, imageMessageMenuItem);
							return true;
						}
					});
		}

		// Enable revoke only for Internet messages
		imageMessageMenuProvider.setVisible(imageMessageMenuRevoke,
				item.transport != DB.TRANSPORT_SMS && item.me());
		imageMessageMenuProvider.setVisible(imageMessageMenuResend, item.me());
		imageMessageMenuProvider.setTitle("Message  [ "
				+ imageMessageMenuItem.localid + " ]");

		return imageMessageMenuProvider;
	}

	// ------------------------------------------------------------------------

	/**
	 * Gets the msg info layout default for the msg context menu info field for
	 * a non group message.
	 * 
	 * @param context
	 *            the context
	 * @return the msg info layout default
	 */
	private LinearLayout getMsgInfoLayoutDefault(Context context) {
		// infoTextBox.setBackgroundColor(Color.YELLOW);

		LinearLayout infoTextBoxInner = new LinearLayout(context);
		LinearLayout.LayoutParams lpInfoTextBoxInner = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		infoTextBoxInner.setLayoutParams(lpInfoTextBoxInner);
		infoTextBoxInner.setGravity(Gravity.CENTER_HORIZONTAL);
		// infoTextBoxInner.setBackgroundColor(Color.CYAN);

		imageMessageMenuInfoText = new TextView(context);
		LinearLayout.LayoutParams lpInfoText = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		lpInfoText.setMargins(25, 8, 25, 8);
		imageMessageMenuInfoText.setLayoutParams(lpInfoText);
		imageMessageMenuInfoText.setTextColor(Color.WHITE);
		imageMessageMenuInfoText.setTextSize(14);
		// imageMessageMenuInfoText.setBackgroundColor(Color.GREEN);

		String infoText = "";

		// Get updated information about this conversation item
		final ConversationItem updatedItem = DB.getMessage(context,
				imageMessageMenuItem.localid, hostUid, DB.DEFAULT_MESSAGEPART);
		if (updatedItem != null) {
			infoText += "        Sent:  "
					+ DB.getDateString(updatedItem.sent, true) + "\n";
			if (!updatedItem.me()) {
				infoText += "Received:  ";
			} else {
				infoText += "Delivered:  ";
			}
			infoText += DB.getDateString(updatedItem.received, true);
			if (imageMessageMenuItem.transport == DB.TRANSPORT_INTERNET) {
				infoText += "\n       Read:  "
						+ DB.getDateString(updatedItem.read, true);
			}
		}
		imageMessageMenuInfoText.setText(infoText);

		infoTextBoxInner.addView(imageMessageMenuInfoText);
		return infoTextBoxInner;
	}

	// ------------------------------------------------------------------------

	private Spinner groupspinner;

	private void groupInvite(final Context context) {
		final String name = Main.UID2Name(context, hostUid, false);
		String title = "Invite " + name;
		String text = "Please select the group to invite " + name + " to:";
		final int serverId = Setup.getServerId(context, hostUid);

		if (hostUid < 0) {
			promptInfo(
					context,
					"Not Registered",
					name
							+ " is not a registered user.\n\nYou cannot invite external users to any groups.");
			return;
		}

		if (serverId == -1
				|| Setup.getGroupsList(context, serverId).size() == 0) {
			promptInfo(
					context,
					"No Groups",
					"You are not a member of any group on server '"
							+ Setup.getServerLabel(context, serverId, true)
							+ "'."
							+ "\n\nIn order to invite somebody yourself must already be part of a group!");
			return;
		}

		final boolean encrypted = Setup.encryptedSentPossible(context, hostUid);
		if (!encrypted) {
			promptInfo(
					context,
					"No Encryption",
					name
							+ " has not enabled encryption or you have not enabled encryption.\n\nEncryption is required"
							+ " for inviting people! Make sure you and your communication partner have turned on encryption.");
			return;
		}

		new MessageAlertDialog(context, title, text, null, " Invite ",
				" Cancel ", new MessageAlertDialog.OnSelectionListener() {
					public void selected(int button, boolean cancel) {
						if (button == MessageAlertDialog.BUTTONOK1 && !cancel) {

							if (groupspinner != null) {
								int index = groupspinner
										.getSelectedItemPosition();
								if (index > -1) {
									String groupId = Setup.groupSpinnerMappingGroupId2
											.get(index);
									String serverName = Setup.getServerLabel(
											context, serverId, true);
									String groupName = Setup.getGroupName(
											context, serverId, groupId);
									String groupSecret = Setup.getGroupSecret(
											context, serverId, groupId);

									// Test if this user is not already a member
									// of the group
									List<Integer> members = Setup
											.getGroupMembersList(context,
													serverId, groupId);

									Log.d("communicator",
											"GROUPS INVITE "
													+ Setup.getGroupMembers(
															context, serverId,
															groupId));
									int suid = Setup.getSUid(context, hostUid);

									if (members.contains(suid)) {
										promptInfo(
												context,
												"Already a Member",
												name
														+ " is already a member of this group.");
										return;
									}

									Setup.groupInvite(context, serverId,
											groupId, suid);

									int myUid = DB.myUid(null, serverId);
									String myName = Main.UID2Name(context,
											myUid, true);
									String otherName = Main.UID2Name(context,
											hostUid, true);
									String text = GROUPINVITATION_PREFIX
											+ groupSecret + " ]\n\n" + myName
											+ " has invited " + otherName
											+ " to the group '" + groupName
											+ "' on server " + serverName + ".";
									if (DB.addSendMessage(context, hostUid,
											text, encrypted,
											DB.TRANSPORT_INTERNET, false,
											DB.PRIORITY_MESSAGE)) {
										updateConversationlist(context);
										Communicator.sendNewNextMessageAsync(
												context, DB.TRANSPORT_INTERNET);
									}
								}
							}
						}
					}
				}, new MessageAlertDialog.OnInnerViewProvider() {

					public View provide(final MessageAlertDialog dialog) {
						groupspinner = new Spinner(context);
						LinearLayout.LayoutParams lpSpinner = new LinearLayout.LayoutParams(
								LinearLayout.LayoutParams.WRAP_CONTENT,
								LinearLayout.LayoutParams.WRAP_CONTENT);
						lpSpinner.setMargins(5, 0, 5, 40);
						groupspinner.setLayoutParams(lpSpinner);
						Setup.updateGroupSpinner2(context, serverId,
								groupspinner);
						return groupspinner;
					}
				}).show();
	}

	// ------------------------------------------------------------------------

	/**
	 * Prompt before resending the message.
	 * 
	 * @param context
	 *            the context
	 * @param conversationItem
	 *            the conversation item
	 */
	public static void promptResend(final Context context,
			final ConversationItem conversationItem) {

		String messageTextString = conversationItem.text;
		int uid = conversationItem.to;

		// now check if SMS and encryption is available
		int serverId = Setup.getServerId(context, uid);
		boolean mySMSAvailable = Setup.isSMSOptionEnabled(context, serverId);
		boolean otherSMSAvailable = Setup.havePhone(context, uid);
		boolean sms = mySMSAvailable && otherSMSAvailable;
		boolean encryption = Setup.isEncryptionAvailable(context, uid);

		final String name = Main.UID2Name(context, uid, false);

		ConversationCompose.sendMessagePrompt(context, sms, encryption, uid,
				name, messageTextString, new OnSendListener() {
					public void onSend(boolean success, boolean encrypted,
							int transport) {
						if (success) {
							String messageOrSMS1 = "Message";
							if (conversationItem.transport != DB.TRANSPORT_INTERNET) {
								messageOrSMS1 = "SMS";
							}
							String messageOrSMS2 = "message";
							if (transport != DB.TRANSPORT_INTERNET) {
								messageOrSMS2 = "SMS";
							}
							String encryptedString = "";
							if (encrypted) {
								encryptedString = "encrypted ";
							}
							Utility.showToastAsync(context, messageOrSMS1 + " "
									+ conversationItem.localid + " resent as "
									+ encryptedString + messageOrSMS2 + " to "
									+ name + ".");

							final Handler mUIHandler = new Handler(Looper
									.getMainLooper());
							mUIHandler.postDelayed(new Thread() {
								@Override
								public void run() {
									super.run();
									Conversation
											.updateConversationlistAsync(context);
								}
							}, 2000);
						} else {
							String messageOrSMS1 = "Message";
							if (conversationItem.transport != DB.TRANSPORT_INTERNET) {
								messageOrSMS1 = "SMS";
							}
							Utility.showToastAsync(context, messageOrSMS1 + " "
									+ conversationItem.localid + " "
									+ "could not be resent.");
						}

					}
				});

		// String titleMessage = "Resend Message ";
		// String textMessage =
		// "Do you really want to resend this message another time?";
		// new MessageAlertDialog(context, titleMessage, textMessage,
		// " Resend ",
		// " Cancel ", null, new MessageAlertDialog.OnSelectionListener() {
		// public void selected(int button, boolean cancel) {
		// if (!cancel) {
		// if (button == 0) {
		// // now really resend
		// resendMessage(context, conversationItem);
		// }
		// }
		// }
		// }).show();
	}

	// ------------------------------------------------------------------------

	// /**
	// * Resend failed SMS or decryption failed message.
	// *
	// * @param conversationItem
	// * the conversation item
	// * @return true, if successful
	// */
	// public static void resendMessage(final Context context,
	// ConversationItem conversationItem) {
	// final String messageTextString = conversationItem.text;
	// final boolean encrypted = conversationItem.encrypted;
	// final int transport = conversationItem.transport;
	//
	// // Do the following async, so that we already have scrolled back
	// // to the original position after closing the
	// // message details window before we will re-send this
	// // message.
	//
	// final Handler mUIHandler = new Handler(Looper.getMainLooper());
	// mUIHandler.postDelayed(new Thread() {
	// @Override
	// public void run() {
	// super.run();
	// if (DB.addSendMessage(context, hostUid, messageTextString,
	// encrypted, transport, false, DB.PRIORITY_MESSAGE)) {
	// Conversation.updateConversationlistAsync(context);
	// Communicator.sendNewNextMessageAsync(context, transport);
	// }
	// }
	// }, 2000);
	//
	// }

	// ------------------------------------------------------------------------

	/**
	 * Ask the user if he really wants to revoke the message.
	 * 
	 * @param context
	 *            the context
	 * @param mid
	 *            the mid
	 * @param localid
	 *            the localid
	 * @param toHostUid
	 *            the to host uid
	 */
	public void promptRevoke(final Context context, final int mid,
			final int localid, final int toHostUid, final Activity activity) {
		String titleMessage = "Revoke Message " + localid;
		String textMessage = "Attention! Revoking a message should be used with"
				+ " precaution!\n\nA revoked message is deleted from server. There"
				+ " is no guarantee that it is deleted from other devices if it "
				+ "already has been delivered. Still, all devices that connect to the "
				+ "server are advised to"
				+ " delete the message. Anyhow, this message may already have been "
				+ "read by the recipient! Furthermore, revoking will cancel and may corrupt new "
				+ "message notifications of the recipient! Therefore, you should proceed ONLY "
				+ "if there is no alternative!\n\nDo you really want to revoke"
				+ " the message?";
		new MessageAlertDialog(context, titleMessage, textMessage, " Revoke ",
				" Cancel ", null, new MessageAlertDialog.OnSelectionListener() {
					public void selected(int button, boolean cancel) {
						if (!cancel) {
							if (button == 0) {
								// now really try to revoke
								DB.tryToRevokeMessage(context, mid, localid,
										DB.getTimestampString(), toHostUid);
							}
						}
					}
				}).show();
	}

	// ------------------------------------------------------------------------
	// ------------------------------------------------------------------------

	/**
	 * The current image to be considered by choices of the context menu bitmap.
	 */
	Bitmap imageImageMenuBitmap = null;
	String imageImageMenuString = null;
	String imageImageMenuTitleAddition = null;
	String imageImageMenuDescription = null;
	int imageImageMenuImageIndex = -1;

	public ImageContextMenuProvider createImageContextMenu(
			final Activity context, Bitmap bitmap, String encodedImg,
			final String titleAddition, final String description, int imageIndex) {
		imageImageMenuBitmap = bitmap;
		imageImageMenuString = encodedImg;
		imageImageMenuTitleAddition = titleAddition;
		imageImageMenuDescription = description;
		imageImageMenuImageIndex = imageIndex;

		if (imageImageMenuProvider == null) {
			imageImageMenuProvider = new ImageContextMenuProvider(context,
					"Image Options", context.getResources().getDrawable(
							R.drawable.pictureimport));
			imageImageMenuProvider.addEntry("View Fullscreen",
					R.drawable.menushowall,
					new ImageContextMenu.ImageContextMenuSelectionListener() {
						public boolean onSelection(ImageContextMenu instance) {
							ImageFullscreenActivity.showFullscreenImage(
									context, imageImageMenuBitmap,
									imageImageMenuImageIndex);
							return true;
						}
					});
			imageImageMenuProvider.addEntry("Copy", R.drawable.menucopy,
					new ImageContextMenu.ImageContextMenuSelectionListener() {
						public boolean onSelection(ImageContextMenu instance) {
							Utility.copyToClipboard(context, "[img "
									+ imageImageMenuString + "]");
							Utility.showToastAsync(context,
									"Image copied to Clipboard.");
							return true;
						}
					});
			imageImageMenuProvider.addEntry("Share", R.drawable.menushare,
					new ImageContextMenu.ImageContextMenuSelectionListener() {
						public boolean onSelection(ImageContextMenu instance) {
							shareImage(context, imageImageMenuString);
							return true;
						}
					});
			imageImageMenuProvider.addEntry("Save", R.drawable.menubackup,
					new ImageContextMenu.ImageContextMenuSelectionListener() {
						public boolean onSelection(ImageContextMenu instance) {
							// SAVE
							String titleAddition = "";
							String description = "CryptSecure Image";
							saveImageInGallery(context, imageImageMenuString,
									false, titleAddition, description);
							return true;
						}
					});
		}
		return imageImageMenuProvider;
	}

	// ------------------------------------------------------------------------
	// ------------------------------------------------------------------------

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// This is necessary to enable a context menu
		// getMenuInflater().inflate(R.menu.activity_main, menu);
		ImageContextMenu.show(instance, createContextMenu(instance));
		return false;
	}

	// ------------------------------------------------------------------------

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onKeyDown(int, android.view.KeyEvent)
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// SEARCH KEY OVERRIDE
		if (keyCode == KeyEvent.KEYCODE_SEARCH) {
			promptSearch(this);
			return true;
		}
		return super.onKeyDown(keyCode, event);

	}

	// ------------------------------------------------------------------------

	/**
	 * Go back to the main activity.
	 * 
	 * @param context
	 *            the context
	 */
	public void goBack(Context context) {
		// GET TO THE MAIN SCREEN IF THIS ICON IS CLICKED !
		finish();
		System.gc();
		// Intent intent = new Intent(this, Main.class);
		// intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		// startActivity(intent);
	}

	// -------------------------------------------------------------------------

	/**
	 * Do refresh and try to send or receive message.
	 * 
	 * @param context
	 *            the context
	 */
	public void doRefresh(final Context context) {
		Communicator.sendNextMessage(this);
		int serverId = Setup.getServerId(context, hostUid);
		Communicator.haveNewMessagesAndReceive(this, serverId);
		Utility.showToastShortAsync(this, "Refreshing...");
		System.gc();
	}

	// ------------------------------------------------------------------------

	/**
	 * Rebuild the conversation after some delay.
	 * 
	 * @param context
	 *            the context
	 * @param delay
	 *            the delay
	 */
	public void rebuildConversation(final Context context, final int delay) {
		final Handler mUIHandler = new Handler(Looper.getMainLooper());
		mUIHandler.postDelayed(new Thread() {
			@Override
			public void run() {
				super.run();
				rebuildConversationlist(context);
				onStart();
				onResume();
			}
		}, delay);
	}

	// ------------------------------------------------------------------------

	/**
	 * Prompt to search the conversation. Note that this might not be standard
	 * behavior but still it is a very simple behavior. A search starts at the
	 * current position (conversation item visible at the top of the screen).
	 * The user can select to search UP or DOWN starting from this item. The
	 * text box allows to enter the search text the user searches for. If there
	 * is no text entered yet the keyboard pops up automatically. Of course this
	 * is annoying if the user wants to search for several occurrences.
	 * Therefore the search text is saved and filled out the next time the user
	 * clicks on search. In this case, when there was a previous search and the
	 * search text contains something, the keyboard is hidden by default and the
	 * user must click into the text field in order to change the text field.
	 * The user may also click on clean which will erase the search text and pop
	 * up the keyboard immediately. We believe this is a very clean an
	 * responsive search dialog behavior. If something is found then the
	 * conversation scrolls to this conversation item and tries to highlight the
	 * whole text. The next search result must be another conversation item (UP
	 * or DOWN). If there is no other (UP or DOWN) then the user will get a
	 * toast informing about the end of search.
	 * 
	 * @param context
	 *            the context
	 */
	@SuppressLint("DefaultLocale")
	public void promptSearch(final Context context) {

		String lastSearch = Utility.loadStringSetting(context,
				"lastconversationsearch", "");
		final int lastFound = Utility.loadIntSetting(context,
				"lastconversationsearchfound", -1);

		final boolean showKeyboard = !(lastSearch != null && lastSearch
				.length() > 0);

		String title = "Enter some text to search for:";
		new MessageInputDialog(context, title, null, " Up ", "Down", "Clear",
				lastSearch, new MessageInputDialog.OnSelectionListener() {
					public void selected(MessageInputDialog dialog, int button,
							boolean cancel, String searchString) {
						if (button == MessageInputDialog.BUTTONCANCEL) {
							// Cancel search (remove search text)
							Utility.saveStringSetting(context,
									"lastconversationsearch", "");
							dialog.setText("", true);
						} else if ((button == MessageInputDialog.BUTTONOK0)
								|| (button == MessageInputDialog.BUTTONOK1)) {
							dialog.dismiss();
							Utility.saveStringSetting(context,
									"lastconversationsearch", searchString);

							// Try to unmark (remove previous highlighting)
							{
								if (lastFound != -1) {
									Mapping mappingItem = mapping
											.get(lastFound);
									if (mappingItem != null) {
										EditText editText = mappingItem.text;
										if (editText != null) {
											editText.setSelection(0, 0);
										}
									}
								}
							}

							boolean reverse = (button == MessageInputDialog.BUTTONOK0);
							int max = conversationList.size() - 1;
							int start = scrollItem + 1;
							int end = max;
							if (start > end) {
								start = end;
							}
							int incr = 1;
							if (reverse) {
								start = scrollItem;
								end = 0;
								if (start < end) {
									start = end;
								}
								incr = -1;
							}
							if (scrollItem > -1) {
								int foundItem = -1;
								int lmid = -1;
								if (start > max) {
									start = max;
								}
								if (end > max) {
									end = max;
								}
								for (int c = start; c != end; c = c + incr) {
									if (conversationList.get(c).text
											.toLowerCase().contains(
													searchString.toLowerCase())) {
										foundItem = c;
										// Try to mark / highlight
										ConversationItem item = conversationList
												.get(c);
										lmid = -1 * item.localid;
										if (lastFound != lmid) {
											// Otherwise search on...
											Mapping mappingItem = mapping
													.get(lmid);
											if (mappingItem != null) {
												EditText editText = mappingItem.text;
												if (editText != null) {
													editText.selectAll();
												}
											}
											Utility.saveIntSetting(
													context,
													"lastconversationsearchfound",
													lmid);
											break;
										} else {
											foundItem = -1; // We did not find
															// anything NEW
															// here...!
										}
									}
								}
								if (foundItem != -1) {
									// Disable autoscrolling!
									Conversation.scrolledDown = false;
									Conversation.scrolledUp = false;
									fastScrollView.scrollToItem(foundItem);
									Utility.showToastInUIThread(
											context,
											"'"
													+ searchString
													+ "' found in message "
													+ (-1 * lmid + "").replace(
															"-", "*") + ".");
								} else {
									String updown = "(down)";
									if (reverse) {
										updown = "(up)";
									}
									Utility.showToastInUIThread(context, "'"
											+ searchString + "' not found "
											+ updown + ".");
									// Clear so that we find the last item the
									// next time again!
									Utility.saveIntSetting(context,
											"lastconversationsearchfound", -1);
								}
							}
						}
					}
				}, InputType.TYPE_CLASS_TEXT, true, showKeyboard).show();
	}

	// ------------------------------------------------------------------------

	/**
	 * Prompt info. This is a shortcut for bringing some information to the
	 * user's attention. It is mostly used in this activity but also in others.
	 * Therefore it is a static method.
	 * 
	 * @param context
	 *            the context
	 * @param title
	 *            the title
	 * @param text
	 *            the text
	 */
	public static void promptInfo(Context context, String title, String text) {
		new MessageAlertDialog(context, title, text, " Ok ", null, null,
				new MessageAlertDialog.OnSelectionListener() {
					public void selected(int button, boolean cancel) {
					}
				}).show();
	}

	// ------------------------------------------------------------------------

	/**
	 * Prompt the backup activity to allow the user to backup its conversation.
	 * 
	 * @param context
	 *            the context
	 */
	public void backup(Context context) {
		Intent dialogIntent = new Intent(context, BackupActivity.class);
		dialogIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		BackupActivity.hostUid = hostUid;
		context.startActivity(dialogIntent);
	}

	// ------------------------------------------------------------------------

	/**
	 * Clear conversation.
	 * 
	 * @param context
	 *            the context
	 */
	public void clearConversation(final Context context) {
		new MessageAlertDialog(
				context,
				"Clear Conversation",
				"Clearing the conversation means deleting ALL messages between you and this user from your local device. Keep in mind that you might only see"
						+ " the last "
						+ Setup.MAX_SHOW_CONVERSATION_MESSAGES
						+ " messages.\n\n"
						+ "Alternative: If you long click beside any message you can more selectively clear only parts of the conversation."
						+ "\n\nDo you really want to clear the ALL messages from the conversation with "
						+ Main.UID2Name(context, hostUid, false) + " ?",
				"Clear All", null, "Abort",
				new MessageAlertDialog.OnSelectionListener() {
					public void selected(int button, boolean cancel) {
						if (!cancel && button == 0) {
							// Clear conversation
							DB.deleteUser(context, hostUid);
							rebuildConversationlist(context);
							// Update the first line
							Utility.saveStringSetting(context,
									Setup.SETTINGS_USERLISTLASTMESSAGE
											+ hostUid, null);
							Utility.saveLongSetting(context,
									Setup.SETTINGS_USERLISTLASTMESSAGETIMESTAMP
											+ hostUid, -1);
						}
					}
				}).show();
	}

	// ------------------------------------------------------------------------

	/**
	 * Update conversation title async from non UI thread.
	 * 
	 * @param context
	 *            the context
	 */
	public void updateConversationTitleAsync(final Context context) {
		updateConversationTitleAsync(context, null);
	}

	// -------------------------------------------------------------------------

	/**
	 * Update conversation title async from non UI thread.
	 * 
	 * @param context
	 *            the context
	 * @param titleText
	 *            the title text
	 */
	public void updateConversationTitleAsync(final Context context,
			final String titleText) {
		final Handler mUIHandler = new Handler(Looper.getMainLooper());
		mUIHandler.post(new Thread() {
			@Override
			public void run() {
				super.run();
				updateConversationTitle(context, titleText);
			}
		});
	}

	// -------------------------------------------------------------------------

	/**
	 * Update conversation title.
	 * 
	 * @param context
	 *            the context
	 */
	public void updateConversationTitle(Context context) {
		updateConversationTitle(context, null);
	}

	// -------------------------------------------------------------------------

	/**
	 * Update conversation title.
	 * 
	 * @param context
	 *            the context
	 * @param titleText
	 *            the title text
	 */
	public void updateConversationTitle(Context context, String titleText) {
		if (titleText == null || titleText.length() == 0) {
			String keyHash = " - " + Setup.getAESKeyHash(context, hostUid);
			if (Setup.isGroup(context, hostUid)) {
				keyHash = "";
			}
			Conversation.getInstance().setTitle(
					Main.UID2Name(context, hostUid, false) + keyHash);
		} else {
			Conversation.getInstance().setTitle(titleText);
		}
	}

	// -------------------------------------------------------------------------

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onActivityResult(int, int,
	 * android.content.Intent)
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// To handle when an image is selected from the browser, add the
		// following
		// to your Activity
		if (resultCode == RESULT_OK) {
			if (requestCode == Utility.SELECT_PICTURE) {
				boolean ok = false;
				try {
					insertImage(this, data.getData());
					ok = true;
				} catch (Exception e) {
					e.printStackTrace();
				}
				if (!ok) {
					Utility.showToastInUIThread(this,
							"Selected file is not a valid image.");
				}
			}
			if (requestCode == Utility.TAKE_PHOTO) {
				final boolean keyboardWasVisible = keyboardVisible;
				final boolean wasScrolledDown = scrolledDown;
				final Context context = this;
				PictureImportActivity.hostUid = hostUid;
				PictureImportActivity
						.setOnPictureImportListener(new PictureImportActivity.OnPictureImportListener() {
							public void onImport(String encodedImage) {
								lastKeyStroke = DB.getTimestamp()
										- Setup.TYPING_TIMEOUT_BEFORE_UI_ACTIVITY
										- 1;
								Utility.smartPaste(messageText, encodedImage,
										" ", " ", false, false, true);
								if (wasScrolledDown) {
									scrollDownNow(context, keyboardWasVisible);
									scrollDownSoon(context, keyboardWasVisible,
											2000);
								}
							}

							public void onCancel() {
								if (wasScrolledDown) {
									scrollDownNow(context, keyboardWasVisible);
									scrollDownSoon(context, keyboardWasVisible,
											2000);
								}
							}
						});

				Bitmap bitmap = (Bitmap) data.getExtras().get("data");
				Intent dialogIntent = new Intent(this,
						PictureImportActivity.class);
				dialogIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				PictureImportActivity.attachmentBitmap = bitmap;
				this.startActivity(dialogIntent);
			}
		}
	}

	// -------------------------------------------------------------------------

	/**
	 * Save image in gallery. The titleAddition is added to the the title and
	 * can contain additional information, e.g., the messageid.
	 * 
	 * @param context
	 *            the context
	 * @param encodedImg
	 *            the encoded img
	 * @param silent
	 *            the silent
	 * @param titleAddition
	 *            the title addition
	 * @return true, if successful
	 */
	public static boolean saveImageInGallery(final Context context,
			String encodedImg, boolean silent, String titleAddition,
			String description) {
		try {
			// Save image in gallery
			Bitmap bitmap = Utility.loadImageFromBASE64String(context,
					encodedImg);
			String bitmapPath = Utility.insertImage(
					context.getContentResolver(), bitmap, "CryptSecure"
							+ titleAddition, description);
			if (bitmapPath != null) {
				Utility.updateMediaScanner(context, bitmapPath);
				if (!silent) {
					Utility.showToastAsync(context, "Image saved to "
							+ bitmapPath);
				}
				return true;
			} else {
				if (!silent) {
					Utility.showToastAsync(context,
							"Error saving image to gallery. (2)");
				}
			}
		} catch (Exception e) {
			if (!silent) {
				Utility.showToastAsync(context,
						"Error saving image to gallery. (1)");
			}
		}
		return false;
	}

	// -------------------------------------------------------------------------

	/**
	 * Share image.
	 * 
	 * @param context
	 *            the context
	 * @param encodedImg
	 *            the encoded img
	 */
	public static void shareImage(final Context context, String encodedImg) {
		// Share image to other apps
		try {
			Bitmap bitmap = Utility.loadImageFromBASE64String(context,
					encodedImg);

			String bitmapPath = Images.Media.insertImage(
					context.getContentResolver(), bitmap, "CryptSecure Images",
					null);
			Uri bitmapUri = Uri.parse(bitmapPath);

			Intent sendIntent = new Intent(Intent.ACTION_SEND);
			sendIntent.setType("image/*");
			sendIntent.putExtra(Intent.EXTRA_SUBJECT, "CryptSecure Image");
			sendIntent.putExtra(Intent.EXTRA_STREAM, bitmapUri);
			sendIntent.putExtra(Intent.EXTRA_TEXT, "CryptSecure Image");
			context.startActivity(Intent.createChooser(sendIntent, "Share"));
		} catch (Exception e) {
			Utility.showToastAsync(context, "Error sharing image.");
		}
	}

	// -------------------------------------------------------------------------

	/**
	 * Prompt the user to save the image (currently in the clipboard) to the
	 * gallery or share it to other apps.
	 * 
	 * @param context
	 *            the context
	 */
	public static void promptImageSaveAs(final Context context,
			final String imageTitleAddition, final String imageDescription) {

		// promptImageSaveAs(context, "",
		// "Copied from message input text field.");
		//
		// promptImageSaveAs(
		// context,
		// buildImageTitleAddition(context,
		// conversationItem),
		// buildImageDescription(context, conversationItem));

		final String copiedText = Utility.pasteFromClipboard(context);
		int imgStart = copiedText.indexOf("[img ");
		if (imgStart == -1) {
			// no image copied or more than an image... do not ask
			return;
		}
		int imgEnd = copiedText.indexOf("]", imgStart);
		if (imgEnd == -1) {
			// no image copied or more than an image... do not ask
			return;
		}
		final String encodedImg = copiedText.substring(imgStart, imgEnd);

		String title = "Save Image?";
		String text = "You copied an image into the Clipboard. Do you want to save or share it?";

		new MessageAlertDialog(context, title, text, " Save ", " Share ",
				" Cancel ", new MessageAlertDialog.OnSelectionListener() {
					public void selected(int button, boolean cancel) {
						if (button == MessageAlertDialog.BUTTONOK0) {
							saveImageInGallery(context, encodedImg, false,
									imageTitleAddition, imageDescription);
						}
						if (button == MessageAlertDialog.BUTTONOK1) {
							shareImage(context, encodedImg);
						}
					}
				}, null).show();
	}

	// -------------------------------------------------------------------------

	/**
	 * Reprocess possible images in text and display them. This is necessary
	 * after adding such images to the text, e.g. by past or a select action.
	 * This is achieved by setting the text of the editText which re-triggers
	 * the image processing. The cursor position is saved before.
	 * 
	 * @param editText
	 *            the edit text
	 */
	public static void reprocessPossibleImagesInText(EditText editText) {
		int selection = editText.getSelectionStart();
		String messageTextBackup = editText.getText().toString();
		editText.setText(messageTextBackup);
		if (selection > -1) {
			editText.setSelection(selection);
		}
	}

	// -------------------------------------------------------------------------

	/**
	 * Prompt the image insert dialog
	 * 
	 * @param context
	 *            the context
	 */
	public void insertImage(final Context context, Uri attachmentPath) {
		final boolean keyboardWasVisible = keyboardVisible;
		final boolean wasScrolledDown = scrolledDown;
		PictureImportActivity
				.setOnPictureImportListener(new PictureImportActivity.OnPictureImportListener() {
					public void onImport(String encodedImage) {
						Utility.smartPaste(messageText, encodedImage, " ", " ",
								false, false, true);
						if (wasScrolledDown) {
							scrollDownNow(context, keyboardWasVisible);
							scrollDownSoon(context, keyboardWasVisible, 2000);
						}
					}

					public void onCancel() {
						if (wasScrolledDown) {
							scrollDownNow(context, keyboardWasVisible);
							scrollDownSoon(context, keyboardWasVisible, 2000);
						}
					}
				});
		PictureImportActivity.hostUid = hostUid;
		Intent dialogIntent = new Intent(context, PictureImportActivity.class);
		dialogIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		Bitmap bitmap = Utility.getBitmapFromContentUri(this, attachmentPath);

		if (bitmap != null) {
			PictureImportActivity.attachmentBitmap = bitmap;
			context.startActivity(dialogIntent);
		} else {
			Utility.showToastAsync(context, "Image error.");
		}
	}

	// ------------------------------------------------------------------------

	/**
	 * Prompt the image insert dialog
	 * 
	 * @deprecated User insertImage() with Uri instead!
	 * @param context
	 *            the context
	 */
	public void insertImage(final Context context, String attachmentPath) {
		final boolean keyboardWasVisible = keyboardVisible;
		final boolean wasScrolledDown = scrolledDown;
		PictureImportActivity
				.setOnPictureImportListener(new PictureImportActivity.OnPictureImportListener() {
					public void onImport(String encodedImage) {
						Utility.smartPaste(messageText, encodedImage, " ", " ",
								false, false, true);
						if (wasScrolledDown) {
							scrollDownNow(context, keyboardWasVisible);
							scrollDownSoon(context, keyboardWasVisible, 2000);
						}
					}

					public void onCancel() {
						if (wasScrolledDown) {
							scrollDownNow(context, keyboardWasVisible);
							scrollDownSoon(context, keyboardWasVisible, 2000);
						}
					}
				});
		PictureImportActivity.hostUid = hostUid;
		Intent dialogIntent = new Intent(context, PictureImportActivity.class);
		dialogIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		byte[] bytes = Utility.getFile(attachmentPath);
		Bitmap bitmap = Utility.getBitmapFromBytes(bytes);

		if (bitmap != null) {
			PictureImportActivity.attachmentBitmap = bitmap;
			context.startActivity(dialogIntent);
		} else {
			Utility.showToastAsync(context, "Image error.");
		}
	}

	// ------------------------------------------------------------------------

	/**
	 * Prompt the user to import a picture from gallery or take a fresh photo.
	 * 
	 * @param context
	 *            the context
	 */
	public static void promptImageInsert(final Activity activity, int hostUid) {
		if (hostUid < 0) {
			// SMS users cannot receive images!
			promptInfo(
					activity,
					"No Registered User",
					"In order to send attachment images, your communication partner needs to be registered.");
			return;
		}
		int serverId = Setup.getServerId(activity, hostUid);
		if (!Setup.isAttachmentsAllowedByServer(activity, serverId)) {
			String title = "Attachments Not Allowed";
			String text = "Attachments are not allowed by the server and will be removed for Internet messages.\n"
					+ "You may still send them via SMS but be advised not to send too "
					+ "large images via SMS.\n\nDo you still want to add an attachment?";

			new MessageAlertDialog(activity, title, text, " Yes ", " No ",
					" Cancel ", new MessageAlertDialog.OnSelectionListener() {
						public void selected(int button, boolean cancel) {
							if (button == MessageAlertDialog.BUTTONOK0) {
								promptImageInsert2(activity);
							}
						}
					}, null).show();

		} else {
			promptImageInsert2(activity);
		}

	}

	// ------------------------------------------------------------------------

	/**
	 * Prompt the user to import a picture from gallery or take a fresh photo.
	 * 
	 * @param context
	 *            the context
	 */
	public static void promptImageInsert2(final Activity activity) {
		String title = "Insert Image";
		String text = "Do you want to import an image from the gallery or take a new photo?";

		new MessageAlertDialog(activity, title, text, null, null, " Cancel ",
				new MessageAlertDialog.OnSelectionListener() {
					public void selected(int button, boolean cancel) {
						// Nothing
					}
				}, new MessageAlertDialog.OnInnerViewProvider() {

					public View provide(final MessageAlertDialog dialog) {
						LinearLayout buttonLayout = new LinearLayout(activity);
						buttonLayout.setOrientation(LinearLayout.HORIZONTAL);
						buttonLayout.setGravity(Gravity.CENTER_HORIZONTAL);

						LinearLayout.LayoutParams lpButtons = new LinearLayout.LayoutParams(
								180, 140);
						lpButtons.setMargins(20, 20, 20, 20);

						ImageLabelButton galleryButton = new ImageLabelButton(
								activity);
						galleryButton.setTextAndImageResource("Gallery",
								R.drawable.pictureimport);
						galleryButton.setLayoutParams(lpButtons);
						galleryButton
								.setOnClickListener(new View.OnClickListener() {
									public void onClick(View v) {
										Utility.selectFromGallery(activity);
										dialog.dismiss();
									}
								});
						ImageLabelButton photoButton = new ImageLabelButton(
								activity);
						photoButton.setTextAndImageResource("Take Photo",
								R.drawable.photobtn);
						photoButton.setLayoutParams(lpButtons);
						photoButton
								.setOnClickListener(new View.OnClickListener() {
									public void onClick(View v) {
										Utility.takePhoto(activity);
										dialog.dismiss();
									}
								});
						buttonLayout.addView(galleryButton);
						buttonLayout.addView(photoButton);
						return buttonLayout;
					}
				}).show();
	}

	// -------------------------------------------------------------------------

	/**
	 * Possibly remove image attachments if too large. This is a convenience
	 * method: It will not force to remove all images and it will substitue
	 * removed images by an empty string.
	 * 
	 * @param context
	 *            the context
	 * @param text
	 *            the text
	 * @param uid
	 *            the uid
	 * @return the string
	 */
	public static String possiblyRemoveImageAttachments(Context context,
			String text, int uid) {
		return possiblyRemoveImageAttachments(context, text, false, "", uid);
	}

	// -------------------------------------------------------------------------

	/**
	 * Possibly remove image attachments if too large. The forceRemoveAll flag
	 * may be used to get a version without any images, e.g. for the first line
	 * or the ticker.
	 * 
	 * @param context
	 *            the context
	 * @param text
	 *            the text
	 * @param forceRemoveAll
	 *            the force remove all
	 * @return the string
	 */
	public static String possiblyRemoveImageAttachments(Context context,
			String text, boolean forceRemoveAll, String substitute, int uid) {

		int serverId = Setup.getServerId(context, uid);
		int limit = Setup.getAttachmentServerLimit(context, serverId) * 1000;
		if (text.length() < limit && !forceRemoveAll) {
			// Log.d("communicator",
			// "total text is smaller than the attachment limit : textlen="
			// + text.length() + " < " + limit + " (limit)");
			// The total text is smaller than the attachment limit, so we do not
			// need to erase images manually
			return text;
		}

		final String STARTTAG = "[img ";
		final String ENDTAG = "]";

		int start = text.indexOf(STARTTAG);

		if (start < 0) {
			// Log.d("communicator",
			// "possiblyRemoveImageAttachments NO images");
			// No images to remove
			return text;
		}

		// The total text is larger than the attachment limit and there are
		// images included. So we now need to erase these...
		String strippedText = "";
		boolean done = false;
		start = 0;
		int end = 0;
		while (!done) {
			// Search for start tag
			start = text.indexOf(STARTTAG, end);
			if (start == -1) {
				// Not further start Found
				done = true;
				// Add last remaining text
				String textBetween = text.substring(end, text.length());
				strippedText += textBetween;
			} else {
				// Process any text since last end to this start
				String textBetween = text.substring(end, start);
				strippedText += textBetween;

				// Found, process this image
				end = text.indexOf(ENDTAG, start) + 1;

				String textImage = text.substring(start, end);

				int diff = end - start;
				if (diff <= limit && !forceRemoveAll) {
					// Image small enough, append
					strippedText += textImage;
				} else {
					strippedText += substitute;
				}
			}
		}
		return strippedText;
	}

	// ------------------------------------------------------------------------

	/**
	 * Send message or prompt the user if the message is too large for SMS or
	 * contains too large images for Internet/server limits.
	 * 
	 * @param context
	 *            the context
	 * @param transport
	 *            the transport
	 * @param encrypted
	 *            the encrypted
	 * @param promptLargeSMSOrLargeImages
	 *            the prompt large sms or large images
	 */
	private void sendMessageOrPrompt(final Context context,
			final int transport, final boolean encrypted) {
		final String messageTextString = messageText.getText().toString()
				.trim();
		if (messageTextString.length() > 0) {
			if (transport == DB.TRANSPORT_INTERNET) {
				final String messageTextString2 = Conversation
						.possiblyRemoveImageAttachments(context,
								messageTextString, hostUid);
				// Log.d("communicator",
				// "msgTextLEN=" + messageTextString.length());
				// Log.d("communicator",
				// "msgText2LEN=" + messageTextString2.length());
				// Log.d("communicator", "msgText=" + messageTextString);
				// Log.d("communicator", "msgText2=" + messageTextString2);

				if ((messageTextString2.length() != messageTextString.length())) {
					String title = "WARNING";
					String text = "This message contains at least one image that exceeds server limits. "
							+ "It will be removed automatically.\n\nDo you still want to send the message?";
					new MessageAlertDialog(context, title, text, " Yes ",
							" No ", " Cancel ",
							new MessageAlertDialog.OnSelectionListener() {
								public void selected(int button, boolean cancel) {
									if (button == MessageAlertDialog.BUTTONOK0) {
										sendMessage(context, transport,
												encrypted, messageTextString2);
									}
								}
							}).show();
					return;
				}
			} else {
				if (messageTextString.length() > Setup.SMS_SIZE_WARNING) {
					int numSMS = (int) (messageTextString.length() / Setup.SMS_DEFAULT_SIZE);
					String title = "WARNING";
					String text = "This is a large message which will need "
							+ numSMS
							+ " multi part SMS to be sent!\n\nReally send "
							+ numSMS + " SMS?";
					new MessageAlertDialog(context, title, text, " Yes ",
							" No ", " Cancel ",
							new MessageAlertDialog.OnSelectionListener() {
								public void selected(int button, boolean cancel) {
									if (button == MessageAlertDialog.BUTTONOK0) {
										sendMessage(context, transport,
												encrypted, messageTextString);
									}
								}
							}).show();
					return;
				}
			}
			// Message is not too long and not contains too large images
			sendMessage(context, transport, encrypted, messageTextString);
		}
	}

	// ------------------------------------------------------------------------

	/**
	 * Builds the image title addition for images saved to the gallery.
	 * 
	 * @param context
	 *            the context
	 * @param conversationItem
	 *            the conversation item
	 * @return the string
	 */
	public static String buildImageTitleAddition(Context context,
			ConversationItem conversationItem) {
		int mid = conversationItem.mid;
		String SMSorInternet = " [ " + mid + " ] message";
		if (mid == -1) {
			SMSorInternet = " [ *" + conversationItem.localid + " ] message";
		}
		if (conversationItem.transport == DB.TRANSPORT_SMS) {
			SMSorInternet = " [ *" + conversationItem.localid + " ] SMS";
		}
		String sentReceived = "from "
				+ Main.UID2Name(context, conversationItem.from, true);
		if (conversationItem.from != hostUid) {
			sentReceived = "to "
					+ Main.UID2Name(context, conversationItem.to, true);
		}

		long time = conversationItem.sent;
		if (time < 0) {
			time = conversationItem.created;
		}
		String date = DB.getDateString(time, false);

		return SMSorInternet + " " + sentReceived + " @ " + date + ".";
	}

	// ------------------------------------------------------------------------

	/**
	 * Builds an appropriate image description so that the image can later be
	 * mapped back to a conversation if the user wonders where the image in the
	 * gallery came from.
	 * 
	 * @param conversationItem
	 *            the conversation item
	 * @return the string
	 */
	public static String buildImageDescription(Context context,
			ConversationItem conversationItem) {
		int mid = conversationItem.mid;
		String SMSorInternet = "Internet message [ " + mid + " ]";
		if (mid == -1) {
			SMSorInternet = "Internet message [ *" + conversationItem.localid
					+ " ]";
		}
		if (conversationItem.transport == DB.TRANSPORT_SMS) {
			SMSorInternet = "SMS message [ *" + conversationItem.localid + " ]";
		}

		String sentReceived = "received from "
				+ Main.UID2Name(context, conversationItem.from, true);
		if (conversationItem.from != hostUid) {
			sentReceived = "sent to "
					+ Main.UID2Name(context, conversationItem.to, true);
		}

		long time = conversationItem.sent;
		if (time < 0) {
			time = conversationItem.created;
		}
		String date = DB.getDateString(time, false);

		return SMSorInternet + " " + sentReceived + " @ " + date + ".";
	}

	// ------------------------------------------------------------------------

	/**
	 * Gets the images size.
	 * 
	 * @return the images size
	 */
	public int getImagesSize() {
		return images.size();
	}

	// ------------------------------------------------------------------------

	/**
	 * Checks if is last image.
	 * 
	 * @param imageIndex
	 *            the image index
	 * @return true, if is last image
	 */
	public boolean isLastImage(int imageIndex) {
		return (getImagesSize() == imageIndex + 1);
	}

	// ------------------------------------------------------------------------

	/**
	 * Internally used by the ImageSimleyEditText to register a new image. The
	 * return value is the index. This is needed to know the position in the
	 * list of images.
	 * 
	 * @param imageSpan
	 *            the image span
	 * @return the int
	 */
	public int registerImage(Bitmap imageBitmap, int localid) {
		images.add(imageBitmap);
		return images.size() - 1;
	}

	// ------------------------------------------------------------------------

	/**
	 * Shows the next image and returns its imageIndex.
	 * 
	 * @param imageIndex
	 *            the image index
	 * @return the int
	 */
	public void showNextImage(Context context, int imageIndex) {
		if (!isLastImage(imageIndex)) {
			imageIndex++;
		} else {
			Utility.showToastShortAsync(context, "Last Image.");
		}
		Bitmap bitmap = (Bitmap) images.toArray()[imageIndex];
		ImageFullscreenActivity
				.showFullscreenImage(context, bitmap, imageIndex);
	}

	// ------------------------------------------------------------------------

	/**
	 * Shows the next image and returns its imageIndex.
	 * 
	 * @param imageIndex
	 *            the image index
	 * @return the int
	 */
	public void showPreviousImage(Context context, int imageIndex) {
		if (imageIndex > 0) {
			imageIndex--;
		} else {
			Utility.showToastShortAsync(context, "First Image.");
		}
		Bitmap bitmap = (Bitmap) images.toArray()[imageIndex];
		ImageFullscreenActivity
				.showFullscreenImage(context, bitmap, imageIndex);
	}

	// ------------------------------------------------------------------------

	/**
	 * Gets the group layout for the localgroupid (== hostuid) and the localid
	 * of the message.
	 * 
	 * @param context
	 *            the context
	 * @param localGroupId
	 *            the local group id
	 * @param localid
	 *            the localid
	 * @return the group layout
	 */
	public static LinearLayout getGroupLayout(Context context,
			int localGroupId, int localid) {
		LinearLayout.LayoutParams lpLayout = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		lpLayout.setMargins(5, 15, 5, 15);

		LinearLayout returnLayout = new LinearLayout(context);
		returnLayout.setLayoutParams(lpLayout);
		returnLayout.setOrientation(LinearLayout.VERTICAL);

		// int serverId = Setup.getGroupServerId(context, localGroupId);
		// String groupId = Setup.getGroupId(context, localGroupId);
		// List<Integer> sUids = Setup.getGroupMembersList(context, serverId,
		// groupId);
		List<Integer> uids = DB.getGroupMembersForMessage(context, localid);

		for (int uid : uids) {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			LinearLayout groupuseritem = (LinearLayout) inflater.inflate(
					R.layout.groupuseritem, null);

			TextView groupuser = ((TextView) groupuseritem
					.findViewById(R.id.groupuser));
			ImageView groupusericon = ((ImageView) groupuseritem
					.findViewById(R.id.groupusericon));
			ImageView groupsent = ((ImageView) groupuseritem
					.findViewById(R.id.groupsent));
			ImageView groupreceived = ((ImageView) groupuseritem
					.findViewById(R.id.groupreceived));
			ImageView groupread = ((ImageView) groupuseritem
					.findViewById(R.id.groupread));

			// int uid = Setup.getUid(context, sUid, serverId);
			String name = Main.UID2Name(context, uid, false);
			groupuser.setText(name);

			// int memberUid = Setup.getUid(context, sUid, serverId);
			int status = DB.isGroupMessage(context, localid, localGroupId, uid);

			if (status == DB.GROUPMESSAGE_READ) {
				groupread.setVisibility(View.VISIBLE);
			} else if (status == DB.GROUPMESSAGE_RECEIVED) {
				groupreceived.setVisibility(View.VISIBLE);
			} else if (status == DB.GROUPMESSAGE_SENT) {
				groupsent.setVisibility(View.VISIBLE);
			}

			Bitmap avatar = Conversation.retrieveAvatar(context, uid, false,
					true, false);
			if (avatar != null) {
				groupusericon.setImageBitmap(avatar);

			}

			returnLayout.addView(groupuseritem);
		}

		return returnLayout;
	}

	// ------------------------------------------------------------------------

}
