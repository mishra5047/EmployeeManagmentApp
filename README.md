<h3 align="center">Employee Management Application</h3>

---

## 📝 Table of Contents

- [App's Usage](#whatAppDoes)
- [Key Points](#keyPoints)
- [Dependencies](#dependencies)
- [Project Structure](#structure)
- [Technology Stack](#tech_stack)
- [Author](#authors)

## 🧐 What This App Does <a name = "whatAppDoes"></a>

"For a user, this is the things he / she can do"
- Add an employee.
- Edit an employee's detail.
- Delete an employee.
- Delete multiple employees to delete them at once.

## Key Points <a name="keyPoints"></a>
- Design Pattern - > M.V.VM - Model View ViewModel
- Architecture Used -> Single Activity Architecture
- Caching using roomDB to allow user to view last fetched database.

## Project Dependencies <a name="dependencies"></a> -
- [Firebase](https://console.firebase.google.com/) - Storage
- [Androidx](https://developer.android.com/jetpack/androidx) - For Layouts, Navigation, Fragments and more
- [Kotlin Coroutines](https://developer.android.com/kotlin/coroutines) - For Asynchronous Task
- [Room DB](https://developer.android.com/training/data-storage/room) - For Local Database
- [Coil](https://coil-kt.github.io/coil/) - For Loading Images
- [Retrofit](https://square.github.io/retrofit/) - For HTTP Client
- [Toasty](https://github.com/GrenderG/Toasty) - Toast with better UI

## 📁 Project Structure <a name="structure"></a>

**Following is the description of different packages present in the project**
## data - 
    * local -> Contains the file/files required to create / maintain the local DB.
    * model -> Contains the model classes representing the local DB.
    * remote -> Contains the model classes representing the Remote API.
    * local -> Contains the repository file/files required to make calls to the remote API.
    
## interfaces - 
    "Contains interfaces and object class required for the API Call." 
    
## ui - 
    "Contains activities, fragments, and adapters used throughout the application"

## util - 
    "Contains the utility functions / classes, and wrapper class for the response from API that are used throughout the application"

## viewmodel -
    "Contains the classes for the activity/fragment viewmodels and their factories"

## res -
    "Contains the resources used throughout the application, like - drawable, layout, navigation, and values."

## ⛏️ Built With <a name = "tech_stack"></a>

- [Kotlin](https://developer.android.com/kotlin)

## ✍️ Authors <a name = "author"></a>

- [@AbhishekMishra](https://github.com/mishra5047)