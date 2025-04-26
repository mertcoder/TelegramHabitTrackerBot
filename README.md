<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
</head>
<body>
  <h1>HabitTrackerBot – Telegram Habit Tracker & Goal Manager 🧐🚀</h1>
      <img src="https://github.com/user-attachments/assets/6e212a07-7718-411b-b93b-a7918e19817f" width="200" />

  <p>HabitTrackerBot is a productivity-focused Telegram bot that helps you build strong daily routines, track your habits, and crush your long-term goals.</p>
  <p>Stay consistent. Stay grinding.</p>
  <hr>

  <h2>✨ Features</h2>
  <ul>
    <li>🎯 Add and manage long-term goals (Sport, Reading, Work, Personal categories)</li>
    <li>📅 Track daily habits and maintain your consistency streaks</li>
    <li>📊 Visualize your progress and completed goals</li>
    <li>🏠 Simple and clean main menu navigation</li>
    <li>🛠️ Settings feature coming soon</li>
  </ul>
  <hr>

  <h2>📦 Installation</h2>
  <ol>
    <li>Clone the repository:
      <pre><code>git clone https://github.com/mertcoder/TelegramHabitTrackerBot.git
cd TelegramHabitTrackerBot
</code></pre>
    </li>
    <li>Build the project:
      <pre><code>./gradlew build
</code></pre>
    </li>
    <li>Run the bot:
      <pre><code>java -jar build/libs/your-bot-jar-file-name.jar
</code></pre>
      <p><em>(replace <code>your-bot-jar-file-name.jar</code> with your actual jar name)</em></p>
    </li>
  </ol>
  <hr>

  <h2>⚙️ Requirements</h2>
  <ul>
    <li>Java 17+</li>
    <li>Kotlin</li>
    <li>Telegram Bot Token</li>
    <li>MongoDB Atlas or MongoDB Server</li>
  </ul>
  <hr>

  <h2>🔒 Environment Variables</h2>
  <p>Before running the bot, make sure you set the following environment variables:</p>
  <table border="1" cellpadding="5">
    <thead>
      <tr><th>Key</th><th>Example Value</th></tr>
    </thead>
    <tbody>
      <tr><td><code>BOT_TOKEN</code></td><td><code>123456789:ABCDEF_yourbotapikey</code></td></tr>
      <tr><td><code>MONGO_URI</code></td><td><code>mongodb+srv://user:pass@cluster.mongodb.net/</code></td></tr>
    </tbody>
  </table>
  <p>You can define them directly in your deployment environment (Railway, Render, VPS).</p>
  <hr>

  <h2>📋 Usage</h2>
  <ul>
    <li><code>/start</code> ➔ Show the main menu</li>
    <li><code>/menu</code> ➔ Navigate back to the main menu</li>
    <li><code>/goals</code> ➔ View and manage your goals</li>
  </ul>
  <p>You can add goals, track habits, and review your daily progress directly inside Telegram.</p>
  <hr>

  <h2>🢾 License</h2>
  <p>This project is licensed under the MIT License - see the <a href="LICENSE">LICENSE</a> file for details.</p>
  <hr>

  <h2>✍️ Author</h2>
  <p>Developed by <a href="https://github.com/mertcoder">Mert Akyıldız</a><br>
  Stay consistent, stay grinding! 🔥</p>
</body>
</html>
