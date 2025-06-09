
class VacancyApp {
    constructor() {
        this.vacancies = [];
        this.filteredVacancies = [];
        this.cities = [];
        this.companies = [];
        this.sortField = null;
        this.sortDirection = 'asc';
        
        this.init();
    }

    async init() {
        this.initTooltips();
        await this.loadInitialData();
        this.loadVacanciesFromStorage();
        this.bindEvents();
    }

    initTooltips() {
        // Initialize Bootstrap tooltips
        const tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
        tooltipTriggerList.map(function (tooltipTriggerEl) {
            return new bootstrap.Tooltip(tooltipTriggerEl);
        });
    }

    bindEvents() {
        console.log('Binding events...');
        
        // Search button
        const searchBtn = document.getElementById('searchBtn');
        if (searchBtn) {
            searchBtn.addEventListener('click', () => this.searchVacancies());
        }
        
        // Clear filters button
        const clearFiltersBtn = document.getElementById('clearFiltersBtn');
        if (clearFiltersBtn) {
            clearFiltersBtn.addEventListener('click', () => this.clearFilters());
        }
        
        // Refresh button
        const refreshBtn = document.getElementById('refreshBtn');
        if (refreshBtn) {
            refreshBtn.addEventListener('click', () => this.refreshVacancies());
        }
        
        // Enter key in search input
        const keywordFilter = document.getElementById('keywordFilter');
        if (keywordFilter) {
            keywordFilter.addEventListener('keypress', (e) => {
                if (e.key === 'Enter') {
                    this.searchVacancies();
                }
            });
        }

        // Filter changes
        const cityFilter = document.getElementById('cityFilter');
        if (cityFilter) {
            cityFilter.addEventListener('change', () => this.searchVacancies());
        }
        
        const companyFilter = document.getElementById('companyFilter');
        if (companyFilter) {
            companyFilter.addEventListener('change', () => this.searchVacancies());
        }
        
        const salaryFromFilter = document.getElementById('salaryFromFilter');
        if (salaryFromFilter) {
            salaryFromFilter.addEventListener('input', () => this.searchVacancies());
        }
        
        const salaryToFilter = document.getElementById('salaryToFilter');
        if (salaryToFilter) {
            salaryToFilter.addEventListener('input', () => this.searchVacancies());
        }
        
        // Pages input validation
        const pagesInput = document.getElementById('pagesInput');
        if (pagesInput) {
            pagesInput.addEventListener('input', (e) => {
                const value = parseInt(e.target.value);
                if (value < 1) e.target.value = 1;
                if (value > 100) e.target.value = 100;
            });
        }
        
        console.log('Events bound successfully!');
    }

    async loadInitialData() {
        try {
            this.showLoading(true);
            
            // Load cities and companies for filters
            await Promise.all([
                this.loadCities(),
                this.loadCompanies(),
                this.loadVacancies()
            ]);
            
        } catch (error) {
            console.error('Error loading initial data:', error);
            this.showError('Ошибка загрузки данных');
        } finally {
            this.showLoading(false);
        }
    }

    async loadVacancies() {
        try {
            const response = await fetch('/api/vacancies');
            if (!response.ok) throw new Error('Failed to fetch vacancies');
            
            this.vacancies = await response.json();
            this.filteredVacancies = [...this.vacancies];
            
            this.saveVacanciesToStorage();
            this.updateStatistics();
            this.renderVacancies();
            
        } catch (error) {
            console.error('Error loading vacancies:', error);
            // Try to load from local storage if API fails
            this.loadVacanciesFromStorage();
        }
    }

    async loadCities() {
        try {
            const response = await fetch('/api/cities');
            if (!response.ok) throw new Error('Failed to fetch cities');
            
            this.cities = await response.json();
            this.populateCityFilter();
            
        } catch (error) {
            console.error('Error loading cities:', error);
        }
    }

    async loadCompanies() {
        try {
            const response = await fetch('/api/companies');
            if (!response.ok) throw new Error('Failed to fetch companies');
            
            this.companies = await response.json();
            this.populateCompanyFilter();
            
        } catch (error) {
            console.error('Error loading companies:', error);
        }
    }

    async searchVacancies() {
        const city = document.getElementById('cityFilter').value.trim();
        const company = document.getElementById('companyFilter').value.trim();
        const keyword = document.getElementById('keywordFilter').value.trim();
        const salaryFrom = document.getElementById('salaryFromFilter').value.trim();
        const salaryTo = document.getElementById('salaryToFilter').value.trim();

        try {
            this.showLoading(true);
            
            const params = new URLSearchParams();
            // Добавляем параметры только если они не пустые
            if (city && city !== '') params.append('city', city);
            if (company && company !== '') params.append('company', company);
            if (keyword && keyword !== '') params.append('keyword', keyword);
            if (salaryFrom && salaryFrom !== '') params.append('salaryFrom', salaryFrom);
            if (salaryTo && salaryTo !== '') params.append('salaryTo', salaryTo);

            const response = await fetch(`/api/vacancies/search?${params}`);
            if (!response.ok) {
                const errorText = await response.text();
                throw new Error(`Search failed: ${response.status} - ${errorText}`);
            }
            
            this.filteredVacancies = await response.json();
            this.renderVacancies();
            this.updateStatistics();
            
        } catch (error) {
            console.error('Error searching vacancies:', error);
            this.showError(`Ошибка поиска вакансий: ${error.message}`);
        } finally {
            this.showLoading(false);
        }
    }

    populateCityFilter() {
        const select = document.getElementById('cityFilter');
        if (!select) {
            console.error('City filter element not found');
            return;
        }
        
        select.innerHTML = '<option value="">Все города</option>';
        
        this.cities.forEach(city => {
            const option = document.createElement('option');
            option.value = city;
            option.textContent = city;
            select.appendChild(option);
        });
    }

    populateCompanyFilter() {
        const select = document.getElementById('companyFilter');
        if (!select) {
            console.error('Company filter element not found');
            return;
        }
        
        select.innerHTML = '<option value="">Все компании</option>';
        
        this.companies.forEach(company => {
            const option = document.createElement('option');
            option.value = company;
            option.textContent = company;
            select.appendChild(option);
        });
    }

    renderVacancies() {
        this.renderTableView();
    }

    renderTableView() {
        const tbody = document.getElementById('vacanciesTableBody');
        if (!tbody) {
            console.error('Table body element not found');
            return;
        }
        
        tbody.innerHTML = '';

        this.filteredVacancies.forEach(vacancy => {
            const row = document.createElement('tr');
            row.innerHTML = `
                <td>
                    <a href="#" class="vacancy-title" onclick="app.showVacancyDetails(${vacancy.id})">
                        ${this.escapeHtml(vacancy.title)}
                    </a>
                </td>
                <td><span class="company-badge">${this.escapeHtml(vacancy.company)}</span></td>
                <td><span class="city-badge">${this.escapeHtml(vacancy.city)}</span></td>
                <td>${vacancy.salary ? `<span class="salary-badge">${this.escapeHtml(vacancy.salary)}</span>` : 'Не указана'}</td>
                <td><span class="date-text">${this.formatDate(vacancy.publicationDate)}</span></td>
                <td><span class="source-badge">${this.escapeHtml(vacancy.source || 'trudvsem.ru')}</span></td>
                <td>
                    <button class="btn btn-sm btn-outline-primary" onclick="app.showVacancyDetails(${vacancy.id})">
                        <i class="fas fa-eye"></i> Подробнее
                    </button>
                </td>
            `;
            tbody.appendChild(row);
        });
    }

    showVacancyDetails(vacancyId) {
        const vacancy = this.vacancies.find(v => v.id === vacancyId) || 
                       this.filteredVacancies.find(v => v.id === vacancyId);
        
        if (!vacancy) return;

        const modalTitle = document.getElementById('vacancyModalTitle');
        const modalBody = document.getElementById('vacancyModalBody');
        const vacancyLink = document.getElementById('vacancyLink');
        
        if (!modalTitle || !modalBody || !vacancyLink) {
            console.error('Modal elements not found');
            return;
        }

        modalTitle.textContent = vacancy.title;
        vacancyLink.href = vacancy.link;
        
        modalBody.innerHTML = `
            <div class="row mb-3">
                <div class="col-md-6">
                    <strong>Компания:</strong> ${this.escapeHtml(vacancy.company)}
                </div>
                <div class="col-md-6">
                    <strong>Город:</strong> ${this.escapeHtml(vacancy.city)}
                </div>
            </div>
            <div class="row mb-3">
                <div class="col-md-6">
                    <strong>Зарплата:</strong> ${vacancy.salary ? this.escapeHtml(vacancy.salary) : 'Не указана'}
                </div>
                <div class="col-md-6">
                    <strong>Дата публикации:</strong> ${this.formatDate(vacancy.publicationDate)}
                </div>
            </div>
            <div class="row mb-3">
                <div class="col-md-6">
                    <strong>Источник:</strong> <span class="source-badge">${this.escapeHtml(vacancy.source || 'trudvsem.ru')}</span>
                </div>
            </div>
            <div class="description-section">
                <h6><i class="fas fa-info-circle me-2"></i>Описание вакансии</h6>
                <p>${this.escapeHtml(vacancy.description).replace(/\n/g, '<br>')}</p>
            </div>
            <div class="requirements-section">
                <h6><i class="fas fa-list-check me-2"></i>Требования</h6>
                <p>${this.escapeHtml(vacancy.requirements).replace(/\n/g, '<br>')}</p>
            </div>
        `;

        const modalElement = document.getElementById('vacancyModal');
        if (modalElement && typeof bootstrap !== 'undefined') {
            const modal = new bootstrap.Modal(modalElement);
            modal.show();
        }
    }

    updateStatistics() {
        const totalVacancies = document.getElementById('totalVacancies');
        const totalCities = document.getElementById('totalCities');
        const totalCompanies = document.getElementById('totalCompanies');
        
        if (totalVacancies) {
            totalVacancies.textContent = this.filteredVacancies.length;
        }
        if (totalCities) {
            totalCities.textContent = this.cities.length;
        }
        if (totalCompanies) {
            totalCompanies.textContent = this.companies.length;
        }
    }

    saveVacanciesToStorage() {
        try {
            localStorage.setItem('vacancies', JSON.stringify(this.vacancies));
            localStorage.setItem('lastUpdate', new Date().toISOString());
        } catch (error) {
            console.error('Error saving to localStorage:', error);
        }
    }

    loadVacanciesFromStorage() {
        try {
            const stored = localStorage.getItem('vacancies');
            if (stored) {
                this.vacancies = JSON.parse(stored);
                this.filteredVacancies = [...this.vacancies];
                this.renderVacancies();
                this.updateStatistics();
                
                const lastUpdate = localStorage.getItem('lastUpdate');
                if (lastUpdate) {
                    console.log('Loaded from storage, last update:', lastUpdate);
                }
            }
        } catch (error) {
            console.error('Error loading from localStorage:', error);
        }
    }

    async refreshVacancies() {
        const refreshBtn = document.getElementById('refreshBtn');
        const refreshIndicator = document.getElementById('refreshIndicator');
        const pagesInput = document.getElementById('pagesInput');
        
        if (!refreshBtn || !refreshIndicator || !pagesInput) {
            console.error('Required elements not found for refresh functionality');
            return;
        }
        
        let pages = parseInt(pagesInput.value);
        
        // Валидация ввода
        if (isNaN(pages) || pages < 1) {
            pages = 1;
            pagesInput.value = 1;
        }
        if (pages > 100) {
            pages = 100;
            pagesInput.value = 100;
        }
        
        try {
            // Disable button and show loading state
            refreshBtn.disabled = true;
            refreshBtn.classList.add('btn-refresh-loading');
            refreshIndicator.style.display = 'block';
            
            this.showMessage(`Запуск обновления данных (${pages} страниц)...`, 'info');
            
            // Call the parse API with pages parameter
            const response = await fetch(`/api/parse-vacancies?pages=${pages}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                }
            });
            
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            
            const result = await response.text();
            
            // Reload data after successful parsing
            await this.loadInitialData();
            
            this.showMessage(`Данные успешно обновлены! Обработано ${pages} страниц с trudvsem.ru`, 'success');
            
        } catch (error) {
            console.error('Error refreshing vacancies:', error);
            this.showMessage('Ошибка при обновлении данных: ' + error.message, 'danger');
        } finally {
            // Re-enable button and hide loading state
            refreshBtn.disabled = false;
            refreshBtn.classList.remove('btn-refresh-loading');
            refreshIndicator.style.display = 'none';
        }
    }

    showLoading(show) {
        const indicator = document.getElementById('loadingIndicator');
        if (indicator) {
            indicator.style.display = show ? 'block' : 'none';
        }
    }

    showMessage(message, type = 'info') {
        const container = document.getElementById('messageContainer');
        if (!container) {
            console.error('Message container element not found');
            return;
        }
        
        const alertClass = `alert-${type}`;
        
        container.innerHTML = `
            <div class="alert ${alertClass} alert-dismissible fade show" role="alert">
                <i class="fas fa-${this.getIconForType(type)} me-2"></i>
                ${message}
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            </div>
        `;
        
        container.style.display = 'block';
        
        // Auto-hide success messages after 5 seconds
        if (type === 'success' || type === 'info') {
            setTimeout(() => {
                const alert = container.querySelector('.alert');
                if (alert) {
                    const bsAlert = new bootstrap.Alert(alert);
                    bsAlert.close();
                }
            }, 5000);
        }
    }

    getIconForType(type) {
        switch (type) {
            case 'success': return 'check-circle';
            case 'danger': return 'exclamation-triangle';
            case 'info': return 'info-circle';
            default: return 'info-circle';
        }
    }

    showError(message) {
        this.showMessage(message, 'danger');
    }

    formatDate(dateString) {
        const date = new Date(dateString);
        return date.toLocaleDateString('ru-RU', {
            year: 'numeric',
            month: 'short',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        });
    }

    escapeHtml(text) {
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }

    sortBy(field) {
        if (this.sortField === field) {
            this.sortDirection = this.sortDirection === 'asc' ? 'desc' : 'asc';
        } else {
            this.sortField = field;
            this.sortDirection = 'asc';
        }

        this.filteredVacancies.sort((a, b) => {
            let valueA, valueB;
            
            if (field === 'salary') {
                // Извлекаем числовые значения из зарплаты для сортировки
                valueA = this.extractSalaryNumber(a.salary);
                valueB = this.extractSalaryNumber(b.salary);
            } else if (field === 'publicationDate') {
                valueA = new Date(a.publicationDate);
                valueB = new Date(b.publicationDate);
            } else {
                valueA = a[field] || '';
                valueB = b[field] || '';
            }

            if (valueA < valueB) return this.sortDirection === 'asc' ? -1 : 1;
            if (valueA > valueB) return this.sortDirection === 'asc' ? 1 : -1;
            return 0;
        });

        this.updateSortIcons(field);
        this.renderVacancies();
    }

    extractSalaryNumber(salary) {
        if (!salary) return 0;
        // Извлекаем первое число из строки зарплаты
        const match = salary.match(/\d+/);
        return match ? parseInt(match[0]) : 0;
    }

    updateSortIcons(activeField) {
        // Сбрасываем все иконки
        const salaryIcon = document.getElementById('salary-sort-icon');
        const dateIcon = document.getElementById('date-sort-icon');
        
        if (salaryIcon) {
            salaryIcon.className = 'fas fa-sort';
        }
        if (dateIcon) {
            dateIcon.className = 'fas fa-sort';
        }
        
        // Устанавливаем активную иконку
        const iconId = activeField === 'salary' ? 'salary-sort-icon' : 'date-sort-icon';
        const icon = document.getElementById(iconId);
        if (icon) {
            icon.className = this.sortDirection === 'asc' ? 'fas fa-sort-up' : 'fas fa-sort-down';
        }
    }

    clearFilters() {
        // Сбрасываем все фильтры
        const cityFilter = document.getElementById('cityFilter');
        const companyFilter = document.getElementById('companyFilter');
        const keywordFilter = document.getElementById('keywordFilter');
        const salaryFromFilter = document.getElementById('salaryFromFilter');
        const salaryToFilter = document.getElementById('salaryToFilter');
        
        if (cityFilter) cityFilter.value = '';
        if (companyFilter) companyFilter.value = '';
        if (keywordFilter) keywordFilter.value = '';
        if (salaryFromFilter) salaryFromFilter.value = '';
        if (salaryToFilter) salaryToFilter.value = '';
        
        // Показываем все вакансии
        this.filteredVacancies = [...this.vacancies];
        this.renderVacancies();
        this.updateStatistics();
    }
}

// Initialize the application when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    window.app = new VacancyApp();
}); 