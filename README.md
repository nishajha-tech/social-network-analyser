# 🌐 Social Network Analyser
> **Second-Year College CSE Project | Beginner DSA Edition**  
> *Built with pure Java OOP, ArrayLists, Loops, and Modern Responsive Web Frontend (No external frameworks, no Maven/Gradle required, zero graph algorithms).*

---

## 📌 1. Project Overview & Objective
The **Social Network Analyser** simulates a real-world social media connection network (such as LinkedIn, Facebook, or Instagram). It enables users to:
1. Register new members and prevent duplicate names.
2. Establish mutual (bidirectional) friendships between users without self-connections.
3. Inspect and search through the user directory.
4. Discover **Mutual Friends** shared between any two people.
5. Compute **Friend Suggestions** ("People You May Know") using friends-of-friends traversal.
6. Generate **Network Analytics** (most popular user, total connections, average friends, density, isolated members).
7. Interact with an **Interactive Network Map** rendered in HTML5 Canvas.

---

## 🛡️ 2. Strict DSA Constraints Compliance

> **Important Evaluation Note:**  
> This project is designed specifically for students who have studied Data Structures and Algorithms **only up to Trees**.  
> **No Graphs, No Adjacency Lists, No Adjacency Matrices, No BFS, and No DFS were used.**

### How the Requirements Were Solved with Beginner Concepts:
| Requirement | Traditional Graph Way (Forbidden) | How This Project Solves It (Beginner-Friendly) |
| :--- | :--- | :--- |
| **User & Friends Storage** | Adjacency List / Matrix | `User` Class holding `String name` and `ArrayList<String> friends`. Network has `ArrayList<User> users`. |
| **Mutual Friends** | Graph intersection / BFS | Simple nested `for` loop comparing two `ArrayList<String>` friend lists: `O(F1 × F2)`. |
| **Friend Suggestions** | 2-Hop BFS / Shortest Path | Nested loops exploring friends-of-friends with 2 conditional checks (`!isSelf` and `!isDirectFriend`). |
| **Most Popular User** | Node Degree in Graph | Linear scan `for` loop tracking `maxFriends` across `ArrayList<User>`. |
| **Duplicate Prevention** | Graph vertex lookup | Linear search loop through `ArrayList<User>` checking `name.equalsIgnoreCase()`. |
| **Sorting Recommendations**| PriorityQueue / Min-Heap | Beginner-friendly Bubble Sort on `ArrayList<Suggestion>` by mutual connection count. |

---

## 📁 3. Project Directory Structure
```
social-network-analyser/
├── bin/                          # Compiled Java bytecode (.class files)
├── src/
│   ├── backend/
│   │   ├── User.java             # Model class (encapsulates user name & friend ArrayList)
│   │   ├── Suggestion.java       # Recommendation data holder (candidate & mutual friends)
│   │   ├── NetworkStats.java     # Network analytics metrics holder
│   │   ├── SocialNetwork.java    # Core business logic (pure ArrayLists, loops, zero graphs)
│   │   ├── NetworkServer.java    # Lightweight built-in Java HttpServer & REST API
│   │   ├── Main.java             # Entry point for Web Server mode (default)
│   │   └── ConsoleApp.java       # Standalone Terminal / CLI Menu mode for lab viva
│   └── frontend/
│       ├── index.html            # Modern semantic dashboard interface
│       ├── style.css             # Glassmorphism styling, responsive layout, dark/light themes
│       └── app.js                # Frontend controller & HTML5 Canvas Network Visualizer
├── run-web.bat                   # One-click Windows runner for Web UI
├── run-console.bat               # One-click Windows runner for Terminal CLI
└── README.md                     # Comprehensive project documentation
```

---

## 🚀 4. How to Run in VS Code or Terminal

### Option A: Run via Batch Scripts (Easiest for Windows)
1. Double-click `run-web.bat` to compile, launch the backend, and open `http://localhost:8080` in your browser.
2. Double-click `start-public-link.bat` to generate a live, shareable public HTTPS link accessible on mobile phones and external devices.
3. Double-click `run-console.bat` to launch the interactive Terminal / CLI menu mode.

### Option B: Run in VS Code
1. Open VS Code.
2. Go to **File -> Open Folder...** and select `social-network-analyser`.
3. Open `src/backend/Main.java`.
4. Click the **Run** button at the top-right of the editor (or press `F5`).
5. Open your web browser and navigate to: `http://localhost:8080`

### Option C: Run via Command Prompt / PowerShell
```powershell
# 1. Navigate to the project root directory
cd C:\Users\Akhil\.gemini\antigravity-ide\scratch\social-network-analyser

# 2. Compile all Java backend files
javac -d bin src/backend/*.java

# 3. Launch the Web Application Server
java -cp bin backend.Main

# Or launch the Terminal CLI Menu
java -cp bin backend.ConsoleApp
```

---

## 🧪 5. Sample Demo Data
The project includes a **Load Demo Data** button that seeds the network with:

### Users:
- `Nisha`
- `Priya`
- `Rahul`
- `Aman`
- `Karan`

### Mutual Friendships:
- `Nisha` ⇄ `Priya`
- `Nisha` ⇄ `Aman`
- `Priya` ⇄ `Rahul`
- `Priya` ⇄ `Karan`

### Expected Test Results:
1. **Total Users:** 5
2. **Total Unique Friendships:** 4
3. **Most Popular User:** Priya (3 connections: Nisha, Rahul, Karan)
4. **Mutual Friends for Rahul & Nisha:** Priya (because Priya is friends with both)
5. **Friend Suggestions for Rahul:**
   - `Nisha` (Connected via Priya)
   - `Karan` (Connected via Priya)

---

## 💡 6. College Viva / Interview Questions & Model Answers

### Q1: Why did you not use an Adjacency Matrix or Graph structure?
> **Answer:**  
> "In introductory DSA curricula, students are taught up to Trees (Arrays, Strings, ArrayLists, Linked Lists, Stacks, Queues, and Trees). Graphs and graph traversal algorithms (BFS/DFS) belong to advanced DSA. This project deliberately demonstrates how standard Object-Oriented Programming (a `User` class with an internal `ArrayList<String> friends`) paired with nested loops can solve friendship networks and recommendations cleanly with low cognitive overhead and zero external dependencies."

### Q2: How does the Mutual Friends algorithm work? What is its time complexity?
> **Answer:**  
> "We retrieve User A's friends list and User B's friends list. We use two nested `for` loops. The outer loop iterates through each friend of User A ($F_1$ items), and the inner loop checks if that friend exists in User B's friend list ($F_2$ items). If `friendA.equalsIgnoreCase(friendB)`, it is added to the mutual friends list. The time complexity is $O(F_1 \times F_2)$, which is virtually instantaneous for social network user friend lists."

### Q3: How do Friend Suggestions work without BFS?
> **Answer:**  
> "It uses a 2-level nested loop (friends-of-friends):
> 1. Loop 1: Iterate over direct friends of the target user.
> 2. Loop 2: For each direct friend, iterate over their friends (friends of friends).
> 3. Conditional filter: If candidate $\neq$ target user AND candidate is not already a direct friend of target user, add them as a suggestion and record the mutual friend bridging them.
> Finally, suggestions are sorted by mutual friend count using a simple Bubble Sort."

### Q4: Why is Total Friendships equal to the sum of friend counts divided by 2?
> **Answer:**  
> "Because friendships in this network are mutual (bidirectional). When Nisha and Priya become friends, 'Priya' is stored in Nisha's list, and 'Nisha' is stored in Priya's list. Therefore, each unique friendship is counted twice when summing all users' friend counts. Dividing the sum by 2 yields the exact number of unique friendships without double counting."

---

## 💻 7. Technologies Used
- **Backend:** Java 17+ (Uses standard built-in `com.sun.net.httpserver.HttpServer` - Zero 3rd party JARs required).
- **Frontend:** HTML5, CSS3 (Glassmorphism & CSS Variables), Vanilla JavaScript (ES6+), HTML5 Canvas.
- **Icons:** Inline SVG Icons (Lucide design style).
- **Typography:** Google Fonts (`Inter` & `Outfit`).
