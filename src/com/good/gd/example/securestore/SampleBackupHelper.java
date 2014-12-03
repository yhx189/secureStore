/*
 *  This file contains Good Sample Code subject to the Good Dynamics SDK Terms and Conditions.
 *  (c) 2013 Good Technology Corporation. All rights reserved.
 */

package com.good.gd.example.securestore;

import java.io.IOException;

import android.app.backup.BackupAgentHelper;
import android.app.backup.BackupDataInput;
import android.app.backup.BackupDataOutput;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import com.good.gd.example.securestore.utils.BackupUtils;
import com.good.gd.backup.GDFileBackupHelper;

/**
 * Represents a BackupAgentHelper subclass used to exemplify how the GDFileBackupHelper is used to backup
 * files from the application secure container.
 */
public class SampleBackupHelper extends BackupAgentHelper {

	static final String FILES_BACKUP_KEY = "BACKUP_KEY";
	private static final String TAG = "SampleBackupHelper";
	
	public void onCreate() {
		Log.i(TAG, "[SampleBackupHelper::onCreate]");
		
		// We create a GDFileBackupHelper instance which should be used instead of the default
		// FileBackupHelper provided by android. We should indicate which files do we want to backup/restore.
		// An important thing to mention is that we are using the BackupUtils to return a list of files
		// which should be backed up. This list can be obtained from anywhere in real-life apps.
		GDFileBackupHelper helper = new GDFileBackupHelper(this, BackupUtils.list());
		addHelper(FILES_BACKUP_KEY, helper);
	}
	
	@Override
	public void onBackup(ParcelFileDescriptor oldState, BackupDataOutput data,
			 ParcelFileDescriptor newState) throws IOException {
		Log.i(TAG, "[SampleBackupHelper::onBackup]");
		super.onBackup(oldState, data, newState);
	}

	@Override
	public void onRestore(BackupDataInput data, int appVersionCode,
			ParcelFileDescriptor newState) throws IOException {
		Log.i(TAG, "[SampleBackupHelper::onRestore]");
		super.onRestore(data, appVersionCode, newState);
	}
}