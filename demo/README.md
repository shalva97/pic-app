# Image Gallery Demo

This is a standalone demo version of the FAP-App that runs entirely in the browser without a backend. It uses the exact same frontend code (CSS and JS) as the main application to ensure a consistent experience.

## How it works

- **Images**: All images are served from the `sample-pics` directory.
- **State**: Your favorites, stars, and flags are saved in your browser's `localStorage`.
- **Shared Code**: The demo uses `static/js/app.js` and `static/css/style.css`, which are shared with the Ktor-based version. The JavaScript code detects it's in demo mode and mocks all API calls.

## Publishing to GitHub Pages

To publish this demo to GitHub Pages:

1. Push this `demo` folder to your repository.
2. Go to your repository settings on GitHub.
3. Select **Pages** from the sidebar.
4. Under **Build and deployment**, set the **Source** to **Deploy from a branch**.
5. Choose the branch (e.g., `main`) and the folder `/demo`.
6. Click **Save**.

Your demo will be available at `https://<your-username>.github.io/<repository-name>/`.
