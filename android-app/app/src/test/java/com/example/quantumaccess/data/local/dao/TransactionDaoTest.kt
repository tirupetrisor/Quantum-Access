package com.example.quantumaccess.data.local.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.quantumaccess.data.local.QuantumAccessDatabase
import com.example.quantumaccess.data.local.entities.LocalTransactionEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.Instant
import java.util.UUID

/**
 * Test pentru TransactionDao folosind Room in-memory.
 * Necesită Robolectric pentru execuție în src/test sau să fie mutat în src/androidTest.
 */
@RunWith(RobolectricTestRunner::class)
class TransactionDaoTest {

    private lateinit var db: QuantumAccessDatabase
    private lateinit var dao: TransactionDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, QuantumAccessDatabase::class.java)
            .allowMainThreadQueries() // Pentru teste simple
            .build()
        dao = db.transactionDao()
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun insertAndGetTransaction() = runTest {
        val id = UUID.randomUUID()
        val transaction = LocalTransactionEntity(
            transactionId = id,
            amount = 100.0,
            mode = "QUANTUM",
            status = "COMPLETED",
            intercepted = false,
            createdAt = Instant.now()
        )

        dao.insert(transaction)

        val loaded = dao.getById(id)
        assertEquals(transaction.amount, loaded?.amount)
        assertEquals(transaction.mode, loaded?.mode)
    }

    @Test
    fun getAllFlowEmitsUpdates() = runTest {
        val t1 = LocalTransactionEntity(UUID.randomUUID(), 50.0, "NORMAL", "PENDING", false, Instant.now())
        dao.insert(t1)

        val list = dao.getAllFlow().first()
        assertEquals(1, list.size)
        assertEquals("NORMAL", list[0].mode)

        val t2 = LocalTransactionEntity(UUID.randomUUID(), 200.0, "QUANTUM", "COMPLETED", true, Instant.now())
        dao.insert(t2)

        val listUpdated = dao.getAllFlow().first()
        assertEquals(2, listUpdated.size)
    }
}

