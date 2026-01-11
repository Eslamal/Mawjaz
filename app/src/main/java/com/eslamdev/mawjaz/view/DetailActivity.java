package com.eslamdev.mawjaz.view;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.eslamdev.mawjaz.BuildConfig;
import com.eslamdev.mawjaz.R;
import com.eslamdev.mawjaz.adapter.CastAdapter;
import com.eslamdev.mawjaz.adapter.WatchProviderAdapter;
import com.eslamdev.mawjaz.api.ActorDetails;
import com.eslamdev.mawjaz.database.DetailViewModel;
import com.eslamdev.mawjaz.database.DetailViewModelFactory;
import com.eslamdev.mawjaz.database.FavoriteMovieEntity;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

public class DetailActivity extends BaseActivity implements CastAdapter.OnCastMemberClickListener, WatchProviderAdapter.OnProviderClickListener {

    private DetailViewModel viewModel;
    private InterstitialAd mInterstitialAd;

    private ImageView detailPoster;
    private TextView detailTitle, detailRating, detailOverview, detailYear;
    private FloatingActionButton fabShare;
    private MaterialButton btnPlayTrailer, btnFavorite, btnWatchlist;
    private RecyclerView rvCast;

    private CastAdapter castAdapter;
    private WatchProviderAdapter watchProviderAdapter;
    private FavoriteMovieEntity currentMovieEntity;
    private String watchProviderLink = null;
    private String trailerUrl = null;
    private AlertDialog actorDetailsDialog;
    private Boolean wasFavorite = null;
    private Boolean wasInWatchlist = null;
    private String originalLanguage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        supportPostponeEnterTransition();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        initializeViews();
        setupToolbar();
        setupRecyclerViews();
        getIntentData();
        setupViewModel();
        setupObservers();
        setupClickListeners();
        loadInterstitialAd();
    }

    private void initializeViews() {
        detailPoster = findViewById(R.id.detail_poster);
        detailTitle = findViewById(R.id.detail_title);
        detailRating = findViewById(R.id.detail_rating);
        detailOverview = findViewById(R.id.detail_overview);
        detailYear = findViewById(R.id.detail_year);

        fabShare = findViewById(R.id.fabShare);

        btnPlayTrailer = findViewById(R.id.btn_play_trailer);
        btnFavorite = findViewById(R.id.btn_favorite);
        btnWatchlist = findViewById(R.id.btn_watchlist);

        rvCast = findViewById(R.id.rv_cast);
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
    }

    private void setupRecyclerViews() {
        castAdapter = new CastAdapter(this);
        castAdapter.setOnCastMemberClickListener(this);
        rvCast.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvCast.setAdapter(castAdapter);

        watchProviderAdapter = new WatchProviderAdapter(this);
        watchProviderAdapter.setOnProviderClickListener(this);
    }

    private void getIntentData() {
        int movieId = getIntent().getIntExtra("id", -1);
        String movieTitle = getIntent().getStringExtra("title");
        String moviePosterPath = getIntent().getStringExtra("image_url");

        this.originalLanguage = getIntent().getStringExtra("original_language");

        currentMovieEntity = new FavoriteMovieEntity(movieId, movieTitle, 0, "", moviePosterPath, "");

        ViewCompat.setTransitionName(detailPoster, "poster_" + movieId);
        detailTitle.setText(movieTitle);

        if (moviePosterPath != null && !moviePosterPath.isEmpty()) {
            String fullUrl = moviePosterPath.startsWith("http") ? moviePosterPath : "https://image.tmdb.org/t/p/w780" + moviePosterPath;

            Picasso.get()
                    .load(fullUrl)
                    .noFade()
                    .into(detailPoster, new Callback() {
                        @Override
                        public void onSuccess() {
                            supportStartPostponedEnterTransition();
                        }
                        @Override
                        public void onError(Exception e) {
                            supportStartPostponedEnterTransition();
                        }
                    });
        } else {
            supportStartPostponedEnterTransition();
        }
    }

    private void setupViewModel() {
        int movieId = currentMovieEntity.getId();
        if (movieId == -1) {
            Toast.makeText(this, R.string.error_no_content, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        String apiKey = BuildConfig.TMDB_API_KEY;
        DetailViewModelFactory factory = new DetailViewModelFactory(getApplication(), movieId, apiKey, this.originalLanguage);
        viewModel = new ViewModelProvider(this, factory).get(DetailViewModel.class);
    }

    private void setupObservers() {
        viewModel.isFavorite.observe(this, isFavorite -> {
            if (isFavorite != null) {
                updateButtonState(btnFavorite, isFavorite, R.drawable.ic_favorite_filled, R.drawable.ic_favorite_border);
                if (wasFavorite != null && !wasFavorite.equals(isFavorite)) {
                    String message = (isFavorite ? getString(R.string.added_to_favorites) : getString(R.string.removed_from_favorites));
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                }
                wasFavorite = isFavorite;
            }
        });

        viewModel.movieDetails.observe(this, details -> {
            if (details != null) {
                detailOverview.setText(details.getOverview());
                detailRating.setText(String.format("%.1f", details.getVoteAverage()));
                detailTitle.setText(details.getTitle());

                String releaseDate = details.getReleaseDate();
                if (releaseDate != null && releaseDate.length() >= 4) {
                    detailYear.setText(releaseDate.substring(0, 4));
                } else {
                    detailYear.setText("");
                }

                if (details.getPosterPath() != null) {
                    Picasso.get().load("https://image.tmdb.org/t/p/w780" + details.getPosterPath()).into(detailPoster);
                }

                currentMovieEntity = new FavoriteMovieEntity(
                        details.getId(),
                        details.getTitle(),
                        details.getVoteAverage(),
                        details.getOverview(),
                        details.getPosterPath(),
                        details.getReleaseDate()
                );
            }
        });

        viewModel.isInWatchlist.observe(this, isInWatchlist -> {
            if (isInWatchlist != null) {
                updateButtonState(btnWatchlist, isInWatchlist, R.drawable.ic_bookmark, R.drawable.ic_bookmark);

                if (wasInWatchlist != null && !wasInWatchlist.equals(isInWatchlist)) {
                    Toast.makeText(this, isInWatchlist ? getString(R.string.added_to_watchlist) : getString(R.string.removed_from_watchlist), Toast.LENGTH_SHORT).show();
                }
                wasInWatchlist = isInWatchlist;
            }
        });

        viewModel.actorDetails.observe(this, actorDetails -> {
            if (actorDetails != null && actorDetailsDialog != null && actorDetailsDialog.isShowing()) {
                updateActorDetailsDialog(actorDetails);
            } else if (actorDetailsDialog != null && actorDetailsDialog.isShowing()) {
                actorDetailsDialog.dismiss();
                Toast.makeText(this, R.string.actor_details_failed, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.trailerUrl.observe(this, url -> {
            this.trailerUrl = url;
            btnPlayTrailer.setEnabled(url != null);
        });

        viewModel.movieCast.observe(this, cast -> {
            if (cast != null && !cast.isEmpty()) {
                castAdapter.setCastList(cast);
                rvCast.setVisibility(View.VISIBLE);
            } else {
                rvCast.setVisibility(View.GONE);
            }
        });

        viewModel.watchProviders.observe(this, providersResult -> {
            if (providersResult != null && providersResult.getProviders() != null && !providersResult.getProviders().isEmpty()) {
                watchProviderAdapter.setProviderList(providersResult.getProviders());
                this.watchProviderLink = providersResult.getLink();
            }
        });
    }

    private void setupClickListeners() {
        btnFavorite.setOnClickListener(v -> viewModel.toggleFavoriteStatus(currentMovieEntity));
        btnWatchlist.setOnClickListener(v -> viewModel.toggleWatchlistStatus(currentMovieEntity));

        btnPlayTrailer.setOnClickListener(v -> {
            if (mInterstitialAd != null) {
                mInterstitialAd.show(DetailActivity.this);
                mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                    @Override
                    public void onAdDismissedFullScreenContent() {
                        playTrailer();
                        loadInterstitialAd();
                    }
                    @Override
                    public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                        playTrailer();
                    }
                });
            } else {
                playTrailer();
            }
        });

        fabShare.setOnClickListener(v -> {
            if (currentMovieEntity != null) {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_movie_prefix) + currentMovieEntity.getTitle());
                startActivity(Intent.createChooser(shareIntent, getString(R.string.share_via)));
            }
        });
    }

    private void showTrailerInWebViewDialog(String url) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_webview_trailer, null);
        WebView webView = view.findViewById(R.id.webViewTrailer);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebChromeClient(new WebChromeClient());

        String videoId = "";
        if (url.contains("watch?v=")) {
            String[] parts = url.split("watch\\?v=");
            if (parts.length > 1) {
                videoId = parts[1].split("&")[0];
            }
        } else if (url.contains("youtu.be/")) {
            String[] parts = url.split("youtu.be/");
            if (parts.length > 1) {
                videoId = parts[1].split("\\?")[0];
            }
        }
        String htmlData = "<html><body style=\"margin:0;padding:0;background:black;\">" +
                "<iframe width=\"100%\" height=\"100%\" src=\"https://www.youtube.com/embed/" + videoId + "?autoplay=1\"" +
                " frameborder=\"0\" allow=\"autoplay; encrypted-media\" allowfullscreen></iframe>" +
                "</body></html>";

        if (!videoId.isEmpty()) {
            webView.loadData(htmlData, "text/html", "utf-8");
        } else {
            webView.setWebViewClient(new android.webkit.WebViewClient());
            webView.loadUrl(url);
        }

        builder.setView(view);
        AlertDialog dialog = builder.create();

        dialog.setOnDismissListener(dialogInterface -> {
            webView.stopLoading();
            webView.loadUrl("about:blank");
            webView.destroy();
        });

        dialog.show();
    }

    private void playTrailer() {
        if (trailerUrl != null) {
            showTrailerInWebViewDialog(trailerUrl);
        } else {
            Toast.makeText(this, R.string.trailer_not_available, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onCastMemberClick(int actorId) {
        showActorDetailsDialog();
        viewModel.fetchActorDetails(actorId);
    }

    @Override
    public void onProviderClick() {
        if (watchProviderLink != null) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(watchProviderLink));
            startActivity(intent);
        }
    }

    private void showActorDetailsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_actor_details, null);
        builder.setView(view);
        builder.setNegativeButton(R.string.dialog_close, (dialog, which) -> dialog.dismiss());
        actorDetailsDialog = builder.create();
        actorDetailsDialog.show();
    }

    private void updateActorDetailsDialog(ActorDetails details) {
        View dialogView = actorDetailsDialog.getWindow().getDecorView();
        ProgressBar progressBar = dialogView.findViewById(R.id.actor_details_progress);
        View contentLayout = dialogView.findViewById(R.id.actor_details_layout);
        progressBar.setVisibility(View.GONE);
        contentLayout.setVisibility(View.VISIBLE);

        ImageView actorImage = dialogView.findViewById(R.id.actor_dialog_image);
        TextView actorName = dialogView.findViewById(R.id.actor_dialog_name);
        TextView actorBirthday = dialogView.findViewById(R.id.actor_dialog_birthday);
        TextView actorBiography = dialogView.findViewById(R.id.actor_dialog_biography);

        actorName.setText(details.getName());
        String birthInfo = (details.getBirthday() != null ? getString(R.string.actor_birth_date) + details.getBirthday() : "") +
                (details.getPlaceOfBirth() != null ? getString(R.string.actor_birth_place) + details.getPlaceOfBirth() : "");
        actorBirthday.setText(birthInfo);
        actorBiography.setText(details.getBiography() != null && !details.getBiography().isEmpty() ? details.getBiography() : getString(R.string.actor_no_biography));

        if (details.getProfilePath() != null) {
            String imageUrl = "https://image.tmdb.org/t/p/h632" + details.getProfilePath();
            Picasso.get().load(imageUrl).into(actorImage);
        }
    }

    private void updateButtonState(MaterialButton button, boolean isActive, int activeIcon, int inactiveIcon) {
        button.setIconResource(isActive ? activeIcon : inactiveIcon);

        if (isActive) {
            button.setIconTint(ContextCompat.getColorStateList(this, R.color.colorPrimary));
            if (button.getId() == R.id.btn_favorite) {
                button.setIconTint(ContextCompat.getColorStateList(this, R.color.red));
                button.setStrokeColor(ContextCompat.getColorStateList(this, R.color.red));
            } else {
                button.setStrokeColor(ContextCompat.getColorStateList(this, R.color.colorPrimary));
            }
        } else {
            button.setIconTint(ContextCompat.getColorStateList(this, R.color.colorPrimary));
            button.setStrokeColor(ContextCompat.getColorStateList(this, R.color.colorPrimary));

            if (button.getId() == R.id.btn_watchlist) {
                button.setIconTint(ContextCompat.getColorStateList(this, android.R.color.darker_gray));
                button.setStrokeColor(ContextCompat.getColorStateList(this, android.R.color.darker_gray));
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadInterstitialAd() {
        AdRequest adRequest = new AdRequest.Builder().build();
        String adUnitId = "ca-app-pub-6321231117080513/2496825263";

        InterstitialAd.load(this, adUnitId, adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        mInterstitialAd = interstitialAd;
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        mInterstitialAd = null;
                    }
                });
    }
}