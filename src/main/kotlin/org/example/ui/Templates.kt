package org.example.ui

import kotlinx.html.*
import org.example.models.AppSession

object Templates {
    fun HTML.indexPage() {
        head {
            title("Image Viewer")
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
                        body { font-family: sans-serif; margin: 0; padding: 10px; background: var(--bg-color); color: var(--text-color); }
                        .container { width: 100%; margin: auto; }
                        
                        /* Responsive Grid */
                        .image-list {
                            display: grid;
                            grid-template-columns: repeat(auto-fill, minmax(min(calc(50% - 15px), 1024px), 1fr));
                            gap: 15px;
                            padding: 10px 0;
                        }

                        .image-card { 
                            background: var(--card-bg); 
                            border-radius: 8px; 
                            overflow: hidden; 
                            box-shadow: 0 2px 5px rgba(0,0,0,0.1); 
                            position: relative;
                            aspect-ratio: 1 / 1;
                        }
                        .image-card img { 
                            width: 100%; 
                            height: 100%;
                            object-fit: cover;
                            display: block; 
                            cursor: zoom-in; 
                            transition: transform 0.2s;
                        }
                        .image-card img:hover { transform: scale(1.02); }

                        .fav-btn {
                            position: absolute;
                            top: 10px;
                            right: 10px;
                            background: rgba(255,255,255,0.7);
                            border: none;
                            border-radius: 50%;
                            width: 32px;
                            height: 32px;
                            cursor: pointer;
                            display: flex;
                            align-items: center;
                            justify-content: center;
                            font-size: 20px;
                            color: #ccc;
                            transition: color 0.2s;
                        }
                        .fav-btn.active { color: #ff4757; }
                        .star-btn {
                            position: absolute;
                            top: 10px;
                            right: 50px;
                            background: rgba(255,255,255,0.7);
                            border: none;
                            border-radius: 50%;
                            width: 32px;
                            height: 32px;
                            cursor: pointer;
                            display: flex;
                            align-items: center;
                            justify-content: center;
                            font-size: 14px;
                            color: #ccc;
                            transition: color 0.2s;
                            visibility: hidden;
                        }
                        .star-btn.visible { visibility: visible; }
                        .star-btn.active { color: #f1c40f; }
                        .flag-btn {
                            position: absolute;
                            top: 10px;
                            right: 90px;
                            background: rgba(255,255,255,0.7);
                            border: none;
                            border-radius: 50%;
                            width: 32px;
                            height: 32px;
                            cursor: pointer;
                            display: flex;
                            align-items: center;
                            justify-content: center;
                            font-size: 16px;
                            color: #ccc;
                            transition: color 0.2s;
                        }
                        .flag-btn.active { color: #e67e22; }
                        .zoom-controls button.active.fav-btn-modal { color: #ff4757; }
                        .zoom-controls button.active.star-btn-modal { color: #f1c40f; }
                        .zoom-controls button.active.flag-btn-modal { color: #e67e22; }
                        
                        /* Skeleton Screen */
                        .skeleton {
                            background: #eee;
                            background: linear-gradient(110deg, #ececec 8%, #f5f5f5 18%, #ececec 33%);
                            background-size: 200% 100%;
                            animation: 1.5s shine linear infinite;
                        }
                        @keyframes shine { to { background-position-x: -200%; } }

                        .pagination { display: flex; justify-content: space-between; align-items: center; margin: 20px 0; }
                        button { padding: 8px 16px; border-radius: 4px; border: 1px solid #ccc; background: var(--card-bg); color: var(--text-color); cursor: pointer; }
                        button:hover { background: #eee; }
                        
                        .nav-links { margin-bottom: 20px; }
                        .nav-links a { margin-right: 15px; color: var(--accent-color); text-decoration: none; font-weight: bold; }

                        /* Modal (lightbox) styles */
                        .modal { display: none; position: fixed; z-index: 1000; left: 0; top: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.95); }
                        .modal.open { display: flex; align-items: center; justify-content: center; flex-direction: column; }
                        .modal .close { position: absolute; top: 20px; right: 30px; color: #fff; font-size: 40px; cursor: pointer; }
                        .modal .nav-btn { position: absolute; top: 50%; transform: translateY(-50%); background: none; border: none; color: white; font-size: 50px; cursor: pointer; padding: 20px; z-index: 1010; }
                        .modal .prev { left: 10px; top: calc(50% - 40px); }
                        .modal .next { right: 10px; top: 50%; }
                        .modal .random { left: 10px; top: calc(50% + 40px); font-size: 30px; }
                        
                        .modal-content-wrapper { position: relative; width: 100%; height: 100%; overflow: hidden; display: flex; align-items: center; justify-content: center; touch-action: none; }
                        .modal img { max-width: 100%; max-height: 100%; transition: transform 0.1s; transform-origin: center; cursor: grab; user-select: none; -webkit-user-drag: none; transition: none; }
                        .modal img:active { cursor: grabbing; }

                        .modal-info { color: white; margin-top: 15px; text-align: center; }
                        .zoom-controls { position: absolute; bottom: 20px; left: 50%; transform: translateX(-50%); display: flex; gap: 10px; z-index: 1010; }
                        .zoom-controls button { background: rgba(255,255,255,0.2); border: none; color: white; width: 40px; height: 40px; border-radius: 50%; font-size: 20px; display: flex; align-items: center; justify-content: center; cursor: pointer; }
                        .zoom-controls button:hover { background: rgba(255,255,255,0.3); }
                    """
                }
            }
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
                        id = "randomToggleContainer"
                        style = "display: inline-block; margin-left: 20px;"
                        label {
                            input(type = InputType.checkBox) { id = "randomToggle"; checked = true }
                            +" Random"
                        }
                    }
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
                span("close") { id = "modalClose"; +"×" }
                button(classes = "nav-btn prev") { id = "modalPrev"; +"‹" }
                button(classes = "nav-btn next") { id = "modalNext"; +"›" }
                button(classes = "nav-btn random") { 
                    id = "modalRandom"
                    title = "Random Image"
                    unsafe {
                        +"""<svg viewBox="0 0 24 24" width="36" height="36" stroke="currentColor" stroke-width="2" fill="none"><path d="M16 3h5v5M4 20L21 3M21 16v5h-5M15 15l6 6M4 4l5 5"></path></svg>"""
                    }
                }
                
                div("modal-content-wrapper") {
                    img(alt = "Expanded image") { id = "modalImg" }
                }
                
                div("modal-info") { id = "modalInfo" }
                
                div("zoom-controls") {
                    button { 
                        id = "modalFavBtn"
                        classes = setOf("fav-btn-modal")
                        title = "Favorite"
                        +"❤"
                    }
                    button { 
                        id = "modalStarBtn"
                        classes = setOf("star-btn-modal")
                        title = "Star"
                        +"★"
                    }
                    button { 
                        id = "modalFlagBtn"
                        classes = setOf("flag-btn-modal")
                        title = "Flag"
                        +"⚑"
                    }
                    button { 
                        id = "fullscreenBtn"
                        title = "Fullscreen"
                        unsafe {
                            +"""<svg viewBox="0 0 24 24" width="24" height="24" stroke="currentColor" stroke-width="2" fill="none"><path d="M8 3H5a2 2 0 0 0-2 2v3m18 0V5a2 2 0 0 0-2-2h-3m0 18h3a2 2 0 0 0 2-2v-3M3 16v3a2 2 0 0 0 2 2h3"></path></svg>"""
                        }
                    }
                }
            }

            script {
                unsafe {
                    +"""
                        let currentPage = 1;
                        let pageSize = 24;
                        let showFavoritesOnly = false;
                        let showStarredOnly = false;
                        let showFlaggedOnly = false;
                        let currentSort = 'new';
                        let isRandom = true;
                        let allImagesData = [];
                        let currentIndex = -1;
                        let zoomLevel = 1;
                        let isLoading = false;
                        let hasMore = true;
                        let offsetX = 0;
                        let offsetY = 0;
                        let isDragging = false;
                        let startX, startY;
                        let evCache = [];
                        let prevDiff = -1;

                        const modal = document.getElementById('imgModal');
                        const modalImg = document.getElementById('modalImg');
                        const modalInfo = document.getElementById('modalInfo');
                        
                        document.getElementById('randomToggle').onchange = (e) => {
                            isRandom = e.target.checked;
                        };

                        function updateModal() {
                            const img = allImagesData[currentIndex];
                            if (!img) return;
                            modalImg.src = `/images/${"$"}{encodeURIComponent(img.name)}`;
                            modalInfo.innerText = `${"$"}{img.name} (${"$"}{img.width}x${"$"}{img.height})`;
                            
                            const favBtn = document.getElementById('modalFavBtn');
                            if (img.isFavorite) favBtn.classList.add('active');
                            else favBtn.classList.remove('active');

                            const starBtn = document.getElementById('modalStarBtn');
                            starBtn.innerText = img.starCount > 0 ? `★ ${"$"}{img.starCount}` : '★';
                            if (img.isFavorite) {
                                starBtn.style.display = 'flex';
                                if (img.starCount > 0) starBtn.classList.add('active');
                                else starBtn.classList.remove('active');
                            } else {
                                starBtn.style.display = 'none';
                            }

                            const flagBtn = document.getElementById('modalFlagBtn');
                            if (img.isFlagged) flagBtn.classList.add('active');
                            else flagBtn.classList.remove('active');

                            zoomLevel = 1;
                            offsetX = 0;
                            offsetY = 0;
                            applyZoom();
                        }

                        function openModal(index) {
                            currentIndex = index;
                            updateModal();
                            modal.classList.add('open');
                            document.body.style.overflow = 'hidden';
                        }

                        function closeModal() {
                            if (document.fullscreenElement) {
                                document.exitFullscreen();
                            }
                            modal.classList.remove('open');
                            document.body.style.overflow = '';
                        }

                        function applyZoom() {
                            modalImg.style.transform = `translate(${"$"}{offsetX}px, ${"$"}{offsetY}px) scale(${"$"}{zoomLevel})`;
                        }

                        document.getElementById('modalFavBtn').onclick = (e) => {
                            e.stopPropagation();
                            const img = allImagesData[currentIndex];
                            if (img) toggleFav(img.name, e.currentTarget);
                        };

                        document.getElementById('modalStarBtn').onclick = (e) => {
                            e.stopPropagation();
                            const img = allImagesData[currentIndex];
                            if (img) toggleStar(img.name, e.currentTarget);
                        };

                        document.getElementById('modalFlagBtn').onclick = (e) => {
                            e.stopPropagation();
                            const img = allImagesData[currentIndex];
                            if (img) toggleFlag(img.name, e.currentTarget);
                        };

                        document.getElementById('fullscreenBtn').onclick = (e) => {
                            e.stopPropagation();
                            if (!document.fullscreenElement) {
                                modal.requestFullscreen().catch(err => {
                                    alert(`Error attempting to enable full-screen mode: ${"$"}{err.message} (${"$"}{err.name})`);
                                });
                            } else {
                                document.exitFullscreen();
                            }
                        };
                        
                        modalImg.addEventListener('wheel', (e) => {
                            e.preventDefault();
                            const oldZoom = zoomLevel;
                            if (e.deltaY < 0) zoomLevel *= 1.05;
                            else zoomLevel = Math.max(0.1, zoomLevel / 1.05);
                            
                            // Adjust offset to zoom towards mouse pointer
                            const rect = modalImg.getBoundingClientRect();
                            const mouseX = e.clientX - rect.left - rect.width / 2;
                            const mouseY = e.clientY - rect.top - rect.height / 2;
                            
                            // Simplified zoom toward point logic
                            // (Actually, since we use transform-origin center, and scale is relative to that,
                            // if we want to zoom toward pointer we'd need more complex math or change origin.
                            // For now let's keep it simple or implement it properly)
                            
                            applyZoom();
                        }, { passive: false });

                        // Drag and Pinch to zoom support
                        modal.addEventListener('pointerdown', (e) => {
                            evCache.push(e);
                            if (evCache.length === 1) {
                                isDragging = true;
                                startX = e.clientX - offsetX;
                                startY = e.clientY - offsetY;
                                modalImg.style.cursor = 'grabbing';
                            }
                        });

                        modal.addEventListener('pointermove', (e) => {
                            const index = evCache.findIndex((cachedEv) => cachedEv.pointerId === e.pointerId);
                            if (index !== -1) {
                                evCache[index] = e;
                            }

                            if (evCache.length === 2) {
                                isDragging = false;
                                const curDiff = Math.hypot(evCache[0].clientX - evCache[1].clientX, evCache[0].clientY - evCache[1].clientY);

                                if (prevDiff > 0) {
                                    if (curDiff > prevDiff) {
                                        zoomLevel *= 1.02;
                                    } else if (curDiff < prevDiff) {
                                        zoomLevel = Math.max(0.1, zoomLevel / 1.02);
                                    }
                                    applyZoom();
                                }
                                prevDiff = curDiff;
                            } else if (isDragging && evCache.length === 1) {
                                offsetX = e.clientX - startX;
                                offsetY = e.clientY - startY;
                                applyZoom();
                            }
                        });

                        function remove_event(e) {
                            const index = evCache.findIndex((cachedEv) => cachedEv.pointerId === e.pointerId);
                            if (index !== -1) {
                                evCache.splice(index, 1);
                            }
                            if (evCache.length < 2) {
                                prevDiff = -1;
                            }
                            if (evCache.length === 0) {
                                isDragging = false;
                                modalImg.style.cursor = 'grab';
                            }
                        }

                        modal.addEventListener('pointerup', remove_event);
                        modal.addEventListener('pointercancel', remove_event);
                        modal.addEventListener('pointerout', remove_event);
                        modal.addEventListener('pointerleave', remove_event);

                        document.getElementById('modalNext').onclick = async (e) => {
                            e.stopPropagation();
                            if (currentIndex < allImagesData.length - 1) { 
                                currentIndex++; 
                                updateModal(); 
                            } else {
                                // Try to load next page
                                const response = await fetch(`/api/images?page=${"$"}{currentPage + 1}&pageSize=${"$"}{pageSize}&favorites=${"$"}{showFavoritesOnly}&starred=${"$"}{showStarredOnly}&flagged=${"$"}{showFlaggedOnly}&sort=${"$"}{currentSort}`);
                                const data = await response.json();
                                if (data.images && data.images.length > 0) {
                                    await loadImages(currentPage + 1);
                                    currentIndex = 0;
                                    updateModal();
                                }
                            }
                        };
                        document.getElementById('modalRandom').onclick = async (e) => {
                            e.stopPropagation();
                            const response = await fetch(`/api/images/random?favorites=${"$"}{showFavoritesOnly}&starred=${"$"}{showStarredOnly}&flagged=${"$"}{showFlaggedOnly}`);
                            if (response.ok) {
                                const img = await response.json();
                                // Check if it's already in allImagesData
                                let idx = allImagesData.findIndex(i => i.name === img.name);
                                if (idx === -1) {
                                    allImagesData.push(img);
                                    idx = allImagesData.length - 1;
                                }
                                currentIndex = idx;
                                updateModal();
                            }
                        };
                        document.getElementById('modalPrev').onclick = async (e) => {
                            e.stopPropagation();
                            if (currentIndex > 0) { 
                                currentIndex--; 
                                updateModal(); 
                            } else if (currentPage > 1) {
                                await loadImages(currentPage - 1);
                                currentIndex = allImagesData.length - 1;
                                updateModal();
                            }
                        };
                        document.getElementById('modalClose').onclick = closeModal;
                        modal.onclick = (e) => { if (e.target === modal || e.target.classList.contains('modal-content-wrapper')) closeModal(); };

                        document.addEventListener('keydown', (e) => {
                            if (!modal.classList.contains('open')) return;
                            if (e.key === 'Escape') closeModal();
                            if (e.key === 'ArrowLeft') document.getElementById('modalPrev').click();
                            if (e.key === 'ArrowRight') document.getElementById('modalNext').click();
                            if (e.key.toLowerCase() === 'r') document.getElementById('modalRandom').click();
                        });

                        async function toggleFav(imgName, btn) {
                            const isFav = !btn.classList.contains('active');
                            const resp = await fetch('/api/favorites', {
                                method: 'POST',
                                headers: { 'Content-Type': 'application/json' },
                                body: JSON.stringify({ path: imgName, isFavorite: isFav })
                            });
                            if (resp.ok) {
                                btn.classList.toggle('active');
                                const img = allImagesData.find(i => i.name === imgName);
                                if (img) {
                                    img.isFavorite = isFav;
                                    if (!isFav) img.starCount = 0;
                                }
                                
                                // Refresh current view if needed
                                if ((showFavoritesOnly && !isFav) || (showStarredOnly && !isFav)) {
                                    loadImages(currentPage);
                                } else {
                                    // Update star button visibility on the card
                                    const cards = document.querySelectorAll('.image-card');
                                    cards.forEach(card => {
                                        const cardImg = card.querySelector('img');
                                        if (cardImg && decodeURIComponent(cardImg.dataset.src.split('/').pop()) === imgName) {
                                            const starBtn = card.querySelector('.star-btn');
                                            if (isFav) starBtn.classList.add('visible');
                                            else {
                                                starBtn.classList.remove('visible');
                                                starBtn.classList.remove('active');
                                                starBtn.innerText = '★';
                                            }
                                            
                                            const favBtn = card.querySelector('.fav-btn');
                                            if (isFav) favBtn.classList.add('active');
                                            else favBtn.classList.remove('active');

                                            // Flag is independent
                                        }
                                    });
                                    // Update modal star button
                                    if (modal.classList.contains('open') && allImagesData[currentIndex].name === imgName) {
                                        updateModal();
                                    }
                                }
                            }
                        }

                        async function toggleStar(imgName, btn) {
                            const resp = await fetch('/api/stars', {
                                method: 'POST',
                                headers: { 'Content-Type': 'application/json' },
                                body: JSON.stringify({ path: imgName })
                            });
                            if (resp.ok) {
                                const img = allImagesData.find(i => i.name === imgName);
                                if (img) {
                                    img.starCount++;
                                    // Update UI
                                    const text = `★ ${"$"}{img.starCount}`;
                                    btn.innerText = text;
                                    btn.classList.add('active');
                                    
                                    // Update card UI
                                    const cards = document.querySelectorAll('.image-card');
                                    cards.forEach(card => {
                                        const cardImg = card.querySelector('img');
                                        if (cardImg && decodeURIComponent(cardImg.dataset.src.split('/').pop()) === imgName) {
                                            const starBtn = card.querySelector('.star-btn');
                                            starBtn.innerText = text;
                                            starBtn.classList.add('active');
                                        }
                                    });
                                }
                            }
                        }

                        async function toggleFlag(imgName, btn) {
                            const isFlagged = !btn.classList.contains('active');
                            const resp = await fetch('/api/flags', {
                                method: 'POST',
                                headers: { 'Content-Type': 'application/json' },
                                body: JSON.stringify({ path: imgName, isFlagged: isFlagged })
                            });
                            if (resp.ok) {
                                btn.classList.toggle('active');
                                const img = allImagesData.find(i => i.name === imgName);
                                if (img) {
                                    img.isFlagged = isFlagged;
                                }
                                
                                if (showFlaggedOnly && !isFlagged) {
                                    loadImages(currentPage);
                                } else {
                                    // Update modal
                                    if (modal.classList.contains('open') && allImagesData[currentIndex].name === imgName) {
                                        updateModal();
                                    }
                                }
                            }
                        }

                        async function loadImages(page, append = false) {
                            if (isLoading) return;
                            isLoading = true;

                        currentPage = page;
                            const list = document.getElementById('imageList');
                            
                            if (!append) {
                                list.innerHTML = Array(8).fill('<div class="image-card skeleton"></div>').join('');
                                allImagesData = [];
                                hasMore = true;
                            }

                            const response = await fetch(`/api/images?page=${"$"}{page}&pageSize=${"$"}{pageSize}&favorites=${"$"}{showFavoritesOnly}&starred=${"$"}{showStarredOnly}&flagged=${"$"}{showFlaggedOnly}&sort=${"$"}{currentSort}`);
                            const data = await response.json();
                            
                            if (!append) {
                                list.innerHTML = '';
                            }
                            
                            const startIndex = allImagesData.length;
                            allImagesData = allImagesData.concat(data.images);
                            
                            if (allImagesData.length === 0) {
                                list.innerHTML = '<div style="grid-column: 1/-1; text-align: center; padding: 40px;">No images found.</div>';
                            }

                            data.images.forEach((img, index) => {
                                const globalIndex = startIndex + index;
                                const card = document.createElement('div');
                                card.className = 'image-card';
                                card.innerHTML = `
                                    <img data-src="/images/${"$"}{encodeURIComponent(img.name)}" 
                                         src="data:image/gif;base64,R0lGODlhAQABAIAAAAAAAP///yH5BAEAAAAALAAAAAABAAEAAAIBRAA7" 
                                         class="lazy" alt="${"$"}{img.name}">
                                    <button class="fav-btn ${"$"}{img.isFavorite ? 'active' : ''}" title="Favorite">❤</button>
                                    <button class="star-btn ${"$"}{img.isFavorite ? 'visible' : ''} ${"$"}{img.starCount > 0 ? 'active' : ''}" title="Star">
                                        ${"$"}{img.starCount > 0 ? '★ ' + img.starCount : '★'}
                                    </button>
                                `;
                                
                                const imgEl = card.querySelector('img');
                                imgEl.onclick = () => openModal(globalIndex);
                                card.querySelector('.fav-btn').onclick = (e) => {
                                    e.stopPropagation();
                                    toggleFav(img.name, e.currentTarget);
                                };
                                card.querySelector('.star-btn').onclick = (e) => {
                                    e.stopPropagation();
                                    toggleStar(img.name, e.currentTarget);
                                };
                                list.appendChild(card);
                            });

                            // Intersection Observer for Lazy Loading
                            const observer = new IntersectionObserver((entries) => {
                                entries.forEach(entry => {
                                    if (entry.isIntersecting) {
                                        const img = entry.target;
                                        // Use thumbnail for gallery
                                        img.src = img.dataset.src + "?thumb=true";
                                        observer.unobserve(img);
                                    }
                                });
                            });
                            document.querySelectorAll('.lazy').forEach(img => observer.observe(img));

                            document.getElementById('pageInfo').innerText = `Page ${"$"}{page} of ${"$"}{Math.ceil(data.totalCount / pageSize) || 1}`;
                            document.getElementById('prevBtn').disabled = page === 1;
                            const isLastPage = page * pageSize >= data.totalCount;
                            document.getElementById('nextBtn').disabled = isLastPage;
                            hasMore = !isLastPage;
                            isLoading = false;
                        }

                        document.getElementById('prevBtn').onclick = () => { if (currentPage > 1) loadImages(currentPage - 1); };
                        document.getElementById('nextBtn').onclick = () => { loadImages(currentPage + 1); };
                        
                        document.getElementById('allLink').onclick = (e) => { 
                            e.preventDefault(); showFavoritesOnly = false; showStarredOnly = false; showFlaggedOnly = false; loadImages(1); 
                        };
                        document.getElementById('favsLink').onclick = (e) => { 
                            e.preventDefault(); showFavoritesOnly = true; showStarredOnly = false; showFlaggedOnly = false; loadImages(1); 
                        };
                        document.getElementById('starredLink').onclick = (e) => { 
                            e.preventDefault(); showFavoritesOnly = false; showStarredOnly = true; showFlaggedOnly = false; loadImages(1); 
                        };
                        document.getElementById('flaggedLink').onclick = (e) => { 
                            e.preventDefault(); showFavoritesOnly = false; showStarredOnly = false; showFlaggedOnly = true; loadImages(1); 
                        };

                        // Infinite Scroll implementation
                        const sentinel = document.getElementById('sentinel');
                        const infiniteObserver = new IntersectionObserver((entries) => {
                            if (entries[0].isIntersecting && hasMore && !isLoading) {
                                loadImages(currentPage + 1, true);
                            }
                        }, { rootMargin: '200px' });
                        infiniteObserver.observe(sentinel);

                        document.getElementById('sortSelect').onchange = (e) => {
                            currentSort = e.target.value;
                            loadImages(1);
                        };

                        loadImages(1);
                    """
                }
            }
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
