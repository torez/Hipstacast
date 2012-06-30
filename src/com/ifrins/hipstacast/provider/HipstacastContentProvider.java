package com.ifrins.hipstacast.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public class HipstacastContentProvider extends ContentProvider {

	private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	private static String AUTHORITY = "com.ifrins.hipstacast.provider.HipstacastContentProvider";
	private static final Uri URI = Uri.parse("content://" + AUTHORITY);
	
	private static final int PODCASTS = 100;
	private static final int PODCASTS_ID = 101;
	private static final int PODCASTS_ID_EPISODES = 102;
	private static final int PODCASTS_ID_EPISODES_ID = 103;
	
	private HipstacastDatabase hDB;
	
	private void matchURIs() {
        sUriMatcher.addURI(AUTHORITY, "podcasts", PODCASTS);
        sUriMatcher.addURI(AUTHORITY, "podcasts/*", PODCASTS_ID);
        sUriMatcher.addURI(AUTHORITY, "podcasts/*/episodes", PODCASTS_ID_EPISODES);
        sUriMatcher.addURI(AUTHORITY, "podcasts/*/episodes/*", PODCASTS_ID_EPISODES_ID);

	}
		
	@Override
	public boolean onCreate() {
		matchURIs();
		final Context context = getContext();
		hDB = new HipstacastDatabase(context);
		return true;
	}

	@Override
	public String getType(Uri uri) {
		final int match = sUriMatcher.match(uri);
		if (match == PODCASTS || match == PODCASTS_ID_EPISODES) {
			return "vnd.android.cursor.dir/";
		} else {
			return "vnd.android.cursor.item";
		}
	}

	
	@Override
	public int delete(Uri uri, String where, String[] whereArgs) {
		final SQLiteDatabase db = hDB.getWritableDatabase();
		final int match = sUriMatcher.match(uri);
		int status; 
		if (match == PODCASTS || match == PODCASTS_ID) {
			status = db.delete("podcasts", where, whereArgs);
		} else {
			status = db.delete("episodes", where, whereArgs);
		}
		return status;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		final SQLiteDatabase db = hDB.getWritableDatabase();
		final int match = sUriMatcher.match(uri);
		long id;
		switch (match) {
			case PODCASTS:
				id = db.insertOrThrow("podcasts", null, values);
				getContext().getContentResolver().notifyChange(uri, null);
				return ContentUris.withAppendedId(Uri.parse("content://" + AUTHORITY+ "/podcasts"), id);
				
			case PODCASTS_ID_EPISODES:
				id = db.insert("episodes", null, values);
				getContext().getContentResolver().notifyChange(uri, null);
				return ContentUris.withAppendedId(Uri.parse("content://" + AUTHORITY+ "/podcasts/" + values.getAsString("podcast_id") + "/"), id);
			
			default:
				throw new UnsupportedOperationException("Unknown uri: " + uri);
		}
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		
		final SQLiteDatabase db = hDB.getWritableDatabase();
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		final int match = sUriMatcher.match(uri);
		
		if (match == PODCASTS || match == PODCASTS_ID) {
			qb.setTables("podcasts");
		} else {
			qb.setTables("episodes");
		}
		Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
		c.setNotificationUri(getContext().getContentResolver(), uri);
		return c;

	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		
		final SQLiteDatabase db = hDB.getWritableDatabase();
		final int match = sUriMatcher.match(uri);
		int count;
		switch (match) {
			case PODCASTS_ID:
				count = db.update("podcasts", values, selection, selectionArgs);
				break;
				
			case PODCASTS_ID_EPISODES_ID:
				count = db.update("episodes", values, selection, selectionArgs);
				break;
			default:
				throw new UnsupportedOperationException("Unknown uri: " + uri);
		}

		return count;
	}

}
