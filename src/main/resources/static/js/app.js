// Global app configuration and utilities
const API_BASE_URL = '/api';

// Utility functions
const utils = {
    // Format date to Korean locale
    formatDate(dateString) {
        const date = new Date(dateString);
        return date.toLocaleDateString('ko-KR', {
            year: 'numeric',
            month: 'long',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        });
    },

    // Format relative time
    formatRelativeTime(dateString) {
        const date = new Date(dateString);
        const now = new Date();
        const diffInSeconds = Math.floor((now - date) / 1000);

        if (diffInSeconds < 60) {
            return '방금 전';
        } else if (diffInSeconds < 3600) {
            const minutes = Math.floor(diffInSeconds / 60);
            return `${minutes}분 전`;
        } else if (diffInSeconds < 86400) {
            const hours = Math.floor(diffInSeconds / 3600);
            return `${hours}시간 전`;
        } else if (diffInSeconds < 2592000) {
            const days = Math.floor(diffInSeconds / 86400);
            return `${days}일 전`;
        } else {
            return this.formatDate(dateString);
        }
    },

    // Show loading state
    showLoading(element, message = '로딩 중...') {
        element.innerHTML = `
            <div class="loading">
                <i class="fas fa-spinner fa-spin"></i>
                ${message}
            </div>
        `;
    },

    // Show error message
    showError(element, message = '오류가 발생했습니다.') {
        element.innerHTML = `
            <div class="error">
                <i class="fas fa-exclamation-triangle"></i>
                ${message}
            </div>
        `;
    },

    // Show success message
    showSuccess(element, message) {
        element.innerHTML = `
            <div class="success">
                <i class="fas fa-check-circle"></i>
                ${message}
            </div>
        `;
    },

    // Truncate text
    truncateText(text, maxLength = 100) {
        if (text.length <= maxLength) return text;
        return text.substring(0, maxLength) + '...';
    },

    // Get URL parameters
    getUrlParams() {
        const params = new URLSearchParams(window.location.search);
        const result = {};
        for (const [key, value] of params) {
            result[key] = value;
        }
        return result;
    },

    // Navigate to URL
    navigateTo(url) {
        window.location.href = url;
    },

    // Debounce function
    debounce(func, wait) {
        let timeout;
        return function executedFunction(...args) {
            const later = () => {
                clearTimeout(timeout);
                func(...args);
            };
            clearTimeout(timeout);
            timeout = setTimeout(later, wait);
        };
    }
};

// API service
const api = {
    // Generic API call
    async call(endpoint, options = {}) {
        const url = `${API_BASE_URL}${endpoint}`;
        const config = {
            headers: {
                'Content-Type': 'application/json',
                ...options.headers
            },
            ...options
        };

        try {
            const response = await fetch(url, config);
            
            if (!response.ok) {
                const errorData = await response.json().catch(() => null);
                const errorMessage = errorData?.message || `HTTP error! status: ${response.status}`;
                console.error('API Error:', errorMessage, errorData);
                throw new Error(errorMessage);
            }

            // Handle empty responses
            const contentType = response.headers.get('content-type');
            if (contentType && contentType.includes('application/json')) {
                return await response.json();
            } else {
                return null;
            }
        } catch (error) {
            console.error('API call failed:', error);
            throw error;
        }
    },

    // Posts API
    posts: {
        getAll() {
            return api.call('/posts');
        },

        getById(id) {
            return api.call(`/posts/${id}`);
        },

        create(data) {
            return api.call('/posts', {
                method: 'POST',
                body: JSON.stringify(data)
            });
        },

        createWithImages(formData) {
            return api.call('/posts', {
                method: 'POST',
                headers: {}, // Let browser set content-type for FormData
                body: formData
            });
        },

        update(id, data) {
            return api.call(`/posts/${id}`, {
                method: 'PUT',
                body: JSON.stringify(data)
            });
        },

        delete(id) {
            return api.call(`/posts/${id}`, {
                method: 'DELETE'
            });
        },

        search(params) {
            const queryString = new URLSearchParams(params).toString();
            return api.call(`/posts/search?${queryString}`);
        },

        getByCategory(categoryId) {
            return api.call(`/posts/category/${categoryId}`);
        }
    },

    // Comments API
    comments: {
        getForPost(postId) {
            return api.call(`/comments/posts/${postId}`);
        },

        create(data) {
            return api.call('/comments', {
                method: 'POST',
                body: JSON.stringify(data)
            });
        },

        createReply(data) {
            return api.call('/comments/replies', {
                method: 'POST',
                body: JSON.stringify(data)
            });
        },

        update(id, data) {
            return api.call(`/comments/${id}`, {
                method: 'PUT',
                body: JSON.stringify(data)
            });
        },

        delete(id, authorId) {
            return api.call(`/comments/${id}?authorId=${authorId}`, {
                method: 'DELETE'
            });
        }
    },

    // Likes API
    likes: {
        toggle(postId, authorName) {
            return api.call(`/likes/posts/${postId}/users/${encodeURIComponent(authorName)}/toggle`, {
                method: 'PUT'
            });
        },

        getStatus(postId, authorName) {
            return api.call(`/likes/posts/${postId}/users/${encodeURIComponent(authorName)}/status`);
        },

        getCount(postId) {
            return api.call(`/likes/posts/${postId}/count`);
        }
    },

    // Users API
    users: {
        getAll() {
            return api.call('/users');
        },

        getById(id) {
            return api.call(`/users/${id}`);
        }
    },

    // Categories API
    categories: {
        getAll() {
            return api.call('/categories');
        },

        getById(id) {
            return api.call(`/categories/${id}`);
        }
    }
};

// Local storage utilities
const storage = {
    get(key) {
        try {
            const item = localStorage.getItem(key);
            return item ? JSON.parse(item) : null;
        } catch (error) {
            console.error('Error reading from localStorage:', error);
            return null;
        }
    },

    set(key, value) {
        try {
            localStorage.setItem(key, JSON.stringify(value));
        } catch (error) {
            console.error('Error writing to localStorage:', error);
        }
    },

    remove(key) {
        try {
            localStorage.removeItem(key);
        } catch (error) {
            console.error('Error removing from localStorage:', error);
        }
    }
};

// Initialize app when DOM is loaded
document.addEventListener('DOMContentLoaded', function() {
    // Load recent posts on home page
    if (window.location.pathname === '/' || window.location.pathname === '/index.html') {
        loadRecentPosts();
    }

    // Set active navigation link
    setActiveNavLink();
});

// Load recent posts for home page
async function loadRecentPosts() {
    const container = document.getElementById('recent-posts-container');
    if (!container) return;

    try {
        utils.showLoading(container, '최근 게시글을 불러���는 중...');
        
        const posts = await api.posts.getAll();
        
        if (posts && posts.length > 0) {
            // Show only first 5 posts
            const recentPosts = posts.slice(0, 5);
            
            container.innerHTML = recentPosts.map(post => `
                <div class="post-preview">
                    <div class="post-preview-header">
                        <a href="/post-detail.html?id=${post.id}" class="post-preview-title">
                            ${post.title}
                        </a>
                    </div>
                    <div class="post-preview-meta">
                        <span><i class="fas fa-user"></i> ${post.author.displayName}</span>
                        <span><i class="fas fa-folder"></i> ${post.category.name}</span>
                        <span><i class="fas fa-clock"></i> ${utils.formatRelativeTime(post.createdAt)}</span>
                    </div>
                    <div class="post-preview-stats">
                        ${post.imageCount > 0 ? `<span><i class="fas fa-images"></i> ${post.imageCount}</span>` : ''}
                        <span><i class="fas fa-heart"></i> ${post.likeCount}</span>
                    </div>
                </div>
            `).join('');
        } else {
            container.innerHTML = `
                <div class="loading">
                    <i class="fas fa-info-circle"></i>
                    아직 게시글이 없습니다.
                </div>
            `;
        }
    } catch (error) {
        console.error('Failed to load recent posts:', error);
        utils.showError(container, '최근 게시글을 불러오는데 실패했습니다.');
    }
}

// Set active navigation link
function setActiveNavLink() {
    const currentPath = window.location.pathname;
    const navLinks = document.querySelectorAll('.nav-link');
    
    navLinks.forEach(link => {
        link.classList.remove('active');
        
        const href = link.getAttribute('href');
        if (href === currentPath || 
            (currentPath === '/' && href === '/') ||
            (currentPath === '/index.html' && href === '/')) {
            link.classList.add('active');
        }
    });
}

// Global error handler
window.addEventListener('error', function(event) {
    console.error('Global error:', event.error);
});

// Global unhandled promise rejection handler
window.addEventListener('unhandledrejection', function(event) {
    console.error('Unhandled promise rejection:', event.reason);
});

// Export utilities for use in other scripts
window.utils = utils;
window.api = api;
window.storage = storage;
