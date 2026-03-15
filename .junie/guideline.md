# Image Viewer Application - Feature Specifications

An efficient, lightweight browser-based image management system using a **Vite + Vanilla JavaScript** frontend and a **Kotlin (Ktor)** backend.

## 1. Administrative Controls (`/admin`)

- **Source Configuration**: A simple input field to define the absolute server-side directory path where images are stored.
- **Library Indexing**:
    - **Auto-Detect**: Background service (e.g., `WatchService` in Kotlin) that monitors the directory for new, moved, or deleted files and updates the gallery in real-time.
    - **Manual Refresh**: A fallback "Scan" button to force a full re-index of the directory.
- **Performance Toggles**: Settings to adjust thumbnail generation quality and cache expiration headers.
- **No-Auth Design**: Accessible directly for local/personal use without a login barrier.

## 2. Main Gallery (`/home`)

- **Fluid Responsive Grid**:
    - CSS Grid implementation that automatically calculates column counts based on viewport width.
    - `object-fit: cover` logic for a clean, uniform grid appearance.
- **Hybrid Navigation**:
    - **Infinite Scroll**: Automatically fetches the next batch of 10 images as the user reaches the bottom.
    - **Pagination UI**: Alternative controls for jumping to specific sections of a large library.
- **Intelligent Caching**:
    - Browser-level caching of high-res images after the first load.
    - Intersection Observer API to lazy-load images only when they enter the viewport.

## 3. Interactive Image Modal

- **Navigation Logic**:
    - **Close Triggers**: Dedicated "Zoom-out" icon, clicking the backdrop, or pressing the `Esc` key.
    - **Zoom Suite**: Mouse-wheel zoom and +/- buttons for detailed inspection.
- **Metadata Overlay**: Displays filename and dimensions.
- **Keyboard Shortcuts**: Left/Right arrow keys to cycle through images while the modal is open.

## 4. Favorites System (`/favorites`)

- **Interaction**: A heart icon overlay on gallery items.
- **State Management**:
    - **Local Persistence**: Toggling the heart updates `localStorage` instantly.
    - **Server Sync**: Backend persists the list of favorite paths/IDs so they survive browser cache clears.
- **Dedicated View**: A filtered gallery view showing only favorited items.

## 5. System Architecture & Backend (Kotlin)

- **Vite Frontend**: Powered by Vite for fast development, HMR, and modern module handling.
- **Image Streaming**: A specialized controller to stream image bytes from the local disk to the browser.
- **Thumbnail Service**: On-the-fly resizing logic to serve smaller previews to the gallery, significantly improving load speeds.
- **Watcher Service**: Utilizes Java's `java.nio.file.WatchService` to provide the auto-detection feature.
- **JSON API**:
    - `GET /api/images`: Returns paginated metadata.
    - `POST /api/favorites`: Synchronizes user preferences.
    - `POST /api/admin/config`: Updates the image source directory.

## 6. UI/UX Enhancements

- **Loading States**: Skeleton screens for the image grid.
- **Empty States**: Clear messaging if the directory is empty or no favorites exist.
- **Dark Mode Support**: Automatic switching based on system `prefers-color-scheme`.