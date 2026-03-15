let currentPage = 1;
let pageSize = 24;
let showFavoritesOnly = false;
let showStarredOnly = false;
let showFlaggedOnly = false;
let currentSort = 'new';
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

const isDemo = window.isDemoMode || false;

const modal = document.getElementById('imgModal');
const modalImg = document.getElementById('modalImg');
const modalInfo = document.getElementById('modalInfo');

// Mock API for Demo Mode
const demoApi = {
    getImages: async (params) => {
        const page = parseInt(params.get('page')) || 1;
        const pageSize = parseInt(params.get('pageSize')) || 24;
        const favs = params.get('favorites') === 'true';
        const starred = params.get('starred') === 'true';
        const flagged = params.get('flagged') === 'true';
        const sort = params.get('sort') || 'new';

        let images = [...(window.demoImages || [])];
        
        // Apply localStorage states
        images = images.map(img => {
            const state = JSON.parse(localStorage.getItem('img_' + img.name) || '{}');
            return { ...img, ...state };
        });

        if (favs) images = images.filter(i => i.isFavorite);
        if (starred) images = images.filter(i => i.starCount > 0);
        if (flagged) images = images.filter(i => i.isFlagged);

        if (sort === 'name') images.sort((a, b) => a.name.localeCompare(b.name));
        else if (sort === 'old') images.reverse();

        const start = (page - 1) * pageSize;
        const pagedImages = images.slice(start, start + pageSize);

        return {
            images: pagedImages,
            totalCount: images.length
        };
    },
    toggleFav: async (imgName, isFav) => {
        const state = JSON.parse(localStorage.getItem('img_' + imgName) || '{}');
        state.isFavorite = isFav;
        if (!isFav) state.starCount = 0;
        localStorage.setItem('img_' + imgName, JSON.stringify(state));
        return { ok: true };
    },
    toggleStar: async (imgName) => {
        const state = JSON.parse(localStorage.getItem('img_' + imgName) || '{}');
        state.starCount = (state.starCount || 0) + 1;
        localStorage.setItem('img_' + imgName, JSON.stringify(state));
        return { ok: true };
    },
    toggleFlag: async (imgName, isFlagged) => {
        const state = JSON.parse(localStorage.getItem('img_' + imgName) || '{}');
        state.isFlagged = isFlagged;
        localStorage.setItem('img_' + imgName, JSON.stringify(state));
        return { ok: true };
    },
    getRandom: async () => {
        const images = window.demoImages || [];
        const img = images[Math.floor(Math.random() * images.length)];
        const state = JSON.parse(localStorage.getItem('img_' + img.name) || '{}');
        return { ...img, ...state };
    }
};

async function apiFetch(url, options = {}) {
    if (!isDemo) return fetch(url, options);

    const urlObj = new URL(url, window.location.origin);
    if (urlObj.pathname === '/api/images') {
        const data = await demoApi.getImages(urlObj.searchParams);
        return { ok: true, json: async () => data };
    }
    if (urlObj.pathname === '/api/favorites') {
        const body = JSON.parse(options.body);
        const data = await demoApi.toggleFav(body.path, body.isFavorite);
        return { ok: true, json: async () => data };
    }
    if (urlObj.pathname === '/api/stars') {
        const body = JSON.parse(options.body);
        const data = await demoApi.toggleStar(body.path);
        return { ok: true, json: async () => data };
    }
    if (urlObj.pathname === '/api/flags') {
        const body = JSON.parse(options.body);
        const data = await demoApi.toggleFlag(body.path, body.isFlagged);
        return { ok: true, json: async () => data };
    }
    if (urlObj.pathname === '/api/images/random') {
        const data = await demoApi.getRandom();
        return { ok: true, json: async () => data };
    }
    return { ok: false };
}

function getImageUrl(imgName) {
    return isDemo ? `sample-pics/${encodeURIComponent(imgName)}` : `/images/${encodeURIComponent(imgName)}`;
}

function updateModal() {
    const img = allImagesData[currentIndex];
    if (!img) return;
    modalImg.src = getImageUrl(img.name);
    modalInfo.innerText = `${img.name} (${img.width}x${img.height})`;
    
    const favBtn = document.getElementById('modalFavBtn');
    if (img.isFavorite) favBtn.classList.add('active');
    else favBtn.classList.remove('active');

    const starBtn = document.getElementById('modalStarBtn');
    const starCountLabel = document.getElementById('starCountLabel');
    if (img.starCount > 0) {
        starCountLabel.innerText = img.starCount;
        starCountLabel.style.display = 'inline';
    } else {
        starCountLabel.style.display = 'none';
    }

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
    modalImg.style.transform = `translate(${offsetX}px, ${offsetY}px) scale(${zoomLevel})`;
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

if (document.getElementById('fullscreenBtn')) {
    document.getElementById('fullscreenBtn').onclick = (e) => {
        e.stopPropagation();
        if (!document.fullscreenElement) {
            modal.requestFullscreen().catch(err => {
                alert(`Error attempting to enable full-screen mode: ${err.message} (${err.name})`);
            });
        } else {
            document.exitFullscreen();
        }
    };
}

modalImg.addEventListener('wheel', (e) => {
    e.preventDefault();
    if (e.deltaY < 0) zoomLevel *= 1.05;
    else zoomLevel = Math.max(0.1, zoomLevel / 1.05);
    applyZoom();
}, { passive: false });

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
        const response = await apiFetch(`/api/images?page=${currentPage + 1}&pageSize=${pageSize}&favorites=${showFavoritesOnly}&starred=${showStarredOnly}&flagged=${showFlaggedOnly}&sort=${currentSort}`);
        const data = await response.json();
        if (data.images && data.images.length > 0) {
            await loadImages(currentPage + 1, true);
            currentIndex++;
            updateModal();
        }
    }
};
document.getElementById('modalRandom').onclick = async (e) => {
    e.stopPropagation();
    const response = await apiFetch(`/api/images/random?favorites=${showFavoritesOnly}&starred=${showStarredOnly}&flagged=${showFlaggedOnly}`);
    if (response.ok) {
        const img = await response.json();
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
    const resp = await apiFetch('/api/favorites', {
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
        
        if ((showFavoritesOnly && !isFav) || (showStarredOnly && !isFav)) {
            loadImages(currentPage);
        } else {
            const cards = document.querySelectorAll('.image-card');
            cards.forEach(card => {
                const cardImg = card.querySelector('img');
                if (cardImg && cardImg.alt === imgName) {
                    const starBtn = card.querySelector('.star-btn');
                    if (isFav) starBtn.classList.add('visible');
                    else {
                        starBtn.classList.remove('visible');
                        starBtn.classList.remove('active');
                        const label = starBtn.querySelector('.star-count-label');
                        if (label) label.innerText = '';
                    }
                    
                    const favBtn = card.querySelector('.fav-btn');
                    if (isFav) favBtn.classList.add('active');
                    else favBtn.classList.remove('active');
                }
            });
            if (modal.classList.contains('open') && allImagesData[currentIndex].name === imgName) {
                updateModal();
            }
        }
    }
}

async function toggleStar(imgName, btn) {
    const resp = await apiFetch('/api/stars', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ path: imgName })
    });
    if (resp.ok) {
        const img = allImagesData.find(i => i.name === imgName);
        if (img) {
            img.starCount = (img.starCount || 0) + 1;
            
            const starCountLabel = document.getElementById('starCountLabel');
            if (starCountLabel) {
                starCountLabel.innerText = img.starCount;
                starCountLabel.style.display = 'inline';
            }
            btn.classList.add('active');
            
            const cards = document.querySelectorAll('.image-card');
            cards.forEach(card => {
                const cardImg = card.querySelector('img');
                if (cardImg && cardImg.alt === imgName) {
                    const starBtn = card.querySelector('.star-btn');
                    const label = starBtn.querySelector('.star-count-label');
                    if (label) label.innerText = img.starCount;
                    starBtn.classList.add('active');
                }
            });
        }
    }
}

async function toggleFlag(imgName, btn) {
    const isFlagged = !btn.classList.contains('active');
    const resp = await apiFetch('/api/flags', {
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

    const response = await apiFetch(`/api/images?page=${page}&pageSize=${pageSize}&favorites=${showFavoritesOnly}&starred=${showStarredOnly}&flagged=${showFlaggedOnly}&sort=${currentSort}`);
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
            <img data-src="${getImageUrl(img.name)}" 
                 src="data:image/gif;base64,R0lGODlhAQABAIAAAAAAAP///yH5BAEAAAAALAAAAAABAAEAAAIBRAA7" 
                 class="lazy" alt="${img.name}">
            <button class="fav-btn ${img.isFavorite ? 'active' : ''}" title="Favorite">
                <svg viewBox="0 0 24 24" width="20" height="20" stroke="black" stroke-width="2.5" fill="none" stroke-linecap="round" stroke-linejoin="round"><path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l8.72-8.72 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"></path></svg>
            </button>
            <button class="star-btn ${img.isFavorite ? 'visible' : ''} ${img.starCount > 0 ? 'active' : ''}" title="Star">
                <svg viewBox="0 0 24 24" width="16" height="16" stroke="black" stroke-width="2.5" fill="none" stroke-linecap="round" stroke-linejoin="round"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"></polygon></svg>
                <span class="star-count-label" style="margin-left: 2px;">${img.starCount > 0 ? img.starCount : ''}</span>
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

    const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                const img = entry.target;
                img.src = img.dataset.src + (isDemo ? "" : "?thumb=true");
                observer.unobserve(img);
            }
        });
    });
    document.querySelectorAll('.lazy').forEach(img => observer.observe(img));

    if (document.getElementById('pageInfo')) {
        document.getElementById('pageInfo').innerText = `Page ${page} of ${Math.ceil(data.totalCount / pageSize) || 1}`;
    }
    if (document.getElementById('prevBtn')) {
        document.getElementById('prevBtn').disabled = page === 1;
    }
    const isLastPage = page * pageSize >= data.totalCount;
    if (document.getElementById('nextBtn')) {
        document.getElementById('nextBtn').disabled = isLastPage;
    }
    hasMore = !isLastPage;
    isLoading = false;
}

if (document.getElementById('prevBtn')) document.getElementById('prevBtn').onclick = () => { if (currentPage > 1) loadImages(currentPage - 1); };
if (document.getElementById('nextBtn')) document.getElementById('nextBtn').onclick = () => { loadImages(currentPage + 1); };

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

const sentinel = document.getElementById('sentinel');
if (sentinel) {
    const infiniteObserver = new IntersectionObserver((entries) => {
        if (entries[0].isIntersecting && hasMore && !isLoading) {
            loadImages(currentPage + 1, true);
        }
    }, { rootMargin: '200px' });
    infiniteObserver.observe(sentinel);
}

if (document.getElementById('sortSelect')) {
    document.getElementById('sortSelect').onchange = (e) => {
        currentSort = e.target.value;
        loadImages(1);
    };
}

loadImages(1);
