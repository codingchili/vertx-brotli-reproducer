import {html, render} from '/node_modules/lit-html/lit-html.js';
import {BunnyStyles} from "../component/styles.js";

import '/component/bunny-box.js'
import '/component/bunny-pages.js'
import '/component/bunny-bar.js'
import '/component/bunny-icon.js'

import './page-start.js'
import './region-picker.js'
import './error-dialog.js'
import './offline-view.js'
import './page-login.js'

class AppView extends HTMLElement {

    static get is() {
        return 'app-view';
    }

    constructor() {
        super();
        this.authenticated = false;
        this.music = application.settings.music;
        window.appView = this;
    }

    get template() {
        return html`
            <style>
                ${BunnyStyles.variables}
                ${BunnyStyles.icons}
                :host {
                    display: block;
                    width: 100%;
                    min-height: 100vh;
                }

                @media (max-width: 728px), (max-height: 768px) {
                    region-picker.page-start {
                        display: none;
                    }
                }

                #toolbar {
                    z-index: 100;
                    position: relative;
                }

                error-dialog {
                    z-index: 800;
                }

                #footer {
                    z-index: 100;
                }

                .icon-mute, .icon-sound {
                    width: 32px;
                    height: 32px;
                    max-width: 32px;
                    max-height: 32px;
                }

                #options {
                    position: fixed;
                    bottom: 58px;
                    right: 24px;
                    padding: 0;
                    margin: 0;
                    height: 32px;
                    z-index: 200;
                }

                @keyframes fadeIn {
                    from {
                        opacity: 0;
                    }
                    to {
                        opacity: 1;
                    }
                }

                #video-container {
                    overflow: hidden;
                    height: 100vh;
                    position: absolute;
                    right: 0;
                    bottom: 0;
                    min-width: 100%;
                    min-height: 100%;
                    background-color: #212121;
                    transform: translateX(calc((100% - 100vw) / 2)) translateY(calc((100% - 100vh) / 2));
                }
                
                #background-video {
                    opacity: 0;
                }

                .loaded {
                    animation: fadeIn 1.2s ease;
                }

                #vignette {
                    position: absolute;
                    box-shadow: inset 0 0 128px rgb(0 0 0);
                    top: 0px;
                    left: 0px;
                    right: 0px;
                    bottom: 0px;
                    z-index: 100;
                }

                #header {
                    position: absolute;
                    z-index: 10;
                    top: 0px;
                    left: 0px;
                    right: 0px;
                    bottom: 0px;
                    overflow: hidden;
                }
            </style>

            ${this.view === 'page-start' || this.view === 'page-login' ? html`
                <region-picker class="${this.view}"></region-picker>` : ''}

            ${this.game || !this.view || this.view === 'page-start' || this.view === 'offline-view' ? '' : html`
                <bunny-bar id="toolbar" location="top">
                    <div id="banner" slot="text"></div>
                    ${this.authenticated ? html`
                        <div slot="left" class="icon" @mousedown="${this._logout.bind(this)}">
                            <bunny-icon icon="close"></bunny-icon>
                        </div>
                    ` : html`
                        <div slot="left" class="icon">
                            <bunny-icon @mousedown="${this._home.bind(this)}" icon="home">
                        </div>
                    `}
                </bunny-bar>
            `}

            ${this.game ? null : html`
                <div id="header">
                    <div id="vignette"></div>
                    <div id="video-container">
                        <!-- 30 fps video has about half of the cpu cost compared to 60 -->
                        <video playsinline muted loop autoplay src="images/video/background.webm" id="background-video"
                               @loadeddata="${this._loaded.bind(this)}"></video>
                    </div>
                </div>`}


            <bunny-pages part="pages">
                <div slot="tabs"></div>
                <div slot="pages">
                    <page-start class="layout horizontal center-justified"></page-start>
                    <page-login class="layout horizontal center-justified"></page-login>
                    <game-realms class="layout horizontal center-justified"></game-realms>
                    <game-characters class="layout horizontal center-justified"></game-characters>
                    <patch-download class="layout horizontal center-justified"></patch-download>
                    <game-view></game-view>
                    <offline-view class="layout horizontal center-justified"></offline-view>
                </div>
            </bunny-pages>

            <div id="error-dialog">
                <error-dialog></error-dialog>
            </div>

            ${(this.game || this.view === 'page-start' || !this.view) ? '' : html`
                <bunny-box id="options">
                    <bunny-icon icon="${this.music ? 'sound' : 'mute'}" @mousedown="${this._music.bind(this)}"></bunny-icon>
                </bunny-box>`
            }

            ${this.game || this.view === 'page-start' ? '' : html`
                <bunny-bar id="footer" location="bottom">${this.version}</bunny-bar>`}
        `;
    }

    connectedCallback() {
        this.attachShadow({mode: 'open'});
        this.render();

        application.onError((e) => {
            customElements.whenDefined('error-dialog').then(() => {
                this.render();
                this.shadowRoot.querySelector('error-dialog').open(e);
            });
        });

        this.setupAmbientAudio();

        application.onLogout(() => {
            this.authenticated = false;
            this.render();
        });

        application.onAuthentication(() => {
            this.authenticated = true;
            this.render();
        });

        this.initialView = ((window.inPWA || application.development.skipStart) ? 'page-login' : 'page-start');

        application.subscribe('view', (view) => {
            window.scrollTo(0, 0);
            this.setView(view);
        });

        application.subscribe('banner', (data) => {
            let banner = this.shadowRoot.querySelector('#banner');
            if (banner) {
                banner.textContent = data.text;
            }
            this.version = data.version;
            this.render();
        });

        customElements.whenDefined('bunny-pages').then(() => {
            if (!application.offline) {
                application.publish('view', this.initialView)
            } else {
                application.showOffline();
            }
        });
    }

    _loaded(e) {
        let target = e.currentTarget;

        if (!target.classList.contains('loaded')) {
            // opacity is set to 0 while loading, if not then
            // the image will appear before animation starts playing.
            target.style.opacity = 1;
            target.classList.add('loaded');
        }
    }

    render() {
        render(this.template, this.shadowRoot);
    }

    _home() {
        application.view('page-start');
    }

    _music() {
        this.music = !this.music;
        (this.music) ? this.play() : this.pause();
        application.settings.music = this.music;
        this.render();
    }

    _logout() {
        if (window.inPWA) {
            document.exitFullscreen();
        }
        application.logout();
    }

    setView(view) {
        let pages = this.shadowRoot.querySelector('bunny-pages');
        this.game = (view === 'game-view');
        this.view = view;

        pages.show(this.shadowRoot.querySelector(view));
        this.render();
    }

    setupAmbientAudio() {
        application.onAuthentication(() => {
            this.play();
        });

        application.onLogout(() => {
            this.pause();
        });

        application.onScriptShutdown(() => {
            if (this.ambient) {
                this.ambient.currentTime = 0;
                this.play();
            }
        });
        application.onGameLoaded(this.pause.bind(this));

        application.onSettingsChanged(settings => {
            if (this.ambient) {
                this.ambient.volume = settings.music;
            }
        });
    }

    play() {
        if (!this.ambient) {
            this.ambient = new Audio('/sound/mutable_theme.mp3');
            this.ambient.loop = true;
            this.ambient.volume = application.settings.ambient;

            this.ambient.addEventListener('loadeddata', () => {
                this.play();
            });
        } else {
            if (this.music) {
                this.ambient.play();
            }
        }
    }

    pause() {
        if (this.ambient) {
            this.ambient.pause();
        }
    }

    get gameView() {
        return this.shadowRoot.querySelector('game-view');
    }

    get realmsView() {
        return this.shadowRoot.querySelector('game-realms');
    }

    get startView() {
        return this.shadowRoot.querySelector('page-start');
    }

    get loginView() {
        return this.shadowRoot.querySelector('page-login');
    }

    get characterView() {
        return this.shadowRoot.querySelector('game-characters');
    }

    get offlineView() {
        return this.shadowRoot.querySelector('offline-view');
    }

    get patchView() {
        return this.shadowRoot.querySelector('patch-download');
    }
}

window.customElements.define(AppView.is, AppView);