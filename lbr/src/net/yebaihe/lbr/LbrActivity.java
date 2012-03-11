package net.yebaihe.lbr;

import android.app.Activity;
import android.os.Bundle;

public class LbrActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        updateLocationView();
    }

	private void updateLocationView() {
		// TODO Auto-generated method stub
		
	}
}