document.addEventListener('DOMContentLoaded', function() {
    const likeButtons = document.querySelectorAll('.like-btn');
    const dislikeButtons = document.querySelectorAll('.dislike-btn');

    likeButtons.forEach(function(button) {
        button.addEventListener('click', function() {
            const reviewId = this.getAttribute('data-review-id');
            fetch(`/reviews/like/${reviewId}`, {
                method: 'POST',
                headers: {
                    'X-CSRF-TOKEN': document.querySelector('meta[name="_csrf"]').getAttribute('content')
                }
            })
                .then(response => response.json())
                .then(data => {
                    if (data.likes !== undefined) {
                        this.querySelector('span').textContent = data.likes;
                    }
                    if (data.dislikes !== undefined) {
                        this.closest('.review').querySelector('.dislike-btn span').textContent = data.dislikes;
                    }
                });
        });
    });

    dislikeButtons.forEach(function(button) {
        button.addEventListener('click', function() {
            const reviewId = this.getAttribute('data-review-id');
            fetch(`/reviews/dislike/${reviewId}`, {
                method: 'POST',
                headers: {
                    'X-CSRF-TOKEN': document.querySelector('meta[name="_csrf"]').getAttribute('content')
                }
            })
                .then(response => response.json())
                .then(data => {
                    if (data.dislikes !== undefined) {
                        this.querySelector('span').textContent = data.dislikes;
                    }
                    if (data.likes !== undefined) {
                        this.closest('.review').querySelector('.like-btn span').textContent = data.likes;
                    }
                });
        });
    });
});
