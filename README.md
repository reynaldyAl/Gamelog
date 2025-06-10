<div align="center">
  <img src="https://raw.githubusercontent.com/reynaldyal/gamemology/main/app/src/main/res/drawable/gamemology_banner.png" alt="Gamemology Banner" width="800"/>
  <h1>Gamemology</h1>
  <p><i>Discover, Track, and Master Your Game Collection</i></p>
  
  <p>
    <img src="https://img.shields.io/badge/Platform-Android-3DDC84?style=flat-square&logo=android" alt="Platform: Android"/>
    <img src="https://img.shields.io/badge/API-24%2B-brightgreen?style=flat-square" alt="API Level: 24+"/>
    <img src="https://img.shields.io/badge/Version-1.0.0-blue?style=flat-square" alt="Version: 1.0.0"/>
    <img src="https://img.shields.io/badge/UI-Material%20Design%203-6750A4?style=flat-square&logo=material-design" alt="UI: Material Design 3"/>
    <img src="https://img.shields.io/badge/License-Apache%202.0-lightgrey?style=flat-square" alt="License: Apache 2.0"/>
  </p>
</div>

## 📱 Overview

*Gamemology* is a comprehensive Android application for discovering, tracking, and managing your video game collection. The app provides detailed information about games across all major platforms, personalized recommendations, and powerful tracking tools to manage your gaming journey.

Our modern Material Design 3 UI ensures a seamless, visually appealing experience while powerful backend features keep your gaming data organized across devices.

## ✨ Key Features

<table>
  <tr>
    <td width="50%">
      <h3>🔍 Game Discovery</h3>
      <ul>
        <li>Browse trending and popular games</li>
        <li>Filter by platforms, genres, stores, platforms, release date</li>
        <li>Advanced search by Newest, Highest Rated, A-Z Z-A </li>
        <li>Advanced search with multiple criteria</li>
      </ul>
    </td>
    <td width="50%">
      <h3>📊 Game Information</h3>
      <ul>
        <li>Detailed game pages with descriptions and ratings</li>
        <li>Screenshots, videos, and trailer previews</li>
        <li>DLC and expansion information</li>
        <li>Achievement/trophy data</li>
      </ul>
    </td>
  </tr>
  <tr>
    <td width="50%">
      <h3>📚 User Collection</h3>
      <ul>
        <li>Mark games as favorites with animated toggle</li>
        <li>Favorite for you!</li>
      </ul>
    </td>
    <td width="50%">
      <h3>👤 Profile Management</h3>
      <ul>
        <li>Customizable user profiles with profile pictures</li>
        <li>Collection insights</li>
        <li>Seamless sync across devices</li>
      </ul>
    </td>
  </tr>
  <tr>
    <td width="50%">
      <h3>🤖 AI Game Assistant</h3>
      <ul>
        <li>Intelligent game recommendations</li>
        <li>Gaming tips and information</li>
        <li>Answers to gaming-related questions</li>
        <li>Personalized gaming advice</li>
      </ul>
    </td>
    <td width="50%">
      <h3>📱 Modern UI</h3>
      <ul>
        <li>Material Design 3 components and animations</li>
        <li>Dark/Light theme with dynamic color support</li>
        <li>Smooth transitions and intuitive navigation</li>
        <li>Responsive layouts for all screen sizes</li>
      </ul>
    </td>
  </tr>
</table>

## 🎨 UI Design Showcase

<div align="center">
<table>
  <tr>
    <td><img src="https://raw.githubusercontent.com/reynaldyal/gamemology/main/screenshots/home_screen.jpg" alt="Home Screen" width="200"/></td>
    <td><img src="https://raw.githubusercontent.com/reynaldyal/gamemology/main/screenshots/detail_screen.jpg" alt="Game Details" width="200"/></td>
    <td><img src="https://raw.githubusercontent.com/reynaldyal/gamemology/main/screenshots/profile_screen.jpg" alt="Profile Screen" width="200"/></td>
    <td><img src="https://raw.githubusercontent.com/reynaldyal/gamemology/main/screenshots/home_screen.jpg" alt="Home Screen" width="200"/></td>
    <td><img src="https://raw.githubusercontent.com/reynaldyal/gamemology/main/screenshots/detail_screen.jpg" alt="Game Details" width="200"/></td>
    <td><img src="https://raw.githubusercontent.com/reynaldyal/gamemology/main/screenshots/profile_screen.jpg" alt="Profile Screen" width="200"/></td>
  </tr>
  <tr>
    <td align="center"><b>Home Screen</b></td>
    <td align="center"><b>Game Details</b></td>
    <td align="center"><b>Profile Screen</b></td>
  </tr>
</table>
</div>

## 🏗 UI Enhancement Guide

Gamemology features a premium, modern UI built with Material Design 3 principles. Our UI enhancement follows these key principles:

### Design System

- *Color Theme*: Deep blue-purple (#5D3FD3), cyan (#00E5FF), amber (#FFC107)
- *Dark Mode*: Optimized dark gradient backgrounds (#121212 → #1E1E1E)
- *Typography*: Consistent hierarchical type system
- *Shape System*: Rounded corners and elevated surfaces
- *Animation*: Smooth transitions and micro-interactions

### Component Examples

<details>
<summary><b>Game Card Component</b></summary>

xml
<com.google.android.material.card.MaterialCardView
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_margin="8dp"
    app:cardCornerRadius="12dp"
    app:cardElevation="4dp"
    app:strokeWidth="0dp">

    <androidx.constraintlayout.widget.ConstraintLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content">

        <ImageView
            android:id="@+id/img_game"
            android:layout_width="0dp"
            android:layout_height="0dp"
            android:scaleType="centerCrop"
            app:layout_constraintDimensionRatio="16:9"
            app:layout_constraintTop_toTopOf="parent"
            app:layout_constraintStart_toStartOf="parent"
            app:layout_constraintEnd_toEndOf="parent" />

        <View
            android:id="@+id/gradient_overlay"
            android:layout_width="0dp"
            android:layout_height="0dp"
            android:background="@drawable/gradient_bottom"
            app:layout_constraintBottom_toBottomOf="@id/img_game"
            app:layout_constraintEnd_toEndOf="@id/img_game"
            app:layout_constraintStart_toStartOf="@id/img_game"
            app:layout_constraintHeight_percent="0.5"
            app:layout_constraintVertical_bias="1.0" />

        <TextView
            android:id="@+id/tv_game_name"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_margin="12dp"
            android:textColor="@color/white"
            android:textSize="16sp"
            android:textStyle="bold"
            app:layout_constraintBottom_toBottomOf="@id/img_game"
            app:layout_constraintEnd_toEndOf="parent"
            app:layout_constraintStart_toStartOf="parent" />
    </androidx.constraintlayout.widget.ConstraintLayout>
</com.google.android.material.card.MaterialCardView>

</details>

<details>
<summary><b>Material Button Example</b></summary>

xml
<com.google.android.material.button.MaterialButton
    android:id="@+id/btnViewFavorite"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:text="@string/view_my_favorites"
    android:paddingVertical="13dp"
    style="@style/Widget.Material3.Button.OutlinedButton"
    app:icon="@drawable/ic_favorite"
    app:iconGravity="textStart"
    app:iconTint="?attr/colorPrimary"
    app:strokeColor="?attr/colorPrimary"
    android:layout_marginBottom="14dp" />

</details>

## 🔧 Technical Architecture

<details>
<summary><b>Project Structure Overview</b></summary>

Gamemology follows the MVVM (Model-View-ViewModel) architecture pattern with Repository for data handling:


├── ui/              # User interface components (Activities, Fragments)
├── viewmodels/      # ViewModels for managing UI-related data
├── models/          # Data models and entities
├── repository/      # Data source management and business logic
├── database/        # Local database implementation (SQLite/Room)
├── api/             # Network API services and clients
├── utils/           # Utility classes and helper functions
└── di/              # Dependency injection configuration

</details>

### 🗄 Data Storage

Gamemology uses a multi-layered data storage approach for optimal performance:

- *SQLite Database*: Core game data, user collection, and preferences are stored in a local SQLite database via Room ORM.
- *In-Memory Cache*: Frequently accessed data is kept in memory for fast access.
- *Network Cache*: API responses are cached to reduce network requests and provide offline functionality.
- *Shared Preferences*: User settings and app configuration.

<details>
<summary><b>Database Implementation</b></summary>

java
@Database(entities = {Game.class, UserCollection.class, GameDetails.class}, version = 1)
public abstract class GamemologyDatabase extends RoomDatabase {
    public abstract GameDao gameDao();
    public abstract UserCollectionDao userCollectionDao();
    public abstract GameDetailsDao gameDetailsDao();
    
    // Database instance creation and migrations
}

</details>

### 🔄 Data Flow

1. *Repository Layer*: Acts as a single source of truth
2. *Memory Cache*: First level cache for instant access
3. *Local Database*: Persistent storage for offline access
4. *Network Layer*: Fetches data from RAWG API and Gemini API

## 📲 Installation

### Requirements

- Android 7.0 (API level 24) or higher
- Internet connection for data synchronization
- 100MB of free space

### Download

The app will be available on Google Play Store soon. For now, you can:

1. Clone the repository:
   bash
   git clone https://github.com/reynaldyal/gamemology.git
   

2. Open the project in Android Studio

3. Build and run on your device or emulator

## 🔑 API Setup

### RAWG API Setup

1. Register for an API key at [RAWG API Portal](https://rawg.io/apidocs)
2. Once registered, copy your API key

### Google Gemini API Setup

1. Visit [Google AI Studio](https://ai.google.dev/) and create an account
2. Navigate to the API section and create a new API key
3. Copy your Gemini API key

### Add Keys to Project

Create a secrets.properties file in the project root with:

properties
RAWG_API_KEY=your_rawg_api_key_here
GEMINI_API_KEY=your_gemini_api_key_here


## 📚 Dependencies

- *AndroidX Core & AppCompat*: Core Android functionality
- *Material Components*: Material Design 3 UI components
- *Glide*: Image loading and caching
- *Retrofit*: HTTP client for API communication
- *Room*: SQLite database abstraction layer
- *LiveData & ViewModel*: Lifecycle-aware data holders
- *Gemini API SDK*: Google AI integration
- *ImagePicker*: Image selection and capture library

## 📋 UI Structure

The UI enhancement focuses on these key files:

<details>
<summary><b>Priority UI Components</b></summary>

### 1. Core Components - Priority: High
- layout/item_game.xml - Card layout for game display
- layout/activity_main.xml - Main application layout
- fragment_home.xml - Home screen layout
- values/themes.xml - App theme definitions

### 2. Navigation - Priority: High
- menu/bottom_navigation_menu.xml - Bottom navigation

### 3. Detail Views - Priority: Medium
- layout/activity_detail.xml - Game detail screen
- fragment_about.xml - Information tab
- fragment_media.xml - Media tab

### 4. User Interface - Priority: Medium
- fragment_profile.xml - User profile screen
- layout/item_category.xml - Category items

### 5. Search & Discovery - Priority: High
- layout/activity_search.xml - Search interface
- fragment_filter_bottom_sheet.xml - Filter options
</details>

## 📜 License


Copyright (c) 2025 Gamemology

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.


## 🙏 Acknowledgments

- Game data provided by [RAWG Video Games Database](https://rawg.io/)
- AI assistance powered by [Google Gemini API](https://ai.google.dev/)
- Icon assets from [Material Design Icons](https://fonts.google.com/icons)

## 👥 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## 📞 Contact

- *Developer*: Reynaldy AL
- *Institution*: Information System 2023 - Hasanuddin University
- *GitHub*: [Reynaldy AL](https://github.com/reynaldyAl)
