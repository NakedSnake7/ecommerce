document.addEventListener('DOMContentLoaded', function () {

  const hamburger = document.getElementById('hamburger');
  const mobileNav = document.getElementById('mobileNav');

  if (hamburger && mobileNav) {
	hamburger.addEventListener('click', () => {
	  const isOpen = mobileNav.classList.toggle('open');

	  hamburger.classList.toggle('open', isOpen);
	  hamburger.setAttribute('aria-expanded', isOpen);
	  mobileNav.setAttribute('aria-hidden', !isOpen);
	});

	document.querySelectorAll('.mobile-link').forEach(link => {
	  link.addEventListener('click', () => {
	    hamburger.classList.remove('open');
	    mobileNav.classList.remove('open');
	    hamburger.setAttribute('aria-expanded', 'false');
	    mobileNav.setAttribute('aria-hidden', 'true');
	  });
	});
  }

  const reveals = document.querySelectorAll('.reveal');

  if ('IntersectionObserver' in window) {
    const observer = new IntersectionObserver((entries) => {
      entries.forEach(entry => {
        if (entry.isIntersecting) {
          entry.target.classList.add('visible');
          observer.unobserve(entry.target);
        }
      });
    }, { threshold: 0.1, rootMargin: '0px 0px -40px 0px' });

    reveals.forEach(el => observer.observe(el));
  } else {
    reveals.forEach(el => el.classList.add('visible'));
  }

  document.querySelectorAll('.faq-item').forEach(item => {
    const question = item.querySelector('.faq-q');

    if (question) {
      question.addEventListener('click', () => {
        const isOpen = item.classList.contains('open');

        document.querySelectorAll('.faq-item.open')
          .forEach(i => i.classList.remove('open'));

        if (!isOpen) item.classList.add('open');
      });
    }
  });

  const form = document.getElementById('leadForm');

  if (form) {
    form.addEventListener('submit', async function (e) {
      e.preventDefault();

      const button = form.querySelector('button[type="submit"]');
      const formMessage = document.getElementById('formMsg');
      if (!button) return;

      button.disabled = true;
      button.innerText = 'Enviando...';

      const data = Object.fromEntries(new FormData(form).entries());
      data.source = 'index';

      try {
        const response = await fetch('/api/leads', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify(data)
        });

        if (!response.ok) {
          const errorText = await response.text();
          throw new Error(errorText || 'Error al enviar lead');
        }

        if (formMessage) {
          formMessage.style.display = 'block';
        }

        form.reset();

        if (window.gtag) gtag('event', 'generate_lead');
        if (window.fbq) fbq('track', 'Lead');

      } catch (err) {
        console.error('Lead error:', err);
        alert('Hubo un error al enviar. Intenta por WhatsApp.');
      } finally {
        button.disabled = false;
        button.innerText = 'Enviar solicitud →';
      }
    });
  }

  const header = document.querySelector('header');

  if (header) {
    window.addEventListener('scroll', () => {
      header.style.background = window.scrollY > 20
        ? 'rgba(6,10,20,.95)'
        : 'rgba(6,10,20,.80)';
    }, { passive: true });
  }
});