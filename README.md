# 🎬 Mawjaz - Movies & TV Shows Guide

**Mawjaz** is a comprehensive Android application designed to provide users with an immersive experience in discovering movies and TV shows. It leverages the **TMDB API** to fetch real-time data, offering features like trending lists, detailed information, search functionality, and local favorites management.

The app follows modern Android development practices, utilizing **MVVM Architecture** and **Material Design 3** components to ensure a smooth and visually appealing user experience.

---

## 📱 Screenshots 

<p align="center">
  <img src="https://github.com/Eslamal/ListMovies/blob/master/mawjazApp/photo_2026-01-11_01-15-34.jpg?raw=true" width="200" />
  <img src="https://github.com/Eslamal/ListMovies/blob/master/mawjazApp/photo_2026-01-11_01-15-36.jpg?raw=true" width="200" />
  <img src="https://github.com/Eslamal/ListMovies/blob/master/mawjazApp/photo_2026-01-11_01-15-37.jpg?raw=true" width="200" />
  <img src="https://github.com/Eslamal/ListMovies/blob/master/mawjazApp/photo_2026-01-11_01-15-38.jpg?raw=true" width="200" />
  <img src="https://github.com/Eslamal/ListMovies/blob/master/mawjazApp/photo_2026-01-11_01-15-39.jpg?raw=true" width="200" />
  <img src="https://github.com/Eslamal/ListMovies/blob/master/mawjazApp/photo_2026-01-11_01-15-40.jpg?raw=true" width="200" />
  <img src="https://github.com/Eslamal/ListMovies/blob/master/mawjazApp/photo_2026-01-11_01-15-41.jpg?raw=true" width="200" />
  <img src="https://github.com/Eslamal/ListMovies/blob/master/mawjazApp/photo_2026-01-11_01-15-42.jpg?raw=true" width="200" />
  <img src="https://github.com/Eslamal/ListMovies/blob/master/mawjazApp/photo_2026-01-11_01-15-43.jpg?raw=true" width="200" />
  <img src="https://github.com/Eslamal/ListMovies/blob/master/mawjazApp/photo_2026-01-11_01-15-44.jpg?raw=true" width="200" />
  <img src="https://github.com/Eslamal/ListMovies/blob/master/mawjazApp/photo_2026-01-11_01-15-45.jpg?raw=true" width="200" />
  <img src="https://github.com/Eslamal/ListMovies/blob/master/mawjazApp/photo_2026-01-11_01-15-46.jpg?raw=true" width="200" />
  <img src="https://github.com/Eslamal/ListMovies/blob/master/mawjazApp/photo_2026-01-11_01-15-47.jpg?raw=true" width="200" />
  <img src="https://github.com/Eslamal/ListMovies/blob/master/mawjazApp/photo_2026-01-11_01-15-49.jpg?raw=true" width="200" />
</p>

---

## ✨ Features 

* **Discover:** Browse Trending, Popular, and Top Rated movies/TV shows.
* **Search:** Powerful search functionality with history tracking and genre filters (Chips).
* **Details:** Deep dive into content with cast members, trailers (WebView/YouTube), and ratings.
* **Local Storage:** Save your favorite content to **Favorites** or **Watchlist** to access them offline.
* **Localization:** Full support for English and Arabic languages (RTL support).
* **UI/UX:** Modern Material 3 design, Shimmer loading effects, and smooth transitions.
* **Security:** API Keys are secured using `local.properties` and `BuildConfig`.

---

## 🛠️ Tech Stack & Architecture 

This project is built using **Java** and follows the **MVVM (Model-View-ViewModel)** architectural pattern to ensure separation of concerns and testability.

### 📚 Libraries & Technologies Used

| Technology | Purpose (Why?) | Where is it used? |
| :--- | :--- | :--- |
| **Java** | The primary programming language used for building the app logic. | Entire Project. |
| **MVVM** | To separate UI logic from business logic, making the code maintainable. | `ViewModel` classes (e.g., `DetailViewModel`, `SearchViewModel`) communicating with `Activities`. |
| **Retrofit 2** | A type-safe HTTP client for Android. Used for its efficiency and ease of use with APIs. | Fetching data from TMDB API in `MovieRepository`. |
| **Gson** | To serialize and deserialize JSON data returned from the API. | Converting API responses into Java POJO classes (`MovieResponse`, `ActorDetails`). |
| **Room Database** | An abstraction layer over SQLite. Used for robust offline data persistence. | Storing `Favorites` and `Watchlist` items locally on the device. |
| **Picasso** | Image loading library. Lightweight and handles caching automatically. | Loading Movie Posters, Cast images, and Backdrops. |
| **Material Design 3** | Google's latest design system for building beautiful, usable interfaces. | Buttons, Cards, SearchBar, Chips, BottomNavigation, and Colors. |
| **Shimmer Effect** | To show a loading skeleton animation instead of a boring spinner. | Used in `HomeFragment` and `SearchActivity` while data is fetching. |
| **Android Architecture Components** | `LiveData` and `ViewModel` to handle lifecycle-aware data data. | Observing data changes in UI without memory leaks. |
| **AdMob** | For monetization. | Displaying Interstitial and Banner ads. |

---

## 🏗️ Project Structure 

```text
com.eslamdev.mawjaz
├── adapter         # RecyclerView Adapters (MovieAdapter, CastAdapter...)
├── api             # Retrofit Interface & API Models
├── database        # Room Entities, DAOs, ViewModels, and Repository
├── util            # Helper classes (LocaleHelper, Constants)
└── view            # Activities and Fragments (UI Layer)
