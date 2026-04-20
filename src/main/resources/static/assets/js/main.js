// Core
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

// Features globales
import { configurarBotonWhatsApp } from './whatsapp.js';

import { initApp } from "./core/app.js";
	
document.addEventListener("DOMContentLoaded", initApp);

document.addEventListener('menu-ready', e => {
    configurarCarrito();      // ✅ ahora el DOM existe
    initMenu(e.target);
	
});
document.addEventListener("DOMContentLoaded", () => {
    configurarBotonWhatsApp();
});