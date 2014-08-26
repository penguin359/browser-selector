package org.northwinds.defaultbrowser;

import java.util.List;

import org.northwinds.defaultbrowser.R;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
//import android.os.Parcel;
//import android.util.Base64;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class BrowserActivity extends Activity {
	private static final String TAG = "BrowserActivity";

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
			if(convertView == null)
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

	private class ActivateBrowser implements OnItemClickListener {
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			ResolveInfo info = (ResolveInfo)parent.getAdapter().getItem(position);
			mIntent.setComponent(new ComponentName(info.activityInfo.packageName, info.activityInfo.name));
			startActivity(mIntent);
			finish();
		}
	}

	//private static final int CREATE_SHORTCUT_REQUEST = 0;
	static final String EXTRA_INTENT = "org.northwinds.intents.EXTRA_INTENT";

	private Intent mIntent;

	void startBrowser(SharedPreferences prefs, String basePref, Intent intent) {
		String packagePref = basePref + "_package";
		String namePref = basePref + "_name";
		String packageName = prefs.getString(packagePref, null);
		String name = prefs.getString(namePref, null);
		if(packageName == null || name == null) {
			startActivity(new Intent(this, SettingsActivity.class));
			packageName = prefs.getString(packagePref, null);
			name = prefs.getString(namePref, null);
			if(packageName == null || name == null) {
				Toast.makeText(getApplicationContext(), "Please set a default browser in settings.", Toast.LENGTH_SHORT).show();
				finish();
				return;
			}
		} else if("default".equals(packageName)) {
			startChooser(intent);
			return;
		}
		ComponentName compName = new ComponentName(packageName, name);
		intent.setComponent(compName);
		startActivity(intent);
		finish();
	}

	private void startChooser(Intent intent) {
		mIntent = new Intent(intent);
		mIntent.setComponent(null);
		setContentView(R.layout.main);
		setResult(RESULT_CANCELED);
		//startActivityForResult(Intent.createChooser(mIntent, "Browser"), CREATE_SHORTCUT_REQUEST);
		List<ResolveInfo> list = getPackageManager().queryIntentActivities(mIntent, 0);
		ListView view = (ListView)findViewById(R.id.list);
		PackageAdapter adapter = new PackageAdapter(getApplicationContext(),
				list, R.layout.row, R.id.icon, R.id.text);
		view.setAdapter(adapter);
		view.setOnItemClickListener(new ActivateBrowser());
		//startActivity(Intent.createChooser(mIntent, "Browser"));
		//finish();
	}

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

	SharedPreferences prefs = getSharedPreferences("browser_prefs", MODE_PRIVATE);

	IntentFilter filter = new IntentFilter(Intent.ACTION_VIEW);
	filter.addCategory(Intent.CATEGORY_DEFAULT);
	filter.addCategory(Intent.CATEGORY_BROWSABLE);
	filter.addDataScheme("http");
	filter.addDataScheme("https");
	//filter.addDataScheme("file"); // Firefox/Dolphin
	if(filter.match(getContentResolver(), getIntent(), false, "Match-DefaultBrowser") >= 0) {
		startBrowser(prefs, "http_browser", getIntent());
		return;
	}
	try {
		filter.addDataType("text/html");
		filter.addDataType("text/plain");
		if(filter.match(getContentResolver(), getIntent(), false, "Match-DefaultBrowser") >= 0) {
			startBrowser(prefs, "http_browser_mime", getIntent());
			return;
		}
	} catch(IntentFilter.MalformedMimeTypeException ex) {
		Log.e(TAG, "Failed to parse mime-type: " + ex.toString());
	}

	startChooser(getIntent());
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
}
