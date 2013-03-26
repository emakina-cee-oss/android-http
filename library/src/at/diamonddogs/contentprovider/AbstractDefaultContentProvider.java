package at.diamonddogs.contentprovider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

public abstract class AbstractDefaultContentProvider extends ContentProvider {

	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractDefaultContentProvider.class.getSimpleName());

	protected abstract SQLiteOpenHelper getDatabaseHelper();

	protected abstract String getDefaultTableName();

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		SQLiteDatabase db = getDatabaseHelper().getWritableDatabase();
		int rows = db.delete(getDefaultTableName(), selection, selectionArgs);
		getContext().getContentResolver().notifyChange(uri, null);
		return rows;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		SQLiteDatabase db = getDatabaseHelper().getWritableDatabase();
		long rowId = db.insertWithOnConflict(getDefaultTableName(), null, values, SQLiteDatabase.CONFLICT_REPLACE);
		if (rowId > 0) {
			Uri newUri = Uri.withAppendedPath(uri, String.valueOf(rowId));
			getContext().getContentResolver().notifyChange(newUri, null);
			return newUri;
		}
		throw new SQLException("Failed to insert row into " + uri);
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		SQLiteDatabase db = getDatabaseHelper().getReadableDatabase();
		return db.query(getDefaultTableName(), projection, selection, selectionArgs, null, null, sortOrder);
	}

	@Override
	public int bulkInsert(Uri uri, ContentValues[] values) {
		SQLiteDatabase db = getDatabaseHelper().getWritableDatabase();
		db.beginTransaction();
		int rows = 0;
		try {
			for (ContentValues cv : values) {
				long i = db.insertWithOnConflict(getDefaultTableName(), null, cv, SQLiteDatabase.CONFLICT_REPLACE);
				if (i == -1) {
					LOGGER.error("inserting " + cv + " failed");
				}
				rows++;
			}
			LOGGER.info("inserted - rows: " + rows);
			db.setTransactionSuccessful();
		} catch (Throwable tr) {
			LOGGER.error("Error while bulk inserting", tr);
		} finally {
			db.endTransaction();
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return rows;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		SQLiteDatabase db = getDatabaseHelper().getWritableDatabase();
		int count = db.update(getDefaultTableName(), values, selection, selectionArgs);
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	@Override
	public String getType(Uri uri) {
		return null;
	}

}
