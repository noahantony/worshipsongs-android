package org.worshipsongs.fragment;


import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.worshipsongs.CommonConstants;
import org.worshipsongs.activity.DatabaseSettingActivity;
import org.worshipsongs.activity.ServiceSongListActivity;
import org.worshipsongs.locator.IImportDatabaseLocator;
import org.worshipsongs.locator.ImportDatabaseLocator;
import org.worshipsongs.utils.CommonUtils;
import org.worshipsongs.utils.PropertyUtils;
import org.worshipsongs.worship.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static com.google.android.gms.location.LocationRequest.create;
import static org.worshipsongs.worship.R.style.MyDialogTheme;

/**
 * author  :Pitchumani, madasamy
 * version: 1.0.0
 */
public class ServiceListFragment extends Fragment
{
    private IImportDatabaseLocator importDatabaseLocator = new ImportDatabaseLocator();
    List<String> serviceNames = new ArrayList<String>();
    String serviceName;
    TextView serviceMsg;
    ListAdapter listAdapter;
    private LinearLayout linearLayout;
    private FragmentActivity FragmentActivity;
    private ListView serviceListView;
    private File serviceFile = null;
    private ArrayAdapter<String> adapter;
    private Button defaultDatabaseButton;
    private TextView resultTextView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        FragmentActivity = (FragmentActivity) super.getActivity();
        linearLayout = (LinearLayout) inflater.inflate(R.layout.service_list_activity, container, false);
        serviceListView = (ListView) linearLayout.findViewById(R.id.list_view);
        serviceMsg = (TextView) linearLayout.findViewById(R.id.serviceMsg);
        serviceNames.clear();
        setHasOptionsMenu(true);
        loadService();
        final Vibrator vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
        serviceListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener()
        {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1, final int position, long arg3)
            {
                vibrator.vibrate(15);
                //serviceName = serviceListView.getItemAtPosition(position).toString();
                serviceName = adapter.getItem(position).toString();
                System.out.println("Selected Song for Service:" + serviceName);
                LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
                View promptsView = layoutInflater.inflate(R.layout.delete_confirmation_dialog, null);
                TextView deleteMsg = (TextView) promptsView.findViewById(R.id.deleteMsg);
                deleteMsg.setText(R.string.message_delete_playlist);
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), MyDialogTheme));
                alertDialogBuilder.setView(promptsView);
                alertDialogBuilder.setCancelable(false).setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        serviceFile = PropertyUtils.getPropertyFile(getActivity(), CommonConstants.SERVICE_PROPERTY_TEMP_FILENAME);
                        PropertyUtils.removeProperty(serviceName, serviceFile);
                        Toast.makeText(getActivity(), "Favourite " + serviceName + " deleted...!", Toast.LENGTH_SHORT).show();
                        SongsListFragment listFragment = new SongsListFragment();
                        listFragment.onRefresh();
                        serviceNames.clear();
                        loadService();
                    }
                }).setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        dialog.cancel();
                    }
                });
                final AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.setOnShowListener(new DialogInterface.OnShowListener()
                {
                    @Override
                    public void onShow(DialogInterface dialog)
                    {
                        Button negativeButton = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
                        negativeButton.setTextColor(getResources().getColor(R.color.accent_material_light));
                    }
                });
                alertDialog.show();
                return true;
            }
        });

        serviceListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                serviceName = adapter.getItem(position).toString();
                //serviceName = serviceListView.getItemAtPosition(position).toString();
                System.out.println("Selected Service:" + serviceName);
                Intent intent = new Intent(getActivity(), ServiceSongListActivity.class);
                intent.putExtra("serviceName", serviceName);
                startActivity(intent);
            }
        });
        return linearLayout;
    }

    public void loadService()
    {
        serviceNames.clear();
        readServiceName();
        if (serviceNames.size() <= 0) {
            serviceMsg.setText("You haven't created any Favourite yet!\n" +
                    "Favourites are a great way to organize selected songs for events.\n" +
                    "To add a song to a Favourites, tap the : icon near a song and select the " + "Add to Favourite" + " action.");
        } else {
            serviceMsg.setVisibility(View.GONE);
        }
        adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, serviceNames);
        listAdapter = new ListAdapter(getActivity());
        serviceListView.setAdapter(listAdapter);
    }


    public List readServiceName()
    {
        Properties property = new Properties();
        InputStream inputStream = null;
        try {
            serviceFile = PropertyUtils.getPropertyFile(getActivity(), CommonConstants.SERVICE_PROPERTY_TEMP_FILENAME);
            inputStream = new FileInputStream(serviceFile);
            property.load(inputStream);
            Enumeration<?> e = property.propertyNames();
            while (e.hasMoreElements()) {
                String key = (String) e.nextElement();
                //String value = property.getProperty(key);
                serviceNames.add(key);
            }
            inputStream.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return serviceNames;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        inflater.inflate(R.menu.add_service_fav, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu)
    {
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId()) {
            case R.id.addToList:
                // EITHER CALL THE METHOD HERE OR DO THE FUNCTION DIRECTLY
                System.out.println("Pressed");
                showDatabaseTypeDialog();

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class ImportDatabaseOnClickListener implements View.OnClickListener
    {
        @Override
        public void onClick(View view)
        {
            showDatabaseTypeDialog();
        }
    }

    private void showDatabaseTypeDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.MyDialogTheme);
        builder.setTitle(getString(R.string.type_service));
        builder.setItems(R.array.dataBaseTypes, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                //importDatabaseLocator.load(DatabaseSettingActivity.this, getStringObjectMap(which));
                dialog.cancel();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.getListView().setSelector(android.R.color.background_light);
        dialog.show();
    }

    @NonNull
    private Map<String, Object> getStringObjectMap(int which)
    {
        Map<String, Object> objectMap = new HashMap<String, Object>();
        objectMap.put(CommonConstants.INDEX_KEY, which);
        objectMap.put(CommonConstants.TEXTVIEW_KEY, resultTextView);
        objectMap.put(CommonConstants.REVERT_DATABASE_BUTTON_KEY, defaultDatabaseButton);
        return objectMap;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser)
    {
        super.setUserVisibleHint(isVisibleToUser);
        Log.d(this.getClass().getSimpleName(), "Is visible to user ?" + isVisibleToUser);
        if (isVisibleToUser && getView() != null) {
            loadService();
            CommonUtils.hideKeyboard(getActivity());
        }
    }

    private class ListAdapter extends BaseAdapter
    {
        LayoutInflater inflater;

        public ListAdapter(Context context)
        {
            inflater = LayoutInflater.from(context);
        }

        public View getView(int position, View convertView, ViewGroup parent)
        {
            convertView = inflater.inflate(R.layout.service_listview_content, null);
            TextView serviceName = (TextView) convertView.findViewById(R.id.serviceName);
            Button delete = (Button) convertView.findViewById(R.id.delete);
            delete.setVisibility(View.GONE);
            serviceName.setText(serviceNames.get(position).trim());
            return convertView;
        }

        public int getCount()
        {
            return serviceNames.size();
        }

        public Object getItem(int position)
        {
            return position;
        }

        public long getItemId(int position)
        {
            return position;
        }
    }
}