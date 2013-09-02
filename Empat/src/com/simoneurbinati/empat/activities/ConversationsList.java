package com.simoneurbinati.empat.activities;


import com.simoneurbinati.empat.R;
import com.simoneurbinati.empat.persistence.MessagesContentProvider;
import com.simoneurbinati.empat.utils.Utility;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.ActionBarActivity;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;


public class ConversationsList extends ActionBarActivity implements LoaderManager.LoaderCallbacks<Cursor> {
	
	private ListView mListView;
	private CursorAdapter mCursorAdapter;
	
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Controllo che il telefono sia stato registrato.
		SharedPreferences prefs = getSharedPreferences("registration", Context.MODE_PRIVATE);
		if (!prefs.getBoolean("registered", false)) {
			finish();
			startActivity(new Intent(this, Login.class));
			return;
		}
		setContentView(R.layout.activity_conversationlist);
		mListView = (ListView) findViewById(R.id.listView1);
		mCursorAdapter = new ThreadsCursorAdapter(this);
		mListView.setAdapter(mCursorAdapter);
		mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> l, View v, int position, long id) {
				openThread(mCursorAdapter.getItemId(position));
			}
		});
		//carico la lista dei thread
		getSupportLoaderManager().initLoader(1, null, this);
		
		//setto la ActionBar
		getSupportActionBar().setIcon(R.drawable.logo_bar);
		getSupportActionBar().setDisplayShowTitleEnabled(false);


	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_conversationslist, menu);
		return true;
	}

	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.compose) {
			startActivity(new Intent(this, Compose.class));
			return true;
		}
		return false;
	}

	//metodi di LoaderCallbacks
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new CursorLoader(this, MessagesContentProvider.THREADS_URI, new String[] { "_id", "phone_number", "last_update", "unread" }, null, null, "last_update DESC");
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		mCursorAdapter.swapCursor(cursor);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mCursorAdapter.swapCursor(null);
	}

	private void openThread(long threadId) {
		Intent intent = new Intent(this, Messages.class);
		intent.putExtra("thread_id", threadId);
		startActivity(intent);
	}
	

	
	private class ThreadsCursorAdapter extends CursorAdapter {

		public ThreadsCursorAdapter(Context context) {
			super(context, null, FLAG_REGISTER_CONTENT_OBSERVER);
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			LayoutInflater inflater = getLayoutInflater();
			return inflater.inflate(R.layout.conversationlist_element, null);
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			final String phoneNumber = cursor.getString(1);
			String displayName = Utility.resolveContactDisplayName(context, phoneNumber);
			
			//immagine contatto
//			final ImageView profile  = (ImageView)view.findViewById(R.id.picture);
//			//profile.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.notify_icon_small));
//			
//			
//			Bitmap img = Utility.resolveContactPhoto(context, phoneNumber);			
//			//String url = Utility.resolveContactPhotoUrl(context, phoneNumber);
//			if(img != null){
//				//aq.id(R.id.picture).getCachedImage(url);
//				//aq.id(R.id.picture).
//				profile.setImageBitmap(img);
//			}
			
			long lastUpdate = cursor.getLong(2);
			int unreadMessages = cursor.getInt(3);
			TextView tvDisplayName = (TextView) view.findViewById(R.id.display_name);
			TextView tvPhoneNumber = (TextView) view.findViewById(R.id.phone_number);
			TextView tvLastMessageTimestamp = (TextView) view.findViewById(R.id.last_message_timestamp);
			TextView tvUnreadMessages = (TextView) view.findViewById(R.id.unread_messages);
			if (displayName != null) {
				tvDisplayName.setText(displayName);
				tvPhoneNumber.setText(phoneNumber);
			} else {
				tvDisplayName.setText(phoneNumber);
				tvPhoneNumber.setText("");
			}
			tvLastMessageTimestamp.setText(DateFormat.format("yyyy/MM/dd k:mm", lastUpdate));
			tvUnreadMessages.setVisibility(unreadMessages > 0 ? View.VISIBLE : View.INVISIBLE);
			tvUnreadMessages.setText(String.valueOf(unreadMessages));
		}

	}

}


