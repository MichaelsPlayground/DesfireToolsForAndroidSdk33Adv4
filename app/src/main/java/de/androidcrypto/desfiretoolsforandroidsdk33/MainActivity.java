package de.androidcrypto.desfiretoolsforandroidsdk33;

import static com.github.skjolber.desfire.libfreefare.MifareDesfire.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.nfc.NfcAdapter;
import android.nfc.NfcAdapter.ReaderCallback;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.github.skjolber.desfire.ev1.model.DesfireApplication;
import com.github.skjolber.desfire.ev1.model.DesfireApplicationId;
import com.github.skjolber.desfire.ev1.model.DesfireApplicationKey;
import com.github.skjolber.desfire.ev1.model.DesfireApplicationKeySettings;
import com.github.skjolber.desfire.ev1.model.DesfireTag;
import com.github.skjolber.desfire.ev1.model.VersionInfo;
import com.github.skjolber.desfire.ev1.model.command.DefaultIsoDepAdapter;
import com.github.skjolber.desfire.ev1.model.command.DefaultIsoDepWrapper;
import com.github.skjolber.desfire.ev1.model.command.Utils;
import com.github.skjolber.desfire.ev1.model.file.DesfireFile;
import com.github.skjolber.desfire.ev1.model.file.RecordDesfireFile;
import com.github.skjolber.desfire.ev1.model.file.StandardDesfireFile;
import com.github.skjolber.desfire.ev1.model.file.ValueDesfireFile;
import com.github.skjolber.desfire.ev1.model.key.Desfire3DESKey;
import com.github.skjolber.desfire.ev1.model.key.Desfire3K3DESKey;
import com.github.skjolber.desfire.ev1.model.key.DesfireAESKey;
import com.github.skjolber.desfire.ev1.model.key.DesfireDESKey;
import com.github.skjolber.desfire.ev1.model.key.DesfireKey;
import com.github.skjolber.desfire.ev1.model.key.DesfireKeyType;
import com.github.skjolber.desfire.libfreefare.MifareDESFireKey;
import com.github.skjolber.desfire.libfreefare.MifareDesfireKey1;
import com.github.skjolber.desfire.libfreefare.MifareTag;

import de.androidcrypto.desfiretoolsforandroidsdk33.filelist.ApplicationDetail;
import de.androidcrypto.desfiretoolsforandroidsdk33.filelist.ApplicationDetailApplicationKey;
import de.androidcrypto.desfiretoolsforandroidsdk33.filelist.ApplicationDetailFile;
import de.androidcrypto.desfiretoolsforandroidsdk33.filelist.ApplicationDetailKey;
import de.androidcrypto.desfiretoolsforandroidsdk33.filelist.ApplicationDetailRecord;
import de.androidcrypto.desfiretoolsforandroidsdk33.keys.DataSource;

/**
 * Note on DesfireToolsForAndroidSdk33Adv2
 * This is the same code as on Adv1 but all fragments are substituted from old/deprecated Fragment to AndroidX Fragment
 */

@SuppressLint("ResourceAsColor")
public class MainActivity extends AppCompatActivity implements ReaderCallback, FragmentManager.OnBackStackChangedListener, FileSaveFragment.Callbacks {

    private static final String ACTION_NFC_SETTINGS = "android.settings.NFC_SETTINGS";
    
    /** this action seems never to be emitted, but is here for future use */
    private static final String ACTION_TAG_LEFT_FIELD = "android.nfc.action.TAG_LOST";

	public static byte[] key_data_aes  = { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };

	public static final byte key_data_aes_version = 0x42;

	private static final String TAG = MainActivity.class.getName();
	
	private interface OnKeyListener {
		void onKey(DesfireKey key);
	}

	private interface OnKeyNumberListener {
		void onKeyNumber(int index, String access);
	}

	private NfcAdapter nfcAdapter;
	private List<DesfireApplication> applications;
	
	private DesfireApplication application;
	
	private DesfireApplicationKey authenticatedKey;
	
	private MifareTag tag;
	private DesfireTag desfireTag;
	private DefaultIsoDepAdapter defaultIsoDepAdapter;
	
	protected AlertDialog alertDialog;
	
	protected FileSaveFragment.Callbacks callbacks;

	protected BroadcastReceiver nfcStateChangeBroadcastReceiver;

	private TagPresenceScanner tagPresenceScanner;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		getFragmentManager().addOnBackStackChangedListener(this);
		
    	// Check for available NFC Adapter
    	PackageManager pm = getPackageManager();
    	if(pm.hasSystemFeature(PackageManager.FEATURE_NFC) && NfcAdapter.getDefaultAdapter(this) != null) {
        	Log.d(TAG, "NFC feature found");

    		nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        	if(!nfcAdapter.isEnabled()) {
        		startNfcSettingsActivity();
        		
        		showToast(R.string.nfcNotEnabledMessage);
        	}
        	
    		showMainFragment();
    		
    		tagPresenceScanner = new TagPresenceScanner(this);
    	} else {
        	Log.d(TAG, "NFC feature not found");

    		showToast(R.string.nfcNotAvailableMessage);
    		
    		finish();
    	}

	}

	@Override
	public void onResume() {
		super.onResume();
		
		nfcAdapter.enableReaderMode(this, this, NfcAdapter.FLAG_READER_NFC_A | NfcAdapter.FLAG_READER_NFC_B | NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK, null);
	}

	@Override
	public void onPause() {
		super.onPause();
		
		nfcAdapter.disableReaderMode(this);
		
		tagPresenceScanner.pause();
	}

	@Override
	public void onTagDiscovered(Tag nfc) {
		IsoDep isoDep = IsoDep.get(nfc);
		
		FragmentManager fragmentManager = getFragmentManager();
		fragmentManager.popBackStack("main", 0);
		
		DefaultIsoDepWrapper isoDepWrapper = new DefaultIsoDepWrapper(isoDep);
		
		defaultIsoDepAdapter = new DefaultIsoDepAdapter(isoDepWrapper, true); // todo change back to false
		
		try {
			isoDep.connect();
		
			setBackgroundColor(R.color.greenWhite);

			tag = mifare_desfire_tag_new();
			tag.setActive(1);
			tag.setIo(defaultIsoDepAdapter);

			desfireTag = new DesfireTag();
			
			VersionInfo versionInfo = mifare_desfire_get_version(tag);
			
			List<DesfireApplicationId> aids = mifare_desfire_get_application_ids(tag);
			if(aids != null) {
				Log.d(TAG, "Found applications " + aids.size());
				
				aids.add(0, new DesfireApplicationId()); // add default
				
				// 			DesfireApplicationKeySettings desfireApplicationKeySettings = mifare_desfire_get_key_settings(tag);
				applications = new ArrayList<DesfireApplication>();

				for(DesfireApplicationId aid : aids) {
					DesfireApplication desfireApplication = new DesfireApplication();
					desfireApplication.setId(aid.getId());
					
					applications.add(desfireApplication);
					
					Log.d(TAG, "Found application " + aid);
					
					if(mifare_desfire_select_application(tag, aid) == 0) {
						Log.d(TAG, "Selected application " + aid.toString());
						
						desfireApplication.setKeySettings(mifare_desfire_get_key_settings(tag));

						DesfireApplicationKeySettings keySettings = desfireApplication.getKeySettings();
					
						Log.d(TAG, keySettings.toString());
					}
				}
				
				desfireTag.setApplications(applications);
				
				showApplicationFragment(applications);

				tagPresenceScanner.resumeDelayed();
			} else {
				Log.d(TAG, "Did not find any applications");
			}
		} catch(Exception e) {
			Log.d(TAG, "Problem running commands", e);
		} finally {
			
		}
	}

	@Override
	public void onBackPressed() {
		FragmentManager fragmentManager = getFragmentManager();

		int count = fragmentManager.getBackStackEntryCount();
		Log.d(TAG, "onBackPressed " + count);
	    if (count == 1) {
	        this.finish();
	    } else {
	    	if(count == 2) {
    			setBackgroundColor(R.color.white);
	    	}
	    	fragmentManager.popBackStack();
	    }
	    
	    
	}
	
	private void setBackgroundColor(final int color) {
		MainActivity.this.runOnUiThread(new Runnable() {
		    public void run() {
				View view = findViewById(R.id.rootLayout);
				view.setBackgroundColor(getResources().getColor(color));
		    }
		});		
	}

	/* new with AppCompatActivity
		final ApplicationNewFragment newFragment = new ApplicationNewFragment();
		Fragment fragment = new ApplicationNewFragment();
		getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
		*/

	private void showApplicationFragment(final List<DesfireApplication> applications) {
		Log.d(TAG, "showApplicationFragment");

		final ApplicationListFragment newFragment = new ApplicationListFragment();
		newFragment.setApplications(applications);


		//FragmentManager fragmentManager = getFragmentManager();
		// Create new fragment and transaction
		//ApplicationListFragment newFragment = new ApplicationListFragment();
		//newFragment.setApplications(applications);
		newFragment.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Log.d(TAG, "onItemClick "  + position + " for " + id);
				
				int parentId = parent.getId();
				
				if(parentId == R.id.listView) {
					application = applications.get(position);
					
					Log.d(TAG, "Click on application " + application.getIdString());
					
					MainActivity.this.authenticatedKey = null;

					try {

						if(tag.getSelectedApplication() != application.getIdInt()) {
							
							if(!isConnected()) {
								Log.d(TAG, "Tag lost wanting to change application");

								onTagLost();

								return;
							}
							
							try {
								if(mifare_desfire_select_application(tag, new DesfireApplicationId(application.getId())) != 0) {
									Log.d(TAG, "Unable to select application");
								}
							} catch (Exception e) {
								Log.d(TAG, "Problem selecting app " + application.getIdString(), e);
								
								return;
							}
						}

						if(!application.hasKeys()) {
							if(!isConnected()) {
								Log.d(TAG, "Tag lost wanting to get keys");
								
								onTagLost();
								
								return;
							}
							
							Log.d(TAG, "Get application keys");
							DesfireKeyType type = application.getKeySettings().getType();
							for(int i = 0; i < application.getKeySettings().getMaxKeys(); i++) {
								
								try {
									byte version = mifare_desfire_get_key_version(tag, (byte)i);
									
									application.add(new DesfireApplicationKey(i, DesfireKey.newInstance(type, version)));
								} catch(IllegalArgumentException e) {
									// assume no key set
								}
							}
						} else {
							Log.d(TAG, "Already read key versions");
						}
						
						if(application.getIdInt() != 0) {
							
							if(!application.hasFiles()) {
								if(!isConnected()) {
									Log.d(TAG, "Tag lost wanting to read application files");
									
									onTagLost();
									
									return;
								}
								
								readApplicationFiles();
							} else {
								Log.d(TAG, "Already read file settings");
							}

						}
						
						showApplicationFragment();
						
					} catch (Exception e) {
						Log.d(TAG, "Problem selecting app " + application.getIdString(), e);
					}
				}
				
			}

			private boolean readApplicationFiles() throws Exception {
				Log.d(TAG, "Get application files");

				DesfireApplicationKeySettings keySettings = application.getKeySettings();
				
				Log.d(TAG, keySettings.toString());
				
				if(keySettings.isRequiresMasterKeyForDirectoryList()) {
					final List<DesfireApplicationKey> keys = application.getKeys();
					
					final DesfireApplicationKey root = keys.get(0);
					
					showKeySelector(keySettings.getType(), new OnKeyListener() {
						
						@Override
						public void onKey(DesfireKey key) {
							if(!isConnected()) {
								Log.d(TAG, "Tag lost wanting to select application");

								onTagLost();

								return;
							}

							try {
								DesfireApplicationKey clone = new DesfireApplicationKey(root.getIndex(), key);

								if(authenticate(clone)) {
									MainActivity.this.authenticatedKey = clone;

									readApplicationFiles();
									
									showApplicationFragment();

									showToast(R.string.applicationAuthenticatedSuccess);
								} else {
									showToast(R.string.applicationAuthenticatedFail);
								}

							} catch (Exception e) {
								Log.d(TAG, "Unable to authenticate", e);
								
								showToast(R.string.applicationAuthenticatedFail);
							}
							
						}
					});

				} else {
					Log.d(TAG, "Can list files");
				}					
				
				Log.d(TAG, "Get files ids");
				byte[] ids = mifare_desfire_get_file_ids(tag);
				
				if(ids != null) {
					Log.d(TAG, "Got " + ids.length + " files");
					
					for(int i = 0; i < ids.length; i++) {
						DesfireFile settings = mifare_desfire_get_file_settings(tag, ids[i]);
						
					    Log.d(TAG, "File setting " + i + ": " + settings);

						application.add(settings);
					}
				} else {
					Log.d(TAG, "Unable to get files ids");
				}
				
				return true;
			}
			
		});

		getSupportFragmentManager().beginTransaction().replace(R.id.content, newFragment).commit();

		/*
		FragmentTransaction transaction = fragmentManager.beginTransaction();
		// Replace whatever is in the fragment_container view with this fragment,
		// and add the transaction to the back stack
		transaction.replace(R.id.content, newFragment, "applications");
		transaction.addToBackStack("applications");

		// Commit the transaction
		transaction.commit();
		 */
	}

	protected void onTagLost() {
		showShortToast(R.string.tagStatusLost);
		
		setBackgroundColor(R.color.redWhite);
	}

	private void showMainFragment() {
		Log.d(TAG, "showMainFragment");
		
		// Create new fragment and transaction
		final MainFragment newFragment = new MainFragment();
		//getSupportFragmentManager().beginTransaction().replace(R.id.content, newFragment).commit();

		androidx.fragment.app.FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		// Replace whatever is in the fragment_container view with this fragment,
		// and add the transaction to the back stack
		//Fragment fragment = new ApplicationNewFragment();
		transaction.replace(R.id.content, newFragment, "main");
		transaction.addToBackStack("main");
		// Commit the transaction
		transaction.commit();



		/*
		final MainFragment newFragment = new MainFragment();
		FragmentTransaction transaction = getFragmentManager().beginTransaction();
		// Replace whatever is in the fragment_container view with this fragment,
		// and add the transaction to the back stack
		transaction.replace(R.id.content, newFragment, "main");
		transaction.addToBackStack("main");

		// Commit the transaction
		transaction.commit();
		 */
	}

	private void showApplicationFragment() {
		Log.d(TAG, "showApplicationFragment");
		
		// Create new fragment and transaction
		final FileListFragment newFragment = new FileListFragment();
		newFragment.setApplication(application);
		newFragment.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Log.d(TAG, "onItemClick "  + position + " for " + id);
				
				ApplicationDetail applicationDetail = newFragment.getApplicationDetail(position);
				
				if(applicationDetail instanceof ApplicationDetailFile) {
					ApplicationDetailFile file = (ApplicationDetailFile)applicationDetail;
					
					final DesfireFile desfireFile = file.getFile();
					
					Log.d(TAG, "Select file " + desfireFile);

					if(desfireFile.isContent()) {
						Log.d(TAG, "Already read file content");

						showFileFragment(desfireFile);
						
						return;
					}

					if(!isConnected()) {
						onTagLost();
						
						return;
					}
					
					if(!desfireFile.isFreeReadWriteAccess()) {
						if(authenticatedKey != null) {
							Log.d(TAG, "Already authenticated using key " + authenticatedKey.getIndex());
						
							if(desfireFile.freeReadAccess() || desfireFile.isReadAccess(authenticatedKey.getIndex())) {
								Log.d(TAG, "Already authenticated with read file access");
								
								if(!desfireFile.freeReadAccess()) {
									try {
										if(authenticate(authenticatedKey)) {
											readFile(desfireFile);
										}
									} catch (Exception e) {
										Log.d(TAG, "Unable to authenticate", e);
										
										showToast(R.string.applicationAuthenticatedFail);
									}
								} else {
									readFile(desfireFile);
								}
								
								showFileFragment(desfireFile);
								
								return;
							}
						}
							
							
						showKeyNumber(desfireFile, new OnKeyNumberListener() {
							
							@Override
							public void onKeyNumber(int index, final String access) {
								
								if(!isConnected()) {
									onTagLost();
									
									return;
								}
								
								final DesfireApplicationKey desfire = application.getKeys().get(index);
								
								showKeySelector(application.getKeySettings().getType(), new OnKeyListener() {
									
									@Override
									public void onKey(DesfireKey key) {
										try {
											DesfireApplicationKey clone = new DesfireApplicationKey(desfire.getIndex(), key);
											
											if(authenticate(clone)) {
												MainActivity.this.authenticatedKey = clone;

												if(desfireFile.freeReadAccess() || access.contains("R")) {
													readFile(desfireFile);
												}
												
												showFileFragment(desfireFile);

												showToast(R.string.applicationAuthenticatedSuccess);
											} else {
												showToast(R.string.applicationAuthenticatedFail);
											}
											
										} catch (Exception e) {
											Log.d(TAG, "Unable to authenticate", e);
											
											showToast(R.string.applicationAuthenticatedFail);
										}
										
									}
								});
								
							}
						});
							
						
					} else {
						try {
							readFile(desfireFile);

							showFileFragment(desfireFile);
						} catch (Exception e) {
							Log.d(TAG, "Problem reading file", e);
						}

					}
					
				} else if(applicationDetail instanceof ApplicationDetailApplicationKey) {
					ApplicationDetailApplicationKey key = (ApplicationDetailApplicationKey)applicationDetail;
					
					final DesfireApplicationKey desfire = key.getKey();
					
					Log.d(TAG, "Select key " + desfire);
					
					DesfireKey desfireKey = desfire.getDesfireKey();
					
					DesfireKeyType type = desfireKey.getType();
					
					showKeySelector(type, new OnKeyListener() {
						
						@Override
						public void onKey(DesfireKey key) {
							if(!isConnected()) {
								Log.d(TAG, "Tag lost wanting to change application");

								onTagLost();

								return;
							}

							try {
								DesfireApplicationKey clone = new DesfireApplicationKey(desfire.getIndex(), key);
								
								if(authenticate(clone)) {
									MainActivity.this.authenticatedKey = clone;
									
									showToast(R.string.applicationAuthenticatedSuccess);
								} else {
									showToast(R.string.applicationAuthenticatedFail);
								}
							} catch (Exception e) {
								Log.d(TAG, "Unable to authenticate", e);
								
								showToast(R.string.applicationAuthenticatedFail);
							}
							
						}
					});
				}
				
			}

		});
		
		androidx.fragment.app.FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

		// Replace whatever is in the fragment_container view with this fragment,
		// and add the transaction to the back stack
		transaction.replace(R.id.content, newFragment, "application");
		transaction.addToBackStack("application");

		// Commit the transaction
		transaction.commit();		
	}

	// added
	private void showApplicationNewFragment() {
		Log.d(TAG, "showApplicationNewFragment");

		// Create new fragment and transaction
		final ApplicationNewFragment newFragment = new ApplicationNewFragment();
		//final FileListFragment newFragment = new FileListFragment();

		//newFragment.setApplication(application);

		/* new with AppCompatActivity
		final ApplicationNewFragment newFragment = new ApplicationNewFragment();
		Fragment fragment = new ApplicationNewFragment();
		getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
		*/

		newFragment.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				System.out.println("*** createApplication pressed");

				// sanity checks on AID
				String inputAid = newFragment.getAid();
				if (TextUtils.isEmpty(inputAid)) {
					newFragment.setLogData("please enter a 6 hex character long string");
					return;
				}
				if (inputAid.length() != 6) {
					newFragment.setLogData("please enter a 6 hex character long string");
					return;
				}
				byte[] aidByte;
				aidByte = Utils.hexStringToByteArray(inputAid);
				if (aidByte == null) {
					newFragment.setLogData("please enter a 6 hex character long string");
					return;
				}
				System.out.println("aidByte: " + Utils.getHexString(aidByte));
				// com.github.skjolber.desfire.libfreefare.MifareDesfire.java

				// select master application
				//public static int mifare_desfire_select_application (MifareTag tag, DesfireApplicationId aid) throws Exception
				int selectMasterApplicationResult = -99;
				try {
					selectMasterApplicationResult = mifare_desfire_select_application(tag, null);
				} catch (Exception e) {
					// throw new RuntimeException(e);
					newFragment.setLogData("createApplication error: " + e.getMessage());
					return;
				}
				if (selectMasterApplicationResult == 0) {
					newFragment.setLogData("createApplication success");
				} else {
					newFragment.setLogData("createApplication failure: " + selectMasterApplicationResult);
					return;
				}

				// how to authenticate for create an application ?
				if (application == null) {
					System.out.println("application is null");

					//applications = new ArrayList<DesfireApplication>();

					//DesfireApplication desfireApplication = new DesfireApplication();
					//desfireApplication.setId(new byte[3]); // master application
					//applications.add(desfireApplication);
				}
				application = applications.get(0); // master application
				System.out.println("*** application: " + application.toString());
				System.out.println("*** application idString: " + application.getIdString());
				System.out.println("*** application hasKeys: " + application.hasKeys());


				DesfireApplicationKeySettings keySettings = application.getKeySettings();
				Log.d(TAG, keySettings.toString());
				if(keySettings.isRequiresMasterKeyForDirectoryList()) {
					final List<DesfireApplicationKey> keys = application.getKeys();
					final DesfireApplicationKey root = keys.get(0);
					showKeySelector(keySettings.getType(), new OnKeyListener() {
						@Override
						public void onKey(DesfireKey key) {
							if(!isConnected()) {
								Log.d(TAG, "Tag lost wanting to select application");
								onTagLost();
								return;
							}
							try {
								DesfireApplicationKey clone = new DesfireApplicationKey(root.getIndex(), key);
								if(authenticate(clone)) {
									MainActivity.this.authenticatedKey = clone;
									// todo run the code after auth here ?
									//readApplicationFiles();
									//showApplicationFragment();
									showToast(R.string.applicationAuthenticatedSuccess);
								} else {
									showToast(R.string.applicationAuthenticatedFail);
								}

							} catch (Exception e) {
								Log.d(TAG, "Unable to authenticate", e);
								showToast(R.string.applicationAuthenticatedFail);
							}
						}
					});
				} else {
					Log.d(TAG, "Can't authenticate an application");
				}

				// line 628
				// public static int
				//	create_application (MifareTag tag, DesfireApplicationId aid, byte settings1, byte settings2, int want_iso_application,
				//	int want_iso_file_identifiers, /* uint16 */ int iso_file_id, byte[] iso_file_name, int iso_file_name_len) throws Exception
				// mifare_desfire_create_application_aes (MifareTag tag, DesfireApplicationId aid, byte settings, byte key_no) throws Exception
				//byte[] aid = new byte[]{(byte) 0x08, (byte) 0x08, (byte) 0x01};
				DesfireApplicationId desfireApplicationId = new DesfireApplicationId(aidByte);
				byte aidKeySettings = (byte) 0x0f;
				byte numberOfKeys = (byte) 0x03;
				int result = -99;
				try {
					result = mifare_desfire_create_application_aes(tag, desfireApplicationId, aidKeySettings, numberOfKeys);
				} catch (Exception e) {
					//throw new RuntimeException(e);
					newFragment.setLogData("createApplication error: " + e.getMessage());
					return;
				}
				if (result == 0) {
					newFragment.setLogData("createApplication success");
				} else {
					newFragment.setLogData("createApplication failure: " + result);
					return;
				}
			}
		});

		//getSupportFragmentManager().beginTransaction().replace(R.id.content, newFragment).commit();
		//FragmentTransaction transaction = getFragmentManager().beginTransaction();
		androidx.fragment.app.FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

		// Replace whatever is in the fragment_container view with this fragment,
		// and add the transaction to the back stack
		//Fragment fragment = new ApplicationNewFragment();
		transaction.replace(R.id.content, newFragment, "addApplication");
		transaction.addToBackStack("addApplication");
		// Commit the transaction
		transaction.commit();

	}

	// added
	private void showFileNewFragment() {
		Log.d(TAG, "showFileNewFragment");

		// Create new fragment and transaction
		final FileNewFragment newFragment = new FileNewFragment(application);
		//final FileListFragment newFragment = new FileListFragment();

		//newFragment.setApplication(application);

		/* new with AppCompatActivity
		final ApplicationNewFragment newFragment = new ApplicationNewFragment();
		Fragment fragment = new ApplicationNewFragment();
		getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
		*/

		newFragment.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				System.out.println("*** createFile pressed");

				// sanity checks on AID
				String inputFid = newFragment.getFid();
				if (TextUtils.isEmpty(inputFid)) {
					newFragment.setLogData("please enter a 2 hex character long string");
					return;
				}
				if (inputFid.length() != 2) {
					newFragment.setLogData("please enter a 2 hex character long string");
					return;
				}
				byte[] fidByte;
				fidByte = Utils.hexStringToByteArray(inputFid);
				if (fidByte == null) {
					newFragment.setLogData("please enter a 2 hex character long string");
					return;
				}
				System.out.println("fidByte: " + Utils.getHexString(fidByte));
				// com.github.skjolber.desfire.libfreefare.MifareDesfire.java

				// select application
				//public static int mifare_desfire_select_application (MifareTag tag, DesfireApplicationId aid) throws Exception
				int selectApplicationResult = -99;
				byte[] selectedAid = newFragment.getApplication().getId();
				DesfireApplicationId desfireApplicationId = new DesfireApplicationId(selectedAid);
				try {
					selectApplicationResult = mifare_desfire_select_application(tag, desfireApplicationId);
				} catch (Exception e) {
					// throw new RuntimeException(e);
					newFragment.setLogData("selectApplication error: " + e.getMessage());
					return;
				}
				if (selectApplicationResult == 0) {
					newFragment.setLogData("selectApplication success");
				} else {
					newFragment.setLogData("selectApplication failure: " + selectApplicationResult);
					return;
				}

				// how to authenticate for create a file ?
				if (application == null) {
					System.out.println("application is null");

					//applications = new ArrayList<DesfireApplication>();

					//DesfireApplication desfireApplication = new DesfireApplication();
					//desfireApplication.setId(new byte[3]); // master application
					//applications.add(desfireApplication);
				}
				//application = applications.get(0); // master application
				System.out.println("*** application: " + application.toString());
				System.out.println("*** application idString: " + application.getIdString());
				System.out.println("*** application hasKeys: " + application.hasKeys());


				DesfireApplicationKeySettings keySettings = application.getKeySettings();
				Log.d(TAG, keySettings.toString());
				//if(keySettings.isRequiresMasterKeyForDirectoryList()) {
					final List<DesfireApplicationKey> keys = application.getKeys();
					final DesfireApplicationKey root = keys.get(0);
					showKeySelector(keySettings.getType(), new OnKeyListener() {
						@Override
						public void onKey(DesfireKey key) {
							if(!isConnected()) {
								Log.d(TAG, "Tag lost wanting to select application");
								onTagLost();
								return;
							}
							try {
								DesfireApplicationKey clone = new DesfireApplicationKey(root.getIndex(), key);
								if(authenticate(clone)) {
									MainActivity.this.authenticatedKey = clone;
									// todo run the code after auth here ?
									//readApplicationFiles();
									//showApplicationFragment();
									showToast(R.string.applicationAuthenticatedSuccess);
								} else {
									showToast(R.string.applicationAuthenticatedFail);
								}

							} catch (Exception e) {
								Log.d(TAG, "Unable to authenticate", e);
								showToast(R.string.applicationAuthenticatedFail);
							}
						}
					});
/*
				} else {
					Log.d(TAG, "Can't create an application");
				}
*/
				// line 628
				// public static int

				// mifare_desfire_create_std_data_file (MifareTag tag, byte file_no, byte communication_settings, int access_rights, int file_size) throws Exception

				byte fileNumber = (byte) 0x03;
				byte communicationSettings = (byte) 0x03;
				/*
				if (communicationSetting == CommunicationSetting.Plain) communicationSettings = (byte) 0x00;
        		if (communicationSetting == CommunicationSetting.MACed) communicationSettings = (byte) 0x01;
        		if (communicationSetting == CommunicationSetting.Encrypted) communicationSettings = (byte) 0x03;
				 */
				int accessRights = 18; // 0x00 0x12 = Read&Write Access & ChangeAccessRights | Read Access & Write Access
				int fileSize = 32;
				int result = -99;
				try {
					result = mifare_desfire_create_std_data_file(tag, fileNumber, communicationSettings, accessRights, fileSize);
				} catch (Exception e) {
					//throw new RuntimeException(e);
					newFragment.setLogData("createFile error: " + e.getMessage());
					return;
				}
				if (result == 0) {
					newFragment.setLogData("createFil success");
				} else {
					newFragment.setLogData("createFile failure: " + result);
					return;
				}
			}
		});

		//FragmentTransaction transaction = getFragmentManager().beginTransaction();
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		// Replace whatever is in the fragment_container view with this fragment,
		// and add the transaction to the back stack
		transaction.replace(R.id.content, newFragment, "addFile");
		transaction.addToBackStack("addFile");
		// Commit the transaction
		transaction.commit();

	}



	private String getName(DesfireKeyType type) {
		switch(type) {
			case TDES : return getString(R.string.applicationAuthenticateKey3DES);
			case TKTDES: return getString(R.string.applicationAuthenticateKey3K3DES);
			case AES : return getString(R.string.applicationAuthenticateKeyAES);
			case DES : return getString(R.string.applicationAuthenticateKeyDES);
		default:
			throw new IllegalArgumentException();
		}
	}
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    //inflater.inflate(R.menu.main, menu);
		inflater.inflate(R.menu.main_add_application, menu); // changed
	    
	    return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		for(int i = 0; i < menu.size(); i++) {
			menu.getItem(i).setVisible(false);
		}

		MenuItem keys = menu.findItem(R.id.action_settings);
		MenuItem addKey = menu.findItem(R.id.action_add);
		MenuItem save = menu.findItem(R.id.action_save);
		MenuItem addApplication = menu.findItem(R.id.action_add_app); // added
		MenuItem addFile = menu.findItem(R.id.action_add_file); // added

		//FragmentManager fragmentManager = getFragmentManager();
		//String name = fragmentManager.getBackStackEntryAt(fragmentManager.getBackStackEntryCount() - 1).getName();

		androidx.fragment.app.FragmentManager fragmentManager = getSupportFragmentManager();
		String name = fragmentManager.getBackStackEntryAt(fragmentManager.getBackStackEntryCount() - 1).getName();

		if(name != null && name.equals("keys")) {
			keys.setVisible(false);
			addKey.setVisible(true);
			addApplication.setVisible(false); // added
			addFile.setVisible(false); // added

		} else {
			keys.setVisible(true);
			addKey.setVisible(false);
			addApplication.setVisible(false); // added
			addFile.setVisible(false); // added
		}

		// added
		if(name != null && name.equals("applications")) {
			//keys.setVisible(false);
			//addKey.setVisible(true);
			addApplication.setVisible(true); // added
			addFile.setVisible(false); // added
		} else {
			//keys.setVisible(true);
			//addKey.setVisible(false);
			addApplication.setVisible(false); // added
			addFile.setVisible(false); // added
		}
		System.out.println("*** name: " + fragmentManager.getBackStackEntryAt(fragmentManager.getBackStackEntryCount() - 1).getName());
		if(name != null && name.equals("application")) {
			addFile.setVisible(true); // added
		} else {
			addFile.setVisible(false); // added
		}



		Log.d(TAG, "Prepare options menu for " + name);
		if(name != null && name.equals("file")) {
			getFragmentManager().executePendingTransactions();
			
			FileFragment fragment = (FileFragment) fragmentManager.findFragmentByTag("file");
			
			DesfireFile file = fragment.getFile();

			if(file instanceof ValueDesfireFile) {
				save.setVisible(false);
			} else {
				save.setVisible(file.isContent());
			}
		} else {
			save.setVisible(false);
		}
		
		return super.onPrepareOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.action_settings) {
			showKeysFragment();
			return true;
		} else if (item.getItemId() == R.id.action_add) {
			addKey();
			return true;
		} else if (item.getItemId() == R.id.action_save) {
			saveFileData();
			return true;
		} else if (item.getItemId() == R.id.action_add_app) { // added
			showApplicationNewFragment();
			return true;
		} else if (item.getItemId() == R.id.action_add_file) { // added
			showFileNewFragment();
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}

		/*
		switch (item.getItemId()) {
    	case R.id.action_settings: {
    		showKeysFragment();
    		return true;
    	}
    	case R.id.action_add: {
    		addKey();
    		return true;
    	}
    	case R.id.action_save : {
    		saveFileData();
    		return true;
    	}
    	default:
    		return super.onOptionsItemSelected(item);
    	}
		*/
	}
	
	private void addKey() {
		//KeyListFragment fragment = (KeyListFragment) getFragmentManager().findFragmentByTag("keys");
		KeyListFragment fragment = (KeyListFragment) getSupportFragmentManager().findFragmentByTag("keys");
		//this.dataSource = MainApplication.getInstance().getDataSource();
		fragment.setDataSource(MainApplication.getInstance().getDataSource());
		fragment.showAddKey(null);

	}

	private void saveFileData() {
		  String fragTag = "saveFileData";

		  // Get an instance supplying a default extension, captions and
		  // icon appropriate to the calling application/activity.
		  FileSaveFragment fsf = FileSaveFragment.newInstance("bin", 
		                                                      R.string.fileSaveDialogOk, 
		                                                      R.string.fileSaveDialogCancel,
		                                                      R.string.fileSaveDialogSaveAs,
		                                                      R.string.fileSaveDialogHintFilenameUnadorned,
		                                                      android.R.drawable.ic_menu_save);
		  //fsf.show(getFragmentManager(), fragTag);
		fsf.show(getSupportFragmentManager(), fragTag);
		  
		  this.callbacks = new FileCallbacks(); 
	}

	private void showKeysFragment() {
		Log.d(TAG, "showKeysFragment");
		
		// Create new fragment and transaction
		final KeyListFragment newFragment = new KeyListFragment();
		newFragment.setContext(this);
		newFragment.setDataSource(MainApplication.getInstance().getDataSource());
		
		newFragment.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Log.d(TAG, "onItemClick "  + position + " for " + id);
				
				ApplicationDetail applicationDetail = newFragment.getApplicationDetail(position);
				
				if(applicationDetail instanceof ApplicationDetailKey) {
					ApplicationDetailKey key = (ApplicationDetailKey)applicationDetail;
					
					DesfireKey desfire = key.getKey();
					
					Log.d(TAG, "Show details for key " + desfire);
					
					//KeyListFragment fragment = (KeyListFragment) getFragmentManager().findFragmentByTag("keys");
					KeyListFragment fragment = (KeyListFragment) getSupportFragmentManager().findFragmentByTag("keys");
					fragment.setDataSource(MainApplication.getInstance().getDataSource());
					fragment.showAddKey(desfire);

				}
				
			}
			
		});
		//FragmentTransaction transaction = getFragmentManager().beginTransaction();
		androidx.fragment.app.FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

		// Replace whatever is in the fragment_container view with this fragment,
		// and add the transaction to the back stack
		transaction.replace(R.id.content, newFragment, "keys");
		transaction.addToBackStack("keys");

		// Commit the transaction
		transaction.commit();		
	}



	private boolean authenticate(DesfireApplicationKey desfireApplicationKey) throws Exception {
		
		DesfireKey key = desfireApplicationKey.getDesfireKey();

		Log.d(TAG, "Authenticate key " + (byte)desfireApplicationKey.getIndex());
		
	    /* Authenticate with this key */
	    switch (key.getType()) {
	    case AES : {

	    	DesfireAESKey aesKey = (DesfireAESKey)key;
	    	
	    	MifareDESFireKey mifareDESFireKey = MifareDesfireKey1.mifare_desfire_aes_key_new_with_version(aesKey.getValue(), (byte)key.getVersion());
	    	
			int result = mifare_desfire_authenticate_aes (tag, (byte)desfireApplicationKey.getIndex(), mifareDESFireKey);
			
			if(result == 0) {
				Log.d(TAG, "Authenticated AES using key " + key.getName() + " index " + (byte)desfireApplicationKey.getIndex());
				
				return true;
			} else {
				Log.d(TAG, "Unable to authenticate AES using key " + key.getName());
			}
			
	    	break;
	    }
	    case TKTDES : {

	    	Desfire3K3DESKey desfire3k3desKey = (Desfire3K3DESKey)key;

	    	MifareDESFireKey mifareDESFireKey = MifareDesfireKey1.mifare_desfire_3k3des_key_new(desfire3k3desKey.getValue());

			int result = mifare_desfire_authenticate_iso (tag, (byte)desfireApplicationKey.getIndex(), mifareDESFireKey);

			if(result == 0) {
				Log.d(TAG, "Authenticated 3K3DES using key " + key.getName());
				
				return true;
			} else {
				Log.d(TAG, "Unable to authenticate 3K3DES using key " + key.getName());
			}

	    	break;
	    }
	    case TDES : {

	    	Desfire3DESKey desfire3desKey = (Desfire3DESKey)key;

	    	MifareDESFireKey mifareDESFireKey = MifareDesfireKey1.mifare_desfire_3des_key_new(desfire3desKey.getValue());

	    	MifareDesfireKey1.mifare_desfire_key_set_version(mifareDESFireKey, (byte)desfire3desKey.getVersion());
	    	
			int result = mifare_desfire_authenticate (tag, (byte)desfireApplicationKey.getIndex(), mifareDESFireKey);

			if(result == 0) {
				Log.d(TAG, "Authenticated 3DES using key " + key.getName());
				
				return true;
			} else {
				Log.d(TAG, "Unable to authenticate 3DES using key " + key.getName());
			}

	    	break;
	    }
	    case DES : {

	    	DesfireDESKey desfireDesKey = (DesfireDESKey)key;

	    	MifareDESFireKey mifareDESFireKey = MifareDesfireKey1.mifare_desfire_des_key_new(desfireDesKey.getValue());

	    	MifareDesfireKey1.mifare_desfire_key_set_version(mifareDESFireKey, (byte)desfireDesKey.getVersion());
	    	
			int result = mifare_desfire_authenticate (tag, (byte)desfireApplicationKey.getIndex(), mifareDESFireKey);

			if(result == 0) {
				Log.d(TAG, "Authenticated DES using key " + key.getName());
				
				return true;
			} else {
				Log.d(TAG, "Unable to authenticate DES using key " + key.getName());
			}

	    	break;
	    }
	}
	    return false;
	}

	private void showKeyNumber(DesfireFile desfireFile, final OnKeyNumberListener listener) {
		
		final Map<Integer, String> compactPermissionMap = desfireFile.getCompactPermissionMap();
		
		final List<Integer> keyNumbers = new ArrayList<>(compactPermissionMap.keySet());
		Collections.sort(keyNumbers);
		
		List<String> keys = new ArrayList<>();
		
		for(int i = 0; i < keyNumbers.size(); i++) {
			Integer keyNumber = keyNumbers.get(i);
			if(keyNumber == 14) {
				continue;
			}
			String access = compactPermissionMap.get(keyNumber);
			StringBuffer buffer = new StringBuffer();
			if(access.contains("R")) {
				buffer.append(getString(R.string.fileAccessKeyRead));
			}
			if(access.contains("W")) {
				if(buffer.length() > 0) {
					buffer.append(", ");
				}
				buffer.append(getString(R.string.fileAccessKeyWrite));
			}
			if(access.contains("C")) {
				if(buffer.length() > 0) {
					buffer.append(", ");
				}
				buffer.append(getString(R.string.fileAccessKeyChange));
			}
				
			keys.add(getString(R.string.fileAccessKey, keyNumber, buffer.toString()));
		}
		
		String names[] = keys.toArray(new String[keys.size()]);
		
	    AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
	    LayoutInflater inflater = getLayoutInflater();
	    View convertView = (View) inflater.inflate(R.layout.dialog_list, null);
	    alertDialog.setView(convertView);

	    alertDialog.setTitle(getString(R.string.fileAccessSelectKey));
	    ListView lv = (ListView) convertView.findViewById(R.id.listView);
	    ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, names);
	    lv.setAdapter(adapter);
	    final AlertDialog show = alertDialog.show();
	    
	    lv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				show.dismiss();

				Integer keyNumber = keyNumbers.get(position);
				
				listener.onKeyNumber(keyNumber, compactPermissionMap.get(keyNumber));
			}
		});
	}
	
	private void showKeySelector(DesfireKeyType type, final OnKeyListener listener) {
		MainApplication application = MainApplication.getInstance();
		
		DataSource dataSource = application.getDataSource();
		
		final List<DesfireKey> keys;
		if(type == DesfireKeyType.TDES || type == DesfireKeyType.DES) {
			keys = new ArrayList<>();
			
			keys.addAll(dataSource.getKeys(DesfireKeyType.DES));
			keys.addAll(dataSource.getKeys(DesfireKeyType.TKTDES));
		} else {
			keys = dataSource.getKeys(type);
		}
		
		if(!keys.isEmpty()) {
			String names[] = new String[keys.size()];
			for(int i = 0; i < names.length; i++) {
				names[i] = getString(R.string.applicationAuthenticateKeyNameVersion, keys.get(i).getName(), keys.get(i).getVersionAsHexString());
			}
		    AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
		    LayoutInflater inflater = getLayoutInflater();
		    View convertView = (View) inflater.inflate(R.layout.dialog_list, null);
		    alertDialog.setView(convertView);

		    alertDialog.setTitle(getString(R.string.applicationAuthenticateKey, getName(type)));
		    ListView lv = (ListView) convertView.findViewById(R.id.listView);
		    ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, names);
		    lv.setAdapter(adapter);
		    final AlertDialog show = alertDialog.show();
		    
		    lv.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					show.dismiss();

					DesfireKey key = keys.get(position);

					listener.onKey(key);
				}

			});
		} else {
			Log.d(TAG, "No " + type + " keys found");
		}
	}

	public void showToast(int resource, Object ... args) {
		Toast.makeText(getApplicationContext(), getString(resource, args), Toast.LENGTH_LONG).show();
	}

	public void showToast(int resource) {
		Toast.makeText(getApplicationContext(), getString(resource), Toast.LENGTH_LONG).show();
	}

	public void showShortToast(int resource) {
		Toast.makeText(getApplicationContext(), getString(resource), Toast.LENGTH_SHORT).show();
	}

	private void showFileFragment(DesfireFile file) {
		Log.d(TAG, "showFileFragment");
		
		// Create new fragment and transaction
		final FileFragment newFragment = new FileFragment();
		newFragment.setFile(file);

		newFragment.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Log.d(TAG, "onItemClick "  + position + " for " + id);
				
				ApplicationDetail applicationDetail = newFragment.getApplicationDetail(position);
				
				if(applicationDetail instanceof ApplicationDetailRecord) {
					ApplicationDetailRecord key = (ApplicationDetailRecord)applicationDetail;
					
					byte[] content = key.getContent();
					
					Log.d(TAG, "Save " + Utils.getHexString(content));
				}
				
			}
			
		});

/*
		FragmentTransaction transaction = getFragmentManager().beginTransaction();
		// Replace whatever is in the fragment_container view with this fragment,
		// and add the transaction to the back stack
		transaction.replace(R.id.content, newFragment, "file");
		transaction.addToBackStack("file");
		// Commit the transaction
		transaction.commit();
*/
		androidx.fragment.app.FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

		// Replace whatever is in the fragment_container view with this fragment,
		// and add the transaction to the back stack
		//Fragment fragment = new ApplicationNewFragment();
		transaction.replace(R.id.content, newFragment, "file");
		transaction.addToBackStack("file");
		// Commit the transaction
		transaction.commit();

	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public void onBackStackChanged() {
		invalidateOptionsMenu();
	}
	
	private class FileCallbacks implements FileSaveFragment.Callbacks {

		@Override
		public boolean onCanSave(String absolutePath, String fileName) {
			return absolutePath != null && absolutePath.length() > 0 && fileName != null && fileName.length() > 0;
		}
	
		@Override
		public void onConfirmSave(String absolutePath, String fileName) {
			if(absolutePath == null || absolutePath.length() == 0 || fileName == null || fileName.length() == 0) {
		    	getFragmentManager().popBackStack();
	
				
				return;
			}

			//FileFragment fragment = (FileFragment) getFragmentManager().findFragmentByTag("file");
			FileFragment fragment = (FileFragment) getSupportFragmentManager().findFragmentByTag("file");
			
			DesfireFile file = fragment.getFile();
			
			if(file instanceof ValueDesfireFile) {
				throw new IllegalArgumentException();
			}

			byte[] data;
			if(file instanceof StandardDesfireFile) {
				StandardDesfireFile standardDesfireFile = (StandardDesfireFile)file;
				data = standardDesfireFile.getData();
			} else if(file instanceof RecordDesfireFile) {
				RecordDesfireFile recordDesfireFile = (RecordDesfireFile)file;
				data = recordDesfireFile.getRecords();
			} else {
				throw new IllegalArgumentException();
			}
			FileOutputStream out = null;
			try {
				File outputFile = new File(absolutePath, fileName);
				
				if(outputFile.exists()) {
					if(!outputFile.delete()) {
						Log.d(TAG, "Unable to delete file " + outputFile);
						
						return;
					}
				}
				
				out = new FileOutputStream(outputFile);
				out.write(data);

				out.flush();
				
	        	Log.d(TAG, "Saved file " + file);
	        	
	    		Toast.makeText(getApplicationContext(), getString(R.string.fileSavedSuccess, outputFile.toString()), Toast.LENGTH_LONG).show();
	        } catch (IOException e) {
	        	Log.d(TAG, "Problem saving file " + absolutePath + " " + fileName);
	        	
	    		Toast.makeText(getApplicationContext(), getString(R.string.fileSavedFailure), Toast.LENGTH_LONG).show();
	        	
	        } finally {
	        	if(out != null) {
	        		try {
	        			out.close();
	        		} catch(IOException e) {
	        			// ignore
	        		}
	        	}
	        }
	    	getFragmentManager().popBackStack();

		}
	}

	
	public void show(AlertDialog altertDialog) {
		synchronized(this) {
			if(alertDialog != null) {
				alertDialog.cancel();
			}
			// create alert dialog
			this.alertDialog = altertDialog;
			
			runOnUiThread(new Runnable() {
				public void run() {
					// show it
					alertDialog.show();
			}});
			
		}
	}
	
	public void hideDialog() {
		synchronized(this) {
			if(alertDialog != null) {
				alertDialog.cancel();
				alertDialog = null;
			}
		}
	}

	private void readFile(final DesfireFile desfireFile) {
		
		Log.d(TAG, "Read file access");
		if(desfireFile instanceof StandardDesfireFile) {
			try {
				StandardDesfireFile standardDesfireFile = (StandardDesfireFile)desfireFile;

				if(!standardDesfireFile.isData()) {
					Log.d(TAG, "Read data from file " + Integer.toHexString(desfireFile.getId()));
					
					byte[] data = mifare_desfire_read_data (tag, (byte)desfireFile.getId(), 0, 0);
				
					Log.d(TAG, "Read data length " + data.length);
				
					standardDesfireFile.setData(data);
				}
			} catch (Exception e) {
				Log.d(TAG, "Problem reading file", e);
			}
		} else if(desfireFile instanceof ValueDesfireFile) {
			try {
				ValueDesfireFile valueDesfireFile = (ValueDesfireFile)desfireFile;

				if(!valueDesfireFile.isValue()) {
					Log.d(TAG, "Read value from file " + Integer.toHexString(desfireFile.getId()));
					
					Integer value = mifare_desfire_get_value(tag, (byte)desfireFile.getId());

					Log.d(TAG, "Read value " + value);
				
					valueDesfireFile.setValue(value);
				}
			} catch (Exception e) {
				Log.d(TAG, "Problem reading file", e);
			}
		} else if(desfireFile instanceof RecordDesfireFile) {
			try {
				RecordDesfireFile recordDesfireFile = (RecordDesfireFile)desfireFile;

				if(!recordDesfireFile.isRecords()) {
					Log.d(TAG, "Read records from file " + Integer.toHexString(desfireFile.getId()));
					
					byte[] records = mifare_desfire_read_records (tag, (byte)desfireFile.getId(), 0, recordDesfireFile.getCurrentRecords());

					Log.d(TAG, "Read " + recordDesfireFile.getCurrentRecords() + " records " + Utils.getHexString(records));
					
					recordDesfireFile.setRecords(records);
				}
			} catch (Exception e) {
				Log.d(TAG, "Problem reading record file", e);
			}
		}
	}

	@Override
	public boolean onCanSave(String absolutePath, String fileName) {
		return callbacks.onCanSave(absolutePath, fileName);
	}

	@Override
	public void onConfirmSave(String absolutePath, String fileName) {
		callbacks.onConfirmSave(absolutePath, fileName);
		
		this.callbacks = null;
	}

	private boolean isConnected() {
		MifareTag tag = this.tag;
		
		if(tag != null) {
			DefaultIsoDepWrapper wrapper = (DefaultIsoDepWrapper) tag.getIo().getIsoDepWrapper();
			
			return wrapper.getIsoDep().isConnected();
		}
		return false;
	}

	/**
	 * 
	 * Launch an activity for NFC (or wireless) settings, so that the user might enable or disable nfc
	 * 
	 */

	
	protected void startNfcSettingsActivity() {
		if (android.os.Build.VERSION.SDK_INT >= 16) {
			startActivity(new Intent(ACTION_NFC_SETTINGS)); // android.provider.Settings.ACTION_NFC_SETTINGS
		} else {
			startActivity(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));
		}
	}
	
    protected static class TagPresenceScanner extends Handler {
    	
        private static final long TAG_RESCAN_INTERVAL_MS = 1000;

		private WeakReference<MainActivity> activityReference;
		
		public TagPresenceScanner(MainActivity activity) {
			this.activityReference = new WeakReference<MainActivity>(activity);
		}

        void resume() {
        	synchronized (this) {
	            if (!hasMessages(0)) {
	                sendEmptyMessage(0);
	            }
        	}
        }
        
        public void resumeDelayed() {
        	synchronized (this) {
	            if (!hasMessages(0)) {
	            	 sendEmptyMessageDelayed(0, TAG_RESCAN_INTERVAL_MS);
	            }
        	}
        }

        public void pause() {
        	synchronized (this) {
        		removeMessages(0);
        	}
        }

        @Override
        public void handleMessage(android.os.Message msg) {
        	//Log.v(TAG, "Handle message");
        	
        	MainActivity activity = activityReference.get();
			if(activity != null) {
				if(activity.isConnected()) {
					resumeDelayed();
				} else {
					activity.onTagLost();
					
					pause();
				}
			}
        }
    }
}
