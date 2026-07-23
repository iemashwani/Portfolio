/**
 * Portfolio JavaScript - Ashwani Singh
 * Interactive controls for dark mode, scroll progress, navigation, toast notifications & animations.
 */

document.addEventListener('DOMContentLoaded', () => {
  initTheme();
  initScrollProgress();
  initMobileNav();
  initSectionNavigation();
  initActiveNavHighlighting();
  initScrollAnimations();
  initCustomCursor();
});

/* ---------------- 1. Theme Toggle ---------------- */
function initTheme() {
  const themeToggleBtn = document.getElementById('themeToggle');
  const savedTheme = localStorage.getItem('theme');

  // Check saved theme or prefers dark media query
  if (savedTheme === 'dark' || (!savedTheme && window.matchMedia('(prefers-color-scheme: dark)').matches)) {
    document.documentElement.setAttribute('data-theme', 'dark');
  } else {
    document.documentElement.setAttribute('data-theme', 'light');
  }

  themeToggleBtn.addEventListener('click', () => {
    const currentTheme = document.documentElement.getAttribute('data-theme');
    const newTheme = currentTheme === 'dark' ? 'light' : 'dark';

    document.documentElement.setAttribute('data-theme', newTheme);
    localStorage.setItem('theme', newTheme);
    showToast(`Switched to ${newTheme} mode`);
  });
}

/* ---------------- 2. Scroll Progress Bar ---------------- */
function initScrollProgress() {
  const progressBar = document.getElementById('scrollProgress');

  window.addEventListener('scroll', () => {
    const scrollTop = window.scrollY;
    const docHeight = document.documentElement.scrollHeight - window.innerHeight;
    const scrollPercent = docHeight > 0 ? (scrollTop / docHeight) * 100 : 0;

    progressBar.style.width = `${scrollPercent}%`;
  });
}

/* ---------------- 3. Navigation & Section Clicks ---------------- */
function initMobileNav() {
  const mobileToggle = document.getElementById('mobileToggle');
  const navLinks = document.getElementById('navLinks');

  if (mobileToggle && navLinks) {
    mobileToggle.addEventListener('click', () => {
      navLinks.classList.toggle('open');
    });
  }
}

function initSectionNavigation() {
  document.querySelectorAll('[data-section]').forEach(link => {
    link.addEventListener('click', (e) => {
      e.preventDefault();
      const targetId = link.getAttribute('data-section');
      const targetSection = document.getElementById(targetId);

      if (targetSection) {
        targetSection.scrollIntoView({ behavior: 'smooth' });
      }

      const navLinks = document.getElementById('navLinks');
      if (navLinks) navLinks.classList.remove('open');
    });
  });
}

/* ---------------- 4. Active Section Navigation ---------------- */
function initActiveNavHighlighting() {
  const sections = document.querySelectorAll('section[id], main[id]');
  const navLinks = document.querySelectorAll('[data-section]');

  const observerOptions = {
    root: null,
    rootMargin: '-20% 0px -60% 0px',
    threshold: 0
  };

  const observer = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
      if (entry.isIntersecting) {
        const id = entry.target.getAttribute('id');
        navLinks.forEach(link => {
          if (link.getAttribute('data-section') === id) {
            link.classList.add('active');
          } else {
            link.classList.remove('active');
          }
        });
      }
    });
  }, observerOptions);

  sections.forEach(section => observer.observe(section));
}

/* ---------------- 5. Toast Notifications & Copy ---------------- */
function showToast(message) {
  const toast = document.getElementById('toast');
  if (!toast) return;

  toast.textContent = message;
  toast.classList.add('show');

  setTimeout(() => {
    toast.classList.remove('show');
  }, 3000);
}

function copyToClipboard(text, successMessage) {
  navigator.clipboard.writeText(text).then(() => {
    showToast(successMessage || 'Copied to clipboard!');
  }).catch(() => {
    // Fallback copy
    const textarea = document.createElement('textarea');
    textarea.value = text;
    document.body.appendChild(textarea);
    textarea.select();
    document.execCommand('copy');
    document.body.removeChild(textarea);
    showToast(successMessage || 'Copied to clipboard!');
  });
}

/* ---------------- 6. Contact Form Handler (FormSubmit Integration) ---------------- */
function handleFormSubmit(event) {
  event.preventDefault();
  const form = event.target;
  const submitBtn = form.querySelector('.btn-submit');
  const originalBtnContent = `<span>Send Message</span><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="18" height="18"><path d="M22 2L11 13"/><path d="M22 2l-7 20-4-9-9-4 20-7z"/></svg>`;

  submitBtn.disabled = true;
  submitBtn.innerHTML = `<span>Sending Email...</span>`;

  const formData = new FormData(form);

  fetch('https://formsubmit.co/ajax/iemashwani2004@gmail.com', {
    method: 'POST',
    body: formData,
    headers: {
      'Accept': 'application/json'
    }
  })
  .then(res => res.json())
  .then(data => {
    if (data.success === "true" || data.success === true) {
      showToast(`Thank you! Your message has been sent to Ashwani's email.`);
      form.reset();
      submitBtn.disabled = false;
      submitBtn.innerHTML = originalBtnContent;
    } else {
      // If FormSubmit AJAX requires initial activation or server origin, fallback to direct native submit
      showToast(`Redirecting to complete email delivery...`);
      setTimeout(() => {
        form.submit();
      }, 500);
    }
  })
  .catch(error => {
    showToast(`Submitting message...`);
    setTimeout(() => {
      form.submit();
    }, 500);
  });
}

/* ---------------- 7. Scroll Reveal & Fluid Motion ---------------- */
function initScrollAnimations() {
  const fadeElements = document.querySelectorAll('.fade-in, .project-card, .skill-card, .achievement-card, .education-card');

  const observer = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
      if (entry.isIntersecting) {
        entry.target.classList.add('visible');
      }
    });
  }, {
    threshold: 0.1,
    rootMargin: '0px 0px -30px 0px'
  });

  fadeElements.forEach(el => observer.observe(el));
}

/* ---------------- 8. Cyber-Diamond Trailing Cursor System ---------------- */
function initCustomCursor() {
  if (window.matchMedia('(pointer: coarse)').matches) return;

  const dot = document.createElement('div');
  const ring = document.createElement('div');

  dot.className = 'cursor-dot';
  ring.className = 'cursor-ring';

  document.body.appendChild(dot);
  document.body.appendChild(ring);
  document.documentElement.classList.add('has-custom-cursor');

  let mouseX = -100, mouseY = -100;
  let ringX = -100, ringY = -100;

  window.addEventListener('mousemove', (e) => {
    mouseX = e.clientX;
    mouseY = e.clientY;

    // Instant diamond dot positioning
    dot.style.transform = `translate3d(${mouseX - 4.5}px, ${mouseY - 4.5}px, 0) rotate(45deg)`;
  });

  // Smooth lerp (linear interpolation) physics loop
  function renderCursor() {
    ringX += (mouseX - ringX) * 0.18;
    ringY += (mouseY - ringY) * 0.18;
    ring.style.transform = `translate3d(${ringX - 19}px, ${ringY - 19}px, 0) rotate(45deg)`;
    requestAnimationFrame(renderCursor);
  }
  requestAnimationFrame(renderCursor);

  // Click ripple contraction
  window.addEventListener('mousedown', () => ring.classList.add('clicked'));
  window.addEventListener('mouseup', () => ring.classList.remove('clicked'));

  // Magnetic hover state on interactive elements
  const interactives = document.querySelectorAll('a, button, input, textarea, .project-card, .skill-card, .achievement-card, .education-card, .contact-card');

  interactives.forEach(el => {
    el.addEventListener('mouseenter', () => {
      ring.classList.add('hovered');
      dot.classList.add('hovered');
    });
    el.addEventListener('mouseleave', () => {
      ring.classList.remove('hovered');
      dot.classList.remove('hovered');
    });
  });
}
