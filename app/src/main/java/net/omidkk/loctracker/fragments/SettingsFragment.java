package net.omidkk.loctracker.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;
import net.omidkk.loctracker.R;
import net.omidkk.loctracker.utils.Constants;


public class SettingsFragment extends Fragment implements View.OnClickListener {

    private SharedPreferences preferences;
    private RelativeLayout btnChangeDistance;
    private AppCompatButton btnOk,btnCansel;
    private AppCompatEditText edtDistance;
    private AppCompatTextView txtDistance;
    private int distance;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_settings,container,false);
        initViews(view);

        getSettings();
        txtDistance.setText(String.valueOf(distance));

        return view;
    }

    private void getSettings() {
        preferences=getActivity().getSharedPreferences(Constants.KEY_SETTINGS_PREFERENCES, Context.MODE_PRIVATE);
        distance=preferences.getInt(Constants.KEY_DISTANCE,Constants.DEFAULT_VALUE_DISTANCE);
    }

    private void initViews(View view) {
        btnChangeDistance=view.findViewById(R.id.btn_change_distance);
        btnChangeDistance.setOnClickListener(this);
        txtDistance=view.findViewById(R.id.txt_distance);
        txtDistance.setText(String.valueOf(distance));
    }

    @Override
    public void onClick(View view) {
        //handle change distance settings
        if (view.getId()==btnChangeDistance.getId()){
            final Dialog dialog=new Dialog(getContext());
            dialog.setContentView(R.layout.dialog_change_distance);

            btnOk=dialog.findViewById(R.id.btn_ok);
            btnCansel=dialog.findViewById(R.id.btn_cansel);
            edtDistance=dialog.findViewById(R.id.edt_distance);
            edtDistance.setText(String.valueOf(distance));
            dialog.show();
            btnOk.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    distance=Integer.valueOf(edtDistance.getText().toString());
                    updateSettings(distance);
                    txtDistance.setText(String.valueOf(distance));
                    dialog.dismiss();
                }
            });
            btnCansel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                }
            });
        }
    }

    private void updateSettings(int distance) {
        SharedPreferences.Editor editor=preferences.edit();
        editor.putInt(Constants.KEY_DISTANCE,distance);
        editor.apply();
    }
}

