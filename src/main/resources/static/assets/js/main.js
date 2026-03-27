// Core
import './core/navbar.js';
import './core/particles.js';

// Auth
import './auth/register.js';

// Cart
import './cart/cartStore.js';
import { configurarCarrito } from './cart/carrito.js';



// UI
import './ui/footer-year.js';
import './ui/modal.js';
import './ui/maya-loader.js';
import './ui/sombras.js';
import './ui/reviews.js';

// Features globales
import { configurarBotonWhatsApp } from './whatsapp.js';

// Web Component del menú
import './menu/menu-weedtlan.js';

// Init del menú
import { initMenu } from './menu/menu-init.js';

document.addEventListener('menu-ready', e => {
    configurarCarrito();      // ✅ ahora el DOM existe
    initMenu(e.target);
	
});
document.addEventListener("DOMContentLoaded", () => {
    configurarBotonWhatsApp();
});