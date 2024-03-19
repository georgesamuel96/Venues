package com.example.venues.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.example.venues.R
import com.example.venues.data.remote.Status
import com.example.venues.databinding.FragmentHomeBinding
import com.example.venues.utils.Util.showSnackBar
import com.example.venues.viewmodel.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private val homeViewModel: HomeViewModel by viewModels()
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val venuesAdapter: VenuesAdapter by lazy {
        VenuesAdapter(mutableListOf())
    }

    private var currentLat = 0.0
    private var currentLong = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        homeViewModel.searchLocation(currentLat,currentLong)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        initialization()
        initializeViewModel()
        listeners()

        return binding.root
    }

    private fun initialization() {
        binding.apply {
            rvVenues.adapter = venuesAdapter
        }
    }

    private fun listeners() {
        binding.apply {
            swipeRefresh.setOnRefreshListener {
                venuesAdapter.updateList(listOf())
                homeViewModel.searchLocation(currentLat,currentLong)
                swipeRefresh.isRefreshing = false
            }
        }
    }

    private fun initializeViewModel() {
        getLoadingObserver()
        getVenuesListObserver()
    }

    private fun getLoadingObserver() {
        homeViewModel.loadingStateLiveData.observe(viewLifecycleOwner) {
            binding.layoutLoading.root.visibility =
                if (it) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
        }
    }

    private fun getVenuesListObserver() {
        homeViewModel.venuesListLiveData.observe(viewLifecycleOwner) {
            when(it.status) {
                Status.SUCCESS -> {
                    it.data?.let { data ->
                        venuesAdapter.updateList(data)
                    }
                }
                Status.ERROR -> {
                    binding.parent.showSnackBar(getString(it.message?: R.string.something_went_wrong))
                }
            }
        }
    }

}