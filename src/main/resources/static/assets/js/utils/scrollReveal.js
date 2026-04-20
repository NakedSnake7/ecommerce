import { $$ } from "../core/dom.js";

export default function ScrollReveal() {

  const init = () => {
    const els = $$('.reveal');

    if (!els.length) {
      console.warn("⏳ Aún no hay elementos .reveal, reintentando...");
      setTimeout(init, 100); // reintento controlado
      return;
    }

    console.log("🔥 ScrollReveal activo:", els.length);

    // Mostrar los visibles desde inicio
    els.forEach(el => {
      if (el.getBoundingClientRect().top < window.innerHeight) {
        el.classList.add('visible');
      }
    });

    const observer = new IntersectionObserver((entries) => {
      entries.forEach(entry => {
        if (entry.isIntersecting) {
          entry.target.classList.add('visible');
          observer.unobserve(entry.target);
        }
      });
    }, { threshold: 0.12 });

    els.forEach(el => observer.observe(el));
  };

  // ✅ esperar a que el navegador pinte
  requestAnimationFrame(() => {
    requestAnimationFrame(init);
  });
}