package tech.danielwaiguru.fity.ui.views

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.PolylineOptions
import kotlinx.android.synthetic.main.fragment_run_progress.*
import tech.danielwaiguru.fity.R
import tech.danielwaiguru.fity.common.Constants.ACTION_PAUSE
import tech.danielwaiguru.fity.common.Constants.ACTION_START
import tech.danielwaiguru.fity.common.gone
import tech.danielwaiguru.fity.common.visible
import tech.danielwaiguru.fity.service.RunningService
import tech.danielwaiguru.fity.service.route
import tech.danielwaiguru.fity.utils.TimeUtils

class RunProgressFragment : Fragment() {
    private var map: GoogleMap? = null
    private var isUserRunning = false
    private var userRoute = mutableListOf<route>()
    private var currentTimeInMillis = 0L
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_run_progress, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView.onCreate(savedInstanceState)
        initListeners()
        mapView.getMapAsync {
            this.map = it
            drawAllRoutes()
        }
        subscriber()
    }
    private fun initListeners(){
        buttonAction.setOnClickListener { flipRunningStates() }
    }
    private fun drawUserRoute(){
        if (userRoute.isNotEmpty() && userRoute.last().size > 1){
            val preLastRoute = userRoute.last()[userRoute.last().size - 2]
            val lastRoute = userRoute.last().last()
            val polylineOptions = PolylineOptions()
                .color(Color.GREEN)
                .width(8f)
                .add(preLastRoute)
                .add(lastRoute)
            this.map?.addPolyline(polylineOptions)
        }
    }
    private fun drawAllRoutes(){
        for (route in userRoute){
            val polylineOptions = PolylineOptions()
                .color(Color.GREEN)
                .width(8f)
                .addAll(route)
            this.map?.addPolyline(polylineOptions)
        }
    }
    private fun moveCameraToUserFinishLocation() {
        val zoomLevel = 12.0f
        if (userRoute.isNotEmpty() && userRoute.last().isNotEmpty()){
            this.map?.moveCamera(
                CameraUpdateFactory.newLatLngZoom(userRoute.last().last(), zoomLevel)
            )
        }
    }
    private fun updateUi(isUserRunning: Boolean){
        this.isUserRunning = isUserRunning
        if (!isUserRunning){
            buttonAction.text = getString(R.string.start_run)
            finish.visible()
        }
        else {
            buttonAction.text = getString(R.string.stop_run)
            finish.gone()
        }
    }
    private fun flipRunningStates(){
        if (isUserRunning){
            sendIntent(ACTION_PAUSE)
        }
        else {
            sendIntent(ACTION_START)
        }
    }
    private fun subscriber(){
        RunningService.isRunning.observe(viewLifecycleOwner, Observer {
            updateUi(it)
        })
        RunningService.routeCoords.observe(viewLifecycleOwner, Observer {
            userRoute = it
            drawUserRoute()
            moveCameraToUserFinishLocation()
        })
        RunningService.timeRanInMillis.observe(viewLifecycleOwner, Observer {
            currentTimeInMillis = it
            val formattedTime = TimeUtils.formatTime(currentTimeInMillis, true)
            timer.text = formattedTime
        })
    }
    private fun sendIntent(action: String){
        Intent(requireContext(), RunningService::class.java).let {
            it.action = action
            requireContext().startService(it)
        }
    }
    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }
    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView?.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }
}