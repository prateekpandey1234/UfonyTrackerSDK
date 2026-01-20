package com.ufony.trackersdk

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ufony.trackersdk.UserPreferenceManager.Companion.forUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class TripListActivity : AppCompatActivity(), CoroutineScope {

    val sJob = SupervisorJob()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + sJob

    var tripList = ArrayList<Trip>()
    var searchTripList = ArrayList<Trip>()
    var activeTripList = ArrayList<ActiveRoutes>()
    private var trackerActivityViewModel: TrackerActivitySDKViewModel? = null
    var rv_trip: RecyclerView? = null
    var tv_toolbar: TextView? = null
    var tripListAdapter: TripListAdapter? = null
    var searchView: SearchView? = null
    var img_back: ImageView? = null
    protected var loggedInUserId = 0L
    var prefs: UserPreferenceManager? = null
    private var context: Context? = null

    private var progressBar: ProgressBar?=null
    private var noRoutes: TextView?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_trip_list)
        context = this
        trackerActivityViewModel = ViewModelProvider(this).get(TrackerActivitySDKViewModel::class.java)

        prefs = forUser(loggedInUserId, this)
        val intent = intent
        val args = intent.getBundleExtra("BUNDLE")
        // tripList = (ArrayList<TripSubscription>) args.getSerializable("ARRAYLIST");
        Log.d("TripListActivity", "totalTripIs===" + tripList.size)
        init()
        FontUtils.setFont(this, tv_toolbar, FontUtils.MYRIADPRO_SEMIBOLD, FontUtils.FONT_SIZE_LARGE)
    }

    private suspend fun callApi() {
        trackerActivityViewModel!!.showProgress.observe(this) { it ->
            if (it) {
                progressBar!!.visibility = View.VISIBLE
            } else {
                progressBar!!.visibility = View.GONE
            }
        }

        trackerActivityViewModel!!.isTripEmpty.observe(this){

            if (it){
                rv_trip!!.visibility= View.GONE
                noRoutes!!.visibility= View.VISIBLE
            }else{
                rv_trip!!.visibility= View.VISIBLE
                noRoutes!!.visibility= View.GONE
            }
        }
        trackerActivityViewModel!!.activeTrips.observe(this, Observer { trips ->
            tripList.addAll(trips)
            tripListAdapter!!.updateTripList(tripList)
        })
        trackerActivityViewModel!!.getActiveTrips(loggedInUserId)
    }

    override fun onResume() {
        super.onResume()
        launch {
            callApi()

        }
    }

    //    CustomListener.ActiveRoutesListenerListener activeRoutesListener = new CustomListener.ActiveRoutesListenerListener() {
    //
    //        @Override
    //        public void onUnauthorized() {
    //            Toast.makeText(context,
    //                    getResources().getString(R.string.unauthorized_access),
    //                    Toast.LENGTH_SHORT).show();
    //        }
    //
    //        @Override
    //        public void onSuccess(ActiveRoutes response) {
    //            Log.d("TripList", "ActiveRoutes===="+response);
    //            tripList.addAll(response.getTrips());
    //            for (int i=0;i<tripList.size();i++){
    //                Log.d("TripList", "ActiveRoutes===3="+tripList.get(i).getName());
    //            }
    //            tripListAdapter= new TripListAdapter(tripList,context);
    //            rv_trip.setHasFixedSize(true);
    //            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context, RecyclerView.VERTICAL, false);
    //            rv_trip.setLayoutManager(linearLayoutManager);
    //            rv_trip.setItemAnimator(new DefaultItemAnimator());
    //            rv_trip.setAdapter(tripListAdapter);
    //            tripListAdapter.notifyDataSetChanged();
    //                //prefs.setActiveTrips(response);
    //            //activeTripList=prefs.getGetActiveTrips();
    ////                JSONArray  array = new JSONArray(response);
    ////                for (int i=0;i<array.length();i++){
    ////                    JSONObject obj = array.getJSONObject(i);
    ////                    int routeId=obj.getInt("routeId");
    ////                    String name = obj.getString("name");
    //              //      Log.d("TripList", "ActiveRoutes===2="+activeTripList);
    ////                }
    //
    //            //Toast.makeText(context, "Profile Added successfully",Toast.LENGTH_SHORT).show();
    //           // onBackPressed();
    //        }
    //
    //        @Override
    //        public void onError(String msg) {
    //            if (msg != null)
    //                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    //            else
    //                Toast.makeText(context, R.string.msg_error, Toast.LENGTH_SHORT)
    //                        .show();
    //        }
    //    };
    private fun init() {
        rv_trip = findViewById(R.id.rv_trip)
        searchView = findViewById(R.id.search_trip)
        tv_toolbar = findViewById(R.id.tv_toolbar)
        img_back = findViewById(R.id.tripList_backBtn)
        progressBar=findViewById(R.id.progressBar)
        noRoutes=findViewById(R.id.tv_noRoutes)

        Log.d("TripListActivity", "totalTripIs===" + tripList.size)
        tripListAdapter = TripListAdapter(tripList,loggedInUserId, context)
        rv_trip!!.setHasFixedSize(true)
        var linearLayoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        rv_trip!!.layoutManager = linearLayoutManager
        rv_trip!!.itemAnimator = DefaultItemAnimator()
        rv_trip!!.adapter = tripListAdapter
        tripListAdapter!!.notifyDataSetChanged()
        searchView!!.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }
            override fun onQueryTextChange(query: String): Boolean {
                tripListAdapter!!.filter.filter(query)
                //callSearchTrip(query);
                return false
            }
        })
        img_back!!.setOnClickListener(View.OnClickListener {
            onBackPressed()
        })
    } //    private void callSearchTrip(String query) {

    //
    //        searchTripList=new ArrayList<>();
    //         if (query!=null){
    //
    //          String serachInput=query.toLowerCase();
    //           for (TripSubscription searchTripLists:tripList){
    //               if (searchTripLists.getName().toLowerCase().contains(serachInput)){
    //                   searchTripList.add(searchTripLists);
    //               }
    //               search=true;
    //               tripListAdapter=new TripListAdapter(searchTripList,this);
    //               tripListAdapter.notifyDataSetChanged();
    //
    //           }
    //
    //         }
    //
    //    }
    override fun onDestroy() {
        sJob.cancelChildren()
        super.onDestroy()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val i = Intent(this@TripListActivity, TrackerActivitySDK::class.java)
        startActivity(i)
        finish()
    }
}