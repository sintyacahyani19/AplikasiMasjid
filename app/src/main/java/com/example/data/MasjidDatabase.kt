package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [TransactionEntity::class], version = 1, exportSchema = false)
abstract class MasjidDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao

    companion object {
        @Volatile
        private var INSTANCE: MasjidDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): MasjidDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MasjidDatabase::class.java,
                    "masjid_keuangan_db"
                )
                .addCallback(MasjidDatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class MasjidDatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateDatabase(database.transactionDao())
                    }
                }
            }

            suspend fun populateDatabase(dao: TransactionDao) {
                val now = System.currentTimeMillis()
                // Seed with beautiful real-world initial transactions for immediate display polish
                dao.insertTransaction(
                    TransactionEntity(
                        description = "Infaq Jumat Keliling",
                        amount = 2450000.0,
                        isIncome = true,
                        category = "Infaq Jumaat",
                        dateMillis = now - 1 * 24 * 60 * 60 * 1000L // 1 day ago
                    )
                )
                dao.insertTransaction(
                    TransactionEntity(
                        description = "Bayar Tagihan Listrik & Air",
                        amount = 850000.0,
                        isIncome = false,
                        category = "Operasional",
                        dateMillis = now - 2 * 24 * 60 * 60 * 1000L // 2 days ago
                    )
                )
                dao.insertTransaction(
                    TransactionEntity(
                        description = "Donasi Hamba Allah (Pembangunan Kubah)",
                        amount = 5000000.0,
                        isIncome = true,
                        category = "Pembangunan",
                        dateMillis = now - 3 * 24 * 60 * 60 * 1000L // 3 days ago
                    )
                )
                dao.insertTransaction(
                    TransactionEntity(
                        description = "Pembelian Sound System Baru (Mic & Mixer)",
                        amount = 3200000.0,
                        isIncome = false,
                        category = "Sarana Prasarana",
                        dateMillis = now - 4 * 24 * 60 * 60 * 1000L // 4 days ago
                    )
                )
                dao.insertTransaction(
                    TransactionEntity(
                        description = "Penerimaan Zakat Maal Bpk. H. Ahmad",
                        amount = 1500000.0,
                        isIncome = true,
                        category = "Zakat",
                        dateMillis = now - 5 * 24 * 60 * 60 * 1000L // 5 days ago
                    )
                )
                dao.insertTransaction(
                    TransactionEntity(
                        description = "Santunan Anak Yatim dan Dhuafa",
                        amount = 1200000.0,
                        isIncome = false,
                        category = "Sosial / Syiar",
                        dateMillis = now - 6 * 24 * 60 * 60 * 1000L // 6 days ago
                    )
                )
            }
        }
    }
}
