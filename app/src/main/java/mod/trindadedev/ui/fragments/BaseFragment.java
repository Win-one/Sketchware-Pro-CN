package mod.trindadedev.ui.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.transition.MaterialSharedAxis;

import mod.hey.studios.util.Helper;
import pro.sketchware.R;
import pro.sketchware.SketchApplication;

public class BaseFragment extends Fragment {
    private Activity activity;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setEnterTransition(new MaterialSharedAxis(MaterialSharedAxis.X, true));
        setReturnTransition(new MaterialSharedAxis(MaterialSharedAxis.X, false));
        setExitTransition(new MaterialSharedAxis(MaterialSharedAxis.X, true));
        setReenterTransition(new MaterialSharedAxis(MaterialSharedAxis.X, false));
    }

    public void configureToolbar(MaterialToolbar view) {
        view.setNavigationOnClickListener(Helper.getBackPressedClickListener(getActivity()));
    }

    public void openFragment(Fragment fragment) {
        getParentFragmentManager().beginTransaction()
                .replace(R.id.settings_fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    @Nullable
    @Override
    public Context getContext() {
        if (activity == null) {
            return SketchApplication.getInstance();
        }
        return activity;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = getActivity();
    }
}
