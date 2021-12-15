package com.edgesoft.resulthour.Fragment;

import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.edgsoft.resulthour.R;

public class ContentMainFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstantState) {
        View rootView = inflater.inflate(R.layout.content_main,container,false);
        return rootView;
    }
    @Override
    public void onViewCreated(View view,@Nullable Bundle savedInstanceState) {

    }


}
