package com.eslamdev.mawjaz.api;

public class ContentItem {
    private final int id;
    private final String title;
    private final double voteAverage;
    private final String overview;
    private final String posterPath;
    private final String releaseDate;
    private final String type;
    private final String originalLanguage;

    public ContentItem(int id, String title, double voteAverage, String overview, String posterPath, String releaseDate, String type, String originalLanguage) {
        this.id = id;
        this.title = title;
        this.voteAverage = voteAverage;
        this.overview = overview;
        this.posterPath = posterPath;
        this.releaseDate = releaseDate;
        this.type = type;
        this.originalLanguage = originalLanguage;
    }


    public int getId() { return id; }
    public String getTitle() { return title; }
    public double getVoteAverage() { return voteAverage; }
    public String getOverview() { return overview; }
    public String getPosterPath() { return posterPath; }
    public String getReleaseDate() { return releaseDate; }
    public String getType() { return type; }

    public String getOriginalLanguage() { return originalLanguage; }

    public static ContentItem fromMovie(Movie movie) {
        return new ContentItem(
                movie.getId(),
                movie.getTitle(),
                movie.getVoteAverage(),
                movie.getOverview(),
                movie.getPosterPath(),
                movie.getReleaseDate(),
                "movie",
                movie.getOriginalLanguage()
        );
    }

    public static ContentItem fromTvShow(TvShow tvShow) {
        return new ContentItem(
                tvShow.getId(),
                tvShow.getName(),
                tvShow.getVoteAverage(),
                tvShow.getOverview(),
                tvShow.getPosterPath(),
                tvShow.getFirstAirDate(),
                "tv",
                tvShow.getOriginalLanguage()
        );
    }


    public static ContentItem fromTrendingItem(TrendingItem item) {
        if (item.getMediaType() == null) return null;

        if ("movie".equals(item.getMediaType())) {
            return new ContentItem(
                    item.getId(),
                    item.getTitle(),
                    item.getVoteAverage(),
                    item.getOverview(),
                    item.getPosterPath(),
                    item.getReleaseDate(),
                    "movie",
                    item.getOriginalLanguage()
            );
        } else if ("tv".equals(item.getMediaType())) {
            return new ContentItem(
                    item.getId(),
                    item.getName(),
                    item.getVoteAverage(),
                    item.getOverview(),
                    item.getPosterPath(),
                    item.getFirstAirDate(),
                    "tv",
                    item.getOriginalLanguage()
            );
        }
        return null;
    }
}