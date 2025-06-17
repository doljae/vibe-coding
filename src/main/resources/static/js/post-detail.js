// Post detail page functionality
class PostDetailPage {
    constructor() {
        this.postId = null;
        this.post = null;
        this.currentUserId = this.getCurrentUserId(); // Mock user ID for demo
        
        this.init();
    }

    async init() {
        this.postId = this.getPostIdFromUrl();
        
        if (!this.postId) {
            this.showError('게시글 ID가 없습니다.');
            return;
        }

        await this.loadPost();
        this.setupEventListeners();
    }

    getPostIdFromUrl() {
        const params = utils.getUrlParams();
        return params.id;
    }

    getCurrentUserId() {
        // In a real application, this would come from authentication
        // For demo purposes, we'll use a mock user ID
        let userId = storage.get('currentUserId');
        if (!userId) {
            userId = 'user-' + Math.random().toString(36).substr(2, 9);
            storage.set('currentUserId', userId);
        }
        return userId;
    }

    async loadPost() {
        const container = document.getElementById('post-container');
        
        try {
            utils.showLoading(container, '게시글을 불러오는 중...');
            
            this.post = await api.posts.getById(this.postId);
            
            this.renderPost();
            this.showCommentsSection();
            
            // Update page title
            document.title = `${this.post.title} - Vibe Coding`;
            
        } catch (error) {
            console.error('Failed to load post:', error);
            utils.showError(container, '게시글을 불러오는데 실패했습니다.');
        }
    }

    renderPost() {
        const container = document.getElementById('post-container');
        
        container.innerHTML = `
            <article class="post-detail">
                <header class="post-header">
                    <div class="post-category">
                        <span class="category-badge">${this.post.category.name}</span>
                    </div>
                    <h1 class="post-title">${this.post.title}</h1>
                    <div class="post-meta">
                        <div class="post-author">
                            <i class="fas fa-user"></i>
                            <span>${this.post.author.displayName}</span>
                        </div>
                        <div class="post-date">
                            <i class="fas fa-clock"></i>
                            <span>${utils.formatDate(this.post.createdAt)}</span>
                        </div>
                        ${this.post.updatedAt !== this.post.createdAt ? `
                            <div class="post-updated">
                                <i class="fas fa-edit"></i>
                                <span>수정됨: ${utils.formatDate(this.post.updatedAt)}</span>
                            </div>
                        ` : ''}
                    </div>
                </header>

                <div class="post-content">
                    <div class="post-text">
                        ${this.formatPostContent(this.post.content)}
                    </div>
                    
                    ${this.post.imageAttachments && this.post.imageAttachments.length > 0 ? `
                        <div class="post-images">
                            <h4>첨부 이미지</h4>
                            <div class="image-gallery">
                                ${this.post.imageAttachments.map(image => `
                                    <div class="image-item" data-image-id="${image.id}">
                                        <img src="/api/posts/${this.postId}/images/${image.id}" 
                                             alt="${image.filename}"
                                             class="post-image"
                                             onclick="postDetailPage.openImageModal('${image.id}', '${image.filename}')">
                                        <div class="image-info">
                                            <span class="image-filename">${image.filename}</span>
                                        </div>
                                    </div>
                                `).join('')}
                            </div>
                        </div>
                    ` : ''}
                </div>

                <footer class="post-footer">
                    <div class="post-actions">
                        <button id="like-btn" class="like-btn" data-post-id="${this.postId}">
                            <i class="fas fa-heart"></i>
                            <span id="like-count">${this.post.likeCount}</span>
                        </button>
                        <button class="share-btn" onclick="postDetailPage.sharePost()">
                            <i class="fas fa-share"></i>
                            공유하기
                        </button>
                    </div>
                    
                    <div class="post-management">
                        <a href="/post-form.html?id=${this.postId}" class="btn btn-outline btn-sm">
                            <i class="fas fa-edit"></i>
                            수정
                        </a>
                        <button onclick="postDetailPage.deletePost()" class="btn btn-danger btn-sm">
                            <i class="fas fa-trash"></i>
                            삭제
                        </button>
                    </div>
                </footer>
            </article>
        `;
    }

    formatPostContent(content) {
        // Convert line breaks to HTML
        return content.replace(/\n/g, '<br>');
    }

    showCommentsSection() {
        const commentsSection = document.getElementById('comments-section');
        if (commentsSection) {
            commentsSection.style.display = 'block';
        }
    }

    setupEventListeners() {
        // Image modal close
        const closeImageModal = document.getElementById('close-image-modal');
        if (closeImageModal) {
            closeImageModal.addEventListener('click', () => this.closeImageModal());
        }

        // Click outside modal to close
        const imageModal = document.getElementById('image-modal');
        if (imageModal) {
            imageModal.addEventListener('click', (e) => {
                if (e.target === imageModal) {
                    this.closeImageModal();
                }
            });
        }

        // Escape key to close modal
        document.addEventListener('keydown', (e) => {
            if (e.key === 'Escape') {
                this.closeImageModal();
            }
        });
    }

    openImageModal(imageId, filename) {
        const modal = document.getElementById('image-modal');
        const modalImage = document.getElementById('modal-image');
        const modalTitle = document.getElementById('image-modal-title');
        
        if (modal && modalImage && modalTitle) {
            modalImage.src = `/api/posts/${this.postId}/images/${imageId}`;
            modalImage.alt = filename;
            modalTitle.textContent = filename;
            modal.style.display = 'flex';
            
            // Prevent body scroll
            document.body.style.overflow = 'hidden';
        }
    }

    closeImageModal() {
        const modal = document.getElementById('image-modal');
        if (modal) {
            modal.style.display = 'none';
            document.body.style.overflow = 'auto';
        }
    }

    async sharePost() {
        const url = window.location.href;
        const title = this.post.title;
        const text = `${title} - Vibe Coding`;

        if (navigator.share) {
            try {
                await navigator.share({
                    title: title,
                    text: text,
                    url: url
                });
            } catch (error) {
                console.log('Share cancelled or failed:', error);
            }
        } else {
            // Fallback: copy to clipboard
            try {
                await navigator.clipboard.writeText(url);
                this.showNotification('링크가 클립보드에 복사되었습니다!');
            } catch (error) {
                console.error('Failed to copy to clipboard:', error);
                this.showNotification('링크 복사에 실패했습니다.');
            }
        }
    }

    async deletePost() {
        if (!confirm('\uc815\ub9d0\ub85c \uc774 \uac8c\uc2dc\uae00\uc744 \uc0ad\uc81c\ud558\uc2dc\uaca0\uc2b5\ub2c8\uae4c?')) {
            return;
        }

        try {
            const container = document.getElementById('post-container');
            utils.showLoading(container, '\uac8c\uc2dc\uae00\uc744 \uc0ad\uc81c\ud558\ub294 \uc911...');
            
            await api.posts.delete(this.postId);
            
            this.showNotification('\uac8c\uc2dc\uae00\uc774 \uc0ad\uc81c\ub418\uc5c8\uc2b5\ub2c8\ub2e4!');
            
            // Redirect to posts list after a short delay
            setTimeout(() => {
                utils.navigateTo('/posts.html');
            }, 1000);
            
        } catch (error) {
            console.error('Failed to delete post:', error);
            this.showNotification('\uac8c\uc2dc\uae00 \uc0ad\uc81c\uc5d0 \uc2e4\ud328\ud588\uc2b5\ub2c8\ub2e4.', 'error');
            
            // Remove loading indicator if there was an error
            const container = document.getElementById('post-container');
            if (container) {
                container.innerHTML = this.post ? this.renderPost() : '<div class="error">Failed to delete post</div>';
            }
        }
    }

    showError(message) {
        const container = document.getElementById('post-container');
        utils.showError(container, message);
    }

    showNotification(message, type = 'success') {
        const notification = document.createElement('div');
        notification.className = `notification ${type}`;
        notification.innerHTML = `
            <i class="fas ${type === 'error' ? 'fa-exclamation-triangle' : 'fa-check-circle'}"></i>
            ${message}
        `;
        
        document.body.appendChild(notification);
        
        setTimeout(() => {
            notification.classList.add('show');
        }, 100);
        
        setTimeout(() => {
            notification.classList.remove('show');
            setTimeout(() => {
                if (document.body.contains(notification)) {
                    document.body.removeChild(notification);
                }
            }, 300);
        }, 3000);
    }
}

// Additional CSS for post detail page
const additionalStyles = `
<style>
.back-navigation {
    margin-bottom: 2rem;
}

.back-link {
    display: inline-flex;
    align-items: center;
    gap: 0.5rem;
    color: #6b7280;
    text-decoration: none;
    font-size: 0.875rem;
    padding: 0.5rem 0;
    transition: color 0.2s;
}

.back-link:hover {
    color: #2563eb;
}

.post-detail {
    background: white;
    border-radius: 0.75rem;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
    overflow: hidden;
    margin-bottom: 2rem;
}

.post-header {
    padding: 2rem;
    border-bottom: 1px solid #e5e7eb;
}

.post-category {
    margin-bottom: 1rem;
}

.post-title {
    font-size: 2rem;
    font-weight: 700;
    color: #1f2937;
    margin-bottom: 1rem;
    line-height: 1.3;
}

.post-meta {
    display: flex;
    gap: 2rem;
    font-size: 0.875rem;
    color: #6b7280;
    flex-wrap: wrap;
}

.post-author,
.post-date,
.post-updated {
    display: flex;
    align-items: center;
    gap: 0.25rem;
}

.post-content {
    padding: 2rem;
}

.post-text {
    font-size: 1.1rem;
    line-height: 1.7;
    color: #374151;
    margin-bottom: 2rem;
}

.post-images {
    margin-top: 2rem;
}

.post-images h4 {
    font-size: 1.25rem;
    font-weight: 600;
    color: #1f2937;
    margin-bottom: 1rem;
}

.image-gallery {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
    gap: 1rem;
}

.image-item {
    border-radius: 0.5rem;
    overflow: hidden;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    transition: transform 0.2s;
}

.image-item:hover {
    transform: scale(1.02);
}

.post-image {
    width: 100%;
    height: 200px;
    object-fit: cover;
    cursor: pointer;
    transition: opacity 0.2s;
}

.post-image:hover {
    opacity: 0.9;
}

.image-info {
    padding: 0.75rem;
    background: #f9fafb;
}

.image-filename {
    font-size: 0.875rem;
    color: #6b7280;
    word-break: break-all;
}

.post-footer {
    padding: 1.5rem 2rem;
    border-top: 1px solid #e5e7eb;
    background: #f9fafb;
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.post-actions {
    display: flex;
    gap: 1rem;
}

.share-btn {
    background: none;
    border: none;
    color: #6b7280;
    cursor: pointer;
    display: flex;
    align-items: center;
    gap: 0.5rem;
    padding: 0.5rem 1rem;
    border-radius: 0.5rem;
    transition: all 0.2s;
}

.share-btn:hover {
    background-color: #e5e7eb;
    color: #374151;
}

.post-management {
    display: flex;
    gap: 0.5rem;
}

.modal {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: rgba(0, 0, 0, 0.8);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 1000;
}

.modal-content {
    background: white;
    border-radius: 0.75rem;
    max-width: 500px;
    width: 90%;
    max-height: 90%;
    overflow: hidden;
    box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1);
}

.modal-content.modal-image {
    max-width: 90%;
    max-height: 90%;
}

.modal-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 1.5rem;
    border-bottom: 1px solid #e5e7eb;
}

.modal-header h3 {
    margin: 0;
    font-size: 1.25rem;
    font-weight: 600;
    color: #1f2937;
}

.modal-close {
    background: none;
    border: none;
    color: #6b7280;
    cursor: pointer;
    padding: 0.5rem;
    border-radius: 0.25rem;
    transition: all 0.2s;
}

.modal-close:hover {
    background-color: #f3f4f6;
    color: #374151;
}

.modal-body {
    padding: 1.5rem;
}

.modal-image-content {
    width: 100%;
    height: auto;
    max-height: 70vh;
    object-fit: contain;
}

.notification {
    position: fixed;
    top: 20px;
    right: 20px;
    background: #059669;
    color: white;
    padding: 1rem 1.5rem;
    border-radius: 0.5rem;
    box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1);
    transform: translateX(100%);
    transition: transform 0.3s ease;
    z-index: 1001;
    display: flex;
    align-items: center;
    gap: 0.5rem;
}

.notification.show {
    transform: translateX(0);
}

@media (max-width: 768px) {
    .post-title {
        font-size: 1.5rem;
    }
    
    .post-header,
    .post-content {
        padding: 1.5rem;
    }
    
    .post-footer {
        padding: 1rem 1.5rem;
        flex-direction: column;
        gap: 1rem;
        align-items: stretch;
    }
    
    .post-actions {
        justify-content: center;
    }
    
    .post-management {
        justify-content: center;
    }
    
    .post-meta {
        flex-direction: column;
        gap: 0.5rem;
    }
    
    .image-gallery {
        grid-template-columns: 1fr;
    }
    
    .modal-content {
        width: 95%;
        margin: 1rem;
    }
    
    .modal-body {
        padding: 1rem;
    }
}
</style>
`;

// Initialize post detail page
let postDetailPage;

document.addEventListener('DOMContentLoaded', function() {
    // Add additional styles
    document.head.insertAdjacentHTML('beforeend', additionalStyles);
    
    // Initialize post detail page
    postDetailPage = new PostDetailPage();
});
