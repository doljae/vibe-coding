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
            this.showNotification('\uc791\uc131\uc790\uc640 \ub313\uae00 \ub0b4\uc6a9\uc744 \ubaa8\ub450 \uc785\ub825\ud574\uc8fc\uc138\uc694.', 'error');
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

            // Create the comment
            const newComment = await api.comments.create(commentData);
            
            // Clear form
            contentInput.value = '';
            
            // Add the new comment to the comments array
            if (newComment) {
                this.comments.push(newComment);
                
                // Re-render the comments without reloading from server
                this.renderComments();
                this.updateCommentsCount(this.comments.length);
                
                this.showNotification('\ub313\uae00\uc774 \uc791\uc131\ub418\uc5c8\uc2b5\ub2c8\ub2e4!');
            } else {
                // If the API didn't return the new comment, reload all comments
                await this.loadComments();
                this.showNotification('\ub313\uae00\uc774 \uc791\uc131\ub418\uc5c8\uc2b5\ub2c8\ub2e4!');
            }
        } catch (error) {
            console.error('Failed to create comment:', error);
            this.showNotification('\ub313\uae00 \uc791\uc131\uc5d0 \uc2e4\ud328\ud588\uc2b5\ub2c8\ub2e4.', 'error');
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
            this.showNotification('\uc791\uc131\uc790\uc640 \ub2f5\uae00 \ub0b4\uc6a9\uc744 \ubaa8\ub450 \uc785\ub825\ud574\uc8fc\uc138\uc694.', 'error');
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

            // Create the reply
            const newReply = await api.comments.createReply(replyData);
            
            // Hide reply form
            this.hideReplyForm();
            
            if (newReply) {
                // Add the new reply to the comments array
                this.comments.push(newReply);
                
                // Re-render the comments without reloading from server
                this.renderComments();
                this.updateCommentsCount(this.comments.length);
                
                this.showNotification('\ub2f5\uae00\uc774 \uc791\uc131\ub418\uc5c8\uc2b5\ub2c8\ub2e4!');
            } else {
                // If the API didn't return the new reply, reload all comments
                await this.loadComments();
                this.showNotification('\ub2f5\uae00\uc774 \uc791\uc131\ub418\uc5c8\uc2b5\ub2c8\ub2e4!');
            }
        } catch (error) {
            console.error('Failed to create reply:', error);
            this.showNotification('\ub2f5\uae00 \uc791\uc131\uc5d0 \uc2e4\ud328\ud588\uc2b5\ub2c8\ub2e4.', 'error');
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

        // Create a modal for editing instead of using prompt
        const editModal = document.createElement('div');
        editModal.className = 'modal';
        editModal.id = 'edit-comment-modal';
        editModal.innerHTML = `
            <div class="modal-content">
                <div class="modal-header">
                    <h3>댓글 수정</h3>
                    <button class="modal-close" id="close-edit-modal">
                        <i class="fas fa-times"></i>
                    </button>
                </div>
                <div class="modal-body">
                    <form id="edit-comment-form">
                        <div class="form-group">
                            <label for="edit-comment-content">내용</label>
                            <textarea id="edit-comment-content" class="form-control" rows="4" required>${comment.content}</textarea>
                        </div>
                        <div class="form-actions">
                            <button type="button" id="cancel-edit" class="btn btn-secondary">취소</button>
                            <button type="submit" class="btn btn-primary">수정</button>
                        </div>
                    </form>
                </div>
            </div>
        `;
        
        document.body.appendChild(editModal);
        document.body.style.overflow = 'hidden';
        
        // Focus on the textarea
        const textarea = document.getElementById('edit-comment-content');
        if (textarea) {
            textarea.focus();
            // Place cursor at the end of the text
            textarea.selectionStart = textarea.selectionEnd = textarea.value.length;
        }
        
        // Set up event listeners
        const closeBtn = document.getElementById('close-edit-modal');
        const cancelBtn = document.getElementById('cancel-edit');
        const form = document.getElementById('edit-comment-form');
        
        const closeModal = () => {
            document.body.removeChild(editModal);
            document.body.style.overflow = 'auto';
        };
        
        if (closeBtn) {
            closeBtn.addEventListener('click', closeModal);
        }
        
        if (cancelBtn) {
            cancelBtn.addEventListener('click', closeModal);
        }
        
        // Click outside modal to close
        editModal.addEventListener('click', (e) => {
            if (e.target === editModal) {
                closeModal();
            }
        });
        
        // Escape key to close modal
        const escapeHandler = (e) => {
            if (e.key === 'Escape') {
                closeModal();
                document.removeEventListener('keydown', escapeHandler);
            }
        };
        document.addEventListener('keydown', escapeHandler);
        
        if (form) {
            form.addEventListener('submit', async (e) => {
                e.preventDefault();
                
                const contentInput = document.getElementById('edit-comment-content');
                const newContent = contentInput.value.trim();
                
                if (!newContent) {
                    this.showNotification('댓글 내용을 입력해주세요.', 'error');
                    return;
                }
                
                if (newContent === comment.content) {
                    closeModal();
                    return;
                }
                
                try {
                    const updateData = {
                        content: newContent,
                        authorId: comment.authorId
                    };
                    
                    await api.comments.update(commentId, updateData);
                    
                    // Update the comment in the local array
                    const index = this.comments.findIndex(c => c.id === commentId);
                    if (index !== -1) {
                        this.comments[index] = { ...this.comments[index], content: newContent };
                        
                        // Re-render the comments without reloading from server
                        this.renderComments();
                    } else {
                        // If we can't find the comment locally, reload all comments
                        await this.loadComments();
                    }
                    
                    closeModal();
                    this.showNotification('댓글이 수정되었습니다!');
                } catch (error) {
                    console.error('Failed to update comment:', error);
                    this.showNotification('댓글 수정에 실패했습니다.', 'error');
                }
            });
        }
    }

    async deleteComment(commentId) {
        const comment = this.comments.find(c => c.id === commentId);
        if (!comment) return;
        
        // Check if this comment has replies
        const hasReplies = comment.isReply === false && 
            this.comments.some(c => c.parentCommentId === commentId);
        
        let confirmMessage = '정말로 이 댓글을 삭제하시겠습니까?';
        if (hasReplies) {
            confirmMessage = '이 댓글을 삭제하면 모든 답글도 함께 삭제됩니다. 계속하시겠습니까?';
        }
        
        if (!confirm(confirmMessage)) {
            return;
        }

        try {
            // Get the authorId from the comment
            const authorId = comment.authorId;
            
            // Call the API to delete the comment
            await api.comments.delete(commentId, authorId);
            
            // Remove the comment from the local array
            this.comments = this.comments.filter(c => c.id !== commentId);
            
            // If it's a root comment, also remove all replies
            if (!comment.isReply) {
                this.comments = this.comments.filter(c => c.parentCommentId !== commentId);
            }
            
            // Re-render the comments without reloading from server
            this.renderComments();
            this.updateCommentsCount(this.comments.length);
            
            this.showNotification('댓글이 삭제되었습니다!');
        } catch (error) {
            console.error('Failed to delete comment:', error);
            this.showNotification('댓글 삭제에 실패했습니다.', 'error');
            
            // If there's an error, reload all comments to ensure consistency
            await this.loadComments();
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

.modal {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: rgba(0, 0, 0, 0.5);
    display: flex;
    justify-content: center;
    align-items: center;
    z-index: 1000;
}

.modal-content {
    background: white;
    border-radius: 0.5rem;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
    padding: 2rem;
    width: 90%;
    max-width: 600px;
}

.modal-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 1rem;
}

.modal-header h3 {
    margin: 0;
    font-size: 1.25rem;
    font-weight: 600;
    color: #1f2937;
}

.modal-header .modal-close {
    background: none;
    border: none;
    color: #6b7280;
    cursor: pointer;
    font-size: 1.25rem;
    padding: 0.5rem;
    border-radius: 0.25rem;
    transition: all 0.2s;
}

.modal-header .modal-close:hover {
    color: #374151;
}

.modal-body {
    margin-bottom: 1rem;
}

.modal-body form {
    display: flex;
    flex-direction: column;
    gap: 1rem;
}

.modal-body form .form-group {
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
}

.modal-body form .form-group label {
    font-size: 0.875rem;
    color: #6b7280;
}

.modal-body form .form-group textarea {
    width: 100%;
    padding: 0.75rem;
    border: 1px solid #e5e7eb;
    border-radius: 0.25rem;
    resize: vertical;
}

.modal-body form .form-actions {
    display: flex;
    gap: 1rem;
    justify-content: flex-end;
}

.modal-body form .form-actions button {
    padding: 0.5rem 1rem;
    border-radius: 0.25rem;
    transition: all 0.2s;
}

.modal-body form .form-actions button.btn-secondary {
    background: #e5e7eb;
    color: #6b7280;
}

.modal-body form .form-actions button.btn-secondary:hover {
    background: #d1d5db;
}

.modal-body form .form-actions button.btn-primary {
    background: #1f2937;
    color: white;
}

.modal-body form .form-actions button.btn-primary:hover {
    background: #151b22;
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
