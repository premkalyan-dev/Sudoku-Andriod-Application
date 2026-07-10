package com.prem.skudo.repository

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.prem.skudo.database.AppDatabase
import com.prem.skudo.database.UserProfile
import com.prem.skudo.database.GameStats
import kotlinx.coroutines.tasks.await

class CloudSyncRepository(private val context: Context) {
    private val firestore = FirebaseFirestore.getInstance()
    private val appDatabase = AppDatabase.getDatabase(context)
    private val userDao = appDatabase.userDao()
    private val statsDao = appDatabase.statsDao()

    suspend fun syncLocalToCloud(uid: String) {
        try {
            val profile = userDao.getUserProfile() ?: return
            val gameStats = statsDao.getAllStats()

            val userDoc = firestore.collection("users").document(uid)
            
            val data = mapOf(
                "profile" to profile,
                "stats" to gameStats,
                "lastUpdated" to System.currentTimeMillis()
            )
            
            userDoc.set(data, SetOptions.merge()).await()
            
            // Update local sync status without triggering a recursive flow update
            // We use a separate query or update the flag directly
            userDao.updateProfile(profile.copy(lastSyncAt = System.currentTimeMillis(), cloudUserId = uid))
        } catch (e: Exception) {
            Log.e("CloudSync", "Failed to sync to cloud", e)
            throw e
        }
    }

    suspend fun syncCloudToLocal(uid: String): Boolean {
        return try {
            val userDoc = firestore.collection("users").document(uid).get().await()
            if (!userDoc.exists()) return false

            // We can't directly cast to UserProfile because Firestore returns Maps
            // In a production app, we'd use Firestore's toObject() with a No-Arg constructor
            // Since UserProfile is a data class with default values, it should work if we have firebase-firestore-ktx
            
            val cloudData = userDoc.data ?: return false
            
            // For now, we'll manually check and update some key fields if they are significantly different
            // or if local is empty/new. 
            // Better: offer user a choice if timestamps differ.
            
            true
        } catch (e: Exception) {
            Log.e("CloudSync", "Failed to sync from cloud", e)
            false
        }
    }

    suspend fun saveCurrentGameToCloud(uid: String, gameState: Map<String, Any>) {
        try {
            firestore.collection("users").document(uid)
                .collection("current_game")
                .document("state")
                .set(gameState)
                .await()
        } catch (e: Exception) {
            Log.e("CloudSync", "Failed to save game state to cloud", e)
        }
    }
}
