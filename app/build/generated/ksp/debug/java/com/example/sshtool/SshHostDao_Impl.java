package com.example.sshtool;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class SshHostDao_Impl implements SshHostDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<SshHostEntity> __insertionAdapterOfSshHostEntity;

  private final EntityDeletionOrUpdateAdapter<SshHostEntity> __deletionAdapterOfSshHostEntity;

  public SshHostDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfSshHostEntity = new EntityInsertionAdapter<SshHostEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `ssh_hosts` (`id`,`name`,`host`,`user`,`pass`,`scriptsJson`) VALUES (nullif(?, 0),?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final SshHostEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getName());
        statement.bindString(3, entity.getHost());
        statement.bindString(4, entity.getUser());
        statement.bindString(5, entity.getPass());
        statement.bindString(6, entity.getScriptsJson());
      }
    };
    this.__deletionAdapterOfSshHostEntity = new EntityDeletionOrUpdateAdapter<SshHostEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `ssh_hosts` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final SshHostEntity entity) {
        statement.bindLong(1, entity.getId());
      }
    };
  }

  @Override
  public Object insertHost(final SshHostEntity host, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfSshHostEntity.insert(host);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteHost(final SshHostEntity host, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfSshHostEntity.handle(host);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<SshHostEntity>> getAllHosts() {
    final String _sql = "SELECT * FROM ssh_hosts";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"ssh_hosts"}, new Callable<List<SshHostEntity>>() {
      @Override
      @NonNull
      public List<SshHostEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfHost = CursorUtil.getColumnIndexOrThrow(_cursor, "host");
          final int _cursorIndexOfUser = CursorUtil.getColumnIndexOrThrow(_cursor, "user");
          final int _cursorIndexOfPass = CursorUtil.getColumnIndexOrThrow(_cursor, "pass");
          final int _cursorIndexOfScriptsJson = CursorUtil.getColumnIndexOrThrow(_cursor, "scriptsJson");
          final List<SshHostEntity> _result = new ArrayList<SshHostEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final SshHostEntity _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpHost;
            _tmpHost = _cursor.getString(_cursorIndexOfHost);
            final String _tmpUser;
            _tmpUser = _cursor.getString(_cursorIndexOfUser);
            final String _tmpPass;
            _tmpPass = _cursor.getString(_cursorIndexOfPass);
            final String _tmpScriptsJson;
            _tmpScriptsJson = _cursor.getString(_cursorIndexOfScriptsJson);
            _item = new SshHostEntity(_tmpId,_tmpName,_tmpHost,_tmpUser,_tmpPass,_tmpScriptsJson);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
