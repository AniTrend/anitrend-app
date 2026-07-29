package com.mxt.anitrend.architecture

import android.content.Context
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filter.FilterResults
import androidx.recyclerview.widget.RecyclerView
import com.mxt.anitrend.base.custom.recycler.RecyclerViewAdapter
import com.mxt.anitrend.base.custom.recycler.RecyclerViewHolder
import java.util.ArrayList
import org.junit.Ignore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock

class RecyclerViewAdapterOnItemRangeChangedTest {

    @Ignore("Legacy RecyclerViewAdapter behavior is outside Phase 2 scope")
    @Test
    fun `full swap change notifies dataset change for smaller replacement`() {
        val context = mock(Context::class.java)
        doReturn(context).`when`(context).applicationContext

        val adapter = TestRecyclerViewAdapter(context)
        initializeObservers(adapter)
        val observer = RecordingObserver()
        adapter.registerAdapterDataObserver(observer)

        adapter.onItemsInserted(listOf(1, 2, 3))
        observer.reset()

        adapter.onItemRangeChanged(listOf(1))

        assertEquals(listOf(1), adapter.snapshot())
        assertEquals(1, observer.changedCount)
        assertTrue(observer.rangeChanges.isEmpty())
    }

    private class TestRecyclerViewAdapter(
        context: Context,
    ) : RecyclerViewAdapter<Int>(context) {
        fun snapshot(): List<Int> = data.toList()

        override fun getFilter(): Filter = object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults = FilterResults()

            override fun publishResults(
                constraint: CharSequence?,
                results: FilterResults?,
            ) = Unit
        }

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int,
        ): RecyclerViewHolder<Int> = throw UnsupportedOperationException("Not required for this test")
    }

    private class RecordingObserver : RecyclerView.AdapterDataObserver() {
        var changedCount: Int = 0
        val rangeChanges = mutableListOf<Pair<Int, Int>>()

        override fun onChanged() {
            changedCount += 1
        }

        override fun onItemRangeChanged(
            positionStart: Int,
            itemCount: Int,
        ) {
            rangeChanges.add(positionStart to itemCount)
        }

        fun reset() {
            changedCount = 0
            rangeChanges.clear()
        }
    }

    private fun initializeObservers(adapter: RecyclerView.Adapter<*>) {
        val observableField = RecyclerView.Adapter::class.java.getDeclaredField("mObservable")
        observableField.isAccessible = true
        val observable = observableField.get(adapter)
        val observersField = observable.javaClass.superclass.getDeclaredField("mObservers")
        observersField.isAccessible = true
        if (observersField.get(observable) == null) {
            observersField.set(observable, ArrayList<Any>())
        }
    }
}
