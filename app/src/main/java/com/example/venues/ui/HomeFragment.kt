package com.example.venues.ui

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.example.venues.R
import com.example.venues.data.model.Venues
import com.example.venues.data.remote.Status
import com.example.venues.databinding.FragmentHomeBinding
import com.example.venues.databinding.ItemVenuesBinding
import com.example.venues.utils.Util
import com.example.venues.utils.Util.getAddress
import com.example.venues.utils.Util.getIconURL
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
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

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

    private fun initializeMap() {
        // Get the SupportMapFragment and request notification when the map is ready to be used.
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)
    }


    override fun onMapReady(map : GoogleMap) {
        val venuesList = venuesAdapter.getVenuesList()
        val markersList = mutableListOf<LatLng>()


        // Add some markers to the map, and add a data object to each marker.
        for(venues in venuesList.withIndex()) {
            markersList.add(LatLng(venues.value.location.lat.toDouble(), venues.value.location.lng.toDouble()))

            val marker = map.addMarker(
                MarkerOptions()
                    .position(markersList.last())
                    .title(venues.value.name)
            )
            marker?.tag = venues.index
        }
        // Set a listener for marker click.
        map.setOnMarkerClickListener(this)

        val markerBounds = LatLngBounds.Builder()

        // Loop through your markers and add their locations to the builder
        for (marker in markersList) {
            markerBounds.include(marker)
        }
        val bounds = markerBounds.build()
        val padding = 100

        // Animate the camera to show all markers with padding
        map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding))
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        // Retrieve the data from the marker.
        val index = marker.tag as? Int
        index?.let {
            val venues = venuesAdapter.getVenuesAt(it)

            showAlertForVenuesDetails(venues)
        }

        return false
    }

    private fun showAlertForVenuesDetails(venues: Venues) {
        val dialog = Dialog(requireContext(), R.style.PauseDialog)
        val inflater =
            requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        val binding = ItemVenuesBinding.inflate(inflater)
        dialog.setContentView(binding.root)

        binding.apply {
            tvName.text = venues.name
            tvAddress.text = getString(R.string.full_address, getAddress(venues))
            if (venues.categories.isNotEmpty()) {
                tvCategory.text =
                    getString(R.string.category, venues.categories[0].name)
                Glide.with(requireContext())
                    .load(getIconURL(venues))
                    .into(ivCategory)

                groupCategory.visibility = View.VISIBLE
            } else {
                groupCategory.visibility = View.GONE
            }
        }

        val width = (resources.displayMetrics.widthPixels * 0.85).toInt()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.show()
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

            switchMap.setOnCheckedChangeListener { _, isChecked ->
                if(isChecked) {
                    swipeRefresh.visibility = View.GONE
                    map.visibility = View.VISIBLE
                    initializeMap()
                } else {
                    map.visibility = View.GONE
                    swipeRefresh.visibility = View.VISIBLE
                }
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

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}