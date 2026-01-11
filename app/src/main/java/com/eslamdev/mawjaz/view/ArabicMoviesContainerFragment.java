package com.eslamdev.mawjaz.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;
import com.eslamdev.mawjaz.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class ArabicMoviesContainerFragment extends Fragment {
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_container, container, false);
        TabLayout subTabLayout = view.findViewById(R.id.sub_tab_layout);
        ViewPager2 subViewPager = view.findViewById(R.id.sub_view_pager);
        subViewPager.setAdapter(new PagerAdapter(requireActivity()));
        new TabLayoutMediator(subTabLayout, subViewPager, (tab, position) -> {
            tab.setText(position == 0 ? R.string.category_egyptian : R.string.category_gulf);
        }).attach();
        return view;
    }

    private static class PagerAdapter extends FragmentStateAdapter {
        public PagerAdapter(@NonNull FragmentActivity fa) { super(fa); }
        @NonNull @Override public Fragment createFragment(int pos) {
            return ContentFragment.newInstance("movie", "discover", null, pos == 0 ? "EG" : "SA");
        }
        @Override public int getItemCount() { return 2; }
    }
}