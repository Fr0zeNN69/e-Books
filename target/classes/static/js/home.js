document.addEventListener('DOMContentLoaded', function() {
    const favoriteButtons = document.querySelectorAll('.favorite-button');

    favoriteButtons.forEach(function(button) {
        button.addEventListener('click', function(event) {
            const bookId = this.getAttribute('data-book-id');
            const isFavorited = this.classList.contains('favorited');
            const url = isFavorited ? '/users/favorites/remove' : '/users/favorites/add';
            const formData = new FormData();
            formData.append('bookId', bookId);

            fetch(url, {
                method: 'POST',
                body: formData,
                headers: {
                    'X-Requested-With': 'XMLHttpRequest'
                }
            })
                .then(response => {
                    if (!response.ok) {
                        console.error('Serverul a returnat o eroare:', response.status, response.statusText);
                        throw new Error('Răspunsul rețelei nu este OK');
                    }
                    return response.text();
                })
                .then(data => {
                    // Schimbă starea butonului
                    this.classList.toggle('favorited');

                    // Dacă suntem pe pagina /home și am eliminat cartea din favorite, o eliminăm din DOM
                    if (!this.classList.contains('favorited') && window.location.pathname === '/home') {
                        const bookItem = this.closest('.book-item');
                        bookItem.parentNode.removeChild(bookItem);

                        // Dacă nu mai există cărți în listă, afișăm mesajul corespunzător
                        if (document.querySelectorAll('.book-item').length === 0) {
                            const messageDiv = document.createElement('div');
                            messageDiv.innerHTML = '<p>You have no favorite books.</p>';
                            document.querySelector('.container').appendChild(messageDiv);
                        }
                    }
                })
                .catch(error => console.error('Error:', error));
        });
    });
});
