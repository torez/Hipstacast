package com.ifrins.hipstacast.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class HipstacastDatabase extends SQLiteOpenHelper {
	private static final String DATABASE = "hipstacastdb"; 
	private static final int DB_VERSION = 2;
	
	public HipstacastDatabase(Context context) {
		super(context, DATABASE, null, DB_VERSION);
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		// Creating a table for the podcasts
		db.execSQL("CREATE  TABLE \"podcasts\" (\"_id\" INTEGER PRIMARY KEY  AUTOINCREMENT  NOT NULL  UNIQUE , \"title\" VARCHAR NOT NULL , \"link\" VARCHAR NOT NULL , " +
					"\"feed_link\" VARCHAR NOT NULL , \"author\" VARCHAR NOT NULL , \"description\" VARCHAR NOT NULL , \"imageUrl\" VARCHAR NOT NULL , \"last_check\" INTEGER NOT NULL , " +
					"\"last_update\" INTEGER NOT NULL , \"etag\" VARCHAR, \"etag_lm\" VARCHAR)");
		
		// Creating a table for the episodes
		
		db.execSQL("CREATE TABLE \"episodes\" (\"_id\" INTEGER PRIMARY KEY  AUTOINCREMENT  NOT NULL  UNIQUE , \"podcast_id\" INTEGER NOT NULL , \"guid\" VARCHAR NOT NULL , " +
				"\"publication_date\" DATETIME NOT NULL , \"author\" VARCHAR NOT NULL , \"description\" VARCHAR NOT NULL , \"content_url\" VARCHAR NOT NULL , " +
				"\"content_length\" INTEGER NOT NULL ,  \"donation_url\" VARCHAR , \"duration\" INTEGER NOT NULL , \"title\" VARCHAR, " +
				"\"shownotes\" VARCHAR, \"status\" INTEGER, \"position\" INTEGER ,\"type\" INTEGER NOT NULL, \"downloaded\" INTEGER)");
		

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (oldVersion < 2) {
			db.execSQL("ALTER TABLE \"podcasts\" ADD COLUMN \"etag\" VARCHAR");
			db.execSQL("ALTER TABLE \"podcasts\" ADD COLUMN \"etag_lm\" VARCHAR");

			db.execSQL("ALTER TABLE \"episodes\" ADD COLUMN \"downloaded\" INTEGER");
		}

	}

}
