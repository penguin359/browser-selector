package org.northwinds.defaultbrowser;

import java.util.List;

import org.northwinds.defaultbrowser.R;
import android.app.Activity;
//import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
//import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
//import android.os.Parcel;
//import android.util.Base64;
import android.util.Log;
import android.widget.AdapterView;
//import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
//import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class SettingsActivity extends Activity {
	private static class PackageAdapter extends BaseAdapter {
		private static final String TAG = "PackageAdapter";

		//Context mContext;
		PackageManager mPm;
		LayoutInflater mInflater;
		private List<ResolveInfo> mInfoList;
		int mLayoutResource;
		int mIconId;
		int mTextId;

		PackageAdapter(Context context,
			       List<ResolveInfo> infoList,
			       int layoutResource,
			       int iconId,
			       int textId) {
			//mContext = context;
			mPm = context.getPackageManager();
			mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			mInfoList = infoList;
			mLayoutResource = layoutResource;
			mIconId = iconId;
			mTextId = textId;

			/* Don't include myself in list of browser choices */
			for(ResolveInfo info: mInfoList)
				if(info.activityInfo.packageName.equals(context.getPackageName()))
					mInfoList.remove(info);
					//mInfoList.remove();
		}

		public int getCount() {
			return mInfoList.size();
		}

		public ResolveInfo getItem(int position) {
			return mInfoList.get(position);
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			View view;
			TextView text = null;
			ImageView icon = null;

			view = convertView;
			if(view == null)
				view = mInflater.inflate(mLayoutResource, parent, false);

			try {
				if(mTextId != 0)
					text = (TextView)view.findViewById(mTextId);
				if(mIconId != 0)
					icon = (ImageView)view.findViewById(mIconId);
			} catch(ClassCastException ex) {
				Log.e(TAG, "Invalid resource from layout");
				throw new IllegalStateException("PackageAdapter requires a correct resource ID", ex);
			}

			ResolveInfo info = getItem(position);
			if(text != null)
				text.setText(info.loadLabel(mPm));
			if(icon != null)
				icon.setImageDrawable(info.loadIcon(mPm));

			return view;
		}
	}

	//private static final int CREATE_SHORTCUT_REQUEST = 0;
	static final String EXTRA_INTENT = "org.northwinds.intents.EXTRA_INTENT";

	//private Intent mIntent;

	private SetBrowserPref mHttpPref;
	private SetBrowserPref mHttpMimePref;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

	//IntentFilter a = new IntentFilter(Intent.ACTION_VIEW);
	//a.addCategory(Intent.CATEGORY_DEFAULT);
	//a.addCategory(Intent.CATEGORY_BROWSABLE);
	//a.addDataScheme("http");
	//a.addDataScheme("https");
	////a.addDataScheme("file"); // Firefox/Dolphin

	SharedPreferences prefs = getSharedPreferences("browser_prefs", MODE_PRIVATE);

	Intent browse = new Intent(Intent.ACTION_VIEW, Uri.fromParts("http", "dummy", null)).addCategory(Intent.CATEGORY_BROWSABLE);
	//browse.setDataAndType(,);
	//browse.setType("text/html");
	//browse.setType("text/plain");
	List<ResolveInfo> list = getPackageManager().queryIntentActivities(browse, 0);
	PackageAdapter adapter = new PackageAdapter(getApplicationContext(),
			list, R.layout.row, R.id.icon, R.id.text);
	Spinner view = (Spinner)findViewById(R.id.http_browser_spinner);
	//view.setAdapter(adapter);
	//view.setOnItemSelectedListener(new SetBrowserPref(prefs, "http_browser", adapter, view));
	mHttpPref = new SetBrowserPref(prefs, "http_browser", adapter, view);

	//browse.setType("text/html");
	browse.setDataAndType(Uri.fromParts("http", "dummy", null), "text/html");
	//browse.setType("text/plain");
	list = getPackageManager().queryIntentActivities(browse, 0);
	adapter = new PackageAdapter(getApplicationContext(),
			list, R.layout.row, R.id.icon, R.id.text);
	view = (Spinner)findViewById(R.id.http_browser_mime_spinner);
	//view.setAdapter(adapter);
	//view.setOnItemSelectedListener(new SetBrowserPref(prefs, "http_browser_mime", adapter, view));
	mHttpMimePref = new SetBrowserPref(prefs, "http_browser_mime", adapter, view);
    }

	/* (non-Javadoc)
	 * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
	 */
    /*
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == CREATE_SHORTCUT_REQUEST) {
			if(resultCode == RESULT_OK) {
				Intent intent = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT);
				Parcel parcel = Parcel.obtain();
				Bundle extra = null;
				try {
					mIntent.writeToParcel(parcel, 0);
					extra = new Bundle();
					extra.putString(EXTRA_INTENT, Base64.encodeToString(parcel.marshall(), Base64.DEFAULT));
				} finally {
					parcel.recycle();
				}
				Intent result = new Intent();
				setResult(RESULT_OK, result);
			}
			finish();
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	*/

	@Override
	protected void onPause() {
		//mHttpAdapter.getItem(mHttpSpinner.get
		super.onPause();
	}

	private static class SetBrowserPref implements OnItemSelectedListener {
		private SharedPreferences mPrefs;
		private String mPackagePref;
		private String mNamePref;
		private PackageAdapter mAdapter;
		//private AdapterView<?> mView;

		public SetBrowserPref(SharedPreferences prefs, String basePref, PackageAdapter adapter, Spinner view) {
			mPrefs = prefs;
			mPackagePref = basePref + "_package";
			mNamePref = basePref + "_name";
			mAdapter = adapter;
			//mView = view;

			view.setAdapter(adapter);
			view.setOnItemSelectedListener(this);

			String packageName = prefs.getString(mPackagePref, null);
			String name = prefs.getString(mNamePref, null);
			if(packageName == null || name == null)
				return;
			for(int i = 0; i < adapter.getCount(); i++) {
				ResolveInfo info = adapter.getItem(i);
				if(info.activityInfo.packageName.equals(packageName) &&
				   info.activityInfo.name.equals(name)) {
					view.setSelection(i);
					break;
				}
			}
		}

		public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
			ResolveInfo info = mAdapter.getItem(pos);
			SharedPreferences.Editor editor = mPrefs.edit();
			editor.putString(mPackagePref, info.activityInfo.packageName);
			editor.putString(mNamePref, info.activityInfo.name);
			editor.commit();
		}

		public void onNothingSelected(AdapterView<?> parent) {
			SharedPreferences.Editor editor = mPrefs.edit();
			editor.putString(mPackagePref, null);
			editor.putString(mNamePref, null);
			editor.commit();
		}
	}
}
