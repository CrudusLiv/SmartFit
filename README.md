# 🏋️ SmartFit - Your Personal Fitness Companion

<div align="center">

![SmartFit Logo](https://img.shields.io/badge/SmartFit-Fitness%20Tracking-blueviolet?style=for-the-badge)
[![Android](https://img.shields.io/badge/Android-28%2B-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://www.android.com/)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![License](https://img.shields.io/badge/License-MIT-green?style=for-the-badge)](LICENSE)

**Track. Analyze. Achieve.**

A modern Android fitness tracking application built with Jetpack Compose that seamlessly integrates with Google Fit to help you achieve your health and fitness goals.

[Features](#-features) • [Screenshots](#-screenshots) • [Installation](#-installation) • [Tech Stack](#-tech-stack) • [Testing](#-testing) • [Contributing](#-contributing)

</div>

---

## ✨ Features

### 🎯 Core Functionality
- **📊 Real-time Activity Tracking**: Monitor steps, calories, distance, and workout duration
- **🔄 Google Fit Integration**: Seamlessly sync your fitness data with Google Fit
- **💪 Exercise Library**: Access 700+ exercises from the Wger API with detailed instructions
- **📈 Progress Analytics**: Visualize your weekly and monthly fitness trends
- **🎨 Beautiful UI**: Modern Material Design 3 interface with smooth animations
- **🌙 Dark Theme**: Elegant night mode for comfortable viewing

### 🏃 Activity Management
- **Add Custom Activities**: Manually log workouts, runs, and fitness activities
- **Edit & Delete**: Full CRUD operations for activity management
- **Activity Filtering**: Sort and filter activities by type, date, or intensity
- **Smart Categorization**: Automatic classification of different activity types

### 📊 Statistics & Insights
- **Daily Dashboard**: View today's progress at a glance
- **Weekly Summary**: Track your 7-day performance trends
- **Calorie Calculator**: MET-based accurate calorie burn calculations
- **BMI Tracker**: Monitor your Body Mass Index over time
- **Goal Progress**: Set and track step goals with visual indicators

### 👤 Profile Management
- **User Profiles**: Create and manage multiple user profiles
- **Personal Stats**: Track weight, height, and fitness goals
- **Google Fit Sync**: One-click synchronization with Google Fit
- **Demo Mode**: Try the app without Google account

### 🎯 Workout Suggestions
- **Personalized Recommendations**: AI-powered workout suggestions based on your activity
- **Exercise Details**: View muscle groups, equipment needed, and intensity levels
- **Category Filters**: Browse by cardio, strength, mobility, or sports
- **Search Functionality**: Find specific exercises quickly

---

## 📱 Screenshots

<div align="center">

| Home Dashboard | Activity Log | Exercise Library | Profile |
|:---:|:---:|:---:|:---:|
| ![Home](https://via.placeholder.com/200x400/667eea/ffffff?text=Home) | ![Activities](https://via.placeholder.com/200x400/764ba2/ffffff?text=Activities) | ![Exercises](https://via.placeholder.com/200x400/667eea/ffffff?text=Exercises) | ![Profile](https://via.placeholder.com/200x400/764ba2/ffffff?text=Profile) |

</div>

---

## 🚀 Installation

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or later
- Android SDK 28 (Android 9.0) or higher
- JDK 11 or higher
- Gradle 8.9+

### Setup Instructions

1. **Clone the repository**
   ```bash
   git clone https://github.com/CrudusLiv/SmartFit.git
   cd SmartFit
   ```

2. **Configure API Keys**
   
   Create a `local.properties` file in the root directory:
   ```properties
   # Wger API Token (optional - app works without it)
   wger.token=YOUR_WGER_API_TOKEN_HERE
   ```

3. **Google Fit Setup** (Optional)
   
   To enable Google Fit integration:
   - Go to [Google Cloud Console](https://console.cloud.google.com/)
   - Create a new project
   - Enable Fitness API
   - Create OAuth 2.0 credentials
   - Add SHA-1 fingerprint of your signing key

4. **Build the project**
   ```bash
   ./gradlew build
   ```

5. **Run on device/emulator**
   ```bash
   ./gradlew installDebug
   ```

---

## 🛠️ Tech Stack

### **Core Technologies**
| Technology | Version | Purpose |
|------------|---------|---------|
| ![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-7F52FF?logo=kotlin) | 2.0.21 | Primary programming language |
| ![Android](https://img.shields.io/badge/Android%20SDK-28+-3DDC84?logo=android) | 28+ (Android 9.0+) | Platform |
| ![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-1.6.0-4285F4?logo=jetpackcompose) | 1.6.0 | Modern UI framework |
| ![Material Design 3](https://img.shields.io/badge/Material%203-Latest-757575?logo=material-design) | Latest | Design system |

### **Architecture & Libraries**
- **Architecture**: MVVM (Model-View-ViewModel)
- **Dependency Injection**: Manual DI with ViewModelProvider
- **Database**: Room (2.6.1) - Local SQLite database
- **Networking**: Retrofit (2.11.0) + OkHttp (4.12.0)
- **Async**: Kotlin Coroutines (1.9.0) + Flow
- **Image Loading**: Coil (2.7.0)
- **Data Storage**: DataStore Preferences (1.1.1)

### **Google Services**
- Google Fit API (21.1.0) - Fitness data synchronization
- Google Sign-In (21.2.0) - Authentication

### **Testing**
- **Unit Tests**: JUnit 4 (4.13.2) - 65 tests, 100% pass rate
- **UI Tests**: Compose Testing (1.6.0) - 59 comprehensive tests
- **Test Runner**: AndroidJUnit (1.2.1)
- **Assertions**: Espresso (3.6.1)

---

## 🧪 Testing

SmartFit includes comprehensive test coverage:

### Run All Tests
```bash
# PowerShell (Recommended)
.\run-tests.ps1

# Or using Gradle directly
.\gradlew.bat :app:testDebugUnitTest
```

### Test Coverage
- ✅ **65 Unit Tests** - 100% pass rate
  - 33 tests: ActivityRepository (calorie calculations, progress tracking)
  - 26 tests: ViewModel business logic
  - 5 tests: Data models
  - 1 test: Sanity check

- ✅ **59 UI Tests** (require emulator/device)
  - 22 tests: Home screen interactions
  - 16 tests: Navigation flows
  - 21 tests: Data display validation

### View Test Reports
- **Terminal**: Color-coded results with statistics
- **HTML Report**: Open `test-report.html` in browser for detailed analysis
- **XML Reports**: Located in `app/build/test-results/testDebugUnitTest/`

For detailed testing documentation, see [TESTING_GUIDE.md](TESTING_GUIDE.md)

---

## 📂 Project Structure

```
SmartFit/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/smartfit/
│   │   │   │   ├── data/
│   │   │   │   │   ├── datastore/      # User preferences storage
│   │   │   │   │   ├── local/          # Room database entities & DAOs
│   │   │   │   │   ├── model/          # Data models
│   │   │   │   │   ├── remote/         # API services & DTOs
│   │   │   │   │   └── repository/     # Data layer abstraction
│   │   │   │   ├── google/             # Google Fit integration
│   │   │   │   ├── ui/
│   │   │   │   │   ├── navigation/     # Navigation graph
│   │   │   │   │   ├── screens/        # Composable screens
│   │   │   │   │   └── theme/          # Material Design theme
│   │   │   │   ├── viewmodel/          # ViewModels
│   │   │   │   └── MainActivity.kt     # Main entry point
│   │   │   └── res/                     # Resources
│   │   ├── test/                        # Unit tests
│   │   └── androidTest/                 # Instrumented tests
│   └── build.gradle.kts                 # App-level Gradle config
├── gradle/                              # Gradle wrapper
├── README.md                            # This file
├── TESTING_GUIDE.md                     # Detailed testing documentation
├── run-tests.ps1                        # Enhanced test runner
└── test-report.html                     # Visual test report
```

---

## 🎯 Key Features Explained

### Calorie Calculation
SmartFit uses the scientifically accurate **MET (Metabolic Equivalent of Task)** formula:

```
Calories Burned = MET × Weight (kg) × Duration (hours)
```

**MET Values by Activity:**
- Walking: 3.5
- Running: 8.0
- Cycling: 6.0
- Swimming: 7.0
- Yoga: 2.5
- Strength Training: 5.0

### Google Fit Integration
- Real-time step counting
- Activity segment tracking
- Heart rate monitoring
- Distance and speed tracking
- Automatic workout detection

### Data Persistence
- **Room Database**: Local storage for offline access
- **DataStore**: User preferences and settings
- **Automatic Sync**: Background synchronization with Google Fit

---

## 🔧 Configuration

### Build Variants
- **Debug**: Development build with logging
- **Release**: Production-ready optimized build

### Gradle Properties
```properties
# Android
compileSdk=35
minSdk=28
targetSdk=35

# Kotlin
kotlin.version=2.0.21

# Build
jvmTarget=11
```

---

## 📊 Performance

- **App Size**: ~15 MB (release APK)
- **Min SDK**: Android 9.0 (API 28)
- **Target SDK**: Android 14 (API 35)
- **Startup Time**: <2 seconds on average devices
- **Memory Usage**: ~80-120 MB during normal operation

---

## 🤝 Contributing

We welcome contributions! Please follow these steps:

1. **Fork the repository**
2. **Create a feature branch**
   ```bash
   git checkout -b feature/AmazingFeature
   ```
3. **Commit your changes**
   ```bash
   git commit -m 'Add some AmazingFeature'
   ```
4. **Push to the branch**
   ```bash
   git push origin feature/AmazingFeature
   ```
5. **Open a Pull Request**

### Development Guidelines
- Follow [Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Write unit tests for new features
- Update documentation as needed
- Use meaningful commit messages

---

## 🐛 Known Issues & Roadmap

### Known Issues
- Google Fit sync may require manual permission grant on some devices
- Exercise descriptions might be missing for some Wger API entries

### Roadmap
- [ ] Add nutrition tracking
- [ ] Implement social features (friend challenges)
- [ ] Add custom workout plans
- [ ] Integrate with wearable devices
- [ ] Add offline mode with full functionality
- [ ] Implement data export (CSV, PDF)
- [ ] Add multilingual support

---

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 👥 Authors

**SmartFit Development Team**
- GitHub: [@CrudusLiv](https://github.com/CrudusLiv)
- Repository: [SmartFit](https://github.com/CrudusLiv/SmartFit)

---

## 🙏 Acknowledgments

- [Wger Exercise API](https://wger.de/) for the comprehensive exercise database
- [Google Fit API](https://developers.google.com/fit) for fitness data integration
- [Jetpack Compose](https://developer.android.com/jetpack/compose) for the modern UI framework
- [Material Design 3](https://m3.material.io/) for design guidelines
- All contributors who have helped improve this project

---

## 📞 Support

- **Issues**: [GitHub Issues](https://github.com/CrudusLiv/SmartFit/issues)
- **Discussions**: [GitHub Discussions](https://github.com/CrudusLiv/SmartFit/discussions)
- **Email**: support@smartfit.example.com

---

## 🌟 Star History

If you find SmartFit helpful, please consider giving it a star ⭐

---

<div align="center">

**Made with ❤️ and 💪 by the SmartFit Team**

[![GitHub followers](https://img.shields.io/github/followers/CrudusLiv?style=social)](https://github.com/CrudusLiv)
[![GitHub stars](https://img.shields.io/github/stars/CrudusLiv/SmartFit?style=social)](https://github.com/CrudusLiv/SmartFit/stargazers)

</div>
