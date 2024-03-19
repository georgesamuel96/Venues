package com.example.venues.ui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.venues.R
import com.example.venues.data.remote.Status
import com.example.venues.databinding.FragmentHomeBinding
import com.example.venues.utils.Util
import com.example.venues.utils.Util.showAlert
import com.example.venues.utils.Util.showSnackBar
import com.example.venues.viewmodel.HomeViewModel
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
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

    private lateinit var locationRequest: LocationRequest.Builder
    private lateinit var resolvableApiException: ResolvableApiException

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkGPSPermission()
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
                checkGPSPermission()
                swipeRefresh.isRefreshing = false
            }

            binding.btnOpenLocation.setOnClickListener {
                binding.containerLocation.visibility = View.GONE
                checkGPSPermission()
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
            when (it.status) {
                Status.SUCCESS -> {
                    it.data?.let { data ->
                        venuesAdapter.updateList(data)
                    }
                }

                Status.ERROR -> {
                    binding.parent.showSnackBar(
                        getString(
                            it.message ?: R.string.something_went_wrong
                        )
                    )
                }
            }
        }
    }

    private fun checkGPSPermission() {
        val locationManager =
            requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        locationRequest = LocationRequest.Builder(5000)
        locationRequest.setPriority(Priority.PRIORITY_HIGH_ACCURACY)

        val builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(locationRequest.build())

        if (!isGPSEnabled) {
            builder.setAlwaysShow(true)

            val locationSettingResponseTask = LocationServices.getSettingsClient(requireContext())
                .checkLocationSettings(builder.build())

            locationSettingResponseTask.addOnSuccessListener {
                checkGetLocationPermissions()
            }.addOnFailureListener {
                if (it is ResolvableApiException) {
                    resolvableApiException = it
                    showEnableLocation()
                }
            }
        } else {
            checkGetLocationPermissions()
        }
    }

    private fun showEnableLocation() {
        try {
            val intentSenderRequest =
                IntentSenderRequest.Builder(resolvableApiException.resolution).build()
            gpsPermissionRequest.launch(intentSenderRequest)
        } catch (sendIntentException: IntentSender.SendIntentException) {
            sendIntentException.printStackTrace()
        }
    }

    private val gpsPermissionRequest =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            when (result.resultCode) {
                Activity.RESULT_OK -> {
                    checkGPSPermission()
                }

                Activity.RESULT_CANCELED -> {
                    showAlertEnableLocation()
                }

                else -> {
                    showAlertEnableLocation()
                }
            }
        }

    private fun showAlertEnableLocation() {
        requireActivity().showAlert(
            title = getString(R.string.error),
            message = getString(R.string.message_enable_gps_location),
            textPositiveButton = getString(R.string.enable)
        ) {
            showEnableLocation()
        }
    }

    private fun checkGetLocationPermissions() {
        if (hasPermission()) {
            getCurrentLocation()
        } else {
            locationPermissionRequest.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allPermissionsGranted = permissions.all { it.value }
        if (allPermissionsGranted) {
            getCurrentLocation()
        } else {
            showAlertLocationPermission()
        }
    }

    private fun showAlertLocationPermission() {
        requireActivity().showAlert(
            title = getString(R.string.error),
            message = getString(R.string.message_location_permission),
            canCancel = false,
            textPositiveButton = getString(R.string.settings)
        ) {
            Util.goToAppDetailsSettings(requireActivity())
        }
    }

    private fun getCurrentLocation() {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.requestLocationUpdates(
                locationRequest.build(),
                object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult) {
                    }
                },
                null
            )

            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        searchOnLocation(location)
                    } else {
                        venuesAdapter.updateList(listOf())
                        binding.containerLocation.visibility = View.VISIBLE
                    }
                }
                .addOnFailureListener {
                    venuesAdapter.updateList(listOf())
                    binding.containerLocation.visibility = View.VISIBLE
                }
        }
    }

    private fun searchOnLocation(location: Location) {
        currentLat = location.latitude
        currentLong = location.longitude

        homeViewModel.searchLocation(currentLat, currentLong)
    }

    private fun hasPermission(): Boolean {
        return when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                true
            }

            else -> {
                false
            }
        }
    }
}