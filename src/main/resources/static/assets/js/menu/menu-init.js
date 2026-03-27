import { configurarCategorias } from './menu-categorias.js';
import { configurarDescripciones } from './menu-descripcion.js';
import { initCarousels } from './menu-carousels.js';

export function initMenu(root) {
    initCarousels(root);
    configurarCategorias(root);
    configurarDescripciones(root);
}
