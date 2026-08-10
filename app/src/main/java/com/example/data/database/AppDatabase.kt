package com.example.data.database

import androidx.room.*
import com.example.data.model.UserWallet
import com.example.data.model.MatchEntity
import com.example.data.model.MarketEntity
import com.example.data.model.SportMatch
import com.example.data.model.Bet
import com.example.data.model.TransactionRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface WalletDao {
    @Query("SELECT * FROM users WHERE id = 1 LIMIT 1")
    fun getWallet(): Flow<UserWallet?>

    @Query("SELECT * FROM users WHERE id = 1 LIMIT 1")
    suspend fun getWalletDirect(): UserWallet?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    fun getWalletByIdDirect(id: Int): UserWallet?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWallet(wallet: UserWallet)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertWalletDirect(wallet: UserWallet)

    @Update
    suspend fun updateWallet(wallet: UserWallet)
}

@Dao
interface MatchDao {
    @Query("SELECT * FROM sport_matches ORDER BY isLive DESC, id ASC")
    fun getAllMatches(): Flow<List<SportMatch>>

    @Query("SELECT * FROM sport_matches WHERE isLive = 1")
    fun getLiveMatches(): Flow<List<SportMatch>>

    @Query("SELECT * FROM sport_matches WHERE id = :id LIMIT 1")
    suspend fun getMatchById(id: Int): SportMatch?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatches(matches: List<SportMatch>)

    @Update
    suspend fun updateMatch(match: SportMatch)

    @Update
    fun updateMatchDirect(match: SportMatch)

    // Relational core table methods (1:1 with DB schema matches / markets)
    @Query("SELECT * FROM matches ORDER BY id ASC")
    fun getAllMatchEntities(): Flow<List<MatchEntity>>

    @Query("SELECT * FROM matches WHERE id = :id LIMIT 1")
    suspend fun getMatchEntityById(id: Int): MatchEntity?

    @Query("SELECT * FROM matches WHERE id = :id LIMIT 1")
    fun getMatchEntityByIdDirect(id: Int): MatchEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatchEntities(matches: List<MatchEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMatchEntitiesDirect(matches: List<MatchEntity>)

    @Query("SELECT * FROM markets ORDER BY id ASC")
    fun getAllMarketEntities(): Flow<List<MarketEntity>>

    @Query("SELECT * FROM markets WHERE match_id = :matchId AND market_type = :marketType LIMIT 1")
    fun getMarketByMatchAndTypeDirect(matchId: Int, marketType: String): MarketEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMarketEntities(markets: List<MarketEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMarketEntitiesDirect(markets: List<MarketEntity>)

    @Query("SELECT * FROM sport_matches WHERE id = :id LIMIT 1")
    fun getMatchByIdDirect(id: Int): SportMatch?

    @Query("DELETE FROM sport_matches")
    suspend fun clearAllSportMatches()

    @Query("DELETE FROM matches")
    suspend fun clearAllMatchEntities()

    @Query("DELETE FROM markets")
    suspend fun clearAllMarketEntities()
}

@Dao
interface BetDao {
    @Query("SELECT * FROM bets ORDER BY created_at DESC")
    fun getAllBets(): Flow<List<Bet>>

    @Query("SELECT * FROM bets WHERE status = 'PENDING'")
    fun getPendingBetsDirect(): List<Bet>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBet(bet: Bet)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertBetDirect(bet: Bet): Long

    @Update
    suspend fun updateBet(bet: Bet)

    @Update
    fun updateBetDirect(bet: Bet)
}

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY created_at DESC")
    fun getAllTransactions(): Flow<List<TransactionRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionRecord)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTransactionDirect(transaction: TransactionRecord)
}

@Database(
    entities = [
        UserWallet::class,
        MatchEntity::class,
        MarketEntity::class,
        SportMatch::class,
        Bet::class,
        TransactionRecord::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun walletDao(): WalletDao
    abstract fun matchDao(): MatchDao
    abstract fun betDao(): BetDao
    abstract fun transactionDao(): TransactionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: android.content.Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "bet_master_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
