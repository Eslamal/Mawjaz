package com.eslamdev.mawjaz.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;
import com.eslamdev.mawjaz.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class TvShowsContainerFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_container, container, false);

        TabLayout subTabLayout = view.findViewById(R.id.sub_tab_layout);
        ViewPager2 subViewPager = view.findViewById(R.id.sub_view_pager);

        TvShowsPagerAdapter adapter = new TvShowsPagerAdapter(getActivity());
        subViewPager.setAdapter(adapter);

        new TabLayoutMediator(subTabLayout, subViewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText(getString(R.string.category_popular));
                    break;
                case 1:
                    tab.setText(getString(R.string.category_top_rated));
                    break;
            }
        }).attach();

        return view;
    }

    private static class TvShowsPagerAdapter extends FragmentStateAdapter {
        public TvShowsPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return ContentFragment.newInstance("tv", "popular", "en-US", null);
                case 1:
                    return ContentFragment.newInstance("tv", "top_rated", "en-US", null);
                default:
                    return ContentFragment.newInstance("tv", "popular", "en-US", null);
            }
        }

        @Override
        public int getItemCount() {
            return 2;
        }
    }
}