package com.eslamdev.mawjaz.view;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;
import com.eslamdev.mawjaz.R;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Your Application class should handle loading the theme and locale
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // --- Toolbar Setup ---
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.app_name);
        }

        // --- ViewPager and TabLayout Setup ---
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        ViewPager2 viewPager = findViewById(R.id.viewPager);
        viewPager.setAdapter(new MainPagerAdapter(this));

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText(R.string.main_tab_movies); // Use string resources
                    break;
                case 1:
                    tab.setText(R.string.main_tab_tv_shows); // Use string resources
                    break;
                case 2:
                    tab.setText(R.string.main_tab_arabic); // Use string resources
                    break;
            }
        }).attach();
        if (savedInstanceState == null) {
            AdView mAdView = findViewById(R.id.adView);
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_search) {
            startActivity(new Intent(this, SearchActivity.class));
            return true;
        } else if (id == R.id.action_favorites) {
            startActivity(new Intent(this, FavoritesActivity.class));
            return true;
        } else if (id == R.id.action_watchlist) {
            startActivity(new Intent(this, WatchlistActivity.class));
            return true;
        } else if (id == R.id.action_settings) {
            // Navigate to the new SettingsActivity
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("MissingSuperCall")
    // في MainActivity.java

    @Override
    public void onBackPressed() {
        // إنشاء الديالوج المخصص
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_exit, null);
        builder.setView(view);

        // إنشاء الديالوج وتخلي خلفيته شفافة عشان الكيرفات تبان
        androidx.appcompat.app.AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        // ربط الأزرار
        view.findViewById(R.id.btnStay).setOnClickListener(v -> dialog.dismiss());
        view.findViewById(R.id.btnExit).setOnClickListener(v -> {
            dialog.dismiss();
            super.onBackPressed(); // الخروج الفعلي
        });

        dialog.show();
    }

    // --- Adapter for ViewPager ---
    private static class MainPagerAdapter extends FragmentStateAdapter {
        public MainPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return new HomeModernFragment();
                case 1:
                    return new TvShowsContainerFragment();
                case 2:
                    return new ArabicContainerFragment();
                default:
                    return new MoviesContainerFragment();
            }
        }

        @Override
        public int getItemCount() {
            return 3;
        }
    }
}