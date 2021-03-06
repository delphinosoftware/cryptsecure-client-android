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

import java.math.BigInteger;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;

import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Telephony;
import android.text.InputType;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * The Setup class is the third most important activity. It allows the user to
 * create an account or enter/login with an existing account. It allows to
 * enable the SMS option and has a second appearance for the settings dialog.
 * 
 * @author Christian Motika
 * @date 08/23/2015
 * @since 1.2
 * 
 */
@SuppressLint({ "TrulyRandom", "DefaultLocale", "UseSparseArrays" })
public class Setup extends Activity {

	// //// BASIC APP CONSTANTS //// //

	/** The version singleserver. */
	public static int VERSION_SINGLESERVER = 0;

	/** The version multiserver. */
	public static int VERSION_MULTISERVER = 2;

	/** The version multiserver with revoke column. */
	public static int VERSION_MULTISERVERREVOKE = 3;

	/** The programversion may lead to recovery actions for database changes. */
	public static int VERSION_CURRENT = VERSION_MULTISERVER;

	/** The server URL to be used per default. */
	public static String DEFAULT_SERVER = "http://www.cryptsecure.org";

	/**
	 * The prefix to be used for UID databases names. All conversations are
	 * saved in separate databases.
	 */
	public static String DATABASEPREFIX = "dcomm";

	/** The postfix to be used for UID database names. */
	public static String DATABASEPOSTFIX = ".db";

	/** The name of the sending database. */
	public static String DATABASESENDING = "sending.db";

	/** The name of the database for keeping a mapping for read confirmations. */
	public static String DATABASESENT = "sent.db";

	/** The internal id for intentextra. */
	public static String INTENTEXTRA = "org.cryptsecure.hostuid";

	/** The aler prefix. */
	public static String ALERT_PREFIX = "ALERT: ";

	/**
	 * The application package name used for making this app the default SMS
	 * app.
	 */
	public static String APPLICATION_PACKAGE_NAME = "org.cryptsecure";

	/** The group for system notifications created by CryptSecure. */
	public static String GROUP_CRYPTSECURE = "org.cryptsecure.notificationgroup";

	/**
	 * The locked count-down initial value for enabling editing the account
	 * information.
	 */
	private int accountLocked = 3;

	/**
	 * The advanced count-down initial value for enabling advanced options that
	 * should typically not be edited.
	 */
	public static final int ADVANCEDSETUPCOUNTDOWNSTART = 11;

	/** The current advanced count-down. */
	private int advanedSetupCountdown = ADVANCEDSETUPCOUNTDOWNSTART;

	/** The the server URL as saved (if not the default one). */
	public static final String SETTINGS_BASEURL = "baseurl";

	/**
	 * The cached version of the base URL for faster access because of frequent
	 * use.
	 */
	@SuppressLint("UseSparseArrays")
	private static HashMap<Integer, String> BASESERVERADDRESSCACHED = new HashMap<Integer, String>();

	/**
	 * After this time these confirmations will be discarded (from the sent.db),
	 * maybe the person has not enabled read confirmation or has left and does
	 * not use CryptSecure any more. It is ~ 3 month.
	 */
	public static final int TIMEOUT_FOR_RECEIVEANDREAD_CONFIRMATIONS = 90 * 24
			* 60 * 60 * 1000;

	/**
	 * The timeout for renewing the session key when about to send a message.
	 * Typically 60 minutes.
	 */
	public static final int AES_KEY_TIMEOUT_SENDING = 60 * 60 * 1000;

	/**
	 * The timeout for renewing the session key when about to receive a message.
	 * Typically 70 minutes. It should be a little longer to have an asymmetry
	 * and reduce the risk of both clients sending new session keys at the same
	 * time. This would considered to be a session key clash and needs to be
	 * resolved manually by one of the client users initiating a new sesison.
	 */
	public static final int AES_KEY_TIMEOUT_RECEIVING = 70 * 60 * 1000;

	/**
	 * The length of the session secret. The secret should have this exact
	 * length because the server implementation also depends on this length. The
	 * secret lives only for one session/login. The secret should be 20 bytes
	 * long.
	 */
	public static final int SECRETLEN = 20;

	/**
	 * The length of the request salt. The salt should have this exact length
	 * because the server implementation also depends on this length. The salt
	 * is renewed for every request. The salt should be 10 bytes long.
	 */
	public static final int SALTLEN = 10;

	/**
	 * Number of additionally added stuffing bytes to enhance encryption for
	 * short and for messages of equal content. It ensures that an attacker
	 * cannot tell that two messages are equal. The length must be the same for
	 * both clients and should be 5.
	 */
	public static final int RANDOM_STUFF_BYTES = 5;

	/**
	 * Keys cannot be updated more frequent than this interval. This helps
	 * making the app more responsive when switching back to the main activity.
	 * This will always try to update keys but will skip if the previous update
	 * was not at least ago this interval of time. Only the user may manually
	 * trigger the refresh an override this interval. Its 1 min.
	 */
	public static final int UPDATE_KEYS_MIN_INTERVAL = 60 * 1000;

	/** Save the timestamp when the last key update took place. */
	public static final String SETTING_LASTUPDATEKEYS = "lastupdatekeys";

	/**
	 * Similar to the update keys (s.a.) this is for updating phone numbers. The
	 * interval is a little longer: 10 min.
	 */
	public static final int UPDATE_PHONES_MIN_INTERVAL = 10 * 60 * 1000; // 10

	/** Save the time stamp when the last phone number update took place. */
	public static final String SETTING_LASTUPDATEPHONES = "lastupdatephones";

	/**
	 * Similar to the update keys (s.a.) this is for updating avatars. The
	 * interval is a much longer: 1200 min.
	 */
	public static final int UPDATE_AVATAR_MIN_INTERVAL = 1200 * 60 * 1000; // 10

	/** Save the time stamp when the last phone number update took place. */
	public static final String SETTING_LASTUPDATEAVATAR = "lastupdateavatar";

	/**
	 * Similar to the update keys (s.a.) this is for updating groups and invited
	 * groups and groupusers. The interval is 30min.
	 */
	public static final int UPDATE_GROUPS_MIN_INTERVAL = 30 * 60 * 1000; // 10

	/** Save the time stamp when the last phone number update took place. */
	public static final String SETTING_LASTUPDATEGROUPS = "lastupdategroups";

	/**
	 * Similar to the update keys (s.a.) this is for updating avatarss. The
	 * interval is a much longer: 1200 min.
	 */
	public static final int UPDATE_AUTOBACKUP_MIN_INTERVAL = 1200 * 60 * 1000; // 10

	/** Save the time stamp when the last phone number update took place. */
	public static final String SETTING_LASTUPDATEAUTOBACKUP = "lastupdateautobackup";

	/**
	 * Similar to the keys this is the minimal interval when automatic refresh
	 * of user names takes place. Typically 60 min.
	 */
	public static final int UPDATE_NAMES_MIN_INTERVAL = 60 * 60 * 1000;

	/** Save the time stamp when the last user name update took place. */
	public static final String SETTING_LASTUPDATENAMES = "lastupdatenames";

	/**
	 * This is the regular update time for requesting new messages when the app
	 * is in the background and power-save is OFF. It is 20 sec.
	 */
	public static final int REGULAR_UPDATE_TIME = 20;

	/**
	 * This is the regular update time for requesting new messages when the app
	 * is in the foreground and power-save is OFF. It is 5 sec.
	 */
	public static final int REGULAR_UPDATE_TIME_FAST = 5;

	/**
	 * This is the regular update time for requesting new messages when the app
	 * is in the background and power-save is ON. It is 60 sec.
	 */
	public static final int REGULAR_POWERSAVE_UPDATE_TIME = 60;

	/**
	 * This is the regular update time for requesting new messages when the app
	 * is in the foreground and power-save is ON. It is 10 sec.
	 */
	public static final int REGULAR_POWERSAVE_UPDATE_TIME_FAST = 10;

	/**
	 * "recursion" interval for sending/receiveing multiple messages one after
	 * another in seconds. It is 5 sec.
	 */
	public static final int REGULAR_UPDATE_TIME_TRYNEXT = 5;

	/**
	 * do not interrupt the user while he types, only if he stops typing for at
	 * least these milliseconds, allow background activity (sending &&
	 * receiving). It is 5 sec.
	 */
	public static final int TYPING_TIMEOUT_BEFORE_BACKGROUND_ACTIVITY = 5000;

	/**
	 * do not interrupt if the user is typing fast, do not even SCROLL (UI
	 * activity) in this time but scroll if the user holds on for at least 1
	 * seconds.
	 */
	public static final int TYPING_TIMEOUT_BEFORE_UI_ACTIVITY = 2000;

	/** After a connection error first try again after 10 sec. */
	public static final int ERROR_UPDATE_INTERVAL = 10; // 10 seconds

	/**
	 * After multiple consecutive connection errors for each error add 50% of
	 * interval time to save energy.
	 */
	public static final int ERROR_UPDATE_INCREMENT = 50;

	/**
	 * After multiple consecutive connection errors do not enlarge the retry
	 * interval to more than this maximum, typically 5 min.
	 */
	public static final int ERROR_UPDATE_MAXIMUM = 5 * 60;

	/**
	 * When showing a conversation first show only this maximum number of
	 * messages. Typically 50.
	 */
	public static final int MAX_SHOW_CONVERSATION_MESSAGES = 20;

	/** The Constant for showing ALL messages. */
	public static final int SHOW_ALL = -1;

	/** The Constant for showing ALL messages. */
	public static final int SHOW_MORE = 50;

	/** The Constant for not applicable. */
	public static final String NA = "N/A";

	/** The Constant ERROR_TIME_TO_WAIT. */
	public static final String ERROR_TIME_TO_WAIT = "errortimetowait";

	/** The Constant OPTION_ACTIVE. */
	public static final String OPTION_ACTIVE = "active";

	/** The Constant DEFAULT_ACTIVE. */
	public static final boolean DEFAULT_ACTIVE = true;

	/** The Constant HELP_ACTIVE. */
	public static final String HELP_ACTIVE = "Enables the background service for receiving messages. Turning this off may save battery but you will not receive messages if the app is closed.";

	/** The Constant OPTION_TONE. */
	public static final String OPTION_TONE = "System Alert Tone";

	/** The Constant DEFAULT_TONE. */
	public static final boolean DEFAULT_TONE = true;

	/** The Constant HELP_TONE. */
	public static final String HELP_TONE = "Play system alert tone when a new message is received (and the phone is not muted).";

	/** The Constant OPTION_VIBRATE. */
	public static final String OPTION_VIBRATE = "vibrate";

	/** The Constant DEFAULT_VIBRATE. */
	public static final boolean DEFAULT_VIBRATE = true;

	/** The Constant HELP_VIBTRATE. */
	public static final String HELP_VIBTRATE = "Vibrate when a new message is received (and the phone is not muted).";

	/** The Constant OPTION_NOTIFICATION. */
	public static final String OPTION_NOTIFICATION = "notification";

	/** The Constant DEFAULT_NOTIFICATION. */
	public static final boolean DEFAULT_NOTIFICATION = true;

	/** The Constant HELP_NOTIFICATION. */
	public static final String HELP_NOTIFICATION = "Prompt a system notification when a new message is received.";

	/** The Constant OPTION_IGNORE. */
	public static final String OPTION_IGNORE = "ignore";

	/** The Constant DEFAULT_IGNORE. */
	public static final boolean DEFAULT_IGNORE = false;

	/** The Constant HELP_IGNORE. */
	public static final String HELP_IGNORE = "Only message from users in your userlist will be received. Messages from other users will be silently discarded.";

	/** The Constant OPTION_ENCRYPTION. */
	public static final String OPTION_ENCRYPTION = "encryption";

	/** The Constant DEFAULT_ENCRYPTION. */
	public static final boolean DEFAULT_ENCRYPTION = false;

	/** The Constant HELP_ENCRYPTION. */
	public static final String HELP_ENCRYPTION = "Use encryption for sending messages. Will only work if your communication partner has also turned on encryption.\n\nIt is strongly advised that you always leave encryption on!";

	/** The Constant OPTION_AUTOSAVE. */
	public static final String OPTION_AUTOSAVE = "autosaveattachments";

	/** The Constant DEFAULT_AUTOSAVE. */
	public static final boolean DEFAULT_AUTOSAVE = true;

	/** The Constant HELP_AUTOSAVE. */
	public static final String HELP_AUTOSAVE = "Image attachments can be automatically saved to your gallery.";

	/** The Constant OPTION_NOREAD. */
	public static final String OPTION_NOREAD = "noread";

	/** The Constant DEFAULT_NOREAD. */
	public static final boolean DEFAULT_NOREAD = false;

	/** The Constant HELP_NOREAD. */
	public static final String HELP_NOREAD = "Refuse read confirmations for received messages (second blue checkmark).\n\nWARNING: If you refuse read confirmation you cannot see read confirmations of anybody else!";

	/** The Constant OPTION_NOSCREENSHOTS. */
	public static final String OPTION_NOSCREENSHOTS = "noscreenshots";

	/** The Constant DEFAULT_NOSCREENSHOTS. */
	public static final boolean DEFAULT_NOSCREENSHOTS = true;

	/** The Constant HELP_NOSCREENSHOTS. */
	public static final String HELP_NOSCREENSHOTS = "Disallows making automatic or manual screenshots of your messages for privacy protection.";

	/** The Constant OPTION_CHATMODE. */
	public static final String OPTION_CHATMODE = "chatmode";

	/** The Constant DEFAULT_CHATMODE. */
	public static final boolean DEFAULT_CHATMODE = false;

	/** The Constant HELP_CHATMODE. */
	public static final String HELP_CHATMODE = "Send a message by hitting <RETURN>. If chat mode is turned on, you cannot make explicit linebreaks.";

	/** The Constant OPTION_QUICKTYPE. */
	public static final String OPTION_QUICKTYPE = "Quick Type";

	/** The Constant DEFAULT_QUICKTYPE. */
	public static final boolean DEFAULT_QUICKTYPE = true;

	/** The Constant HELP_QUICKTYPE. */
	public static final String HELP_QUICKTYPE = "If you switch your phone orientation to landscape in order to type, the keyboard is shown automatically and you can just start typing without extra clicking into the message input text field.";

	/** The Constant OPTION_QUICKTYPE. */
	public static final String OPTION_SMILEYS = "Graphical Smileys";

	/** The Constant DEFAULT_QUICKTYPE. */
	public static final boolean DEFAULT_SMILEYS = true;

	/** The Constant HELP_QUICKTYPE. */
	public static final String HELP_SMILEY = "If you enable this option then textual smileys are shown as graphical ones.";

	/** The Constant OPTION_DARKMODE. */
	public static final String OPTION_DARKMODE = "Dark Theme";

	/** The Constant DEFAULT_DARKMODE. */
	public static final boolean DEFAULT_DARKMODE = true;

	/** The Constant HELP_DARKMODE. */
	public static final String HELP_DARKMODE = "You may want to enable the light or the dark theme. The dark theme is the default.";

	/** The Constant OPTION_RECEIVEALLSMS. */
	public static final String OPTION_RECEIVEALLSMS = "receiveallsms";

	/** The Constant DEFAULT_RECEIVEALLSMS. */
	public static final boolean DEFAULT_RECEIVEALLSMS = false;

	/** The Constant HELP_RECEIVEALLSMSE. */
	public static final String HELP_RECEIVEALLSMSE = "You can use Delphino CryptSecure even as your default app for all SMS. Users that are not registered are listed by their names from your address book and you can only send them plain text SMS.";

	/** The Constant OPTION_POWERSAVE. */
	public static final String OPTION_POWERSAVE = "powersave";

	/** The Constant DEFAULT_POWERSAVE. */
	public static final boolean DEFAULT_POWERSAVE = true;

	/** The Constant HELP_POWERSAVE. */
	public static final String HELP_POWERSAVE = "Delphino CryptSecure can operate in a power save mode were sending/receiving is reduced to every 10 seconds when active or 60 seconds when passive instead of 5 seconds and 20 seconds respectively in the non-power save mode.\n\nThis mode saves your battery.";

	/** The Constant OPTION_SMSMODE. */
	public static final String OPTION_SMSMODE = "smsmode";

	/** The Constant DEFAULT_SMSMODE. */
	public static final boolean DEFAULT_SMSMODE = false;

	/** The Constant PUBKEY for saving/loading the public RSA key. */
	public static final String PUBRSAKEY = "pk";

	/** The Constant PRIVATEKEY for saving/loading the private RSA key. */
	public static final String PRIVATERSAKEY = "k";

	/** The Constant AESKEY for session key backups. */
	public static final String AESKEYBAK = "aesbak";

	/** The Constant AESKEY for session keys. */
	public static final String AESKEY = "aes";

	/** The Constant SERVERLIST. */
	public static final String SERVERLIST = "serverlist";

	/** The Constant SERVER_UID. */
	public static final String SERVER_UID = "uid";

	/**
	 * The Constant UID_AUTOADDED for users that were automatically added. For
	 * these users the device user must allow the adding afterwards, otherwise
	 * these users will not get backedup to the server to ensure auto-added
	 * users do not get key/avatar/username!
	 */
	public static final String UID_AUTOADDED = "usersautoadded";

	/** The Constant UID_IGNORED for users that were ignored by the user! */
	public static final String UID_IGNORED = "usersignored";

	/** The Constant GROUPNAME for the name of a group. */
	public static final String GROUPNAME = "servergroupname";

	/**
	 * The Constant GROUPSECRET for the secret of a group. Each group has a
	 * server generated random secret that is shared by all group members. It is
	 * necessary to join and also to send messages that should be listed in this
	 * group.
	 */
	public static final String GROUPSECRET = "servergroupsecret";

	/** The Constant GROUPS for groups of a specific server. */
	public static final String GROUPS = "servergroups";

	/** The Constant GROUPMEMBERS for group members of a specific server group. */
	public static final String GROUPMEMBERS = "servergroupmembers";

	/** The Constant LOCALGROUP2GROUPID for mapping localgroup id. */
	public static final String LOCALGROUP2GROUPID = "servergrouplocalgroup2group";

	/** The Constant LOCALGROUP2SERVERD for mapping localgroup id. */
	public static final String LOCALGROUP2SERVER = "servergrouplocalgroup2server";

	/** The Constant SECRET2LOCALGROUP for mapping secret to localgroup id. */
	public static final String SECRET2LOCALGROUP = "servergroupsecret2local";

	/** The Constant GROUPS for invited groups of a specific server. */
	public static final String INVITEDGROUPS = "servergroupsinvited";

	/** The Constant SERVER_EMAIL. */
	public static final String SERVER_EMAIL = "email";

	/** The Constant SERVER_PWD. */
	public static final String SERVER_PWD = "pwd";

	/** The Constant SERVER_USERNAME. */
	public static final String SERVER_USERNAME = "username";

	/** The Constant SUID2UID enables mapping from UID to SUID. */
	public static final String UID2SUID = "UID2SUID";

	/** The Constant SUID2SERVERID enables mapping from UID to SERVERID. */
	public static final String UID2SERVERID = "UID2SERVERID";

	/**
	 * The Constant DEFAULTSERVERINDEX is the index in the serverid list that is
	 * the default one,e.g. for displaying the name in the main screen.
	 */
	public static final int DEFAULTSERVERINDEX = 0;

	/** The Constant SERVER_ACTIVE. */
	public static final String SERVER_ACTIVE = "serveractive";

	/** The Constant for the saved round robin server id for receiving messages. */
	public static final String SERVERLASTRRIDRECEIVE = "serverlastrridreceive";

	/** The Constant for the saved round robin server id for sending messages. */
	public static final String SERVERLASTRRIDSEND = "serverlastrridsend";

	/** The Constant SERVER_ACTIVEDEFAULT for new servers. */
	public static final boolean SERVER_ACTIVEDEFAULT = true;

	/**
	 * The Constant KEYEXTRAWAITCOUNTDOWN is a countdown that is set by the
	 * automated session key sending component for SMS to 10 and for Internet to
	 * 3. Basically it enforces to delay the sending of the next SMS or Internet
	 * message a certain amount of time to make it more likely that the new auto
	 * generated session key has been received before.
	 */
	public static final String KEYEXTRAWAITCOUNTDOWN = "keyextrawaitcountdown";

	/**
	 * The Constant LASTKEYMID for the last mid of a key message sent to a user.
	 * This should be reset when a new key is send, it should be set by the
	 * first use of DB.getLastSendKeyMessage().
	 */
	public static final String LASTKEYMID = "lastaeskeymid";

	/** The Constant PHONE for other users phone numbers. */
	public static final String PHONE = "hostphone";

	/** The Constant AVATAR for other users avatars. */
	public static final String AVATAR = "hostavatar";

	/** The Constant SETTINGS_USERLIST for saving/loading the userlist. */
	public static final String SETTINGS_USERLIST = "userlist";

	/**
	 * The Constant SETTINGS_USERLISTLASTMESSAGE for saving/loading the last
	 * message per user.
	 */
	public static final String SETTINGS_USERLISTLASTMESSAGE = "userlistlastmessage";

	/**
	 * The Constant SETTINGS_USERLISTLASTMESSAGETIMESTAMP for saving/loading the
	 * timestamp of the last message per user.
	 */
	public static final String SETTINGS_USERLISTLASTMESSAGETIMESTAMP = "userlistlastmessagetimestamp";

	/** The Constant SETTINGS_PHONE for the phone number. */
	public static final String SETTINGS_PHONE = "phone";

	/** The Constant SETTINGS_UPDATENAME. */
	public static final String SETTINGS_UPDATENAME = "updatename";

	/** The Constant SETTINGS_DEFAULT_UPDATENAME. */
	public static final boolean SETTINGS_DEFAULT_UPDATENAME = true;

	/** The Constant SETTINGS_UPDATEPHONE. */
	public static final String SETTINGS_UPDATEPHONE = "updatephone";

	/** The Constant SETTINGS_DEFAULT_UPDATEPHONE. */
	public static final boolean SETTINGS_DEFAULT_UPDATEPHONE = true;

	/** The Constant SETTINGS_PHONEISMODIFIED. */
	public static final String SETTINGS_PHONEISMODIFIED = "phoneismodified";

	/** The Constant SETTINGS_UPDATEAVATAR. */
	public static final String SETTINGS_UPDATEAVATAR = "updateavatar";

	/** The Constant SETTINGS_DEFAULT_UPDATEAVATAR. */
	public static final boolean SETTINGS_DEFAULT_UPDATEAVATAR = true;

	/** The Constant SETTINGS_AVATARISMODIFIED. */
	public static final String SETTINGS_AVATARISMODIFIED = "avatarismodified";

	/**
	 * The Constant SETTINGS_DEFAULTMID. Globally for all users: This is the
	 * base mid, we do not request messages before this mid.
	 */
	public static final String SETTINGS_DEFAULTMID = "defaultmid";

	/**
	 * The Constant SETTINGS_DEFAULTMID_DEFAULT. Globally for all users: should
	 * only be -1 if there is no msg in the DB. Then we retrieve the highest mid
	 * from server!
	 */
	public static final int SETTINGS_DEFAULTMID_DEFAULT = -1;

	/**
	 * The Constant SETTINGS_SERVERKEY for sending, e.g., encrypted login data
	 * to the server.
	 */
	public static final String SETTINGS_SERVERKEY = "serverkey";

	/** The Constant SETTINGS_SESSIONSECRET. To remember the session. */
	public static final String SETTINGS_SESSIONSECRET = "tmpsessionsecret";

	/** The Constant SETTINGS_SESSIONID. To remember the session id */
	public static final String SETTINGS_SESSIONID = "tmpsessionid";

	/**
	 * The Constant SETTINGS_LOGINERRORCNT. Saves the login errors that we
	 * receive from the server on successful login. It is currently not used.
	 */
	public static final String SETTINGS_LOGINERRORCNT = "loginerrorcnt";

	/**
	 * The Constant SETTINGS_INVALIDATIONCOUNTER. If uids are corrupted more
	 * than MAX (see next) then invalidate the session.
	 */
	public static final String SETTINGS_INVALIDATIONCOUNTER = "invalidationcounter";

	/**
	 * The Constant SETTINGS_INVALIDATIONCOUNTER_MAX. If uids are corrupted more
	 * than MAX (see next) then invalidate the session
	 */
	public static final int SETTINGS_INVALIDATIONCOUNTER_MAX = 3;

	/**
	 * The Constant SETTINGS_LARGEST_MID_RECEIVED. This is used as a basis for
	 * receiving (newer) messages than this mid.
	 */
	public static final String SETTINGS_LARGEST_MID_RECEIVED = "largestmidreceived";

	/**
	 * The Constant SETTINGS_LARGEST_TS_RECEIVED. This is used as a basis for
	 * receiving (newer) receive confirmation than this timestamp.
	 */
	public static final String SETTINGS_LARGEST_TS_RECEIVED = "largesttsreceived";

	/**
	 * The Constant SETTINGS_LARGEST_TS_READ. This is used as a basis for
	 * receiving (newer) read confirmation than this timestamp.
	 */
	public static final String SETTINGS_LARGEST_TS_READ = "largesttsread";

	/**
	 * The Constant SETTINGS_HAVEASKED_NOENCRYPTION. Remember if we have asked
	 * the user to enable the encryption option. This is reset on UID change or
	 * when turning the encryption feature off.
	 */
	public static final String SETTINGS_HAVEASKED_NOENCRYPTION = "haveaskednoenryption";

	/**
	 * The Constant SMS_FAIL_CNT. Try to send an SMS 5 times before claiming
	 * failed (ONLY counting unknown errors, NO network errors!). If the network
	 * is down the SMS should wait until it is up again and NOT fail.
	 */
	public static final int SMS_FAIL_CNT = 2;

	/**
	 * The Constant SERVER_ATTACHMENT_LIMIT. Must be acquired by the server on
	 * refresh of userlist or if not set (=-1). A value of 0 means that
	 * attachments are not allowed for Internet messages and any other values
	 * limits the size in KB for messages.
	 */
	public static final String SERVER_ATTACHMENT_LIMIT = "serverattachmentlimit";

	/**
	 * The Constant SERVER_ATTACHMENT_LIMIT_DEFAULT tells that we did not yet
	 * load the server limit.
	 */
	public static final int SERVER_ATTACHMENT_LIMIT_DEFAULT = -1;

	/**
	 * The Constant UPDATE_SERVER_ATTACHMENT_LIMIT_INTERVAL. Update at most
	 * every 60 minutes.
	 */
	public static final int UPDATE_SERVER_ATTACHMENT_LIMIT_MINIMAL_INTERVAL = 60; // Minutes

	/**
	 * The Constant LASTUPDATE_SERVER_ATTACHMENT_LIMIT. The timestamp when the
	 * server limit last time
	 */
	public static final String LASTUPDATE_SERVER_ATTACHMENT_LIMIT = "lastupdateserverattachmentlimit";

	/**
	 * The Constant SIZEWARNING_SMS. If this numebr of bytes is passed more than
	 * 10 multipart SMS need to be send. Before sending such large SMS alert the
	 * user!
	 */
	public static final int SMS_SIZE_WARNING = 1600;

	/** The standardsmssize. */
	public static int SMS_DEFAULT_SIZE = 160; // standard one sms

	/** The standardsmssize. */
	public static int SMS_DEFAULT_SIZE_MULTIPART = 153; // standard multipart
														// sms

	/** The standardsmssize. */
	public static int SMS_DEFAULT_SIZE_ENCRYPTED = 80; // ~ roughly 100% blow up

	/** Split a message if the part size is larger than this constant */
	public static final int MULTIPART_MESSAGELIMIT = 450;

	/** The Constant COLOR_MAIN_BLUE. */
	public static final int COLOR_MAIN_BLUE = Color.parseColor("#FF5DA9FE");

	/** The Constant COLOR_MAIN_BLUEDARK a darker version. */
	public static final int COLOR_MAIN_BLUEDARK = Color.parseColor("#ff0077ff");

	/** The Constant COLOR_MAIN_BLUEDARK an even darker version. */
	public static final int COLOR_MAIN_BLUEDARKEST = Color
			.parseColor("#445DA9FE");

	/** The Constant COLOR_BLUELINE. */
	public static final int COLOR_BLUELINE = Color.parseColor("#FF00CCFF");

	// ------------------------------------------------------------------------

	/** The currently loaded avatar. */
	private String avatar = null;

	/**
	 * The flag if a restart required. When closing this activoity, a restart is
	 * performed!
	 */
	private boolean restartRequiredFlag = false;

	/** The ignoredspinner. */
	private Spinner ignoredspinner;

	/** The groupspinner. */
	private static Spinner groupspinner;

	/** The active. */
	private CheckBox active;

	/** The encryption. */
	private CheckBox encryption;

	/** The notification. */
	private CheckBox notification;

	/** The tone. */
	private CheckBox tone;

	/** The vibrate. */
	private CheckBox vibrate;

	/** The ignore. */
	private CheckBox ignore;

	/** The autosave. */
	private CheckBox autosave;

	/** The noread. */
	private CheckBox noread;

	/** The chatmode. */
	private CheckBox chatmode;

	/** The quicktype. */
	private CheckBox quicktype;

	/** The smileys. */
	private CheckBox smileys;

	/** The darkmode. */
	private CheckBox darkmode;

	/** The noscreenshots. */
	private CheckBox noscreenshots;

	/** The powersave. */
	private CheckBox powersave;

	/** The receiveallsms. */
	private CheckBox receiveallsms;

	/** The helpactive. */
	private ImageView helpactive;

	/** The helpencryption. */
	private ImageView helpencryption;

	/** The helptone. */
	private ImageView helptone;

	/** The helpvibrate. */
	private ImageView helpvibrate;

	/** The helpnotification. */
	private ImageView helpnotification;

	/** The helpignore. */
	private ImageView helpignore;

	/** The helpautosave. */
	private ImageView helpautosave;

	/** The helpnoread. */
	private ImageView helpnoread;

	/** The helpquicktype. */
	private ImageView helpquicktype;

	/** The helpsmileys. */
	private ImageView helpsmileys;

	/** The helpnodarkmode. */
	private ImageView helpdarkmode;

	/** The helpchatmode. */
	private ImageView helpchatmode;

	/** The helpnoscreenshots. */
	private ImageView helpnoscreenshots;

	/** The helppowersave. */
	private ImageView helppowersave;

	/** The helpreceiveallsms. */
	private ImageView helpreceiveallsms;

	/** The uid. */
	private static EditText uid;

	/** The email. */
	private EditText email;

	/** The pwd. */
	private EditText pwd;

	/** The usernew. */
	private EditText usernew;

	/** The emailnew. */
	private EditText emailnew;

	/** The pwdnew. */
	private EditText pwdnew;

	/** The user. */
	private EditText user;

	/** The pwdchange. */
	private EditText pwdchange;

	/** The error. */
	private static TextView error;

	/** The info. */
	private static TextView info;

	/** The deviceid. */
	private static TextView deviceid;

	/** The advancedsettings. */
	private static LinearLayout advancedsettings;

	/** The buttonclearsending. */
	private static Button buttonclearsending;

	/** The buttondebugprint. */
	private static Button buttondebugprint;

	/** The buttondeletedatabase. */
	private static Button buttondeletedatabase;

	/** The ownaccountkeyparent. */
	private static LinearLayout ownaccountkey = null;

	/** The create. */
	private Button create;

	/** The login. */
	private Button login;

	/** The updatepwd. */
	private Button updatepwd;

	/** The updateuser. */
	private Button updateuser;

	/** The updateavatar. */
	private Button updateavatar;

	/** The phone. */
	private static EditText phone;

	/** The enablesmsoption. */
	private static Button enablesmsoption;

	/** The disablesmsoption. */
	private static Button disablesmsoption;

	/** The backup. */
	private static Button backup;

	/** The restore. */
	private Button restore;

	/** The accountnew. */
	private LinearLayout accountnew;

	/** The accountexisting. */
	private LinearLayout accountexisting;

	/** The accountexistinglogindata section only. */
	private static LinearLayout accountexistinglogindata;

	/** The accountonline. */
	private static LinearLayout accountonline;

	/** The account settingspart. */
	private LinearLayout accountsection;

	/** The header of account settingspart. */
	private LinearLayout accountheader;

	/** The settingspart. */
	private LinearLayout settingssection;

	/** The online. */
	private static boolean online = false;

	/** The newaccount. */
	private CheckBox newaccount;

	/** The account type. */
	private boolean accountType = false;

	private ImageView serverimage = null;

	private CheckBox serverdisabled = null;

	/** The serverbuttonmodify. */
	private ImageButton serverbuttonmodify = null;

	/** The serverbuttonadd. */
	private Button serverbuttonadd = null;

	/** The serverbuttondelete. */
	private Button serverbuttondelete = null;

	/** The server selection spinner. */
	private Spinner serverspinner = null;

	/** The selected server id. */
	private int selectedServerId = 0;

	// ------------------------------------------------------------------------

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Setup.possiblyDisableScreenshot(this);

		final Activity context = this;

		// Apply custom title bar (with holo :-)
		// See Main for more details.
		LinearLayout main = Utility.setContentViewWithCustomTitle(this,
				R.layout.activity_setup, R.layout.title_general);

		LinearLayout titlegeneral = (LinearLayout) findViewById(R.id.titlegeneral);
		Utility.setBackground(this, titlegeneral, R.drawable.dolphins3blue);
		ImagePressButton btnback = (ImagePressButton) findViewById(R.id.btnback);
		btnback.initializePressImageResource(R.drawable.btnback);
		LinearLayout btnbackparent = (LinearLayout) findViewById(R.id.btnbackparent);
		btnback.setAdditionalPressWhiteView(btnbackparent);
		btnback.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				goBack(context);
			}
		});

		accountType = this.getIntent().hasExtra("account");

		error = (TextView) findViewById(R.id.error);
		info = (TextView) findViewById(R.id.info);

		LinearLayout mainsettingsinnerlayout = (LinearLayout) findViewById(R.id.mainsettingsinnerlayout);
		mainsettingsinnerlayout.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				setErrorInfo(null);
			}
		});

		accountsection = (LinearLayout) findViewById(R.id.accountsection);
		accountheader = (LinearLayout) findViewById(R.id.accountheader);
		settingssection = (LinearLayout) findViewById(R.id.settingssection);

		serverimage = (ImageView) findViewById(R.id.serverimage);
		serverdisabled = (CheckBox) findViewById(R.id.serverdisabled);

		serverbuttonmodify = (ImageButton) findViewById(R.id.serverbuttonmodify);
		serverbuttonadd = (Button) findViewById(R.id.serverbuttonadd);
		serverbuttondelete = (Button) findViewById(R.id.serverbuttondelete);
		hideServerModifyButtons();
		serverbuttonmodify.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Utility.showToastAsync(context,
						"Long press to modify message servers.");
			}
		});
		serverbuttonmodify
				.setOnLongClickListener(new View.OnLongClickListener() {
					public boolean onLongClick(View v) {
						promptModifyServer(context);
						return true;
					}
				});
		serverbuttonadd.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				promptAddServer(context);
			}
		});
		serverbuttondelete.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				promptDeleteServer(context);
			}
		});

		serverimage.setOnLongClickListener(new OnLongClickListener() {
			public boolean onLongClick(View v) {
				boolean active = isServerActive(context, selectedServerId,
						false);
				setServerActive(context, selectedServerId, !active);
				updateServerImage(context, false, false);
				return false;
			}
		});
		serverdisabled.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (!serverdisabled.isChecked()) {
					enableServer(context, false, false);
					if (Main.isAlive()) {
						Main.getInstance().updateInfoMessageBlockAsync(context);
					}
				}
			}
		});

		serverspinner = (Spinner) findViewById(R.id.serverspinner);
		serverspinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				selectedServerId = -1;
				try {
					String serverAddress = (String) serverspinner
							.getSelectedItem();
					selectedServerId = getServerId(serverAddress);
					// login == false
					online = false;
					updateonline();
					// reload login info text fields
					loadServerTab(context, selectedServerId);
				} catch (Exception e) {
					// ignore
				}
			}

			public void onNothingSelected(AdapterView<?> parent) {
			}
		});

		if (!accountType) {
			accountsection.setVisibility(View.GONE);
			loadSettings(context);
		} else {
			Setup.updateGroupsFromAllServers(context, true);

			settingssection.setVisibility(View.GONE);
			updateServerSpinner(context, serverspinner);
			buildServerTabs(context);
			buildAccountOptionButtons(context);
		}

		ownaccountkey = (LinearLayout) findViewById(R.id.ownaccountkey);

		if (this.getIntent().hasExtra("serverId")) {
			// Set the preferred account on startup, per default do not change
			// selectedServerId
			selectedServerId = this.getIntent().getIntExtra("serverId",
					selectedServerId);
			// reload login info text fields
			loadServerTab(context, selectedServerId);
			int index = 0;
			for (int serverIdVgl : getServerIds(context)) {
				if (serverIdVgl == selectedServerId) {
					serverspinner.setSelection(index);
				}
				index++;
			}
			// Utility.showToastAsync(context,
			// getServer(context, selectedServerId));
		}

		advancedsettings = (LinearLayout) findViewById(R.id.advancedsettings);
		advancedsettings.setVisibility(View.GONE);
		buttonclearsending = (Button) findViewById(R.id.buttonclearsending);
		buttonclearsending.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				DB.rebuildDBSending(context);
				Utility.showToastInUIThread(context, "Sending Queue Cleared.");
			}
		});
		buttondebugprint = (Button) findViewById(R.id.buttondebugprint);
		buttondebugprint.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				String debug = DB.printDBSending(context);
				for (int uid : Main.loadUIDList(context)) {
					DB.printDB(context, uid);
				}
				Utility.showToastInUIThread(context, "Debug Output Printed");
				setErrorInfo(debug, false);
			}
		});
		buttondeletedatabase = (Button) findViewById(R.id.buttondeletedatabase);
		buttondeletedatabase.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				String title = "Delete Local Database";
				String text = "Enter UID to completely (!) delete the local database:";
				new MessageInputDialog(context, title, text, " Delete ", null,
						"Cancel", "",
						new MessageInputDialog.OnSelectionListener() {
							public void selected(MessageInputDialog dialog,
									int button, boolean cancel,
									String uidToDelete) {
								if (button == MessageInputDialog.BUTTONOK0) {
									// look if UID is a valid user
									int uid = Utility.parseInt(uidToDelete, -1);
									if (uid != -1) {
										DB.dropDB(context, uid);
										Utility.showToastAsync(context,
												"Database of UID " + uid
														+ " deleted.");
									} else {
										Utility.showToastAsync(context,
												"UID is invalid. Cannot proceed.");
									}
								}
								dialog.dismiss();
							};
						}).show();
			}
		});

		deviceid = (TextView) findViewById(R.id.deviceid);
		deviceid.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				advanedSetupCountdown--;
				if (advanedSetupCountdown <= 5 && advanedSetupCountdown > 1) {
					String toastText = "" + advanedSetupCountdown
							+ " more clicks to see the advanced options.";
					Utility.showToastInUIThread(context, toastText);
				}
				if (advanedSetupCountdown == 1) {
					String toastText = "You will see the ADVANCED options at the next click!";
					Utility.showToastInUIThread(context, toastText);
				}
				if (advanedSetupCountdown <= 0) {
					advanedSetupCountdown = ADVANCEDSETUPCOUNTDOWNSTART;
					// Make it visible
					advancedsettings.postDelayed(new Runnable() {
						public void run() {
							advancedsettings.setVisibility(View.VISIBLE);
						}
					}, 4000);
				}
			}
		});

		updateTitleIDInfo(context);
		// requestWindowFeature(Window.FEATURE_LEFT_ICON);
		int serverDefaultId = Setup.getServerDefaultId(context);
		String uidString2 = Utility.loadStringSetting(context, Setup.SERVER_UID
				+ serverDefaultId, "");
		if (uidString2 != null & uidString2.trim().length() > 0) {
			uidString2 = " - UID " + uidString2;
		}

		if (!accountType) {
			setTitle("Settings" + uidString2);
		} else {
			setTitle("Account" + uidString2);
		}
		setErrorInfo(null);

		try {
			// Setting backgrounds
			Utility.setBackground(this, main, R.drawable.dolphins1);
			LinearLayout settingsBackground = ((LinearLayout) findViewById(R.id.settingsbackground));
			Utility.setBackground(this, settingsBackground,
					R.drawable.dolphins1);
			settingsBackground.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					setErrorInfo(null);
				}
			});

			Utility.setBackground(this, accountheader, R.drawable.dolphins3);

		} catch (Exception e) {
			e.printStackTrace();
		}

		if (accountType) {
			// reload login info text fields
			loadServerTab(context, selectedServerId);
		}

		Utility.hideKeyboard(context);
	}

	// ------------------------------------------------------------------------

	/**
	 * Hide server modify buttons.
	 */
	public void hideServerModifyButtons() {
		serverbuttonmodify.setVisibility(View.VISIBLE);
		serverbuttonadd.setVisibility(View.GONE);
		serverbuttondelete.setVisibility(View.GONE);
	}

	// ------------------------------------------------------------------------

	/**
	 * Builds the server tabs.
	 * 
	 * @param context
	 *            the context
	 */
	@SuppressLint("InflateParams")
	private void buildServerTabs(final Context context) {
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final ScrollView accountInner = (ScrollView) inflater.inflate(
				R.layout.setup_account, null);
		accountsection.addView(accountInner);
		linkAccountOptions();
	}

	// ------------------------------------------------------------------------

	private final static int ACCOUNTOPTION_USERNAME = 1;
	private final static int ACCOUNTOPTION_SMS = 2;
	private final static int ACCOUNTOPTION_AVATAR = 3;
	private final static int ACCOUNTOPTION_PASSWORD = 4;
	private final static int ACCOUNTOPTION_GROUPS = 5;
	private final static int ACCOUNTOPTION_BACKUP = 6;

	/**
	 * Builds the account option buttons for a nicer view to the options.
	 * 
	 * @param context
	 *            the context
	 */
	private void buildAccountOptionButtons(final Context context) {
		LinearLayout accountoptionbuttons = (LinearLayout) findViewById(R.id.accountoptionbuttons);
		accountoptionbuttons.removeAllViews();

		LinearLayout buttonLayout = new LinearLayout(context);
		buttonLayout.setOrientation(LinearLayout.VERTICAL);
		buttonLayout.setGravity(Gravity.CENTER_VERTICAL);

		LinearLayout buttonLayout1 = new LinearLayout(context);
		buttonLayout1.setOrientation(LinearLayout.HORIZONTAL);
		buttonLayout1.setGravity(Gravity.CENTER_HORIZONTAL);

		LinearLayout buttonLayout2 = new LinearLayout(context);
		buttonLayout2.setOrientation(LinearLayout.HORIZONTAL);
		buttonLayout2.setGravity(Gravity.CENTER_HORIZONTAL);

		LinearLayout buttonLayout3 = new LinearLayout(context);
		buttonLayout3.setOrientation(LinearLayout.HORIZONTAL);
		buttonLayout3.setGravity(Gravity.CENTER_HORIZONTAL);

		LinearLayout.LayoutParams lpButtons1 = new LinearLayout.LayoutParams(
				200, 140);
		lpButtons1.setMargins(5, 20, 5, 5);
		LinearLayout.LayoutParams lpButtons2 = new LinearLayout.LayoutParams(
				200, 140);
		lpButtons2.setMargins(5, 5, 5, 5);
		LinearLayout.LayoutParams lpButtons3 = new LinearLayout.LayoutParams(
				200, 140);
		lpButtons3.setMargins(5, 5, 5, 20);

		ImageLabelButton usernameButton = new ImageLabelButton(context);
		usernameButton.setTextAndImageResource("Username",
				R.drawable.btnusername);
		usernameButton.setLayoutParams(lpButtons1);
		usernameButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				makeVisible(context, ACCOUNTOPTION_USERNAME);
			}
		});
		ImageLabelButton smsButton = new ImageLabelButton(context);
		smsButton.setTextAndImageResource("SMS Option", R.drawable.btnsms);
		smsButton.setLayoutParams(lpButtons1);
		smsButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				makeVisible(context, ACCOUNTOPTION_SMS);
			}
		});
		ImageLabelButton avatarButton = new ImageLabelButton(context);
		avatarButton.setTextAndImageResource("Avatar", R.drawable.person);
		avatarButton.setLayoutParams(lpButtons2);
		avatarButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				makeVisible(context, ACCOUNTOPTION_AVATAR);
			}
		});
		ImageLabelButton pwdButton = new ImageLabelButton(context);
		pwdButton.setTextAndImageResource("Password", R.drawable.btnpassword);
		pwdButton.setLayoutParams(lpButtons2);
		pwdButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				makeVisible(context, ACCOUNTOPTION_PASSWORD);
			}
		});
		ImageLabelButton groupsButton = new ImageLabelButton(context);
		groupsButton.setTextAndImageResource("Groups", R.drawable.btngroups);
		groupsButton.setLayoutParams(lpButtons3);
		groupsButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				makeVisible(context, ACCOUNTOPTION_GROUPS);
			}
		});
		ImageLabelButton backupButton = new ImageLabelButton(context);
		backupButton.setTextAndImageResource("Backup/Restore",
				R.drawable.btnbackup);
		backupButton.setLayoutParams(lpButtons3);
		backupButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				makeVisible(context, ACCOUNTOPTION_BACKUP);
			}
		});

		buttonLayout1.addView(usernameButton);
		buttonLayout1.addView(smsButton);
		buttonLayout2.addView(avatarButton);
		buttonLayout2.addView(pwdButton);
		buttonLayout3.addView(groupsButton);
		buttonLayout3.addView(backupButton);
		buttonLayout.addView(buttonLayout1);
		buttonLayout.addView(buttonLayout2);
		buttonLayout.addView(buttonLayout3);
		accountoptionbuttons.addView(buttonLayout);
	}

	// ------------------------------------------------------------------------

	private static LinearLayout userparent = null;
	private static LinearLayout smsparent = null;
	private static LinearLayout avatarparent = null;
	private static LinearLayout passwordparent = null;
	private static LinearLayout backupparent = null;
	private static LinearLayout groupsparent = null;
	private static ScrollView mainscrollview = null;
	private static LinearLayout mainpanel = null;

	/**
	 * Link account options so that they can be modified by static metho
	 * update().
	 */
	private void linkAccountOptions() {
		userparent = (LinearLayout) findViewById(R.id.userparent);
		smsparent = (LinearLayout) findViewById(R.id.smsparent);
		avatarparent = (LinearLayout) findViewById(R.id.avatarparent);
		passwordparent = (LinearLayout) findViewById(R.id.passwordparent);
		backupparent = (LinearLayout) findViewById(R.id.backupparent);
		groupsparent = (LinearLayout) findViewById(R.id.groupsparent);
		mainscrollview = (ScrollView) findViewById(R.id.mainscrollview);
		mainpanel = (LinearLayout) findViewById(R.id.mainpanel);
	}

	/**
	 * Update visible hides all account options except one (accountOption). If
	 * accountOption < 1 then it hides all.
	 * 
	 * @param context
	 *            the context
	 * @param accountOption
	 *            the account option
	 */
	private static void updateVisible(int accountOption) {
		if (userparent != null) {
			if (accountOption != ACCOUNTOPTION_USERNAME) {
				userparent.setVisibility(View.GONE);
			} else {
				userparent.setVisibility(View.VISIBLE);
			}
		}
		if (smsparent != null) {
			if (accountOption != ACCOUNTOPTION_SMS) {
				smsparent.setVisibility(View.GONE);
			} else {
				smsparent.setVisibility(View.VISIBLE);
			}
		}
		if (avatarparent != null) {
			if (accountOption != ACCOUNTOPTION_AVATAR) {
				avatarparent.setVisibility(View.GONE);
			} else {
				avatarparent.setVisibility(View.VISIBLE);
			}
		}
		if (passwordparent != null) {
			if (accountOption != ACCOUNTOPTION_PASSWORD) {
				passwordparent.setVisibility(View.GONE);
			} else {
				passwordparent.setVisibility(View.VISIBLE);
			}
		}
		if (groupsparent != null) {
			if (accountOption != ACCOUNTOPTION_GROUPS) {
				groupsparent.setVisibility(View.GONE);
			} else {
				groupsparent.setVisibility(View.VISIBLE);
			}
		}
		if (backupparent != null) {
			if (accountOption != ACCOUNTOPTION_BACKUP) {
				backupparent.setVisibility(View.GONE);
			} else {
				backupparent.setVisibility(View.VISIBLE);
			}
		}
		if (mainscrollview != null && accountOption > 0) {
			mainscrollview.postDelayed(new Runnable() {
				public void run() {
					mainscrollview.scrollTo(0, mainpanel.getHeight());
				}
			}, 200);
		}
		if (mainscrollview != null && accountOption == 0) {
			mainscrollview.postDelayed(new Runnable() {
				public void run() {
					mainscrollview.scrollTo(0, 0);
				}
			}, 200);
		}
	}

	// ------------------------------------------------------------------------

	/**
	 * Scoll after login.
	 * 
	 * @param context
	 *            the context
	 */
	private static void scollAfterLogin(Context context) {
		if (mainscrollview != null) {
			mainscrollview.postDelayed(new Runnable() {
				public void run() {
					mainscrollview.scrollTo(0,
							accountexistinglogindata.getHeight());
				}
			}, 200);
		}
	}

	// ------------------------------------------------------------------------

	/**
	 * Make visible.
	 * 
	 * @param context
	 *            the context
	 * @param accountOption
	 *            the account option
	 */
	private void makeVisible(Context context, int accountOption) {
		if (!online) {
			Conversation
					.promptInfo(
							context,
							"Not Logged In",
							"You must login to modify your account settings on the server.\n\nClick on 'Validate / Login' first. You need an Internet connection to change your account settings.");
			updateVisible(0);
			return;
		}
		updateVisible(accountOption);
	}

	// ------------------------------------------------------------------------

	/**
	 * Update server spinner.
	 * 
	 * @param context
	 *            the context
	 */
	public static void updateServerSpinner(final Context context,
			final Spinner spinner) {
		List<String> spinnerArray = getServers(context);
		ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(
				context, android.R.layout.simple_spinner_item, spinnerArray);
		spinnerArrayAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(spinnerArrayAdapter);
	}

	// ------------------------------------------------------------------------
	// ------------------------------------------------------------------------

	/**
	 * Load setup settings.
	 * 
	 * @param context
	 *            the context
	 */
	@SuppressLint("InflateParams")
	private void loadSettings(final Context context) {
		// Create a view
		LayoutInflater serverTabInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		ScrollView settings = (ScrollView) serverTabInflater.inflate(
				R.layout.setup_settings, null);
		settingssection.addView(settings);

		active = (CheckBox) findViewById(R.id.active);
		encryption = (CheckBox) findViewById(R.id.encryption);
		tone = (CheckBox) findViewById(R.id.tone);
		vibrate = (CheckBox) findViewById(R.id.vibrate);
		ignore = (CheckBox) findViewById(R.id.ignore);
		notification = (CheckBox) findViewById(R.id.notification);
		autosave = (CheckBox) findViewById(R.id.autosave);
		noread = (CheckBox) findViewById(R.id.noread);
		chatmode = (CheckBox) findViewById(R.id.chatmode);
		quicktype = (CheckBox) findViewById(R.id.quicktype);
		smileys = (CheckBox) findViewById(R.id.smileys);
		darkmode = (CheckBox) findViewById(R.id.darkmode);
		receiveallsms = (CheckBox) findViewById(R.id.receiveallsms);
		noscreenshots = (CheckBox) findViewById(R.id.noscreenshots);
		powersave = (CheckBox) findViewById(R.id.powersave);

		ignoredspinner = (Spinner) findViewById(R.id.ignoredspinner);
		Button ignoredremove = (Button) findViewById(R.id.ignoredremove);
		ignoredremove.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				int index = ignoredspinner.getSelectedItemPosition();
				if (index >= 0) {
					String separatedString = Utility.loadStringSetting(context,
							UID_IGNORED, "");
					List<String> uids = Utility.getListFromString(
							separatedString, ";");
					if (uids.size() > index) {
						int uid = Utility.parseInt(uids.get(index), -1);
						if (uid != -1) {
							Setup.setIgnored(context, uid, false);
							Setup.updateIgnoreSpinner(context, ignoredspinner);
						}
					}
				}
			}
		});
		Setup.updateIgnoreSpinner(context, ignoredspinner);

		// other settings
		active.setChecked(Utility.loadBooleanSetting(context, OPTION_ACTIVE,
				DEFAULT_ACTIVE));
		active.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				setActive(context, active.isChecked());
				if (active.isChecked()) {
					// Start the scheduler
					Scheduler.reschedule(context, false, false, false);
				} else {
					// We do not have to end the scheduler, it will end
					// automatically...
				}
			}
		});
		tone.setChecked(Utility.loadBooleanSetting(context, Setup.OPTION_TONE,
				Setup.DEFAULT_TONE));
		tone.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				Utility.saveBooleanSetting(context, Setup.OPTION_TONE,
						tone.isChecked());
			}
		});
		vibrate.setChecked(Utility.loadBooleanSetting(context,
				Setup.OPTION_VIBRATE, true));
		vibrate.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				Utility.saveBooleanSetting(context, Setup.OPTION_VIBRATE,
						vibrate.isChecked());
			}
		});
		ignore.setChecked(Utility.loadBooleanSetting(context,
				Setup.OPTION_IGNORE, Setup.DEFAULT_IGNORE));
		ignore.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				Utility.saveBooleanSetting(context, Setup.OPTION_IGNORE,
						ignore.isChecked());
			}
		});
		notification.setChecked(Utility.loadBooleanSetting(context,
				Setup.OPTION_NOTIFICATION, Setup.DEFAULT_NOTIFICATION));
		notification.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				Utility.saveBooleanSetting(context, Setup.OPTION_NOTIFICATION,
						notification.isChecked());
			}
		});
		autosave.setChecked(Utility.loadBooleanSetting(context,
				Setup.OPTION_AUTOSAVE, Setup.DEFAULT_AUTOSAVE));
		autosave.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				Utility.saveBooleanSetting(context, Setup.OPTION_AUTOSAVE,
						autosave.isChecked());
			}
		});
		noread.setChecked(Utility.loadBooleanSetting(context,
				Setup.OPTION_NOREAD, Setup.DEFAULT_NOREAD));
		noread.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				if (!noread.isChecked()) {
					// we allowed it now, save the timestamp to just receive
					// read confirmations FROM NOW ON
					// this will hide ALL confirmations sent before this
					// time!
					Utility.saveStringSetting(context, "timestampread",
							DB.getTimestampString());
					Utility.saveBooleanSetting(context, Setup.OPTION_NOREAD,
							false);
				} else {
					noread.setChecked(false);
					askOnRefuseReadConfirmation(context);
				}
			}
		});
		chatmode.setChecked(Utility.loadBooleanSetting(context,
				Setup.OPTION_CHATMODE, Setup.DEFAULT_CHATMODE));
		chatmode.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				Utility.saveBooleanSetting(context, Setup.OPTION_CHATMODE,
						chatmode.isChecked());
			}
		});
		quicktype.setChecked(Utility.loadBooleanSetting(context,
				Setup.OPTION_QUICKTYPE, Setup.DEFAULT_QUICKTYPE));
		quicktype.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				Utility.saveBooleanSetting(context, Setup.OPTION_QUICKTYPE,
						quicktype.isChecked());
			}
		});
		smileys.setChecked(Utility.loadBooleanSetting(context,
				Setup.OPTION_SMILEYS, Setup.DEFAULT_SMILEYS));
		smileys.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				Utility.saveBooleanSetting(context, Setup.OPTION_SMILEYS,
						smileys.isChecked());
			}
		});
		darkmode.setChecked(Utility.loadBooleanSetting(context,
				Setup.OPTION_DARKMODE, Setup.DEFAULT_DARKMODE));
		darkmode.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				Utility.saveBooleanSetting(context, Setup.OPTION_DARKMODE,
						darkmode.isChecked());
				restartRequiredFlag = true;
				Utility.showToastAsync(
						context,
						"Background theme changed. CryptSecure must be re-started in order to operate properly...");
			}
		});
		noscreenshots.setChecked(Utility.loadBooleanSetting(context,
				Setup.OPTION_NOSCREENSHOTS, Setup.DEFAULT_NOSCREENSHOTS));
		noscreenshots.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				Utility.saveBooleanSetting(context, Setup.OPTION_NOSCREENSHOTS,
						noscreenshots.isChecked());
				if (!noscreenshots.isChecked()) {
					// allow screenshots, tell user to restart app!
					Conversation
							.promptInfo(
									context,
									"Allow Screenshots",
									"You may need to fully restart the App in order to make screenshots.\n\nIf this does not work, you may even need to restart your phone.\n\nConsider re-enabling this feature soon to protect your privacy!");
				}
			}
		});
		powersave.setChecked(Utility.loadBooleanSetting(context,
				Setup.OPTION_POWERSAVE, Setup.DEFAULT_POWERSAVE));
		powersave.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				Utility.saveBooleanSetting(context, Setup.OPTION_POWERSAVE,
						powersave.isChecked());
			}
		});
		encryption.setChecked(Utility.loadBooleanSetting(context,
				Setup.OPTION_ENCRYPTION, Setup.DEFAULT_ENCRYPTION));
		encryption.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				if (encryption.isChecked()) {
					enableEncryption(context);
					updateTitleIDInfo(context);
					Utility.saveBooleanSetting(context,
							Setup.OPTION_ENCRYPTION, true);
				} else {
					encryption.setChecked(true);
					askOnDisableEncryption(context);
				}
			}
		});
		receiveallsms.setChecked(isSMSDefaultApp(context, false));
		receiveallsms.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				if (receiveallsms.isChecked()) {
					// Enable Delphino CryptSecure as default messaging app
					setSMSDefaultApp(context, true);
				} else {
					receiveallsms.setChecked(true);
					promptDisableReceiveAllSms(context);
				}
			}
		});

		// help icon clicks
		helpactive = (ImageView) findViewById(R.id.helpactive);
		helpencryption = (ImageView) findViewById(R.id.helpencryption);
		helptone = (ImageView) findViewById(R.id.helptone);
		helpvibrate = (ImageView) findViewById(R.id.helpvibrate);
		helpnotification = (ImageView) findViewById(R.id.helpnotification);
		helpignore = (ImageView) findViewById(R.id.helpignore);
		helpautosave = (ImageView) findViewById(R.id.helpautosave);
		helpnoread = (ImageView) findViewById(R.id.helpnoread);
		helpchatmode = (ImageView) findViewById(R.id.helpchatmode);
		helpquicktype = (ImageView) findViewById(R.id.helpquicktype);
		helpsmileys = (ImageView) findViewById(R.id.helpsmileys);
		helpdarkmode = (ImageView) findViewById(R.id.helpnodarkmode);
		helpnoscreenshots = (ImageView) findViewById(R.id.helpnoscreenshots);
		helppowersave = (ImageView) findViewById(R.id.helppowersave);
		helpreceiveallsms = (ImageView) findViewById(R.id.helpreceiveallsms);
		helpactive.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				setErrorInfo(HELP_ACTIVE, false);
			}
		});
		helpencryption.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				setErrorInfo(HELP_ENCRYPTION, false);
			}
		});
		helptone.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				setErrorInfo(HELP_TONE, false);
			}
		});
		helpvibrate.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				setErrorInfo(HELP_VIBTRATE, false);
			}
		});
		helpnotification.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				setErrorInfo(HELP_NOTIFICATION, false);
			}
		});
		helpignore.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				setErrorInfo(HELP_IGNORE, false);
			}
		});
		helpautosave.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				setErrorInfo(HELP_AUTOSAVE, false);
			}
		});
		helpnoread.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				setErrorInfo(HELP_NOREAD, false);
			}
		});
		helpchatmode.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				setErrorInfo(HELP_CHATMODE, false);
			}
		});
		helpquicktype.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				setErrorInfo(HELP_QUICKTYPE, false);
			}
		});
		helpsmileys.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				setErrorInfo(HELP_SMILEY, false);
			}
		});
		helpdarkmode.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				setErrorInfo(HELP_DARKMODE, false);
			}
		});
		helpnoscreenshots.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				setErrorInfo(HELP_NOSCREENSHOTS, false);
			}
		});
		helppowersave.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				setErrorInfo(HELP_POWERSAVE, false);
			}
		});
		helpreceiveallsms.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				setErrorInfo(HELP_RECEIVEALLSMSE, false);
			}
		});

	}

	// ------------------------------------------------------------------------

	/**
	 * Load the current server tab. This is called when a server tab changes.
	 * 
	 * @param activity
	 *            the context
	 * @param serverId
	 *            the server id
	 */
	private void loadServerTab(final Activity activity, final int serverId) {
		if (serverId == -1) {
			// NO VALID SERVER SELECTED - HIDE EVERYTHING!
			accountnew = (LinearLayout) findViewById(R.id.accountnew);
			accountexisting = (LinearLayout) findViewById(R.id.accountexisting);
			accountexistinglogindata = (LinearLayout) findViewById(R.id.accountexistinglogindata);
			accountexisting.setVisibility(View.GONE);
			accountnew.setVisibility(View.GONE);
			online = false;
			updateonline();
			return;
		}
		// We may need the serverkey later... load it
		Setup.updateServerkey(activity, serverId, true);

		uid = (EditText) findViewById(R.id.uid);
		uid.setTag(uid.getKeyListener()); // remember the listener for later use
		uid.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				updateAccountLocked(activity, false, serverId);
			}
		});
		TextView textexisting2 = (TextView) findViewById(R.id.textexisting2);
		textexisting2.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				updateAccountLocked(activity, false, serverId);
			}
		});

		email = (EditText) findViewById(R.id.email);
		email.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				updateAccountLocked(activity, false, serverId);
			}
		});
		pwd = (EditText) findViewById(R.id.pwd);
		pwd.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				updateAccountLocked(activity, false, serverId);
			}
		});

		usernew = (EditText) findViewById(R.id.usernew);
		emailnew = (EditText) findViewById(R.id.emailnew);
		pwdnew = (EditText) findViewById(R.id.pwdnew);
		phone = (EditText) findViewById(R.id.phone);

		pwdchange = (EditText) findViewById(R.id.pwdchange);

		accountnew = (LinearLayout) findViewById(R.id.accountnew);
		accountexisting = (LinearLayout) findViewById(R.id.accountexisting);
		accountexistinglogindata = (LinearLayout) findViewById(R.id.accountexistinglogindata);
		accountonline = (LinearLayout) findViewById(R.id.accountonline);
		newaccount = (CheckBox) findViewById(R.id.newaccount);
		user = (EditText) findViewById(R.id.user);

		create = (Button) findViewById(R.id.create);
		login = (Button) findViewById(R.id.login);

		updatepwd = (Button) findViewById(R.id.updatepwd);
		updateuser = (Button) findViewById(R.id.updateuser);
		backup = (Button) findViewById(R.id.backup);
		restore = (Button) findViewById(R.id.restore);

		enablesmsoption = (Button) findViewById(R.id.enablesmsoption);
		disablesmsoption = (Button) findViewById(R.id.disablesmsoption);

		try {
			if (accountType) {
				newaccount
						.setOnCheckedChangeListener(new OnCheckedChangeListener() {
							public void onCheckedChanged(
									CompoundButton buttonView, boolean isChecked) {
								setErrorInfo(null);
								if (newaccount.isChecked()) {
									accountexisting.setVisibility(View.GONE);
									accountnew.setVisibility(View.VISIBLE);

									online = false;
									updateonline();
								} else {
									accountexisting.setVisibility(View.VISIBLE);
									accountnew.setVisibility(View.GONE);
								}

								updateonline();
							}
						});
			}

			String uidString = Utility.loadStringSetting(activity, SERVER_UID
					+ serverId, "");
			boolean noAccountYet = uidString.equals("");
			uid.setText(uidString);
			newaccount.setChecked(true);
			newaccount.setChecked(false);
			newaccount.setChecked(noAccountYet);

			if (!noAccountYet) {
				this.accountLocked = 3;
			} else {
				this.accountLocked = 0;
			}

			updateAccountLocked(activity, true, serverId);

			user.setText(Utility.loadStringSetting(activity, SERVER_USERNAME
					+ serverId, ""));
			email.setText(Utility.loadStringSetting(activity, SERVER_EMAIL
					+ serverId, ""));
			pwd.setText(Utility.loadStringSetting(activity, SERVER_PWD
					+ serverId, ""));
			pwd.setInputType(InputType.TYPE_CLASS_TEXT
					| InputType.TYPE_TEXT_VARIATION_PASSWORD);

			pwdnew.setInputType(InputType.TYPE_CLASS_TEXT
					| InputType.TYPE_TEXT_VARIATION_PASSWORD);
			pwdchange.setInputType(InputType.TYPE_CLASS_TEXT
					| InputType.TYPE_TEXT_VARIATION_PASSWORD);

			create.setOnClickListener(new OnClickListener() {
				public void onClick(View arg0) {
					setErrorInfo(null);
					createNewAccount(activity, serverId);
				}
			});

			login.setOnClickListener(new OnClickListener() {
				public void onClick(View arg0) {
					setErrorInfo(null);
					validate(activity, serverId);
				}
			});

			updateuser.setOnClickListener(new OnClickListener() {
				public void onClick(View arg0) {
					setErrorInfo(null);
					updateUsername(activity, serverId);
				}
			});

			updatepwd.setOnClickListener(new OnClickListener() {
				public void onClick(View arg0) {
					setErrorInfo(null);
					updatePwd(activity, serverId);
				}
			});

			backup.setOnClickListener(new OnClickListener() {
				public void onClick(View arg0) {
					setErrorInfo(null);
					backup(activity, false, true, serverId);
				}
			});
			restore.setOnClickListener(new OnClickListener() {
				public void onClick(View arg0) {
					setErrorInfo(null);
					restore(activity, true, serverId);
				}
			});

			updatePhoneNumberAndButtonStates(activity, serverId);
			enablesmsoption.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					// enable or update?
					updateSMSOption(activity, true, serverId);
					backup(activity, true, false, serverId);
				}
			});
			disablesmsoption.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					// enable or update?
					updateSMSOption(activity, false, serverId);
				}
			});

			avatar = Setup.getAvatar(activity, DB.myUid(activity, serverId));
			Button avatarclear = (Button) findViewById(R.id.avatarclear);
			avatarclear.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					avatar = null;
					updateAvatarView(activity);
				}
			});
			updateAvatarView(activity);
			Button avatarphoto = (Button) findViewById(R.id.avatarphoto);
			avatarphoto.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					Utility.takePhoto(activity);
				}
			});
			Button avatargallery = (Button) findViewById(R.id.avatargallery);
			avatargallery.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					Utility.selectFromGallery(activity);
				}
			});
			updateavatar = (Button) findViewById(R.id.updateavatar);
			updateavatar.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					backup(activity, true, false, serverId);

					if (avatar == null) {
						// Do not prompt if the user deletes its avatar
						updateAvatar(activity, serverId, avatar);
						return;
					}

					try {
						final String titleMessage = "Publish Avatar?";
						final String textMessage = "Avatars are always sent/received unencrypted to/from the server to enhance performance!\n\nYou should not use private photos as avatars if you have any concerns.\n\nDo you still want to send your avatar to the server?";
						new MessageAlertDialog(activity, titleMessage,
								textMessage, " Publish ", " Cancel ", null,
								new MessageAlertDialog.OnSelectionListener() {
									public void selected(int button,
											boolean cancel) {
										if (!cancel) {
											if (button == 0) {
												updateAvatar(activity,
														serverId, avatar);
											}
										}
									}
								}).show();
					} catch (Exception e) {
						// Ignore
					}
				}
			});

			groupspinner = (Spinner) findViewById(R.id.groupspinner);
			Setup.updateGroupSpinner(activity, groupspinner);

			Button groupcreate = (Button) findViewById(R.id.groupcreate);
			groupcreate.setOnClickListener(new OnClickListener() {
				public void onClick(View arg0) {
					String titleMessage = "New Group";
					String textMessage = "Enter the name of the new group to create on this server:";
					new MessageInputDialog(activity, titleMessage, textMessage,
							"Create", "       Cancel      ", null, "",
							new MessageInputDialog.OnSelectionListener() {
								public void selected(MessageInputDialog dialog,
										int button, boolean cancel,
										String returnText) {
									if (!cancel) {
										if (button == 0) {
											if (returnText.trim().length() > 0) {
												groupCreate(activity, serverId,
														returnText);
											} else {
												setErrorInfoAsync(
														activity,
														"Error creating group '"
																+ returnText
																+ "'. No valid group name.",
														true);
											}
										}
									}
									dialog.dismiss();
								}
							}, InputType.TYPE_CLASS_TEXT, true, true).show();
				}
			});

			Button grouprename = (Button) findViewById(R.id.grouprename);
			grouprename.setOnClickListener(new OnClickListener() {
				public void onClick(View arg0) {
					int index = groupspinner.getSelectedItemPosition();

					final int groupServerId = groupSpinnerMappingServer
							.get(index);
					final String groupId = groupSpinnerMappingGroupId
							.get(index);
					final String oldName = Setup.getGroupName(activity,
							serverId, groupId);

					String titleMessage = "Rename Group";
					String textMessage = "Enter the new name for the group '"
							+ oldName + "':";
					new MessageInputDialog(activity, titleMessage, textMessage,
							"Rename", "       Cancel      ", null, oldName,
							new MessageInputDialog.OnSelectionListener() {
								public void selected(MessageInputDialog dialog,
										int button, boolean cancel,
										String returnText) {
									if (!cancel) {
										if (button == 0) {
											if (returnText.trim().length() > 0
													&& !oldName
															.equals(returnText)) {
												groupRename(activity,
														groupServerId, groupId,
														returnText);
											} else {
												setErrorInfoAsync(
														activity,
														"Error renaming group '"
																+ returnText
																+ "'. No valid group name.",
														true);
											}
										}
									}
									dialog.dismiss();
								}
							}, InputType.TYPE_CLASS_TEXT, true, true).show();
				}
			});

			Button groupquit = (Button) findViewById(R.id.groupquit);
			groupquit.setOnClickListener(new OnClickListener() {
				public void onClick(View arg0) {
					int index = groupspinner.getSelectedItemPosition();

					final int groupServerId = groupSpinnerMappingServer
							.get(index);
					final String groupId = groupSpinnerMappingGroupId
							.get(index);
					int localGroupId = Setup.getLocalGroupId(activity, groupId,
							groupServerId);
					Setup.promptQuit(activity, localGroupId);

				}
			});

		} catch (Exception e) {
			e.printStackTrace();
		}

		updateServerImage(activity, false, false);
	}

	// ------------------------------------------------------------------------

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
					Bitmap bitmap = Utility.getBitmapFromContentUri(this,
							data.getData());
					avatar = Utility.getResizedImageAsBASE64String(this,
							bitmap, 100, 100, 80, true);
					updateAvatarView(this);
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
				Bitmap bitmap = (Bitmap) data.getExtras().get("data");
				avatar = Utility.getResizedImageAsBASE64String(this, bitmap,
						100, 100, 80, true);
				updateAvatarView(this);
			}
		}
	}

	// -------------------------------------------------------------------------

	/**
	 * Update the avatar if the user has logged in / validate before.
	 * 
	 * @param context
	 *            the context
	 */
	void updateAvatar(final Context context, final int serverId,
			final String avatar) {
		updateavatar.setEnabled(false);
		setErrorInfo(null);

		String avatarEnc = avatar; // Setup.encLongText(context, avatar,
		// serverId);
		if (avatarEnc == null) {
			avatarEnc = "";
		}
		avatarEnc = Utility.urlEncode(avatarEnc);

		String session = Setup.getTmpLoginEncoded(context, serverId);
		if (session == null) {
			setErrorInfo("Session error. Try again after restarting the app.");
			updateuser.setEnabled(true);
			// error resume is automatically done by getTmpLogin, not logged in
			return;
		}

		String url = null;
		url = Setup.getBaseURL(context, serverId) + "cmd=updateavatar&session="
				+ session + "&val=" + avatarEnc;
		Log.d("communicator", "UPDATE ENC: " + url);
		final String url2 = url;
		@SuppressWarnings("unused")
		HttpStringRequest httpStringRequest = (new HttpStringRequest(context,
				url2, new HttpStringRequest.OnResponseListener() {
					public void response(String response) {
						if (Communicator.isResponseValid(response)) {
							final String response2 = response;
							if (Communicator.isResponsePositive(response2)) {
								Setup.saveAvatar(context,
										DB.myUid(context, serverId), avatar,
										true);
							}
							final Handler mUIHandler = new Handler(
									Looper.getMainLooper());
							mUIHandler.post(new Thread() {
								@Override
								public void run() {
									super.run();
									updateavatar.setEnabled(true);
									// UPDATE DISPLAY HERE
									if (Communicator
											.isResponsePositive(response2)) {
										// EVERYTHING OK
										setErrorInfo("Avatar changed.", false);
									} else {
										if (response2.equals("-44")) {
											// email already registered
											setErrorInfo("The server "
													+ Setup.getServerLabel(
															context, serverId,
															true)
													+ " does not allow to upload avatars. You can still use local avatars for you userlist.");
										} else if (response2.equals("-22")) {
											// email already registered
											setErrorInfo("Transmission error, please try again.");
										} else {
											setErrorInfo("Login failed.");
											online = false;
											updateonline();
										}
									}
								}
							});
						} else {
							final Handler mUIHandler = new Handler(Looper
									.getMainLooper());
							mUIHandler.post(new Thread() {
								@Override
								public void run() {
									super.run();
									updateavatar.setEnabled(true);
									setErrorInfo(
											"Server error or no internet connection. Try again later.",
											true);
								}
							});
						}
					}
				}));

	}

	// -------------------------------------------------------------------------

	/**
	 * Update avatar if there is a valied one BASE64 encoded in the avatar
	 * String field.
	 */
	private void updateAvatarView(Context context) {
		ImageView avatarView = (ImageView) findViewById(R.id.avatarimage);

		// Button avatarclear = (Button) findViewById(R.id.avatarclear);
		// if (avatar != null) {
		// avatarclear.setText(avatar.length() + "");
		// } else {
		// avatarclear.setText("0");
		// }

		if (avatar == null || avatar.length() == 0) {
			avatarView.setImageResource(R.drawable.person);
		} else {
			try {
				Bitmap bitmap = Utility.loadImageFromBASE64String(context,
						avatar);
				avatarView.setImageBitmap(bitmap);
			} catch (Exception e) {
				avatarView.setImageResource(R.drawable.person);
				avatar = null;
			}
		}
	}

	// ------------------------------------------------------------------------

	/**
	 * Update server image for the selected server id.
	 * 
	 * @param context
	 *            the context
	 */
	private void updateServerImage(Context context,
			boolean userClickedActivate, boolean forceRefresh) {

		boolean active = isServerActive(context, selectedServerId, forceRefresh);
		boolean haveaccount = isServerAccount(context, selectedServerId,
				forceRefresh);

		if (active && haveaccount) {
			if (serverdisabled != null) {
				serverdisabled.setVisibility(View.GONE);
				if (serverdisabled.isChecked()) {
					serverdisabled.setChecked(false);
				}
			}
			if (serverimage != null) {
				serverimage.setImageResource(R.drawable.server);
			}
		} else {
			if (userClickedActivate) {
				// The user wanted to activate but actually the server is
				// inactive again
				// prompt the user to tell him...
				Conversation
						.promptInfo(
								context,
								"Server Not Active",
								"You need a valid account and an "
										+ "Internet connection in order to activate a server.\n\nIf you do not have an "
										+ "account for this server yet, create one first below. After that, try to "
										+ "activate the server again.");
			}
			if (serverdisabled != null) {
				serverdisabled.setVisibility(View.VISIBLE);
				if (!serverdisabled.isChecked()) {
					serverdisabled.setChecked(true);
				}
			}
			if (serverimage != null) {
				serverimage.setImageResource(R.drawable.serverdisabled);
			}
		}

		// Update the title as well
		String uidString2 = Utility.loadStringSetting(context, Setup.SERVER_UID
				+ selectedServerId, "");

		String serverAddress = getServerLabel(context, selectedServerId, false);

		if (uidString2 != null & uidString2.trim().length() > 0) {
			uidString2 = " - UID " + uidString2; // +
			if (serverAddress.length() > 0) {
				uidString2 += " @ " + serverAddress;
			}
		} else {
			uidString2 = " - no account";
			if (serverAddress.length() > 0) {
				uidString2 += " @ " + serverAddress;
			}
		}

		if (!accountType) {
			setTitle("Settings" + uidString2);
		} else {
			setTitle("Account" + uidString2);
		}

	}

	// ------------------------------------------------------------------------

	/**
	 * This method simply ensures that the server URL ends with "/?" in order to
	 * add parameters for a request.
	 * 
	 * @param serverUrl
	 *            the server url
	 * @return the string
	 */
	private static String ensureServerURLWithRequestEnding(String serverAddress) {
		if (!serverAddress.endsWith("?")) {
			if (!serverAddress.endsWith("/")) {
				serverAddress = serverAddress + "/";
			}
			serverAddress = serverAddress + "?";
		}
		return serverAddress;
	}

	// ------------------------------------------------------------------------

	/**
	 * Gets the base url of a server for a serverId. Typically this is
	 * cryptsecure.org if not another server is configured.
	 * 
	 * @param context
	 *            the context
	 * @return the base url
	 */
	public static String getBaseURL(Context context, int serverId) {
		if (!BASESERVERADDRESSCACHED.containsKey(serverId)) {
			String serverAddress = getServer(context, serverId);
			if (serverAddress != null && serverAddress.trim().length() > 3) {
				serverAddress = ensureServerURLWithRequestEnding(serverAddress);
				BASESERVERADDRESSCACHED.put(serverId, serverAddress);
			}
		}
		if (serverId == -1 || !BASESERVERADDRESSCACHED.containsKey(serverId)) {
			return DEFAULT_SERVER + "/?";
		}
		return BASESERVERADDRESSCACHED.get(serverId);
	}

	// ------------------------------------------------------------------------

	/**
	 * Sets the title of the custom title bar of the setup activity.
	 * 
	 * @param title
	 *            the new title
	 */
	public void setTitle(String title) {
		TextView titletext = (TextView) findViewById(R.id.titletext);
		titletext.setText(title);
	}

	// -------------------------------------------------------------------------

	/**
	 * Update account locked. Count down the number of times the user clicks on
	 * the account login information. If it was more than the initial
	 * accountLocked valued (we count down to 0), then unlock the user
	 * information text input fields but prompt a warning.
	 * 
	 * @param context
	 *            the context
	 * @param silent
	 *            the silent
	 */
	private void updateAccountLocked(Context context, boolean silent,
			int serverId) {
		String uidString = Utility.loadStringSetting(context, SERVER_UID
				+ serverId, "");
		boolean noAccountYet = uidString.equals("");

		if (accountLocked > 0) {
			login.setText("   Validate / Login   ");
			newaccount.setEnabled(false);
			uid.setFocusable(false);
			uid.setFocusableInTouchMode(false);
			email.setFocusable(false);
			email.setFocusableInTouchMode(false);
			pwd.setFocusable(false);
			pwd.setFocusableInTouchMode(false);
			uid.setClickable(true);
			email.setClickable(true);
			pwd.setClickable(true);
			uid.setTextColor(Color.parseColor("#FF666666"));
			email.setTextColor(Color.parseColor("#FF666666"));
			pwd.setTextColor(Color.parseColor("#FF666666"));
			uid.setOnKeyListener(new View.OnKeyListener() {
				public boolean onKey(View v, int keyCode, KeyEvent event) {
					return true;
				}
			});
			email.setOnKeyListener(new View.OnKeyListener() {
				public boolean onKey(View v, int keyCode, KeyEvent event) {
					return true;
				}
			});
			pwd.setOnKeyListener(new View.OnKeyListener() {
				public boolean onKey(View v, int keyCode, KeyEvent event) {
					return true;
				}
			});
			if (!silent) {
				Utility.showToastShortAsync(context, accountLocked
						+ " more clicks to edit the login information.");
				accountLocked = accountLocked - 1;
			}
		} else if (accountLocked == 0) {
			online = false;
			updateonline();

			login.setText("   Validate / Save   ");
			accountLocked = accountLocked - 1;
			if (!noAccountYet) {
				// Only warn if there exists already an account
				Conversation
						.promptInfo(
								context,
								"Changing Login Information",
								"You are about to change the login information!\n\nIt is very crucial that you only change your login information if this is really required! All local data on you phone is linked to your account. If you switch to another account (user) you should clear all your contacts beforehand. Also note that your old account key becomes invalid and others might get into trouble sending you encrypted messages!\n\nDo not change your login information unless you really know what you are doing!");
			}

			newaccount.setEnabled(true);
			uid.setTextColor(Color.parseColor("#FFDDDDDD"));
			email.setTextColor(Color.parseColor("#FFDDDDDD"));
			pwd.setTextColor(Color.parseColor("#FFDDDDDD"));
			uid.setOnKeyListener(new View.OnKeyListener() {
				public boolean onKey(View v, int keyCode, KeyEvent event) {
					return false;
				}
			});
			email.setOnKeyListener(new View.OnKeyListener() {
				public boolean onKey(View v, int keyCode, KeyEvent event) {
					return false;
				}
			});
			pwd.setOnKeyListener(new View.OnKeyListener() {
				public boolean onKey(View v, int keyCode, KeyEvent event) {
					return false;
				}
			});
			uid.setFocusable(true);
			uid.setFocusableInTouchMode(true);
			uid.setEnabled(true);
			email.setFocusable(true);
			email.setFocusableInTouchMode(true);
			email.setEnabled(true);
			pwd.setFocusable(true);
			pwd.setFocusableInTouchMode(true);
			pwd.setEnabled(true);
		}
	}

	// -------------------------------------------------------------------------

	/**
	 * Update title id info. The title info field always displayes the DeviceID
	 * and the current RSA account key to the user.
	 * 
	 * @param context
	 *            the context
	 */
	public static void updateTitleIDInfo(Context context) {
		deviceid.setText("DeviceID: " + getDeviceId(context)
				+ "   --   Account Key: " + Setup.getPublicKeyHash(context));
		if (ownaccountkey != null) {
			ownaccountkey.removeAllViews();
			ownaccountkey.addView(Main.getAccountKeyView(context, DB.myUid(),
					"YOUR ACCOUNT KEY", true));
		}
	}

	// -------------------------------------------------------------------------

	private void promptAddServer(final Context context) {
		String titleMessage = "New Message Server";
		String textMessage = "Enter the URL of the new message server:";
		new MessageInputDialog(context, titleMessage, textMessage, "Add",
				"       Cancel      ", null, "http://www.yourserverurl",
				new MessageInputDialog.OnSelectionListener() {
					public void selected(MessageInputDialog dialog, int button,
							boolean cancel, String returnText) {
						if (!cancel) {
							if (button == 0) {
								testServerPingBeforeAdd(context, returnText);
								updateServerSpinner(context, serverspinner);
							}
						}
						dialog.dismiss();
					}
				}, 0x00000011, true, true).show();
	}

	// -------------------------------------------------------------------------

	/**
	 * Test server before add. First do a ping then a post ping.
	 * 
	 * @param context
	 *            the context
	 */
	private void testServerPingBeforeAdd(final Context context,
			final String serverUrlToAdd) {
		final String randomPingToPong = Utility.getRandomString(5);

		String url = ensureServerURLWithRequestEnding(serverUrlToAdd)
				+ "cmd=ping&val=" + randomPingToPong;
		final String url2 = url;
		@SuppressWarnings("unused")
		HttpStringRequest httpStringRequest = (new HttpStringRequest(context,
				url2, new HttpStringRequest.OnResponseListener() {
					public void response(String response) {
						if (response.endsWith(randomPingToPong)) {
							// That was our ping... okay, now test the same for
							// the POST
							String url = ensureServerURLWithRequestEnding(serverUrlToAdd)
									+ "cmd=pingpost&val=" + randomPingToPong;
							HttpStringRequest httpStringRequest = (new HttpStringRequest(
									context, url2, true,
									new HttpStringRequest.OnResponseListener() {
										public void response(String response) {
											if (response
													.endsWith(randomPingToPong)) {
												// That was our ping... okay, we
												// can add the server
												final Handler mUIHandler = new Handler(
														Looper.getMainLooper());
												mUIHandler.post(new Thread() {
													@Override
													public void run() {
														addServer(context,
																serverUrlToAdd);
														Utility.showToastAsync(
																context,
																"Server '"
																		+ serverUrlToAdd
																		+ "' added.");
														updateServerSpinner(
																context,
																serverspinner);
													}
												});
											} else {
												// No luck with pinging using
												// POST, do not add
												final Handler mUIHandler = new Handler(
														Looper.getMainLooper());
												mUIHandler.post(new Thread() {
													@Override
													public void run() {
														Utility.copyToClipboard(
																context,
																serverUrlToAdd);
														Utility.showToastAsync(
																context,
																"Server '"
																		+ serverUrlToAdd
																		+ "' did not answer to POST request."
																		+ " Please check URL. Maybe a 'www.' "
																		+ "is missing?  URL copied to Clipboard.");
													}
												});
											}
										}
									}));
						} else {
							// No luck with pinging using GET, do not add
							final Handler mUIHandler = new Handler(Looper
									.getMainLooper());
							mUIHandler.post(new Thread() {
								@Override
								public void run() {
									Utility.copyToClipboard(context,
											serverUrlToAdd);
									Utility.showToastAsync(
											context,
											"Server '"
													+ serverUrlToAdd
													+ "' did not answer to GET request."
													+ " Please check URL. URL copied to Clipboard.");
								}
							});
						}
					}
				}));

	}

	// -------------------------------------------------------------------------

	private void promptDeleteServer(final Context context) {
		String titleMessage = "Delete Message Server";
		String textMessage = "!!! WARNING !!!\n\nYou are about to delete a message server. If you proceed all users and conversations linked to this server will be removed from your userlist! Additionally your login data for this server will be cleared!\n\nDo you still want to proceed and delete this data?";
		new MessageAlertDialog(context, titleMessage, textMessage,
				"Still Do it!", "       Cancel      ", null,
				new MessageAlertDialog.OnSelectionListener() {
					public void selected(int button, boolean cancel) {
						if (!cancel) {
							if (button == 0) {
								if (selectedServerId == getServerId(DEFAULT_SERVER)) {
									Conversation
											.promptInfo(
													context,
													"Cannot Delete Server",
													"You cannot delete this (default) server.\n\nBut"
															+ " you can disable it by long pressing the blue"
															+ " server image.");
								} else {
									String serverLabel = getServerLabel(
											context, selectedServerId, true);
									removeServer(context, selectedServerId);
									updateServerSpinner(context, serverspinner);
									Utility.showToastAsync(context, "Server '"
											+ serverLabel + "' removed.");
								}
							}
						}
					}
				}).show();
	}

	// -------------------------------------------------------------------------

	/**
	 * Prompt to change server list. This is basically a WARNING.
	 * 
	 * @param context
	 *            the context
	 */
	public void promptModifyServer(final Context context) {
		String titleMessage = "Changing Message Server";
		String textMessage = "ATTENTION: You are about to modify the message server list. You should only proceed if you know what you are doing!\n\nIf you delete or modify an existing server then all the users and conversations you added from this server will be removed. Additionally your locally stored account login information will be cleared.\n\nDo you really want to modify the server list?";
		new MessageAlertDialog(context, titleMessage, textMessage,
				"Still Do it!", "       Cancel      ", null,
				new MessageAlertDialog.OnSelectionListener() {
					public void selected(int button, boolean cancel) {
						if (!cancel) {
							if (button == 0) {
								serverbuttonmodify.setVisibility(View.GONE);
								serverbuttonadd.setVisibility(View.VISIBLE);
								serverbuttondelete.setVisibility(View.VISIBLE);
							}

						}
					}
				}).show();
	}

	// ------------------------------------------------------------------------

	/**
	 * Update phone number and button states.
	 * 
	 * @param context
	 *            the context
	 */
	private static void updatePhoneNumberAndButtonStates(Context context,
			int serverId) {
		if (phone == null || enablesmsoption == null
				|| disablesmsoption == null) {
			// not visible
			return;
		}
		if (!isSMSOptionEnabled(context, serverId)) {
			// Not saved phone number
			phone.setText(Utility.getPhoneNumber(context));
			// disabled
			enablesmsoption.setEnabled(true);
			enablesmsoption.setText(" Enable SMS ");
			disablesmsoption.setEnabled(false);
		} else {
			phone.setText(Utility.loadStringSetting(context, SETTINGS_PHONE
					+ serverId, ""));
			// enabled
			enablesmsoption.setEnabled(true);
			enablesmsoption.setText(" Update ");
			disablesmsoption.setEnabled(true);
		}
	}

	// -------------------------------------------------------------------------

	/**
	 * Checks if is SMS mode on.
	 * 
	 * @param context
	 *            the context
	 * @param hostUid
	 *            the host uid
	 * @return true, if is SMS mode on
	 */
	public static boolean isSMSModeOn(Context context, int hostUid) {
		return (hostUid < 0 || Utility.loadBooleanSetting(context,
				Setup.OPTION_SMSMODE + hostUid, Setup.DEFAULT_SMSMODE));
	}

	// -------------------------------------------------------------------------

	/**
	 * Sets the error info asynchronously in the UI thread. Should be used if
	 * not in the ui thread.
	 * 
	 * @param message
	 *            the message
	 * @param isError
	 *            the is error
	 */
	static void setErrorInfoAsync(final Context context, final String message,
			final boolean isError) {
		final Handler mUIHandler = new Handler(Looper.getMainLooper());
		mUIHandler.post(new Thread() {
			@Override
			public void run() {
				super.run();
				setErrorInfo(context, message, isError);
			}
		});
	}

	/**
	 * Sets the error info.
	 * 
	 * @param errorMessage
	 *            the new error info
	 */
	void setErrorInfo(String errorMessage) {
		setErrorInfo(this, errorMessage, true);
	}

	/**
	 * Sets the error info.
	 * 
	 * @param message
	 *            the message
	 * @param isError
	 *            the is error
	 */
	void setErrorInfo(String message, boolean isError) {
		setErrorInfo(this, message, isError);
	}

	/**
	 * Sets the error info.
	 * 
	 * @param context
	 *            the context
	 * @param errorMessage
	 *            the error message
	 */
	static void setErrorInfo(Context context, String errorMessage) {
		setErrorInfo(context, errorMessage, true);
	}

	/**
	 * Sets the error info.
	 * 
	 * @param context
	 *            the context
	 * @param message
	 *            the message
	 * @param isError
	 *            the is error
	 */
	static void setErrorInfo(final Context context, String message,
			boolean isError) {
		if (error == null || info == null) {
			// Not visible only send this as toast
			if (message != null && message.length() > 0) {
				Utility.showToastAsync(context, message);
			}
			return;
		}
		if (message == null || message.length() == 0) {
			error.setVisibility(View.GONE);
			error.setText("");
			info.setVisibility(View.GONE);
			info.setText("");
		} else {
			if (isError) {
				error.setVisibility(View.VISIBLE);
				error.setText(message);
				error.requestFocus();
			} else {
				info.setVisibility(View.VISIBLE);
				info.setText(message);
				info.requestFocus();
			}
			info.postDelayed(new Runnable() {
				public void run() {
					// Hide the info after 3 sec + 1 sec per 20 characters
					setErrorInfo(context, null, false);
				}
			}, 3000 + (1000 * (message.length() / 20)));
			if (uid != null) {
				uid.requestFocus();
				Utility.hideKeyboardExplicit(uid);
			}
		}
	}

	// -------------------------------------------------------------------------

	/**
	 * Updateonline if the login/validation was successful or hide account
	 * modifications (change pw, change username, enable/disable sms option) in
	 * case online is false.
	 */
	static void updateonline() {
		updateVisible(0);
		if (accountonline == null) {
			// not visible, nothing to do
			return;
		}
		if (!online) {
			accountonline.setVisibility(View.GONE);
		} else {
			accountonline.setVisibility(View.VISIBLE);
		}
	}

	// ------------------------------------------------------------------------

	/**
	 * Validate the login. This will also set online:=true in the success case.
	 * If the account login information was change we here detect an UID change
	 * and remove the key from server and shutdown the app.
	 * 
	 * @param context
	 *            the context
	 */
	void validate(final Context context, final int serverId) {
		Log.d("communicator", "#### UPDATE 2 :" + serverId);

		Setup.updateServerkey(context, serverId, true);

		final String uidBefore = Utility.loadStringSetting(context, SERVER_UID
				+ serverId, "");

		login.setEnabled(false);
		setErrorInfo(null);
		String uidString = "";
		final int tmpUid = Utility
				.parseInt(uid.getText().toString().trim(), -1);
		if (tmpUid > 0) {
			uidString = tmpUid + "";
		} else {
			uid.setText("");
		}
		String emailString = email.getText().toString().trim();
		String pwdString = pwd.getText().toString().trim();

		if ((emailString.length() == 0 && uidString.length() == 0)
				|| pwdString.length() == 0) {
			setErrorInfo("You must provide a password in combination of a UID or email!");
			login.setEnabled(true);
			return;
		}

		// DETECT UID=CHANGE ===> EXIT
		// APPLICATION!
		if (!uidBefore.equals(uidString)) {
			Setup.disableEncryption(context, false);
			updateCurrentMid(context, serverId);
		}

		Utility.saveStringSetting(context, SERVER_UID + serverId, uidString);
		cachedServerAccount.put(serverId, uidString.trim().length() > 0);
		Utility.saveStringSetting(context, SERVER_EMAIL + serverId, emailString);
		Utility.saveStringSetting(context, SERVER_PWD + serverId, pwdString);

		LoginData loginData = calculateLoginVal(context, uidString,
				emailString, pwdString, serverId);
		if (loginData == null) {
			setErrorInfo("Encryption error (1), try again or restart the app.");
			login.setEnabled(true);
			return;
		}

		// RSA encode
		PublicKey serverKey = getServerkey(context, serverId);
		String uidStringEnc = Communicator.encryptServerMessage(context,
				uidString, serverKey);
		if (uidStringEnc == null) {
			setErrorInfo("Encryption error (2). Try again or restart the app.");
			login.setEnabled(true);
			return;
		}
		uidStringEnc = Utility.urlEncode(uidStringEnc);
		String emailStringEnd = Communicator.encryptServerMessage(context,
				emailString, serverKey);
		if (emailStringEnd == null) {
			setErrorInfo("Encryption error (3). Try again or restart the app.");
			login.setEnabled(true);
			return;
		}
		emailStringEnd = Utility.urlEncode(emailStringEnd);

		String reseturl = null;
		String reactivateurl = null;
		String url = Setup.getBaseURL(context, serverId) + "cmd=validate&val1="
				+ loginData.user + "&val2=" + loginData.password + "&val3="
				+ loginData.val;
		if (!uidString.equals("")) {
			reseturl = Setup.getBaseURL(context, serverId)
					+ "cmd=resetpwd&uid=" + uidStringEnc;
			reactivateurl = Setup.getBaseURL(context, serverId)
					+ "cmd=resendactivation&uid=" + uidStringEnc;
		} else {
			reseturl = Setup.getBaseURL(context, serverId)
					+ "cmd=resetpwd&email=" + emailStringEnd;
			reactivateurl = Setup.getBaseURL(context, serverId)
					+ "cmd=resendactivation&email=" + emailStringEnd;
		}
		Log.d("communicator", "LOGIN VALIDATE: " + url);

		final String url2 = url;
		final String reseturl2 = reseturl;
		final String reactivateurl2 = reactivateurl;
		@SuppressWarnings("unused")
		HttpStringRequest httpStringRequest = (new HttpStringRequest(context,
				url2, new HttpStringRequest.OnResponseListener() {
					public void response(String response) {
						Log.d("communicator", "###### VALIDATE: response="
								+ response);
						if (Communicator.isResponseValid(response)) {
							Log.d("communicator",
									"###### VALIDATE: response positive="
											+ response);
							String emailR = "";
							String uidR = "";
							String usernameR = "";
							final String response2 = response;
							final boolean positiveResponse = Communicator
									.isResponsePositive(response);
							if (positiveResponse) {
								String responseContent = Communicator
										.getResponseContent(response);
								Log.d("communicator",
										"###### VALIDATE: responseContent="
												+ responseContent);
								// the return value should be the new created
								// UID
								String[] array = responseContent.split("#");
								if (array != null && array.length == 5) {
									String sessionID = array[0];
									String loginErrCnt = array[1];
									updateSuccessfullLogin(context, sessionID,
											loginErrCnt, serverId);
									uidR = decUid(context, array[2], serverId)
											+ "";
									emailR = decText(context, array[3],
											serverId);
									usernameR = decText(context, array[4],
											serverId);
									// no invalid but also no unknown users
									if ((!uidR.equals("-1"))
											&& (!uidR.equals("-2"))
											&& emailR != null
											&& usernameR != null) {
										Utility.saveStringSetting(context,
												SERVER_UID + serverId, uidR);
										Utility.saveStringSetting(context,
												SERVER_EMAIL + serverId, emailR);
										Utility.saveStringSetting(context,
												SERVER_USERNAME + serverId,
												usernameR);
										// DETECT UID=CHANGE ===> EXIT
										// APPLICATION!
										if (!uidBefore.equals(uidR)) {
											restartRequiredFlag = true;
											Utility.showToastAsync(
													context,
													"Account/UID changed. CryptSecure must be re-started in order to operate properly...");
										}
									} else {
										uidR = tmpUid + "";
										emailR = email.getText().toString()
												.trim();
										usernameR = Utility.loadStringSetting(
												context, SERVER_USERNAME
														+ serverId, "");
									}
								}
							}
							final String email2 = emailR;
							final String uid2 = uidR;
							final String username2 = usernameR;
							final Handler mUIHandler = new Handler(Looper
									.getMainLooper());
							mUIHandler.post(new Thread() {
								@Override
								public void run() {
									super.run();
									login.setEnabled(true);
									// UPDATE DISPLAY HERE
									if (positiveResponse) {
										// EVERYTHING OK
										email.setText(email2);
										uid.setText(uid2);
										user.setText(username2);
										Utility.saveStringSetting(context,
												SERVER_UID + serverId, uid2);
										Utility.saveStringSetting(context,
												SERVER_USERNAME + serverId,
												username2);
										if (tmpUid > 0) {
											Main.saveUID2Name(context, tmpUid,
													username2);
										}
										pwdchange.setText("");
										online = true;
										updateVisible(0);
										updateonline();
										// IF LOGIN IS SUCCESSFULL => ENABLE
										// SERVER!
										enableServer(context, true, true);

										setErrorInfo(
												"Login successfull.\n\nYou can now edit your username, enable sms support, change your avatar or your password, modify group membership, or backup or restore your user list below.",
												false);
										scollAfterLogin(context);
									} else {
										if (response2.equals("-4")) {
											// Email already registered
											setErrorInfo("You account has not been activated yet. Go to your email inbox and follow the activation link! Be sure to also look in the spam email folder.\n\nIf you cannot find the activation email, click the following link to resend it to you:\n"
													+ reactivateurl2);
										} else if (response2.equals("-11")) {
											// Email already registered
											setErrorInfo("You new password has not been activated yet. Go to your email inbox and follow the activation link! Be sure to also look in the spam email folder.\n\nIf you cannot find the activation email, you can reset your password once more here:\n"
													+ reseturl2);
										} else {
											// Clear server key to enforce a
											// soon reload!
											Utility.saveStringSetting(context,
													Setup.SETTINGS_SERVERKEY
															+ serverId, null);

											Utility.saveStringSetting(context,
													SERVER_PWD + serverId, "");
											setErrorInfo("Login failed. \n\nIf you don't know your password, click the following link to reset it:\n"
													+ reseturl2);
										}
									}
								}
							});
						} else {
							// Clear server key to enforce a soon reload!
							Utility.saveStringSetting(context,
									Setup.SETTINGS_SERVERKEY, null);

							final Handler mUIHandler = new Handler(Looper
									.getMainLooper());
							mUIHandler.post(new Thread() {
								@Override
								public void run() {
									super.run();
									login.setEnabled(true);
									setErrorInfo(
											"Server error or no internet connection. Try again later.",
											true);
								}
							});
						}
					}
				}));

	}

	// ------------------------------------------------------------------------

	/**
	 * Update the username if the user has logged in / validate before.
	 * 
	 * @param context
	 *            the context
	 */
	void updateUsername(final Context context, final int serverId) {
		updateuser.setEnabled(false);
		setErrorInfo(null);
		final String usernameString = user.getText().toString();

		// RSA encode
		PublicKey serverKey = getServerkey(context, serverId);
		String usernameStringEnc = Communicator.encryptServerMessage(context,
				usernameString, serverKey);
		if (usernameStringEnc == null) {
			setErrorInfo("Encryption error. Try again or restart the app.");
			updateuser.setEnabled(true);
			return;
		}
		usernameStringEnc = Utility.urlEncode(usernameStringEnc);

		String session = Setup.getTmpLoginEncoded(context, serverId);
		if (session == null) {
			setErrorInfo("Session error. Try again after restarting the app.");
			updateuser.setEnabled(true);
			// error resume is automatically done by getTmpLogin, not logged in
			return;
		}

		String url = null;
		url = Setup.getBaseURL(context, serverId) + "cmd=updateuser&session="
				+ session + "&user=" + usernameStringEnc;
		Log.d("communicator", "UPDATE USERNAME: " + url);
		final String url2 = url;
		@SuppressWarnings("unused")
		HttpStringRequest httpStringRequest = (new HttpStringRequest(context,
				url2, new HttpStringRequest.OnResponseListener() {
					public void response(String response) {
						if (Communicator.isResponseValid(response)) {
							final String response2 = response;
							if (Communicator.isResponsePositive(response2)) {
								Utility.saveStringSetting(context,
										SERVER_USERNAME + serverId,
										usernameString);
							}
							final Handler mUIHandler = new Handler(
									Looper.getMainLooper());
							mUIHandler.post(new Thread() {
								@Override
								public void run() {
									super.run();
									updateuser.setEnabled(true);
									// UPDATE DISPLAY HERE
									if (Communicator
											.isResponsePositive(response2)) {
										// EVERYTHING OK
										setErrorInfo("Username changed.", false);
									} else {
										if (response2.equals("-5")) {
											// email already registered
											setErrorInfo("Update username failed!");
										} else if (response2.equals("-13")) {
											setErrorInfo("Username too or long short. It should consist of at least 2 (valid) up to 16 characters!");
										} else if (response2.equals("-16")) {
											setErrorInfo("Username contains invalid characters!");
										} else {
											setErrorInfo("Login failed.");
											online = false;
											updateonline();
										}
									}
								}
							});
						} else {
							final Handler mUIHandler = new Handler(Looper
									.getMainLooper());
							mUIHandler.post(new Thread() {
								@Override
								public void run() {
									super.run();
									updateuser.setEnabled(true);
									setErrorInfo(
											"Server error or no internet connection. Try again later.",
											true);
								}
							});
						}
					}
				}));

	}

	// ------------------------------------------------------------------------

	/**
	 * Update pwd if the user has logged in/validate before. On success this
	 * will update the saved password. The new password must be activated
	 * afterwards by clicking on the link in the email. Otherwise the old
	 * password will not change. But due to the fact that the new password is
	 * already saved the user is forced to 1. click on the link in the email to
	 * activate the new password or alternatively 2. reenter the old password.
	 * 
	 * @param context
	 *            the context
	 */
	void updatePwd(final Context context, final int serverId) {
		updatepwd.setEnabled(false);
		setErrorInfo(null);
		final String usernameString = user.getText().toString();
		final String pwdChangeString = pwdchange.getText().toString();

		// RSA encode
		PublicKey serverKey = getServerkey(context, serverId);
		String pwdChangeStringEnc = Communicator.encryptServerMessage(context,
				pwdChangeString, serverKey);
		if (pwdChangeStringEnc == null) {
			setErrorInfo("Encryption error. Try again or restart the app.");
			updatepwd.setEnabled(true);
			return;
		}
		pwdChangeStringEnc = Utility.urlEncode(pwdChangeStringEnc);

		String session = Setup.getTmpLoginEncoded(context, serverId);
		if (session == null) {
			setErrorInfo("Session error. Try again after restarting the app.");
			updatepwd.setEnabled(true);
			// error resume is automatically done by getTmpLogin, not logged in
			return;
		}

		String url = null;
		url = Setup.getBaseURL(context, serverId) + "cmd=updatepwd&session="
				+ session + "&val=" + pwdChangeStringEnc;
		Log.d("communicator", "UPDATE PWD: " + url);

		final String url2 = url;
		@SuppressWarnings("unused")
		HttpStringRequest httpStringRequest = (new HttpStringRequest(context,
				url2, new HttpStringRequest.OnResponseListener() {
					public void response(String response) {
						if (Communicator.isResponseValid(response)) {
							final String response2 = response;
							if (Communicator.isResponsePositive(response2)) {
								Utility.saveStringSetting(context,
										SERVER_USERNAME + serverId,
										usernameString);
							}
							final Handler mUIHandler = new Handler(
									Looper.getMainLooper());
							mUIHandler.post(new Thread() {
								@Override
								public void run() {
									super.run();
									updatepwd.setEnabled(true);
									// UPDATE DISPLAY HERE
									if (Communicator
											.isResponsePositive(response2)) {
										// EVERYTHING OK
										// save the new password
										Utility.saveStringSetting(context,
												SERVER_PWD + serverId,
												pwdChangeString);
										pwd.setText(pwdChangeString);
										pwdchange.setText("");
										setErrorInfo(
												"Password changed. Check your email to activate the new password!",
												false);
										// Must log out! User must verify link
										// in mail to activate password!
										online = false;
										updateonline();
									} else {
										if (response2.equals("-6")) {
											// email already registered
											setErrorInfo("Update password failed!");
										} else if (response2.equals("-14")) {
											pwdchange.setText("");
											setErrorInfo("Password too or long short. It should consist of at least 6 (valid) up to 16 characters!");
										} else if (response2.equals("-15")) {
											pwdchange.setText("");
											setErrorInfo("Password contains invalid characters!");
										} else {
											pwdchange.setText("");
											setErrorInfo("Login failed.");
											online = false;
											updateonline();
										}
									}
								}
							});
						} else {
							final Handler mUIHandler = new Handler(Looper
									.getMainLooper());
							mUIHandler.post(new Thread() {
								@Override
								public void run() {
									super.run();
									updatepwd.setEnabled(true);
									setErrorInfo(
											"Server error or no internet connection. Try again later.",
											true);
								}
							});
						}
					}
				}));

	}

	// ------------------------------------------------------------------------

	/**
	 * Creates a new account. It disables encryption (so the user is prompted on
	 * next restart of the app to enable encryption). After creating the account
	 * it needs to be activated which can only be done by clicking on the link
	 * in the email. If the account is not activated it is useless and the
	 * server is allowed to clear un-activated accounts after a while.
	 * 
	 * @param context
	 *            the context
	 */
	void createNewAccount(final Context context, final int serverId) {
		Setup.updateServerkey(context, serverId, true);

		create.setEnabled(false);
		setErrorInfo(null);
		// String uidString = uid.getText().toString();
		String emailString = emailnew.getText().toString().trim();
		String usernameString = usernew.getText().toString().trim();
		String pwdString = pwdnew.getText().toString();

		// RSA encode
		PublicKey serverKey = getServerkey(context, serverId);

		emailString = Communicator.encryptServerMessage(context, emailString,
				serverKey);
		if (emailString == null) {
			setErrorInfo("Encryption error. Have you specified a valid email address? Try again or restart the app.");
			create.setEnabled(true);
			return;
		}
		emailString = Utility.urlEncode(emailString);

		pwdString = Communicator.encryptServerMessage(context, pwdString,
				serverKey);
		if (pwdString == null) {
			setErrorInfo("Encryption error. Have you specified a valid password? Try again or restart the app.");
			create.setEnabled(true);
			return;
		}
		pwdString = Utility.urlEncode(pwdString);

		usernameString = Communicator.encryptServerMessage(context,
				usernameString, serverKey);
		if (usernameString == null) {
			setErrorInfo("Encryption error. Have you specified a valid username? Try again after restart the app.");
			create.setEnabled(true);
			return;
		}
		usernameString = Utility.urlEncode(usernameString);

		String url = null;
		String reseturl = null;
		url = Setup.getBaseURL(context, serverId) + "cmd=create&email="
				+ emailString + "&pwd=" + pwdString + "&user=" + usernameString;
		reseturl = Setup.getBaseURL(context, serverId) + "cmd=resetpwd&email="
				+ emailString;
		Log.d("communicator", "CREATE ACCOUNt: " + url);

		final String url2 = url;
		final String reseturl2 = reseturl;
		@SuppressWarnings("unused")
		HttpStringRequest httpStringRequest = (new HttpStringRequest(context,
				url2, new HttpStringRequest.OnResponseListener() {
					public void response(String response) {
						final String response2 = response;
						if (Communicator.isResponseValid(response)) {
							final String newUID = Communicator
									.getResponseContent(response);
							if (Communicator.isResponsePositive(response)) {
								// the return value should be the new created
								// UID
								Utility.saveStringSetting(context, SERVER_UID
										+ serverId, newUID);
								cachedServerAccount.put(serverId, true);
							} else {
								cachedServerAccount.put(serverId, false);
							}
							final Handler mUIHandler = new Handler(Looper
									.getMainLooper());
							mUIHandler.post(new Thread() {
								@Override
								public void run() {
									super.run();
									create.setEnabled(true);
									// UPDATE DISPLAY HERE
									if (Communicator
											.isResponsePositive(response2)) {
										// EVERYTHING OK
										uid.setText(newUID);
										email.setText(emailnew.getText()
												.toString());
										pwd.setText(pwdnew.getText().toString());

										// Utility.saveStringSetting(context,
										// "uid", uidString);
										String emailString = emailnew.getText()
												.toString().trim();
										String usernameString = usernew
												.getText().toString().trim();
										String pwdString = pwdnew.getText()
												.toString();
										Utility.saveStringSetting(context,
												SERVER_USERNAME + serverId,
												usernameString);
										Utility.saveStringSetting(context,
												SERVER_EMAIL + serverId,
												emailString);
										Utility.saveStringSetting(context,
												SERVER_PWD + serverId,
												pwdString);

										Utility.saveBooleanSetting(context,
												Setup.OPTION_ENCRYPTION, false);
										saveHaveAskedForEncryption(context,
												false);

										newaccount.setChecked(false);
										updateCurrentMid(context, serverId);
										setErrorInfo(
												"Registration successfull!\n\nYOUR NEW UNIQUE UID IS: "
														+ newUID
														+ "\n\nGo to your email account and follow the activation link we sent you. Be sure to also check your spam email folder.",
												false);
										Utility.showToastAsync(
												context,
												"Your new UID is "
														+ newUID
														+ ". CryptSecure must be re-started in order to operate properly...");
										restartRequiredFlag = true;
									} else {
										if (response2.equals("-17")) {
											setErrorInfo("This server currently does not permit any new registrations.");
										} else if (response2.equals("-2")) {
											setErrorInfo("Email address already taken.\n\nHave you activated your account already? If not go to your email account and follow the activation link we sent you.\n\nIf this is your address and you cannot find the activation email then reset the password, otherwise use a different address.\n\nTo reset the password click here:\n"
													+ reseturl2);
										} else if (response2.equals("-12")) {
											setErrorInfo("Email address is not valid. You cannot use this address for registration.");
										} else if (response2.equals("-13")) {
											setErrorInfo("Username too or long short. It should consist of at least 2 (valid) up to 16 characters!");
										} else if (response2.equals("-14")) {
											setErrorInfo("Password too or long short. It should consist of at least 6 (valid) up to 16 characters!");
										} else if (response2.equals("-15")) {
											setErrorInfo("Password contains invalid characters!");
										}
									}
								}
							});
						} else {
							final Handler mUIHandler = new Handler(Looper
									.getMainLooper());
							mUIHandler.post(new Thread() {
								@Override
								public void run() {
									super.run();
									create.setEnabled(true);
									setErrorInfo(
											"Server error or no internet connection. Try again later.",
											true);
								}
							});
						}
					}
				}));

	}

	// ------------------------------------------------------------------------

	/**
	 * Update current highest mid. This is necessary to receive (request) the
	 * correct next newest messages from the server. After an UID change or
	 * after creating a new account the current highest mid is fetched from the
	 * server.
	 * 
	 * @param context
	 *            the context
	 */
	public static void updateCurrentMid(final Context context,
			final int serverId) {
		String url = null;
		url = Setup.getBaseURL(context, serverId) + "cmd=mid";
		final String url2 = url;
		@SuppressWarnings("unused")
		HttpStringRequest httpStringRequest = (new HttpStringRequest(context,
				url2, new HttpStringRequest.OnResponseListener() {
					public void response(String response) {
						if (Communicator.isResponseValid(response)) {
							String content = Communicator
									.getResponseContent(response);
							if (content != null) {
								DB.resetLargestMid(context,
										Utility.parseInt(content, -1), serverId);
								Log.d("communicator",
										"XXXX SAVED LARGEST MID '" + content
												+ "'");
							}
						}
					}
				}));
	}

	// ------------------------------------------------------------------------
	// ------------------------------------------------------------------------

	/**
	 * Try autobackup userlist for all server.
	 * 
	 * @param context
	 *            the context
	 */
	public static void autobackupAllServer(final Context context) {
		for (int serverId : Setup.getServerIds(context)) {
			if (Setup.isServerAccount(context, serverId, false)) {
				autobackup(context, serverId);
			}
		}
	}

	// ------------------------------------------------------------------------

	/**
	 * Try autobackup userlist for a specific server (if the last successfull
	 * autobackup is long enough ago).
	 * 
	 * @param context
	 *            the context
	 * @param serverId
	 *            the server id
	 */
	public static void autobackup(final Context context, int serverId) {
		// public static final int UPDATE_AUTOBACKUP_MIN_INTERVAL = 1200 * 60 *
		// 1000; // 10
		// public static final String SETTING_LASTUPDATEAUTOBACKUP =
		// "lastupdateautobackup";
		long lastTime = Utility.loadLongSetting(context,
				Setup.SETTING_LASTUPDATEAVATAR + serverId, 0);
		long currentTime = DB.getTimestamp();
		if ((lastTime + Setup.UPDATE_AVATAR_MIN_INTERVAL + serverId > currentTime)) {
			// Do not do this more frequently
			return;
		}
		// The following call will, on success, save the current time!
		backup(context, true, false, serverId);
	}

	// ------------------------------------------------------------------------

	/**
	 * Backup the userlist to the server. There is a manual and an automatic
	 * backup. The automatic is used if SMS option is turned on in order do
	 * validate if someone else is allowed to download/see our phone number.
	 * Only registered users will be backed up. Only the UIDs will be backed up
	 * no display name or phone number will be send to the server!
	 * 
	 * @param context
	 *            the context
	 * @param silent
	 *            the silent
	 * @param manual
	 *            the manual
	 */
	public static void backup(final Context context, final boolean silent,
			final boolean manual, final int serverId) {
		if (backup != null) {
			backup.setEnabled(false);
		}
		if (!silent) {
			setErrorInfo(context, null);
		}

		// String userlistString = Utility.loadStringSetting(context,
		// SETTINGS_USERLIST, "");
		List<Integer> uidList = Main.loadUIDList(context);

		if (uidList.size() == 0 && manual) {
			if (!silent) {
				setErrorInfo(context, "No users in your list yet!");
			}
			if (backup != null) {
				backup.setEnabled(true);
			}
			return;
		}

		// We MUST encrypt each UID individually because the message gets too
		// long otherwise for RSA encryption
		String userlistString = "";
		for (int uid : uidList) {
			// Only backup registered users that belong to this server for now!
			// Additionally only update users that are NOT auto-added!
			// Only users that belong to this server
			if (uid >= 0) {
				if (!Setup.isAutoAdded(context, uid)) {
					final int serverIdOfHost = Setup.getServerId(context, uid);
					if (serverIdOfHost == serverId) {
						final int suid = Setup.getSUid(context, uid);
						String uidEnc = Setup.encUid(context, suid, serverId);
						if (userlistString.length() > 0) {
							userlistString += "#";
						}
						userlistString += uidEnc;

					}
				}
			}
		}

		// // RSA encode
		// PublicKey serverKey = getServerkey(context);
		// String userlistStringEnc = Communicator.encryptServerMessage(context,
		// userlistString, serverKey);
		// if (userlistStringEnc == null) {
		// setErrorInfo(context,
		// "Encryption error. Try again after restarting the App.");
		// backup.setEnabled(true);
		// return;
		// }
		String userlistStringEnc = Utility.urlEncode(userlistString);

		String session = Setup.getTmpLoginEncoded(context, serverId);
		if (session == null) {
			setErrorInfo(context,
					"Session error. Try again after restarting the App.");
			if (backup != null) {
				backup.setEnabled(true);
			}
			// error resume is automatically done by getTmpLogin, not logged in
			return;
		}

		String url = null;
		url = Setup.getBaseURL(context, serverId) + "cmd=backup&session="
				+ session + "&val=" + userlistStringEnc;
		if (manual) {
			url = Setup.getBaseURL(context, serverId)
					+ "cmd=backupmanual&session=" + session + "&val="
					+ userlistStringEnc;
		}

		Log.d("communicator", "BACKUP: " + url);

		final String url2 = url;
		@SuppressWarnings("unused")
		HttpStringRequest httpStringRequest = (new HttpStringRequest(context,
				url2, new HttpStringRequest.OnResponseListener() {
					public void response(String response) {
						if (Communicator.isResponseValid(response)) {
							final String response2 = response;
							final Handler mUIHandler = new Handler(
									Looper.getMainLooper());
							mUIHandler.post(new Thread() {
								@Override
								public void run() {
									super.run();
									if (backup != null) {
										backup.setEnabled(true);
									}
									// UPDATE DISPLAY HERE
									if (Communicator
											.isResponsePositive(response2)) {

										if (!manual) {
											long currentTime = DB
													.getTimestamp();
											// Save the time so we do the
											// autobackup not too often!
											Utility.saveLongSetting(
													context,
													Setup.SETTING_LASTUPDATEAVATAR
															+ serverId,
													currentTime);
										}

										// EVERYTHING OK
										if (!silent) {
											setErrorInfo(
													context,
													"Backup of user list to server successful. You can now later restore it, e.g., on a different device or in case of data loss.\n\nNote that no messages are backed up.\n\nAlso note that ONLY registered users of THIS server are backed up and only their UID not their display name or phone number was saved at the server!",
													false);
										}
									} else {
										if (!silent) {
											setErrorInfo(context,
													"Backup of user list failed. Try again later.");
										}
										online = false;
										updateonline();
									}
								}
							});
						} else {
							final Handler mUIHandler = new Handler(Looper
									.getMainLooper());
							mUIHandler.post(new Thread() {
								@Override
								public void run() {
									super.run();
									if (backup != null) {
										backup.setEnabled(true);
									}
									if (!silent) {
										setErrorInfo(context,
												"Backup of user list failed. Try again later.");
										online = false;
										updateonline();
									}
								}
							});
						}
					}
				}));

	}

	// ------------------------------------------------------------------------

	/**
	 * Restore the userlist. Again, there is a manual and an automatic version.
	 * 
	 * @param context
	 *            the context
	 * @param manual
	 *            the manual
	 */
	void restore(final Context context, boolean manual, final int serverId) {
		restore.setEnabled(false);
		setErrorInfo(null);

		String session = Setup.getTmpLoginEncoded(context, serverId);
		if (session == null) {
			setErrorInfo(context,
					"Session error. Try again after restarting the App.");
			restore.setEnabled(true);
			// error resume is automatically done by getTmpLogin, not logged in
			return;
		}

		String url = null;
		url = Setup.getBaseURL(context, serverId) + "cmd=restore&session="
				+ session;
		if (manual) {
			url = Setup.getBaseURL(context, serverId)
					+ "cmd=restoremanual&session=" + session;
		}
		final String url2 = url;
		@SuppressWarnings("unused")
		HttpStringRequest httpStringRequest = (new HttpStringRequest(context,
				url2, new HttpStringRequest.OnResponseListener() {
					public void response(String response) {
						if (Communicator.isResponseValid(response)) {
							final String response2 = response;
							final Handler mUIHandler = new Handler(
									Looper.getMainLooper());
							mUIHandler.post(new Thread() {
								@Override
								public void run() {
									super.run();
									restore.setEnabled(true);
									// UPDATE DISPLAY HERE
									if (Communicator
											.isResponsePositive(response2)) {
										String responseContent = Communicator
												.getResponseContent(response2);
										if (responseContent == null) {
											responseContent = "";
										}
										responseContent = decText(context,
												responseContent, serverId);
										if (responseContent == null) {
											responseContent = "";
										}
										// EVERYTHING OK
										// CONVERT SUIDs to local UIDs
										List<Integer> possiblyNewUIDs = new ArrayList<Integer>();
										for (String suidString : responseContent
												.split("#")) {
											int suid = Utility.parseInt(
													suidString, -1);
											if (suid != -1) {
												int uid = Setup.getUid(context,
														suid, serverId);
												possiblyNewUIDs.add(uid);
											}
										}
										List<Integer> updateList = Main
												.loadUIDList(context);
										updateList.addAll(possiblyNewUIDs);
										Main.saveUIDList(context, updateList);
										if (Setup.isSMSOptionEnabled(context,
												serverId)) {
											Setup.backup(context, true, false,
													serverId);
										}
										setErrorInfo(
												"Restore of user list from server successful. Users from backup where added to your existing local userlist.",
												false);
									} else {
										setErrorInfo("Restore of user list failed. Try again later.");
										online = false;
										updateonline();
									}
								}
							});
						} else {
							final Handler mUIHandler = new Handler(Looper
									.getMainLooper());
							mUIHandler.post(new Thread() {
								@Override
								public void run() {
									super.run();
									setErrorInfo("Restore of user list failed. Try again later.");
									online = false;
									updateonline();
									restore.setEnabled(true);
								}
							});
						}
					}
				}));

	}

	// -------------------------------------------------------------------------
	// -------------------------------------------------------------------------

	/**
	 * Update SMS option. If turning SMS option on then also backup the current
	 * userlist to the server (automatic). Send the phone number to the server.
	 * If disabling turn off automatic backup and clear the phone number from
	 * the server. <BR>
	 * <BR>
	 * If SMS option is disabled, also clear all automatically downloaded phone
	 * numbers!
	 * 
	 * @param context
	 *            the context
	 * @param enable
	 *            the enable
	 */
	public static void updateSMSOption(final Context context,
			final boolean enable, final int serverId) {
		if (enablesmsoption != null) {
			enablesmsoption.setEnabled(false);
		}
		setErrorInfo(context, null);
		String phoneString = "";
		if (phone != null) {
			phoneString = phone.getText().toString(); // Utility.loadStringSetting(context,
			// Setup.SETTINGS_PHONE,
			// "");
		} else {
			// the case if this is called not from the dialog but from
			// Conversation
			phoneString = Utility.getPhoneNumber(context);
		}
		if (phoneString == null) {
			phoneString = "";
		}
		// only check valid number for enabling/update
		phoneString = normalizePhone(phoneString);
		final String phoneString2 = phoneString;
		if (!Utility.isValidPhoneNumber(phoneString) && enable) {
			setErrorInfo(
					context,
					"No valid phone number provided! Phone number must be in the following format:\n+491711234567");
			if (enablesmsoption != null) {
				enablesmsoption.setEnabled(true);
			}
			return;
		}
		if (!enable) {
			phoneString = "delete";
		}
		if (!enable) {
			// Remove all automatically downloaed phone numbers! Only users that
			// enable the SMS option themselves are eligable to do an
			// autoupdate.
			for (Integer uid : Main.loadUIDList(context)) {
				if (!Setup.isPhoneModified(context, uid)) {
					// Only for registered users we will get here
					// We fake "manual" here because after disabling the SMS
					// option the user can only manually edit a phone number
					// anyways.
					Setup.savePhone(context, uid, null, true);
				}
			}
		}

		// RSA encode
		PublicKey serverKey = getServerkey(context, serverId);
		String phoneStringEnc = Communicator.encryptServerMessage(context,
				phoneString, serverKey);
		if (phoneStringEnc == null) {
			setErrorInfo(context,
					"Encryption error. Try again or restart the app.");
			enablesmsoption.setEnabled(true);
			return;
		}
		phoneStringEnc = Utility.urlEncode(phoneStringEnc);

		String session = Setup.getTmpLoginEncoded(context, serverId);
		if (session == null) {
			setErrorInfo(context,
					"Session error. Try again or restart the app.");
			enablesmsoption.setEnabled(true);
			// error resume is automatically done by getTmpLogin, not logged in
			return;
		}

		String url = null;
		url = Setup.getBaseURL(context, serverId) + "cmd=updatephone&session="
				+ session + "&val=" + phoneStringEnc;
		final String url2 = url;
		@SuppressWarnings("unused")
		HttpStringRequest httpStringRequest = (new HttpStringRequest(context,
				url2, new HttpStringRequest.OnResponseListener() {
					public void response(String response) {
						if (Communicator.isResponseValid(response)) {
							final String response2 = response;
							final Handler mUIHandler = new Handler(
									Looper.getMainLooper());
							mUIHandler.post(new Thread() {
								@Override
								public void run() {
									super.run();
									if (enablesmsoption != null) {
										enablesmsoption.setEnabled(true);
									}
									// UPDATE DISPLAY HERE
									if (Communicator
											.isResponsePositive(response2)) {
										// EVERYTHING OK
										if (enable) {
											Utility.saveStringSetting(context,
													SETTINGS_PHONE + serverId,
													phoneString2);
										} else {
											Utility.saveStringSetting(context,
													SETTINGS_PHONE + serverId,
													"");
										}
										updatePhoneNumberAndButtonStates(
												context, serverId);
										if (enable) {
											setErrorInfo(
													context,
													"SMS option is enabled. Your phone number was updated on the server. Users from your userlist can now send you secure SMS!\n\nAttention: You are advised to check [x] Ignore Unknown Users in the settings to ensure your phone number can just be retrieved by persons you know.",
													false);
										} else {
											setErrorInfo(
													context,
													"SMS option is now disabled. Your phone number was removed from the server.",
													false);
										}

									} else {
										setErrorInfo(context,
												"Failed to update your phone number on server. Try again later.");
										online = false;
										updateonline();
									}
								}
							});
						} else {
							final Handler mUIHandler = new Handler(Looper
									.getMainLooper());
							mUIHandler.post(new Thread() {
								@Override
								public void run() {
									super.run();
									if (enablesmsoption != null) {
										enablesmsoption.setEnabled(true);
									}
									online = false;
									updateonline();
								}
							});
						}
					}
				}));
	}

	// -------------------------------------------------------------------------
	// -------------------------------------------------------------------------

	/**
	 * Sets the active flag for the background send/receive service.
	 * 
	 * @param context
	 *            the context
	 * @param active
	 *            the active
	 */
	public static void setActive(Context context, boolean active) {
		Utility.saveBooleanSetting(context, Setup.OPTION_ACTIVE, active);
		if (active) {
			Scheduler.reschedule(context, false, true, false);
		}
	}

	// -------------------------------------------------------------------------

	/**
	 * Checks if the background send/receive service is active.
	 * 
	 * @param context
	 *            the context
	 * @return true, if is active
	 */
	public static boolean isActive(Context context) {
		return Utility.loadBooleanSetting(context, Setup.OPTION_ACTIVE,
				Setup.DEFAULT_ACTIVE);
	}

	// -------------------------------------------------------------------------

	/**
	 * Gets the error update interval that is calculated based on the
	 * consecutive errors that might have occurred.
	 * 
	 * @param context
	 *            the context
	 * @return the error update interval
	 */
	public static int getErrorUpdateInterval(Context context) {
		return Utility.loadIntSetting(context, ERROR_TIME_TO_WAIT,
				Setup.ERROR_UPDATE_INTERVAL);
	}

	// -------------------------------------------------------------------------

	/**
	 * Sets or recalculates the error update interval which might increase after
	 * many consecutive errors up to a maximum. If error is false, then the
	 * interval is reset to its default minimal interval.
	 * 
	 * @param context
	 *            the context
	 * @param error
	 *            the error
	 */
	public static void setErrorUpdateInterval(Context context, boolean error) {
		if (!error) {
			// in the non-error case, reset the counter
			Utility.saveIntSetting(context, ERROR_TIME_TO_WAIT,
					Setup.ERROR_UPDATE_INTERVAL);
		} else {
			// flag a new error and possibly increment the counter
			int currentCounter = getErrorUpdateInterval(context);
			if (Main.isVisible() || Conversation.isVisible()) {
				// if the conversation is visible then do not increment!!! This
				// is just for not wasting energy on standby or if the user
				// is not in this app!
				Utility.saveIntSetting(context, ERROR_TIME_TO_WAIT,
						Setup.ERROR_UPDATE_INTERVAL);
			} else if (currentCounter < ERROR_UPDATE_MAXIMUM) {
				// add 20%
				currentCounter += ((currentCounter * ERROR_UPDATE_INCREMENT) / 100);
				Utility.saveIntSetting(context, ERROR_TIME_TO_WAIT,
						currentCounter);
			}
		}
	}

	// -------------------------------------------------------------------------

	/**
	 * Checks if is user active.
	 * 
	 * @return true, if is user active
	 */
	// Returns true if the user is supposed to be actively using the program
	public static boolean isUserActive() {
		return (Conversation.isVisible() || Main.isVisible());
	}

	// -------------------------------------------------------------------------

	/**
	 * Enable encryption.
	 * 
	 * @param context
	 *            the context
	 */
	@SuppressLint({ "DefaultLocale", "TrulyRandom" })
	public static void enableEncryption(Context context) {
		Utility.saveBooleanSetting(context, Setup.OPTION_ENCRYPTION, true);

		// First look if there is a key saved (we only delete keys if the user
		// manually disables encryption!)
		String pubk = Utility.loadStringSetting(context, Setup.PUBRSAKEY, null);
		String privk = Utility.loadStringSetting(context, Setup.PRIVATERSAKEY,
				null);
		boolean haveOldKeys = (pubk != null && privk != null
				&& pubk.length() > 0 && privk.length() > 0);

		// Generate key pair for 1024-bit RSA encryption and decryption
		try {
			String encodedpublicKey = pubk;

			if (!haveOldKeys) {
				Key publicKey = null;
				Key privateKey = null;
				KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
				kpg.initialize(1024); // 2048
				KeyPair kp = kpg.genKeyPair();
				publicKey = kp.getPublic();
				encodedpublicKey = Base64.encodeToString(
						publicKey.getEncoded(), Base64.DEFAULT);
				encodedpublicKey = encodedpublicKey.replace("\n", "").replace(
						"\r", "");
				privateKey = kp.getPrivate();
				String encodedprivateKey = Base64.encodeToString(
						privateKey.getEncoded(), Base64.DEFAULT);
				Utility.saveStringSetting(context, Setup.PUBRSAKEY,
						encodedpublicKey);
				Utility.saveStringSetting(context, Setup.PRIVATERSAKEY,
						encodedprivateKey);
			} else {
				encodedpublicKey = pubk;
			}

			// Renew keydate in all cases because the server saves its own time!
			Setup.setKeyDate(context, DB.myUid(), DB.getTimestampString());

			Log.d("communicator", "###### encodedpublicKey=" + encodedpublicKey);

			// Send public key to all servers
			String keyHash = getPublicKeyHash(context);
			for (int serverId : getServerIds(context)) {
				if (Setup.isServerAccount(context, serverId, true)) {
					Communicator.sendKeyToServer(context, encodedpublicKey,
							keyHash, serverId, false);
				}
			}

			promptEnabledEncryption(context);

		} catch (Exception e) {
			Log.e("communicator", "RSA key pair error");
		}

	}

	// -------------------------------------------------------------------------

	/**
	 * Send OUR current public RSA key to server.
	 * 
	 * @param context
	 *            the context
	 * @param serverId
	 *            the server id
	 */
	public boolean sendCurrentKeyToServer(Context context, int serverId,
			boolean silent) {
		String encodedpublicKey = Utility.loadStringSetting(context,
				Setup.PUBRSAKEY, null);
		if (encodedpublicKey == null) {
			return false;
		}
		String keyHash = getPublicKeyHash(context);
		Communicator.sendKeyToServer(context, encodedpublicKey, keyHash,
				serverId, silent);
		// true == sent
		return true;
	}

	// -------------------------------------------------------------------------

	/**
	 * Disable encryption. Only if manually disable, delete the key.
	 * 
	 * @param context
	 *            the context
	 */
	public static void disableEncryption(Context context, boolean manual) {
		Utility.saveBooleanSetting(context, Setup.OPTION_ENCRYPTION, false);
		saveHaveAskedForEncryption(context, false);
		String keyHash = getPublicKeyHash(context);
		if (manual) {
			Utility.saveStringSetting(context, Setup.PUBRSAKEY, null);
			Utility.saveStringSetting(context, Setup.PRIVATERSAKEY, null);
			Setup.setKeyDate(context, DB.myUid(), "0");
		}
		for (int serverId : getServerIds(context)) {
			if (Setup.isServerAccount(context, serverId, true)) {
				Communicator.clearKeyFromServer(context, keyHash, serverId);
			}
		}
	}

	// -------------------------------------------------------------------------
	// -------------------------------------------------------------------------
	// THE AES PART

	/**
	 * Checks if is AES key outdated.
	 * 
	 * @param context
	 *            the context
	 * @param uid
	 *            the uid
	 * @param forSending
	 *            the for sending
	 * @return true, if is AES key outdated
	 */
	public static boolean isAESKeyOutdated(Context context, int uid,
			boolean forSending, int transport) {
		long nowTime = DB.getTimestamp();
		long otherTime = getAESKeyDate(context, uid, transport);
		if (otherTime < 1) {
			return true;
		}
		// Log.d("communicator", "###### AES : nowTime = " + nowTime);
		// Log.d("communicator",
		// "###### AES : nowTime - (Setup.AES_KEY_TIMEOUT*1000) = " + (nowTime -
		// (Setup.AES_KEY_TIMEOUT*1000)));
		// Log.d("communicator", "###### AES : otherTime = "+ otherTime);
		if (forSending) {
			if (nowTime - (Setup.AES_KEY_TIMEOUT_SENDING) > otherTime) {
				// Log.d("communicator", "###### AES : OUTDATED!!! :((( ");
				return true;
			}
		} else {
			if (nowTime - (Setup.AES_KEY_TIMEOUT_RECEIVING) > otherTime) {
				// Log.d("communicator", "###### AES : OUTDATED!!! :((( ");
				return true;
			}
		}
		// Log.d("communicator", "###### AES : NOT OUTDATED");
		return false;
	}

	// -------------------------------------------------------------------------

	/**
	 * Gets the AES key date.
	 * 
	 * @param context
	 *            the context
	 * @param uid
	 *            the uid
	 * @return the AES key date
	 */
	public static long getAESKeyDate(Context context, int uid, int transport) {
		String keycreated = Utility.loadStringSetting(context, Setup.AESKEY
				+ "created" + transport + "_" + uid, "");
		if ((keycreated == null || keycreated.length() == 0)) {
			return 0;
		}
		long returnKey = Utility.parseLong(keycreated, 0);
		return returnKey;
	}

	// -------------------------------------------------------------------------

	/**
	 * Sets the AES key date.
	 * 
	 * @param context
	 *            the context
	 * @param uid
	 *            the uid
	 * @param keycreated
	 *            the keycreated
	 */
	public static void setAESKeyDate(Context context, int uid,
			String keycreated, int transport) {
		Utility.saveStringSetting(context, Setup.AESKEY + "created" + transport
				+ "_" + uid, keycreated);
	}

	// -------------------------------------------------------------------------

	/**
	 * Save aes key.
	 * 
	 * @param context
	 *            the context
	 * @param uid
	 *            the uid
	 * @param key
	 *            the key
	 */
	public static void saveAESKey(Context context, int uid, String newAESKey) {
		// Before saving a NEW aes key, save the current one as a backup
		// maybe someone sends us a message encrypted with that old key
		// if he did not receive our updated key early enough!
		// This is only considered to be likely in the middle of an
		// ongoing conversation. But this can happen once an hour so
		// we want to be prepared!
		String currentAESKey = Setup.getAESKeyAsString(context, uid);
		Utility.saveStringSetting(context, Setup.AESKEYBAK + uid, currentAESKey);
		Utility.saveStringSetting(context, Setup.AESKEY + uid, newAESKey);
	}

	// -------------------------------------------------------------------------

	/**
	 * Have aes key.
	 * 
	 * @param context
	 *            the context
	 * @param uid
	 *            the uid
	 * @return true, if successful
	 */
	public static boolean haveAESKey(Context context, int uid) {
		String key = Utility.loadStringSetting(context, Setup.AESKEY + uid, "");
		boolean returnValue = false;
		if ((key != null && key.length() != 0)) {
			returnValue = true;
		}
		return returnValue;
	}

	// -------------------------------------------------------------------------

	/**
	 * Serialize aes key.
	 * 
	 * @param key
	 *            the key
	 * @return the string
	 */
	public static String serializeAESKey(Key key) {
		return Base64.encodeToString(key.getEncoded(), Base64.DEFAULT);
	}

	// -------------------------------------------------------------------------

	/**
	 * Generate aes key.
	 * 
	 * @param randomSeed
	 *            the random seed
	 * @return the key
	 */
	public static Key generateAESKey(String randomSeed) {
		// Set up secret key spec for 128-bit AES encryption and decryption
		SecretKeySpec sks = null;
		try {
			SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
			sr.setSeed(randomSeed.getBytes());
			KeyGenerator kg = KeyGenerator.getInstance("AES");
			kg.init(128, sr);
			sks = new SecretKeySpec((kg.generateKey()).getEncoded(), "AES");
		} catch (Exception e) {
			Log.e("communicator", "AES secret key spec error");
		}
		return sks;
	}

	// -------------------------------------------------------------------------

	/**
	 * Gets the Backup AES key as string. The backup is the rsa key used before
	 * the current one. If a message cannot be decrypted we first try this
	 * backup key before claiming a fail.
	 * 
	 * @param context
	 *            the context
	 * @param uid
	 *            the uid
	 * @return the AES key as string
	 */
	public static String getAESKeyBackupAsString(Context context, int uid) {
		String encodedKey = Utility.loadStringSetting(context, Setup.AESKEYBAK
				+ uid, "");
		return encodedKey;
	}

	/**
	 * Gets the Backup AES key. The backup is the rsa key used before the
	 * current one. If a message cannot be decrypted we first try this backup
	 * key before claiming a fail.
	 * 
	 * @param context
	 *            the context
	 * @param uid
	 *            the uid
	 * @return the AES key
	 */
	public static Key getAESKeyBackup(Context context, int uid) {
		String encodedKey = getAESKeyBackupAsString(context, uid);
		try {
			if ((encodedKey != null && encodedKey.length() != 0)) {
				byte[] decodedKey = Base64.decode(encodedKey, Base64.DEFAULT);
				// rebuild key using SecretKeySpec
				Key originalKey = new SecretKeySpec(decodedKey, 0,
						decodedKey.length, "AES");
				// PublicKey originalKey = KeyFactory.getInstance("RSA")
				// .generatePublic(new X509EncodedKeySpec(decodedKey));
				return originalKey;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	// -------------------------------------------------------------------------

	/**
	 * Gets the AES key as string.
	 * 
	 * @param context
	 *            the context
	 * @param uid
	 *            the uid
	 * @return the AES key as string
	 */
	public static String getAESKeyAsString(Context context, int uid) {
		String encodedKey = Utility.loadStringSetting(context, Setup.AESKEY
				+ uid, "");
		return encodedKey;
	}

	/**
	 * Gets the AES key hash.
	 * 
	 * @param context
	 *            the context
	 * @param uid
	 *            the uid
	 * @return the AES key hash
	 */
	public static String getAESKeyHash(Context context, int uid) {
		String key = getAESKeyAsString(context, uid);
		if (key == null || key.length() < 1) {
			return NA;
		}
		String hash = NA;
		try {
			hash = Utility.md5(key).substring(0, 4).toUpperCase();
		} catch (Exception e) {
		}
		return hash;
	}

	/**
	 * Gets the AES key.
	 * 
	 * @param context
	 *            the context
	 * @param uid
	 *            the uid
	 * @return the AES key
	 */
	public static Key getAESKey(Context context, int uid) {
		String encodedKey = getAESKeyAsString(context, uid);
		try {
			if ((encodedKey != null && encodedKey.length() != 0)) {
				byte[] decodedKey = Base64.decode(encodedKey, Base64.DEFAULT);
				// rebuild key using SecretKeySpec
				Key originalKey = new SecretKeySpec(decodedKey, 0,
						decodedKey.length, "AES");
				// PublicKey originalKey = KeyFactory.getInstance("RSA")
				// .generatePublic(new X509EncodedKeySpec(decodedKey));
				return originalKey;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	// -------------------------------------------------------------------------
	// -------------------------------------------------------------------------
	// THE RSA PART

	/**
	 * Gets the key date.
	 * 
	 * @param context
	 *            the context
	 * @param uid
	 *            the uid
	 * @return the key date
	 */
	public static String getKeyDate(Context context, int uid) {
		String keycreated = Utility.loadStringSetting(context, Setup.PUBRSAKEY
				+ "created" + uid, "");
		if ((keycreated == null || keycreated.length() == 0)) {
			return null;
		}
		return keycreated;
	}

	// -------------------------------------------------------------------------

	/**
	 * Sets the key date.
	 * 
	 * @param context
	 *            the context
	 * @param uid
	 *            the uid
	 * @param keycreated
	 *            the keycreated
	 */
	public static void setKeyDate(Context context, int uid, String keycreated) {
		Utility.saveStringSetting(context, Setup.PUBRSAKEY + "created" + uid,
				keycreated);
	}

	// -------------------------------------------------------------------------

	/**
	 * Save key.
	 * 
	 * @param context
	 *            the context
	 * @param uid
	 *            the uid
	 * @param key
	 *            the key
	 */
	public static void saveKey(Context context, int uid, String key,
			boolean manually) {

		Log.d("communicator", "ACCOUNTKEY #1");

		String oldKey = Utility.loadStringSetting(context, Setup.PUBRSAKEY
				+ uid, null);

		Log.d("communicator", "ACCOUNTKEY #2 key=" + key);
		Log.d("communicator", "ACCOUNTKEY #2 oldKey=" + oldKey);

		Utility.saveStringSetting(context, Setup.PUBRSAKEY + uid, key);
		if (key == null || !key.equals(oldKey)) {
			Log.d("communicator", "ACCOUNTKEY #3 ");

			// Generate a account changed message for security reasons!
			// The user should pay attention that the identity of the
			// corresponding part did not change!

			String name = Main.UID2Name(context, uid, false);

			// if (key == null || !key.equals(oldKey)) {
			if ((key == null && oldKey != null)
					|| (key != null && ((key != oldKey) || (!key.equals(oldKey))))) {
				Log.d("communicator", "ACCOUNTKEY #4 ");
				String messageTextToShow = "";
				if (key != null) {
					Log.d("communicator", "ACCOUNTKEY #5 ");
					String keyhash = Setup.getKeyHash(context, uid);
					messageTextToShow = "[ account key "
							+ keyhash
							+ " changed ]\n\n"
							+ name
							+ " created a new account key or you just added "
							+ name
							+ " and therefore received this key.\n\nIf hou haven't just added "
							+ name
							+ " then you are advised to contact "
							+ name
							+ " personally to"
							+ " assure that "
							+ name
							+ " himself/herself changed the key.\n\nIn ANY case you should contact "
							+ name
							+ " to verify that this new key with the ID "
							+ keyhash + " is matching the ID that " + name
							+ " finds in his/her settings!";

				} else {
					Log.d("communicator", "ACCOUNTKEY #6 ");
					messageTextToShow = "[ account key removed ]\n\n"
							+ name
							+ " removed his/her account key. You are advised to contact "
							+ name
							+ " personally to"
							+ " assure that "
							+ name
							+ " himself/herself removed the key.\n\nYou are no longer able to exchange encrypted "
							+ "messages with " + name + " until " + name
							+ " re-enables encryption in his/her settings!";

				}

				Log.d("communicator", "ACCOUNTKEY " + messageTextToShow);

				// Do not insert a message account key removed if we delete a
				// user manually!
				if (!(manually && key == null)) {
					// Put message into conversation
					DB.addMessage(context, uid, DB.myUid(), Setup.ALERT_PREFIX
							+ messageTextToShow, DB.getTimestampString(),
							DB.getTimestampString(), DB.getTimestampString(),
							DB.getTimestampString(), null, false,
							DB.TRANSPORT_INTERNET, false, 0, 1, "");
					ConversationItem newItem = new ConversationItem();
					newItem.from = uid;
					newItem.to = DB.myUid();
					newItem.text = messageTextToShow;
					Main.updateLastMessage(context, uid, messageTextToShow,
							DB.getTimestamp());
					Communicator.liveUpdateOrNotify(context, newItem);
				}
			}

			// Invalidate any session if key changed!
			// Do this twice to also invalidate the backup key
			Setup.saveAESKey(context, uid, null);
			Setup.saveAESKey(context, uid, null);
			Setup.setAESKeyDate(context, uid, "0", DB.TRANSPORT_INTERNET);
			Setup.setAESKeyDate(context, uid, "0", DB.TRANSPORT_SMS);
		}
	}

	// -------------------------------------------------------------------------

	/**
	 * Have key.
	 * 
	 * @param context
	 *            the context
	 * @param uid
	 *            the uid
	 * @return true, if successful
	 */
	public static boolean haveKey(Context context, int uid) {
		String key = Utility.loadStringSetting(context, Setup.PUBRSAKEY + uid,
				"");
		boolean returnValue = false;
		if ((key != null && key.length() != 0)) {
			returnValue = true;
		}
		return returnValue;
	}

	// -------------------------------------------------------------------------

	/**
	 * Gets the key as string.
	 * 
	 * @param context
	 *            the context
	 * @param uid
	 *            the uid
	 * @return the key as string
	 */
	public static String getKeyAsString(Context context, int uid) {
		String encodedKey = Utility.loadStringSetting(context, Setup.PUBRSAKEY
				+ uid, "");
		return encodedKey;
	}

	/**
	 * Gets the key hash.
	 * 
	 * @param context
	 *            the context
	 * @param uid
	 *            the uid
	 * @return the key hash
	 */
	public static String getKeyHash(Context context, int uid) {
		String key = getKeyAsString(context, uid);
		if (key == null || key.length() < 1) {
			return NA;
		}
		String hash = NA;
		try {
			hash = ((Utility.md5(key.trim().toUpperCase())).substring(0, 4))
					.toUpperCase();
		} catch (Exception e) {
		}
		return hash;
	}

	/**
	 * Gets the key.
	 * 
	 * @param context
	 *            the context
	 * @param encodedKeyAsString
	 *            the encoded key as string
	 * @return the key
	 */
	public static PublicKey getKey(Context context, String encodedKeyAsString) {
		if ((encodedKeyAsString != null && encodedKeyAsString.length() != 0)) {
			Log.d("communicator", "XXXX LOAD SOME RSA KEY1 '"
					+ (new String(encodedKeyAsString)) + "'");

			byte[] decodedKey = Base64.decode(encodedKeyAsString,
					Base64.DEFAULT);

			Log.d("communicator", "XXXX LOAD SOME RSA KEY2 '"
					+ (new String(decodedKey)) + "'");

			// rebuild key using SecretKeySpec
			// Key originalKey = new SecretKeySpec(decodedKey, 0,
			// decodedKey.length, "AES");
			try {
				PublicKey originalKey = KeyFactory.getInstance("RSA")
						.generatePublic(new X509EncodedKeySpec(decodedKey));
				return originalKey;
			} catch (InvalidKeySpecException e) {
				e.printStackTrace();
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * Gets the key.
	 * 
	 * @param context
	 *            the context
	 * @param uid
	 *            the uid
	 * @return the key
	 */
	public static PublicKey getKey(Context context, int uid) {
		String encodedKeyAsString = getKeyAsString(context, uid);
		return (getKey(context, encodedKeyAsString));
	}

	// -------------------------------------------------------------------------

	/**
	 * Gets the public key as string.
	 * 
	 * @param context
	 *            the context
	 * @return the public key as string
	 */
	public static String getPublicKeyAsString(Context context) {
		String encodedKey = Utility.loadStringSetting(context, Setup.PUBRSAKEY,
				"");
		return encodedKey;
	}

	/**
	 * Gets the public key hash.
	 * 
	 * @param context
	 *            the context
	 * @return the public key hash
	 */
	public static String getPublicKeyHash(Context context) {
		String key = getPublicKeyAsString(context);
		if (key == null || key.length() < 1) {
			return NA;
		}
		String hash = NA;
		try {
			hash = ((Utility.md5(key.trim().toUpperCase())).substring(0, 4))
					.toUpperCase();
			// hash = key; // Utility.md5(key).substring(0, 4).toUpperCase();
		} catch (Exception e) {
		}
		return hash;
	}

	/**
	 * Gets the public key.
	 * 
	 * @param context
	 *            the context
	 * @return the public key
	 */
	public static PublicKey getPublicKey(Context context) {
		String encodedKey = getPublicKeyAsString(context);
		if ((encodedKey != null && encodedKey.length() != 0)) {
			byte[] decodedKey = Base64.decode(encodedKey, Base64.DEFAULT);
			// rebuild key using SecretKeySpec
			// SecretKey originalKey = new SecretKeySpec(decodedKey, 0,
			// decodedKey.length, "RSA");
			try {
				PublicKey originalKey = KeyFactory.getInstance("RSA")
						.generatePublic(new X509EncodedKeySpec(decodedKey));
				return originalKey;
			} catch (InvalidKeySpecException e) {
				e.printStackTrace();
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	// -------------------------------------------------------------------------

	/**
	 * Gets the private key.
	 * 
	 * @param context
	 *            the context
	 * @return the private key
	 */
	public static PrivateKey getPrivateKey(Context context) {
		String encodedKey = Utility.loadStringSetting(context,
				Setup.PRIVATERSAKEY, "");
		if ((encodedKey != null && encodedKey.length() != 0)) {
			byte[] decodedKey = Base64.decode(encodedKey, Base64.DEFAULT);
			// rebuild key using SecretKeySpec
			// SecretKey originalKey = new SecretKeySpec(decodedKey, 0,
			// decodedKey.length, "AES");
			try {
				// PrivateKey originalKey = KeyFactory.getInstance("RSA")
				// .generatePrivate(new X509EncodedKeySpec(decodedKey));
				PrivateKey originalKey = KeyFactory.getInstance("RSA")
						.generatePrivate(new PKCS8EncodedKeySpec(decodedKey));
				return originalKey;
			} catch (InvalidKeySpecException e) {
				e.printStackTrace();
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	// -------------------------------------------------------------------------

	/**
	 * Encrypted sent possible.
	 * 
	 * @param context
	 *            the context
	 * @param uid
	 *            the uid
	 * @return true, if successful
	 */
	public static boolean encryptedSentPossible(Context context, int uid) {
		boolean encryption = Utility.loadBooleanSetting(context,
				Setup.OPTION_ENCRYPTION, Setup.DEFAULT_ENCRYPTION);
		boolean haveKey = Setup.haveKey(context, uid);

		if (!encryption || !haveKey) {
			return false;
		}
		return true;
	}

	// -------------------------------------------------------------------------

	/**
	 * Checks if is encryption enabled.
	 * 
	 * @param context
	 *            the context
	 * @return true, if is encryption enabled
	 */
	public static boolean isEncryptionEnabled(Context context) {
		boolean encryption = Utility.loadBooleanSetting(context,
				Setup.OPTION_ENCRYPTION, Setup.DEFAULT_ENCRYPTION);
		return encryption;
	}

	// -------------------------------------------------------------------------

	/**
	 * Checks if is encryption available.
	 * 
	 * @param context
	 *            the context
	 * @param hostUid
	 *            the host uid
	 * @return true, if is encryption available
	 */
	public static boolean isEncryptionAvailable(Context context, int hostUid) {
		boolean encryption = isEncryptionEnabled(context);
		boolean haveKey = Setup.haveKey(context, hostUid);
		return (encryption && haveKey);
	}

	// -------------------------------------------------------------------------
	// -------------------------------------------------------------------------

	/**
	 * Save phone is modified.
	 * 
	 * @param context
	 *            the context
	 * @param uid
	 *            the uid
	 * @param isModified
	 *            the is modified
	 */
	public static void savePhoneIsModified(Context context, int uid,
			boolean isModified) {
		Utility.saveBooleanSetting(context, Setup.SETTINGS_PHONEISMODIFIED
				+ uid, isModified);
	}

	/**
	 * Checks if is phone number was modified by the user manually.
	 * 
	 * @param context
	 *            the context
	 * @param uid
	 *            the uid
	 * @return true, if is phone modified
	 */
	public static boolean isPhoneModified(Context context, int uid) {
		// for NOT registered users this is always true!
		if (uid < 0) {
			return true;
		}
		return Utility.loadBooleanSetting(context,
				Setup.SETTINGS_PHONEISMODIFIED + uid, false);
	}

	// -------------------------------------------------------------------------

	/**
	 * Save phone for a user.
	 * 
	 * @param context
	 *            the context
	 * @param uid
	 *            the uid
	 * @param phone
	 *            the phone
	 * @param manual
	 *            the manual
	 */
	public static void savePhone(Context context, int uid, String phone,
			boolean manual) {
		if (!manual) {
			// this phone number comes from the server...
			savePhoneIsModified(context, uid, false);
		} else {
			// this phone number was entered manually in the user details dialog
			savePhoneIsModified(context, uid, true);
		}
		Utility.saveStringSetting(context, Setup.PHONE + uid, phone);
	}

	// -------------------------------------------------------------------------

	/**
	 * Gets the phone for a user.
	 * 
	 * @param context
	 *            the context
	 * @param uid
	 *            the uid
	 * @return the phone
	 */
	public static String getPhone(Context context, int uid) {
		return Utility.loadStringSetting(context, Setup.PHONE + uid, null);
	}

	// -------------------------------------------------------------------------

	/**
	 * Have phone for a user.
	 * 
	 * @param context
	 *            the context
	 * @param uid
	 *            the uid
	 * @return true, if successful
	 */
	public static boolean havePhone(Context context, int uid) {
		String phone = getPhone(context, uid);
		return (phone != null && phone.length() > 0);
	}

	// -------------------------------------------------------------------------

	/**
	 * Gets the UID by phone. This is necessary to sort incoming SMS.
	 * 
	 * @param context
	 *            the context
	 * @param phone
	 *            the phone
	 * @param allInternalAndExternalUsers
	 *            the all internal and external users
	 * @return the UID by phone
	 */
	public static int getUIDByPhone(Context context, String phone,
			boolean allInternalAndExternalUsers) {
		// Prefer registered users if we have the sme phone number for a
		// registered users
		int smsuserAsFallback = -1;
		int registeredDefault = -1;
		for (int uid : Main.loadUIDList(context)) {
			String currentPhone = getPhone(context, uid);
			// Log.d("communicator",
			// "XXXX getUIDByPhone currentPhone["+uid+"] '"+currentPhone+"' =?= '"+phone+"' (searched) ");
			if (currentPhone != null && currentPhone.equals(phone)) {
				if (uid != -1 || allInternalAndExternalUsers) {
					if (uid >= 0) {
						registeredDefault = uid;
					} else {
						smsuserAsFallback = uid;
					}
				}
			}
		}
		if ((registeredDefault != -1) && (smsuserAsFallback != -1)) {
			askMergeUser(context, smsuserAsFallback, registeredDefault);
		}
		if (registeredDefault != -1) {
			return registeredDefault;
		}
		return smsuserAsFallback;
	}

	// -------------------------------------------------------------------------

	/**
	 * Checks if is SMS option enabled.
	 * 
	 * @param context
	 *            the context
	 * @return true, if is SMS option enabled
	 */
	public static boolean isSMSOptionEnabled(Context context, int serverId) {
		String personalphone = Utility.loadStringSetting(context,
				SETTINGS_PHONE + serverId, "");
		return (personalphone.trim().length() != 0);
	}

	// -------------------------------------------------------------------------

	/**
	 * Ask merge user. If a user is already in the userlist and a separate user
	 * existed with this telephone number we ask to merge both accounts
	 * together.
	 * 
	 * @param context
	 *            the context
	 * @param smsuserAsFallback
	 *            the smsuser as fallback
	 * @param registeredDefault
	 *            the registered default
	 */
	private static void askMergeUser(final Context context,
			final int smsuserAsFallback, final int registeredDefault) {
		try {
			final String titleMessage = "User Merge";
			final String textMessage = "SMS contact "
					+ Main.UID2Name(context, smsuserAsFallback, true)
					+ " has the same phone number as registered user "
					+ Main.UID2Name(context, registeredDefault, true)
					+ ". It is recommended that you merge the SMS to the registered user.\n\nDo you want to merge them now?";
			new MessageAlertDialog(context, titleMessage, textMessage, " Yes ",
					" Cancel ", null,
					new MessageAlertDialog.OnSelectionListener() {
						public void selected(int button, boolean cancel) {
							if (!cancel) {
								if (button == 0) {
									if (DB.mergeUser(context,
											smsuserAsFallback,
											registeredDefault)) {
										// remove merged user
										if (Main.isAlive()) {
											Main.deleteUser(context,
													smsuserAsFallback);
										}
									}
								}
							}
						}
					}).show();
		} catch (Exception e) {
			// ignore
		}
	}

	// -------------------------------------------------------------------------
	// -------------------------------------------------------------------------

	/**
	 * Prompt after enabling encryption.
	 * 
	 * @param context
	 *            the context
	 */
	public static void promptEnabledEncryption(final Context context) {
		String title = "New Account Key Created";
		String text = null;

		new MessageAlertDialog(context, title, text, null, null, " Close ",
				new MessageAlertDialog.OnSelectionListener() {
					public void selected(int button, boolean cancel) {
						// nothing
					}
				}, new MessageAlertDialog.OnInnerViewProvider() {

					public View provide(final MessageAlertDialog dialog) {

						LinearLayout outerLayout = new LinearLayout(context);
						outerLayout.setOrientation(LinearLayout.VERTICAL);

						LinearLayout infoTextBoxInner = new LinearLayout(
								context);
						infoTextBoxInner
								.setOrientation(LinearLayout.HORIZONTAL);
						LinearLayout.LayoutParams lpInfoTextBoxInner = new LinearLayout.LayoutParams(
								LinearLayout.LayoutParams.MATCH_PARENT,
								LinearLayout.LayoutParams.WRAP_CONTENT);
						infoTextBoxInner.setLayoutParams(lpInfoTextBoxInner);
						infoTextBoxInner.setGravity(Gravity.CENTER_HORIZONTAL);

						LinearLayout.LayoutParams lpInfoText = new LinearLayout.LayoutParams(
								LinearLayout.LayoutParams.WRAP_CONTENT,
								LinearLayout.LayoutParams.WRAP_CONTENT);
						lpInfoText.setMargins(5, 15, 5, 15);
						TextView infoTextAccount = new TextView(context);
						infoTextAccount.setLayoutParams(lpInfoText);
						infoTextAccount.setTextColor(Color.WHITE);
						infoTextAccount.setTextSize(16);
						infoTextAccount.setGravity(Gravity.CENTER_VERTICAL
								| Gravity.CENTER_HORIZONTAL);

						infoTextBoxInner.addView(infoTextAccount);
						infoTextAccount
								.setText("Your new account key was created. It will be part"
										+ " of your identity together with your UID.\n\nYour communication"
										+ " partners should verify that they receive the correct"
										+ " account key. Tell them about your new account key so they"
										+ " can verify. Preferably do this by phone or in person.");

						LinearLayout infoTextBoxInnerAccountKey = new LinearLayout(
								context);
						LinearLayout.LayoutParams lpInfoTextBoxInnerAccountKey = new LinearLayout.LayoutParams(
								220, LinearLayout.LayoutParams.WRAP_CONTENT);
						lpInfoTextBoxInnerAccountKey.setMargins(5, 10, 5, 45);
						lpInfoTextBoxInnerAccountKey.gravity = Gravity.CENTER_HORIZONTAL;
						infoTextBoxInnerAccountKey
								.setLayoutParams(lpInfoTextBoxInnerAccountKey);
						infoTextBoxInnerAccountKey
								.setGravity(Gravity.CENTER_HORIZONTAL);
						LinearLayout accountInfo = Main.getAccountKeyView(
								context, DB.myUid(), "YOUR ACCOUNT KEY", true);
						accountInfo.setGravity(Gravity.CENTER_HORIZONTAL);
						infoTextBoxInnerAccountKey.addView(accountInfo);

						outerLayout.addView(infoTextBoxInner);
						outerLayout.addView(infoTextBoxInnerAccountKey);
						return outerLayout;
					}
				}).show();
	}

	// -------------------------------------------------------------------------

	/**
	 * Ask on disable encryption.
	 * 
	 * @param context
	 *            the context
	 */
	private void askOnDisableEncryption(final Context context) {
		try {
			final String titleMessage = "Disable Encryption";
			final String textMessage = "Encrypting messages is a main feature of CryptSecure!\n\nUnencrypted plaintext messages can be possibly read by anyone observing your internet connection. If you disable encryption your account key will be deleted permanently. If you re-enable later, a new account key is created and you should verify that your friends receive this new account key correctly!\n\n"
					+ "Do you really want to disable encryption and only send and receive plaintext messages? ";
			new MessageAlertDialog(context, titleMessage, textMessage,
					"Still Disable", "Cancel", null,
					new MessageAlertDialog.OnSelectionListener() {
						public void selected(int button, boolean cancel) {
							encryption.setChecked(true);
							if (!cancel) {
								if (button == 0) {
									// disable encryption
									disableEncryption(context, true);
									encryption.setChecked(false);
									updateTitleIDInfo(context);
								}
							}
						}
					}).show();
		} catch (Exception e) {
			// ignore
		}
	}

	// ------------------------------------------------------------------------

	/**
	 * Ask on refuse read confirmation.
	 * 
	 * @param context
	 *            the context
	 */
	private void askOnRefuseReadConfirmation(final Context context) {
		try {
			final String titleMessage = "Refuse Read Confirmation";
			final String textMessage = "Read confirmations make sense in most cases because devices nowadays are always-on an often receive messages without our attendance. It makes sense to use read confirmations. This is a mutual value meaning that if you benefit from other people sending you read confirmations you should also send read confirmations out.\n\nIn order to enforce this mutual value, you can see read confirmations ONLY iff you have enabled them too!\n\nYou are now about to refuse sending and receiving any read confirmations. Do you really want to proceed?";
			new MessageAlertDialog(context, titleMessage, textMessage,
					" Disable Read Confirmations ", " Cancel ", null,
					new MessageAlertDialog.OnSelectionListener() {
						public void selected(int button, boolean cancel) {
							noread.setChecked(false);
							if (!cancel) {
								if (button == 0) {
									Utility.saveBooleanSetting(context,
											Setup.OPTION_NOREAD, true);
									noread.setChecked(true);
								}
							}
						}
					}).show();
		} catch (Exception e) {
			// ignore
		}
	}

	// ------------------------------------------------------------------------

	/**
	 * No account yet.
	 * 
	 * @param context
	 *            the context
	 * @return true, if successful
	 */
	public static boolean noAccountYet(Context context) {
		boolean foundAccount = false;
		for (int serverId : getServerIds(context)) {
			if (Setup.isServerAccount(context, serverId, false)) {
				foundAccount = true;
				break;
			}
		}
		return !foundAccount;
	}

	// ------------------------------------------------------------------------

	/**
	 * Normalize phone.
	 * 
	 * @param phone
	 *            the phone
	 * @return the string
	 */
	public static String normalizePhone(String phone) {
		return phone.replace("(", "").replace(")", "").replace(" ", "")
				.replace("-", "").replace("/", "");
	}

	// ------------------------------------------------------------------------
	// -------------------------------------------------------------------------

	/**
	 * Calculate a hopefully unique UID from a phone numer.
	 * 
	 * @return the fake uid from phone
	 */
	private static int FAKEUIDLEN = 4;

	/**
	 * Gets the fake uid from phone.
	 * 
	 * @param phone
	 *            the phone
	 * @return the fake uid from phone
	 */
	public static int getFakeUIDFromPhone(String phone) {
		// Log.d("communicator", "XXX FAKEUID input phone=" + phone);
		phone = Setup.normalizePhone(phone);
		phone = phone.replace("+49", "");
		phone = phone.replace("+1", "");
		phone = phone.replace("+", "");
		// phone = phone.replaceAll("[^0-9]", "");
		// // Log.d("communicator", "XXX FAKEUID normalized phone=" + phone);
		// int parts = phone.length() / FAKEUIDLEN;
		// if (phone.length() % FAKEUIDLEN != 0) {
		// parts++;
		// }
		// int returnUID = 0;
		// int phoneLen = phone.length();
		// for (int part = 0; part < parts; part++) {
		// int start = part * FAKEUIDLEN;
		// int end = start + FAKEUIDLEN;
		// if (end >= phoneLen) {
		// end = phoneLen - 1;
		// }
		// // Log.d("communicator", "XXX FAKEUID start=" + start + ", end=" +
		// // end);
		// String phonePart = phone.substring(start, end);
		// int phonePartInt = Utility.parseInt(phonePart, 0);
		// // Log.d("communicator", "XXX FAKEUID part[" + part + "] returnUID="
		// // + returnUID + " + phonePartInt=" + phonePartInt);
		// returnUID = returnUID + phonePartInt;
		// }
		// // Log.d("communicator", "XXX FAKEUID " + phone + " --> "
		// // + (-1 * returnUID));
		// if (returnUID == 0) {

		int i = 0;
		boolean toggle = false;
		String tmp = Utility.md5(phone);
		for (byte c : tmp.getBytes()) {
			if (toggle) {
				i += c;
			} else {
				i += 10 * c;
			}
			toggle = !toggle;
		}
		String tmp2 = i + "";
		if (tmp2.length() > FAKEUIDLEN) {
			tmp2 = tmp2.substring(0, FAKEUIDLEN);
		}
		int returnUID = Utility.parseInt(tmp2, 0);
		// }
		// Log.d("communicator", "XXX FAKEUID RETURNED " + phone + " --> "
		// + (-1 * returnUID));
		return (-1 * returnUID);
	}

	// -------------------------------------------------------------------------
	// ------------------------------------------------------------------------

	/**
	 * Prompt disable receive all sms.
	 * 
	 * @param context
	 *            the context
	 */
	private void promptDisableReceiveAllSms(final Context context) {
		if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			try {
				final String titleMessage = "Default SMS App";
				final String textMessage = "If you disable Delphino CryptSecure to receive all SMS, you should define a different SMS default app. Otherwise you wont't receive unsecure plain text SMS anymore!\n\nDo you want to change the default SMS app?";
				new MessageAlertDialog(context, titleMessage, textMessage,
						" Yes ", " Cancel ", null,
						new MessageAlertDialog.OnSelectionListener() {
							public void selected(int button, boolean cancel) {
								noread.setChecked(false);
								if (!cancel) {
									if (button == 0) {
										setSMSDefaultApp(context, false);
										receiveallsms.setChecked(false);
										startActivityForResult(
												new Intent(
														android.provider.Settings.ACTION_WIRELESS_SETTINGS),
												0);
									}
								}
							}
						}).show();
			} catch (Exception e) {
				// ignore
			}
		} else {
			// Just turn this feature off
			setSMSDefaultApp(context, false);
			receiveallsms.setChecked(false);
		}
	}

	// ------------------------------------------------------------------------

	/**
	 * Checks if is SMS default app.
	 * 
	 * @param context
	 *            the context
	 * @param strictOnlyLocalSettings
	 *            the strict only local settings
	 * @return true, if is SMS default app
	 */
	@TargetApi(Build.VERSION_CODES.KITKAT)
	public static boolean isSMSDefaultApp(Context context,
			boolean strictOnlyLocalSettings) {
		boolean receiveAllSMS = Utility.loadBooleanSetting(context,
				Setup.OPTION_RECEIVEALLSMS, Setup.DEFAULT_RECEIVEALLSMS);
		boolean androidDefaultSMSApp = receiveAllSMS;
		if (strictOnlyLocalSettings) {
			return receiveAllSMS;
		}
		if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			String androidSettings = Telephony.Sms
					.getDefaultSmsPackage(context);
			androidDefaultSMSApp = context.getPackageName().equals(
					androidSettings);
			return androidDefaultSMSApp;
			// Log.d("communicator", "XXXX DEFAULT ASK FOR SMS APP = "
			// + androidDefaultSMSApp + " =?= " + androidSettings);
		} else {
			return receiveAllSMS;
			// Log.d("communicator", "XXXX DEFAULT NOT ASK FOR SMS APP");
		}
		// return androidDefaultSMSApp || receiveAllSMS;
	}

	// -------------------------------------------------------------------------

	/**
	 * Sets the SMS default app.
	 * 
	 * @param context
	 *            the context
	 * @param enable
	 *            the enable
	 */
	@TargetApi(Build.VERSION_CODES.KITKAT)
	public void setSMSDefaultApp(Context context, boolean enable) {
		if (enable) {
			if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
				String packageName = context.getPackageName();
				// Log.d("communicator", "XXXX DEFAULT SMS APP REQUEST FOR "
				// + packageName);
				Intent intent = new Intent(
						Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
				intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME,
						packageName);
				context.startActivity(intent);
			}
		} else {
			// Intent intent = new
			// Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
			// intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME,
			// context.getPackageName());
		}
		Utility.saveBooleanSetting(context, Setup.OPTION_RECEIVEALLSMS, enable);
	}

	// -------------------------------------------------------------------------
	// -------------------------------------------------------------------------

	/**
	 * Gets the session secret.
	 * 
	 * @param context
	 *            the context
	 * @return the session secret
	 */
	public static String getSessionSecret(final Context context, int serverId) {
		return Utility.loadStringSetting(context, Setup.SETTINGS_SESSIONSECRET
				+ serverId, "");
	}

	// -------------------------------------------------------------------------

	/**
	 * Gets the session id.
	 * 
	 * @param context
	 *            the context
	 * @return the session id
	 */
	public static String getSessionID(final Context context, int serverId) {
		return Utility.loadStringSetting(context, Setup.SETTINGS_SESSIONID
				+ serverId, "");
	}

	// -------------------------------------------------------------------------

	/**
	 * Invalidate tmp login.
	 * 
	 * @param context
	 *            the context
	 */
	// if a tmplogin failed, do a new login!
	public static void invalidateTmpLogin(Context context, int serverId) {
		Utility.saveStringSetting(context, Setup.SETTINGS_SESSIONSECRET
				+ serverId, "");
		Utility.saveStringSetting(context, Setup.SETTINGS_SESSIONID + serverId,
				"");
	}

	// -------------------------------------------------------------------------

	/**
	 * Ensure logged in.
	 * 
	 * @param context
	 *            the context
	 * @return true, if successful
	 */
	public static boolean ensureLoggedIn(Context context, int serverId) {
		// Check if we are logged in... if not re-login
		String secret = Setup.getSessionSecret(context, serverId);
		String sessionid = Setup.getSessionID(context, serverId);
		if (secret == null || secret.length() == 0 || sessionid == null
				|| sessionid.length() == 0) {
			long lastLoginRequest = Utility.loadLongSetting(context,
					"lastloginrequest" + serverId, 0);
			long now = DB.getTimestamp();
			final int MINIMUM_WAIT_TO_LOGIN = 10 * 1000; // do not relogin twice
															// within 10
															// seconds!
			if (now - lastLoginRequest < MINIMUM_WAIT_TO_LOGIN) {
				// NO RELOGIN FOR NOW, WAIT AT LEAST MINIMUM_WAIT_TO_LOGIN
				return false;
			}
			// Remember that we relogin now
			Utility.saveLongSetting(context, "lastloginrequest" + serverId, now);
			Setup.possiblyInvalidateSession(context, true, serverId); // reset
			// invalidation
			// counter
			// Auto-Re-Login
			Setup.login(context, serverId);
			// Automatic error resume
			Scheduler.reschedule(context, true, false, false);
			// Do nothing
			return false;
		}
		// we are logged in
		return true;
	}

	// -------------------------------------------------------------------------

	/**
	 * Gets the tmp login url encoded or null.
	 * 
	 * @param context
	 *            the context
	 * @return the tmp login encoded
	 */
	public static String getTmpLoginEncoded(Context context, int serverId) {
		String session = getTmpLogin(context, serverId);
		if (session != null) {
			return Utility.urlEncode(session);
		}
		return null;
	}

	// -------------------------------------------------------------------------

	/**
	 * Gets the tmp login. returns the session value that is used expect
	 * session=md5(sessionid#timestampinseconds#secret#salt)#sessionid#salt
	 * 
	 * @param context
	 *            the context
	 * @return the tmp login
	 */
	public static String getTmpLogin(Context context, int serverId) {
		if (serverId == -1) {
			// for invalid serverId return null
			return null;
		}
		String secret = getSessionSecret(context, serverId);
		String sessionid = getSessionID(context, serverId);
		if (!Setup.ensureLoggedIn(context, serverId)) {
			return null;
		}
		String timeStampInSeconds = "" + ((DB.getTimestamp() / 1000) / 100);
		String salt = Utility.getRandomString(Setup.SALTLEN);
		String session = Utility.md5(sessionid + "#" + timeStampInSeconds + "#"
				+ secret + "#" + salt)
				+ "#" + sessionid + "#" + salt;
		return session;
	}

	// -------------------------------------------------------------------------

	/**
	 * Calculate login val based on a uid or email in combination with a
	 * password. Either uid or email may be null. If both are set then uid is
	 * taken.
	 * 
	 * @param context
	 *            the context
	 * @param uid
	 *            the uid
	 * @param email
	 *            the email
	 * @param pwd
	 *            the pwd
	 * @return the string
	 */
	public static LoginData calculateLoginVal(Context context, String uid,
			String email, String pwd, int serverId) {
		String sessionSecret = Utility.getRandomString(Setup.SECRETLEN);
		Utility.saveStringSetting(context, Setup.SETTINGS_SESSIONSECRET
				+ serverId, sessionSecret);
		String timeStampInSeconds = "" + (DB.getTimestamp() / 1000);
		String deviceID = getDeviceId(context);

		// RSA encode
		PublicKey serverKey = getServerkey(context, serverId);

		String loginUser = email;
		if (uid != null && uid.trim().length() > 0) {
			loginUser = uid;
		}
		if (serverKey == null) {
			Setup.updateServerkey(context, serverId, true);
			return null; // no server key, seems to be an internet error?
		}
		if (loginUser == null) {
			return null; // email or uid must be given
		}
		Log.d("communicator", "XXXX LOGGING IN: loginUser ='" + loginUser
				+ "' email=" + email + ", uid=" + uid + ", serverKey="
				+ serverKey);

		String eLoginUser = Communicator.encryptServerMessage(context,
				loginUser, serverKey);
		if (eLoginUser == null) {
			return null; // encryption error
		}
		String eLoginUserHash = Utility.md5(eLoginUser).substring(0, 5);

		String loginPassword = pwd;
		String eLoginPassword = Communicator.encryptServerMessage(context,
				loginPassword, serverKey);
		if (eLoginPassword == null) {
			return null; // encryption error
		}
		String eLoginPasswordHash = Utility.md5(eLoginPassword).substring(0, 5);

		// expect val= shortuserhash#password#session#timestamp (timestamp in
		// seconds!!!!)
		String valString = eLoginUserHash + "#" + eLoginPasswordHash + "#"
				+ sessionSecret + "#" + timeStampInSeconds + "#" + deviceID;

		Log.d("communicator", "XXXX LOGGING IN: DECODED val ='" + valString
				+ "'");

		String encryptedBase64ValString = Communicator.encryptServerMessage(
				context, valString, serverKey);

		if (encryptedBase64ValString == null) {
			// encoding error, we cannot login!!!
			return null;
		}

		Log.d("communicator", "XXXX LOGGING IN: ENCODED BASE64 val ="
				+ encryptedBase64ValString);

		LoginData loginData = new LoginData();
		loginData.user = Utility.urlEncode(eLoginUser);
		loginData.password = Utility.urlEncode(eLoginPassword);
		loginData.val = Utility.urlEncode(encryptedBase64ValString);

		return loginData;
	}

	// -------------------------------------------------------------------------

	/**
	 * Update successfull login.
	 * 
	 * @param context
	 *            the context
	 * @param sessionID
	 *            the session id
	 * @param loginErrCnt
	 *            the login err cnt
	 */
	public static void updateSuccessfullLogin(Context context,
			String sessionID, String loginErrCnt, int serverId) {
		Utility.saveStringSetting(context, Setup.SETTINGS_SESSIONID + serverId,
				sessionID);
		int loginerrorcnt = Utility.parseInt(loginErrCnt, 0);
		// save the login error counter
		Utility.saveIntSetting(context,
				Setup.SETTINGS_LOGINERRORCNT + serverId, loginerrorcnt);
		Log.d("communicator", "XXXX LOGGED IN TO " + serverId
				+ ": SESSIONID = '" + sessionID + "', errcnt=" + loginerrorcnt);
	}

	// -------------------------------------------------------------------------

	/**
	 * Login.
	 * 
	 * @param context
	 *            the context
	 */
	// update if not present, will be deleted on login failure
	public static void login(final Context context, final int serverId) {
		Communicator.accountNotActivated = false;

		Setup.updateServerkey(context, serverId, true);
		if (getSessionSecret(context, serverId).equals("")
				|| getSessionID(context, serverId).equals("") || true) {
			// no sessionsecret or no session seed

			String uidString = Utility.loadStringSetting(context, SERVER_UID
					+ serverId, "");
			String pwdString = Utility.loadStringSetting(context, SERVER_PWD
					+ serverId, "");

			LoginData loginData = calculateLoginVal(context, uidString, null,
					pwdString, serverId);
			if (loginData == null) {
				return;
			}

			String url = null;
			// www.delphino.net/cryptsecure/index.php?cmd=login2&uid=5&val=passw%23session%23timestamp%23HTC1
			url = Setup.getBaseURL(context, serverId) + "cmd=login2&val1="
					+ loginData.user + "&val2=" + loginData.password + "&val3="
					+ loginData.val;
			Log.d("communicator", "XXXX LOGGING IN: URL = '" + url);
			final String url2 = url;
			@SuppressWarnings("unused")
			HttpStringRequest httpStringRequest = (new HttpStringRequest(
					context, url2, new HttpStringRequest.OnResponseListener() {
						public void response(String response) {
							if (response.equals("-4")) {
								// not activated
								Communicator.accountNotActivated = true;
								Communicator.accountActivated = false;
								if (Main.isAlive()) {
									Main.getInstance()
											.updateInfoMessageBlockAsync(
													context);
								}
							}
							if (Communicator.isResponseValid(response)) {
								String responseContent = Communicator
										.getResponseContent(response);
								Log.d("communicator",
										"XXXX LOGGING IN: RESPONS = '"
												+ response);
								if (responseContent != null) {
									String[] values = responseContent
											.split("#");
									if (values != null && values.length == 2) {
										Communicator.accountActivated = true;
										String sessionID = values[0];
										String loginErrCnt = values[1];
										updateSuccessfullLogin(context,
												sessionID, loginErrCnt,
												serverId);
									}
								}
							} else {
								Log.d("communicator", "XXXX LOGIN FAILED'");
								// / Clear server key to enforce a soon reload!
								Utility.saveStringSetting(context,
										Setup.SETTINGS_SERVERKEY + serverId,
										null);
							}
						}
					}));
		}
	}

	// -------------------------------------------------------------------------

	/**
	 * Gets the serverkey as string.
	 * 
	 * @param context
	 *            the context
	 * @return the serverkey as string
	 */
	public static String getServerkeyAsString(final Context context,
			int serverId) {
		return Utility.loadStringSetting(context, Setup.SETTINGS_SERVERKEY
				+ serverId, "");
	}

	// -------------------------------------------------------------------------

	/**
	 * Gets the serverkey.
	 * 
	 * @param context
	 *            the context
	 * @return the serverkey
	 */
	public static PublicKey getServerkey(final Context context,
			final int serverId) {

		try {
			String modAndExpString = new String(getServerkeyAsString(context,
					serverId));
			String[] values = modAndExpString.split("#");
			if (values.length == 2) {
				String exp = values[0];
				String mod = values[1];

				Log.d("communicator", "XXXX LOAD KEY EXP '" + (new String(exp))
						+ "'");
				Log.d("communicator", "XXXX LOAD KEY MOD '" + (new String(mod))
						+ "'");

				// If convert from string
				// BigInteger biExp = new BigInteger(exp.getBytes());
				// BigInteger biMod = new BigInteger(mod.getBytes());

				// If convert from hexstring
				BigInteger biExp = new BigInteger(exp, 16);
				BigInteger biMod = new BigInteger(mod, 16);

				RSAPublicKeySpec pubKeySpec = new RSAPublicKeySpec(biMod, biExp);

				// RSAPublicKeySpec pubKeySpec = new RSAPublicKeySpec(
				// new BigInteger(1, mod.getBytes()), new BigInteger(1,
				// exp.getBytes()));
				KeyFactory keyFactory = null;
				keyFactory = KeyFactory.getInstance("RSA");
				PublicKey publicKey = keyFactory.generatePublic(pubKeySpec);
				return publicKey;
			}

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return null;
	}

	// -------------------------------------------------------------------------

	/**
	 * Gets the server.
	 * 
	 * @param serverId
	 *            the server id
	 * @return the server
	 */
	public static String getServer(Context context, int serverId) {
		List<String> serverList = getServers(context);
		for (int i = 0; i < serverList.size(); i++) {
			String serverAddress = serverList.get(i);
			if (getServerId(serverAddress) == serverId) {
				return serverAddress;
			}
		}
		return "";
	}

	// -------------------------------------------------------------------------

	/**
	 * Update ALL serverkeys. Only use this on startup or manual refresh, this
	 * may be costly.
	 * 
	 * @param context
	 *            the context
	 */
	public static void updateAllServerkeys(final Context context) {
		for (int serverId : getServerIds(context)) {
			if (isServerAccount(context, serverId, false)) {
				updateServerkey(context, serverId, true);
			}
		}
	}

	// -------------------------------------------------------------------------

	/**
	 * Update serverkey.
	 * 
	 * @param context
	 *            the context
	 */
	// Update if not present, will be deleted on login failure
	public static void updateServerkey(final Context context,
			final int serverId, final boolean silent) {

		if (getServerkeyAsString(context, serverId).equals("")) {
			// No serverkey there, needs to update

			final String baseUrl = getServer(context, serverId);

			String url = null;
			url = Setup.getBaseURL(context, serverId) + "cmd=serverkey";
			final String url2 = url;
			@SuppressWarnings("unused")
			HttpStringRequest httpStringRequest = (new HttpStringRequest(
					context, url2, new HttpStringRequest.OnResponseListener() {
						public void response(String response) {
							if (Communicator.isResponseValid(response)) {
								if (response.length() > 10) {
									Utility.saveStringSetting(
											context,
											Setup.SETTINGS_SERVERKEY + serverId,
											Communicator
													.getResponseContent(response));
									Log.d("communicator",
											"XXXX SAVED SERVER KEY '"
													+ getServerkeyAsString(
															context, serverId)
													+ "'");
									Communicator.internetFailCnt = 0;
								} else {
									Communicator.internetFailCnt++;
								}
							} else {
								Communicator.internetFailCnt++;

								if (!silent) {
									Utility.showToastAsync(
											context,
											"Server '"
													+ baseUrl
													+ "' not reachable. It will be disabled.");
								}

								// Log.d("communicator",
								// "XXXX FAILED TO UPDATE SERVER KEY '"
								// + response + "'");
							}
						}
					}));
		}
	}

	// -------------------------------------------------------------------------
	// -------------------------------------------------------------------------

	/**
	 * Prepare key.
	 * 
	 * @param context
	 *            the context
	 * @return the byte[]
	 */
	private static byte[] prepareKey(Context context, int serverId) {
		try {
			String secret = Utility.md5(getSessionSecret(context, serverId)
					.substring(5)); // the
			// first 5 characters remain a secret!
			String timeStampInSeconds = "" + ((DB.getTimestamp() / 1000) / 100);

			timeStampInSeconds = Utility.md5(timeStampInSeconds);

			byte[] secretArray = secret.getBytes();
			byte[] timeStampInSecondsArray = timeStampInSeconds.getBytes();
			byte[] entrcypted = new byte[32];

			for (int i = 0; i < 32; i++) {
				entrcypted[i] = (byte) (secretArray[i] ^ timeStampInSecondsArray[i]);
			}
			return entrcypted;
		} catch (Exception e) {
			return null;
		}
	}

	// -------------------------------------------------------------------------

	/**
	 * Dec text.
	 * 
	 * @param context
	 *            the context
	 * @param textEncrypted
	 *            the text encrypted
	 * @return the string
	 */
	public static String decText(Context context, String textEncrypted,
			int serverId) {
		try {
			byte[] encrypted = Base64.decode(textEncrypted, Base64.DEFAULT);

			byte[] keyArray = prepareKey(context, serverId);
			if (keyArray == null) {
				return null;
			}
			byte[] decrypted = new byte[keyArray.length];

			int i = 0;
			for (byte b : encrypted) {
				decrypted[i] = (byte) (b ^ keyArray[i++]);
			}

			String decryptedString = new String(decrypted);

			int i1 = decryptedString.indexOf("#");
			int i2 = decryptedString.lastIndexOf("#");
			if (i1 >= 0 && i2 > -0) {
				String result = decryptedString.substring(i1 + 1, i2);
				return result;
			}
			return null;
		} catch (Exception e) {
			return null;
		}
	}

	// -------------------------------------------------------------------------

	/**
	 * The chunklen must be equivalent to the server a number 0 <= chunklen <=
	 * 30.
	 */
	private static int CHUNKLEN = 30;

	/**
	 * Enc a long text > 30 chars.
	 * 
	 * @param context
	 *            the context
	 * @param text
	 *            the text
	 * @return the string
	 */
	public static String encLongText(Context context, String text, int serverId) {
		String backText = "";
		int chunks = (int) Math
				.ceil(((double) text.length() / (double) CHUNKLEN));
		for (int c = 0; c < chunks; c++) {
			int end = (c + 1) * CHUNKLEN;
			if (end >= text.length()) {
				end = text.length() - 1;
			}
			String chunk = text.substring(c * CHUNKLEN, end);
			String chunkEnc = encText(context, chunk, serverId);
			if (backText.length() > 0) {
				backText += ";";
			}
			backText += chunkEnc;
		}
		return backText;
	}

	// -------------------------------------------------------------------------

	/**
	 * Dec long text that had > 30 chars in the decoded version.
	 * 
	 * @param context
	 *            the context
	 * @param text
	 *            the text
	 * @param serverId
	 *            the server id
	 * @return the string
	 */
	public static String decLongText(Context context, String text, int serverId) {
		String backText = "";
		String[] encChunks = text.split(";");
		for (String encChunk : encChunks) {
			String chunk = decText(context, encChunk, serverId);
			if (chunk == null) {
				return null;
			}
			backText += chunk;
		}
		return backText;
	}

	// -------------------------------------------------------------------------

	/**
	 * Enc text up to 30 chars.
	 * 
	 * @param context
	 *            the context
	 * @param text
	 *            the text
	 * @return the string
	 */
	public static String encText(Context context, String text, int serverId) {
		byte[] keyArray = prepareKey(context, serverId);
		if (keyArray == null) {
			return null;
		}
		int pad = keyArray.length - text.length() - 2;
		int index = (int) (Math.random() * pad);
		String rnd = Utility.getRandomString(pad);
		text = rnd.substring(0, index) + "#" + text + "#"
				+ rnd.substring(index);

		byte[] textArray = text.getBytes();
		byte[] entrcypted = new byte[keyArray.length];
		int i = 0;
		for (byte b : textArray) {
			entrcypted[i] = (byte) (b ^ keyArray[i]);
			i++;
		}

		String encryptedString = Base64.encodeToString(entrcypted,
				Base64.DEFAULT);
		return encryptedString;
	}

	// -------------------------------------------------------------------------
	// -------------------------------------------------------------------------

	/**
	 * Prepare simple key.
	 * 
	 * @param context
	 *            the context
	 * @return the byte[]
	 */
	private static byte[] prepareSimpleKey(Context context, int serverId) {
		String secret = getSessionSecret(context, serverId);
		if (secret == null || secret.length() < 20) {
			return null;
		}
		secret = Utility.md5(secret.substring(5)); // the first 5 characters
													// remain a secret!
		String timeStampInSeconds = "" + ((DB.getTimestamp() / 1000) / 100);

		// Log.d("communicator",
		// "XXXXX prepare key timeStampInSeconds='"
		// + timeStampInSeconds + "'");
		// Log.d("communicator",
		// "XXXXX prepare key secret='"
		// + secret + "'");

		timeStampInSeconds = Utility.md5(timeStampInSeconds);

		byte[] secretArray = secret.getBytes();
		byte[] timeStampInSecondsArray = timeStampInSeconds.getBytes();

		byte[] encrypted = new byte[8];

		encrypted[0] = timeStampInSecondsArray[0];
		encrypted[1] = secretArray[0];
		encrypted[2] = timeStampInSecondsArray[1];
		encrypted[3] = secretArray[1];
		encrypted[4] = timeStampInSecondsArray[2];
		encrypted[5] = secretArray[2];
		encrypted[6] = timeStampInSecondsArray[3];
		encrypted[7] = secretArray[3];

		// for (int c = 0; c <= 7; c++) {
		// Log.d("communicator",
		// "XXXXX KEY["+c+"]='"
		// + encrypted[c] + "'");
		// }

		return encrypted;
	}

	// -------------------------------------------------------------------------

	/**
	 * Enc uid.
	 * 
	 * @param context
	 *            the context
	 * @param uid
	 *            the uid
	 * @return the string
	 */
	public static String encUid(Context context, int uid, int serverId) {
		String text = uid + "";
		byte[] simpleKey = prepareSimpleKey(context, serverId);
		if (simpleKey == null) {
			return null;
		}
		String checkSum = Utility.md5(text).substring(0, 1);
		int uidEncrypted = uid + 1 * simpleKey[0] + 1 * simpleKey[1] + 10
				* simpleKey[2] + 10 * simpleKey[3] + 100 * simpleKey[4] + 100
				* simpleKey[5] + 1000 * simpleKey[6] + 1000 * simpleKey[7];
		return checkSum + uidEncrypted;
	}

	// -------------------------------------------------------------------------

	/**
	 * Dec uid.
	 * 
	 * @param context
	 *            the context
	 * @param uidEncrypted
	 *            the uid encrypted
	 * @return the int
	 */
	public static int decUid(Context context, String uidEncrypted, int serverId) {
		byte[] simpleKey = prepareSimpleKey(context, serverId);
		if (simpleKey == null) {
			return -1;
		}
		String checkSum = uidEncrypted.substring(0, 1);
		String uidEncrypted2 = uidEncrypted.substring(1);

		int decUid = Utility.parseInt(uidEncrypted2, -1);
		if (decUid >= 0) {
			int diff = 1 * simpleKey[0] + 1 * simpleKey[1] + 10 * simpleKey[2]
					+ 10 * simpleKey[3] + 100 * simpleKey[4] + 100
					* simpleKey[5] + 1000 * simpleKey[6] + 1000 * simpleKey[7];
			decUid = decUid - diff;
			String checkSum2 = Utility.md5(decUid + "").substring(0, 1);
			if (checkSum.equals(checkSum2)) {
				return decUid;
			}
		}
		return -2; // make a difference to -1 which is just an invalid user but
					// -1 is invalid decoding == unknown user
	}

	// -------------------------------------------------------------------------

	/**
	 * Possibly invalidate session.
	 * 
	 * @param context
	 *            the context
	 * @param reset
	 *            the reset
	 */
	public static void possiblyInvalidateSession(Context context,
			boolean reset, int serverId) {
		Log.d("communicator", "possiblyInvalidateSession() reset? " + reset);
		if (!reset) {
			int counter = Utility.loadIntSetting(context,
					Setup.SETTINGS_INVALIDATIONCOUNTER + serverId,
					Setup.SETTINGS_INVALIDATIONCOUNTER_MAX);
			Log.d("communicator", "possiblyInvalidateSession() counter: "
					+ counter);
			if (counter > Setup.SETTINGS_INVALIDATIONCOUNTER_MAX) {
				// guard against two high counter
				// reset
				possiblyInvalidateSession(context, true, serverId);
				return;
			}
			if (counter > 0) {
				counter--;
				Utility.saveIntSetting(context,
						Setup.SETTINGS_INVALIDATIONCOUNTER + serverId, counter);
			} else {
				// HERE INVALIDATING
				Log.d("communicator", "INVALIDATING NOW");
				possiblyInvalidateSession(context, true, serverId);
				// trigger a new login by invalidating the session
				invalidateTmpLogin(context, serverId);
			}
		} else {
			// reset count down to the maximum (for next start)
			Utility.saveIntSetting(context, Setup.SETTINGS_INVALIDATIONCOUNTER
					+ serverId, Setup.SETTINGS_INVALIDATIONCOUNTER_MAX);
		}
	}

	// -------------------------------------------------------------------------

	/**
	 * Gets the device id.
	 * 
	 * @param context
	 *            the context
	 * @return the device id
	 */
	public static String getDeviceId(Context context) {
		String deviceID = Utility.getDeviceId(context);
		if (deviceID == null) {
			deviceID = "";
		}
		deviceID = Utility.md5(deviceID);
		if (deviceID.length() > 4) {
			// 0 inclusive, 4 exclusiv == 4 characters
			deviceID = deviceID.substring(0, 4);
		}
		return deviceID;
	}

	// ------------------------------------------------------------------------

	/**
	 * Go back.
	 * 
	 * @param context
	 *            the context
	 */
	public void goBack(Context context) {
		// GET TO THE MAIN SCREEN IF THIS ICON IS CLICKED !
		Intent intent = new Intent(this, Main.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}

	// ------------------------------------------------------------------------

	/**
	 * Possibly disable screenshot.
	 * 
	 * @param activity
	 *            the activity
	 */
	static void possiblyDisableScreenshot(Activity activity) {
		if (Utility.loadBooleanSetting(activity, Setup.OPTION_NOSCREENSHOTS,
				Setup.DEFAULT_NOSCREENSHOTS)) {
			Utility.noScreenshots(activity);
		}
	}

	// ------------------------------------------------------------------------

	/**
	 * Possibly prompt no encryption.
	 * 
	 * @param context
	 *            the context
	 */
	static void possiblyPromptNoEncryption(final Context context) {
		if (Setup.isEncryptionEnabled(context)) {
			return; // encryption is already enabled, everything is ok
		}
		if (Utility.loadBooleanSetting(context,
				Setup.SETTINGS_HAVEASKED_NOENCRYPTION, false)) {
			return; // we already asked!
		}
		if (!Setup.isUIDDefined(context)) {
			return; // no account defined!
		}
		// At this point we should ask the user to enable encryption!
		final String titleMessage = "Enable Encryption?";
		final String textMessage = "You currently have disabled encryption.\n\nGenerally this is a bad idea because nobody is able to send you encrypted private messages. You can still receive unencrypted plain text messages.\n\nDo you want to turn on encryption and enable you to send/receive secure messages?";
		new MessageAlertDialog(context, titleMessage, textMessage, " Yes ",
				" Stay Insecure ", null,
				new MessageAlertDialog.OnSelectionListener() {
					public void selected(int button, boolean cancel) {
						if (cancel) {
							// prompt again on cancel!
							possiblyPromptNoEncryption(context);
						}
						if (button == 0) {
							// enable
							enableEncryption(context);
						} else if (button == 1) {
							// remember we have asked
							saveHaveAskedForEncryption(context, true);
						}
					}
				}).show();
	}

	// ------------------------------------------------------------------------

	/**
	 * Save have asked for encryption.
	 * 
	 * @param context
	 *            the context
	 * @param haveAsked
	 *            the have asked
	 */
	public static void saveHaveAskedForEncryption(Context context,
			boolean haveAsked) {
		Utility.saveBooleanSetting(context,
				Setup.SETTINGS_HAVEASKED_NOENCRYPTION, haveAsked);
	}

	// ------------------------------------------------------------------------

	/**
	 * Checks if is UID defined at a server.
	 * 
	 * @param context
	 *            the context
	 * @param serverId
	 *            the server id
	 * @return true, if is UID defined
	 */
	public static boolean isUIDDefined(Context context, int serverId) {
		return (Utility.loadStringSetting(context, SERVER_UID + serverId, "")
				.trim().length() != 0);
	}

	// ------------------------------------------------------------------------

	/**
	 * Checks if is UID defined at ANY server.
	 * 
	 * @param context
	 *            the context
	 * @return true, if is UID defined
	 */
	public static boolean isUIDDefined(Context context) {
		for (int serverId : getServerIds(context)) {
			if (isUIDDefined(context, serverId)) {
				return true;
			}
		}
		// No account at non of the servers!
		return false;
	}

	// ------------------------------------------------------------------------

	/**
	 * Update attachment ALL server limits. This should oly be called at startup
	 * or on manual refresh.
	 * 
	 * @param context
	 *            the context
	 * @param forceUpdate
	 *            the force update
	 */
	public static void updateAttachmentAllServerLimits(final Context context,
			boolean forceUpdate) {
		for (int serverId : getServerIds(context)) {
			if (isServerAccount(context, serverId, false)) {
				updateAttachmentServerLimit(context, forceUpdate, serverId);
			}
		}
	}

	// ------------------------------------------------------------------------

	/**
	 * Update attachment server limit if more than
	 * UPDATE_SERVER_ATTACHMENT_LIMIT_MINIMAL_INTERVAL minutes have passed or
	 * forceUpdate is set.
	 * 
	 * @param context
	 *            the context
	 * @param forceUpdate
	 *            the force update
	 */
	public static void updateAttachmentServerLimit(final Context context,
			boolean forceUpdate, final int serverId) {
		long lastTime = Utility.loadLongSetting(context,
				Setup.LASTUPDATE_SERVER_ATTACHMENT_LIMIT + serverId, 0);
		final long currentTime = DB.getTimestamp();
		if (!forceUpdate
				&& (lastTime
						+ (Setup.UPDATE_SERVER_ATTACHMENT_LIMIT_MINIMAL_INTERVAL * 1000 * 60) > currentTime)) {
			// Do not do this more frequently
			return;
		}

		String url = null;
		url = Setup.getBaseURL(context, serverId) + "cmd=attachments";
		final String url2 = url;
		@SuppressWarnings("unused")
		HttpStringRequest httpStringRequest = (new HttpStringRequest(context,
				url2, new HttpStringRequest.OnResponseListener() {
					public void response(String response) {
						if (Communicator.isResponseValid(response)) {
							if (Communicator.isResponsePositive(response)) {
								// Save the attachment limit in KB
								String content = Communicator
										.getResponseContent(response);
								if (content != null) {
									int limit = Utility.parseInt(content, -1);
									Utility.saveIntSetting(context,
											Setup.SERVER_ATTACHMENT_LIMIT
													+ serverId, limit);
									Log.d("communicator",
											"XXXX SAVED SERVER ATTACHMENT LIMIT KB='"
													+ content + "'");
									Utility.saveLongSetting(
											context,
											Setup.LASTUPDATE_SERVER_ATTACHMENT_LIMIT
													+ serverId, currentTime);
								}
							} else {
								// No attachments allowed
								Utility.saveIntSetting(context,
										Setup.SERVER_ATTACHMENT_LIMIT
												+ serverId, 0);
							}
						}
					}
				}));
	}

	// ------------------------------------------------------------------------

	/**
	 * Gets the attachment server limit. -1 means that no limit has been set
	 * (this is also means we do not allow attachments), 0 means that the server
	 * does not allow attachments at all and any other values is the limit in
	 * KB.
	 * 
	 * @param context
	 *            the context
	 * @return the attachment server limit
	 */
	public static int getAttachmentServerLimit(Context context, int serverId) {
		return Utility.loadIntSetting(context, Setup.SERVER_ATTACHMENT_LIMIT
				+ serverId, Setup.SERVER_ATTACHMENT_LIMIT_DEFAULT);
	}

	// ------------------------------------------------------------------------

	/**
	 * Checks if is attachments allowed by server that is if the server has ben
	 * queried already if he allows and if the server has responded to allow
	 * more than 0 KB.
	 * 
	 * @param context
	 *            the context
	 * @return true, if is attachments allowed by server
	 */
	public static boolean isAttachmentsAllowedByServer(Context context,
			int serverId) {
		return (getAttachmentServerLimit(context, serverId) > 0);
	}

	// ------------------------------------------------------------------------

	/**
	 * Extra count down to zero. Returns true if sending is permitted, it
	 * returns false if we need to wait for the next cycle and the count down is
	 * not yet at zero.
	 * 
	 * @param context
	 *            the context
	 * @return true, if successful
	 */
	public static boolean extraCountDownToZero(Context context) {
		int cnt = Utility.loadIntSetting(context, Setup.KEYEXTRAWAITCOUNTDOWN,
				0);
		if (cnt <= 0) {
			return true;
		}
		cnt = cnt - 1;
		Utility.saveIntSetting(context, Setup.KEYEXTRAWAITCOUNTDOWN, cnt);
		return false;
	}

	// ------------------------------------------------------------------------

	/**
	 * Establish the extra crount down depending on the transport type.
	 * 
	 * @param context
	 *            the context
	 * @param transport
	 *            the transport
	 */
	public static void extraCrountDownSet(Context context, int transport) {
		int cnt = 15; // For SMS
		if (transport == DB.TRANSPORT_INTERNET) {
			cnt = 5;
		}
		Utility.saveIntSetting(context, Setup.KEYEXTRAWAITCOUNTDOWN, cnt);
	}

	// ------------------------------------------------------------------------

	public static String normalizeServer(String serverAddress) {
		String normalizedAddress = serverAddress.toLowerCase();
		// Do NOT do the following. Maybe we constantly want to add another
		// parameter!
		//
		// if (!normalizedAddress.endsWith("/")) {
		// normalizedAddress += "/";
		// }
		return normalizedAddress;
	}

	// ------------------------------------------------------------------------

	/** The cached server ids from server address. */
	private static HashMap<String, Integer> cachedServerIdsFromServerAddress = new HashMap<String, Integer>();

	/**
	 * Gets the server id from the server address.
	 * 
	 * @param serverName
	 *            the server address
	 * @return the server id
	 */
	public static int getServerId(String serverAddress) {
		if (!cachedServerIdsFromServerAddress.containsKey(serverAddress)) {
			cachedServerIdsFromServerAddress.put(serverAddress,
					Math.abs(normalizeServer(serverAddress).hashCode()));
		}
		return cachedServerIdsFromServerAddress.get(serverAddress);
	}

	// ------------------------------------------------------------------------

	/**
	 * Removes a server.
	 * 
	 * @param context
	 *            the context
	 * @param serverId
	 *            the server id
	 * @return true, if successful
	 */
	public static boolean removeServer(Context context, int serverId) {
		List<String> serverList = getServers(context);
		int indexToDelete = -1;
		for (int i = 0; i < serverList.size(); i++) {
			String serverAddress = serverList.get(i);
			if (getServerId(serverAddress) == serverId) {
				indexToDelete = i;
				break;
			}
		}
		if (indexToDelete >= 0) {
			serverList.remove(indexToDelete);
			String separatedString = Utility.getListAsString(
					new ArrayList<String>(serverList), "|");
			Utility.saveStringSetting(context, Setup.SERVERLIST,
					separatedString);
			// Invalidate caches
			cachedServerIds = null;
			cachedServers = null;
			return true;
		}
		return false;
	}

	// ------------------------------------------------------------------------

	/**
	 * Adds a server.
	 * 
	 * @param context
	 *            the context
	 * @param serverAddress
	 *            the server address
	 */
	public static void addServer(Context context, String serverAddress) {
		LinkedHashSet<String> serverList = new LinkedHashSet<String>(
				getServers(context));
		serverList.add(serverAddress);
		String separatedString = Utility.getListAsString(new ArrayList<String>(
				serverList), "|");
		Utility.saveStringSetting(context, Setup.SERVERLIST, separatedString);
		// Invalidate caches
		cachedServerIds = null;
		cachedServers = null;
	}

	// ------------------------------------------------------------------------

	/** The cached server ids as list. */
	private static List<Integer> cachedServerIds = null;

	/**
	 * Gets the server ids.
	 * 
	 * @param context
	 *            the context
	 * @return the server ids
	 */
	public static List<Integer> getServerIds(Context context) {
		if (cachedServerIds == null) {
			ArrayList<Integer> returnList = new ArrayList<Integer>();
			for (String serverAddress : getServers(context)) {
				returnList.add(getServerId(serverAddress));
			}
			cachedServerIds = returnList;
		}
		return new ArrayList<Integer>(cachedServerIds);
	}

	// ------------------------------------------------------------------------

	/** The cached servers as list. */
	private static List<String> cachedServers = null;

	/**
	 * Gets the list of servers.
	 * 
	 * @param context
	 *            the context
	 * @return the servers
	 */
	public static List<String> getServers(Context context) {
		if (cachedServers == null) {
			LinkedHashSet<String> returnList = new LinkedHashSet<String>();

			String separatedString = Utility.loadStringSetting(context,
					Setup.SERVERLIST, null);
			returnList = new LinkedHashSet<String>();
			returnList.add(Setup.DEFAULT_SERVER);
			returnList
					.addAll(Utility.getListFromString(separatedString, "\\|"));
			cachedServers = new ArrayList<String>(returnList);
		}
		return new ArrayList<String>(cachedServers);
	}

	// ------------------------------------------------------------------------

	/**
	 * Gets the next active server in round robin style. If no such server
	 * exists, the empty string is returned.
	 * 
	 * @param context
	 *            the context
	 * @return the next server
	 */
	public static String getNextReceivingServer(Context context) {
		int lastServerIndex = Utility.loadIntSetting(context,
				Setup.SERVERLASTRRIDRECEIVE, 0);
		String returnServerAddress = "";
		List<String> serverList = getServers(context);
		if (serverList.size() == 0) {
			return returnServerAddress;
		}
		int serverIndex = lastServerIndex;
		boolean foundActive = false;
		boolean seenZero = false;
		while (!foundActive) {
			serverIndex++;
			if (serverIndex >= serverList.size()) {
				if (seenZero) {
					// We do not want to loop forever... there is no active
					// server!
					break;
				}
				serverIndex = 0;
				seenZero = true;
			}
			String serverAddress = serverList.get(serverIndex);
			int serverId = getServerId(serverAddress);
			if (isServerActive(context, serverId, false)
					&& isServerAccount(context, serverId, false)) {
				Utility.saveIntSetting(context, Setup.SERVERLASTRRIDRECEIVE,
						serverIndex);
				returnServerAddress = serverAddress;
				foundActive = true;
				break;
			}
		}
		return returnServerAddress;
	}

	// ------------------------------------------------------------------------

	/**
	 * Gets the next conversation item to send to a server in round robin style.
	 * If no such server exists, then null is returned.
	 * 
	 * @param context
	 *            the context
	 * @return the next server
	 */
	public static ConversationItem getNextSendingServerMessage(Context context) {
		int lastServerIndex = Utility.loadIntSetting(context,
				Setup.SERVERLASTRRIDSEND, 0);
		ConversationItem returnItem = null;
		List<String> serverList = getServers(context);
		if (serverList.size() == 0) {
			return returnItem;
		}
		int serverIndex = lastServerIndex;
		boolean foundServerWeHaveMessagesToSendTo = false;
		boolean seenZero = false;
		while (!foundServerWeHaveMessagesToSendTo) {
			serverIndex++;
			if (serverIndex >= serverList.size()) {
				if (seenZero) {
					// We do not want to loop forever... there is no active
					// server!
					break;
				}
				serverIndex = 0;
				seenZero = true;
			}
			String serverAddress = serverList.get(serverIndex);
			int serverId = getServerId(serverAddress);
			// Now test if we have messages to send to THIS server, otherwise we
			// proceed to test the next one
			// until we reached a loop.
			if (isServerAccount(context, serverId, false)) {
				// Only test this server if we have an active account for it!
				returnItem = DB.getNextMessage(context, DB.TRANSPORT_INTERNET,
						serverId);
				Utility.saveIntSetting(context, Setup.SERVERLASTRRIDSEND,
						serverIndex);
				if (returnItem != null) {
					foundServerWeHaveMessagesToSendTo = true;
					break;
				}
			}
		}
		return returnItem;
	}

	// ------------------------------------------------------------------------

	public static void setServerActive(Context context, int serverId,
			boolean active) {
		Utility.saveBooleanSetting(context, Setup.SERVER_ACTIVE + serverId,
				active);
		cachedServerActive.put(serverId, active);
	}

	// ------------------------------------------------------------------------

	/** The cached server active flag from serverid. */
	private static HashMap<Integer, Boolean> cachedServerActive = new HashMap<Integer, Boolean>();

	public static boolean isServerActive(Context context, int serverId,
			boolean forceRefresh) {
		if (!cachedServerActive.containsKey(serverId) || forceRefresh) {
			boolean serverActive = Utility.loadBooleanSetting(context,
					Setup.SERVER_ACTIVE + serverId, SERVER_ACTIVEDEFAULT);
			cachedServerActive.put(serverId, serverActive);
		}
		return cachedServerActive.get(serverId);
	}

	// ------------------------------------------------------------------------

	/** The cached server active flag from serverid. */
	private static HashMap<Integer, Boolean> cachedServerAccount = new HashMap<Integer, Boolean>();

	/**
	 * Checks if there exists an account for this server. If no account for a
	 * server is registered we won't check it, won't download server keys or
	 * upload our rsa key to this server. We DO THIS for disabled servers
	 * because we would like to enable other users of a disabled server to write
	 * messages to us.
	 * 
	 * @param context
	 *            the context
	 * @param serverId
	 *            the server id
	 * @return true, if is server account
	 */
	public static boolean isServerAccount(Context context, int serverId,
			boolean forceRefresh) {
		if (!cachedServerAccount.containsKey(serverId) || forceRefresh) {
			String uidString = Utility.loadStringSetting(context, SERVER_UID
					+ serverId, "");
			// No account at this server, this server is automatically deactive
			boolean serverAccount = !uidString.equals("");
			cachedServerAccount.put(serverId, serverAccount);
		}
		return cachedServerAccount.get(serverId);
	}

	// ------------------------------------------------------------------------

	/**
	 * Gets and saves a (new) UID for a server-internal uid (suid) and a
	 * serverId.
	 * 
	 * @param context
	 *            the context
	 * @param uid
	 *            the uid
	 * @param serverUid
	 *            the server uid
	 * @return the s uid
	 */
	public static int getUid(Context context, int sUid, int serverId) {
		// First try to find a match in the current userlist!
		for (int vglUid : Main.loadUIDList(context)) {
			int vglServerId = Setup.getServerId(context, vglUid);
			int vglSUid = Setup.getSUid(context, vglUid);
			if (vglSUid == sUid && serverId == vglServerId) {
				// We found the UID we were searching for!
				return vglUid;
			}
		}
		// We found nothing, so create/issue a new UID and save it!
		int uid = Math.abs((Integer.valueOf(sUid)).hashCode())
				+ Math.abs((Integer.valueOf(serverId).hashCode()));
		Utility.saveIntSetting(context, UID2SUID + uid, sUid);
		Utility.saveIntSetting(context, UID2SERVERID + uid, serverId);
		return uid;
	}

	// ------------------------------------------------------------------------

	/** The cached s uid mapping from uid. */
	private static HashMap<Integer, Integer> cachedSUid = new HashMap<Integer, Integer>();

	/**
	 * Gets the suid from an UID. Note that for the userlist now internally only
	 * UIDs are used in place of SUIDs. SUIDs are only unique for a server (S).
	 * If no mapping was found, the legacy fallback will return the uid itself.
	 * 
	 * @param context
	 *            the context
	 * @param sUid
	 *            the s uid
	 * @return the uid
	 */
	public static int getSUid(Context context, int uid) {
		if (uid < 0) {
			// Log.d("communicator",
			// "MIGRATE getSUid: uid=" + uid + " --> same ");
			// for SMS user return the uid itself
			return uid;
		}
		if (!cachedSUid.containsKey(uid)) {
			int suid = Utility.loadIntSetting(context, UID2SUID + uid, uid);
			// Log.d("communicator",
			// "MIGRATE getSUid: uid=" + uid + " --> " + r);
			cachedSUid.put(uid, suid);
		}
		return cachedSUid.get(uid);
	}

	// ------------------------------------------------------------------------

	/** The cached server id mapping from uid. */
	private static HashMap<Integer, Integer> cachedServerId = new HashMap<Integer, Integer>();

	/**
	 * Gets the server id from an UID. Note that for the userlist now internally
	 * only UIDs are used in place of SUIDs. SUIDs are only unique for a server
	 * (S). If no mapping was found, the legacy-fallback will return
	 * cryptsecure.org as the server.
	 * 
	 * @param context
	 *            the context
	 * @param sUid
	 *            the s uid
	 * @return the server id
	 */
	public static int getServerId(Context context, int uid) {
		if (uid < 0) {
			// for SMS user return -1
			return -1;
		}
		if (!cachedServerId.containsKey(uid)) {
			int serverId = Utility.loadIntSetting(context, UID2SERVERID + uid,
					getServerId(DEFAULT_SERVER));
			cachedServerId.put(uid, serverId);
		}
		return cachedServerId.get(uid);
	}

	// ------------------------------------------------------------------------

	/**
	 * Gets the default server id. E.g., this is responsible first for the
	 * display name/uid of the current user.
	 * 
	 * @param context
	 *            the context
	 * @return the server default id
	 */
	public static int getServerDefaultId(Context context) {
		List<Integer> serverIds = Setup.getServerIds(context);
		int serverId = -1;
		if (serverIds.size() > Setup.DEFAULTSERVERINDEX) {
			serverId = serverIds.get(Setup.DEFAULTSERVERINDEX);
		}
		return serverId;
	}

	// ------------------------------------------------------------------------

	/**
	 * Gets the current version. This gets the last version of the database
	 * before this updated app ran. Typically isUpdateVersion may be sufficient.
	 * If you need to process different update stategies depending on the exact
	 * version, user this method. Be sure to trigger ALL recovery actions and
	 * setVersionUpdated() afterwards.
	 * 
	 * @param context
	 *            the context
	 * @return the current version
	 */
	public static int getVersion(Context context) {
		return Utility.loadIntSetting(context, "programversion", 0);
	}

	// ------------------------------------------------------------------------

	/**
	 * Sets the version to a specific other intermediate one. Do this after you
	 * processed your recovery action. You may also select if a reboot of the
	 * application is required.
	 * 
	 * @param context
	 *            the context
	 * @return the int
	 */
	public static void setVersionUpdated(Context context, int updatedVersion,
			boolean requiresAppReboot, String messageToUser) {
		Utility.saveIntSetting(context, "programversion", updatedVersion);
		if (messageToUser != null) {
			Utility.showToastAsync(context, messageToUser);
		}
		if (requiresAppReboot) {
			Main.exitApplication(context);
		}
	}

	// ------------------------------------------------------------------------

	/**
	 * Sets the version to the most updated one. Do this after you processed
	 * your recovery action.
	 * 
	 * @param context
	 *            the context
	 * @return the int
	 */
	public static void setVersionUpdated(Context context) {
		Utility.saveIntSetting(context, "programversion", Setup.VERSION_CURRENT);
	}

	// ------------------------------------------------------------------------

	/**
	 * Checks if is is an updated program version. Be sure to trigger ALL
	 * recovery actions and setVersionUpdated() afterwards.
	 * 
	 * @param context
	 *            the context
	 * @return true, if is updates version
	 */
	public static boolean isVersionUpdated(Context context) {
		return (getVersion(context) < Setup.VERSION_CURRENT);
	}

	// ------------------------------------------------------------------------

	/** The cached server label mapping from serverid. */
	private static HashMap<Integer, String> cachedServerLabel = new HashMap<Integer, String>();

	/**
	 * Gets the server label if either forceDisplay is true or there is more
	 * than one server configured.
	 * 
	 * @param context
	 *            the context
	 * @param serverId
	 *            the server id
	 * @param forceDisplay
	 *            the force display
	 * @return the server label
	 */
	public static String getServerLabel(Context context, int serverId,
			boolean forceDisplay) {
		if (serverId == -1) {
			return "";
		}
		if (getServers(context).size() < 2 && !forceDisplay) {
			return "";
		}
		if (!cachedServerLabel.containsKey(serverId)) {
			String returnLabel = getServer(context, serverId).replace(
					"http://", "").replace("www.", "");

			int i = returnLabel.indexOf("/");
			if (i > 0) {
				returnLabel = returnLabel.substring(0, i);
			}
			cachedServerLabel.put(serverId, returnLabel);
		}
		return cachedServerLabel.get(serverId);
	}

	// ------------------------------------------------------------------------

	/**
	 * Enable the server. If forceRefresh is true, then do it with a small
	 * delay.
	 * 
	 * @param context
	 *            the context
	 */
	public void enableServer(final Context context, final boolean forceRefresh,
			final boolean silent) {
		if (isServerActive(context, selectedServerId, true)) {
			// Do not do anything if the server IS online, maybe just update the
			// image
			updateServerImage(context, true, forceRefresh);
			return;
		}

		setServerActive(context, selectedServerId, true);

		// Update the server key / attachment
		saveHaveAskedForEncryption(context, false);
		updateServerkey(context, selectedServerId, false);
		updateAttachmentServerLimit(context, true, selectedServerId);

		// If the user has enabled encryption, update his key cause
		// this was skipped - ask before if the server is still
		// active or already has been flagged as non reachable by
		// updateServerkey()!
		if (isEncryptionEnabled(context)
				&& isServerActive(context, selectedServerId, forceRefresh)) {
			sendCurrentKeyToServer(context, selectedServerId, silent);
		}

		updateServerImage(context, true, forceRefresh);
	}

	// ------------------------------------------------------------------------

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onStop()
	 */
	@Override
	public void onStop() {
		if (restartRequiredFlag) {
			Utility.showToastAsync(
					this,
					"CryptSecure must be re-started in order to operate properly...");
			Main.exitApplication(this);
		}
		System.gc();
		super.onStop();
	}

	// ------------------------------------------------------------------------

	/**
	 * Checks if is update avatar.
	 * 
	 * @param context
	 *            the context
	 * @param uid
	 *            the uid
	 * @return true, if is update avatar
	 */
	public static boolean isUpdateAvatar(Context context, int uid) {
		// Only update registered users where the option [x] autoupdate is set!
		// OR if this avatar was NOT manually edited (meaning it came from
		// the server --> then we want for example to delete/update it
		return (uid >= 0 && (Utility.loadBooleanSetting(context,
				Setup.SETTINGS_UPDATEAVATAR + uid,
				Setup.SETTINGS_DEFAULT_UPDATEAVATAR) || !Setup
				.isAvatarModified(context, uid)));
	}

	// ------------------------------------------------------------------------

	/**
	 * Sets the update avatar.
	 * 
	 * @param context
	 *            the context
	 * @param uid
	 *            the uid
	 * @param automaticUpdate
	 *            the automatic update
	 */
	public static void setUpdateAvatar(Context context, int uid,
			boolean automaticUpdate) {
		Utility.saveBooleanSetting(context, Setup.SETTINGS_UPDATEAVATAR + uid,
				automaticUpdate);
	}

	// ------------------------------------------------------------------------

	/**
	 * Save avatar is modified.
	 * 
	 * @param context
	 *            the context
	 * @param uid
	 *            the uid
	 * @param isModified
	 *            the is modified
	 */
	public static void saveAvatarIsModified(Context context, int uid,
			boolean isModified) {
		Utility.saveBooleanSetting(context, Setup.SETTINGS_AVATARISMODIFIED
				+ uid, isModified);
	}

	// ------------------------------------------------------------------------

	/**
	 * Checks if is avatar modified.
	 * 
	 * @param context
	 *            the context
	 * @param uid
	 *            the uid
	 * @return true, if is avatar modified
	 */
	public static boolean isAvatarModified(Context context, int uid) {
		// for NOT registered users this is always true!
		if (uid < 0) {
			return true;
		}
		return Utility.loadBooleanSetting(context,
				Setup.SETTINGS_AVATARISMODIFIED + uid, false);
	}

	// -------------------------------------------------------------------------

	/**
	 * Save avatar.
	 * 
	 * @param context
	 *            the context
	 * @param uid
	 *            the uid
	 * @param avatar
	 *            the avatar
	 * @param manual
	 *            the manual
	 */
	public static void saveAvatar(Context context, int uid, String avatar,
			boolean manual) {
		if (!manual) {
			// this avatar comes from the server...
			saveAvatarIsModified(context, uid, false);
		} else {
			// this avatar was entered manually in the user details dialog
			saveAvatarIsModified(context, uid, true);
		}
		Utility.saveStringSetting(context, Setup.AVATAR + uid, avatar);
		avatars.clear();
		// Should invalidate Main's and Conversations avatar cache!
		Main.invalidateAvatarCache();
		Conversation.invalidateAvatarCache();
	}

	// -------------------------------------------------------------------------

	/**
	 * Gets the avatar.
	 * 
	 * @param context
	 *            the context
	 * @param uid
	 *            the uid
	 * @return the avatar
	 */
	public static String getAvatar(Context context, int uid) {
		return Utility.loadStringSetting(context, Setup.AVATAR + uid, null);
	}

	// -------------------------------------------------------------------------

	static HashMap<Integer, Bitmap> avatars = new HashMap<Integer, Bitmap>();

	/**
	 * Gets the avatar.
	 * 
	 * @param context
	 *            the context
	 * @param uid
	 *            the uid
	 * @return the avatar
	 */
	public static Bitmap getAvatarAsBitmap(Context context, int uid) {
		if (!avatars.containsKey(uid)) {
			String avatar = getAvatar(context, uid);
			if (avatar == null) {
				avatars.put(uid, null);
			} else {
				Bitmap bitmap = Utility.loadImageFromBASE64String(context,
						avatar);
				avatars.put(uid, bitmap);
			}
		}
		return avatars.get(uid);
	}

	// -------------------------------------------------------------------------

	/**
	 * Have avatar.
	 * 
	 * @param context
	 *            the context
	 * @param uid
	 *            the uid
	 * @return true, if successful
	 */
	public static boolean haveAvatar(Context context, int uid) {
		String avatar = getAvatar(context, uid);
		return (avatar != null && avatar.length() > 0);
	}

	// -------------------------------------------------------------------------

	/**
	 * Sets the auto added.
	 * 
	 * @param context
	 *            the context
	 * @param uid
	 *            the uid
	 * @param add
	 *            the add
	 */
	public static void setAutoAdded(Context context, int uid, boolean add) {
		String separatedString = Utility.loadStringSetting(context,
				UID_AUTOADDED, "");
		List<String> uids = Utility.getListFromString(separatedString, ";");
		LinkedHashSet<String> uuids = new LinkedHashSet<String>();
		uuids.addAll(uids);
		if (add) {
			uuids.add(uid + "");
		} else {
			uuids.remove(uid + "");
		}
		uids = new ArrayList<String>(uuids);
		separatedString = Utility.getListAsString(uids, ";");
		Log.d("communicator", "AUTOADD setAutoAdded() SET UID_AUTOADDED:="
				+ separatedString);
		Utility.saveStringSetting(context, UID_AUTOADDED, separatedString);
	}

	// -------------------------------------------------------------------------

	/**
	 * Checks if user is auto added.
	 * 
	 * @param context
	 *            the context
	 * @param uid
	 *            the uid
	 * @return true, if is auto added
	 */
	public static boolean isAutoAdded(Context context, int uid) {
		String separatedString = Utility.loadStringSetting(context,
				UID_AUTOADDED, "");
		Log.d("communicator", "AUTOADD isAutoAdded() READ UID_AUTOADDED:="
				+ separatedString);
		List<String> uids = Utility.getListFromString(separatedString, ";");
		if (uids.contains(uid + "")) {
			return true;
		}
		return false;
	}

	// -------------------------------------------------------------------------

	/**
	 * Adds or removes user from ignored list.
	 * 
	 * @param context
	 *            the context
	 * @param uid
	 *            the uid
	 * @param add
	 *            the add
	 */
	public static void setIgnored(Context context, int uid, boolean add) {
		String separatedString = Utility.loadStringSetting(context,
				UID_IGNORED, "");
		List<String> uids = Utility.getListFromString(separatedString, ";");
		LinkedHashSet<String> uuids = new LinkedHashSet<String>();
		uuids.addAll(uids);
		if (add) {
			uuids.add(uid + "");
		} else {
			uuids.remove(uid + "");
		}
		uids = new ArrayList<String>(uuids);
		separatedString = Utility.getListAsString(uids, ";");
		Log.d("communicator", "AUTOADD setIgnored() SET UID_IGNORED:="
				+ separatedString);
		Utility.saveStringSetting(context, UID_IGNORED, separatedString);
	}

	// -------------------------------------------------------------------------

	/**
	 * Checks if uid is ignored.
	 * 
	 * @param context
	 *            the context
	 * @param uid
	 *            the uid
	 * @return true, if is ignored
	 */
	public static boolean isIgnored(Context context, int uid) {
		String separatedString = Utility.loadStringSetting(context,
				UID_IGNORED, "");
		Log.d("communicator", "AUTOADD isIgnored() UID_IGNORED="
				+ separatedString);

		List<String> uids = Utility.getListFromString(separatedString, ";");
		if (uids.contains(uid + "")) {
			return true;
		}
		return false;
	}

	// -------------------------------------------------------------------------

	/**
	 * Reset ignored users.
	 * 
	 * @param context
	 *            the context
	 */
	public static void resetIgnored(Context context) {
		Log.d("communicator", "AUTOADD resetIgnored()");
		Utility.saveStringSetting(context, UID_IGNORED, "");
	}

	// -------------------------------------------------------------------------

	/**
	 * The flag for currently prompting auto adding. Do not prompt another
	 * window.
	 */
	private static boolean promptingAutoAdding = false;

	/**
	 * Possibly prompt auto added user if there are any and ask the user to
	 * confirm the adding. Otherwise remove these users and possibly ignore
	 * them!
	 * 
	 * @param context
	 *            the context
	 */
	public static void possiblyPromptAutoAddedUser(final Context context) {
		if (promptingAutoAdding) {
			// Do not prompt again if we already prompt!
			return;
		}
		promptingAutoAdding = true;
		String separatedString = Utility.loadStringSetting(context,
				UID_AUTOADDED, "");
		Log.d("communicator",
				"AUTOADD possiblyPromptAutoAddedUser() UID_AUTOADDED="
						+ separatedString);

		List<String> uids = Utility.getListFromString(separatedString, ";");
		if (uids.size() > 0) {
			// Get only one user at time!
			final int uid = Utility.parseInt(uids.get(0), -1);
			if (uid == -1) {
				// heavy failure... remove all autoadded for
				Utility.saveStringSetting(context, UID_AUTOADDED, "");
				promptingAutoAdding = false;
				return;
			}

			// Okay we need to prompt the user!
			try {
				final String titleMessage = "Confirm Added User ";
				final String name = Main.UID2Name(context, uid, false);
				final int suid = Setup.getSUid(context, uid);
				final String server = Setup.getServerLabel(context,
						Setup.getServerId(context, uid), true);
				final String textMessage = name
						+ " with UID "
						+ suid
						+ " of server '"
						+ server
						+ "'"
						+ " was added automatically.\n\nYou should ONLY confirm the auto adding if you know "
						+ name
						+ "!"
						+ "\nOtherwise you should ignore this user. If you select 'Cancel' you only reject the auto adding this time.\n\nDo you want to confirm the adding of "
						+ name + " or ignore " + name + " permanently?";
				new MessageAlertDialog(context, titleMessage, textMessage,
						"Confirm", " Ignore ", " Cancel ",
						new MessageAlertDialog.OnSelectionListener() {
							public void selected(int button, boolean cancel) {
								// Anyways remove him from autoadded because we
								// prompted the user
								Setup.setAutoAdded(context, uid, false);
								if (!cancel) {
									if (button == 0) {
										// CONFIRM == REMOVE IT FROM AUTOADDED
										// (AS WE DID BEFORE)
										// AND UPDATE THE UIDS NOW
										int serverId = Setup.getServerId(
												context, uid);
										Setup.backup(context, true, false,
												serverId);
									} else if (button == 1) {
										// IGNORE == FULL PROGRAM
										Setup.setIgnored(context, uid, true);
										Main.deleteUser(context, uid);
									}
								} else {
									// Cancel === REJECT THIS TIME BUT DO NOT
									// ADD TO IGNORE LISTY
									Main.deleteUser(context, uid);
								}
								promptingAutoAdding = false;
							}
						}).show();
			} catch (Exception e) {
				// Ignore
			}
		} else {
			promptingAutoAdding = false;
		}
	}

	// -------------------------------------------------------------------------

	public static void updateIgnoreSpinner(final Context context,
			final Spinner spinner) {
		String separatedString = Utility.loadStringSetting(context,
				UID_IGNORED, "");
		List<String> uids = Utility.getListFromString(separatedString, ";");
		List<String> displayUsers = new ArrayList<String>();
		for (String uidString : uids) {
			int uid = Utility.parseInt(uidString, -1);
			if (uid != -1) {
				displayUsers.add(Setup.getSUid(context, uid)
						+ "@"
						+ Setup.getServerLabel(context,
								Setup.getServerId(context, uid), true));
			} else {
				displayUsers.add(uidString + " @ unknownhost");
			}
		}
		ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(
				context, android.R.layout.simple_spinner_item, displayUsers);
		spinnerArrayAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(spinnerArrayAdapter);
	}

	// ------------------------------------------------------------------------

	/*
	 * Create a group.
	 * 
	 * @param context the context
	 * 
	 * @param uid the uid
	 */
	public static void groupCreate(final Context context, final int serverId,
			final String groupName) {
		String session = Setup.getTmpLoginEncoded(context, serverId);
		if (session == null) {
			setErrorInfo(context,
					"Session error. Try again after restarting the App.");
			// error resume is automatically done by getTmpLogin, not logged in
			return;
		}
		String url = null;
		url = Setup.getBaseURL(context, serverId)
				+ "cmd=creategroup&session="
				+ session
				+ "&val="
				+ Utility
						.urlEncode(Setup.encText(context, groupName, serverId));
		final String url2 = url;
		Log.d("communicator", "XXXX REQUEST groupCreate :" + url2);
		@SuppressWarnings("unused")
		HttpStringRequest httpStringRequest = (new HttpStringRequest(context,
				url2, new HttpStringRequest.OnResponseListener() {
					public void response(String response) {
						Log.d("communicator", "XXXX RESPONSE groupCreate :"
								+ response);
						if (Communicator.isResponseValid(response)) {
							// Log.d("communicator",
							// "XXXX RESPONSE2 addUser :"+response);
							if (Communicator.isResponsePositive(response)) {
								String responseContent = Communicator
										.getResponseContent(response);

								String[] responseValues = responseContent
										.split("#");
								if (responseValues.length == 2) {
									String groupid = responseValues[0];
									String groupsecret = responseValues[1];
									groupid = Setup.decUid(context, groupid,
											serverId) + "";
									groupsecret = Setup.decText(context,
											groupsecret, serverId);
									Setup.setGroupName(context, serverId,
											groupid, groupName);
									Setup.setGroupSecret(context, serverId,
											groupid, groupsecret);
									setErrorInfoAsync(context, "Group '"
											+ groupName + "' created.", false);
									Setup.updateGroupSpinnerAsync(context,
											groupspinner);
									Setup.updateGroupsFromServer(context, true,
											serverId);
								} else {
									setErrorInfoAsync(context,
											"Error creating group '"
													+ groupName + "'.", true);
								}
							} else {
								setErrorInfoAsync(context,
										"Error creating group '" + groupName
												+ "'. Not logged in.", true);
							}
						} else {
							setErrorInfoAsync(context, "Error creating group '"
									+ groupName + "'. Server error.", true);
						}
					}
				}));
	}

	// -------------------------------------------------------------------------

	/*
	 * Rename a group.
	 * 
	 * @param context the context
	 * 
	 * @param uid the uid
	 */
	public static void groupRename(final Context context, final int serverId,
			final String groupId, final String newGroupName) {
		String session = Setup.getTmpLoginEncoded(context, serverId);
		if (session == null) {
			setErrorInfo(context,
					"Session error. Try again after restarting the App.");
			// error resume is automatically done by getTmpLogin, not logged in
			return;
		}
		String url = null;
		url = Setup.getBaseURL(context, serverId)
				+ "cmd=renamegroup&session="
				+ session
				+ "&val="
				+ Setup.encUid(context, Utility.parseInt(groupId, -1), serverId)
				+ "&val1="
				+ Utility.urlEncode(Setup.encText(context, newGroupName,
						serverId));
		final String url2 = url;
		Log.d("communicator", "XXXX REQUEST groupRename :" + url2);
		@SuppressWarnings("unused")
		HttpStringRequest httpStringRequest = (new HttpStringRequest(context,
				url2, new HttpStringRequest.OnResponseListener() {
					public void response(String response) {
						Log.d("communicator", "XXXX RESPONSE groupRename :"
								+ response);
						if (Communicator.isResponseValid(response)) {
							if (Communicator.isResponsePositive(response)
									&& response.equals("1")) {
								Setup.setGroupName(context, serverId, groupId,
										newGroupName);
								setErrorInfoAsync(context, "Group '"
										+ newGroupName + "' renamed.", false);
								Setup.updateGroupSpinnerAsync(context,
										groupspinner);
							} else {
								setErrorInfoAsync(
										context,
										"Error renaming group '"
												+ newGroupName
												+ "'. Not logged in or not a member of the group.",
										true);
							}
						} else {
							setErrorInfoAsync(context, "Error renaming group '"
									+ newGroupName + "'. Server error.", true);
						}
					}
				}));
	}

	// -------------------------------------------------------------------------

	/*
	 * Quit membership for a group.
	 * 
	 * @param context the context
	 * 
	 * @param uid the uid
	 */
	public static void groupQuit(final Context context, final int serverId,
			final String groupId) {
		String session = Setup.getTmpLoginEncoded(context, serverId);
		if (session == null) {
			setErrorInfo(context,
					"Session error. Try again after restarting the App.");
			// error resume is automatically done by getTmpLogin, not logged in
			return;
		}
		final String groupName = Setup.getGroupName(context, serverId, groupId);
		String url = null;
		url = Setup.getBaseURL(context, serverId)
				+ "cmd=quitgroup&session="
				+ session
				+ "&val="
				+ Setup.encUid(context, Utility.parseInt(groupId, -1), serverId);
		final String url2 = url;
		Log.d("communicator", "XXXX REQUEST groupQuit :" + url2);
		@SuppressWarnings("unused")
		HttpStringRequest httpStringRequest = (new HttpStringRequest(context,
				url2, new HttpStringRequest.OnResponseListener() {
					public void response(String response) {
						Log.d("communicator", "XXXX RESPONSE groupQuit :"
								+ response);
						if (Communicator.isResponseValid(response)) {
							if (Communicator.isResponsePositive(response)
									&& response.equals("1")) {
								Setup.setGroupName(context, serverId, groupId,
										groupName);
								setErrorInfoAsync(context,
										"Quit membership for group '"
												+ groupName + "' successfull.",
										false);
								Setup.setGroupName(context, serverId, groupId,
										null);
								Setup.setGroupSecret(context, serverId,
										groupId, null);
								Setup.updateGroupsFromServer(context, true,
										serverId);
							} else {
								setErrorInfoAsync(
										context,
										"Error quitting membership for group '"
												+ groupName
												+ "'. Not logged in or not a member of the group.",
										true);
							}
						} else {
							setErrorInfoAsync(context,
									"Error quitting  membership for group '"
											+ groupName + "'. Server error.",
									true);
						}
					}
				}));
	}

	// -------------------------------------------------------------------------

	/*
	 * Invite another user to a group that we already are a member of.
	 * 
	 * @param context the context
	 * 
	 * @param uid the uid
	 */
	public static void groupInvite(final Context context, final int serverId,
			final String groupId, final int hostUid) {
		String session = Setup.getTmpLoginEncoded(context, serverId);
		if (session == null) {
			setErrorInfo(context,
					"Session error. Try again after restarting the App.");
			// error resume is automatically done by getTmpLogin, not logged in
			return;
		}
		// final String groupName = Setup.getGroupName(context, serverId,
		// groupId);
		String url = null;
		url = Setup.getBaseURL(context, serverId)
				+ "cmd=invitegroup&session="
				+ session
				+ "&val="
				+ Setup.encUid(context, Utility.parseInt(groupId, -1), serverId)
				+ "&host=" + Setup.encUid(context, hostUid, serverId);
		final String url2 = url;
		Log.d("communicator", "XXXX REQUEST groupInvite :" + url2);
		@SuppressWarnings("unused")
		HttpStringRequest httpStringRequest = (new HttpStringRequest(context,
				url2, new HttpStringRequest.OnResponseListener() {
					public void response(String response) {
						Log.d("communicator", "XXXX RESPONSE groupInvite :"
								+ response);
						if (Communicator.isResponseValid(response)) {
							if (Communicator.isResponsePositive(response)
									&& response.equals("1")) {
								// Invited successfully, s
							}
						}
					}
				}));
	}

	// -------------------------------------------------------------------------

	/*
	 * Confirm a group invitation.
	 * 
	 * @param context the context
	 * 
	 * @param uid the uid
	 */
	public static void groupConfirm(final Context context, final int serverId,
			final String groupsecret) {
		String session = Setup.getTmpLoginEncoded(context, serverId);
		if (session == null) {
			setErrorInfo(context,
					"Session error. Try again after restarting the App.");
			// error resume is automatically done by getTmpLogin, not logged in
			return;
		}
		// final String groupName = Setup.getGroupName(context, serverId,
		// groupId);
		String url = null;
		url = Setup.getBaseURL(context, serverId)
				+ "cmd=confirmgroup&session="
				+ session
				+ "&val="
				+ Utility.urlEncode(Setup.encText(context, groupsecret,
						serverId));
		final String url2 = url;

		Log.d("communicator", "XXXX REQUEST groupConfirm groupsecret="
				+ groupsecret);
		Log.d("communicator", "XXXX REQUEST groupConfirm :" + url2);
		@SuppressWarnings("unused")
		HttpStringRequest httpStringRequest = (new HttpStringRequest(context,
				url2, new HttpStringRequest.OnResponseListener() {
					public void response(String response) {
						boolean ok = false;
						Log.d("communicator",
								"XXXX RESPONSE groupConfirm ("
										+ response.length() + ") :"
										+ response.replace("\n", ""));
						if (Communicator.isResponseValid(response)) {
							if (Communicator.isResponsePositive(response)
									&& response.startsWith("1")) {
								String responseContent = Communicator
										.getResponseContent(response);
								Log.d("communicator",
										"XXXX RESPONSE groupConfirm responseContent="
												+ responseContent);
								if (responseContent != null
										&& responseContent.length() > 0) {
									int groupId = Setup.decUid(context,
											responseContent, serverId);
									if (groupId != -1) {
										// Invited successfully, s
										Setup.setGroupSecret(context, serverId,
												groupId + "", groupsecret);
										Utility.showToastAsync(context,
												"Join group completed.");
										ok = true;
										// reload and rebuild userlist
										Setup.updateGroupsFromServer(context,
												true, serverId);
									}
								}
							}
						}
						if (!ok) {
							Utility.showToastAsync(context,
									"An error occurred when trying to join the group.");
						}
					}
				}));
	}

	// -------------------------------------------------------------------------

	// public static final String GROUPNAME = "servergroupname";
	// public static final String GROUPSECRET = "servergroupsecret";
	// public static final String GROUPS = "servergroups";
	// public static final String GROUPMEMBERS = "servergroups";
	// INVITEDGROUPS

	// -------------------------------------------------------------------------

	public static void setGroupName(Context context, int serverId,
			String groupId, String name) {
		Utility.saveStringSetting(context,
				GROUPNAME + serverId + "_" + groupId, name);
		localGroupId2Name.clear();
	}

	public static String getGroupName(Context context, int serverId,
			String groupId) {
		return Utility.loadStringSetting(context, GROUPNAME + serverId + "_"
				+ groupId, null);
	}

	public static HashMap<Integer, String> localGroupId2Name = new HashMap<Integer, String>();

	public static String getGroupName(Context context, int localGroupId) {
		if (!localGroupId2Name.containsKey(localGroupId)) {
			int serverId = getGroupServerId(context, localGroupId);
			String groupId = getGroupId(context, localGroupId);
			Log.d("communicator", "GROUPS serverId=" + serverId + ", groupId="
					+ groupId);
			localGroupId2Name.put(localGroupId,
					getGroupName(context, serverId, groupId));
		}
		return localGroupId2Name.get(localGroupId);
	}

	// -------------------------------------------------------------------------

	public static void setGroupSecret(Context context, int serverId,
			String groupId, String secret) {
		Utility.saveStringSetting(context, GROUPSECRET + serverId + "_"
				+ groupId, secret);
		localGroupId2Secret.clear();
	}

	public static String getGroupSecret(Context context, int serverId,
			String groupId) {
		return Utility.loadStringSetting(context, GROUPSECRET + serverId + "_"
				+ groupId, null);
	}

	public static HashMap<Integer, String> localGroupId2Secret = new HashMap<Integer, String>();

	public static String getGroupSecret(Context context, int localGroupId) {
		if (!localGroupId2Secret.containsKey(localGroupId)) {
			int serverId = getGroupServerId(context, localGroupId);
			String groupId = getGroupId(context, localGroupId);
			Log.d("communicator", "GROUPS serverId=" + serverId + ", groupId="
					+ groupId);
			localGroupId2Secret.put(localGroupId,
					getGroupSecret(context, serverId, groupId));
		}
		return localGroupId2Secret.get(localGroupId);
	}

	// -------------------------------------------------------------------------

	public static void setInvitedGroups(Context context, int serverId,
			String groups) {
		Utility.saveStringSetting(context, INVITEDGROUPS + serverId, groups);
	}

	public static String getInvitedGroups(Context context, int serverId) {
		return Utility.loadStringSetting(context, INVITEDGROUPS + serverId,
				null);
	}

	public static List<String> getInvitedGroupsList(Context context,
			int serverId) {
		String separatedString = getInvitedGroups(context, serverId);
		List<String> groupids = Utility.getListFromString(separatedString, ",");
		return groupids;
	}

	// -------------------------------------------------------------------------

	public static void setGroups(Context context, int serverId, String groups) {
		invalidateGroupCache();
		Utility.saveStringSetting(context, GROUPS + serverId, groups);
	}

	public static String getGroups(Context context, int serverId) {
		return Utility.loadStringSetting(context, GROUPS + serverId, null);
	}

	public static List<String> getGroupsList(Context context, int serverId) {
		String separatedString = getGroups(context, serverId);
		List<String> groupids = Utility.getListFromString(separatedString, ",");
		return groupids;
	}

	public static void invalidateGroupCache() {
		LocalGroup2GroupIdCache.clear();
		LocalGroup2ServerIdCache.clear();
		LocalGroupSecret2LocalId.clear();
	}

	static HashMap<Integer, String> LocalGroup2GroupIdCache = new HashMap<Integer, String>();
	static HashMap<Integer, Integer> LocalGroup2ServerIdCache = new HashMap<Integer, Integer>();
	static HashMap<String, Integer> LocalGroupSecret2LocalId = new HashMap<String, Integer>();

	public static int getLocalIdBySecret(Context context, int serverId,
			String groupSecret) {
		String id = serverId + "_" + groupSecret;
		if (!LocalGroupSecret2LocalId.containsKey(id)) {
			int localGroup = Utility.loadIntSetting(context, SECRET2LOCALGROUP
					+ id, -1);
			LocalGroupSecret2LocalId.put(id, localGroup);
		}
		return LocalGroupSecret2LocalId.get(id);
	}

	public static String getGroupId(Context context, int localGroupId) {
		if (!LocalGroup2GroupIdCache.containsKey(localGroupId)) {
			LocalGroup2GroupIdCache.put(
					localGroupId,
					Utility.loadStringSetting(context, LOCALGROUP2GROUPID
							+ localGroupId, null));
		}
		return LocalGroup2GroupIdCache.get(localGroupId);
	}

	public static int getGroupServerId(Context context, int localGroupId) {
		try {
			if (!LocalGroup2ServerIdCache.containsKey(localGroupId)) {
				int serverId = Utility.loadIntSetting(context,
						LOCALGROUP2SERVER + localGroupId, -1);
				if (serverId != -1) {
					LocalGroup2ServerIdCache.put(localGroupId, serverId);
				}
			}
			return LocalGroup2ServerIdCache.get(localGroupId);
		} catch (Exception e) {
			e.printStackTrace();
			invalidateGroupCache();
		}
		return -1;
	}

	public static int getLocalGroupId(Context context, String groupId,
			int serverId) {
		int groupIdInt = Utility.parseInt(groupId, -1);
		if (groupIdInt > 10000) {
			// ATTENTION: Return the groupId as localgroupid if this IS already
			// the local id. This means you can only apply this method once
			// to a localid
			return groupIdInt;
		}

		int localGroupId = Math.abs((serverId + "_" + groupId).hashCode());
		if (getGroupId(context, localGroupId) == null) {
			Utility.saveStringSetting(context, LOCALGROUP2GROUPID
					+ localGroupId, groupId);
		}
		if (getGroupServerId(context, localGroupId) == -1) {
			Utility.saveIntSetting(context, LOCALGROUP2SERVER + localGroupId,
					serverId);
		}
		String groupSecret = Setup.getGroupSecret(context, serverId, groupId);
		if (getLocalIdBySecret(context, serverId, groupSecret) == -1) {
			Utility.saveIntSetting(context, SECRET2LOCALGROUP + serverId + "_"
					+ groupSecret, localGroupId);
		}
		return localGroupId;
	}

	public static boolean isGroup(Context context, int uid) {
		return (getGroupId(context, uid) != null);
	}

	// -------------------------------------------------------------------------

	public static void setGroupMembers(Context context, int serverId,
			String groupId, String groupmembers) {
		
		//Log.d("communicator", "GROUPMEMBERS groupId=" + groupId + ", serverId=" + serverId + ", members=" + groupmembers);
		Utility.saveStringSetting(context, GROUPMEMBERS + serverId + "_"
				+ groupId, groupmembers);
	}

	public static String getGroupMembers(Context context, int serverId,
			String groupId) {
		String groupmembers = Utility.loadStringSetting(context, GROUPMEMBERS + serverId + "_"
				+ groupId, null);
		//Log.d("communicator", "GROUPMEMBERS groupId=" + groupId + ", serverId=" + serverId + ", members=" + groupmembers);
		return groupmembers;
	}

	public static List<Integer> getGroupMembersList(Context context,
			int serverId, String groupId) {
		String separatedString = getGroupMembers(context, serverId, groupId);
		List<Integer> groupids = Utility.getListFromString(separatedString,
				",", -1);
		return groupids;
	}

	// -------------------------------------------------------------------------

	public static void updateGroupsFromAllServers(final Context context,
			final boolean forceUpdate) {
		for (int serverId : Setup.getServerIds(context)) {
			if (Setup.isServerAccount(context, serverId, false)) {
				updateGroupsFromServer(context, forceUpdate, serverId);
			}
		}
	}

	// -------------------------------------------------------------------------

	public static void updateGroupsFromServer(final Context context,
			final boolean forceUpdate, final int serverId) {
		Log.d("communicator", "###### REQUEST UPDATE GROUPS");

		long lastTime = Utility.loadLongSetting(context,
				Setup.SETTING_LASTUPDATEGROUPS + serverId, 0);
		long currentTime = DB.getTimestamp();
		if (!forceUpdate
				&& (lastTime + Setup.UPDATE_GROUPS_MIN_INTERVAL + serverId > currentTime)) {
			// Do not do this more frequently
			return;
		}
		Utility.saveLongSetting(context, Setup.SETTING_LASTUPDATEGROUPS
				+ serverId, currentTime);

		// Update
		groupsUpdate(context, serverId);
	}

	// -------------------------------------------------------------------------

	/*
	 * Update group list
	 * 
	 * @param context the context
	 * 
	 * @param uid the uid
	 */
	public static void groupsUpdate(final Context context, final int serverId) {
		String session = Setup.getTmpLoginEncoded(context, serverId);
		if (session == null) {
			setErrorInfo(context,
					"Session error. Try again after restarting the App.");
			// error resume is automatically done by getTmpLogin, not logged in
			return;
		}
		String url = null;
		url = Setup.getBaseURL(context, serverId) + "cmd=getgroups&session="
				+ session;
		final String url2 = url;
		Log.d("communicator", "XXXX REQUEST groupsUpdate :" + url2);
		@SuppressWarnings("unused")
		HttpStringRequest httpStringRequest = (new HttpStringRequest(context,
				url2, new HttpStringRequest.OnResponseListener() {
					public void response(String response) {
						Log.d("communicator", "XXXX RESPONSE groupsUpdate :"
								+ response);
						if (Communicator.isResponseValid(response)) {
							// Log.d("communicator",
							// "XXXX RESPONSE2 addUser :"+response);
							if (Communicator.isResponsePositive(response)) {
								String responseContent = Communicator
										.getResponseContent(response);

								if (responseContent != null
										&& responseContent.length() > 1) {

									if (responseContent.startsWith("#")) {
										// Remove preceeding '#' because a
										// request starts with
										// 1##groupid1#groupname1#m1#mw#m...##groupid2#groupname2#m1#m...
										responseContent = responseContent
												.substring(1);
									}

									String[] responseValues = responseContent
											.split("##");
									if (responseValues != null
											&& responseValues.length > 0) {
										List<String> groupIds = new ArrayList<String>();

										for (String responseValue : responseValues) {
											// one group value:
											// ## id # name # member1 # member2
											// ... # membern
											String[] groupValues = responseValue
													.split("#");
											if (groupValues != null
													&& groupValues.length > 1) {
												// at least we need an id and a
												// name for each group! maybe no
												// other
												// members than ourself
												String groupId = Setup.decUid(
														context,
														groupValues[0],
														serverId)
														+ "";
												String groupName = Setup
														.decText(context,
																groupValues[1],
																serverId);

												groupIds.add(groupId);
												Setup.setGroupName(context,
														serverId, groupId,
														groupName);

												Log.d("communicator",
														"XXXX RESPONSE groupsUpdate groupId="
																+ groupId
																+ ", groupName="
																+ groupName);

												if (groupValues.length > 2) {
													List<Integer> memberUids = new ArrayList<Integer>();
													for (int i = 2; i < groupValues.length; i++) {
														String uidEncrypted = groupValues[i];
														int memberUid = Setup
																.decUid(context,
																		uidEncrypted,
																		serverId);

														Log.d("communicator",
																"XXXX RESPONSE groupMembersUpdate member of group "
																		+ groupId
																		+ " is "
																		+ memberUid);

														memberUids
																.add(memberUid);
													}
													Setup.setGroupMembers(
															context,
															serverId,
															groupId,
															Utility.getListAsString(
																	memberUids,
																	","));
												} else {
													// We are the only member
													Setup.setGroupMembers(
															context, serverId,
															groupId, "");
												}
											}
										}

										// now all groupIds are gatherd, save
										// them
										// We are the only member
										Setup.setGroups(context, serverId,
												Utility.getListAsString(
														groupIds, ","));
										Setup.updateGroupSpinnerAsync(context,
												groupspinner);
									} else {
										// No groups we are in
										Setup.setGroups(context, serverId, "");
										Setup.updateGroupSpinnerAsync(context,
												groupspinner);
									}
								} else {
									// No groups we are in
									Setup.setGroups(context, serverId, "");
									Setup.updateGroupSpinnerAsync(context,
											groupspinner);
								}
							}
						}
					}
				}));
	}

	// -------------------------------------------------------------------------
	// -------------------------------------------------------------------------

	// These mappings are used to remember which group on which server is
	// selected

	private static HashMap<Integer, Integer> groupSpinnerMappingServer = new HashMap<Integer, Integer>();
	private static HashMap<Integer, String> groupSpinnerMappingGroupId = new HashMap<Integer, String>();

	public static void updateGroupSpinnerAsync(final Context context,
			final Spinner spinner) {
		final Handler mUIHandler = new Handler(Looper.getMainLooper());
		mUIHandler.post(new Thread() {
			@Override
			public void run() {
				super.run();
				updateGroupSpinner(context, spinner);
			}
		});
	}

	public static void updateGroupSpinner(final Context context,
			final Spinner spinner) {

		// Generate the values and remember the mapping
		groupSpinnerMappingServer.clear();
		groupSpinnerMappingGroupId.clear();
		List<String> spinnerNames = new ArrayList<String>();
		int index = 0;
		for (int serverId : Setup.getServerIds(context)) {
			if (Setup.isServerAccount(context, serverId, false)) {
				String serverLabel = Setup.getServerLabel(context, serverId,
						true);
				List<String> groupIds = Setup.getGroupsList(context, serverId);
				for (String groupId : groupIds) {
					String groupName = Setup.getGroupName(context, serverId,
							groupId);
					int groupmembers = Setup.getGroupMembersList(context,
							serverId, groupId).size() + 1;
					spinnerNames.add(groupName + "@" + serverLabel + " ("
							+ groupmembers + ")");
					groupSpinnerMappingServer.put(index, serverId);
					groupSpinnerMappingGroupId.put(index, groupId);
					index++;
				}
			}
		}

		if (spinner != null) {
			ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(
					context, android.R.layout.simple_spinner_item, spinnerNames);
			spinnerArrayAdapter
					.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			spinner.setAdapter(spinnerArrayAdapter);
		}
	}

	// -------------------------------------------------------------------------

	public static HashMap<Integer, String> groupSpinnerMappingGroupId2 = new HashMap<Integer, String>();

	public static void updateGroupSpinner2(final Context context,
			final int serverId, final Spinner spinner) {
		groupSpinnerMappingGroupId2.clear();

		List<String> spinnerNames = new ArrayList<String>();
		int index = 0;
		if (Setup.isServerAccount(context, serverId, false)) {
			List<String> groupIds = Setup.getGroupsList(context, serverId);
			for (String groupId : groupIds) {
				String groupName = Setup.getGroupName(context, serverId,
						groupId);
				int groupmembers = Setup.getGroupMembersList(context, serverId,
						groupId).size() + 1;
				spinnerNames.add(groupName + " (" + groupmembers + ")");
				groupSpinnerMappingGroupId2.put(index, groupId);
				index++;
			}
		}

		if (spinner != null) {
			ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(
					context, android.R.layout.simple_spinner_item, spinnerNames);
			spinnerArrayAdapter
					.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			spinner.setAdapter(spinnerArrayAdapter);
		}
	}

	// ------------------------------------------------------------------------

	/**
	 * Prompt quit. The activity is passed if on deletion we want to finish it
	 * like the UserDetailsActivity. For the setup activity we do not want this
	 * behavior and simply pass a null.
	 * 
	 * @param context
	 *            the context
	 * @param activity
	 *            the activity
	 */
	public static void promptQuit(final Activity activity,
			final int localGroupId) {
		try {
			String titleMessage = "Clear Conversation or Quit?";
			String textMessage = "Do you really want to quit group membership permanently?\n\nThis cannot be undone and you will need to be re-invited by some other member. If you currently are the only member the group will be removed permanently.";

			new MessageAlertDialog(activity, titleMessage, textMessage,
					"Still, QUIT!", null, " Cancel ",
					new MessageAlertDialog.OnSelectionListener() {
						public void selected(int button, boolean cancel) {
							if (!cancel) {
								if (button == 0) {
									Utility.showToastAsync(
											activity,
											"You left the group '"
													+ Main.UID2Name(activity,
															localGroupId, false)
													+ "' permanently.");
									int groupServerId = Setup.getGroupServerId(
											activity, localGroupId);
									String groupId = Setup.getGroupId(activity,
											localGroupId);
									groupQuit(activity, groupServerId, groupId);
									Main.deleteUser(activity, localGroupId);
									if (activity != null) {
										activity.finish();
									}
								}
							}
						}
					}).show();
		} catch (Exception e) {
			// Ignore
		}
	}

	// -------------------------------------------------------------------------
	// -------------------------------------------------------------------------

	/**
	 * Checks if is no darkmode so the light mode is selected.
	 *
	 * @param context the context
	 * @return true, if is no darkmode
	 */
	public static boolean isDarkmode(Context context) {
		return Utility.loadBooleanSetting(context,
				Setup.OPTION_DARKMODE, Setup.DEFAULT_DARKMODE);
	}
	
	// -------------------------------------------------------------------------
	
	public static int dolphins1(Context context) {
		if (isDarkmode(context)) {
			return R.drawable.dolphins1;
		} else {
			return R.drawable.dolphins3w;
		}
	}
	
	// -------------------------------------------------------------------------
	
	public static int dolphins2(Context context) {
		if (isDarkmode(context)) {
			return R.drawable.dolphins2;
		} else {
			return R.drawable.dolphins3w;
		}
	}
	
	// -------------------------------------------------------------------------

	
	public static int dolphins3(Context context) {
		if (isDarkmode(context)) {
			return R.drawable.dolphins3;
		} else {
			return R.drawable.dolphins2w;
		}
	}
	
	// -------------------------------------------------------------------------

	
	public static int dolphins3light(Context context) {
		if (isDarkmode(context)) {
			return R.drawable.dolphins3light;
		} else {
			return R.drawable.dolphins3lightw;
		}
	}
	
	// -------------------------------------------------------------------------

	
	// public int dolphins4(Context context) {
	// if (isDarkmode(context)) {
	// return R.drawable.dolphins4;
	// } else {
	// return R.drawable.dolphins4w;
	// }
	// }
	
	// -------------------------------------------------------------------------

	
	public static int dolphins4light(Context context) {
		if (isDarkmode(context)) {
			return R.drawable.dolphins4light;
		} else {
			return R.drawable.dolphins4lightw;
		}
	}
	
	// -------------------------------------------------------------------------

	/** The Constant TEXTCOLOEWHITE. */
	public static final int TEXTCOLORWHITE = Color.parseColor("#FFFFFFFF");

	/** The Constant TEXTCOLOEWHITEDIMMED. */
	public static final int TEXTCOLORWHITEDIMMED = Color
			.parseColor("#FFE8E8E8");

	/** The Constant TEXTCOLOEWHITEDIMMED. */
	public static final int TEXTCOLORWHITEDIMMED2 = Color
			.parseColor("#CCDDDDDD");

	/** The Constant TEXTCOLOEWHITE. */
	public static final int TEXTCOLORBLACK = Color.parseColor("#FF000000");

	/** The Constant TEXTCOLOEWHITEDIMMED. */
	public static final int TEXTCOLORBLACKDIMMED = Color
			.parseColor("#AA000000");

	/** The Constant TEXTCOLOEWHITEDIMMED. */
	public static final int TEXTCOLORBLACKDIMMED2 = Color
			.parseColor("#99000000");

	
	public static int textcolor(Context context) {
		if (isDarkmode(context)) {
			return TEXTCOLORWHITE;
		} else {
			return TEXTCOLORBLACK;
		}
	}

	public static int textcolordimmed(Context context) {
		if (isDarkmode(context)) {
			return TEXTCOLORWHITEDIMMED;
		} else {
			return TEXTCOLORBLACKDIMMED;
		}
	}

	public static int textcolordimmed2(Context context) {
		if (isDarkmode(context)) {
			return TEXTCOLORWHITEDIMMED2;
		} else {
			return TEXTCOLORBLACKDIMMED2;
		}
	}
	
}
