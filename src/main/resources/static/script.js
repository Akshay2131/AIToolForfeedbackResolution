class ChatSystem {
    constructor() {
        this.chatMessages = document.getElementById('chatMessages');
        this.userInput = document.getElementById('userInput');
        this.sendButton = document.getElementById('sendButton');
        this.loadingIndicator = document.getElementById('loadingIndicator');
        
        this.setupEventListeners();
        this.scrollToBottom();
    }

    setupEventListeners() {
        // Send button click
        this.sendButton.addEventListener('click', () => this.sendMessage());
        
        // Enter key press
        this.userInput.addEventListener('keypress', (e) => {
            if (e.key === 'Enter' && !e.shiftKey) {
                e.preventDefault();
                this.sendMessage();
            }
        });

        // Input change for button state
        this.userInput.addEventListener('input', () => {
            this.sendButton.disabled = this.userInput.value.trim().length === 0;
        });
    }

    async sendMessage() {
        const message = this.userInput.value.trim();
        if (!message) return;

        // Add user message to chat
        this.addMessage(message, 'user');
        this.userInput.value = '';
        this.sendButton.disabled = true;

        // Show loading indicator
        this.showLoading(true);

        try {
            // Call the API
            const response = await this.callGeminiAPI(message);
            
            // Add bot response to chat
            this.addMessage(response, 'bot');
        } catch (error) {
            console.error('Error:', error);
            this.addMessage('Sorry, I encountered an error while processing your request. Please try again.', 'bot');
        } finally {
            this.showLoading(false);
        }
    }

    async callGeminiAPI(userPrompt) {
        try {
            const response = await fetch('/ask-gemini-flash', {
                method: 'POST',
                headers: {
                    'Content-Type': 'text/plain',
                },
                body: userPrompt
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const result = await response.text();
            
            // Try to parse as JSON to check if it's a data response
            try {
                const jsonResult = JSON.parse(result);
                if (jsonResult.success === false) {
                    return `Error: ${jsonResult.error}`;
                }
                if (jsonResult.data) {
                    return this.formatDataResponse(jsonResult.data);
                }
            } catch (e) {
                // If not JSON, return as plain text
            }

            return result;
        } catch (error) {
            throw new Error(`Failed to communicate with AI service: ${error.message}`);
        }
    }

    formatDataResponse(data) {
        if (!data || data.length === 0) {
            return 'No data found matching your query.';
        }

        if (Array.isArray(data)) {
            if (data.length === 0) {
                return 'No data found matching your query.';
            }

            // Check if it's an array of objects (entity results)
            if (typeof data[0] === 'object' && data[0] !== null) {
                return this.createDataTable(data);
            }

            // Check if it's an array of arrays (scalar results)
            if (Array.isArray(data[0])) {
                return this.createScalarTable(data);
            }

            // Simple array
            return `Found ${data.length} results: ${data.join(', ')}`;
        }

        return `Result: ${JSON.stringify(data, null, 2)}`;
    }

    createDataTable(data) {
        const firstItem = data[0];
        const columns = Object.keys(firstItem);
        
        let tableHTML = '<div class="data-display"><h4>Query Results</h4><table class="data-table"><thead><tr>';
        
        // Add headers
        columns.forEach(col => {
            tableHTML += `<th>${this.formatColumnName(col)}</th>`;
        });
        tableHTML += '</tr></thead><tbody>';

        // Add data rows
        data.forEach(row => {
            tableHTML += '<tr>';
            columns.forEach(col => {
                const value = row[col];
                tableHTML += `<td>${this.formatCellValue(value)}</td>`;
            });
            tableHTML += '</tr>';
        });

        tableHTML += '</tbody></table></div>';
        return tableHTML;
    }

    createScalarTable(data) {
        let tableHTML = '<div class="data-display"><h4>Query Results</h4><table class="data-table"><tbody>';
        
        data.forEach((row, index) => {
            tableHTML += '<tr>';
            if (Array.isArray(row)) {
                row.forEach(cell => {
                    tableHTML += `<td>${this.formatCellValue(cell)}</td>`;
                });
            } else {
                tableHTML += `<td>${this.formatCellValue(row)}</td>`;
            }
            tableHTML += '</tr>';
        });

        tableHTML += '</tbody></table></div>';
        return tableHTML;
    }

    formatColumnName(colName) {
        return colName.replace(/([A-Z])/g, ' $1')
                    .replace(/^./, str => str.toUpperCase())
                    .trim();
    }

    formatCellValue(value) {
        if (value === null || value === undefined) {
            return '<em>null</em>';
        }
        if (typeof value === 'string' && value.length > 100) {
            return value.substring(0, 100) + '...';
        }
        return String(value);
    }

    addMessage(content, sender) {
        const messageDiv = document.createElement('div');
        messageDiv.className = `message ${sender}-message`;
        
        const messageContent = document.createElement('div');
        messageContent.className = 'message-content';
        
        const icon = document.createElement('i');
        icon.className = sender === 'user' ? 'fas fa-user' : 'fas fa-robot';
        
        const textDiv = document.createElement('div');
        textDiv.className = 'text';
        
        if (typeof content === 'string' && content.includes('<table')) {
            textDiv.innerHTML = content;
        } else {
            textDiv.textContent = content;
        }
        
        messageContent.appendChild(icon);
        messageContent.appendChild(textDiv);
        messageDiv.appendChild(messageContent);
        
        this.chatMessages.appendChild(messageDiv);
        this.scrollToBottom();
    }

    showLoading(show) {
        this.loadingIndicator.style.display = show ? 'flex' : 'none';
        this.sendButton.disabled = show;
        this.userInput.disabled = show;
    }

    scrollToBottom() {
        this.chatMessages.scrollTop = this.chatMessages.scrollHeight;
    }
}

// Initialize the chat system when the page loads
document.addEventListener('DOMContentLoaded', () => {
    new ChatSystem();
}); 