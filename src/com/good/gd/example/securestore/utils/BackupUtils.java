/*
 *  This file contains Good Sample Code subject to the Good Dynamics SDK Terms and Conditions.
 *  (c) 2013 Good Technology Corporation. All rights reserved.
 */

package com.good.gd.example.securestore.utils;

import com.good.gd.file.FileOutputStream;

import java.io.IOException;
import java.io.FileNotFoundException;

import android.content.Context;
import android.util.Log;

/**
 * BackupUtils contains a set of useful method for generating sample data in the secure container which can be used
 * by the file backup helper to backup app data.
 */
public class BackupUtils {
	
	private static final String TAG = "BackupUtils";
	
	private static final String RootFile = "root.txt";

	private static final int s_aFolderSize = 5;
	private static final int s_bFolderSize = 3;
	private static final int s_cFolderSize = 4;
	
	private static final String s_aFolder = "A";
	private static final String s_bFolder = "B";
	private static final String s_cFolder = "C";
	
	private static String[] s_content = null;
	private static int s_contentIndex = 0;

	/**
	 * Checks whether we currently have sample data on the secure container. We try to access the Roo File, if is there
	 * we can safely assume we have already created some sample data for the app.
	 */
	public static boolean doesExists() {
		String absolutePath = com.good.gd.file.GDFileSystem.getAbsoluteEncryptedPath(RootFile);
		java.io.File file = new java.io.File(absolutePath);

		return file.exists();
	}

	/**
	 * Creates sample data for the secure container. It will create folders and files which will be stored
	 * on the secure container.
	 */
	public static void create(Context ctx) {
		try {
			FileOutputStream out = com.good.gd.file.GDFileSystem.openFileOutput(RootFile, com.good.gd.file.GDFileSystem.MODE_PRIVATE);
			out.write(RootFile.getBytes());
			out.flush();
			out.close();

			createFolder(ctx, s_aFolder, s_aFolderSize);
			createFolder(ctx, s_bFolder, s_bFolderSize);
			createFolder(ctx, s_cFolder, s_cFolderSize);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void createFolder(Context ctx, String letter, int quantity) throws IOException {

		String absolutePath = com.good.gd.file.GDFileSystem.getAbsoluteEncryptedPath(RootFile);
		String[] split = absolutePath.split("/");
		int index = absolutePath.indexOf(split[split.length - 1]);
		String path = absolutePath.substring(0, index);

		com.good.gd.file.File file = new com.good.gd.file.File(folderName(letter));
		boolean success = file.mkdir();

		for (int i = 0; i < quantity; i++) {
			String filename = letter.toLowerCase() + "_" + i + ".txt";
			
			FileOutputStream out = com.good.gd.file.GDFileSystem.openFileOutput(folderName(letter) + "/" + filename, com.good.gd.file.GDFileSystem.MODE_PRIVATE);
			out.write(filename.getBytes());
			out.flush();
			out.close();
		}
	}
	
	/**
	 * Lists the content which we want to backup/restore. This might be different from the content created and already existing on
	 * the secure container. This is mainly used by the file backup helper to retrieve the files we want to backup/restore.
	 */
	public static String[] list() {
		createList();		
		return s_content;
	}

	private static void createList() {
		s_content = new String[s_aFolderSize + s_cFolderSize + 1];
		s_contentIndex = 0;
		
		createListForFolder(s_aFolder, s_aFolderSize);
		createListForFolder(s_cFolder, s_cFolderSize);
		
		s_content[s_contentIndex] = RootFile;
		s_contentIndex++;
	}
	
	private static void createListForFolder(String letter, int quantity) {
		for (int i = 0; i < quantity; i++) {
			String filename = letter.toLowerCase() + "_" + i + ".txt";			
			s_content[s_contentIndex] = folderName(letter) + "/" + filename;
			s_contentIndex++;
		}
	}

	/**
	 * Deletes a single file from the secure container.
	 */
	public static boolean delete(String path) {
		com.good.gd.file.File file = new com.good.gd.file.File(path);
		return file.delete();
	}

	/**
	 * Wipes the sample data generated for the secure container.
	 */
	public static void wipe(Context ctx) {
		String[] files = BackupUtils.list();

		for (int i = 0; i < files.length; i++) {
			BackupUtils.delete(files[i]);
		}

		BackupUtils.delete(folderName(s_aFolder));
		BackupUtils.delete(folderName(s_bFolder));
		BackupUtils.delete(folderName(s_cFolder));
	}

	/**
	 * Traverses the content of the secure container. This will list the entire content of the secure container
	 * as opposed to the list method previously defined.
	 */
	public static void traverse(Context ctx) {
		String absolutePath = com.good.gd.file.GDFileSystem.getAbsoluteEncryptedPath(RootFile);
		String[] split = absolutePath.split("/");
		int index = absolutePath.indexOf(split[split.length - 1]);

		String path = absolutePath.substring(0, index);
		java.io.File file = new java.io.File(path);

		traverse(ctx, file);
	}
	
	private static void traverse(Context ctx, java.io.File root) {
		if (root.exists()) {
			Log.i(TAG, "[BackupUtils::traverse]" + root.getAbsolutePath());
			java.io.File[] files = root.listFiles();
			
			for (int i = 0; i < files.length; i++) {
				if (files[i].isDirectory()) {
					traverse(ctx, files[i]);
				}
				else {
					Log.i(TAG, "[BackupUtils::traverse]" + files[i].getAbsolutePath());
				}
			}
		}
	}
	
	/**
	 * Returns the name of the folder for a given letter.
	 */
	private static String folderName(String letter) {
		return letter + "_Folder";
	}
}
