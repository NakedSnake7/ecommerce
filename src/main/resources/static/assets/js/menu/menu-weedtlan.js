class MenuWeedTlan extends HTMLElement {

    connectedCallback() {
        this.loadMenuFragment();
    }

    async loadMenuFragment() {
        try {
            const resp = await fetch("/fragmento-menu");
            const html = await resp.text();

            this.innerHTML = html;

            this.runScripts();

            requestAnimationFrame(() => {
                this.dispatchEvent(new CustomEvent("menu-ready", {
                    bubbles: true,
                    composed: true
                }));
            });

        } catch (error) {
            console.error("Error cargando fragmento del menú:", error);

            this.dispatchEvent(new CustomEvent("menu-error", {
                bubbles: true,
                composed: true
            }));
        }
    }

    runScripts() {
        const scripts = this.querySelectorAll("script:not([src])");

        scripts.forEach(oldScript => {
            const newScript = document.createElement("script");
            newScript.type = "module";
            newScript.textContent = oldScript.textContent;

            document.body.appendChild(newScript);
            oldScript.remove();
        });
    }
}

customElements.define("menu-weedtlan", MenuWeedTlan);
