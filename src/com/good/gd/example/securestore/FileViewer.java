/*
 *  This file contains Good Sample Code subject to the Good Dynamics SDK Terms and Conditions.
 *  (c) 2013 Good Technology Corporation. All rights reserved.
 */

package com.good.gd.example.securestore;

import java.io.UnsupportedEncodingException;

import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;

import android.support.v4.app.FragmentActivity;

import com.good.gd.GDAndroid;
import com.good.gd.example.securestore.utils.FileUtils;

/** FileViewer - a basic viewer which will load content into an HTML webview.
 */
public class FileViewer extends FragmentActivity {

    public static final String FILE_VIEWER_PATH = "path";

    private String mPath;

    /** onCreate - takes a path from the caller to get the file from
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        GDAndroid.getInstance().activityInit(this);

        setContentView(R.layout.viewer);

        Intent i = getIntent();
        if (i != null) {
            mPath = i.getStringExtra(FILE_VIEWER_PATH);
        }
    }

    /** onResume - sets up and loads data into the webview
     */
    public void onResume() {
        super.onResume();
        byte b[] = FileUtils.getInstance().getFileData(mPath);
        try {
        	if (b != null && b.length > 0) {
        		WebView wv = (WebView) findViewById(R.id.webview);
        		wv.loadData(new String(b, "UTF-8"), "text/html", "UTF-8");
        	}
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}
