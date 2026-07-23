# Ashwani Singh — Full Stack Developer Portfolio

A sleek, responsive, neo-brutalist tactile personal portfolio website built with clean HTML5, CSS3, and modern JavaScript.

![Ashwani Singh Portfolio](assets/resume.pdf)

## 🚀 Live Preview & Local Setup

### Running Locally
To view the portfolio locally, you can use any static server or Python's built-in HTTP server:

```bash
# Clone or navigate to directory
cd d:\Portfolio

# Start python local HTTP server
python -m http.server 8080
```
Open **`http://localhost:8080`** in your browser.

---

## 🛠️ Project Structure

```
Portfolio/
├── index.html        # Main HTML structure, SEO metadata, JSON-LD Schema & Content
├── style.css         # Custom Design System, Light/Dark Mode variables, Responsive Grid
├── script.js         # Interactive Theme Switcher, Scroll Progress Bar, Toast Alerts
├── assets/
│   └── resume.pdf    # Resume PDF file linked to the hero Résumé button
└── README.md         # Documentation & deployment guide
```

---

## ✨ Features & Highlights

- **Neo-Brutalist Tactile Aesthetic**: Custom HSL color palette with cream paper background, deep ink navy text, and signal orange accents.
- **Light & Dark Mode**: Seamless theme switcher with user preference persistence using `localStorage`.
- **Interactive Résumé Button**: Directly opens `assets/resume.pdf` in a new tab.
- **Career Path & Experience**: Highlighting full-stack achievements at ETS (Educational Testing Service) with metric cards (+35% onboarding, -40% MTTR reduction).
- **Featured Projects Grid**: Detailed project cards for TOEIC Link, GET Platform, and AI Code Inspector with Gemini AI.
- **Multi-Card Education Section**: Detailed B.Tech (CGPA: 7.38/10), Class XII (74.00%), and Class X (84.16%) scores.
- **Achievements & Research**: Competitive programming stats (LeetCode 1400+, Codeforces 800+) and published ML research paper.
- **One-Click Contact System**: Copy-to-clipboard email and phone buttons with toast notifications, plus social links.

---

## 🌐 Deploying to GitHub Pages

1. Initialize git and commit your files:
   ```bash
   git init
   git add .
   git commit -m "Initial portfolio release"
   ```
2. Link your GitHub repository:
   ```bash
   git remote add origin https://github.com/iemashwani/portfolio.git
   git branch -M main
   git push -u origin main
   ```
3. Go to your repository settings on GitHub -> **Pages** -> Select `main` branch and `/ (root)` folder -> **Save**.
