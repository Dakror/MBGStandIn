package de.dakror.mbg;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * @author Maximilian Stark | Dakror
 */
public class SettingsFragment extends PreferenceFragment {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);
	}
}
