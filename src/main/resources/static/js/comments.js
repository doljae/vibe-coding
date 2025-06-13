// Comments functionality
class CommentsManager {
    constructor() {
        this.postId = null;
        this.comments = [];
        this.currentUserId = this.getCurrentUserId();
        
        this.init();
    }

    init() {
        this.postId = this.getPostIdFromUrl();
        
        if (this.postId) {
            this.loadComments();
            this.setupEventListeners();
        }
    }

    getPostIdFromUrl() {
        const params = utils.getUrlParams();
        return params.id;
    }

    getCurrentUserId() {
        // In a real application, this would come from authentication
        let userId = storage.get('currentUserId');
        if (!userId) {
            userId = 'user-' + Math.random().toString(36).substr(2, 9);
            storage.set('currentUserId', userId);
        }
        return userId;
    }

    async loadComments() {
        const container = document.getElementById('comments-list');
        if (!container) return;

        try {
            utils.showLoading(container, '댓글을 불러오는 중...');
            
            const response = await api.comments.getForPost(this.postId);
            
            // Convert nested structure to flat array
            this.comments = [];
            if (response.comments) {
                response.comments.forEach(commentWithReplies => {
                    // Add main comment
                    this.comments.push(commentWithReplies.comment);
                    // Add replies
                    if (commentWithReplies.replies) {
                        this.comments.push(...commentWithReplies.replies);
                    }
                });
            }
            
            this.renderComments();
            this.updateCommentsCount(response.totalCommentCount || 0);
            
        } catch (error) {
            console.error('Failed to load comments:', error);
            utils.showError(container, '댓글을 불러오는데 실패했습니다.');
        }
    }

    renderComments() {
        const container = document.getElementById('comments-list');
        if (!container) return;

        if (this.comments.length === 0) {
            container.innerHTML = `
                <div class="empty-comments">
                    <i class="fas fa-comments"></i>
                    <p>아직 댓글이 없습니다. 첫 번째 댓글을 작성해보세요!</p>
                </div>
            `;
            return;
        }

        // Group comments by parent-child relationship
        const rootComments = this.comments.filter(comment => !comment.parentCommentId);
        const replies = this.comments.filter(comment => comment.parentCommentId);

        const commentsHtml = rootComments.map(comment => {
            const commentReplies = replies.filter(reply => reply.parentCommentId === comment.id);
            return this.renderComment(comment, commentReplies);
        }).join('');

        container.innerHTML = `<div class="comments-container">${commentsHtml}</div>`;
    }

    renderComment(comment, replies = []) {
        const repliesHtml = replies.map(reply => this.renderReply(reply)).join('');
        
        return `
            <div class="comment" data-comment-id="${comment.id}">
                <div class="comment-content">
                    <div class="comment-header">
                        <div class="comment-author">
                            <i class="fas fa-user-circle"></i>
                            <span class="author-name">${comment.authorName}</span>
                        </div>
                        <div class="comment-date">
                            ${utils.formatRelativeTime(comment.createdAt)}
                        </div>
                    </div>
                    <div class="comment-text">
                        ${this.formatCommentContent(comment.content)}
                    </div>
                    <div class="comment-actions">
                        <button class="comment-action reply-btn" onclick="commentsManager.showReplyForm('${comment.id}')">
                            <i class="fas fa-reply"></i>
                            답글
                        </button>
                        ${this.canEditComment(comment) ? `
                            <button class="comment-action edit-btn" onclick="commentsManager.editComment('${comment.id}')">
                                <i class="fas fa-edit"></i>
                                수정
                            </button>
                            <button class="comment-action delete-btn" onclick="commentsManager.deleteComment('${comment.id}')">
                                <i class="fas fa-trash"></i>
                                삭제
                            </button>
                        ` : ''}
                    </div>
                </div>
                
                ${replies.length > 0 ? `
                    <div class="comment-replies">
                        ${repliesHtml}
                    </div>
                ` : ''}
            </div>
        `;
    }

    renderReply(reply) {
        return `
            <div class="comment-reply" data-comment-id="${reply.id}">
                <div class="comment-content">
                    <div class="comment-header">
                        <div class="comment-author">
                            <i class="fas fa-user-circle"></i>
                            <span class="author-name">${reply.authorName}</span>
                            <span class="reply-indicator">
                                <i class="fas fa-reply"></i>
                                답글
                            </span>
                        </div>
                        <div class="comment-date">
                            ${utils.formatRelativeTime(reply.createdAt)}
                        </div>
                    </div>
                    <div class="comment-text">
                        ${this.formatCommentContent(reply.content)}
                    </div>
                    <div class="comment-actions">
                        ${this.canEditComment(reply) ? `
                            <button class="comment-action edit-btn" onclick="commentsManager.editComment('${reply.id}')">
                                <i class="fas fa-edit"></i>
                                수정
                            </button>
                            <button class="comment-action delete-btn" onclick="commentsManager.deleteComment('${reply.id}')">
                                <i class="fas fa-trash"></i>
                                삭제
                            </button>
                        ` : ''}
                    </div>
                </div>
            </div>
        `;
    }

    formatCommentContent(content) {
        return content.replace(/\n/g, '<br>');
    }

    canEditComment(comment) {
        // In a real application, this would check user permissions
        // For demo purposes, allow editing if author name matches stored name
        const storedAuthorName = storage.get('authorName');
        console.log('Debug - canEditComment:', {
            storedAuthorName,
            commentAuthorName: comment.authorName,
            commentId: comment.id,
            canEdit: storedAuthorName && comment.authorName === storedAuthorName
        });
        return storedAuthorName && comment.authorName === storedAuthorName;
    }

    updateCommentsCount(count) {
        const countElement = document.getElementById('comments-count');
        if (countElement) {
            countElement.textContent = count;
        }
    }

    setupEventListeners() {
        // Comment form submission
        const commentForm = document.getElementById('comment-form');
        if (commentForm) {
            commentForm.addEventListener('submit', (e) => this.handleCommentSubmit(e));
        }

        // Reply form submission
        const replyForm = document.getElementById('reply-form');
        if (replyForm) {
            replyForm.addEventListener('submit', (e) => this.handleReplySubmit(e));
        }

        // Reply modal controls
        const closeReplyModal = document.getElementById('close-reply-modal');
        const cancelReply = document.getElementById('cancel-reply');
        
        if (closeReplyModal) {
            closeReplyModal.addEventListener('click', () => this.hideReplyForm());
        }
        
        if (cancelReply) {
            cancelReply.addEventListener('click', () => this.hideReplyForm());
        }

        // Click outside modal to close
        const replyModal = document.getElementById('reply-modal');
        if (replyModal) {
            replyModal.addEventListener('click', (e) => {
                if (e.target === replyModal) {
                    this.hideReplyForm();
                }
            });
        }

        // Escape key to close modal
        document.addEventListener('keydown', (e) => {
            if (e.key === 'Escape') {
                this.hideReplyForm();
            }
        });
    }

    async handleCommentSubmit(e) {
        e.preventDefault();
        
        const form = e.target;
        const authorInput = form.querySelector('#comment-author');
        const contentInput = form.querySelector('#comment-content');
        
        const author = authorInput.value.trim();
        const content = contentInput.value.trim();
        
        if (!author || !content) {
            this.showNotification('작성자와 댓글 내용을 모두 입력해주세요.', 'error');
            return;
        }

        try {
            // Store author name for future use
            storage.set('authorName', author);
            
            const commentData = {
                content: content,
                authorName: author,
                postId: this.postId
            };

            await api.comments.create(commentData);
            
            // Clear form
            contentInput.value = '';
            
            // Reload comments
            await this.loadComments();
            
            this.showNotification('댓글이 작성되었습니다!');
            
        } catch (error) {
            console.error('Failed to create comment:', error);
            this.showNotification('댓글 작성에 실패했습니다.', 'error');
        }
    }

    async handleReplySubmit(e) {
        e.preventDefault();
        
        const form = e.target;
        const parentIdInput = form.querySelector('#reply-parent-id');
        const authorInput = form.querySelector('#reply-author');
        const contentInput = form.querySelector('#reply-content');
        
        const parentId = parentIdInput.value;
        const author = authorInput.value.trim();
        const content = contentInput.value.trim();
        
        if (!author || !content) {
            this.showNotification('작성자와 답글 내용을 모두 입력해주세요.', 'error');
            return;
        }

        try {
            // Store author name for future use
            storage.set('authorName', author);
            
            const replyData = {
                content: content,
                authorName: author,
                postId: this.postId,
                parentCommentId: parentId
            };

            await api.comments.createReply(replyData);
            
            // Hide reply form
            this.hideReplyForm();
            
            // Reload comments
            await this.loadComments();
            
            this.showNotification('답글이 작성되었습니다!');
            
        } catch (error) {
            console.error('Failed to create reply:', error);
            this.showNotification('답글 작성에 실패했습니다.', 'error');
        }
    }

    showReplyForm(parentCommentId) {
        const modal = document.getElementById('reply-modal');
        const parentIdInput = document.getElementById('reply-parent-id');
        const authorInput = document.getElementById('reply-author');
        const contentInput = document.getElementById('reply-content');
        
        if (modal && parentIdInput) {
            parentIdInput.value = parentCommentId;
            
            // Pre-fill author name if stored
            const storedAuthorName = storage.get('authorName');
            if (storedAuthorName && authorInput) {
                authorInput.value = storedAuthorName;
            }
            
            // Clear content
            if (contentInput) {
                contentInput.value = '';
            }
            
            modal.style.display = 'flex';
            document.body.style.overflow = 'hidden';
            
            // Focus on content input
            setTimeout(() => {
                if (contentInput) {
                    contentInput.focus();
                }
            }, 100);
        }
    }

    hideReplyForm() {
        const modal = document.getElementById('reply-modal');
        if (modal) {
            modal.style.display = 'none';
            document.body.style.overflow = 'auto';
        }
    }

    async editComment(commentId) {
        const comment = this.comments.find(c => c.id === commentId);
        if (!comment) return;

        const newContent = prompt('댓글을 수정하세요:', comment.content);
        if (!newContent || newContent.trim() === comment.content) return;

        try {
            const updateData = {
                content: newContent.trim(),
                authorId: comment.authorId
            };

            await api.comments.update(commentId, updateData);
            
            // Reload comments
            await this.loadComments();
            
            this.showNotification('댓글이 수정되었습니다!');
            
        } catch (error) {
            console.error('Failed to update comment:', error);
            this.showNotification('댓글 수정에 실패했습니다.', 'error');
        }
    }

    async deleteComment(commentId) {
        if (!confirm('정말로 이 댓글을 삭제하시겠습니까?')) {
            return;
        }

        const comment = this.comments.find(c => c.id === commentId);
        if (!comment) {
            this.showNotification('댓글을 찾을 수 없습니다.', 'error');
            return;
        }

        try {
            await api.comments.delete(commentId, comment.authorId);
            
            // Reload comments
            await this.loadComments();
            
            this.showNotification('댓글이 삭제되었습니다!');
            
        } catch (error) {
            console.error('Failed to delete comment:', error);
            this.showNotification('댓글 삭제에 실패했습니다.', 'error');
        }
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

// Additional CSS for comments
const commentsStyles = `
<style>
.comments-section {
    background: white;
    border-radius: 0.75rem;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
    overflow: hidden;
}

.comments-title {
    padding: 1.5rem 2rem;
    margin: 0;
    font-size: 1.25rem;
    font-weight: 600;
    color: #1f2937;
    border-bottom: 1px solid #e5e7eb;
    display: flex;
    align-items: center;
    gap: 0.5rem;
}

.comment-form-container {
    padding: 2rem;
    border-bottom: 1px solid #e5e7eb;
    background: #f9fafb;
}

.comment-form .form-actions {
    display: flex;
    justify-content: flex-end;
}

.comments-list {
    padding: 1.5rem 2rem;
}

.empty-comments {
    text-align: center;
    padding: 3rem 2rem;
    color: #6b7280;
}

.empty-comments i {
    font-size: 3rem;
    margin-bottom: 1rem;
    color: #d1d5db;
}

.comments-container {
    display: flex;
    flex-direction: column;
    gap: 1.5rem;
}

.comment {
    border: 1px solid #e5e7eb;
    border-radius: 0.5rem;
    overflow: hidden;
}

.comment-content {
    padding: 1.5rem;
}

.comment-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 0.75rem;
}

.comment-author {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    font-weight: 500;
    color: #374151;
}

.comment-author i {
    color: #6b7280;
}

.reply-indicator {
    color: #6b7280;
    font-size: 0.875rem;
    font-weight: 400;
}

.comment-date {
    font-size: 0.875rem;
    color: #6b7280;
}

.comment-text {
    color: #4b5563;
    line-height: 1.6;
    margin-bottom: 1rem;
}

.comment-actions {
    display: flex;
    gap: 1rem;
}

.comment-action {
    background: none;
    border: none;
    color: #6b7280;
    cursor: pointer;
    font-size: 0.875rem;
    padding: 0.25rem 0.5rem;
    border-radius: 0.25rem;
    transition: all 0.2s;
    display: flex;
    align-items: center;
    gap: 0.25rem;
}

.comment-action:hover {
    background-color: #f3f4f6;
    color: #374151;
}

.comment-action.delete-btn:hover {
    color: #dc2626;
    background-color: #fef2f2;
}

.comment-replies {
    background: #f9fafb;
    border-top: 1px solid #e5e7eb;
}

.comment-reply {
    padding: 1.5rem;
    border-bottom: 1px solid #e5e7eb;
    margin-left: 1rem;
    position: relative;
}

.comment-reply:last-child {
    border-bottom: none;
}

.comment-reply::before {
    content: '';
    position: absolute;
    left: -1rem;
    top: 0;
    bottom: 0;
    width: 2px;
    background: #d1d5db;
}

.reply-form .form-actions {
    display: flex;
    gap: 1rem;
    justify-content: flex-end;
}

.notification.error {
    background: #dc2626;
}

@media (max-width: 768px) {
    .comments-title,
    .comment-form-container,
    .comments-list {
        padding: 1rem;
    }
    
    .comment-content {
        padding: 1rem;
    }
    
    .comment-header {
        flex-direction: column;
        align-items: flex-start;
        gap: 0.5rem;
    }
    
    .comment-actions {
        flex-wrap: wrap;
    }
    
    .comment-reply {
        margin-left: 0.5rem;
        padding: 1rem;
    }
    
    .comment-reply::before {
        left: -0.5rem;
    }
    
    .reply-form .form-actions {
        flex-direction: column;
    }
}
</style>
`;

// Initialize comments manager
let commentsManager;

document.addEventListener('DOMContentLoaded', function() {
    // Add comments styles
    document.head.insertAdjacentHTML('beforeend', commentsStyles);
    
    // Initialize comments manager
    commentsManager = new CommentsManager();
    
    // Pre-fill author name in comment form if stored
    const storedAuthorName = storage.get('authorName');
    if (storedAuthorName) {
        const commentAuthorInput = document.getElementById('comment-author');
        if (commentAuthorInput) {
            commentAuthorInput.value = storedAuthorName;
        }
    }
});
