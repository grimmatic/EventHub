let map, startMarker, endMarker, routeLayer;
let startPoint = null;
const routeColors = {
    car: '#3b82f6',    // blue
    bus: '#10b981',    // green
    foot: '#ef4444'    // red
};

// Haritayı başlat
function initMap(eventLat, eventLng, eventName) {
    // Harita oluştur
    map = L.map('map').setView([eventLat, eventLng], 13);

    // OpenStreetMap layer ekle
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '&copy; <a href="https://openstreetmap.org">OpenStreetMap</a> contributors'
    }).addTo(map);

    // Arama kontrolü ekle
    const geocoder = L.Control.geocoder({
        defaultMarkGeocode: false
    })
        .on('markgeocode', function(e) {
            const latlng = e.geocode.center;
            setStartingPoint(latlng.lat, latlng.lng);
            map.setView(latlng, 13);
        })
        .addTo(map);

    // Etkinlik konumunu işaretle
    endMarker = L.marker([eventLat, eventLng])
        .addTo(map)
        .bindPopup(eventName);

    // Haritaya tıklama olayını dinle
    map.on('click', function(e) {
        setStartingPoint(e.latlng.lat, e.latlng.lng);
    });
}

// Başlangıç noktasını ayarla
function setStartingPoint(lat, lng) {
    // Varsa eski marker'ı kaldır
    if (startMarker) {
        map.removeLayer(startMarker);
    }

    // Yeni marker ekle
    startMarker = L.marker([lat, lng], {
        draggable: true,    // Sürüklenebilir
        icon: L.icon({
            iconUrl: '/images/start-marker.png',  // Özel başlangıç ikonu
            iconSize: [25, 41],
            iconAnchor: [12, 41]
        })
    }).addTo(map);

    // Marker sürüklendiğinde konumu güncelle
    startMarker.on('dragend', function(e) {
        const position = e.target.getLatLng();
        setStartingPoint(position.lat, position.lng);
    });

    startPoint = [lat, lng];
    document.getElementById('startingPointInfo').classList.remove('hidden');

    // Reverse geocoding ile adres bilgisini al
    reverseGeocode(lat, lng);

    // Rota butonlarını aktif et
    enableRouteButtons();
}

// Reverse geocoding
function reverseGeocode(lat, lng) {
    const url = `https://nominatim.openstreetmap.org/reverse?format=json&lat=${lat}&lon=${lng}`;

    fetch(url)
        .then(response => response.json())
        .then(data => {
            const address = data.display_name;
            document.getElementById('startingPointAddress').textContent = address;
        })
        .catch(error => {
            console.error('Adres bulunamadı:', error);
            document.getElementById('startingPointAddress').textContent = `${lat}, ${lng}`;
        });
}

// Rota hesapla
function calculateRoute(mode) {
    if (!startPoint) {
        alert('Lütfen önce başlangıç noktası seçin');
        return;
    }

    // Varsa eski rotayı temizle
    if (routeLayer) {
        map.removeLayer(routeLayer);
    }

    const endPoint = [endMarker.getLatLng().lat, endMarker.getLatLng().lng];

    // OSRM API'yi kullan
    const url = `https://router.project-osrm.org/route/v1/${mode}/${startPoint[1]},${startPoint[0]};${endPoint[1]},${endPoint[0]}?overview=full&geometries=geojson`;

    fetch(url)
        .then(response => response.json())
        .then(data => {
            if (data.code === 'Ok') {
                const route = data.routes[0];

                // Rotayı çiz
                routeLayer = L.geoJSON(route.geometry, {
                    style: {
                        color: routeColors[mode],
                        weight: 6,
                        opacity: 0.7
                    }
                }).addTo(map);

                // Rota bilgilerini göster
                showRouteInfo(route, mode);

                // Haritayı rotaya sığdır
                map.fitBounds(routeLayer.getBounds(), { padding: [50, 50] });
            }
        })
        .catch(error => {
            console.error('Rota hesaplanamadı:', error);
            alert('Rota hesaplanırken bir hata oluştu.');
        });
}

// Rota bilgilerini göster
function showRouteInfo(route, mode) {
    const distance = (route.distance / 1000).toFixed(1);  // km
    let duration = route.duration / 60;  // dakika

    let durationText;
    if (duration > 60) {
        const hours = Math.floor(duration / 60);
        const minutes = Math.round(duration % 60);
        durationText = `${hours} saat ${minutes} dakika`;
    } else {
        durationText = `${Math.round(duration)} dakika`;
    }

    const transportMode = {
        car: 'Arabayla',
        bicycle: 'Bisikletle',
        foot: 'Yürüyerek'
    };

    document.getElementById('routeInfo').classList.remove('hidden');
    document.getElementById('routeDistance').textContent = `${distance} km`;
    document.getElementById('routeDuration').textContent = `${durationText}`;
}

document.getElementById('carButton').onclick = function() {
    calculateRoute('car');
};
document.getElementById('busButton').onclick = function() {
    calculateRoute('bicycle');
};
document.getElementById('walkButton').onclick = function() {
    calculateRoute('foot');
};