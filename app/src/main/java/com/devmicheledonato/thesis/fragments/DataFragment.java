package com.devmicheledonato.thesis.fragments;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.devmicheledonato.thesis.GeofenceFile;
import com.devmicheledonato.thesis.R;
import com.devmicheledonato.thesis.simplegeofence.SimpleGeofence;
import com.devmicheledonato.thesis.simplegeofence.SimpleGeofenceStore;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;


public class DataFragment extends Fragment {

    private final String TAG = getClass().getSimpleName();
    private Context context;
    private File file;
    private ArrayList<StructureGeofence> array;
    private SimpleGeofenceStore simpleGeofenceStore;
    private SimpleGeofence geofence;

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private MyAdapter mAdapter;

    public DataFragment() {
        // Required empty public constructor
    }

    public static DataFragment newInstance() {
        DataFragment fragment = new DataFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        context = getActivity();
        simpleGeofenceStore = new SimpleGeofenceStore(context);

        file = new File(context.getExternalCacheDir(), GeofenceFile.GEOFENCE_FILENAME);
        if (file.exists()) {
            readFile();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_data, container, false);

        mRecyclerView = (RecyclerView) root.findViewById(R.id.my_recycler_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(context);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter
        mAdapter = new MyAdapter(array);
        mRecyclerView.setAdapter(mAdapter);

        return root;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_fragment, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refreshButton:
                if (file.exists()) {
                    readFile();
                    mAdapter.updateList(array);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void readFile() {
        array = new ArrayList<>();

        FileReader fileReader = null;
        BufferedReader bufferedReader = null;
        try {
            fileReader = new FileReader(file);
            bufferedReader = new BufferedReader(fileReader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String[] lineSplitted = line.split(",");
                StructureGeofence structureGeofence = new StructureGeofence();

                String id = lineSplitted[0];
                Long dateEnter = Long.parseLong(lineSplitted[1]);

                structureGeofence.setId(id);
                structureGeofence.setEnter(dateEnter);

                Long oldDateExit = simpleGeofenceStore.getGeofenceDateExit(id);

                if (dateEnter < oldDateExit) {
                    if (simpleGeofenceStore.getGeofenceDateExit(id) != SimpleGeofenceStore.INVALID_LONG_VALUE) {
                        structureGeofence.setExit(simpleGeofenceStore.getGeofenceDateExit(id));
                    }
                }

//                if (lineSplitted.length == 3) {
//                    structureGeofence.setExit(Long.parseLong(lineSplitted[2]));
//                }
                array.add(structureGeofence);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bufferedReader != null)
                    bufferedReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (fileReader != null)
                    fileReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
        private List<StructureGeofence> geoList;

        public class ViewHolder extends RecyclerView.ViewHolder {
            public TextView geofenceID, enter, exit;
            public CardView cardView;

            public ViewHolder(View v) {
                super(v);
                cardView = (CardView) v.findViewById(R.id.cv);
                geofenceID = (TextView) v.findViewById(R.id.geofenceID);
                enter = (TextView) v.findViewById(R.id.enter);
                exit = (TextView) v.findViewById(R.id.exit);
            }
        }

        public MyAdapter(List<StructureGeofence> list) {
            geoList = list;
        }

        public void updateList(List<StructureGeofence> list) {
            if (geoList != null) {
                geoList.clear();
                geoList.addAll(list);
            } else {
                geoList = list;
            }
            notifyDataSetChanged();
        }

        @Override
        public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            // create a new view
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.geofence_list_view, parent, false);

            return new ViewHolder(v);
        }


        // Replace the contents of a view (invoked by the layout manager)
        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {

            StructureGeofence sgeofence = geoList.get(position);

            holder.geofenceID.setText(sgeofence.getId());

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ITALY);
            Calendar calendar = Calendar.getInstance();

            calendar.setTimeInMillis(sgeofence.getEnter());
            String enter = simpleDateFormat.format(calendar.getTime());

            String exit = "NA";
            if (sgeofence.getExit() != null) {
                calendar.setTimeInMillis(sgeofence.getExit());
                exit = simpleDateFormat.format(calendar.getTime());
            }

            holder.enter.setText("Date enter: " + enter);
            holder.exit.setText("Date exit: " + exit);
        }

        @Override
        public int getItemCount() {
            if (geoList != null) {
                return geoList.size();
            } else {
                return 0;
            }
        }
    }

    private class StructureGeofence {
        String id;
        Long enter, exit;

        public StructureGeofence() {
        }

        public void setId(String value) {
            this.id = value;
        }

        public void setEnter(Long value) {
            this.enter = value;
        }

        public void setExit(Long value) {
            this.exit = value;
        }

        public String getId() {
            return id;
        }

        public Long getEnter() {
            return enter;
        }

        public Long getExit() {
            return exit;
        }
    }
}
