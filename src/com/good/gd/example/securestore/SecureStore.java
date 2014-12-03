/*
 *  This file contains Good Sample Code subject to the Good Dynamics SDK Terms and Conditions.
 *  (c) 2013 Good Technology Corporation. All rights reserved.
 */

package com.good.gd.example.securestore;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ToggleButton;
import android.os.Handler;

import com.good.gd.GDAndroid;
import com.good.gd.GDAppEvent;
import com.good.gd.GDAppEventListener;
import com.good.gd.GDAppEventType;

/**
 * SecureStore activity - a basic file browser list which supports multiple modes
 * (Container and insecure SDCard). Files can be deleted, moved to the container
 * and if they're .txt files they can be opened and viewed.
 */
public class SecureStore extends FragmentActivity implements OnClickListener {

	public GDEventListen mListener = null;
    protected FileBrowserFragment m_fragment = null;
    protected Activity mActivity;
    private Runnable runnable;
    private Handler handler;

	/**
	 * onCreate - sets up the core activity members
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mListener = new GDEventListen();
		mListener.setActivity(this);
		GDAndroid.getInstance().setGDAppEventListener(mListener);
		GDAndroid.getInstance().activityInit(this);

		setContentView(R.layout.mainfragment);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mListener.setActivity(null);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (m_fragment != null)
			m_fragment.onClick(item.getItemId());
		return super.onOptionsItemSelected(item);
	}

	class GDEventListen implements GDAppEventListener {


		public void setActivity(Activity a) {
			mActivity = a;
		}

		@Override
		public void onGDEvent(GDAppEvent event) {

            GDAppEventType eventType = event.getEventType();
            /*
            * Since the callback is an Async Event, it could return
            * before the actual UI control returns from GD to the App after
            * authorization. Hence its advisable to post any UI related bits
            * as a runnable that would be handled after onResume() rather than
            * handling this in the GDAppEventListener.
            */
            if(eventType == GDAppEventType.GDAppEventAuthorized || eventType == GDAppEventType.GDAppEventNotAuthorized) {
            	if (handler == null){
            		handler = new Handler();
            	}
            	doAfterGainingUI(eventType);
            }
            
        }
	}

    /*
			 * Handle the authorization event. If authorized and we haven't yet
			 * created the fragment create it and add it to the activity. As
			 * this process is asynchronous we will pick up the state change in
			 * the fragments onStart() call rather than call back directly. If
			 * the fragment was already attached we can simply call back to
			 * inform it of the state change.
			 *
			 * On not authorized simply call back to the fragment if it exists.
			 */
    public void doAfterGainingUI (final GDAppEventType eventType) {
        runnable = new Runnable() {
            @Override
            public void run() {
                if (eventType == GDAppEventType.GDAppEventAuthorized) {

                    if (m_fragment == null) {
                        m_fragment = new FileBrowserFragment();
                        FragmentManager fragmentManager = getSupportFragmentManager();
                        FragmentTransaction fragmentTransaction = fragmentManager
                                .beginTransaction();
                        fragmentTransaction.replace(R.id.listFragmentSpace,
                                m_fragment, "listFragment");
                        fragmentTransaction.commit();
                    } else {
                        m_fragment.onAuthorizeStateChange(true);
                    }

                } else if (eventType == GDAppEventType.GDAppEventNotAuthorized) {

                    if (mActivity != null) {
                        if (m_fragment != null) {
                            m_fragment.onAuthorizeStateChange(false);
                        }

                    }
                }

            }
        };
    }

    @Override
    public void onResume() {
        super.onResume();
        if(handler != null) {
            handler.post(runnable);
        } 
    }

	@Override
	public void onClick(View v) {
		if(m_fragment != null){
			m_fragment.onClick(v.getId());
			switch (v.getId()) {
			case R.id.action_btn_container:
				updateBtns(R.id.action_btn_container, R.id.action_btn_sdcard);
				break;
			case R.id.action_btn_sdcard:
				updateBtns(R.id.action_btn_sdcard, R.id.action_btn_container);
				break;	
			}
		}
	}

	public void updateBtns(int enabledButton, int disabledButton) {
		((ToggleButton) findViewById(enabledButton)).setChecked(true);
		((ToggleButton) findViewById(disabledButton)).setChecked(false);
	}
}
