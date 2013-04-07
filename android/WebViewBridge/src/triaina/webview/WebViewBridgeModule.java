package triaina.webview;

import javax.inject.Singleton;

import triaina.injector.AbstractTriainaModule;
import triaina.webview.config.ConfigCache;
import triaina.webview.config.WebViewBridgeAnnotationConfigurator;
import triaina.webview.config.WebViewBridgeConfigurator;

public class WebViewBridgeModule extends AbstractTriainaModule {
    @Override
    protected void configure() {
        bind(WebViewBridgeConfigurator.class).to(WebViewBridgeAnnotationConfigurator.class);
        bind(ConfigCache.class).in(Singleton.class);
        bind(WebViewRestoreManager.class);
    }
}
