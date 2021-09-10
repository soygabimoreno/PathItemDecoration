package soy.gabimoreno.pathitemdecoration

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import soy.gabimoreno.pathitemdecoration.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var fakeItemDecorator: FakeItemDecoration? = null
    private val listener: (FakeItem) -> Unit = {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        showFakeList()
    }

    private fun showFakeList() {
        val fakeItems = buildFakeItems()
        binding.apply {
            rvFake.apply {
                setHasFixedSize(true)
                layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
                adapter = FakeAdapter { fakeItem, selectedPosition ->
                    listener(fakeItem)
                    updateGraph(fakeItems, selectedPosition)
                }
            }
            val fakeAdapter = rvFake.adapter as FakeAdapter
            fakeAdapter.submitList(fakeItems)
            updateGraph(fakeItems, fakeItems.size - 1)
        }
    }

    private fun updateGraph(
        fakeItems: List<FakeItem>,
        selectedPosition: Int
    ) {
        binding.apply {
            fakeItemDecorator?.let { rvFake.removeItemDecoration(it) }
            fakeItemDecorator = FakeItemDecoration(this@MainActivity, fakeItems, selectedPosition)
            fakeItemDecorator?.let { rvFake.addItemDecoration(it) }
        }
    }

    private fun buildFakeItems() = listOf(
        FakeItem(value = 30.00f, label = "Aug 7"),
        FakeItem(value = 20.00f, label = "Aug 8"),
        FakeItem(value = 20.00f, label = "Aug 9"),
        FakeItem(value = 95.31f, label = "Aug 10"),
        FakeItem(value = 81.82f, label = "Aug 11"),
        FakeItem(value = 72.33f, label = "Yesterday"),
        FakeItem(value = 80.38f, label = "Today")
    )
}
