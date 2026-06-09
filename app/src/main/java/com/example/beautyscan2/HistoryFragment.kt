package com.example.beautyscan2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

class HistoryFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rvHistoryList = view.findViewById<RecyclerView>(R.id.rvHistoryList)
        rvHistoryList.layoutManager = LinearLayoutManager(requireContext())

        // Łączymy się z bazą danych i każemy jej nasłuchiwać zmian na żywo
        viewLifecycleOwner.lifecycleScope.launch {
            val database = AppDatabase.getDatabase(requireContext())

            // Collect reaguje za każdym razem, gdy cokolwiek zmieni się w bazie
            database.historyDao().getAllHistory().collect { historyData ->
                rvHistoryList.adapter = HistoryAdapter(historyData)
            }
        }
    }
}