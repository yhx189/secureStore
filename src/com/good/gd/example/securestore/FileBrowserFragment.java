package com.good.gd.example.securestore;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.backup.BackupManager;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import com.good.gd.example.securestore.iconifiedlist.IconifiedText;
import com.good.gd.example.securestore.iconifiedlist.IconifiedTextListAdapter;
import com.good.gd.example.securestore.utils.BackupUtils;
import com.good.gd.example.securestore.utils.FileUtils;
import com.good.gd.example.securestore.utils.ListUtils;

/**
 * AndroidFileBrowser - a basic file browser list which supports multiple modes
 * (Container and insecure SDCard). Files can be deleted, moved to the container
 * and if they're .txt files they can be opened and viewed.
 */
public class FileBrowserFragment extends ListFragment {

	public final static String STATE_CURRENT_PATH = "curr_path";

	private List<IconifiedText> directoryEntries = new ArrayList<IconifiedText>();
	private String mCurrentPath = null;

	private static final int MENU_ITEM_COPY_TO_CONTAINER = 1;
	private static final int MENU_ITEM_OPEN_ITEM = 2;
	private static final int MENU_ITEM_DELETE_ITEM = 3;

	private BackupManager mBackupManager = null;
	private boolean m_authorized = false;

	private View mView;

	public FileBrowserFragment() {

	}

	/**
	 * onCreate - sets up the core activity members
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState != null) {
			mCurrentPath = savedInstanceState.getString(STATE_CURRENT_PATH);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.main, container, false);
		mView = v;

		return v;

	}

	@Override
	public void onStart() {
		super.onStart();

		getListView().setLongClickable(true);
		registerForContextMenu(getListView());

		/*
		 * Set the path the very first time the fragment is created. As fragment
		 * addition to the activity is asynchronous the FileBrowser activity
		 * will not call back directly in case the callback triggers any action
		 * that requires the fragment to be in the started state (typically view
		 * related operations). Therefore we can action the callback here
		 * instead if this is the first start after creation and we can tell
		 * this because the mCurrentPath member is null.
		 */
		if (mCurrentPath == null) {
			mCurrentPath = FileUtils.getInstance().getCurrentRoot();
			// now we've set the path update authorized state as we know it must
			// now be true
			onAuthorizeStateChange(true);
		}
	}

	/**
	 * onSaveInstanceState - saves the path for restore during transitions
	 */
	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putString(STATE_CURRENT_PATH, mCurrentPath);
	}

	/**
	 * onCreateContextMenu - populates the context menu which is shown after a
	 * long press on a row
	 */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.add(Menu.NONE, MENU_ITEM_OPEN_ITEM, 0, R.string.MENU_STRING_OPEN);
		menu.add(Menu.NONE, MENU_ITEM_DELETE_ITEM, 1,
				R.string.MENU_STRING_DELETE);
		if (FileUtils.getInstance().getMode() == FileUtils.MODE_SDCARD) {
			menu.add(Menu.NONE, MENU_ITEM_COPY_TO_CONTAINER, 2,
					R.string.MENU_STRING_COPY_TO_CONTAINER);
		}
	}

	/**
	 * onContextItemSelected - something on the context menu was selected
	 */
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
				.getMenuInfo();
		if (info == null) {
			return false;
		}

		String fullFilePath = !mCurrentPath.equals("/") ? mCurrentPath + "/"
				+ directoryEntries.get(info.position).getText() : mCurrentPath
				+ directoryEntries.get(info.position).getText();
		switch (item.getItemId()) {
		case MENU_ITEM_COPY_TO_CONTAINER:
			FileUtils.getInstance().copyToContainer(fullFilePath);
			break;

		case MENU_ITEM_OPEN_ITEM:
			browseToPath(fullFilePath);
			break;

		case MENU_ITEM_DELETE_ITEM:
			FileUtils.getInstance().deleteItem(fullFilePath);
			browseToPath(mCurrentPath);
			break;
		}
		return true;
	}

	/**
	 * onListItemClick - something on the list was clicked (not a long click)
	 */
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		String browseTo = !mCurrentPath.equals("/") ? mCurrentPath + "/"
				+ directoryEntries.get(position).getText() : mCurrentPath
				+ directoryEntries.get(position).getText();
		browseToPath(browseTo);
	}

	void handleBackup() {
		// We do a quickly check to verify whether the sample data is already
		// created, if not, we will create it
		// and mark the application as ready to be backed up.
		// Note: this can safely be omitted if the app is not intended to create
		// backups.
		if (!BackupUtils.doesExists()) {
			// create the sample data.
			BackupUtils.create(getActivity());
			// Notify the backup manager that the application has some data that
			// must be backed up.
			// This should be called anytime the application changes data and is
			// considered important enough
			// to mark the application as backup-ready.
			mBackupManager.dataChanged();
		}
	}

	void onAuthorizeStateChange(boolean authorized) {
		m_authorized = authorized;

		if (authorized) {
			// create backup manager if needed
			if (mBackupManager == null) {
				mBackupManager = new BackupManager(getActivity());
			}
			// handle any backup
			handleBackup();

			// browse to path
			browseToPath(mCurrentPath);
		}
	}

	/**
	 * onClick - one of the buttons was pressed
	 */
	public void onClick(int id) {
		switch (id) {
		case R.id.action_back:
			Log.d("onClick", "action_back");
			upOneLevel();
			break;

		case R.id.action_create_folder:
			Log.d("onClick", "action_create_folder");
			showNewDirUI();
			break;

		case R.id.action_btn_container:
			Log.d("onClick", "action_btn_container");
			FileUtils.getInstance().setMode(FileUtils.MODE_CONTAINER);
			mCurrentPath = FileUtils.CONTAINER_ROOT;
			browseToPath(mCurrentPath);
			break;

		case R.id.action_btn_sdcard:
			Log.d("onClick", "action_btn_sdcard");
			FileUtils.getInstance().setMode(FileUtils.MODE_SDCARD);
			mCurrentPath = FileUtils.SDCARD_ROOT;
			browseToPath(mCurrentPath);
			break;
		}
	}

	/**
	 * upOneLevel - switch the current working directory one layer up
	 */
	private void upOneLevel() {
		File parentFile = FileUtils.getInstance().getParentFile(mCurrentPath);
		if (parentFile != null
				&& FileUtils.getInstance().canGoUpOne(mCurrentPath)) {
			browseToPath(parentFile.getPath());
		}
	}

	/**
	 * browseToPath - load the file browser at the specified path
	 */
	private void browseToPath(String path) {
		if (m_authorized && (path != null)) {
			File file = FileUtils.getInstance().getFileFromPath(path);
			if (file.isDirectory()) {
				mCurrentPath = file.getPath();
				populateList(file.listFiles());
				updateButtonsAndTitle();
			} else {
				FileUtils.getInstance().openItem(getActivity(), path);
			}
		}
	}

	/**
	 * populateList - takes a set of files and writes into the list adapter
	 */
	private void populateList(File[] files) {
		this.directoryEntries.clear();
		List<String> folderLst = new ArrayList<String>();
		List<String> fileLst = new ArrayList<String>();

		if (files != null) {
			for (File currentFile : files) {
				if (currentFile.isDirectory()) {
					ListUtils.insertAsc(folderLst, currentFile.getName());
				} else {
					ListUtils.insertAsc(fileLst, currentFile.getName());
				}
			}

			Drawable folderIcon = getResources().getDrawable(
					R.drawable.fb_folder);
			Drawable fileIcon = (FileUtils.getInstance().getMode() == FileUtils.MODE_SDCARD) ? getResources()
					.getDrawable(R.drawable.fb_file) : getResources()
					.getDrawable(R.drawable.fb_file_secure);
			for (String str : folderLst) {
				this.directoryEntries.add(new IconifiedText(str, folderIcon));
			}
			for (String str : fileLst) {
				this.directoryEntries.add(new IconifiedText(str, fileIcon));
			}
		}

		IconifiedTextListAdapter itla = new IconifiedTextListAdapter(
				getActivity());
		itla.setListItems(this.directoryEntries);
		setListAdapter(itla);
		setSelection(0);
	}

	/**
	 * updateButtonsAndTitle - redraw the toggle buttons, update the title and
	 * toggle the padlock
	 */
	private void updateButtonsAndTitle() {

		if (FileUtils.getInstance().getMode() == FileUtils.MODE_CONTAINER) {
			((ImageView) mView.findViewById(R.id.fb_padlock))
					.setVisibility(View.VISIBLE);
		} else {
			((ImageView) mView.findViewById(R.id.fb_padlock))
					.setVisibility(View.INVISIBLE);
		}

		// setTitle(mCurrentPath);
	}

	/**
	 * showNewDirUI - show some UI which gets a new directory name
	 */
	public void showNewDirUI() {
		AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
		alert.setTitle(R.string.DIR_NAME);
		final EditText input = new EditText(getActivity());
		alert.setView(input);
		alert.setPositiveButton(R.string.OK,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						String dirName = input.getText().toString();
						if (dirName.length() > 0) {
							FileUtils.getInstance().makeNewDir(mCurrentPath,
									dirName);
							browseToPath(mCurrentPath);
						}
					}
				});
		alert.setNegativeButton("Cancel", null);
		alert.show();
	}
}
