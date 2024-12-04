package com.opsc7311poe.gourmetguru_opscpoe

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.MockitoAnnotations


class TimerTest {

    private lateinit var timerFragment: TimerFragment
    private lateinit var mockContext: Context
    private lateinit var mockConnectivityManager: ConnectivityManager
    private lateinit var mockNetworkInfo: NetworkInfo

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        timerFragment = TimerFragment()
        mockContext = Mockito.mock(Context::class.java)
        mockConnectivityManager = Mockito.mock(ConnectivityManager::class.java)
        mockNetworkInfo = Mockito.mock(NetworkInfo::class.java)

        Mockito.`when`(mockContext.getSystemService(Context.CONNECTIVITY_SERVICE))
            .thenReturn(mockConnectivityManager)
    }

    @Test
    fun `test isOnline with active network connection`() {
        Mockito.`when`(mockNetworkInfo.isConnected).thenReturn(true)
        Mockito.`when`(mockConnectivityManager.activeNetworkInfo).thenReturn(mockNetworkInfo)

        assertTrue(timerFragment.isOnline(mockContext))
    }

    @Test
    fun `test isOnline with no network connection`() {
        Mockito.`when`(mockConnectivityManager.activeNetworkInfo).thenReturn(null)

        assertFalse(timerFragment.isOnline(mockContext))
    }
}
