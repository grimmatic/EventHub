

// Önerileri API'den çekme
function fetchRecommendations() {
    // İstek durumunu göster
    const container = document.querySelector('#featured-events');
    container.innerHTML = "<p class='text-center col-span-3 text-gray-600'>Öneriler yükleniyor...</p>";

    fetch('/api/recommendations', {
        cache: 'no-cache',
        headers: {
            'Cache-Control': 'no-cache'
        }
    })
        .then(response => {
            console.log('API Yanıtı:', response);
            if (!response.ok) {
                console.error('API Yanıt Hatası:', response.statusText);
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            return response.json();
        })
        .then(data => {
            console.log('Veri Başarıyla Alındı:', data);
            renderRecommendations(data);
        })
        .catch(error => {
            console.error('Hata:', error.message);
            const container = document.querySelector('#featured-events');
            container.innerHTML = "<p class='text-center col-span-3 text-red-500'>Etkinlikler yüklenirken bir hata oluştu. Lütfen sayfayı yenileyin.</p>";
        });


}

// Etkinlikleri ekrana render etme
function renderRecommendations(events) {
    const container = document.querySelector('#featured-events');
    container.innerHTML = ""; // Önce içeriği temizle

    if (events.length === 0) {
        container.innerHTML = "<p class='text-center col-span-3 text-gray-600'>Henüz önerilecek etkinlik bulunmamaktadır.</p>";
        return;
    }

    events.forEach(event => {
        console.log('Rendering Event:', event); // Hangi etkinliklerin render edildiğini kontrol edin
        const slide = document.createElement('div');
        slide.className = 'swiper-slide';
        const imagePath = `/images/categories/${event.category.toLowerCase()}.jpg`;
        slide.innerHTML = `
            <div class="event-card">
                <img src="${imagePath}"
                     alt="${event.category}"
                     class="event-image">
                <div class="p-6">
                    <div class="flex justify-between items-start mb-4">
                        <div>
                            <h3 class="text-xl font-semibold mb-2">${event.name}</h3>
                            <p class="text-gray-600">${new Date(event.startDate).toLocaleDateString('tr-TR', {
            year: 'numeric',
            month: 'long',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        })}</p>
                        </div>
                        <span class="bg-blue-100 text-blue-800 text-xs font-medium px-2.5 py-0.5 rounded">${event.category}</span>
                    </div>
                    <p class="text-gray-600 mb-4">${event.description}</p>
                    <div class="flex justify-between items-center">
                        <span class="text-gray-500">
                            <svg class="w-4 h-4 inline-block" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z"/>
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 11a3 3 0 11-6 0 3 3 0 016 0z"/>
                            </svg>
                            ${event.location || 'Bilinmeyen Konum'}
                        </span>
                        <a href="/event/${event.id}" class="text-blue-600 hover:text-blue-800 font-medium">Detaylar →</a>
                    </div>
                </div>
            </div>`;
        container.appendChild(slide);
    });

    initializeSwiper(); // Swiper başlat
}

// Swiper'ı başlatma
function initializeSwiper() {
    console.log('Initializing Swiper...'); // Swiper'ın başlatılıp başlatılmadığını kontrol edin
    new Swiper('.swiper', {
        slidesPerView: 3,
        spaceBetween: 20,
        centeredSlides:false,
        autoHeight: false,
        navigation: {
            nextEl: '.swiper-button-next',
            prevEl: '.swiper-button-prev',
        },
        breakpoints: {
            0: {
                slidesPerView: 1,
            },
            768: {
                slidesPerView: 1,
            },
            1024: {
                slidesPerView: 2,
            },
            1280: {
                slidesPerView: 3,
            },
        },
    });
}

// Sayfa yüklendiğinde önerileri yükle
document.addEventListener('DOMContentLoaded', () => {
    fetchRecommendations();
});
