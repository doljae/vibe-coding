// Posts list page functionality
class PostsPage {
    constructor() {
        this.currentPage = 1;
        this.postsPerPage = 10;
        this.allPosts = [];
        this.filteredPosts = [];
        this.categories = [];
        this.currentFilter = {
            search: '',
            category: ''
        };

        this.init();
    }

    async init() {
        await this.loadCategories();
        await this.loadPosts();
        this.setupEventListeners();
        this.setupUrlParams();
    }

    async loadCategories() {
        try {
            this.categories = await api.categories.getAll();
            this.renderCategoryFilter();
        } catch (error) {
            console.error('Failed to load categories:', error);
        }
    }

    async loadPosts() {
        const container = document.getElementById('posts-container');
        
        try {
            utils.showLoading(container, '게시글을 불러오는 중...');
            
            this.allPosts = await api.posts.getAll();
            this.filteredPosts = [...this.allPosts];
            
            this.renderPosts();
            this.renderPagination();
        } catch (error) {
            console.error('Failed to load posts:', error);
            utils.showError(container, '게시글을 불러오는데 실패했습니다.');
        }
    }

    renderCategoryFilter() {
        const categoryFilter = document.getElementById('category-filter');
        if (!categoryFilter) return;

        const options = this.categories.map(category => 
            `<option value="${category.id}">${category.name}</option>`
        ).join('');

        categoryFilter.innerHTML = `
            <option value="">모든 카테고리</option>
            ${options}
        `;
    }

    renderPosts() {
        const container = document.getElementById('posts-container');
        if (!container) return;

        if (this.filteredPosts.length === 0) {
            container.innerHTML = `
                <div class="empty-state">
                    <i class="fas fa-inbox"></i>
                    <h3>게시글이 없습니다</h3>
                    <p>첫 번째 게시글을 작성해보세요!</p>
                    <a href="/post-form.html" class="btn btn-primary">
                        <i class="fas fa-pen"></i>
                        글쓰기
                    </a>
                </div>
            `;
            return;
        }

        const startIndex = (this.currentPage - 1) * this.postsPerPage;
        const endIndex = startIndex + this.postsPerPage;
        const postsToShow = this.filteredPosts.slice(startIndex, endIndex);

        container.innerHTML = `
            <div class="posts-grid">
                ${postsToShow.map(post => this.renderPostCard(post)).join('')}
            </div>
        `;
    }

    renderPostCard(post) {
        return `
            <div class="post-card">
                <div class="post-card-header">
                    <h3 class="post-card-title">
                        <a href="/post-detail.html?id=${post.id}">
                            ${post.title}
                        </a>
                    </h3>
                    <div class="post-card-category">
                        <span class="category-badge">${post.category.name}</span>
                    </div>
                </div>
                
                <div class="post-card-meta">
                    <div class="post-author">
                        <i class="fas fa-user"></i>
                        <span>${post.author.name}</span>
                    </div>
                    <div class="post-date">
                        <i class="fas fa-clock"></i>
                        <span>${utils.formatRelativeTime(post.createdAt)}</span>
                    </div>
                </div>

                <div class="post-card-stats">
                    ${post.imageCount > 0 ? `
                        <div class="stat-item">
                            <i class="fas fa-images"></i>
                            <span>${post.imageCount}</span>
                        </div>
                    ` : ''}
                    <div class="stat-item">
                        <i class="fas fa-heart"></i>
                        <span>${post.likeCount}</span>
                    </div>
                    <div class="stat-item">
                        <i class="fas fa-calendar"></i>
                        <span>${utils.formatDate(post.createdAt)}</span>
                    </div>
                </div>

                <div class="post-card-actions">
                    <a href="/post-detail.html?id=${post.id}" class="btn btn-outline btn-sm">
                        <i class="fas fa-eye"></i>
                        자세히 보기
                    </a>
                </div>
            </div>
        `;
    }

    renderPagination() {
        const paginationContainer = document.getElementById('pagination-container');
        if (!paginationContainer) return;

        const totalPages = Math.ceil(this.filteredPosts.length / this.postsPerPage);
        
        if (totalPages <= 1) {
            paginationContainer.style.display = 'none';
            return;
        }

        paginationContainer.style.display = 'block';

        const prevBtn = document.getElementById('prev-page');
        const nextBtn = document.getElementById('next-page');
        const pageNumbers = document.getElementById('page-numbers');

        // Update prev/next buttons
        prevBtn.disabled = this.currentPage === 1;
        nextBtn.disabled = this.currentPage === totalPages;

        // Generate page numbers
        const pageNumbersHtml = [];
        const maxVisiblePages = 5;
        let startPage = Math.max(1, this.currentPage - Math.floor(maxVisiblePages / 2));
        let endPage = Math.min(totalPages, startPage + maxVisiblePages - 1);

        if (endPage - startPage + 1 < maxVisiblePages) {
            startPage = Math.max(1, endPage - maxVisiblePages + 1);
        }

        if (startPage > 1) {
            pageNumbersHtml.push(`
                <button class="page-number" data-page="1">1</button>
                ${startPage > 2 ? '<span class="page-ellipsis">...</span>' : ''}
            `);
        }

        for (let i = startPage; i <= endPage; i++) {
            pageNumbersHtml.push(`
                <button class="page-number ${i === this.currentPage ? 'active' : ''}" data-page="${i}">
                    ${i}
                </button>
            `);
        }

        if (endPage < totalPages) {
            pageNumbersHtml.push(`
                ${endPage < totalPages - 1 ? '<span class="page-ellipsis">...</span>' : ''}
                <button class="page-number" data-page="${totalPages}">${totalPages}</button>
            `);
        }

        pageNumbers.innerHTML = pageNumbersHtml.join('');
    }

    setupEventListeners() {
        // Search functionality
        const searchInput = document.getElementById('search-input');
        const searchBtn = document.getElementById('search-btn');

        if (searchInput && searchBtn) {
            const debouncedSearch = utils.debounce(() => this.handleSearch(), 300);
            
            searchInput.addEventListener('input', debouncedSearch);
            searchInput.addEventListener('keypress', (e) => {
                if (e.key === 'Enter') {
                    this.handleSearch();
                }
            });
            
            searchBtn.addEventListener('click', () => this.handleSearch());
        }

        // Category filter
        const categoryFilter = document.getElementById('category-filter');
        if (categoryFilter) {
            categoryFilter.addEventListener('change', () => this.handleCategoryFilter());
        }

        // Clear filter
        const clearFilterBtn = document.getElementById('clear-filter-btn');
        if (clearFilterBtn) {
            clearFilterBtn.addEventListener('click', () => this.clearFilters());
        }

        // Pagination
        const prevBtn = document.getElementById('prev-page');
        const nextBtn = document.getElementById('next-page');
        const pageNumbers = document.getElementById('page-numbers');

        if (prevBtn) {
            prevBtn.addEventListener('click', () => this.goToPage(this.currentPage - 1));
        }

        if (nextBtn) {
            nextBtn.addEventListener('click', () => this.goToPage(this.currentPage + 1));
        }

        if (pageNumbers) {
            pageNumbers.addEventListener('click', (e) => {
                if (e.target.classList.contains('page-number')) {
                    const page = parseInt(e.target.dataset.page);
                    this.goToPage(page);
                }
            });
        }
    }

    setupUrlParams() {
        const params = utils.getUrlParams();
        
        if (params.search) {
            const searchInput = document.getElementById('search-input');
            if (searchInput) {
                searchInput.value = params.search;
                this.currentFilter.search = params.search;
            }
        }

        if (params.category) {
            const categoryFilter = document.getElementById('category-filter');
            if (categoryFilter) {
                categoryFilter.value = params.category;
                this.currentFilter.category = params.category;
            }
        }

        if (params.page) {
            this.currentPage = parseInt(params.page) || 1;
        }

        // Apply filters if any
        if (this.currentFilter.search || this.currentFilter.category) {
            this.applyFilters();
        }
    }

    handleSearch() {
        const searchInput = document.getElementById('search-input');
        if (!searchInput) return;

        this.currentFilter.search = searchInput.value.trim();
        this.currentPage = 1;
        this.applyFilters();
        this.updateUrl();
    }

    handleCategoryFilter() {
        const categoryFilter = document.getElementById('category-filter');
        if (!categoryFilter) return;

        this.currentFilter.category = categoryFilter.value;
        this.currentPage = 1;
        this.applyFilters();
        this.updateUrl();
    }

    applyFilters() {
        this.filteredPosts = this.allPosts.filter(post => {
            const matchesSearch = !this.currentFilter.search || 
                post.title.toLowerCase().includes(this.currentFilter.search.toLowerCase());
            
            const matchesCategory = !this.currentFilter.category || 
                post.category.id === this.currentFilter.category;

            return matchesSearch && matchesCategory;
        });

        this.renderPosts();
        this.renderPagination();
    }

    clearFilters() {
        const searchInput = document.getElementById('search-input');
        const categoryFilter = document.getElementById('category-filter');

        if (searchInput) searchInput.value = '';
        if (categoryFilter) categoryFilter.value = '';

        this.currentFilter = { search: '', category: '' };
        this.currentPage = 1;
        this.filteredPosts = [...this.allPosts];
        
        this.renderPosts();
        this.renderPagination();
        this.updateUrl();
    }

    goToPage(page) {
        const totalPages = Math.ceil(this.filteredPosts.length / this.postsPerPage);
        
        if (page < 1 || page > totalPages) return;
        
        this.currentPage = page;
        this.renderPosts();
        this.renderPagination();
        this.updateUrl();

        // Scroll to top
        window.scrollTo({ top: 0, behavior: 'smooth' });
    }

    updateUrl() {
        const params = new URLSearchParams();
        
        if (this.currentFilter.search) {
            params.set('search', this.currentFilter.search);
        }
        
        if (this.currentFilter.category) {
            params.set('category', this.currentFilter.category);
        }
        
        if (this.currentPage > 1) {
            params.set('page', this.currentPage.toString());
        }

        const newUrl = `${window.location.pathname}${params.toString() ? '?' + params.toString() : ''}`;
        window.history.replaceState({}, '', newUrl);
    }
}

// Additional CSS for posts page
const additionalStyles = `
<style>
.page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 2rem;
    padding-bottom: 1rem;
    border-bottom: 1px solid #e5e7eb;
}

.page-title {
    font-size: 2rem;
    font-weight: 700;
    color: #1f2937;
    margin: 0;
}

.page-actions {
    display: flex;
    gap: 1rem;
}

.search-section {
    background: white;
    padding: 1.5rem;
    border-radius: 0.75rem;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
    margin-bottom: 2rem;
}

.search-form {
    display: flex;
    flex-direction: column;
    gap: 1rem;
}

.search-input-group {
    display: flex;
    gap: 1rem;
}

.search-input-group .form-input {
    flex: 1;
}

.filter-group {
    display: flex;
    gap: 1rem;
    align-items: center;
}

.filter-group .form-select {
    min-width: 200px;
}

.posts-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
    gap: 1.5rem;
}

.post-card {
    background: white;
    border-radius: 0.75rem;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
    padding: 1.5rem;
    transition: all 0.2s;
    border: 1px solid #e5e7eb;
}

.post-card:hover {
    box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
    transform: translateY(-2px);
}

.post-card-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 1rem;
    gap: 1rem;
}

.post-card-title {
    margin: 0;
    flex: 1;
}

.post-card-title a {
    text-decoration: none;
    color: #1f2937;
    font-size: 1.1rem;
    font-weight: 600;
    line-height: 1.4;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
}

.post-card-title a:hover {
    color: #2563eb;
}

.category-badge {
    background-color: #eff6ff;
    color: #2563eb;
    padding: 0.25rem 0.75rem;
    border-radius: 1rem;
    font-size: 0.75rem;
    font-weight: 500;
    white-space: nowrap;
}

.post-card-meta {
    display: flex;
    justify-content: space-between;
    margin-bottom: 1rem;
    font-size: 0.875rem;
    color: #6b7280;
}

.post-author,
.post-date {
    display: flex;
    align-items: center;
    gap: 0.25rem;
}

.post-card-stats {
    display: flex;
    gap: 1rem;
    margin-bottom: 1rem;
    font-size: 0.875rem;
    color: #6b7280;
}

.stat-item {
    display: flex;
    align-items: center;
    gap: 0.25rem;
}

.post-card-actions {
    display: flex;
    justify-content: flex-end;
}

.btn-sm {
    padding: 0.5rem 1rem;
    font-size: 0.875rem;
}

.pagination-container {
    margin-top: 3rem;
    display: flex;
    justify-content: center;
}

.pagination {
    display: flex;
    align-items: center;
    gap: 0.5rem;
}

.pagination-btn {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    padding: 0.75rem 1rem;
    border: 1px solid #d1d5db;
    background: white;
    color: #374151;
    border-radius: 0.5rem;
    cursor: pointer;
    transition: all 0.2s;
}

.pagination-btn:hover:not(:disabled) {
    background-color: #f3f4f6;
    border-color: #9ca3af;
}

.pagination-btn:disabled {
    opacity: 0.5;
    cursor: not-allowed;
}

.page-numbers {
    display: flex;
    gap: 0.25rem;
}

.page-number {
    padding: 0.75rem 1rem;
    border: 1px solid #d1d5db;
    background: white;
    color: #374151;
    border-radius: 0.5rem;
    cursor: pointer;
    transition: all 0.2s;
    min-width: 44px;
    text-align: center;
}

.page-number:hover {
    background-color: #f3f4f6;
    border-color: #9ca3af;
}

.page-number.active {
    background-color: #2563eb;
    border-color: #2563eb;
    color: white;
}

.page-ellipsis {
    padding: 0.75rem 0.5rem;
    color: #6b7280;
}

.empty-state {
    text-align: center;
    padding: 4rem 2rem;
    color: #6b7280;
}

.empty-state i {
    font-size: 4rem;
    margin-bottom: 1rem;
    color: #d1d5db;
}

.empty-state h3 {
    font-size: 1.5rem;
    margin-bottom: 0.5rem;
    color: #374151;
}

.empty-state p {
    margin-bottom: 2rem;
}

@media (max-width: 768px) {
    .page-header {
        flex-direction: column;
        gap: 1rem;
        align-items: flex-start;
    }
    
    .search-input-group {
        flex-direction: column;
    }
    
    .filter-group {
        flex-direction: column;
        align-items: stretch;
    }
    
    .filter-group .form-select {
        min-width: auto;
    }
    
    .posts-grid {
        grid-template-columns: 1fr;
    }
    
    .post-card-header {
        flex-direction: column;
        gap: 0.5rem;
    }
    
    .post-card-meta {
        flex-direction: column;
        gap: 0.5rem;
    }
    
    .pagination {
        flex-wrap: wrap;
        justify-content: center;
    }
    
    .page-numbers {
        order: -1;
        width: 100%;
        justify-content: center;
        margin-bottom: 1rem;
    }
}
</style>
`;

// Initialize posts page when DOM is loaded
document.addEventListener('DOMContentLoaded', function() {
    // Add additional styles
    document.head.insertAdjacentHTML('beforeend', additionalStyles);
    
    // Initialize posts page
    new PostsPage();
});

