document.addEventListener('DOMContentLoaded', function () {
    const messagesContainer = document.querySelector('.chat-messages');
    const contactsList = document.querySelector('.contacts-list');
    const newChatButton = document.getElementById('new-chat-button');
    const currentUserId = parseInt(document.getElementById('currentUserId').value);
    const messageInput = document.querySelector('.message-input');
    const sendButton = document.querySelector('.send-button');
    const searchInput = document.querySelector('#search-input');

    let allUsers = [];

    // Load users list
    function loadUsers() {
        fetch('/user/users/list')
            .then(response => response.json())
            .then(users => {
                allUsers = users.filter(user => user.id !== currentUserId);
                displayUsers(allUsers);
            })
            .catch(err => console.error('Error loading user list:', err));
    }

    // Display users in the contact list
    function displayUsers(users) {
        contactsList.innerHTML = '';
        users.forEach(user => {
            const contactDiv = document.createElement('div');
            contactDiv.className = 'contact-item';
            contactDiv.dataset.userId = user.id;

            const initials = `${user.firstName.charAt(0)}${user.lastName.charAt(0)}`;
            contactDiv.innerHTML = `
                <div class="contact-avatar">${initials}</div>
                <div class="contact-info">
                    <div class="contact-name">${user.firstName} ${user.lastName}
                        <span class="online-status ${user.online ? '' : 'offline-status'}"></span>
                    </div>
                </div>
            `;

            contactDiv.addEventListener('click', () => {
                document.querySelectorAll('.contact-item').forEach(item => item.classList.remove('active'));
                contactDiv.classList.add('active');
                loadMessages(user.id);
                updateChatHeader(user);
            });

            contactsList.appendChild(contactDiv);
        });
    }
    searchInput.addEventListener('input', function () {
        const query = searchInput.value.toLowerCase();

        const filteredUsers = allUsers.filter(user => {
            const fullName = `${user.firstName} ${user.lastName}`.toLowerCase();
            console.log(fullName);
            return fullName.includes(query) ||
                user.firstName.toLowerCase().includes(query) ||
                user.lastName.toLowerCase().includes(query);
        });

        displayUsers(filteredUsers);
    });

    // Update chat header
    function updateChatHeader(user) {
        const chatHeader = document.querySelector('.chat-header');
        chatHeader.innerHTML = `
            <div class="flex items-center">
                <div class="contact-avatar">${user.firstName.charAt(0)}${user.lastName.charAt(0)}</div>
                <div class="ml-3">
                    <div class="font-semibold">${user.firstName} ${user.lastName}</div>
                    <div class="text-sm text-gray-500">${user.online ? 'Çevrimiçi' : 'Çevrimdışı'}</div>
                </div>
            </div>
        `;
    }

    // Load messages
    function loadMessages(receiverId) {
        fetch(`/sohbet/messages?receiverId=${receiverId}`)
            .then(response => response.json())
            .then(messages => {
                messagesContainer.innerHTML = '';
                messages.forEach(message => {
                    const messageElement = createMessageElement(message);
                    messagesContainer.appendChild(messageElement);
                });
                messagesContainer.scrollTop = messagesContainer.scrollHeight;
            })
            .catch(err => console.error('Error loading messages:', err));
    }

    // Create message element
    function createMessageElement(message) {
        const messageDiv = document.createElement('div');
        const isReceived = message.senderId !== currentUserId;
        messageDiv.className = `message ${isReceived ? 'message-received' : 'message-sent'}`;
        messageDiv.dataset.messageId = message.id;

        messageDiv.innerHTML = `
            <div class="message-content">${message.messageText}</div>
            <div class="message-time">${formatMessageTime(message.sentAt)}</div>
            ${!isReceived ? '<div class="read-status">Okundu</div>' : ''}
            ${!isReceived ? `
                <div class="message-actions">
                    <span class="action-button edit-message">✏️</span>
                    <span class="action-button delete-message">🗑️</span>
                </div>
            ` : ''}
        `;

        // Add event listeners for edit and delete actions
        const editButton = messageDiv.querySelector('.edit-message');
        const deleteButton = messageDiv.querySelector('.delete-message');

        if (editButton) {
            editButton.addEventListener('click', () => editMessage(message));
        }

        if (deleteButton) {
            deleteButton.addEventListener('click', () => deleteMessage(message.id));
        }

        return messageDiv;
    }

    // Format message time
    function formatMessageTime(timestamp) {
        const date = new Date(timestamp);
        return date.toLocaleTimeString('tr-TR', { hour: '2-digit', minute: '2-digit' });
    }

    // Send message
    sendButton.addEventListener('click', () => {
        const activeContact = document.querySelector('.contact-item.active');
        if (activeContact && messageInput.value.trim() !== '') {
            const receiverId = parseInt(activeContact.dataset.userId);
            const message = messageInput.value.trim();
            sendMessage(receiverId, message)
                .then(() => {
                    messageInput.value = ''; // Clear the input field
                    loadMessages(receiverId); // Refresh messages
                })
                .catch(err => console.error('Error sending message:', err));
        }
    });

    // Send message request
    function sendMessage(receiverId, message) {
        return fetch('/sohbet/send', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: new URLSearchParams({ message, receiverId })
        }).then(response => response.json());
    }

    // Edit message
    function editMessage(message) {
        const newText = prompt('Yeni mesajı girin:', message.messageText);
        if (newText && newText.trim() !== '') {
            fetch(`/sohbet/update/${message.id}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ message: newText.trim() })
            })
                .then(response => response.json())
                .then(data => {
                    if (data.success) {
                        alert('Mesaj başarıyla güncellendi.');
                        loadMessages(message.receiverId); // Mesajları yeniden yükleyin
                    } else {
                        alert(`Mesaj güncellenemedi: ${data.error}`);
                    }
                })
                .catch(err => {
                    console.error('Error editing message:', err);
                    alert('Bir hata oluştu. Lütfen tekrar deneyin.');
                });
        }
    }


    // Delete message
    function deleteMessage(messageId) {
        if (confirm('Bu mesajı silmek istediğinize emin misiniz?')) {
            fetch(`/sohbet/delete/${messageId}`, { method: 'DELETE' })
                .then(() => {
                    const messageDiv = document.querySelector(`.message[data-message-id="${messageId}"]`);
                    if (messageDiv) messageDiv.remove();
                })
                .catch(err => console.error('Error deleting message:', err));
        }
    }

    loadUsers();
});