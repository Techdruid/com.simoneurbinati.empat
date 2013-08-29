package com.simoneurbinati.empat.activities;

import java.net.URL;

import com.simoneurbinati.empat.R;
import com.simoneurbinati.empat.net.Server;
import com.simoneurbinati.empat.persistence.MessagesContentProvider;
import com.simoneurbinati.empat.utils.Utility;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.ActionBarActivity;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class Messages extends ActionBarActivity implements LoaderManager.LoaderCallbacks<Cursor>{

	private long mThreadId;

	private String mPhoneNumber;

	private String mDisplayName;

	private ListView mListView;

	private CursorAdapter mCursorAdapter;
	
	private EditText sendMessage;
	
	private Button buttonSend;

	@Override
	@TargetApi(11)
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Recupera l'ID del thread da mostrare.
		Intent intent = getIntent();
		mThreadId = intent.getLongExtra("thread_id", 0);
		// Carica il thread dal database.
		Cursor c = getContentResolver().query(MessagesContentProvider.THREADS_URI, new String[] { "_id", "phone_number" }, "_id = ?", new String[] { String.valueOf(mThreadId) }, null);
		if (!c.moveToFirst()) {
			// thread non trovato!
			finish();
			return;
		}
		mPhoneNumber = c.getString(1);
		c.close();
		// Risolve, se possibile, il numero dell'interlocutore in rubrica.
		mDisplayName = Utility.resolveContactDisplayName(this, mPhoneNumber);
		// Imposta il titolo e la action bar dove disponibile.
		if (Build.VERSION.SDK_INT >= 11) {
			ActionBar actionBar = getActionBar();
			actionBar.setDisplayHomeAsUpEnabled(true);
			if (mDisplayName != null) {
				actionBar.setTitle(mDisplayName);
				actionBar.setSubtitle(mPhoneNumber);
			} else {
				actionBar.setTitle(mPhoneNumber);
				actionBar.setSubtitle(null);
			}
		} else {
			if (mDisplayName != null) {
				setTitle(mDisplayName + "( " + mPhoneNumber + ")");
			} else {
				setTitle(mPhoneNumber);
			}
		}
		// Crea l'adapter per la lista..
		mCursorAdapter = new MessagesCursorAdapter(this);
		// Carica il layout.
		setContentView(R.layout.activity_messages);
		mListView = (ListView) findViewById(R.id.listview);
		mListView.setAdapter(mCursorAdapter);
		// Carica i messaggi con un loader asincrono.
		getSupportLoaderManager().initLoader(1, null, this);
		
		sendMessage = (EditText) findViewById(R.id.enter_message);
		
		buttonSend = (Button) findViewById(R.id.send_button);

		buttonSend.setOnClickListener( new OnClickListener() {

            @Override
            public void onClick(View v) {
            	sendMessage();
                
            }
        });
		
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_messages, menu);
		return true;
	}

//	@Override
//	public boolean onMenuItemSelected(int featureId, MenuItem item) {
//		int id = item.getItemId();
//		if (id == android.R.id.home) {
//			finish();
//			return true;
//		}
//		if (id == R.id.compose) {
//			Intent intent = new Intent(this, Compose.class);
//			intent.putExtra("phone_number", mPhoneNumber);
//			startActivity(intent);
//			return true;
//		}
//		return false;
//	}
	
//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//		int id = item.getItemId();
//		if (id == android.R.id.home) {
//			finish();
//			return true;
//		}
//		if (id == R.id.compose) {
//			Intent intent = new Intent(this, Compose.class);
//			intent.putExtra("phone_number", mPhoneNumber);
//			startActivity(intent);
//			return true;
//		}
//		return false;
//	}
	

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new CursorLoader(this, MessagesContentProvider.MESSAGES_URI, new String[] { "_id", "direction", "message", "ts_sent" }, "thread = ?", new String[] { String.valueOf(mThreadId) }, "ts_sent ASC");
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		mCursorAdapter.swapCursor(cursor);
		// Esegue una update che imposta a zero i messaggi non letti per il thread.
		ContentValues values = new ContentValues();
		values.put("unread", 0);
		getContentResolver().update(MessagesContentProvider.THREADS_URI, values, "_id = ?", new String[] { String.valueOf(mThreadId) });
		// Scrolla la lista fino in fondo.
		if (mListView.getCount() > 0) {
			mListView.setSelection(mListView.getCount() - 1);
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mCursorAdapter.swapCursor(null);
	}

	private class MessagesCursorAdapter extends CursorAdapter {

		public MessagesCursorAdapter(Context context) {
			super(context, null, FLAG_REGISTER_CONTENT_OBSERVER);
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			return new FrameLayout(context);
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			// Estre i dati dal cursore.
			boolean sent = cursor.getInt(1) == 0;
			String message = cursor.getString(2);
			long sentTimestamp = cursor.getLong(3);
			// Impagina i dati nella view
			int res = sent ? R.layout.list_element_message_sent : R.layout.list_element_message_received;
			View messageView = getLayoutInflater().inflate(res, null);
			TextView tvMessage = (TextView) messageView.findViewById(R.id.message);
			TextView tvTimestamp = (TextView) messageView.findViewById(R.id.timestamp);
			tvMessage.setText(message);
			tvTimestamp.setText(DateFormat.format("yyyy/MM/dd k:mm", sentTimestamp));
			FrameLayout frame = (FrameLayout) view;
			frame.removeAllViews();
			frame.addView(messageView);
		}
	}
	
	public void sendMessage() {
		// Recupera e valida i campi del modulo.
		String recipientPhoneNumber = mPhoneNumber.trim();
		if (recipientPhoneNumber.length() == 0) {
			Toast.makeText(this, getString(R.string.missing_phone_number), Toast.LENGTH_SHORT).show();
			return;
		}
		String message = sendMessage.getText().toString().trim();
		if (message.length() == 0) {
			//Toast.makeText(this, getString(R.string.missing_message), Toast.LENGTH_SHORT).show();
			return;
		}
		// Aggiunge il prefisso al numero, se necessario.
		if (!recipientPhoneNumber.startsWith("+")) {
			recipientPhoneNumber = "+" + Utility.resolveCurrentICC(this) + recipientPhoneNumber;
		}
		// Risolve gli ulteriori dati necessari per l'invio.
		SharedPreferences prefs = getSharedPreferences("registration", MODE_PRIVATE);
		String serverBaseUrl = prefs.getString("server_address", null);
		String privateKey = prefs.getString("private_key", null);
		String senderPhoneNumber = prefs.getString("phone_number", null);
		// Avvia un task parallelo per la chiamata al server.
		AsyncTask<String, Void, Exception> task = new AsyncTask<String, Void, Exception>() {

			private ProgressDialog dialog;

			@Override
			protected void onPreExecute() {
				dialog = new ProgressDialog(Messages.this);
				dialog.setTitle(getString(R.string.send_dialog_title));
				dialog.setMessage(getString(R.string.send_dialog_message));
				dialog.setIndeterminate(true);
				dialog.setCancelable(true);
				dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog) {
						cancel(true);
					}
				});
				dialog.show();
			}

			@Override
			protected Exception doInBackground(String... params) {
				Exception error = null;
				try {
					Server.send(new URL(params[0]), params[1], params[2], params[3], params[4]);
				} catch (Exception e) {
					Log.e("freem", "impossibile inviare il messaggio", e);
					error = e;
				}
				if (error == null) {
					// Salva su database.
					saveSentMessage(params[3], params[4]);
				}
				return error;
			}

			@Override
			protected void onPostExecute(Exception error) {
			    try {
			        dialog.dismiss();
			        dialog = null;
			    } catch (Exception e) {
			    }
				if (error == null) {
					// Termina il composer.
					finish();
					// Mostra avviso.
					Toast.makeText(Messages.this, R.string.send_toast_success, Toast.LENGTH_SHORT).show();
				} else {
					// Mostra errore.
					if (error instanceof Server.UnknownRecipientException) {
						Toast.makeText(Messages.this, R.string.send_toast_unknown_recipient, Toast.LENGTH_LONG).show();
					} else {
						Toast.makeText(Messages.this, R.string.send_toast_error, Toast.LENGTH_SHORT).show();
					}
				}
			}

		};
		task.execute(serverBaseUrl, privateKey, senderPhoneNumber, recipientPhoneNumber, message);
	}

	private void saveSentMessage(String phoneNumber, String message) {
		long now = System.currentTimeMillis();
		ContentResolver r = getContentResolver();
		// Cerca se esiste un thread per l'interlocutore.
		long threadId = -1;
		Cursor c = r.query(MessagesContentProvider.THREADS_URI, new String[] { "_id" }, "phone_number = ?", new String[] { phoneNumber }, null);
		if (c.moveToFirst()) {
			threadId = c.getLong(0);
		}
		c.close();
		if (threadId == -1) {
			// Se non esiste, lo crea.
			ContentValues values = new ContentValues();
			values.put("phone_number", phoneNumber);
			values.put("unread", 0);
			values.put("last_update", now);
			Uri uri = r.insert(MessagesContentProvider.THREADS_URI, values);
			threadId = Long.parseLong(uri.getLastPathSegment());
		} else {
			// Se esiste, lo aggiorna.
			ContentValues values = new ContentValues();
			values.put("last_update", now);
			r.update(MessagesContentProvider.THREADS_URI, values, "phone_number = ?", new String[] { phoneNumber });
		}
		// Aggiunge il messaggio.
		ContentValues values = new ContentValues();
		values.put("thread", threadId);
		values.put("direction", 0);
		values.put("message", message);
		values.put("ts_sent", now);
		r.insert(MessagesContentProvider.MESSAGES_URI, values);
	}

}
