package com.example.quan_ly_tro.data.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.quan_ly_tro.data.database.entity.User;

import java.util.List;

@Dao
public interface UserDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(User user);
    
    @Update
    void update(User user);
    
    @Delete
    void delete(User user);
    
    @Query("SELECT * FROM users WHERE uid = :uid LIMIT 1")
    User getUserByUidSync(String uid);
    
    @Query("SELECT * FROM users WHERE uid = :uid LIMIT 1")
    LiveData<User> getUserByUid(String uid);
    
    @Query("SELECT * FROM users WHERE role = 'LANDLORD' LIMIT 1")
    User getLandlordSync();
    
    @Query("SELECT * FROM users WHERE role = :role")
    LiveData<List<User>> getUsersByRole(String role);

    @Query("DELETE FROM users")
    void deleteAll();
}
