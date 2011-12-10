/*
 * Copyright 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.geozen.smarttrail.ui.phone;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuItem;

import com.geozen.smarttrail.R;
import com.geozen.smarttrail.ui.BaseSinglePaneActivity;
import com.geozen.smarttrail.ui.TrailsFragment;

public class TrailsActivity extends BaseSinglePaneActivity {
	

	
    @Override
    protected Fragment onCreatePane() {
        return new TrailsFragment();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        getActivityHelper().setupSubActivity();
    }
    
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.refresh_menu_items, menu);
		
		super.onCreateOptionsMenu(menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
//		if (item.getItemId() == R.id.menu_refresh) {
//			triggerRefresh();
//			return true;
//		} 
//		else if (item.getItemId() == R.id.menu_add_condition) {
//			Fragment frag = new AddConditionFragment();
//			
//			Bundle args = new Bundle();
//	        args.putParcelable(AddConditionFragment.EXTRA_TRAILS_URI, AreasSchema.buildTrailsUri("marshallmesa"));
//			frag.setArguments(args);
//			
//			FragmentTransaction transaction = getSupportFragmentManager()
//					.beginTransaction();
//
//			// Replace whatever is in the fragment_container view with this
//			// fragment,
//			// and add the transaction to the back stack
//			transaction.replace(R.id.root_container, frag);
//			transaction.addToBackStack(null);
//
//			// Commit the transaction
//			transaction.commit();
//			return true;
//		}
		return super.onOptionsItemSelected(item);
	}




}
