package org.northwinds.defaultbrowser;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.util.Base64;

public class BrowserActivity extends Activity {
	private static final int CREATE_SHORTCUT_REQUEST = 0;
	static final String EXTRA_INTENT = "org.northwinds.intents.EXTRA_INTENT";

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
	Intent intent = new Intent(getIntent());
	intent.setComponent(null);
        //setContentView(R.layout.main);
        setResult(RESULT_CANCELED);
        //startActivityForResult(Intent.createChooser(intent, "Browser"), CREATE_SHORTCUT_REQUEST);
        startActivity(Intent.createChooser(intent, "Browser"));
	finish();
    }

	/* (non-Javadoc)
	 * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == CREATE_SHORTCUT_REQUEST) {
			if(resultCode == RESULT_OK) {
				Intent intent = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT);
				Parcel parcel = Parcel.obtain();
				Bundle extra = null;
				try {
					intent.writeToParcel(parcel, 0);
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
}
