package by.riewe.cadence.data.db

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Database
import by.riewe.cadence.data.dao.*
import by.riewe.cadence.data.entity.*

@Database(
    entities = [
        CadenzzaEntity::class,
        Period::class,
        Route::class,
        Refueling::class,
        Expense::class,
        TrailerCouplings::class
    ],
    version = 1,
    exportSchema = false
)

abstract class CadenzzaDatabase : RoomDatabase() {

    abstract fun cadenzzaDao(): CadenzzaDao
    abstract fun periodDao(): PeriodDao
    abstract fun routeDao(): RouteDao
    abstract fun refuelingDao(): RefuelingDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun trailerCouplingsDao(): TrailerCouplingsDao

    companion object {
        @Volatile
        private var INSTANCE: CadenzzaDatabase? = null

            fun getDatabase(context: Context): CadenzzaDatabase {
               return INSTANCE ?: synchronized(this) {
                  val instance = Room.databaseBuilder(
                      context.applicationContext,
                      CadenzzaDatabase::class.java,
                      "cadenza_database"
                  )
                      .fallbackToDestructiveMigration()
                      .build()
                  INSTANCE = instance
                   instance
               }
            }
    }

}