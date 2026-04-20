import { $, exists } from "../core/dom.js";

export default function MobileMenu() {
  const btn = $('#hamburger');
  const menu = $('#mobile-menu');

  if (!exists(btn) || !exists(menu)) return;

  let open = false;

  btn.addEventListener('click', () => {
    open = !open;
    menu.classList.toggle('open', open);
    btn.classList.toggle('open', open);
  });
}