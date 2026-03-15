package org.example.ui

import kotlinx.html.*
import org.example.models.AppSession

object Templates {
    fun HTML.indexPage() {
        head {
            title("Image Viewer")
            meta(name = "viewport", content = "width=device-width, initial-scale=1")
            link(rel = "stylesheet", href = "/static/css/style.css")
        }
        body {
            div("container") {
                h1 { +"Image Gallery" }
                div("nav-links") {
                    a(href = "#") { id = "allLink"; +"All Images" }
                    a(href = "#") { id = "favsLink"; +"Favorites" }
                    a(href = "#") { id = "starredLink"; +"Starred" }
                    a(href = "#") { id = "flaggedLink"; +"Flagged" }
                    a(href = "/admin") { +"Settings" }
                    
                    div {
                        style = "display: inline-block; margin-left: 20px;"
                        +"Sort: "
                        select {
                            id = "sortSelect"
                            option { value = "name"; +"Name" }
                            option { value = "new"; selected = true; +"Newest First" }
                            option { value = "old"; +"Oldest First" }
                        }
                    }
                }
                div("image-list") { id = "imageList" }
                div("pagination") {
                    style = "display: none;"
                    button { id = "prevBtn"; +"Previous" }
                    span { id = "pageInfo"; +"Page 1" }
                    button { id = "nextBtn"; +"Next" }
                }
                div { id = "sentinel"; style = "height: 50px;" }
            }

            // Modal container for image preview
            div("modal") {
                id = "imgModal"
                button {
                    classes = setOf("close")
                    id = "modalClose"
                    title = "Close"
                    unsafe {
                        +"""<svg viewBox="0 0 24 24" width="32" height="32" stroke="currentColor" stroke-width="2.5" fill="none" stroke-linecap="round" stroke-linejoin="round"><line x1="18" y1="6" x2="6" y2="18"></line><line x1="6" y1="6" x2="18" y2="18"></line></svg>"""
                    }
                }
                button { 
                    classes = setOf("nav-btn", "prev")
                    id = "modalPrev"
                    title = "Previous Image"
                    unsafe {
                        +"""<svg viewBox="0 0 24 24" width="36" height="36" stroke="currentColor" stroke-width="2.5" fill="none" stroke-linecap="round" stroke-linejoin="round"><polyline points="15 18 9 12 15 6"></polyline></svg>"""
                    }
                }
                button { 
                    classes = setOf("nav-btn", "next")
                    id = "modalNext"
                    title = "Next Image"
                    unsafe {
                        +"""<svg viewBox="0 0 24 24" width="36" height="36" stroke="currentColor" stroke-width="2.5" fill="none" stroke-linecap="round" stroke-linejoin="round"><polyline points="9 18 15 12 9 6"></polyline></svg>"""
                    }
                }
                div("modal-content-wrapper") {
                    img(alt = "Expanded image") { id = "modalImg" }
                }
                div("modal-info") { id = "modalInfo" }
                div("zoom-controls") {
                    button { 
                        id = "modalRandom"
                        classes = setOf("nav-btn", "random")
                        title = "Random Image"
                        unsafe {
                            +"""<svg viewBox="0 0 24 24" width="24" height="24" stroke="currentColor" stroke-width="2.5" fill="none" stroke-linecap="round" stroke-linejoin="round"><path d="M16 3h5v5M4 20L21 3M21 16v5h-5M15 15l6 6M4 4l5 5"></path></svg>"""
                        }
                    }
                    button { 
                        id = "modalFavBtn"
                        classes = setOf("fav-btn-modal")
                        title = "Favorite"
                        unsafe {
                            +"""<svg viewBox="0 0 24 24" width="24" height="24" stroke="currentColor" stroke-width="2.5" fill="none" stroke-linecap="round" stroke-linejoin="round"><path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l8.72-8.72 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"></path></svg>"""
                        }
                    }
                    button { 
                        id = "modalStarBtn"
                        classes = setOf("star-btn-modal")
                        title = "Star"
                        unsafe {
                            +"""<svg viewBox="0 0 24 24" width="24" height="24" stroke="currentColor" stroke-width="2.5" fill="none" stroke-linecap="round" stroke-linejoin="round"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"></polygon></svg>"""
                        }
                        span { id = "starCountLabel"; style = "margin-left: 5px; font-size: 14px; display: none;" }
                    }
                    button { 
                        id = "modalFlagBtn"
                        classes = setOf("flag-btn-modal")
                        title = "Flag"
                        unsafe {
                            +"""<svg viewBox="0 0 24 24" width="24" height="24" stroke="currentColor" stroke-width="2.5" fill="none" stroke-linecap="round" stroke-linejoin="round"><path d="M4 15s1-1 4-1 5 2 8 2 4-1 4-1V3s-1 1-4 1-5-2-8-2-4 1-4 1zM4 22v-7"></path></svg>"""
                        }
                    }
                    button { 
                        id = "fullscreenBtn"
                        title = "Fullscreen"
                        unsafe {
                            +"""<svg viewBox="0 0 24 24" width="24" height="24" stroke="currentColor" stroke-width="2.5" fill="none" stroke-linecap="round" stroke-linejoin="round"><path d="M8 3H5a2 2 0 0 0-2 2v3m18 0V5a2 2 0 0 0-2-2h-3m0 18h3a2 2 0 0 0 2-2v-3M3 16v3a2 2 0 0 0 2 2h3"></path></svg>"""
                        }
                    }
                }
            }

            script { src = "/static/js/app.js" }
        }
    }

    fun HTML.adminPage(currentDir: String?) {
        head {
            title("Admin - Settings")
            meta(name = "viewport", content = "width=device-width, initial-scale=1")
            style {
                unsafe {
                    +"""
                        :root {
                            --bg-color: #f0f0f0;
                            --card-bg: white;
                            --text-color: #333;
                            --accent-color: #007bff;
                        }
                        @media (prefers-color-scheme: dark) {
                            :root {
                                --bg-color: #121212;
                                --card-bg: #1e1e1e;
                                --text-color: #e0e0e0;
                                --accent-color: #4da3ff;
                            }
                        }
                        body { font-family: sans-serif; padding: 20px; background: var(--bg-color); color: var(--text-color); }
                        .card { background: var(--card-bg); padding: 20px; border-radius: 8px; box-shadow: 0 2px 5px rgba(0,0,0,0.1); max-width: 600px; margin: auto; }
                        input { width: 100%; padding: 10px; margin: 10px 0; box-sizing: border-box; background: var(--bg-color); color: var(--text-color); border: 1px solid #ccc; }
                        button { padding: 10px 20px; background: var(--accent-color); color: white; border: none; border-radius: 4px; cursor: pointer; margin-right: 10px; }
                        button.secondary { background: #6c757d; }
                        .back-link { display: block; margin-top: 20px; color: var(--accent-color); text-decoration: none; }
                    """
                }
            }
        }
        body {
            div("card") {
                h1 { +"Administrative Controls" }
                form(action = "/admin", method = FormMethod.post) {
                    label { +"Image Source Directory (Absolute Path):" }
                    input(type = InputType.text, name = "dir") {
                        value = currentDir ?: ""
                        placeholder = "e.g. C:\\Photos"
                    }
                    button(type = ButtonType.submit) { +"Save Configuration" }
                }
                
                hr {}
                
                h2 { +"Library Indexing" }
                p { +"Manual trigger to refresh the gallery and clear metadata caches." }
                button { 
                    id = "scanBtn"
                    +"Scan Library Now" 
                }
                span { id = "scanStatus"; style = "margin-left: 10px;" }
                
                a(href = "/", classes = "back-link") { +"← Back to Gallery" }
            }
            
            script {
                unsafe {
                    +"""
                        document.getElementById('scanBtn').onclick = async () => {
                            const status = document.getElementById('scanStatus');
                            status.innerText = 'Scanning...';
                            try {
                                const resp = await fetch('/api/admin/scan');
                                if (resp.ok) status.innerText = 'Success!';
                                else status.innerText = 'Error occurred.';
                            } catch (e) {
                                status.innerText = 'Network error.';
                            }
                        };
                    """
                }
            }
        }
    }
}
