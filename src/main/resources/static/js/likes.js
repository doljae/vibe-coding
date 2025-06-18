// Likes functionality
class LikesManager {
    constructor() {
        this.currentAuthorName = this.getCurrentAuthorName();
        this.init();
    }

    init() {
        this.setupEventListeners();
        this.loadLikeStatus();
    }

    getCurrentAuthorName() {
        // Use authorName from storage, consistent with comments system
        let authorName = storage.get('authorName');
        if (!authorName) {
            // Fallback to temporary user ID if no author name is set
            authorName = 'user-' + Math.random().toString(36).substr(2, 9);
            storage.set('authorName', authorName);
        }
        return authorName;
    }

    setupEventListeners() {
        // Like button click
        document.addEventListener('click', (e) => {
            if (e.target.closest('.like-btn')) {
                const likeBtn = e.target.closest('.like-btn');
                const postId = likeBtn.dataset.postId;
                if (postId) {
                    this.toggleLike(postId);
                }
            }
        });
    }

    async loadLikeStatus() {
        const likeBtn = document.querySelector('.like-btn');
        if (!likeBtn) return;

        const postId = likeBtn.dataset.postId;
        if (!postId) return;

        try {
            // Get current like status
            const statusResponse = await api.likes.getStatus(postId, this.currentAuthorName);
            const isLiked = statusResponse.hasLiked;

            // Get current like count
            const countResponse = await api.likes.getCount(postId);
            const likeCount = countResponse.count;

            this.updateLikeButton(likeBtn, isLiked, likeCount);

        } catch (error) {
            console.error('Failed to load like status:', error);
            // Don't show error to user for like status loading
        }
    }

    async toggleLike(postId) {
        const likeBtn = document.querySelector(`[data-post-id="${postId}"]`);
        if (!likeBtn) return;

        // Check if user name is set, prompt if not
        let authorName = storage.get('authorName');
        if (!authorName || authorName.startsWith('user-')) {
            authorName = prompt('\uc88b\uc544\uc694\ub97c \ub204\ub974\ub824\uba74 \uc774\ub984\uc744 \uc785\ub825\ud574\uc8fc\uc138\uc694:');
            if (!authorName || authorName.trim() === '') {
                return; // User cancelled or entered empty name
            }
            authorName = authorName.trim();
            storage.set('authorName', authorName);
            this.currentAuthorName = authorName;
        }

        // Disable button during request
        likeBtn.disabled = true;
        likeBtn.classList.add('loading');

        try {
            // Make sure we're using the correct endpoint
            const response = await api.likes.toggle(postId, this.currentAuthorName);
            const isLiked = response.isLiked;

            // Get updated like count
            const countResponse = await api.likes.getCount(postId);
            const likeCount = countResponse.count;

            this.updateLikeButton(likeBtn, isLiked, likeCount);
            
            // Show feedback
            if (isLiked) {
                this.showLikeFeedback('\uc88b\uc544\uc694\ub97c \ub20c\ub800\uc2b5\ub2c8\ub2e4! \u2764\ufe0f');
            } else {
                this.showLikeFeedback('\uc88b\uc544\uc694\ub97c \ucde8\uc18c\ud588\uc2b5\ub2c8\ub2e4.');
            }

        } catch (error) {
            console.error('Failed to toggle like:', error);
            this.showLikeFeedback('\uc88b\uc544\uc694 \ucc98\ub9ac\uc5d0 \uc2e4\ud328\ud588\uc2b5\ub2c8\ub2e4.', 'error');
        } finally {
            likeBtn.disabled = false;
            likeBtn.classList.remove('loading');
        }
    }

    updateLikeButton(likeBtn, isLiked, likeCount) {
        const icon = likeBtn.querySelector('i');
        const countSpan = likeBtn.querySelector('#like-count');

        if (isLiked) {
            likeBtn.classList.add('liked');
            if (icon) {
                icon.className = 'fas fa-heart';
            }
        } else {
            likeBtn.classList.remove('liked');
            if (icon) {
                icon.className = 'far fa-heart';
            }
        }

        if (countSpan) {
            countSpan.textContent = likeCount;
        }

        // Add animation class
        likeBtn.classList.add('like-animation');
        setTimeout(() => {
            likeBtn.classList.remove('like-animation');
        }, 300);
    }

    showLikeFeedback(message, type = 'success') {
        // Create feedback element
        const feedback = document.createElement('div');
        feedback.className = `like-feedback ${type}`;
        feedback.textContent = message;
        
        // Position near the like button
        const likeBtn = document.querySelector('.like-btn');
        if (likeBtn) {
            const rect = likeBtn.getBoundingClientRect();
            feedback.style.position = 'fixed';
            feedback.style.left = `${rect.left}px`;
            feedback.style.top = `${rect.top - 40}px`;
            feedback.style.zIndex = '1002';
        }
        
        document.body.appendChild(feedback);
        
        // Show feedback
        setTimeout(() => {
            feedback.classList.add('show');
        }, 100);
        
        // Hide and remove feedback
        setTimeout(() => {
            feedback.classList.remove('show');
            setTimeout(() => {
                if (document.body.contains(feedback)) {
                    document.body.removeChild(feedback);
                }
            }, 300);
        }, 2000);
    }
}

// Additional CSS for likes
const likesStyles = `
<style>
.like-btn {
    background: none;
    border: 1px solid #e5e7eb;
    color: #6b7280;
    cursor: pointer;
    display: flex;
    align-items: center;
    gap: 0.5rem;
    padding: 0.75rem 1rem;
    border-radius: 0.5rem;
    transition: all 0.2s;
    font-size: 1rem;
    position: relative;
    overflow: hidden;
}

.like-btn:hover {
    background-color: #fef2f2;
    border-color: #fecaca;
    color: #dc2626;
}

.like-btn.liked {
    background-color: #fef2f2;
    border-color: #dc2626;
    color: #dc2626;
}

.like-btn.liked i {
    color: #dc2626;
}

.like-btn:disabled {
    opacity: 0.6;
    cursor: not-allowed;
}

.like-btn.loading::after {
    content: '';
    position: absolute;
    top: 0;
    left: -100%;
    width: 100%;
    height: 100%;
    background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.4), transparent);
    animation: loading-shimmer 1.5s infinite;
}

.like-btn.like-animation {
    transform: scale(1.1);
}

.like-btn.liked.like-animation i {
    animation: heartBeat 0.6s ease-in-out;
}

@keyframes loading-shimmer {
    0% {
        left: -100%;
    }
    100% {
        left: 100%;
    }
}

@keyframes heartBeat {
    0% {
        transform: scale(1);
    }
    25% {
        transform: scale(1.3);
    }
    50% {
        transform: scale(1.1);
    }
    75% {
        transform: scale(1.25);
    }
    100% {
        transform: scale(1);
    }
}

.like-feedback {
    background: #059669;
    color: white;
    padding: 0.5rem 1rem;
    border-radius: 0.5rem;
    font-size: 0.875rem;
    box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
    transform: translateY(10px);
    opacity: 0;
    transition: all 0.3s ease;
    pointer-events: none;
    white-space: nowrap;
}

.like-feedback.error {
    background: #dc2626;
}

.like-feedback.show {
    transform: translateY(0);
    opacity: 1;
}

/* Like button variations for different contexts */
.like-btn-small {
    padding: 0.5rem 0.75rem;
    font-size: 0.875rem;
}

.like-btn-large {
    padding: 1rem 1.5rem;
    font-size: 1.125rem;
}

/* Like button in post cards */
.post-card .like-btn {
    border: none;
    background: none;
    padding: 0.25rem 0.5rem;
    font-size: 0.875rem;
}

.post-card .like-btn:hover {
    background-color: #fef2f2;
}

/* Like button in post preview */
.post-preview .like-btn {
    border: none;
    background: none;
    padding: 0.25rem 0.5rem;
    font-size: 0.875rem;
}

.post-preview .like-btn:hover {
    background-color: #fef2f2;
}

/* Responsive adjustments */
@media (max-width: 768px) {
    .like-btn {
        padding: 0.625rem 0.875rem;
        font-size: 0.875rem;
    }
    
    .like-feedback {
        position: fixed !important;
        left: 50% !important;
        top: 50% !important;
        transform: translate(-50%, -50%) translateY(10px);
    }
    
    .like-feedback.show {
        transform: translate(-50%, -50%) translateY(0);
    }
}

/* Accessibility improvements */
.like-btn:focus {
    outline: 2px solid #2563eb;
    outline-offset: 2px;
}

.like-btn:focus:not(:focus-visible) {
    outline: none;
}

/* High contrast mode support */
@media (prefers-contrast: high) {
    .like-btn {
        border-width: 2px;
    }
    
    .like-btn.liked {
        background-color: #dc2626;
        color: white;
    }
}

/* Reduced motion support */
@media (prefers-reduced-motion: reduce) {
    .like-btn,
    .like-feedback {
        transition: none;
    }
    
    .like-btn.like-animation,
    .like-btn.liked.like-animation i {
        animation: none;
        transform: none;
    }
}
</style>
`;

// Initialize likes manager
let likesManager;

document.addEventListener('DOMContentLoaded', function() {
    // Add likes styles
    document.head.insertAdjacentHTML('beforeend', likesStyles);
    
    // Initialize likes manager
    likesManager = new LikesManager();
});
