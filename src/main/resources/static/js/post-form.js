// Post form functionality
class PostFormPage {
    constructor() {
        this.isEditMode = false;
        this.postId = null;
        this.selectedFiles = [];
        this.maxFiles = 3;
        this.maxFileSize = 5 * 1024 * 1024; // 5MB
        this.allowedTypes = ['image/jpeg', 'image/png', 'image/gif'];
        this.currentUserId = this.getCurrentUserId();
        this.categories = [];
        
        this.init();
    }

    async init() {
        this.checkEditMode();
        await this.loadCategories();
        this.setupEventListeners();
        this.loadDraftData();
        
        if (this.isEditMode) {
            await this.loadPostData();
        }
        
        this.prefillAuthorName();
    }

    checkEditMode() {
        const params = utils.getUrlParams();
        this.postId = params.id;
        this.isEditMode = !!this.postId;
        
        if (this.isEditMode) {
            document.getElementById('form-title').textContent = '게시글 수정';
            document.getElementById('submit-text').textContent = '수정 완료';
            document.title = '게시글 수정 - Vibe Coding';
        }
    }

    getCurrentUserId() {
        let userId = storage.get('currentUserId');
        if (!userId) {
            userId = 'user-' + Math.random().toString(36).substr(2, 9);
            storage.set('currentUserId', userId);
        }
        return userId;
    }

    async loadCategories() {
        try {
            this.categories = await api.categories.getAll();
            this.renderCategoryOptions();
        } catch (error) {
            console.error('Failed to load categories:', error);
            this.showNotification('카테고리를 불러오는데 실패했습니다.', 'error');
        }
    }

    renderCategoryOptions() {
        const categorySelect = document.getElementById('post-category');
        if (!categorySelect) return;

        const options = this.categories.map(category => 
            `<option value="${category.id}">${category.name}</option>`
        ).join('');

        categorySelect.innerHTML = `
            <option value="">카테고리를 선택하세요</option>
            ${options}
        `;
    }

    async loadPostData() {
        if (!this.postId) return;

        try {
            this.showLoadingModal('게시글 정보를 불러오는 중...');
            
            const post = await api.posts.getById(this.postId);
            
            // Fill form with post data
            document.getElementById('post-title').value = post.title;
            document.getElementById('post-content').value = post.content;
            document.getElementById('post-category').value = post.category.id;
            document.getElementById('author-name').value = post.author.name;
            
            this.hideLoadingModal();
            
        } catch (error) {
            console.error('Failed to load post data:', error);
            this.hideLoadingModal();
            this.showNotification('게시글 정보를 불러오는데 실패했습니다.', 'error');
        }
    }

    prefillAuthorName() {
        const authorNameInput = document.getElementById('author-name');
        const storedAuthorName = storage.get('authorName');
        
        if (storedAuthorName && !this.isEditMode) {
            authorNameInput.value = storedAuthorName;
        }
    }

    setupEventListeners() {
        // Form submission
        const form = document.getElementById('post-form');
        if (form) {
            form.addEventListener('submit', (e) => this.handleSubmit(e));
        }

        // File upload
        const fileInput = document.getElementById('post-images');
        const uploadArea = document.getElementById('file-upload-area');
        
        if (fileInput) {
            fileInput.addEventListener('change', (e) => this.handleFileSelect(e));
        }

        if (uploadArea) {
            uploadArea.addEventListener('click', () => fileInput?.click());
            uploadArea.addEventListener('dragover', (e) => this.handleDragOver(e));
            uploadArea.addEventListener('drop', (e) => this.handleDrop(e));
        }

        // Cancel button
        const cancelBtn = document.getElementById('cancel-btn');
        if (cancelBtn) {
            cancelBtn.addEventListener('click', () => this.handleCancel());
        }

        // Save draft button
        const saveDraftBtn = document.getElementById('save-draft-btn');
        if (saveDraftBtn) {
            saveDraftBtn.addEventListener('click', () => this.saveDraft());
        }

        // Auto-save draft
        const titleInput = document.getElementById('post-title');
        const contentInput = document.getElementById('post-content');
        
        if (titleInput && contentInput) {
            const debouncedSave = utils.debounce(() => this.saveDraft(true), 2000);
            titleInput.addEventListener('input', debouncedSave);
            contentInput.addEventListener('input', debouncedSave);
        }

        // Form validation
        this.setupFormValidation();
    }

    setupFormValidation() {
        const form = document.getElementById('post-form');
        const inputs = form.querySelectorAll('input[required], textarea[required], select[required]');
        
        inputs.forEach(input => {
            input.addEventListener('blur', () => this.validateField(input));
            input.addEventListener('input', () => this.clearFieldError(input));
        });
    }

    validateField(field) {
        const value = field.value.trim();
        let isValid = true;
        let errorMessage = '';

        if (field.hasAttribute('required') && !value) {
            isValid = false;
            errorMessage = '이 필드는 필수입니다.';
        } else if (field.type === 'email' && value && !this.isValidEmail(value)) {
            isValid = false;
            errorMessage = '올바른 이메일 주소를 입력하세요.';
        } else if (field.id === 'post-title' && value.length > 200) {
            isValid = false;
            errorMessage = '제목은 200자를 초과할 수 없습니다.';
        }

        if (!isValid) {
            this.showFieldError(field, errorMessage);
        } else {
            this.clearFieldError(field);
        }

        return isValid;
    }

    showFieldError(field, message) {
        this.clearFieldError(field);
        
        field.classList.add('error');
        
        const errorElement = document.createElement('div');
        errorElement.className = 'field-error';
        errorElement.textContent = message;
        
        field.parentNode.appendChild(errorElement);
    }

    clearFieldError(field) {
        field.classList.remove('error');
        
        const existingError = field.parentNode.querySelector('.field-error');
        if (existingError) {
            existingError.remove();
        }
    }

    isValidEmail(email) {
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        return emailRegex.test(email);
    }

    handleFileSelect(e) {
        const files = Array.from(e.target.files);
        this.processFiles(files);
    }

    handleDragOver(e) {
        e.preventDefault();
        e.stopPropagation();
        e.currentTarget.classList.add('drag-over');
    }

    handleDrop(e) {
        e.preventDefault();
        e.stopPropagation();
        e.currentTarget.classList.remove('drag-over');
        
        const files = Array.from(e.dataTransfer.files);
        this.processFiles(files);
    }

    processFiles(files) {
        // Filter valid image files
        const validFiles = files.filter(file => {
            if (!this.allowedTypes.includes(file.type)) {
                this.showNotification(`${file.name}은(는) 지원하지 않는 파일 형식입니다.`, 'error');
                return false;
            }
            
            if (file.size > this.maxFileSize) {
                this.showNotification(`${file.name}은(는) 파��� 크기가 너무 큽니다. (최대 5MB)`, 'error');
                return false;
            }
            
            return true;
        });

        // Check total file count
        const totalFiles = this.selectedFiles.length + validFiles.length;
        if (totalFiles > this.maxFiles) {
            this.showNotification(`최대 ${this.maxFiles}개의 이미지만 업로드할 수 있습니다.`, 'error');
            return;
        }

        // Add valid files
        this.selectedFiles.push(...validFiles);
        this.updateFilePreview();
        this.updateFileInput();
    }

    updateFilePreview() {
        const previewSection = document.getElementById('image-preview');
        const previewContainer = document.getElementById('preview-container');
        
        if (!previewSection || !previewContainer) return;

        if (this.selectedFiles.length === 0) {
            previewSection.style.display = 'none';
            return;
        }

        previewSection.style.display = 'block';
        previewContainer.innerHTML = '';

        this.selectedFiles.forEach((file, index) => {
            const reader = new FileReader();
            reader.onload = (e) => {
                const previewItem = document.createElement('div');
                previewItem.className = 'preview-item';
                previewItem.innerHTML = `
                    <img src="${e.target.result}" alt="${file.name}" class="preview-image">
                    <div class="preview-info">
                        <span class="preview-filename">${file.name}</span>
                        <span class="preview-size">${this.formatFileSize(file.size)}</span>
                    </div>
                    <button type="button" class="preview-remove" onclick="postFormPage.removeFile(${index})">
                        <i class="fas fa-times"></i>
                    </button>
                `;
                previewContainer.appendChild(previewItem);
            };
            reader.readAsDataURL(file);
        });
    }

    updateFileInput() {
        const fileInput = document.getElementById('post-images');
        if (!fileInput) return;

        // Create new FileList with selected files
        const dt = new DataTransfer();
        this.selectedFiles.forEach(file => dt.items.add(file));
        fileInput.files = dt.files;
    }

    removeFile(index) {
        this.selectedFiles.splice(index, 1);
        this.updateFilePreview();
        this.updateFileInput();
    }

    formatFileSize(bytes) {
        if (bytes === 0) return '0 Bytes';
        const k = 1024;
        const sizes = ['Bytes', 'KB', 'MB', 'GB'];
        const i = Math.floor(Math.log(bytes) / Math.log(k));
        return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
    }

    async handleSubmit(e) {
        e.preventDefault();
        
        // Validate form
        if (!this.validateForm()) {
            return;
        }

        const formData = this.getFormData();
        
        try {
            this.showLoadingModal(this.isEditMode ? '게시글을 수정하는 중...' : '게시글을 작성하는 중...');
            
            let result;
            if (this.isEditMode) {
                result = await this.updatePost(formData);
            } else {
                result = await this.createPost(formData);
            }
            
            // Save author name
            storage.set('authorName', formData.get('authorName'));
            
            // Clear draft
            this.clearDraft();
            
            this.hideLoadingModal();
            
            const message = this.isEditMode ? '게시글이 수정되었습니다!' : '게시글이 작성되었습니다!';
            this.showNotification(message);
            
            // Redirect to post detail
            setTimeout(() => {
                utils.navigateTo(`/post-detail.html?id=${result.id}`);
            }, 1500);
            
        } catch (error) {
            console.error('Failed to submit post:', error);
            this.hideLoadingModal();
            
            const message = this.isEditMode ? '게시글 수정에 실패했습니다.' : '게시글 작성에 실패했습니다.';
            this.showNotification(message, 'error');
        }
    }

    validateForm() {
        const form = document.getElementById('post-form');
        const requiredFields = form.querySelectorAll('input[required], textarea[required], select[required]');
        let isValid = true;

        requiredFields.forEach(field => {
            if (!this.validateField(field)) {
                isValid = false;
            }
        });

        if (!isValid) {
            this.showNotification('필수 항목을 모두 입력해주세요.', 'error');
        }

        return isValid;
    }

    getFormData() {
        const formData = new FormData();
        
        formData.append('title', document.getElementById('post-title').value.trim());
        formData.append('content', document.getElementById('post-content').value.trim());
        formData.append('authorName', document.getElementById('author-name').value.trim());
        formData.append('categoryId', document.getElementById('post-category').value);
        
        // Add images
        this.selectedFiles.forEach(file => {
            formData.append('images', file);
        });
        
        return formData;
    }

    async createPost(formData) {
        // Always use the multipart endpoint for consistency
        return await api.posts.createWithImages(formData);
    }

    async updatePost(formData) {
        const postData = {
            title: formData.get('title'),
            content: formData.get('content'),
            categoryId: formData.get('categoryId')
        };
        return await api.posts.update(this.postId, postData);
    }

    handleCancel() {
        if (this.hasUnsavedChanges()) {
            if (!confirm('작성 중인 내용이 있습니다. 정말로 취소하시겠습니까?')) {
                return;
            }
        }
        
        utils.navigateTo('/posts.html');
    }

    hasUnsavedChanges() {
        const title = document.getElementById('post-title').value.trim();
        const content = document.getElementById('post-content').value.trim();
        
        return title || content || this.selectedFiles.length > 0;
    }

    saveDraft(silent = false) {
        const draftData = {
            title: document.getElementById('post-title').value.trim(),
            content: document.getElementById('post-content').value.trim(),
            categoryId: document.getElementById('post-category').value,
            authorName: document.getElementById('author-name').value.trim(),
            timestamp: new Date().toISOString()
        };
        
        storage.set('postDraft', draftData);
        
        if (!silent) {
            this.showNotification('임시저장되었습니다.');
        }
    }

    loadDraftData() {
        if (this.isEditMode) return;
        
        const draftData = storage.get('postDraft');
        if (!draftData) return;
        
        // Check if draft is not too old (24 hours)
        const draftAge = new Date() - new Date(draftData.timestamp);
        if (draftAge > 24 * 60 * 60 * 1000) {
            this.clearDraft();
            return;
        }
        
        if (confirm('임시저장된 내용이 있습니다. 불러오시겠습니까?')) {
            document.getElementById('post-title').value = draftData.title || '';
            document.getElementById('post-content').value = draftData.content || '';
            document.getElementById('post-category').value = draftData.categoryId || '';
            document.getElementById('author-name').value = draftData.authorName || '';
        }
    }

    clearDraft() {
        storage.remove('postDraft');
    }

    showLoadingModal(message) {
        const modal = document.getElementById('loading-modal');
        const messageElement = document.getElementById('loading-message');
        
        if (modal && messageElement) {
            messageElement.textContent = message;
            modal.style.display = 'flex';
            document.body.style.overflow = 'hidden';
        }
    }

    hideLoadingModal() {
        const modal = document.getElementById('loading-modal');
        if (modal) {
            modal.style.display = 'none';
            document.body.style.overflow = 'auto';
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

// Additional CSS for post form
const postFormStyles = `
<style>
.form-container {
    max-width: 800px;
    margin: 0 auto;
    background: white;
    border-radius: 0.75rem;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
    overflow: hidden;
}

.form-header {
    padding: 2rem;
    border-bottom: 1px solid #e5e7eb;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
    text-align: center;
}

.form-title {
    font-size: 2rem;
    font-weight: 700;
    margin: 0 0 0.5rem 0;
}

.form-description {
    margin: 0;
    opacity: 0.9;
}

.post-form {
    padding: 2rem;
}

.form-section {
    margin-bottom: 2rem;
    padding-bottom: 2rem;
    border-bottom: 1px solid #f3f4f6;
}

.form-section:last-of-type {
    border-bottom: none;
    margin-bottom: 0;
    padding-bottom: 0;
}

.section-title {
    font-size: 1.25rem;
    font-weight: 600;
    color: #1f2937;
    margin: 0 0 1.5rem 0;
    display: flex;
    align-items: center;
    gap: 0.5rem;
}

.section-title::before {
    content: '';
    width: 4px;
    height: 1.25rem;
    background: #2563eb;
    border-radius: 2px;
}

.form-help {
    font-size: 0.875rem;
    color: #6b7280;
    margin-top: 0.25rem;
}

.form-input.error,
.form-textarea.error,
.form-select.error {
    border-color: #dc2626;
    box-shadow: 0 0 0 3px rgba(220, 38, 38, 0.1);
}

.field-error {
    color: #dc2626;
    font-size: 0.875rem;
    margin-top: 0.25rem;
    display: flex;
    align-items: center;
    gap: 0.25rem;
}

.field-error::before {
    content: '⚠';
}

.file-upload-area {
    border: 2px dashed #d1d5db;
    border-radius: 0.5rem;
    padding: 2rem;
    text-align: center;
    cursor: pointer;
    transition: all 0.2s;
    background: #f9fafb;
}

.file-upload-area:hover,
.file-upload-area.drag-over {
    border-color: #2563eb;
    background: #eff6ff;
}

.file-input {
    display: none;
}

.file-upload-content i {
    font-size: 3rem;
    color: #9ca3af;
    margin-bottom: 1rem;
}

.file-upload-content p {
    margin: 0.5rem 0;
    color: #374151;
}

.file-upload-help {
    font-size: 0.875rem;
    color: #6b7280;
}

.image-preview {
    margin-top: 1.5rem;
}

.image-preview h4 {
    font-size: 1rem;
    font-weight: 600;
    color: #374151;
    margin: 0 0 1rem 0;
}

.preview-container {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
    gap: 1rem;
}

.preview-item {
    position: relative;
    border: 1px solid #e5e7eb;
    border-radius: 0.5rem;
    overflow: hidden;
    background: white;
}

.preview-image {
    width: 100%;
    height: 150px;
    object-fit: cover;
}

.preview-info {
    padding: 0.75rem;
    display: flex;
    flex-direction: column;
    gap: 0.25rem;
}

.preview-filename {
    font-size: 0.875rem;
    font-weight: 500;
    color: #374151;
    word-break: break-all;
}

.preview-size {
    font-size: 0.75rem;
    color: #6b7280;
}

.preview-remove {
    position: absolute;
    top: 0.5rem;
    right: 0.5rem;
    background: rgba(220, 38, 38, 0.9);
    color: white;
    border: none;
    border-radius: 50%;
    width: 28px;
    height: 28px;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 0.875rem;
    transition: all 0.2s;
}

.preview-remove:hover {
    background: rgba(220, 38, 38, 1);
    transform: scale(1.1);
}

.form-actions {
    display: flex;
    gap: 1rem;
    justify-content: flex-end;
    padding-top: 2rem;
    border-top: 1px solid #e5e7eb;
    margin-top: 2rem;
}

.modal-loading {
    max-width: 300px;
    text-align: center;
}

.loading-content {
    padding: 2rem;
}

.loading-content i {
    font-size: 2rem;
    color: #2563eb;
    margin-bottom: 1rem;
}

.loading-content p {
    margin: 0;
    color: #374151;
    font-weight: 500;
}

@media (max-width: 768px) {
    .form-container {
        margin: 0 1rem;
    }
    
    .form-header,
    .post-form {
        padding: 1.5rem;
    }
    
    .form-title {
        font-size: 1.5rem;
    }
    
    .form-actions {
        flex-direction: column;
    }
    
    .preview-container {
        grid-template-columns: 1fr;
    }
    
    .file-upload-area {
        padding: 1.5rem;
    }
    
    .file-upload-content i {
        font-size: 2rem;
    }
}

@media (max-width: 480px) {
    .form-container {
        margin: 0 0.5rem;
    }
    
    .form-header,
    .post-form {
        padding: 1rem;
    }
    
    .section-title {
        font-size: 1.125rem;
    }
}
</style>
`;

// Initialize post form page
let postFormPage;

document.addEventListener('DOMContentLoaded', function() {
    // Add post form styles
    document.head.insertAdjacentHTML('beforeend', postFormStyles);
    
    // Initialize post form page
    postFormPage = new PostFormPage();
});
