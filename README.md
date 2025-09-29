# ConnectSphere---Real-Time-Chat-Application

ConnectSphere is a sophisticated, feature-rich messaging application built with a powerful backend using **Spring Boot** and a dynamic, modern frontend using **vanilla JavaScript** and **Tailwind CSS**. It provides a comprehensive suite of tools for real-time communication, including private messaging, rich media sharing, and interactive controls, all wrapped in a clean, professional, and intuitive user interface.


## ✨ Features

ConnectSphere is packed with professional-grade features designed to create an engaging and seamless chat experience:

* **💬 Real-Time Messaging Core:** Full support for public (group) and private (one-on-one) conversations using **STOMP over WebSocket** for minimal latency.
* **👤 User Profiles & Presence:** Secure user authentication with custom avatars and status messages. The sidebar includes a live list of online users with green presence indicators.
* **🖼️ Rich Content Sharing:**
    * **Giphy Integration:** Fetch and send GIFs directly in chat using a `/gif` command.
    * **File Sharing:** Upload any file, which is then displayed as a clean, downloadable card for other users.
    * **Link Previews:** Any URL shared in a message automatically generates a rich preview card by scraping web page metadata on the server.
* ** interactive Message Controls:**
    * **Replies:** Reply to specific messages to maintain conversational context, with a UI modeled after modern messaging apps.
    * **Reactions:** Instantly react to messages using a WhatsApp-style quick-reaction bar that appears on hover.
    * **Edit & Delete:** Users can edit or delete their own messages. All controls are available via a clean dropdown menu.
* **MENTIONS:** Directly notify other users by tagging them with `@username`.
* **💎 Modern, Responsive UI:**
    * A beautiful, minimalist user interface built with **Tailwind CSS** that is fully responsive and looks great on any device.
    * **Message Grouping:** Consecutive messages from the same user are visually grouped to reduce clutter.
    * **Dark & Light Modes:** Switch between themes for optimal viewing comfort.

---

## 🛠️ Tech Stack

This project leverages a modern, robust technology stack for both the backend and frontend.

| Backend                        | Frontend                           | Database               | Build Tool |
| ------------------------------ | ---------------------------------- | ---------------------- | ---------- |
| Java (JDK 17+)                 | HTML5                              | H2 (In-Memory)         | Maven      |
| Spring Boot 3                  | Tailwind CSS (Utility-First CSS)   | Spring Data JPA        |            |
| Spring Security                | JavaScript (ES6+)                  | (with Hibernate)       |            |
| Spring WebSocket (for STOMP)   | Thymeleaf (Server-Side Templating) |                        |            |
| Spring Web (MVC for REST APIs) | SockJS & Stomp.js                  |                        |            |
| Lombok                         | Font Awesome (Icons)               |                        |            |
| Jsoup (for Web Scraping)       |                                    |                        |            |

---

## 🚀 Getting Started

To get a local copy up and running, follow these simple steps.

### **Prerequisites**

* **Java (JDK) 17** or later installed.
* **Apache Maven** installed.
* A **Giphy API Key** from the [Giphy for Developers](https://developers.giphy.com/) portal.

### **Installation & Setup**

1.  **Clone the repository:**
    ```sh
    git clone [https://github.com/your-username/connectsphere.git](https://github.com/your-username/connectsphere.git)
    cd connectsphere
    ```

2.  **Configure the Application:**
    * Open the `src/main/resources/application.properties` file.
    * Update the `giphy.api.key` property with your Giphy API key.
        ```properties
        # Giphy API Key
        giphy.api.key=YOUR_GIPHY_API_KEY_HERE
        ```

3.  **Build and Run the Application:**
    * Use Maven to build the project. This will also download all the necessary dependencies.
        ```sh
        mvn clean install
        ```
    * Run the application:
        ```sh
        mvn spring-boot:run
        ```
    The application will start up on the embedded Tomcat server. Since it uses an H2 in-memory database, no database setup is required.

4.  **Access the Application:**
    * Open your web browser and navigate to `http://localhost:8080`.
    * You will see the registration page. Create an account, log in, and start messaging!

---

## 📂 Project Structure

The project follows the standard Maven project structure and is organized using the Model-View-Controller (MVC) architectural pattern:
Of course. Here is the complete project structure for your chat application, displayed in a text-based tree format.

```
.
├── HELP.md
├── pom.xml
├── mvnw
├── mvnw.cmd
├── src
│   ├── main
│   │   ├── java
│   │   │   └── com
│   │   │       └── chat
│   │   │           └── app
│   │   │               ├── config
│   │   │               │   ├── ChatRoom.java
│   │   │               │   ├── SecurityConfig.java
│   │   │               │   ├── WebSocketConfig.java
│   │   │               │   └── WebSocketEventListener.java
│   │   │               ├── controller
│   │   │               │   ├── AuthController.java
│   │   │               │   ├── ChatController.java
│   │   │               │   ├── FileController.java
│   │   │               │   └── ProfileController.java
│   │   │               ├── model
│   │   │               │   ├── ChatMessage.java
│   │   │               │   ├── ChatMessageEntity.java
│   │   │               │   ├── LinkPreview.java
│   │   │               │   ├── MessageReaction.java
│   │   │               │   └── User.java
│   │   │               ├── repository
│   │   │               │   ├── ChatMessageRepository.java
│   │   │               │   ├── MessageReactionRepository.java
│   │   │               │   └── UserRepository.java
│   │   │               ├── service
│   │   │               │   ├── AppUserDetailsService.java
│   │   │               │   ├── FileStorageService.java
│   │   │               │   ├── GiphyService.java
│   │   │               │   └── LinkPreviewService.java
│   │   │               └── AppApplication.java
│   │   └── resources
│   │       ├── static
│   │       │   └── img
│   │       │       └── default-avatar.png
│   │       ├── templates
│   │       │   ├── chat.html
│   │       │   ├── login.html
│   │       │   └── register.html
│   │       └── application.properties
│   └── test
│       └── java
│           └── com
│               └── chat
│                   └── app
│                       └── AppApplicationTests.java
├── target
└── uploads



