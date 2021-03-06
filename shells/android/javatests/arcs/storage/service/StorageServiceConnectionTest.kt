/*
 * Copyright 2019 Google LLC.
 *
 * This code may only be used under the BSD style license found at
 * http://polymer.github.io/LICENSE.txt
 *
 * Code distributed by Google as part of this project is also subject to an additional IP rights
 * grant found at
 * http://polymer.github.io/PATENTS.txt
 */

package arcs.storage.service

import androidx.test.ext.junit.runners.AndroidJUnit4
import arcs.crdt.CrdtCount
import arcs.crdt.parcelables.ParcelableCrdtType
import arcs.data.CountType
import arcs.storage.ExistenceCriteria
import arcs.storage.StoreOptions
import arcs.storage.driver.RamDiskStorageKey
import arcs.storage.parcelables.ParcelableProxyMessage
import arcs.storage.parcelables.toParcelable
import arcs.testutil.assertSuspendingThrows
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** Tests for [StorageServiceConnection]. */
@RunWith(AndroidJUnit4::class)
@UseExperimental(ExperimentalCoroutinesApi::class)
class StorageServiceConnectionTest {
    private lateinit var delegateMock: StorageServiceBindingDelegate
    private lateinit var serviceMock: IStorageService

    @Before
    fun setUp() {
        delegateMock = mock {
            on { bindStorageService(any(), any(), any()) }.doReturn(true)
        }
        serviceMock = object : IStorageService.Stub() {
            override fun registerCallback(callback: IStorageServiceCallback?): Int = 1

            override fun sendProxyMessage(
                message: ParcelableProxyMessage?,
                resultCallback: IResultCallback?
            ) = Unit

            override fun getLocalData(callback: IStorageServiceCallback?) = Unit

            override fun unregisterCallback(token: Int) = Unit
        }
    }

    @Test
    fun isConnected_returnsFalse_whenNeedsDisconnectIsFalse() = runBlockingTest {
        val connection = StorageServiceConnection(delegateMock, OPTIONS, coroutineContext)

        assertThat(connection.isConnected).isFalse()
    }

    @Test
    fun isConnected_returnsFalse_whileServiceIsUnresolved() = runBlockingTest {
        val connection = StorageServiceConnection(delegateMock, OPTIONS, coroutineContext)

        // DelegateMock never calls onServiceConnected, so the service will still be unresolved.
        val deferred = connection.connectAsync()

        assertThat(connection.isConnected).isFalse()

        // Resolve the deferred, so we don't have a dangling job.
        (deferred as CompletableDeferred).complete(serviceMock)
    }

    @Test
    fun connectAsync_returnsDeferredWhichIsResolved_afterOnServiceConnected() = runBlockingTest {
        val connection = StorageServiceConnection(delegateMock, OPTIONS, coroutineContext)

        val deferred = connection.connectAsync()

        connection.onServiceConnected(null, serviceMock.asBinder())

        verify(delegateMock).bindStorageService(eq(connection), any(), eq(OPTIONS))

        assertThat(deferred.isCompleted).isTrue()
        assertThat(connection.isConnected).isTrue()

        assertThat(deferred.await()).isEqualTo(serviceMock)
    }

    @Test
    fun connectAsync_returnsDeferredWhichThrows_afterDelegateFailsToBind() = runBlockingTest {
        val connection = StorageServiceConnection(delegateMock, OPTIONS, coroutineContext)

        whenever(delegateMock.bindStorageService(any(), any(), any())).thenReturn(false)

        val deferred = connection.connectAsync()
        assertSuspendingThrows(IllegalStateException::class) {
            deferred.await()
        }
    }

    @Test
    fun disconnect_callsDelegate_unbindStorageService_whenConnected() = runBlockingTest {
        val connection = StorageServiceConnection(delegateMock, OPTIONS, coroutineContext)

        val deferred = connection.connectAsync()

        // Complete the connection (and thus: the deferred)
        connection.onServiceConnected(null, serviceMock.asBinder())
        deferred.await()

        connection.disconnect()

        verify(delegateMock).unbindStorageService(eq(connection))
    }

    @Test
    fun disconnect_beforeConnect_completesDeferredWithException() = runBlockingTest {
        val connection = StorageServiceConnection(delegateMock, OPTIONS, coroutineContext)

        val deferred = connection.connectAsync()
        connection.disconnect()

        assertSuspendingThrows(IllegalStateException::class) {
            deferred.await()
        }
    }

    companion object {
        private val OPTIONS = StoreOptions<CrdtCount.Data, CrdtCount.Operation, Int>(
            RamDiskStorageKey("myData"),
            ExistenceCriteria.ShouldCreate,
            CountType()
        ).toParcelable(ParcelableCrdtType.Count)
    }
}
