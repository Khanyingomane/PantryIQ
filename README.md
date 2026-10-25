Pantry IQ

**Your pantry, your possibility.**

Pantry IQ is an Android application designed to help users manage their pantry ingredients and discover recipes based on what they already have available. The application aims to reduce food waste, simplify meal planning, and make better use of ingredients at home.

## Project Overview

Pantry IQ allows users to maintain a digital record of their pantry ingredients and receive recipe suggestions based on ingredient availability and required quantities.

Unlike applications that recommend recipes requiring additional shopping, Pantry IQ follows a strict matching approach: a recipe is suggested only when the user has all the required ingredients in the necessary quantities.

## Features

- **Pantry Management:** Add, view, update, and delete pantry ingredients.
- **Recipe Suggestions:** Discover recipes that match the ingredients and quantities available in the pantry.
- **Strict Recipe Matching:** Recipes requiring missing ingredients or insufficient quantities are excluded from suggestions.
- **Recipe Details:** View information about suggested recipes and their required ingredients.
- **Local Data Storage:** Store pantry information locally using SQLite.
- **Built-in Recipes:** Includes 20 predefined recipes for recipe discovery.
- **User-Friendly Interface:** Navigate pantry ingredients and recipe suggestions through a simple Android interface.

## Technologies Used

- **Programming Language:** Java
- **Development Environment:** Android Studio
- **User Interface:** XML layouts and Android native views
- **Database:** SQLite
- **Android Components:** Activities, RecyclerView, and CardView
- **Application Package:** `com.example.pantryiq`

## Design Identity

Pantry IQ uses a warm, modern visual identity designed to make pantry management feel simple and approachable.

| **Colour**      | **Hex Code**  | **Purpose**        |
| Rice Paper      | `#FFF8F0`     | Primary background |
| Smoked Plum     | `#59445F`     | Main brand colour  |
| Dragon Fruit    | `#F05283`     | Accent colour      |

**Slogan:** Your pantry, your possibility.

## How It Works

1. **Manage ingredients:** Add ingredients to your pantry and maintain their quantities and units.
2. **Access recipe suggestions:** The application compares your pantry inventory with the requirements of its built-in recipes.
3. **Match ingredients and quantities:** Each recipe is evaluated against the ingredients available in the pantry.
4. **View matching recipes:** Recipes are displayed only when all required ingredients and quantities are available.
5. **Update your pantry:** When ingredients or quantities change, the available recipe matches can change accordingly.

## Recipe-Matching Logic

Pantry IQ uses a strict recipe-matching approach rather than recommending recipes based on partial ingredient availability.

For a recipe to qualify:

- Every required ingredient must be available.
- The available quantity must meet or exceed the required quantity.
- Ingredient units must be compatible with the application's supported matching logic.

If a required ingredient is missing or its quantity is insufficient, the recipe should not appear as a match.

## Database

Pantry IQ uses SQLite for local data storage. This allows pantry information to be stored on the device without requiring a remote database server.

The database supports the application's pantry-management functionality and helps retain stored information between application sessions.

## Getting Started

### Prerequisites

- Android Studio
- Android SDK compatible with the project configuration
- Java development support through Android Studio

### Installation

1. Clone this repository:

   ```bash
   https://github.com/Khanyingomane/PantryIQ.git
   ```

2. Open Android Studio.

3. Select **Open** and choose the cloned project folder.

4. Allow Gradle to sync and resolve the project dependencies.

5. Connect an Android device with USB debugging enabled or start an Android emulator.

6. Run the application using Android Studio.

**Note:** Replace `<YOUR_GITHUB_REPOSITORY_URL>` with the actual URL of this repository.

Project Structure

The application follows a Java-based Android project structure.


PantryIQ/
├── app/
│   └── src/
│       └── main/
│           ├── java/
│           │   └── com/example/pantryiq/
│           │       ├── Activities and application logic
│           │       ├── Database helper
│           │       └── Recipe-matching logic
│           ├── res/
│           │   ├── layout/
│           │   ├── drawable/
│           │   └── values/
│           └── AndroidManifest.xml
├── build.gradle
├── settings.gradle
└── README.md


*The exact files and folders may vary depending on the current project structure.*

## Project Objectives

Pantry IQ was developed to:

- Improve household pantry organisation.
- Help users make informed decisions about available ingredients.
- Reduce unnecessary food waste.
- Simplify recipe discovery.
- Demonstrate Android application development using Java, XML layouts, and SQLite.
- Apply conditional matching logic to a practical everyday problem.

## Future Improvements

Potential improvements include:

- Additional recipes and recipe categories.
- More flexible unit conversions.
- Ingredient expiry-date tracking and notifications.
- Recipe search and filtering.
- Improved meal-planning functionality.
- Pantry usage statistics and food-waste insights.
- Optional backup and synchronisation features.

## Author

Developed by Sherine Ngomane as an Android application project using Java and Android Studio.

## License

No licence has been specified for this repository. All rights and permissions remain with the copyright holder unless a licence is added.



Pantry IQ — Your pantry, your possibility.
