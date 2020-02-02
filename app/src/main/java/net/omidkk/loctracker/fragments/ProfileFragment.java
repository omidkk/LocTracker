package net.omidkk.loctracker.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;

import net.omidkk.loctracker.R;
import net.omidkk.loctracker.model.User;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {

    private CircleImageView imgProfile;
    private AppCompatTextView txtName;
    private User user;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_profile,container,false);
        initViews(view);

        user=User.getUser();

        updateUI();

        return view;
    }

    private void initViews(View view) {
        imgProfile=view.findViewById(R.id.img_profile);
        txtName=view.findViewById(R.id.txt_name);
    }

    private void updateUI() {
        txtName.setText(user.getName());
        Glide
                .with(this)
                .load(user.getPhoto())
                .placeholder(R.drawable.img_placeholder2)
                .skipMemoryCache(true)
                .into(imgProfile);
    }

}
